/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

package franklinmath.gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.*;
import java.util.*;

import franklinmath.parser.*;
import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.plot.*;
import franklinmath.gui.text.*;
import franklinmath.util.*;

import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;

/**
 * Main GUI window class for Franklin Math
 * @author  Allen Jordan
 */
public class MainWindow extends javax.swing.JFrame {

    //flag to check if the initial help text (visible on program startup) has been cleared from the screen
    protected boolean isInitialTextCleared = false;
    //output text pane for math command results
    protected FancyTextPane outputTextPane;
    //root node of the GUI's "available functions" tree
    protected DefaultMutableTreeNode rootFunctionNode;
    //Franklin Math's command execution class
    protected TreeExecutor executor;
    //atomic boolean to help ensure proper concurrency
    protected java.util.concurrent.atomic.AtomicBoolean threadRunning = new java.util.concurrent.atomic.AtomicBoolean();
    //class to store function information parsed from an xml input file
    protected FunctionInformation functionInformation;
    //table containing function information for documentation lookup
    protected Hashtable<String, FunctionInformation.FunctionInfo> functionInfoTable;
    //constants for easily constructing html strings
    protected final String HTMLBegin = "<html><body>";
    protected final String HTMLEnd = "</body></html>";
    //object that keeps track of input box undo-ing
    protected UndoManager undoManager;

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();
        
        this.setIconImage((new ImageIcon("icon.png")).getImage());

        //mark that the command execution thread is not currently running
        threadRunning.set(false);

        //setup the output text pane
        outputTextPane = new FancyTextPane(new DefaultStyledDocument());
        outputTextPane.setEditable(false);
        outputScrollPane.getViewport().setView(outputTextPane);
        outputTextPane.setBackground(java.awt.Color.WHITE);
        outputTextPane.Append("Press Shift+Enter to execute math commands entered above, or use the Options->Evaluate menu option.  ");

        //setup the function tree
        rootFunctionNode = new DefaultMutableTreeNode("Available Functions");
        DefaultTreeModel treeModel = new DefaultTreeModel(rootFunctionNode);
        functionTree.setModel(treeModel);

        //make sure the cursor starts in the input text box
        inputTextArea.requestFocus();

        //setup the ability to undo edits to the input box
        undoManager = new UndoManager();
        inputTextArea.getDocument().addUndoableEditListener(undoManager);

        //center this frame on the screen
        this.setLocationRelativeTo(null);

        //attempt to load in the project settings
        try {
            FMProperties.LoadProperties();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading in user properties/settings: " + ex.toString());
        }

        //try to load in in the system function information
        try {
            functionInformation = new FunctionInformation("functions.xml");
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Critical Error loading in system functions: " + ex.toString());
            System.exit(0);
        }

        //display function information in the GUI tree
        Vector<FunctionInformation.FunctionInfo> functionInfoList = functionInformation.GetFunctionList();
        Vector<String> functionCategoryList = functionInformation.GetCategoryList();
        //store the category nodes in a hash table indexec by category name for easy function insertion later
        Hashtable<String, DefaultMutableTreeNode> categoryTable = new Hashtable<String, DefaultMutableTreeNode>();
        //store the function information in a table indexed by function name for easy access later
        functionInfoTable = new Hashtable<String, FunctionInformation.FunctionInfo>();
        for (int categoryIndex = 0; categoryIndex < functionCategoryList.size(); categoryIndex++) {
            String category = functionCategoryList.get(categoryIndex);
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            categoryTable.put(category, categoryNode);
            rootFunctionNode.add(categoryNode);
        }
        //insert the function names into their proper categories in the GUI tree
        for (int functionIndex = 0; functionIndex < functionInfoList.size(); functionIndex++) {
            FunctionInformation.FunctionInfo info = functionInfoList.get(functionIndex);
            functionInfoTable.put(info.name, info);
            DefaultMutableTreeNode functionNode = new DefaultMutableTreeNode(info.name);
            categoryTable.get(info.category).add(functionNode);
        }

