/*
 * TrainingWindow.java
 *
 * Created on May 29, 2006, 12:43 PM
 */

package lambdacalc.gui;

import lambdacalc.logic.*;
import lambdacalc.exercises.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.TitledBorder;

import java.util.*;
import java.io.*;

/**
 *
 * @author  tauberer
 */
public class TrainingWindow extends JFrame {

    
    // If you edit this, don't add a dot, it messes up the ExerciseFileFilter
    public static final String SERIALIZED_FILE_SUFFIX = "lbd"; 
    
    public static final ImageIcon UNSOLVED_FILE_ICON = new ImageIcon("images/logo.gif");
    public static final ImageIcon SOLVED_FILE_ICON   = new ImageIcon("images/logo_green.gif");
    
    ExerciseFile exFile; // this is null if no file has been loaded yet
    
    Exercise ex;
    int currentGroup = 0, currentEx = 0;
    ExerciseTreeModel treemodel;
    boolean updatingTree = false;
    
    int wrongInARowCount = 0;
    
    boolean hasUserSaved; // true iff the user has saved this exercise into a .lbd file (i.e. the Save menu is enabled if there are unsaved changes)
    boolean hasUnsavedWork; // true iff the user has entered any unsaved work
    File usersWorkFile = null;

    private static final ExerciseFileFilter onlyTextFiles = new ExerciseFileFilter
            ("txt", "Exercise files");
    private static final ExerciseFileFilter onlySerializedFiles = new ExerciseFileFilter
            (SERIALIZED_FILE_SUFFIX, "Saved work");
    private static final ExerciseFileFilter allRecognizedFiles = new ExerciseFileFilter
            (new String[]{"txt",SERIALIZED_FILE_SUFFIX}, "All recognized files");    
    

    private static TrainingWindow singleton=null;
    
    static TrainingWindow getSingleton() {
        return singleton;
    }
    
    public static void prepareWindow() {
        if (singleton == null) {
            singleton = new TrainingWindow();
        }
    }
    
    public static void showWindow() {
        prepareWindow();
        singleton.show();
    }
    
    
    
    public static void initializeJFileChooser(JFileChooser chooser, boolean includeTextFiles, boolean includeSerializedFiles) {
        ExerciseFileView fileView = new ExerciseFileView();
    
        fileView.putTypeDescription("lbd", "Lambda file");
        chooser.setFileView(fileView);
 
        if (includeTextFiles && includeSerializedFiles) chooser.addChoosableFileFilter(allRecognizedFiles);
        if (includeTextFiles) chooser.addChoosableFileFilter(onlyTextFiles);
        if (includeSerializedFiles) chooser.addChoosableFileFilter(onlySerializedFiles);
        
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    
    }
    
