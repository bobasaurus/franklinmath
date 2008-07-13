package franklinmath.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
//import java.io.*;
//import java.math.*;

import com.Ostermiller.Syntax.*;
//import atp.cHotEqn;

import franklinmath.parser.*;
import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.util.*;
import franklinmath.gui.highlighting.*;

/**
 * The primary gui window of the Franklin Math applicaiton
 * @author Allen Jordan
 */
public class MainWindow extends JFrame {

    protected HighlightedDocument inDocument;
    protected HighlightedDocument outDocument;
    protected FancyTextPane inputPane;
    protected FancyTextPane outputPane;
    private TreeExecutor executor;
    private java.util.concurrent.atomic.AtomicBoolean threadRunning = new java.util.concurrent.atomic.AtomicBoolean();
//    protected cHotEqn hotEqn;

    public MainWindow() {
        setTitle("Franklin Math");

        //set a native look and feel
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }

        //make sure the program closes when the window is exited
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        //create a menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        //menu item for execution
        JMenuItem evaluateMenuItem = new JMenuItem("Evaluate");
        evaluateMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                Evaluate();
            }
        });
        menu.add(evaluateMenuItem);
        //menu item for system settings
        JMenuItem settingsMenuItem = new JMenuItem("Settings");
        settingsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                OpenSettingsDialog();
            }
        });
        menu.add(settingsMenuItem);
        //menu item for the about box
        JMenuItem aboutBoxMenuItem = new JMenuItem("About");
        aboutBoxMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                OpenAboutDialog();
            }
        });
        menu.add(aboutBoxMenuItem);

        //set this frame's menu bar
        setJMenuBar(menuBar);

        inDocument = new HighlightedDocument();
        inDocument.setHighlightStyle(HighlightedDocument.JAVA_STYLE);
        outDocument = new HighlightedDocument();
        outDocument.setHighlightStyle(HighlightedDocument.JAVA_STYLE);
        inputPane = new FancyTextPane(inDocument);
        outputPane = new FancyTextPane(outDocument);
        JScrollPane inputScrollPane = new JScrollPane(inputPane);
        JScrollPane outputScrollPane = new JScrollPane(outputPane);

        inputScrollPane.setPreferredSize(new Dimension(300, 200));
        outputScrollPane.setPreferredSize(new Dimension(300, 200));

        inputPane.addKeyListener(new KeyAdapter() {
            //protected boolean altPressed = false;
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isShiftDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        Evaluate();
                    }
                }
            }
        });

        JButton button = new JButton("Evaluate");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Evaluate();
            }
        });

        //create the latex equation renderer
//        hotEqn = new atp.cHotEqn();

        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbLayout.setConstraints(inputScrollPane, gbc);
        add(inputScrollPane);

        gbc.gridy = 1;
        gbLayout.setConstraints(button, gbc);
        add(button);

        gbc.gridy = 2;
        gbLayout.setConstraints(outputScrollPane, gbc);
        add(outputScrollPane);

        gbc.gridy = 3;
//        gbLayout.setConstraints(hotEqn, gbc);
//        add(hotEqn);

        setLayout(gbLayout);
        pack();

        try {
            FMProperties.LoadProperties();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading in user properties/settings: " + ex.toString());
        }

        try {
            executor = new TreeExecutor();
        } catch (Exception ex) {
            outputPane.Append(ex.toString());
        }

        threadRunning.set(false);
        
    }

    protected Expression ProcessString(String str) throws ParseException, ExecutionException, ExpressionException {
        java.io.StringReader strReader = new java.io.StringReader(str);
        java.io.Reader reader = new java.io.BufferedReader(strReader);
        FMParser parser = new FMParser(reader);
        Vector<FMResult> resultList = executor.Execute(parser.Program());
        if (resultList.size() != 1) {
            throw new ExecutionException("Too many results");
        }
        return resultList.get(0).GetExpression();
    }

    private void OpenSettingsDialog() {
        SettingsDialog settings = new SettingsDialog(this, true);
        settings.setVisible(true);
    }

    private void OpenAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setVisible(true);
    }

    private synchronized void Evaluate() {
        outputPane.setText("");
        if (threadRunning.get()) {
            outputPane.Append("Evaluation thread is already running.  Try again later.  \n");
            return;
        }

        String text = inputPane.getText();
        if (text.length() < 1) {
            outputPane.Append("No input provided.  \n");
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
                        outputPane.Append(exprString);
//                        hotEqn.setEquation(exprString);
                    //DisplayExpression(result.GetExpression());
                    } else if (result.IsEquation()) {
                        String equString = result.GetEquation().toString();
                        outputPane.Append(equString);
                    } else if (result.IsString()) {
                        outputPane.Append(result.GetString());
                    } else {
                        outputPane.Append("Could not display result");
                    }
                    outputPane.Append("\n");
                }
            } catch (Exception ex) {
                outputPane.Append("Error: " + ex.toString() + "\n");
            }
            threadRunning.set(false);
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
}
