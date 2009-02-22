package franklinmath.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import franklinmath.parser.*;
import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.plot.*;
import franklinmath.util.*;
import franklinmath.gui.text.*;

import javax.swing.text.DefaultStyledDocument;

/**
 * The primary gui window of the Franklin Math computer algebra system.  
 * @author Allen Jordan
 */
public class MainWindowBasic extends JFrame {

    protected DefaultStyledDocument inDocument;
    protected DefaultStyledDocument outDocument;
    protected FancyTextPane inputPane;
    protected FancyTextPane outputPane;
    protected JPanel outputPanel;
    private TreeExecutor executor;
    //atomic boolean to help ensure proper concurrency
    private java.util.concurrent.atomic.AtomicBoolean threadRunning = new java.util.concurrent.atomic.AtomicBoolean();

    public MainWindowBasic() {
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

        inDocument = new DefaultStyledDocument();
        outDocument = new DefaultStyledDocument();
        inputPane = new FancyTextPane(inDocument);
        inputPane.setBackground(Color.WHITE);
        outputPane = new FancyTextPane(outDocument);
        outputPane.setBackground(Color.WHITE);
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
        outputPanel = new JPanel();
        outputPanel.setBackground(Color.WHITE);
        gbLayout.setConstraints(outputPanel, gbc);
        add(outputPanel);

        setLayout(gbLayout);
        pack();

        try {
            //load in the project settings
            FMProperties.LoadProperties();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading in user properties/settings: " + ex.toString());
        }

        try {
            //create the tree executor, loading in the list of built-in functions
            executor = new TreeExecutor();
        } catch (Exception ex) {
            outputPane.Append(ex.toString());
        }

        threadRunning.set(false);

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
        //todo: this panel removal doesn't seem to clear everything
        outputPanel.removeAll();

        if (threadRunning.get()) {
            outputPane.Append("Evaluation thread is already running.  Try again later.  \n");
            return;
        }

        String text = inputPane.getText();
        if (text.length() < 1) {
            outputPane.Append("No input provided.  \n");
            return;
        }
        javax.swing.SwingWorker worker =
                new EvaluationWorker(text);
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
                        outputPane.Append("\"" + result.GetString() + "\"");
                    } else if (result.IsImage()) {
                        Image img = result.GetImage();
                        outputPane.Append(img);
                    } else if (result.IsPanel()) {
                        JPanel resultPanel = result.GetPanel();
                        resultPanel.setPreferredSize(new Dimension(300, 200));
                        outputPanel.add(resultPanel);
                        PackWindow();
                    } else {
                        outputPane.Append("Could not display result");
                    }
                    outputPane.Append("\n");

                }
            } catch (Exception ex) {
                outputPane.Append(
                        "Error: " + ex.toString() + "\n");
            }
            threadRunning.set(false);
        }
    }

    private void PackWindow() {
        this.pack();
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainWindowBasic().setVisible(true);
            }
        });
    }
}