    /** Creates new form TrainingWindow */
    public TrainingWindow() {
        
        initLookAndFeel();
        initComponents(); // calls the uneditable Netbeans-generated code
        
        initializeJFileChooser(jFileChooser1, true, true);
        
        jTreeExerciseFile.setFont(Util.getUnicodeFont(14));

        lblHelpLambda.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinders.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinaries.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpNot.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpConditionals.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));

        lblHelpLambda.setText("Type capital L for " + Lambda.SYMBOL);
        lblHelpBinders.setText("Type capital A, E, and I for " + ForAll.SYMBOL + ", " + Exists.SYMBOL + ", and " + Iota.SYMBOL);
        lblHelpBinaries.setText("Type & for " + And.SYMBOL + " and | for " + Or.SYMBOL);
        lblHelpNot.setText("Type the tilde (~) for " + Not.SYMBOL);
        lblHelpConditionals.setText("Type -> for " + If.SYMBOL + " and <-> for " + Iff.SYMBOL);

        clearAllControls();
        
        //loadExerciseFile("tests/example1.txt");
    }
    
    // called by TrainingWindow constructor
    private void clearAllControls() {
        hasUserSaved = false;
        hasUnsavedWork = false;
        usersWorkFile = null;
        exFile = null;
        ex = null;
        
        this.jTreeExerciseFile.setModel(new javax.swing.tree.DefaultTreeModel(new javax.swing.tree.DefaultMutableTreeNode("No exercise file opened")));

        lblDirections.setText("Open an exercise file from your instructor by using the File menu above.");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnDoAgain.setVisible(false);
        
        TitledBorder tb = (TitledBorder)txtQuestion.getBorder();
        tb.setTitle("Current Problem");
        
        txtUserAnswer.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        txtUserAnswer.setEnabled(false);
        btnCheckAnswer.setEnabled(false);

        txtUserAnswer.setText("");
        txtFeedback.setText("");

        lblIdentifierTypes.setVisible(false);
        
        menuItemSave.setEnabled(false);
        menuItemSaveAs.setEnabled(false);
    }
    
    private void loadExerciseFile(String filename) {
        loadExerciseFile(new File(filename));       
    }
    
    // used in loadExerciseFile()
    private ExerciseFile parse(File f) throws IOException, ExerciseFileFormatException {
        return new ExerciseFileParser().parse(new FileReader(f));
    }
    
    
    // used in ExerciseFileView.getIcon()
    /**
     * Returns true if the file contains a completed exercise, and 
     * false if it contains either an uncompleted exercise, or no
     * exercise
     */
    public static boolean hasBeenCompleted(File f) throws IOException, ExerciseFileFormatException {
        // currently, check for serialization is a simple extension check,
        // so we first use that
        if (!isSerialized(f)) {
            return false;   
        }
        ExerciseFile temp = new ExerciseFile(f);
        return temp.hasBeenCompleted();    
    }
    
    public static boolean isSerialized(File f) {
        // TODO On Mac OS, suffixes aren't normally used to distinguish files - 
        // what do we do about that?
        return f.toString().endsWith("."+SERIALIZED_FILE_SUFFIX);
    }
    
    // used in:
    // loadExerciseFile(String)
    // menuItemOpenActionPerformed()
    private void loadExerciseFile(File f) {
        boolean isWorkFile = false;

        try {
            if (isSerialized(f)) {
                this.exFile = new ExerciseFile(f);
                isWorkFile = true;
            } else {
                this.exFile = parse(f);
            }
            menuItemSaveAs.setEnabled(true);
            menuItemSave.setEnabled(false);
        } catch (IOException e) { // thrown by deserialize and parse
            e.printStackTrace();
            JOptionPane.showMessageDialog
                    (this, "There was an error opening the exercise file: " + (e.getMessage() == null ? "Unknown read error." : e.getMessage()),
                    "Error loading exercise file", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (ExerciseFileFormatException e) { // thrown by parse
            e.printStackTrace();
            JOptionPane.showMessageDialog
                    (this, "The exercise file couldn't be read: " + e.getMessage(), // e.g. typo 
                    "Error loading exercise file", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (this.exFile.hasBeenCompleted()) {
            JOptionPane.showMessageDialog
                    (this, "All the exercises in this file have already been solved.",
                    "File already completed", JOptionPane.INFORMATION_MESSAGE);
        }
        
        if (isWorkFile) {
            usersWorkFile = f;
            hasUserSaved = true;
            hasUnsavedWork = false;
        } else {
            hasUserSaved = false;
            hasUnsavedWork = false;

            // Construct a suggested .lbd file name for saving this exercise file.
            String fn = f.getPath();
            if (fn.toLowerCase().endsWith(".txt"))
                fn = fn.substring(0, fn.length() - 4);
            fn = fn + "." + SERIALIZED_FILE_SUFFIX;

            usersWorkFile = new File(fn);
        }
        
        this.treemodel = new ExerciseTreeModel(this.exFile);
        this.jTreeExerciseFile.setModel(this.treemodel);
        //this.jTreeExerciseFile.setCellRenderer(new ExerciseTreeRenderer());
        for (int i = 0; i < this.jTreeExerciseFile.getRowCount(); i++) {
            this.jTreeExerciseFile.expandRow(i);
            this.jTreeExerciseFile.setRowHeight(this.jTreeExerciseFile.getFont().getBaselineFor('A'));
        }

        showFirstExercise();
    }
    
    
    private String chopFileSuffix(String s) {
        int dotposition = s.lastIndexOf('.'); // last dot
        if (dotposition == 0 || dotposition == -1) { // initial dot or no dot
            return s;
        } else {
            return s.substring(0, dotposition-1); // remove dot and everything after it
        }
    }
    
    // called in loadExerciseFile()
    private void showFirstExercise() {
        currentGroup = 0;
        currentEx = 0;
        showExercise();
    }
    
    Exercise getCurrentExercise() {
        if (exFile == null) return null;
        return exFile.getGroup(currentGroup).getItem(currentEx);
    }
    
    // called in:
    // showFirstExercise()
    // btnDoAgainActionPerformed()
    // onExerciseTreeValueChanged()
    // btnNextActionPerformed()
    // btnPrevActionPerformed()
    private void showExercise() {
        lblDirections.setText(exFile.getGroup(currentGroup).getDirections());
        ex = getCurrentExercise();

        btnPrev.setEnabled(currentEx > 0 || currentGroup > 0);
        btnNext.setEnabled(currentEx+1 < exFile.getGroup(currentGroup).size() || currentGroup+1 < exFile.size() );
        
        TitledBorder tb = (TitledBorder)txtQuestion.getBorder();
        tb.setTitle("Current Problem: " + ex.getShortDirective());
        
        setAnswerEnabledState();
        setQuestionText();

        if (txtUserAnswer.isEnabled()) {
            txtUserAnswer.requestFocus();
        }

        try {
            updatingTree = true; // this prevents recursion when the event is
                                 // fired as if the user is clicking on this node
            jTreeExerciseFile.setSelectionPath(new TreePath(new Object[] { exFile, new ExerciseTreeModel.ExerciseGroupWrapper(exFile.getGroup(currentGroup)), new ExerciseTreeModel.ExerciseWrapper(ex) } ));
        } finally {
            updatingTree = false;
        }

        if (ex instanceof HasIdentifierTyper) {
            lblIdentifierTypes.setVisible(true);
            IdentifierTyper typer = ((HasIdentifierTyper)ex).getIdentifierTyper();
            lblIdentifierTypes.setText("Use the following typing conventions:\n" + typer.toString());
        } else {
            lblIdentifierTypes.setVisible(false);
        }

        wrongInARowCount = 0;
    }
    
    // called in showExercise()
    private void setQuestionText() {
        if (!ex.isDone()) {
            String lastAnswer = ex.getLastAnswer();
            if (lastAnswer == null) {
                txtQuestion.setText(ex.getExerciseText());
                txtUserAnswer.setTemporaryText(ex.getTipForTextField());
                txtFeedback.setText("You have not yet started this exercise.");
            } else {
                txtQuestion.setText(lastAnswer);
                txtUserAnswer.setText(lastAnswer);
                txtFeedback.setText("You have started this exercise but have not completed it yet.");
            }
        } else {
            txtUserAnswer.setText(ex.getLastAnswer());
            txtFeedback.setText("You have already solved this exercise.");
        }
    }
    
    // called in:
    // showExercise()
    // onCheckAnswer()
    private void setAnswerEnabledState() {
        if (!ex.isDone()) {
            txtUserAnswer.setBackground(UIManager.getColor("TextField.background"));
            txtUserAnswer.setEnabled(true);
            btnCheckAnswer.setEnabled(true);
        } else {
            txtUserAnswer.setBackground(UIManager.getColor("TextField.inactiveBackground"));
            txtUserAnswer.setEnabled(false);
            btnCheckAnswer.setEnabled(false);
        }
        btnDoAgain.setVisible(ex.hasBeenStarted());
    }
    
    // called in TrainingWindow() constructor
    private static void initLookAndFeel() {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        
	try {
	    UIManager.setLookAndFeel(lookAndFeel);
	} catch (ClassNotFoundException e) {
	    System.err.println("Couldn't find class for specified look and feel:"
			       + lookAndFeel);
	    System.err.println("Did you include the L&F library in the class path?");
	    System.err.println("Using the default look and feel.");
	} catch (UnsupportedLookAndFeelException e) {
	    System.err.println("Can't use the specified look and feel ("
			       + lookAndFeel
			       + ") on this platform.");
	    System.err.println("Using the default look and feel.");
	} catch (Exception e) {
	    System.err.println("Couldn't get specified look and feel ("
			       + lookAndFeel
			       + "), for some reason.");
	    System.err.println("Using the default look and feel.");
	    e.printStackTrace();
	}
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jPanelRightHalf = new javax.swing.JPanel();
        jPanelRightHalf2 = new javax.swing.JPanel();
        btnCheckAnswer = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        jScrollPaneFeedback = new javax.swing.JScrollPane();
        txtFeedback = new javax.swing.JTextArea();
        jScrollPaneDirections = new javax.swing.JScrollPane();
        lblDirections = new javax.swing.JTextArea();
        txtUserAnswer = new lambdacalc.gui.LambdaEnabledTextField();
        lblIdentifierTypes = new javax.swing.JTextArea();
        txtQuestion = new lambdacalc.gui.LambdaEnabledTextField();
        btnDoAgain = new javax.swing.JButton();
        jSplitPaneLeftHalf = new javax.swing.JSplitPane();
        jScrollPaneUpperLeft = new javax.swing.JScrollPane();
        jTreeExerciseFile = new javax.swing.JTree();
        jPanelEnterExpressions = new javax.swing.JPanel();
        lblHelpHeader = new javax.swing.JTextArea();
        lblHelpLambda = new javax.swing.JLabel();
        lblHelpBinders = new javax.swing.JLabel();
        lblHelpBinaries = new javax.swing.JLabel();
        lblHelpNot = new javax.swing.JLabel();
        lblHelpConditionals = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemOpen = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        menuItemQuit = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuItemTeacherTool = new javax.swing.JMenuItem();
        menuItemScratchPad = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Lambda");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosed(evt);
            }
        });

        jSplitPaneMain.setDividerLocation(300);
        jPanelRightHalf.setLayout(new java.awt.GridLayout(1, 0));

        jPanelRightHalf.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        btnCheckAnswer.setText("Check Answer");
        btnCheckAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onCheckAnswer(evt);
            }
        });

        btnPrev.setText("< Previous Problem");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        btnNext.setText("Next Problem >");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        jScrollPaneFeedback.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneFeedback.setBorder(javax.swing.BorderFactory.createTitledBorder("Feedback"));
        txtFeedback.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtFeedback.setColumns(20);
        txtFeedback.setEditable(false);
        txtFeedback.setFont(new java.awt.Font("SansSerif", 0, 12));
        txtFeedback.setLineWrap(true);
        txtFeedback.setRows(5);
        txtFeedback.setWrapStyleWord(true);
        txtFeedback.setBorder(null);
        jScrollPaneFeedback.setViewportView(txtFeedback);

        jScrollPaneDirections.setBorder(null);
        lblDirections.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblDirections.setColumns(20);
        lblDirections.setEditable(false);
        lblDirections.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblDirections.setLineWrap(true);
        lblDirections.setRows(5);
        lblDirections.setWrapStyleWord(true);
        lblDirections.setBorder(javax.swing.BorderFactory.createTitledBorder("Directions"));
        jScrollPaneDirections.setViewportView(lblDirections);

        txtUserAnswer.setFont(new java.awt.Font("Serif", 0, 18));
        txtUserAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserAnswerActionPerformed(evt);
            }
        });

        lblIdentifierTypes.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblIdentifierTypes.setColumns(20);
        lblIdentifierTypes.setEditable(false);
        lblIdentifierTypes.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblIdentifierTypes.setLineWrap(true);
        lblIdentifierTypes.setRows(5);
        lblIdentifierTypes.setText(" ");
        lblIdentifierTypes.setWrapStyleWord(true);
        lblIdentifierTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Conventions about letters"));

        txtQuestion.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Problem"));
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new java.awt.Font("Serif", 0, 18));

        btnDoAgain.setText("Do Problem Again");
        btnDoAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoAgainActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanelRightHalf2Layout = new org.jdesktop.layout.GroupLayout(jPanelRightHalf2);
        jPanelRightHalf2.setLayout(jPanelRightHalf2Layout);
        jPanelRightHalf2Layout.setHorizontalGroup(
            jPanelRightHalf2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelRightHalf2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelRightHalf2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblIdentifierTypes)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelRightHalf2Layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(btnCheckAnswer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
                    .add(txtUserAnswer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                    .add(jScrollPaneFeedback, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelRightHalf2Layout.createSequentialGroup()
                        .add(btnPrev)
                        .add(70, 70, 70)
                        .add(btnDoAgain)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 102, Short.MAX_VALUE)
                        .add(btnNext))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelRightHalf2Layout.createSequentialGroup()
                        .add(jPanelRightHalf2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, txtQuestion, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                            .add(jScrollPaneDirections, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        jPanelRightHalf2Layout.setVerticalGroup(
            jPanelRightHalf2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelRightHalf2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPaneDirections, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(txtQuestion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(7, 7, 7)
                .add(txtUserAnswer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnCheckAnswer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(3, 3, 3)
                .add(jScrollPaneFeedback, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 127, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelRightHalf2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnNext)
                    .add(btnPrev)
                    .add(btnDoAgain))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblIdentifierTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .add(32, 32, 32))
        );
        jPanelRightHalf.add(jPanelRightHalf2);

        jSplitPaneMain.setRightComponent(jPanelRightHalf);

        jSplitPaneLeftHalf.setDividerLocation(300);
        jSplitPaneLeftHalf.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jTreeExerciseFile.setFont(new java.awt.Font("Serif", 0, 14));
        jTreeExerciseFile.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                onExerciseTreeValueChanged(evt);
            }
        });

        jScrollPaneUpperLeft.setViewportView(jTreeExerciseFile);

        jSplitPaneLeftHalf.setTopComponent(jScrollPaneUpperLeft);

        jPanelEnterExpressions.setBorder(javax.swing.BorderFactory.createTitledBorder("How to Enter Expressions"));
        lblHelpHeader.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblHelpHeader.setColumns(20);
        lblHelpHeader.setEditable(false);
        lblHelpHeader.setLineWrap(true);
        lblHelpHeader.setRows(5);
        lblHelpHeader.setText("When typing lambda expressions, use the following keyboard shortcuts:");
        lblHelpHeader.setWrapStyleWord(true);
        lblHelpHeader.setBorder(null);

        lblHelpLambda.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpLambda.setText(" ");

        lblHelpBinders.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpBinders.setText(" ");

        lblHelpBinaries.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpBinaries.setText(" ");

        lblHelpNot.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpNot.setText(" ");

        lblHelpConditionals.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpConditionals.setText(" ");

        org.jdesktop.layout.GroupLayout jPanelEnterExpressionsLayout = new org.jdesktop.layout.GroupLayout(jPanelEnterExpressions);
        jPanelEnterExpressions.setLayout(jPanelEnterExpressionsLayout);
        jPanelEnterExpressionsLayout.setHorizontalGroup(
            jPanelEnterExpressionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEnterExpressionsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelEnterExpressionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblHelpHeader)
                    .add(jPanelEnterExpressionsLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jPanelEnterExpressionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(lblHelpBinders, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .add(lblHelpBinaries, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .add(lblHelpLambda, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblHelpNot, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblHelpConditionals, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelEnterExpressionsLayout.setVerticalGroup(
            jPanelEnterExpressionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelEnterExpressionsLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblHelpHeader, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHelpLambda)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHelpBinders)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHelpBinaries)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHelpNot)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblHelpConditionals)
                .addContainerGap(145, Short.MAX_VALUE))
        );
        jSplitPaneLeftHalf.setRightComponent(jPanelEnterExpressions);

        jSplitPaneMain.setLeftComponent(jSplitPaneLeftHalf);

        getContentPane().add(jSplitPaneMain);

        menuFile.setMnemonic('F');
        menuFile.setText("File");
        menuFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFileActionPerformed(evt);
            }
        });

        menuItemOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        menuItemOpen.setMnemonic('O');
        menuItemOpen.setText("Open...");
        menuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenActionPerformed(evt);
            }
        });

        menuFile.add(menuItemOpen);

        menuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        menuItemSave.setMnemonic('S');
        menuItemSave.setText("Save");
        menuItemSave.setEnabled(false);
        menuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveActionPerformed(evt);
            }
        });

        menuFile.add(menuItemSave);

        menuItemSaveAs.setMnemonic('a');
        menuItemSaveAs.setText("Save As...");
        menuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveAsActionPerformed(evt);
            }
        });

        menuFile.add(menuItemSaveAs);

        menuFile.add(jSeparator1);

        menuItemQuit.setMnemonic('x');
        menuItemQuit.setText("Exit Exercise Solver");
        menuItemQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemQuitActionPerformed(evt);
            }
        });

        menuFile.add(menuItemQuit);

        jMenuBar1.add(menuFile);

        menuTools.setMnemonic('T');
        menuTools.setText("Tools");
        menuItemTeacherTool.setText("Teacher Tool...");
        menuItemTeacherTool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemTeacherToolActionPerformed(evt);
            }
        });

        menuTools.add(menuItemTeacherTool);

        menuItemScratchPad.setMnemonic('S');
        menuItemScratchPad.setText("Scratch Pad...");
        menuItemScratchPad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemScratchPadActionPerformed(evt);
            }
        });

        menuTools.add(menuItemScratchPad);

        jMenuBar1.add(menuTools);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemScratchPadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemScratchPadActionPerformed
        ScratchPadWindow.showWindow();
    }//GEN-LAST:event_menuItemScratchPadActionPerformed

    private void btnDoAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoAgainActionPerformed
        ex.reset();
        flagChangeMade();
        jTreeExerciseFile.repaint();
        showExercise();
    }//GEN-LAST:event_btnDoAgainActionPerformed

    private void menuItemTeacherToolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemTeacherToolActionPerformed
        TeacherToolWindow.showWindow();
    }//GEN-LAST:event_menuItemTeacherToolActionPerformed

    private void menuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveAsActionPerformed
        onSaveAs();
    }//GEN-LAST:event_menuItemSaveAsActionPerformed

    private void menuFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFileActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_menuFileActionPerformed

    private void onWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosed
        doExit();
    }//GEN-LAST:event_onWindowClosed

    private void menuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveActionPerformed
        onSave();
    }
    
    // called in:
    // menuItemSaveAsActionPerformed()
    // menuItemOpenActionPerformed()
    // prepareToExit()
    private void onSaveAs() {
// to use native Mac OS save menu, use something like this:
       
//        if(xp.isMac()) {
//            // use the native file dialog on the mac
//            java.awt.FileDialog dialog =
//               new java.awt.FileDialog(this, "Save",java.awt.FileDialog.SAVE);
//            dialog.show();
//        } else {
//            // use a swing file dialog on the other platforms
//            JFileChooser chooser = new JFileChooser();
//            chooser.showOpenDialog(this);
//        }
 
        if (this.exFile.getStudentName() == null) {
            String studentName = JOptionPane.showInputDialog
                    (this, "Please enter your name: ",
                    "Lambda", JOptionPane.QUESTION_MESSAGE);
            if (studentName == null) return; // cancelled
            if (studentName.trim().length() == 0) return; // treat as cancelled, not a valid student name, would cause setStudentName to throw
            this.exFile.setStudentName(studentName);
        }
        
       jFileChooser1.setSelectedFile(usersWorkFile);
        
        jFileChooser1.setFileFilter(this.onlySerializedFiles);               
        int returnVal = jFileChooser1.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File target = jFileChooser1.getSelectedFile();
            if (target.exists()) {
               int n = JOptionPane.showOptionDialog(this,
                "Overwrite " + target.getPath() + "?",
                "Lambda Calculator",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
               if (n != JOptionPane.OK_OPTION)
                   return;
            }
            if (!target.toString().endsWith("."+SERIALIZED_FILE_SUFFIX)) {
                target = new File(target.toString()+"."+SERIALIZED_FILE_SUFFIX);
            }
            writeUsersWorkFile(target);
        }
    }//GEN-LAST:event_menuItemSaveActionPerformed

    // called in:
    // menuItemSaveActionPerformed()
    // menuItemOpenActionPerformed()
    // prepareToExit()
    private void onSave() {
        assert(hasUserSaved);
        writeUsersWorkFile(usersWorkFile);
    }

    // called in:
    // onSave()
    // onSaveAs()
    private void writeUsersWorkFile(File newfile) {
        try {
            this.exFile.saveTo(newfile);
            usersWorkFile = newfile;
            hasUserSaved = true;
            hasUnsavedWork = false;
            menuItemSave.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog
                    (this, "There was an error saving the exercise file: " + e.getMessage(),
                    "Error saving exercise file", JOptionPane.ERROR_MESSAGE);         
        }
    }
    
    private void menuItemQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemQuitActionPerformed
        doExit();
    }//GEN-LAST:event_menuItemQuitActionPerformed

    static void exit() {
        singleton.exit();
    }
    
    // called in:
    // onWindowClosed()
    // menuItemQuitActionPerformed()
    void doExit() {
        if (this.exFile != null && this.exFile.hasBeenStarted() && hasUnsavedWork) {
            
           Object[] options = {"Save and quit",
                    "Quit without saving",
                    "Cancel"};

           int n = JOptionPane.showOptionDialog(this,
            "Some of your answers are not yet saved. What should I do?",
            "Lambda Calculator",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);
           
         switch (n) {
             case JOptionPane.YES_OPTION: // Save and quit
                 if (hasUserSaved)
                    this.onSave();
                 else
                    this.onSaveAs();
                 break;
             case JOptionPane.NO_OPTION: // Quit without saving
                 break;
             case JOptionPane.CANCEL_OPTION: // Cancel
             case JOptionPane.CLOSED_OPTION:
                 return; // don't dispose
         }
        
        }

        this.dispose();
        TeacherToolWindow.disposeWindow();
        ScratchPadWindow.disposeWindow();
    }

    private void menuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenActionPerformed
        if (this.exFile != null && this.exFile.hasBeenStarted() && hasUnsavedWork) {
            int n = JOptionPane.showOptionDialog(this,
                "Some of your answers are not yet saved.  Should I save your work before opening another file?",
                "Lambda Calculator",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
           
           switch (n) {
            case JOptionPane.YES_OPTION:
                 if (hasUserSaved)
                    this.onSave();
                 else
                    this.onSaveAs();
                 break;
             case JOptionPane.NO_OPTION:
                 break;
             case JOptionPane.CANCEL_OPTION:
             case JOptionPane.CLOSED_OPTION:
                 return;
            }
        }
        
        jFileChooser1.setFileFilter(this.allRecognizedFiles);
        int returnVal = jFileChooser1.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            loadExerciseFile(jFileChooser1.getSelectedFile());            
        }             
    }//GEN-LAST:event_menuItemOpenActionPerformed

    private void onExerciseTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_onExerciseTreeValueChanged
        if (this.updatingTree) return;
        TreePath path = jTreeExerciseFile.getSelectionPath();
        if (path != null && exFile != null) {
            if (path.getPathCount() < 2)
                currentGroup = 0;
            else
                currentGroup = ((ExerciseTreeModel.ExerciseGroupWrapper)path.getPathComponent(1)).group.getIndex();
            if (path.getPathCount() < 3)
                currentEx = 0;
            else
                currentEx = ((ExerciseTreeModel.ExerciseWrapper)path.getPathComponent(2)).ex.getIndex();
            showExercise();
        }
    }//GEN-LAST:event_onExerciseTreeValueChanged

    private void txtUserAnswerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserAnswerActionPerformed
        onCheckAnswer(evt);
    }//GEN-LAST:event_txtUserAnswerActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (currentEx+1 < exFile.getGroup(currentGroup).size())
            currentEx++;
        else
            currentGroup++;
        showExercise();
    }//GEN-LAST:event_btnNextActionPerformed

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (currentEx > 0)
            currentEx--;
        else
            currentGroup--;
        showExercise();
    }//GEN-LAST:event_btnPrevActionPerformed

    private void onCheckAnswer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onCheckAnswer
        try {
            String string = txtUserAnswer.getText().trim();
            if (!string.equals(txtUserAnswer.getText()))
                txtUserAnswer.setText(string);
            
            AnswerStatus status = ex.checkAnswer(string);
            if (status.isCorrect() && status.endsExercise()) {
                String response = status.getMessage() + " ";
                if (btnNext.isEnabled() && !exFile.hasBeenCompleted())
                    response += "Click the Next Problem button below to go on to the next exercise.";
                else if (!exFile.hasBeenCompleted())
                    response += "Now go back and finish the exercises you haven't solved yet.";
                else
                    response += "Congratulations! You've solved all the problems in this exercise file. Now save your work for submission.";
                txtFeedback.setText(response);
            } else if (status.isCorrect()) {
                txtFeedback.setText(status.getMessage());
                txtQuestion.setText(ex.getLastAnswer());
            } else {
                String message = status.getMessage();

                // This seems vaguely insulting after x repetitions, so we take it out.
//                if (++wrongInARowCount >= 3) {
//                    message += "\nHave you read the directions carefully?";
//                    wrongInARowCount = 0;
//                }
                txtFeedback.setText(message);
            }
            
            jTreeExerciseFile.repaint();

            setAnswerEnabledState(); // update enabled state of controls
            
            if (status.isCorrect())
                flagChangeMade();
                    
        } catch (SyntaxException s) {
            txtFeedback.setText(s.getMessage());
            if (s.getPosition() >= 0 && s.getPosition() <= txtUserAnswer.getText().length())
                txtUserAnswer.setCaretPosition(s.getPosition());
        }
        txtUserAnswer.requestFocus();
    }//GEN-LAST:event_onCheckAnswer
    
    // called in:
    // btnDoAgainActionPerformed()
    // onCheckAnswer()
    private void flagChangeMade() {
        hasUnsavedWork = true;
        if (hasUserSaved)
            menuItemSave.setEnabled(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckAnswer;
    private javax.swing.JButton btnDoAgain;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanelEnterExpressions;
    private javax.swing.JPanel jPanelRightHalf;
    private javax.swing.JPanel jPanelRightHalf2;
    private javax.swing.JScrollPane jScrollPaneDirections;
    private javax.swing.JScrollPane jScrollPaneFeedback;
    private javax.swing.JScrollPane jScrollPaneUpperLeft;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPaneLeftHalf;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JTree jTreeExerciseFile;
    private javax.swing.JTextArea lblDirections;
    private javax.swing.JLabel lblHelpBinaries;
    private javax.swing.JLabel lblHelpBinders;
    private javax.swing.JLabel lblHelpConditionals;
    private javax.swing.JTextArea lblHelpHeader;
    private javax.swing.JLabel lblHelpLambda;
    private javax.swing.JLabel lblHelpNot;
    private javax.swing.JTextArea lblIdentifierTypes;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemOpen;
    private javax.swing.JMenuItem menuItemQuit;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveAs;
    private javax.swing.JMenuItem menuItemScratchPad;
    private javax.swing.JMenuItem menuItemTeacherTool;
    private javax.swing.JMenu menuTools;
    private javax.swing.JTextArea txtFeedback;
    private lambdacalc.gui.LambdaEnabledTextField txtQuestion;
    private lambdacalc.gui.LambdaEnabledTextField txtUserAnswer;
    // End of variables declaration//GEN-END:variables
    
}
