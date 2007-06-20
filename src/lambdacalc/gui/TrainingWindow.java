/*
 * TrainingWindow.java
 *
 * Created on May 29, 2006, 12:43 PM
 */

package lambdacalc.gui;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import lambdacalc.logic.*;
import lambdacalc.exercises.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.TitledBorder;
import java.io.*;
import lambdacalc.lf.LFNode;

/**
 *
 * @author  tauberer
 */
public class TrainingWindow extends JFrame {

    
    // If you edit this, don't add a dot, it messes up the ExerciseFileFilter
    public static final String SERIALIZED_FILE_SUFFIX = "lbd"; 
    
    public static final String ENCODING = "utf-8";
    
    public static final ImageIcon UNSOLVED_FILE_ICON = new ImageIcon("images/logo.gif");
    public static final ImageIcon SOLVED_FILE_ICON   = new ImageIcon("images/logo_green.gif");

    // used for switchViewTo method that switches the content of the 
    // right half of the screen
    public static final int TYPES_AND_CONVERSIONS = 0;
    public static final int TREES = 1;

    private ExerciseFile currentExFile; // this is null if no file has been loaded yet
    
    Exercise ex;
    int currentGroup = 0, currentEx = 0; // we start counting at zero
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
         // maximize window
        singleton.setExtendedState(JFrame.MAXIMIZED_BOTH); 
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
    
    /** Creates new form TrainingWindow. Private so that showWindow is used instead. */
    private TrainingWindow() {
        
        initLookAndFeel();
        initComponents(); // calls the uneditable Netbeans-generated code
        
        initializeJFileChooser(jFileChooser1, true, true);
        
        jTreeExerciseFile.setFont(Util.getUnicodeFont(14));

        lblHelpLambda.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinders.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinaries.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpNot.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpConditionals.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));

        //TODO synchronize this with ExpressionParser and LambdaEnabledTextField
        lblHelpLambda.setText("Type capital L for " + Lambda.SYMBOL);
        lblHelpBinders.setText("Type capital A, E, and I for " + ForAll.SYMBOL + ", " + Exists.SYMBOL + ", and " + Iota.SYMBOL);
        lblHelpBinaries.setText("Type & for " + And.SYMBOL + " and | for " + Or.SYMBOL);
        lblHelpNot.setText("Type the tilde (~) for " + Not.SYMBOL);
        lblHelpConditionals.setText("Type -> for " + If.SYMBOL + " and <-> for " + Iff.SYMBOL);
      
        txtQuestion.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
  
        //jSplitPaneLowerRight.setDividerLocation(0.7);
        
//        treeDisplay.addSelectionListener
//                (new TreeExerciseWidget.SelectionListener()
//        { public void selectionChanged(TreeExerciseWidget.SelectionEvent evt) {
//              updateInfoBox((LFNode) evt.getSource());
//          }
//        });
                
        clearAllControls();
        
        //loadExerciseFile("examples/tests.txt");
        //TODO comment out previous line
    }
    
    
    