        //allow only one selection at a time on the function tree
        functionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //setup the tree selection listener
        functionTree.addTreeSelectionListener(
                new TreeSelectionListener() {

                    @Override
                    public void valueChanged(TreeSelectionEvent e) {

                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) functionTree.getLastSelectedPathComponent();
                        if (node == null) {
                            return;
                        }
                        String nodeName = ((String) node.getUserObject());
                        FunctionSelected(nodeName);
                    }
                });

        //make sure the function table is set in html mode
        functionDocumentationPane.setContentType("text/html");
        functionDocumentationPane.setText(HTMLBegin + "Select available functions in the tree to view documentation.  " + HTMLEnd);

        //expand the root tree node
        TreePath pathToRoot = new TreePath(rootFunctionNode.getPath());
        functionTree.expandPath(pathToRoot);

        try {
            //create the tree executor
            executor = new TreeExecutor(functionInformation);
        } catch (Exception ex) {
            outputTextPane.Append(ex.toString() + "\n");
        }
    }

    //called to display documentation when a function is selected in the GUI tree
    protected void FunctionSelected(String name) {
        if (functionInfoTable.containsKey(name)) {
            FunctionInformation.FunctionInfo info = functionInfoTable.get(name);
            String docHTML = HTMLBegin + "<center><b>" + info.name + "</b></center><br>" + info.description + "<br><b>Example:</b><br>" + info.exampleInput + "<br>" + info.exampleResult + HTMLEnd;
            functionDocumentationPane.setText(docHTML);
            functionDocumentationScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
            //have the documentation window scroll to the top
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    functionDocumentationPane.scrollRectToVisible(new java.awt.Rectangle(0, 0, 1, 1));
                }
            });
        //functionDocumentationScrollPane.getVerticalScrollBar().getModel().setValue(0);
        }
    }

    /**
     * Start evaluating the current set of math commands in the input box.  
     */
    public void Evaluate() {
        //see if we need to clear away the initial instruction text
        if (!isInitialTextCleared) {
            isInitialTextCleared = true;
            outputTextPane.setText("");
        }

        if (threadRunning.get()) {
            outputTextPane.Prepend("Evaluation thread is already running.  Try again later.  \n\n");
            return;
        }

        String text = inputTextArea.getText();
        if (text.length() < 1) {
            outputTextPane.Prepend("No math commands have been entered.  \n\n");
            return;
        }

        //start the worker thread running to execute the math commands
        javax.swing.SwingWorker worker = new EvaluationWorker(text);
        worker.execute();
    }

    /**
     * Represents a worker thread that will parse and evaluate the given input commands.  
     */
    private class EvaluationWorker extends javax.swing.SwingWorker<Vector<FMResult>, Void> {

        private String inputStr;

        public EvaluationWorker(String input) {
            inputStr = input;
        }

        @Override
        public Vector<FMResult> doInBackground() {
            threadRunning.set(true);
            Vector<FMResult> resultList = new Vector<FMResult>();

            try {
                java.io.StringReader strReader = new java.io.StringReader(inputStr);
                java.io.Reader reader = new java.io.BufferedReader(strReader);
                FMParser parser = new FMParser(reader);

                SimpleNode node = parser.Program();

                //DisplayAST(node, strList, "");

                resultList = executor.Execute(node);

            } catch (Exception ex) {
                //outputPane.append(ex.toString());
                resultList.add(new FMResult(ex.toString()));
            }

            return resultList;
        }

        @Override
        protected void done() {

            try {
                Vector<FMResult> resultList = get();
                if (resultList.size() > 0) {
                    //maintain a visual separation between separate execution results
                    outputTextPane.Prepend("\n");
                }

                //keep track of string insertion location
                int insertLocation = 0;

                for (int i = 0; i < resultList.size(); i++) {
                    FMResult result = resultList.get(i);
                    if (result.IsExpression()) {
                        String exprString = result.GetExpression().toString() + "\n";
                        outputTextPane.InsertAt(exprString, insertLocation);
                        insertLocation += exprString.length();
                    } else if (result.IsEquation()) {
                        String equString = result.GetEquation().toString() + "\n";
                        outputTextPane.InsertAt(equString, insertLocation);
                        insertLocation += equString.length();
                    } else if (result.IsString()) {
                        String str = "\"" + result.GetString() + "\"\n";
                        outputTextPane.InsertAt(str, insertLocation);
                        insertLocation += str.length();
                    } else if (result.IsImage()) {
                        Image img = result.GetImage();
                        outputTextPane.InsertAt(img, insertLocation, true);
                        insertLocation += 2;
                    } else {
                        String errorString = "Could not display result\n";
                        outputTextPane.InsertAt(errorString, insertLocation);
                        insertLocation += errorString.length();
                    }
                }

            } catch (Exception ex) {
                outputTextPane.Prepend("Error: " + ex.toString() + "\n");
            }

            //scroll to the top of the output text area
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    outputTextPane.scrollRectToVisible(new java.awt.Rectangle(0, 0, 1, 1));
                }
            });

            threadRunning.set(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        functionTree = new javax.swing.JTree();
        functionDocumentationScrollPane = new javax.swing.JScrollPane();
        functionDocumentationPane = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inputTextArea = new javax.swing.JTextArea();
        outputScrollPane = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        printMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        examplesMenu = new javax.swing.JMenu();
        basicMathExampleMenuItem = new javax.swing.JMenuItem();
        customVariablesExampleMenuItem = new javax.swing.JMenuItem();
        customFunctionsExampleMenuItem = new javax.swing.JMenuItem();
        functionPlottingExampleMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        evaluateMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();
        clearOutputMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Franklin Math");

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(180);

        jPanel1.setBackground(new java.awt.Color(240, 250, 250));

        jScrollPane2.setViewportView(functionTree);

        functionDocumentationPane.setEditable(false);
        functionDocumentationScrollPane.setViewportView(functionDocumentationPane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(functionDocumentationScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(functionDocumentationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        inputTextArea.setColumns(20);
        inputTextArea.setRows(5);
        inputTextArea.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane1.setViewportView(inputTextArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outputScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        printMenuItem.setMnemonic('p');
        printMenuItem.setText("Print");
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(printMenuItem);

        exitMenuItem.setMnemonic('e');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setMnemonic('u');
        undoMenuItem.setText("Undo");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setMnemonic('r');
        redoMenuItem.setText("Redo");
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);

        jMenuBar1.add(editMenu);

        examplesMenu.setMnemonic('e');
        examplesMenu.setText("Examples");

        basicMathExampleMenuItem.setMnemonic('b');
        basicMathExampleMenuItem.setText("Basic Math");
        basicMathExampleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                basicMathExampleMenuItemActionPerformed(evt);
            }
        });
        examplesMenu.add(basicMathExampleMenuItem);

        customVariablesExampleMenuItem.setMnemonic('v');
        customVariablesExampleMenuItem.setText("Custom Variables");
        customVariablesExampleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customVariablesExampleMenuItemActionPerformed(evt);
            }
        });
        examplesMenu.add(customVariablesExampleMenuItem);

        customFunctionsExampleMenuItem.setMnemonic('f');
        customFunctionsExampleMenuItem.setText("Custom Functions");
        customFunctionsExampleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customFunctionsExampleMenuItemActionPerformed(evt);
            }
        });
        examplesMenu.add(customFunctionsExampleMenuItem);

        functionPlottingExampleMenuItem.setMnemonic('p');
        functionPlottingExampleMenuItem.setText("Function Plotting");
        functionPlottingExampleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                functionPlottingExampleMenuItemActionPerformed(evt);
            }
        });
        examplesMenu.add(functionPlottingExampleMenuItem);

        jMenuBar1.add(examplesMenu);

        optionsMenu.setMnemonic('o');
        optionsMenu.setText("Options");

        evaluateMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.SHIFT_MASK));
        evaluateMenuItem.setMnemonic('e');
        evaluateMenuItem.setText("Evaluate");
        evaluateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evaluateMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(evaluateMenuItem);

        settingsMenuItem.setMnemonic('s');
        settingsMenuItem.setText("Settings");
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(settingsMenuItem);

        clearOutputMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        clearOutputMenuItem.setMnemonic('c');
        clearOutputMenuItem.setText("Clear Output");
        clearOutputMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearOutputMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(clearOutputMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(aboutMenuItem);

        jMenuBar1.add(optionsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void evaluateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateMenuItemActionPerformed
    Evaluate();
}//GEN-LAST:event_evaluateMenuItemActionPerformed

private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
    SettingsDialog settings = new SettingsDialog(this, true);
    settings.setVisible(true);
}//GEN-LAST:event_settingsMenuItemActionPerformed

