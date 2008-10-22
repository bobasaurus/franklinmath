/*
 * MainWindow.java
 *
 * Created on November 25, 2007, 6:53 PM
 */
package franklinmath.gui;

import java.util.*;
import javax.swing.*;
import java.math.*;
import java.awt.*;
import franklinmath.parser.*;
import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.util.*;

/**
 * The main GUI window for this program.  
 * @author  Allen Jordan
 */
public class MainWindowOld extends javax.swing.JFrame {

    private TreeExecutor executor;
    private boolean shiftPressed = false;
    private java.util.concurrent.atomic.AtomicBoolean threadRunning = new java.util.concurrent.atomic.AtomicBoolean();

    /** Creates new form MainWindow */
    public MainWindowOld() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }

        initComponents();
        
        //try this trick to get rid of annoying text pane beeps (doesn't seem to work :( )
        //source: http://elliotth.blogspot.com/2007/10/why-is-my-html-jtextpane-beeping-at-me.html
        //inputTextArea.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);

        try {
            FMProperties.LoadProperties();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading in user properties/settings: " + ex.toString());
        }

        try {
            executor = new TreeExecutor();
        }
        catch (Exception ex) {
            outputTextArea.append(ex.toString());
        }

        threadRunning.set(false);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inputScrollPane = new javax.swing.JScrollPane();
        inputEditorPane = new javax.swing.JEditorPane();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        executeButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        optionsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Franklin Math");

        inputEditorPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inputEditorPaneKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                inputEditorPaneKeyReleased(evt);
            }
        });
        inputScrollPane.setViewportView(inputEditorPane);

        outputTextArea.setColumns(20);
        outputTextArea.setEditable(false);
        outputTextArea.setRows(5);
        outputScrollPane.setViewportView(outputTextArea);

        executeButton.setText("Execute");
        executeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("Menu");

        jMenuItem1.setText("Evaluate [shift + enter]");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        optionsMenuItem.setText("Options");
        optionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(optionsMenuItem);

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(aboutMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(executeButton)
                    .addComponent(inputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(outputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(inputScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(executeButton)
                .addGap(18, 18, 18)
                .addComponent(outputScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        Evaluate();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void executeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeButtonActionPerformed
        Evaluate();
    }//GEN-LAST:event_executeButtonActionPerformed

    private void inputEditorPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputEditorPaneKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_SHIFT) {
            shiftPressed = true;
        } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (shiftPressed) {
                Evaluate();
            }
        }
    }//GEN-LAST:event_inputEditorPaneKeyPressed

    private void inputEditorPaneKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputEditorPaneKeyReleased
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
    }//GEN-LAST:event_inputEditorPaneKeyReleased

    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        SettingsDialog dialog = new SettingsDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_optionsMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog dialog = new AboutDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainWindowOld().setVisible(true);
            }
        });
    }

    private synchronized void Evaluate() {
        outputTextArea.setText("");
        if (threadRunning.get()) {
            outputTextArea.append("Evaluation thread is already running.  Try again later.  \n");
            return;
        }

        String text = inputEditorPane.getText();
        if (text.length() < 1) {
            outputTextArea.append("No input provided.  \n");
            return;
        }
        javax.swing.SwingWorker worker = new EvaluationWorker(text);
        worker.execute();

        threadRunning.set(true);

    }

    /**
     * Represents a worker thread that will parse and evaluate the given input code.  
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
                //outputTextArea.append(ex.toString());
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
                        outputTextArea.append(exprString);
                        //DisplayExpression(result.GetExpression());
                    } else if (result.IsEquation()) {
                        String equString = result.GetEquation().toString();
                        outputTextArea.append(equString);
                    } else if (result.IsString()) {
                        outputTextArea.append(result.GetString());
                    } else {
                        outputTextArea.append("Could not display result");
                    }
                    outputTextArea.append("\n");
                }
            } catch (Exception ex) {
                outputTextArea.append("Error: " + ex.toString() + "\n");
            }
            threadRunning.set(false);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton executeButton;
    private javax.swing.JEditorPane inputEditorPane;
    private javax.swing.JScrollPane inputScrollPane;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextArea outputTextArea;
    // End of variables declaration//GEN-END:variables
}