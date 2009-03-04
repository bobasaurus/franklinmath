package franklinmath.gui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Image;
import java.util.*;

import franklinmath.parser.*;
import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.plot.*;
import franklinmath.gui.text.*;
import franklinmath.util.*;

/**
 * Main GUI window class for Franklin Math
 * @author  Allen Jordan
 */
public class MainWindow extends javax.swing.JFrame {

    protected boolean isInitialTextCleared = false;
    protected FancyTextPane outputTextPane;
    protected DefaultMutableTreeNode rootFunctionNode;
    //Franklin Math's command execution class
    protected TreeExecutor executor;
    //atomic boolean to help ensure proper concurrency
    protected java.util.concurrent.atomic.AtomicBoolean threadRunning = new java.util.concurrent.atomic.AtomicBoolean();
    protected FunctionInformation functionInformation;
    //table containing function information for documentation lookup
    protected Hashtable<String, FunctionInformation.FunctionInfo> functionInfoTable;
    protected String HTMLBegin = "<html><body>";
    protected String HTMLEnd = "</body></html>";

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();

        //mark that the command execution thread is not currently running
        threadRunning.set(false);

        //setup the output text pane
        outputTextPane = new FancyTextPane(new DefaultStyledDocument());
        outputTextPane.setEditable(false);
        outputScrollPane.getViewport().setView(outputTextPane);
        outputTextPane.Append("Press Shift+Enter to execute math commands entered above, or use the Options->Evaluate menu option.  ");

        //setup the function tree
        rootFunctionNode = new DefaultMutableTreeNode("Available Functions");
        DefaultTreeModel treeModel = new DefaultTreeModel(rootFunctionNode);
        functionTree.setModel(treeModel);

        //make sure the cursor starts in the input text box
        inputTextArea.requestFocus();

        //setup the listener for the shift+enter press on the input text box
        inputTextArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        Evaluate();
                    }
                }
            }
        });

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
            outputTextPane.Append("Evaluation thread is already running.  Try again later.  \n");
            return;
        }

        String text = inputTextArea.getText();
        if (text.length() < 1) {
            outputTextPane.Append("No math commands have been entered.  \n");
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
                for (int i = 0; i < resultList.size(); i++) {
                    FMResult result = resultList.get(i);
                    if (result.IsExpression()) {
                        String exprString = result.GetExpression().toString();
                        outputTextPane.Prepend(exprString + "\n");
//                        hotEqn.setEquation(exprString);
                    //DisplayExpression(result.GetExpression());
                    } else if (result.IsEquation()) {
                        String equString = result.GetEquation().toString();
                        outputTextPane.Prepend(equString + "\n");

                    } else if (result.IsString()) {
                        outputTextPane.Prepend("\"" + result.GetString() + "\"\n");
                    } else if (result.IsImage()) {
                        Image img = result.GetImage();
                        outputTextPane.Prepend(img, true);
                    } else if (result.IsPanel()) {
                        /*JPanel resultPanel = result.GetPanel();
                        resultPanel.setPreferredSize(new Dimension(300, 200));
                        outputTextPanel.add(resultPanel);
                        PackWindow();*/
                    } else {
                        outputTextPane.Prepend("Could not display result\n");
                    }

                }
            } catch (Exception ex) {
                outputTextPane.Append(
                        "Error: " + ex.toString() + "\n");
            }
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
        exitMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        evaluateMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Franklin Math");

        jSplitPane1.setBackground(new java.awt.Color(255, 255, 255));
        jSplitPane1.setDividerLocation(180);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

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
                .addComponent(functionDocumentationScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
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
                .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        exitMenuItem.setMnemonic('e');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        optionsMenu.setMnemonic('o');
        optionsMenu.setText("Options");

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
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
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
    private javax.swing.JMenuItem evaluateMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JTextPane functionDocumentationPane;
    private javax.swing.JScrollPane functionDocumentationScrollPane;
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
    private javax.swing.JMenuItem settingsMenuItem;
    // End of variables declaration//GEN-END:variables
}