private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
    AboutDialog about = new AboutDialog(this, true);
    about.setVisible(true);
}//GEN-LAST:event_aboutMenuItemActionPerformed

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
    System.exit(0);
}//GEN-LAST:event_exitMenuItemActionPerformed

private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
    try {
        undoManager.undo();
    } catch (CannotUndoException ex) {
    }
}//GEN-LAST:event_undoMenuItemActionPerformed

private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
    try {
        undoManager.redo();
    } catch (CannotRedoException ex) {
    }
}//GEN-LAST:event_redoMenuItemActionPerformed

private void clearOutputMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearOutputMenuItemActionPerformed
    outputTextPane.setText("");
}//GEN-LAST:event_clearOutputMenuItemActionPerformed

private void basicMathExampleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_basicMathExampleMenuItemActionPerformed
    //example math commands for basic numeric and symbolic arithmetic
    inputTextArea.setText("\"Numeric Arithmetic:\"\n7+6\n2.7-1.1+2*3^2\n\"Symbolic Arithmetic:\"\nvar1+var1+var2^3/var2");
}//GEN-LAST:event_basicMathExampleMenuItemActionPerformed

private void customVariablesExampleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customVariablesExampleMenuItemActionPerformed
    //example math commands for custom variables
    inputTextArea.setText("\"Custom User-Defined Variables:\"\nvar1 = 12+a\nvar1-a");
}//GEN-LAST:event_customVariablesExampleMenuItemActionPerformed