//    void updateInfoBox(LFNode selectedNode) {
//        
//        //jPanelInfoBox contains jScrollPaneInfoBox, which contains jTableInfoBox
//        
//        jTableInfoBox.setModel
//                (new NodePropertiesTableModel(selectedNode.getProperties()));
//        
//        jPanelInfoBox.removeAll();
//        GridLayout layout = (GridLayout) jPanelInfoBox.getLayout();
//        if (true) {
////        if (selectedNode instanceof LexicalTerminal) {
//            layout.setRows(1);
//            //jPanelInfoBox.add(jScrollPaneInfoBox);
//            jPanelInfoBox.add(lexiconList);
//        } else {
//            layout.setRows(1);
//            jPanelInfoBox.add(jScrollPaneInfoBox);
//        }
//        jPanelInfoBox.validate();
//        //jSplitPaneLowerRight.setDividerLocation(0.7);
//    }

    public void switchViewTo(int view) {
        CardLayout cardLayout = (CardLayout) jPanelCardLayout.getLayout();
        switch (view) {
            case TREES:
                cardLayout.show(jPanelCardLayout, "treesCard");
                break;
            case TYPES_AND_CONVERSIONS:
                cardLayout.show(jPanelCardLayout, "typesAndConversionsCard");
                break;
                
//                
//                jSplitPaneUpperRight.removeAll();
//                jSplitPaneLowerRight.removeAll();
//
//                jSplitPaneUpperRight.setTopComponent(jScrollPaneDirections);
//                jSplitPaneUpperRight.setBottomComponent(treeDisplay);
//                
//                jSplitPaneLowerRight.setLeftComponent(jPanelTypesAndConversions);
//                jSplitPaneLowerRight.setRightComponent(jPanelInfoBox);
//                
//                jSplitPaneRightHalf.setTopComponent(jSplitPaneUpperRight);
//                jSplitPaneRightHalf.setBottomComponent(jSplitPaneLowerRight);
//
//             
//                jSplitPaneRightHalf.setDividerLocation(-1); // instructs it to set itself automatically
//             
//                //jSplitPaneLowerRight.setDividerLocation(0.7);
//                
//                break;
//            case TYPES_AND_CONVERSIONS:
//                jSplitPaneUpperRight.removeAll();
//                jSplitPaneLowerRight.removeAll();
//                jSplitPaneRightHalf.removeAll();
//                
//                jSplitPaneRightHalf.setTopComponent(jScrollPaneDirections);
//                jSplitPaneRightHalf.setBottomComponent(jPanelTypesAndConversions);
//                
//                jSplitPaneRightHalf.setDividerLocation(0.4); // a little bit higher than the middle
//                break;
            default:
                throw new IllegalArgumentException("Don't know this view");
                
        }
//        jSplitPaneUpperRight.validate();
//        jSplitPaneLowerRight.validate();
//        jSplitPaneRightHalf.validate();
        
        
        //jPanelDirectionsOrTrees.validate();
        //jSplitPaneRightHalf.setResizeWeight(1);
        //jPanelDirectionsOrTrees.repaint();
        //repaint();
               
    }
    
    
    
    // called by TrainingWindow constructor
    private void clearAllControls() {
        hasUserSaved = false;
        hasUnsavedWork = false;
        usersWorkFile = null;
        currentExFile = null;
        ex = null;
        
        this.jTreeExerciseFile.setModel(new javax.swing.tree.DefaultTreeModel(new javax.swing.tree.DefaultMutableTreeNode("No exercise file opened")));

        lblDirections.setText("Open an exercise file from your instructor by using the File menu above.");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnDoAgain.setEnabled(false);
        
        //TitledBorder tb = (TitledBorder)jPanelQuestion.getBorder();
        //tb.setTitle("Current Problem");
        
        jLabelAboveDirections.setText(" ");
        jLabelAboveQuestion.setText(" ");
        
        txtUserAnswer.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        txtUserAnswer.setEnabled(false);
        btnCheckAnswer.setEnabled(false);

        txtUserAnswer.setText("");
        txtFeedback.setText("");

        //lblIdentifierTypes.setVisible(false);
        
        menuItemSave.setEnabled(false);
        menuItemSaveAs.setEnabled(false);
        
        switchViewTo(TYPES_AND_CONVERSIONS);
    }
    
    private void loadExerciseFile(String filename) {
        loadExerciseFile(new File(filename));       
    }
    
    // used in loadExerciseFile()
    private ExerciseFile parse(File f) throws IOException, ExerciseFileFormatException {
        return ExerciseFileParser.parse(new InputStreamReader (new FileInputStream(f), ENCODING));
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
                this.currentExFile = new ExerciseFile(f);
                isWorkFile = true;
            } else {
                this.currentExFile = parse(f);
            }
            menuItemSaveAs.setEnabled(true);
            menuItemSave.setEnabled(false);
        } catch (IOException e) { // thrown by deserialize and parse
            e.printStackTrace();
            Util.displayErrorMessage
                    (this, "There was an error opening the exercise file: " + (e.getMessage() == null ? "Unknown read error." : e.getMessage()),
                    "Error loading exercise file");
            return;
        } catch (ExerciseFileFormatException e) { // thrown by parse
            e.printStackTrace();
            Util.displayErrorMessage
                    (this, "The exercise file couldn't be read: " + e.getMessage(), // e.g. typo
                    "Error loading exercise file");
            return;
        }
        
        if (this.getCurrentExFile().hasBeenCompleted()) {
            Util.displayInformationMessage
                    (this, "All the exercises in this file have already been solved.",
                    "File already completed");
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
        
        this.treemodel = new ExerciseTreeModel(this.getCurrentExFile());
        this.jTreeExerciseFile.setModel(this.treemodel);
        //this.jTreeExerciseFile.setCellRenderer(new ExerciseTreeRenderer());
        for (int i = 0; i < this.jTreeExerciseFile.getRowCount(); i++) {
            this.jTreeExerciseFile.expandRow(i);
            this.jTreeExerciseFile.setRowHeight(this.jTreeExerciseFile.getFont().getBaselineFor('A'));
        }
        
        showFirstExercise();
        
    }
    
    private ExerciseGroup getCurrentGroup() {
        if (this.getCurrentExFile() == null) return null;
        return this.getCurrentExFile().getGroup(this.currentGroup);
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
        if (getCurrentExFile() == null) return null;
        return getCurrentGroup().getItem(currentEx);
    }
    
    
    // called in:
    // showFirstExercise()
    // btnDoAgainActionPerformed()
    // onExerciseTreeValueChanged()
    // btnNextActionPerformed()
    // btnPrevActionPerformed()
    private void showExercise() {
        ex = getCurrentExercise();
        
        // Show the directions for this exercise. If there are both
        // group-level directions and exercise-level instructions,
        // only display both together for the first exercise in the
        // group so that when the user goes on to the next problem,
        // it is very obvious that the directions displayed have changed.
        String directions = "There are no directions for this exercise.";
        if (!getCurrentGroup().getDirections().trim().equals("")
            && (ex.getIndex() == 0 || ex.getInstructions() == null)) {
            directions = getCurrentGroup().getDirections();
            if (ex.getInstructions() != null)
                directions += "\n\n" + ex.getInstructions();
        } else if (ex.getInstructions() != null) {
            directions = ex.getInstructions();
        }
        lblDirections.setText(directions);
        lblDirections.setCaretPosition(0);
        
        if (ex instanceof TypeExercise) { 
            btnTransfer.setEnabled(false);
            switchViewTo(TYPES_AND_CONVERSIONS);
        } else if (ex instanceof LambdaConversionExercise) {
            btnTransfer.setEnabled(true);
            switchViewTo(TYPES_AND_CONVERSIONS);
        } else if (ex instanceof TreeExercise) {
            btnTransfer.setEnabled(false);
            switchViewTo(TREES);
            treeDisplay.initialize((TreeExercise)ex);
            lexiconList.initialize(getCurrentExFile(), ex, treeDisplay);
       }

        btnPrev.setEnabled(currentEx > 0 || currentGroup > 0);
        btnNext.setEnabled(currentEx+1 < getCurrentGroup().size() || currentGroup+1 < getCurrentExFile().size() );
        
        //TitledBorder tb = (TitledBorder)jPanelQuestion.getBorder();
        //tb.setTitle("Current Problem: " + ex.getShortDirective());
        
        jLabelAboveQuestion.setText((ex.getIndex()+1) + ". " + ex.getShortDirective());
        
        jLabelAboveDirections.setText(getCurrentGroup().getNumberedTitle());
        
        setAnswerEnabledState();
        setQuestionText();

        if (txtUserAnswer.isEnabled()) {
            txtUserAnswer.requestFocusInWindow();
        }

        try {
            updatingTree = true; // this prevents recursion when the event is
                                 // fired as if the user is clicking on this node
            jTreeExerciseFile.setSelectionPath
                    (new TreePath
                    (new Object[] { getCurrentExFile(), 
                                    new ExerciseTreeModel.ExerciseGroupWrapper(getCurrentGroup()), 
                                    new ExerciseTreeModel.ExerciseWrapper(ex) } ));
            jTreeExerciseFile.scrollPathToVisible(jTreeExerciseFile.getSelectionPath());
        } finally {
            updatingTree = false;
        }

        if (ex instanceof HasIdentifierTyper) {
            //lblIdentifierTypes.setVisible(true);
            IdentifierTyper typer = ((HasIdentifierTyper)ex).getIdentifierTyper();
            
            lblIdentifierTypes.setText("Use the following typing conventions:\n" + typer.toString());
            ScratchPadWindow.setTypingConventions(typer);
        } else {
            //lblIdentifierTypes.setVisible(false);
        }

        wrongInARowCount = 0;
        
        txtUserAnswer.requestFocusInWindow();
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
        btnDoAgain.setEnabled(ex.hasBeenStarted());
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
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jSplitPaneLeftHalf = new javax.swing.JSplitPane();
        jScrollPaneUpperLeft = new javax.swing.JScrollPane();
        jTreeExerciseFile = new javax.swing.JTree();
        jPanel1 = new javax.swing.JPanel();
        jPanelEnterExpressions = new javax.swing.JPanel();
        lblHelpHeader = new javax.swing.JTextArea();
        lblHelpLambda = new javax.swing.JLabel();
        lblHelpBinders = new javax.swing.JLabel();
        lblHelpBinaries = new javax.swing.JLabel();
        lblHelpNot = new javax.swing.JLabel();
        lblHelpConditionals = new javax.swing.JLabel();
        lblIdentifierTypes = new javax.swing.JTextArea();
        jSplitPaneRightHalf = new javax.swing.JSplitPane();
        jPanelLowerRight = new javax.swing.JPanel();
        jPanelCardLayout = new javax.swing.JPanel();
        jPanelTypesAndConversions = new javax.swing.JPanel();
        btnCheckAnswer = new javax.swing.JButton();
        jScrollPaneFeedback = new javax.swing.JScrollPane();
        txtFeedback = new javax.swing.JTextArea();
        jPanelQuestion = new javax.swing.JPanel();
        txtQuestion = new lambdacalc.gui.LambdaEnabledTextField();
        btnTransfer = new javax.swing.JButton();
        txtUserAnswer = new lambdacalc.gui.LambdaEnabledTextField();
        jLabelAboveQuestion = new javax.swing.JLabel();
        jPanelTrees = new javax.swing.JPanel();
        treeDisplay = new lambdacalc.gui.TreeExerciseWidget();
        jPanelNodeProperties = new javax.swing.JPanel();
        jPanelInfoBox = new javax.swing.JPanel();
        lexiconList = new lambdacalc.gui.LexiconList();
        jPanelNavigationButtons = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        btnDoAgain = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        jSeparatorBelowNavButtons = new javax.swing.JSeparator();
        jPanelUpperRight = new javax.swing.JPanel();
        jScrollPaneDirections = new javax.swing.JScrollPane();
        lblDirections = new javax.swing.JTextArea();
        jLabelAboveDirections = new javax.swing.JLabel();
        jSeparatorBelowDirections = new javax.swing.JSeparator();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemOpen = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        menuItemClose = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuItemTeacherTool = new javax.swing.JMenuItem();
        menuItemScratchPad = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Interactive Exercise Solver");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosed(evt);
            }
        });

        jSplitPaneMain.setDividerLocation(300);
        jSplitPaneMain.setOneTouchExpandable(true);
        jSplitPaneLeftHalf.setDividerLocation(300);
        jSplitPaneLeftHalf.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneLeftHalf.setOneTouchExpandable(true);
        jTreeExerciseFile.setFont(new java.awt.Font("Serif", 0, 14));
        jTreeExerciseFile.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                onExerciseTreeValueChanged(evt);
            }
        });
        jTreeExerciseFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTreeExerciseFileMouseClicked(evt);
            }
        });

        jScrollPaneUpperLeft.setViewportView(jTreeExerciseFile);

        jSplitPaneLeftHalf.setTopComponent(jScrollPaneUpperLeft);

        jPanel1.setLayout(new java.awt.GridLayout(2, 1));

        jPanel1.setMinimumSize(new java.awt.Dimension(60, 84));
        jPanelEnterExpressions.setBorder(javax.swing.BorderFactory.createTitledBorder("How to Enter Expressions"));
        lblHelpHeader.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblHelpHeader.setColumns(20);
        lblHelpHeader.setEditable(false);
        lblHelpHeader.setFont(new java.awt.Font("Dialog", 0, 12));
        lblHelpHeader.setLineWrap(true);
        lblHelpHeader.setRows(5);
        lblHelpHeader.setText("When typing lambda expressions, use the following keyboard shortcuts:");
        lblHelpHeader.setWrapStyleWord(true);
        lblHelpHeader.setBorder(null);
        lblHelpHeader.setMinimumSize(new java.awt.Dimension(50, 16));

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
                    .add(lblHelpHeader, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanelEnterExpressionsLayout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(jPanelEnterExpressionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(lblHelpBinders, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .add(lblHelpBinaries, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .add(lblHelpLambda, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblHelpNot, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblHelpConditionals, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 19, Short.MAX_VALUE)))
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1.add(jPanelEnterExpressions);

        lblIdentifierTypes.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblIdentifierTypes.setColumns(20);
        lblIdentifierTypes.setEditable(false);
        lblIdentifierTypes.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblIdentifierTypes.setLineWrap(true);
        lblIdentifierTypes.setRows(5);
        lblIdentifierTypes.setText(" ");
        lblIdentifierTypes.setWrapStyleWord(true);
        lblIdentifierTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Conventions about letters"));
        jPanel1.add(lblIdentifierTypes);

        jSplitPaneLeftHalf.setRightComponent(jPanel1);

        jSplitPaneMain.setLeftComponent(jSplitPaneLeftHalf);

        jSplitPaneRightHalf.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneRightHalf.setOneTouchExpandable(true);
        jPanelLowerRight.setLayout(new java.awt.GridBagLayout());

        jPanelCardLayout.setLayout(new java.awt.CardLayout());

        jPanelTypesAndConversions.setLayout(new java.awt.GridBagLayout());

        btnCheckAnswer.setText("Check Answer");
        btnCheckAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onCheckAnswer(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelTypesAndConversions.add(btnCheckAnswer, gridBagConstraints);

        jScrollPaneFeedback.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneFeedback.setBorder(javax.swing.BorderFactory.createTitledBorder("Feedback"));
        jScrollPaneFeedback.setMinimumSize(new java.awt.Dimension(232, 150));
        jScrollPaneFeedback.setPreferredSize(new java.awt.Dimension(232, 150));
        txtFeedback.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtFeedback.setColumns(20);
        txtFeedback.setEditable(false);
        txtFeedback.setFont(new java.awt.Font("SansSerif", 0, 12));
        txtFeedback.setLineWrap(true);
        txtFeedback.setRows(5);
        txtFeedback.setWrapStyleWord(true);
        txtFeedback.setBorder(null);
        jScrollPaneFeedback.setViewportView(txtFeedback);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTypesAndConversions.add(jScrollPaneFeedback, gridBagConstraints);

        jPanelQuestion.setLayout(new java.awt.GridBagLayout());

        txtQuestion.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtQuestion.setBorder(null);
        txtQuestion.setEditable(false);
        txtQuestion.setFont(new java.awt.Font("Serif", 0, 18));
        txtQuestion.setPreferredSize(new java.awt.Dimension(460, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelQuestion.add(txtQuestion, gridBagConstraints);

        btnTransfer.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        btnTransfer.setText("Paste");
        btnTransfer.setMinimumSize(new java.awt.Dimension(55, 29));
        btnTransfer.setPreferredSize(new java.awt.Dimension(55, 29));
        btnTransfer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        jPanelQuestion.add(btnTransfer, gridBagConstraints);

        txtUserAnswer.setFont(new java.awt.Font("Serif", 0, 18));
        txtUserAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUserAnswerActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelQuestion.add(txtUserAnswer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelTypesAndConversions.add(jPanelQuestion, gridBagConstraints);

        jLabelAboveQuestion.setFont(new java.awt.Font("Lucida Grande", 1, 14));
        jLabelAboveQuestion.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelTypesAndConversions.add(jLabelAboveQuestion, gridBagConstraints);

        jPanelCardLayout.add(jPanelTypesAndConversions, "typesAndConversionsCard");

        jPanelTrees.setLayout(new java.awt.GridBagLayout());

        jPanelTrees.setMinimumSize(new java.awt.Dimension(19, 50));
        jPanelTrees.setPreferredSize(new java.awt.Dimension(235, 150));
        treeDisplay.setBackground(java.awt.Color.white);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTrees.add(treeDisplay, gridBagConstraints);

        jPanelNodeProperties.setLayout(new java.awt.GridLayout(1, 0));

        jPanelNodeProperties.setBorder(javax.swing.BorderFactory.createTitledBorder("Lexical entries"));
        jPanelNodeProperties.setMinimumSize(new java.awt.Dimension(180, 100));
        jPanelNodeProperties.setPreferredSize(new java.awt.Dimension(180, 100));
        jPanelInfoBox.setLayout(new java.awt.GridLayout(1, 0));

        jPanelInfoBox.add(lexiconList);

        jPanelNodeProperties.add(jPanelInfoBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelTrees.add(jPanelNodeProperties, gridBagConstraints);

        jPanelCardLayout.add(jPanelTrees, "treesCard");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLowerRight.add(jPanelCardLayout, gridBagConstraints);

        jPanelNavigationButtons.setLayout(new java.awt.GridBagLayout());

        btnPrev.setText("< Previous");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanelNavigationButtons.add(btnPrev, gridBagConstraints);

        btnDoAgain.setText("Repeat");
        btnDoAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoAgainActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanelNavigationButtons.add(btnDoAgain, gridBagConstraints);

        btnNext.setText("Next >");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        btnNext.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnNextKeyPressed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        jPanelNavigationButtons.add(btnNext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelLowerRight.add(jPanelNavigationButtons, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanelLowerRight.add(jSeparatorBelowNavButtons, gridBagConstraints);

        jSplitPaneRightHalf.setRightComponent(jPanelLowerRight);

        jPanelUpperRight.setLayout(new java.awt.GridBagLayout());

        jScrollPaneDirections.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneDirections.setBorder(null);
        jScrollPaneDirections.setPreferredSize(new java.awt.Dimension(100, 100));
        lblDirections.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblDirections.setColumns(20);
        lblDirections.setEditable(false);
        lblDirections.setFont(new java.awt.Font("SansSerif", 0, 12));
        lblDirections.setLineWrap(true);
        lblDirections.setWrapStyleWord(true);
        lblDirections.setBorder(null);
        jScrollPaneDirections.setViewportView(lblDirections);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jPanelUpperRight.add(jScrollPaneDirections, gridBagConstraints);

        jLabelAboveDirections.setFont(new java.awt.Font("Lucida Grande", 1, 16));
        jLabelAboveDirections.setText("Lambda Calculator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanelUpperRight.add(jLabelAboveDirections, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanelUpperRight.add(jSeparatorBelowDirections, gridBagConstraints);

        jSplitPaneRightHalf.setLeftComponent(jPanelUpperRight);

        jSplitPaneMain.setRightComponent(jSplitPaneRightHalf);

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

        menuItemClose.setMnemonic('c');
        menuItemClose.setText("Close Window");
        menuItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCloseActionPerformed(evt);
            }
        });

        menuFile.add(menuItemClose);

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

    private void btnNextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnNextKeyPressed
        // we want this button to be pressable using Enter:
        //TODO add similar code to other buttons -- or find
        //a centralized way to do this
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            btnNext.doClick();
        }
    }//GEN-LAST:event_btnNextKeyPressed

    private void onCheckAnswer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onCheckAnswer
        try {
            String string = txtUserAnswer.getText().trim();
            if (!string.equals(txtUserAnswer.getText()))
                txtUserAnswer.setText(string);
            
            AnswerStatus status = ex.checkAnswer(string);
            if (status.isCorrect() && status.endsExercise()) {
                String response = status.getMessage() + " ";
                if (btnNext.isEnabled() && !getCurrentExFile().hasBeenCompleted()) {
                    response += "Click the Next Problem button to go on to the next exercise.";
                    btnNext.requestFocusInWindow();
                }
                else if (!getCurrentExFile().hasBeenCompleted())
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
        if (txtUserAnswer.isEnabled()) {
            txtUserAnswer.requestFocusInWindow();
        } else if (btnNext.isEnabled() && !getCurrentExFile().hasBeenCompleted()) {
            btnNext.requestFocusInWindow();
        }
        
    }//GEN-LAST:event_onCheckAnswer

    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
        if (currentEx > 0) {
            currentEx--;
        } else {
            currentGroup--;
            currentEx = getCurrentGroup().size()-1;
        }
        showExercise();
    }//GEN-LAST:event_btnPrevActionPerformed

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        if (currentEx+1 < getCurrentGroup().size()) {
            currentEx++;
        } else {
            currentGroup++;
            currentEx = 0;
        }
        showExercise();
    }//GEN-LAST:event_btnNextActionPerformed

    private void txtUserAnswerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUserAnswerActionPerformed
        onCheckAnswer(evt);
    }//GEN-LAST:event_txtUserAnswerActionPerformed

    private void btnDoAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoAgainActionPerformed
        ex.reset();
        flagChangeMade();
        jTreeExerciseFile.repaint();
        showExercise();
    }//GEN-LAST:event_btnDoAgainActionPerformed

    private void btnTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferActionPerformed
        txtUserAnswer.setText(txtQuestion.getText());
        txtUserAnswer.requestFocusInWindow();
    }//GEN-LAST:event_btnTransferActionPerformed

    private void jTreeExerciseFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTreeExerciseFileMouseClicked
        
        // by clicking on the tree view when no exercise is loaded,
        // we open the file chooser
        
        if (evt.getClickCount() >= 2) { // double click
            if (getCurrentExFile() == null) {
                menuItemOpen.doClick();
            }
        }
    }//GEN-LAST:event_jTreeExerciseFileMouseClicked

    private void menuItemScratchPadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemScratchPadActionPerformed
        ScratchPadWindow.showWindow();
    }//GEN-LAST:event_menuItemScratchPadActionPerformed

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
 
        if (this.getCurrentExFile().getStudentName() == null) {
            String studentName = JOptionPane.showInputDialog
                    (this, "Please enter your name (e.g. Noam Chomsky): ",
                    "Lambda", JOptionPane.QUESTION_MESSAGE);
            if (studentName == null) return; // cancelled
            if (studentName.trim().length() == 0) return; // treat as cancelled, not a valid student name, would cause setStudentName to throw
            this.getCurrentExFile().setStudentName(studentName);
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
            this.getCurrentExFile().saveTo(newfile);
            usersWorkFile = newfile;
            hasUserSaved = true;
            hasUnsavedWork = false;
            menuItemSave.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
            Util.displayErrorMessage
                    (this, "There was an error saving the exercise file: " + e.getMessage(),
                    "Error saving exercise file");         
        }
    }
    
    private void menuItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemCloseActionPerformed
        doExit();
    }//GEN-LAST:event_menuItemCloseActionPerformed

    static void exit() {
        singleton.doExit();
    }
    
    // called in:
    // onWindowClosed()
    // menuItemQuitActionPerformed()
    void doExit() {
        if  
        (this.getCurrentExFile() != null 
                && this.getCurrentExFile().hasBeenStarted() 
                && this.hasUnsavedWork) {
            
           Object[] options = {"Save and quit",
                    "Quit without saving",
                    "Cancel"};

           int n = JOptionPane.showOptionDialog(this,
            "Some of your answers have not yet been saved. What should I do?",
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
        this.clearAllControls();
        this.dispose();
    }

    private void menuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenActionPerformed
        if (this.getCurrentExFile() != null && this.getCurrentExFile().hasBeenStarted() && hasUnsavedWork) {
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
        if (path != null && getCurrentExFile() != null) {
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
    
    // called in:
    // btnDoAgainActionPerformed()
    // onCheckAnswer()
    private void flagChangeMade() {
        hasUnsavedWork = true;
        if (hasUserSaved)
            menuItemSave.setEnabled(true);
    }
    
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    public ExerciseFile getCurrentExFile() {
        return currentExFile;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckAnswer;
    private javax.swing.JButton btnDoAgain;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnTransfer;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabelAboveDirections;
    private javax.swing.JLabel jLabelAboveQuestion;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelCardLayout;
    private javax.swing.JPanel jPanelEnterExpressions;
    private javax.swing.JPanel jPanelInfoBox;
    private javax.swing.JPanel jPanelLowerRight;
    private javax.swing.JPanel jPanelNavigationButtons;
    private javax.swing.JPanel jPanelNodeProperties;
    private javax.swing.JPanel jPanelQuestion;
    private javax.swing.JPanel jPanelTrees;
    private javax.swing.JPanel jPanelTypesAndConversions;
    private javax.swing.JPanel jPanelUpperRight;
    private javax.swing.JScrollPane jScrollPaneDirections;
    private javax.swing.JScrollPane jScrollPaneFeedback;
    private javax.swing.JScrollPane jScrollPaneUpperLeft;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparatorBelowDirections;
    private javax.swing.JSeparator jSeparatorBelowNavButtons;
    private javax.swing.JSplitPane jSplitPaneLeftHalf;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JSplitPane jSplitPaneRightHalf;
    private javax.swing.JTree jTreeExerciseFile;
    private javax.swing.JTextArea lblDirections;
    private javax.swing.JLabel lblHelpBinaries;
    private javax.swing.JLabel lblHelpBinders;
    private javax.swing.JLabel lblHelpConditionals;
    private javax.swing.JTextArea lblHelpHeader;
    private javax.swing.JLabel lblHelpLambda;
    private javax.swing.JLabel lblHelpNot;
    private javax.swing.JTextArea lblIdentifierTypes;
    private lambdacalc.gui.LexiconList lexiconList;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemClose;
    private javax.swing.JMenuItem menuItemOpen;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveAs;
    private javax.swing.JMenuItem menuItemScratchPad;
    private javax.swing.JMenuItem menuItemTeacherTool;
    private javax.swing.JMenu menuTools;
    private lambdacalc.gui.TreeExerciseWidget treeDisplay;
    private javax.swing.JTextArea txtFeedback;
    private lambdacalc.gui.LambdaEnabledTextField txtQuestion;
    private lambdacalc.gui.LambdaEnabledTextField txtUserAnswer;
    // End of variables declaration//GEN-END:variables
    
}