private void customFunctionsExampleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customFunctionsExampleMenuItemActionPerformed
    //example math commands for custom functions
    inputTextArea.setText("\"Custom User-Defined Functions:\"\nfunction[x] = 3x^2 + 2x + 1\nfunction2[x, y] = x+y\nfunction[2]\nfunction2[3, 4]");
}//GEN-LAST:event_customFunctionsExampleMenuItemActionPerformed

private void functionPlottingExampleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_functionPlottingExampleMenuItemActionPerformed
    //example math commands for function plotting
    inputTextArea.setText("\"Function Plotting:\"\nPlot[Sin[x], {x, -2Pi[], 2Pi[]}]\nPlot[2x^3+1, {x, -10, 10}]");
}//GEN-LAST:event_functionPlottingExampleMenuItemActionPerformed

private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMenuItemActionPerformed
    try {
        //open a dialog that can print the contents of the output pane
        outputTextPane.print();
    } catch (PrinterException ex) {
        JOptionPane.showMessageDialog(this, ex.toString(), "Printing Error", JOptionPane.ERROR_MESSAGE);
    }
}//GEN-LAST:event_printMenuItemActionPerformed

    /**
     * The static code entry point, which invokes this MainWindow class to run the GUI
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            //set a look and feel to match the current system
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem basicMathExampleMenuItem;
    private javax.swing.JMenuItem clearOutputMenuItem;
    private javax.swing.JMenuItem customFunctionsExampleMenuItem;
    private javax.swing.JMenuItem customVariablesExampleMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem evaluateMenuItem;
    private javax.swing.JMenu examplesMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextPane functionDocumentationPane;
    private javax.swing.JScrollPane functionDocumentationScrollPane;
    private javax.swing.JMenuItem functionPlottingExampleMenuItem;
    private javax.swing.JTree functionTree;
    private javax.swing.JTextArea inputTextArea;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
}
