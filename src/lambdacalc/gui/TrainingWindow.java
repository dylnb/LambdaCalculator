/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * TrainingWindow.java
 *
 * Created on May 29, 2006, 12:43 PM
 */

package lambdacalc.gui;

import java.awt.CardLayout;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import lambdacalc.logic.*;
import lambdacalc.exercises.*;
import java.nio.file.Files;

import javax.swing.*;
import javax.swing.tree.*;
import java.io.*;
import java.nio.file.Path;

import lambdacalc.lf.BareIndex;
import lambdacalc.lf.DummyTerminal;
import lambdacalc.lf.LFNode;
import lambdacalc.lf.LexicalTerminal;
import lambdacalc.lf.Nonterminal;
import lambdacalc.lf.Trace;

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
    public static final int BLANK = 0;
    public static final int TREES = 1;
    public static final int TYPES_AND_CONVERSIONS = 2;

    private File currentFile; // this is null if no file has been loaded yet
    private static ExerciseFile currentExFile; // this is null if no file has been loaded yet
    
    // will be overridden by any exercise file that specifies type conventions
    public static IdentifierTyper currentTypingConventions = IdentifierTyper.createDefault();
    
    Exercise ex;
    int currentGroup = 0, currentEx = 0; // we start counting at zero
    ExerciseTreeModel treemodel;
    
    private ExerciseGroup previousGroup = null;
    
    boolean updatingTree = false;
    
    int wrongInARowCount = 0;

    boolean hasUserSaved; // true iff the user has saved this exercise into a .lbd file (i.e. the Save menu is enabled if there are unsaved changes)
    boolean hasUnsavedWork; // true iff the user has entered any unsaved work
    File usersWorkFile = null;

    String lastOpenDir = null;



    
    
    private static final ExerciseFileFilter onlyTextFiles = new ExerciseFileFilter
            ("txt", "Exercise files");
    private static final ExerciseFileFilter onlySerializedFiles = new ExerciseFileFilter
            (SERIALIZED_FILE_SUFFIX, "Saved work");
    private static final ExerciseFileFilter allRecognizedFiles = new ExerciseFileFilter
            (new String[]{"txt",SERIALIZED_FILE_SUFFIX}, "All recognized files");    
    

    private static TrainingWindow singleton=null;
    
    public static TrainingWindow getSingleton() {
        return singleton;
    }
    
    public static void prepareWindow() {
        if (singleton == null) {
            singleton = new TrainingWindow();
        }
         // maximize window
//        singleton.setExtendedState(JFrame.MAXIMIZE_BOTH);         
        singleton.setExtendedState(JFrame.NORMAL); 
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
        if (!lambdacalc.Main.GOD_MODE) {
            menuTools.remove(menuItemTeacherTool);
        }
        
        initializeJFileChooser(jFileChooser1, true, true);
        
        jTreeExerciseFile.setFont(Util.getUnicodeFont(14));

        lblHelpHeader.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpLambda.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinders.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpBinaries.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpNot.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));
        lblHelpConditionals.setFont(Util.getUnicodeFont(lblHelpHeader.getFont().getSize()));

        updateLetterEnteringDisplay();
        
        txtQuestion.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        
        //jSplitPaneLowerRight.setDividerLocation(0.7);
        
        treeDisplay.addSelectionListener
                (new TreeExerciseWidget.SelectionListener()
        { public void selectionChanged(TreeExerciseWidget.SelectionEvent evt) {
              LFNode selectedNode = (LFNode) evt.getSource(); 
              // we might also say: selectedNode = treeDisplay.getSelectedNode();
              updateNodePropertyPanel(selectedNode);
          }
        });
        
        jPanelRuleSelection.initialize(treeDisplay);

        lexiconList.addListener(new LexiconListChangeListener());
        
        jListNodeHistory.setFont(Util.getUnicodeFont(16)); // same as LambdaEnabledTextField
                
        clearAllControls();
        
//        loadExerciseFile("examples/simple-jac.txt");
    }
    
    public void updateLetterEnteringDisplay() {
        String mod = LambdaEnabledTextField.getModifier();
        lblHelpLambda.setText("Type " + mod + "-" + Character.toLowerCase(Lambda.INPUT_SYMBOL) + " for " + Lambda.SYMBOL);
        lblHelpBinders.setText("Type " + mod + "-" + Character.toLowerCase(ForAll.INPUT_SYMBOL) + ", "
                + mod + "-" + Character.toLowerCase(Exists.INPUT_SYMBOL) + ", and "
                + mod + "-" + Character.toLowerCase(Iota.INPUT_SYMBOL) + " for " 
                + ForAll.SYMBOL + ", " + Exists.SYMBOL + ", and " + Iota.SYMBOL);
        lblHelpBinaries.setText("Type " + And.INPUT_SYMBOL 
        + " or " + And.ALTERNATE_INPUT_SYMBOL + " for " + And.SYMBOL 
                + " and " + mod + "-"
                + Character.toLowerCase(Or.INPUT_SYMBOL) + " for " + Or.SYMBOL);
        lblHelpNot.setText("Type the tilde (" + Not.INPUT_SYMBOL + ") for " + Not.SYMBOL);
        lblHelpConditionals.setText("Type -> for " + If.SYMBOL + " and <-> for " + Iff.SYMBOL);
        
    }
    
    
    public void updateNodePropertyPanel(String treeRep) {
        CardLayout cardLayout = (CardLayout) jPanelNodeProperties.getLayout();
        cardLayout.show(jPanelNodeProperties, "latex");
        jTextPaneLatex.setText(treeRep);
    }
    
    public void updateNodePropertyPanel(LFNode selectedNode) {
        CardLayout cardLayout = (CardLayout) jPanelNodeProperties.getLayout();
        
        //TODO if we ever introduce a NodeView, then it might know how to
        // call the right panel, so we don't have to cycle through all possible
        // node types with instanceof calls
        if (selectedNode instanceof Nonterminal) {
            if (lambdacalc.Main.GOD_MODE) {
                cardLayout.show(jPanelNodeProperties, "history");
                
                DefaultListModel model = new DefaultListModel();
                Nonterminal nt = (Nonterminal)selectedNode;
                
                

                
                
                //  display the entire node history at once
                java.util.Vector steps = nt.getUserMeaningSimplification();
                // steps: a vector of Expr objects
                if (steps != null) {
                    for (java.util.Iterator i = steps.iterator(); i.hasNext(); ) {
                        Expr next = (Expr) i.next();
                        model.addElement(next.toString());
                        if (next.equals(treeDisplay.getNodeExpressionState
                                (treeDisplay.getSelectedNode()))) {
                            break; // don't go on b/c we don't want to display "future history"
                        }
                    }
                }
                if (model.isEmpty()) {
                    model.addElement("This node has not yet been evaluated.");
                }
                jListNodeHistory.setModel(model);
                jListNodeHistory.setEnabled(!model.isEmpty());
                        
            } else if (((Nonterminal) selectedNode).isBranching()) {
                // Try to display the lambda reduction panel, but if not, fall back
                // on the rule selection panel.
                // Show the lambda conversion panel if...
                //    The node's evaluation has already begun.
                //    There were no errors in creating a LambdaConversionExercise around the current state.
                if (treeDisplay.getNodeExpressionState(selectedNode) != null) {
                    Expr current = treeDisplay.getNodeExpressionState(selectedNode);
                    try {
                        LambdaConversionExercise exercise = new LambdaConversionExercise(current, -1, getCurrentTypingConventions());
                        exercise.setParseSingleLetterIdentifiers(false);
                        exercise.setNotSoFast(getCurrentExercise().getNotSoFast());
                        jPanelLambdaConversion.initialize(exercise, treeDisplay);
                        cardLayout.show(jPanelNodeProperties, "lambdaConversion");
                        return;
                    } catch (TypeEvaluationException ex) {
                        // Seems to be thrown if a meaning bracket could not be evaluated.
                        // The message it provides is opaque to the user at this point.
                        treeDisplay.setErrorMessage("This node cannot be evaluated yet. Make sure all children have been evaluated already.");
                        //TODO something more useful here
                    }
                }
                
               cardLayout.show(jPanelNodeProperties, "ruleSelection");
            } else {
                cardLayout.show(jPanelNodeProperties, "nonBranchingNonterminal");
            }
        } else if (selectedNode instanceof LexicalTerminal) {
            cardLayout.show(jPanelNodeProperties, "lexicalTerminal");
            
        } else if (selectedNode instanceof BareIndex) {
            cardLayout.show(jPanelNodeProperties, "bareIndex");
        } else if (selectedNode instanceof Trace) {
            if (((Trace) selectedNode).isActualTrace()) {
                cardLayout.show(jPanelNodeProperties, "trace");
            } else {
                cardLayout.show(jPanelNodeProperties, "pronoun");
            }
        } else if (selectedNode instanceof DummyTerminal) {
            cardLayout.show(jPanelNodeProperties, "dummyTerminal");
        } else {
            cardLayout.show(jPanelNodeProperties, "default");
        }
        
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
            case BLANK:
                cardLayout.show(jPanelCardLayout, "blank");
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
        currentFile = null;
        ex = null;
        
        
        this.jTreeExerciseFile.setModel(new javax.swing.tree.DefaultTreeModel(new javax.swing.tree.DefaultMutableTreeNode("No exercise file opened")));

        lblDirections.setText("Open an exercise file from your instructor by using the File menu above.");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        btnDoAgain.setEnabled(false);
        
        //TitledBorder tb = (TitledBorder)jPanelQuestion.getBorder();
        //tb.setTitle("Current Problem");
        
        jLabelAboveDirections.setText("Lambda Calculator");
        jLabelAboveQuestion.setText(" ");
        
        txtUserAnswer.setBackground(UIManager.getColor("TextField.inactiveBackground"));
        txtUserAnswer.setEnabled(false);
        btnCheckAnswer.setEnabled(false);

        txtUserAnswer.setText("");
        txtFeedback.setText("");

        //lblIdentifierTypes.setVisible(false);
        
        menuItemSave.setEnabled(false);
        menuItemSaveAs.setEnabled(false);
        
        jListNodeHistory.setModel(new DefaultListModel());
        
        
        switchViewTo(BLANK);
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
                this.currentFile = f;
            } else {
                this.currentExFile = parse(f);
                this.currentFile = f;
            }
            menuItemSaveAs.setEnabled(true);
            menuItemSave.setEnabled(false);
            this.previousGroup = null; //so that at the beginning of a new exercise we don't say "You have started a new group"
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

        this.currentFile = f;
        
        this.treemodel = new ExerciseTreeModel(this.getCurrentExFile());
        this.jTreeExerciseFile.setModel(this.treemodel);
        //this.jTreeExerciseFile.setCellRenderer(new ExerciseTreeRenderer());
        for (int i = 0; i < this.jTreeExerciseFile.getRowCount(); i++) {
            this.jTreeExerciseFile.expandRow(i);
            this.jTreeExerciseFile.setRowHeight(this.jTreeExerciseFile.getFont().getBaselineFor('A'));
        }
        
        jPanelRuleSelection.setVisibleRules(this.getCurrentExFile().getRules());
        
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
            setCurrentTypingConventions(typer);
            lblIdentifierTypes.setText("Use the following typing conventions:\n" + typer.toString());
            ScratchPadWindow.setTypingConventions(typer);
        } else {
            //lblIdentifierTypes.setVisible(false);
        }

        wrongInARowCount = 0;
        
        previousGroup = getCurrentGroup();
        
        txtUserAnswer.requestFocusInWindow();
    }
    
    // called in showExercise()
    private void setQuestionText() {
        
        String text = "";
        if (previousGroup != null && !(getCurrentGroup().equals(previousGroup))) {
            text += "You are now in a different group of exercises. Check the directions " +
                    "above.\n\n";
        }
        
        if (!ex.isDone()) {
            String lastAnswer = ex.getLastAnswer();
            if (lastAnswer == null) {
                txtQuestion.setText(ex.getExerciseText());
                txtUserAnswer.setTemporaryText(ex.getTipForTextField());
                text += "You have not yet started this exercise.";
            } else {
                txtQuestion.setText(lastAnswer);
                txtUserAnswer.setText(lastAnswer);
                text += "You have started this exercise but have not completed it yet.";
            }
        } else {
            txtQuestion.setText(ex.getExerciseText());
            txtUserAnswer.setText(ex.getLastAnswer());
            text += "You have already solved this exercise.";
        }
        
        txtFeedback.setText(text);
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
        btnTransfer.setEnabled(!ex.isDone());
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jFileChooser1 = new javax.swing.JFileChooser();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jSplitPaneLeftHalf = new javax.swing.JSplitPane();
        jScrollPaneUpperLeft = new javax.swing.JScrollPane();
        jTreeExerciseFile = new javax.swing.JTree();
        jPanelLowerLeft = new javax.swing.JPanel();
        jPanelNavigationButtons = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        btnDoAgain = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        jScrollPaneIdentifierTypes = new javax.swing.JScrollPane();
        lblIdentifierTypes = new javax.swing.JTextArea();
        jScrollPaneEnterExpressions = new javax.swing.JScrollPane();
        jPanelEnterExpressions = new javax.swing.JPanel();
        lblHelpHeader = new javax.swing.JTextArea();
        lblHelpLambda = new javax.swing.JLabel();
        lblHelpBinders = new javax.swing.JLabel();
        lblHelpBinaries = new javax.swing.JLabel();
        lblHelpNot = new javax.swing.JLabel();
        lblHelpConditionals = new javax.swing.JLabel();
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
        jSplitPaneTrees = new javax.swing.JSplitPane();
        treeDisplay = new lambdacalc.gui.TreeExerciseWidget();
        jPanelNodeProperties = new javax.swing.JPanel();
        jPanelDefault = new javax.swing.JPanel();
        jPanelLexicalTerminal = new javax.swing.JPanel();
        lexiconList = new lambdacalc.gui.LexiconList();
        jPanelLambdaConversion = new lambdacalc.gui.NonterminalReductionPanel();
        jPanelRuleSelection = new lambdacalc.gui.RuleSelectionPanel();
        jPanelTrace = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanelBareIndex = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanelNonBranchingNonterminal = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanelNodeHistory = new javax.swing.JPanel();
        jScrollPaneNodeHistory = new javax.swing.JScrollPane();
        jListNodeHistory = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jPanelPronoun = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jPanelDummyTerminal = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanelLatex = new javax.swing.JPanel();
        jScrollPaneLatex = new javax.swing.JScrollPane();
        jTextPaneLatex = new javax.swing.JTextPane();
        jPanelBlank = new javax.swing.JPanel();
        jPanelUpperRight = new javax.swing.JPanel();
        jScrollPaneDirections = new javax.swing.JScrollPane();
        lblDirections = new javax.swing.JTextArea();
        jLabelAboveDirections = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuItemOpen = new javax.swing.JMenuItem();
        menuItemReload = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        menuItemClose = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuItemTeacherTool = new javax.swing.JMenuItem();
        menuItemScratchPad = new javax.swing.JMenuItem();
        menuView = new javax.swing.JMenu();
        jCheckBoxMenuItemPresentationMode = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Interactive Exercise Solver");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        jSplitPaneMain.setDividerLocation(300);
        jSplitPaneMain.setOneTouchExpandable(true);
        jSplitPaneMain.setPreferredSize(new java.awt.Dimension(1280, 720));

        jSplitPaneLeftHalf.setDividerLocation(300);
        jSplitPaneLeftHalf.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneLeftHalf.setOneTouchExpandable(true);

        jTreeExerciseFile.setFont(new java.awt.Font("Serif", 0, 14)); // NOI18N
        jTreeExerciseFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTreeExerciseFileMouseClicked(evt);
            }
        });
        jTreeExerciseFile.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                onExerciseTreeValueChanged(evt);
            }
        });
        jScrollPaneUpperLeft.setViewportView(jTreeExerciseFile);

        jSplitPaneLeftHalf.setTopComponent(jScrollPaneUpperLeft);

        jPanelLowerLeft.setMinimumSize(new java.awt.Dimension(60, 84));
        jPanelLowerLeft.setLayout(new java.awt.GridBagLayout());

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanelNavigationButtons.add(btnNext, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelLowerLeft.add(jPanelNavigationButtons, gridBagConstraints);

        jScrollPaneIdentifierTypes.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneIdentifierTypes.setBorder(javax.swing.BorderFactory.createTitledBorder("Typing Conventions"));
        jScrollPaneIdentifierTypes.setMinimumSize(new java.awt.Dimension(23, 160));
        jScrollPaneIdentifierTypes.setPreferredSize(new java.awt.Dimension(239, 160));

        lblIdentifierTypes.setEditable(false);
        lblIdentifierTypes.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblIdentifierTypes.setColumns(20);
        lblIdentifierTypes.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblIdentifierTypes.setLineWrap(true);
        lblIdentifierTypes.setRows(5);
        lblIdentifierTypes.setText(" ");
        lblIdentifierTypes.setWrapStyleWord(true);
        jScrollPaneIdentifierTypes.setViewportView(lblIdentifierTypes);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelLowerLeft.add(jScrollPaneIdentifierTypes, gridBagConstraints);

        jScrollPaneEnterExpressions.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneEnterExpressions.setBorder(javax.swing.BorderFactory.createTitledBorder("How to Enter Special Characters"));
        jScrollPaneEnterExpressions.setMinimumSize(new java.awt.Dimension(0, 0));

        jPanelEnterExpressions.setPreferredSize(new java.awt.Dimension(0, 150));

        lblHelpHeader.setEditable(false);
        lblHelpHeader.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblHelpHeader.setColumns(20);
        lblHelpHeader.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblHelpHeader.setLineWrap(true);
        lblHelpHeader.setRows(5);
        lblHelpHeader.setText("When typing lambda expressions, use the following keyboard shortcuts:");
        lblHelpHeader.setWrapStyleWord(true);
        lblHelpHeader.setBorder(null);
        lblHelpHeader.setMinimumSize(new java.awt.Dimension(50, 16));

        lblHelpLambda.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblHelpLambda.setText(" ");

        lblHelpBinders.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblHelpBinders.setText(" ");

        lblHelpBinaries.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblHelpBinaries.setText(" ");

        lblHelpNot.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        lblHelpNot.setText(" ");

        lblHelpConditionals.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
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
                            .add(lblHelpBinders, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblHelpBinaries, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblHelpLambda, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(lblHelpNot, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblHelpConditionals, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 229, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
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
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jScrollPaneEnterExpressions.setViewportView(jPanelEnterExpressions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLowerLeft.add(jScrollPaneEnterExpressions, gridBagConstraints);

        jSplitPaneLeftHalf.setRightComponent(jPanelLowerLeft);

        jSplitPaneMain.setLeftComponent(jSplitPaneLeftHalf);

        jSplitPaneRightHalf.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneRightHalf.setOneTouchExpandable(true);

        jPanelLowerRight.setLayout(new java.awt.GridBagLayout());

        jPanelCardLayout.setPreferredSize(new java.awt.Dimension(606, 250));
        jPanelCardLayout.setLayout(new java.awt.CardLayout());

        jPanelTypesAndConversions.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, javax.swing.UIManager.getDefaults().getColor("Separator.foreground")));
        jPanelTypesAndConversions.setMinimumSize(new java.awt.Dimension(0, 256));
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

        txtFeedback.setEditable(false);
        txtFeedback.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtFeedback.setColumns(20);
        txtFeedback.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
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

        txtQuestion.setEditable(false);
        txtQuestion.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtQuestion.setBorder(null);
        txtQuestion.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        txtQuestion.setPreferredSize(new java.awt.Dimension(460, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelQuestion.add(txtQuestion, gridBagConstraints);

        btnTransfer.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        btnTransfer.setText("Paste");
        btnTransfer.setMinimumSize(new java.awt.Dimension(60, 29));
        btnTransfer.setPreferredSize(new java.awt.Dimension(60, 29));
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

        txtUserAnswer.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
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

        jLabelAboveQuestion.setFont(new java.awt.Font("Lucida Grande", 1, 14)); // NOI18N
        jLabelAboveQuestion.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelTypesAndConversions.add(jLabelAboveQuestion, gridBagConstraints);

        jPanelCardLayout.add(jPanelTypesAndConversions, "typesAndConversionsCard");

        jSplitPaneTrees.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneTrees.setResizeWeight(1.0);
        jSplitPaneTrees.setMinimumSize(new java.awt.Dimension(5, 250));
        jSplitPaneTrees.setOneTouchExpandable(true);
        jSplitPaneTrees.setPreferredSize(new java.awt.Dimension(751, 250));

        treeDisplay.setBackground(java.awt.Color.white);
        treeDisplay.setMinimumSize(new java.awt.Dimension(0, 84));
        treeDisplay.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                treeDisplayAncestorResized(evt);
            }
        });
        jSplitPaneTrees.setLeftComponent(treeDisplay);

        jPanelNodeProperties.setMinimumSize(new java.awt.Dimension(0, 50));
        jPanelNodeProperties.setPreferredSize(new java.awt.Dimension(180, 170));
        jPanelNodeProperties.setLayout(new java.awt.CardLayout());

        jPanelDefault.setMinimumSize(new java.awt.Dimension(0, 200));
        jPanelDefault.setPreferredSize(new java.awt.Dimension(602, 200));

        org.jdesktop.layout.GroupLayout jPanelDefaultLayout = new org.jdesktop.layout.GroupLayout(jPanelDefault);
        jPanelDefault.setLayout(jPanelDefaultLayout);
        jPanelDefaultLayout.setHorizontalGroup(
            jPanelDefaultLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 626, Short.MAX_VALUE)
        );
        jPanelDefaultLayout.setVerticalGroup(
            jPanelDefaultLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 200, Short.MAX_VALUE)
        );

        jPanelNodeProperties.add(jPanelDefault, "default");

        jPanelLexicalTerminal.setLayout(new java.awt.GridLayout(1, 0));
        jPanelLexicalTerminal.add(lexiconList);

        jPanelNodeProperties.add(jPanelLexicalTerminal, "lexicalTerminal");
        jPanelNodeProperties.add(jPanelLambdaConversion, "lambdaConversion");

        jPanelRuleSelection.setPreferredSize(new java.awt.Dimension(499, 180));
        jPanelNodeProperties.add(jPanelRuleSelection, "ruleSelection");

        jPanelTrace.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("This node is a trace. It is not necessary to assign it a lexical entry.");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelTrace.add(jLabel1);

        jPanelNodeProperties.add(jPanelTrace, "trace");

        jPanelBareIndex.setLayout(new java.awt.GridLayout(1, 0));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("This node is an index for lambda abstraction. It is not necessary to assign it a lexical entry.");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelBareIndex.add(jLabel2);

        jPanelNodeProperties.add(jPanelBareIndex, "bareIndex");

        jPanelNonBranchingNonterminal.setLayout(new java.awt.GridLayout(1, 0));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("This node is a nonbranching nonterminal. Its meaning carries over from its daughter.");
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelNonBranchingNonterminal.add(jLabel3);

        jPanelNodeProperties.add(jPanelNonBranchingNonterminal, "nonBranchingNonterminal");

        jListNodeHistory.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPaneNodeHistory.setViewportView(jListNodeHistory);

        jLabel4.setText("Node History");

        org.jdesktop.layout.GroupLayout jPanelNodeHistoryLayout = new org.jdesktop.layout.GroupLayout(jPanelNodeHistory);
        jPanelNodeHistory.setLayout(jPanelNodeHistoryLayout);
        jPanelNodeHistoryLayout.setHorizontalGroup(
            jPanelNodeHistoryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelNodeHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanelNodeHistoryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPaneNodeHistory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 707, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelNodeHistoryLayout.setVerticalGroup(
            jPanelNodeHistoryLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelNodeHistoryLayout.createSequentialGroup()
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPaneNodeHistory, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelNodeProperties.add(jPanelNodeHistory, "history");

        jPanelPronoun.setLayout(new java.awt.GridLayout(1, 0));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("This node is a pronoun. It is not necessary to assign it a lexical entry.");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelPronoun.add(jLabel5);

        jPanelNodeProperties.add(jPanelPronoun, "pronoun");

        jPanelDummyTerminal.setLayout(new java.awt.GridLayout(1, 0));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("This node is vacuous. It is not necessary to assign it a lexical entry.");
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelDummyTerminal.add(jLabel6);

        jPanelNodeProperties.add(jPanelDummyTerminal, "dummyTerminal");

        jScrollPaneLatex.setViewportView(jTextPaneLatex);

        org.jdesktop.layout.GroupLayout jPanelLatexLayout = new org.jdesktop.layout.GroupLayout(jPanelLatex);
        jPanelLatex.setLayout(jPanelLatexLayout);
        jPanelLatexLayout.setHorizontalGroup(
            jPanelLatexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 626, Short.MAX_VALUE)
            .add(jPanelLatexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPaneLatex, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE))
        );
        jPanelLatexLayout.setVerticalGroup(
            jPanelLatexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 170, Short.MAX_VALUE)
            .add(jPanelLatexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jScrollPaneLatex, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
        );

        jPanelNodeProperties.add(jPanelLatex, "latex");

        jSplitPaneTrees.setRightComponent(jPanelNodeProperties);

        jPanelCardLayout.add(jSplitPaneTrees, "treesCard");

        jPanelBlank.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, javax.swing.UIManager.getDefaults().getColor("Separator.foreground")));

        org.jdesktop.layout.GroupLayout jPanelBlankLayout = new org.jdesktop.layout.GroupLayout(jPanelBlank);
        jPanelBlank.setLayout(jPanelBlankLayout);
        jPanelBlankLayout.setHorizontalGroup(
            jPanelBlankLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 630, Short.MAX_VALUE)
        );
        jPanelBlankLayout.setVerticalGroup(
            jPanelBlankLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 501, Short.MAX_VALUE)
        );

        jPanelCardLayout.add(jPanelBlank, "blank");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelLowerRight.add(jPanelCardLayout, gridBagConstraints);

        jSplitPaneRightHalf.setRightComponent(jPanelLowerRight);

        jPanelUpperRight.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("Separator.foreground")));
        jPanelUpperRight.setLayout(new java.awt.GridBagLayout());

        jScrollPaneDirections.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jScrollPaneDirections.setBorder(null);
        jScrollPaneDirections.setPreferredSize(new java.awt.Dimension(100, 100));

        lblDirections.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        lblDirections.setColumns(20);
        lblDirections.setEditable(false);
        lblDirections.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
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

        jLabelAboveDirections.setFont(new java.awt.Font("Lucida Grande", 1, 16)); // NOI18N
        jLabelAboveDirections.setText("Lambda Calculator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanelUpperRight.add(jLabelAboveDirections, gridBagConstraints);

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

        menuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemOpen.setMnemonic('O');
        menuItemOpen.setText("Open...");
        menuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenActionPerformed(evt);
            }
        });
        menuFile.add(menuItemOpen);

        menuItemReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemReload.setText("Reload");
        menuItemReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemReloadActionPerformed(evt);
            }
        });
        menuFile.add(menuItemReload);

        menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemSave.setMnemonic('S');
        menuItemSave.setText("Save");
        menuItemSave.setEnabled(false);
        menuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSave);

        menuItemSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        menuItemSaveAs.setMnemonic('a');
        menuItemSaveAs.setText("Save As...");
        menuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSaveAs);
        menuFile.add(jSeparator1);

        menuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

        menuView.setMnemonic('v');
        menuView.setText("View");

        jCheckBoxMenuItemPresentationMode.setMnemonic('p');
        jCheckBoxMenuItemPresentationMode.setText("Presentation Mode");
        jCheckBoxMenuItemPresentationMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemPresentationModeActionPerformed(evt);
            }
        });
        menuView.add(jCheckBoxMenuItemPresentationMode);

        jMenuBar1.add(menuView);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxMenuItemPresentationModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemPresentationModeActionPerformed
        if (!inPresentationMode) {
            jSplitPaneMain.setDividerLocation(0.0);
            jSplitPaneMainLastDividerSize = jSplitPaneMain.getDividerSize();
            jSplitPaneMain.setDividerSize(0);
            jSplitPaneMain.setEnabled(false);
            
            jSplitPaneRightHalf.setDividerLocation(0.0);
            jSplitPaneRightHalfLastDividerSize = jSplitPaneRightHalf.getDividerSize();
            jSplitPaneRightHalf.setDividerSize(0);
            jSplitPaneRightHalf.setEnabled(false);
            //jSplitPaneTrees.setDividerLocation(1.0);
            inPresentationMode=true;
            
      
        } else {
            jSplitPaneMain.setDividerLocation(jSplitPaneMain.getLastDividerLocation());
            jSplitPaneMain.setDividerSize(jSplitPaneMainLastDividerSize);
            jSplitPaneMain.setEnabled(true);
            
            jSplitPaneRightHalf.setDividerLocation(jSplitPaneRightHalf.getLastDividerLocation());
            jSplitPaneRightHalf.setDividerSize(jSplitPaneRightHalfLastDividerSize);
            jSplitPaneRightHalf.setEnabled(true);
            //jSplitPaneTrees.setDividerLocation(jSplitPaneTrees.getLastDividerLocation());
            
            inPresentationMode = false;
        }
    }//GEN-LAST:event_jCheckBoxMenuItemPresentationModeActionPerformed

    boolean inPresentationMode = false;
    int jSplitPaneMainLastDividerSize;
    int jSplitPaneRightHalfLastDividerSize;
    
    private void treeDisplayAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_treeDisplayAncestorResized
// TODO get rid of this method (I generated it by mistake and Netbeans won't let me)
    }//GEN-LAST:event_treeDisplayAncestorResized

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
            txtUserAnswer.deleteAnyTemporaryText();
            String string = txtUserAnswer.getText().trim();
            if (!string.equals(txtUserAnswer.getText()))
                txtUserAnswer.setText(string);
            
            AnswerStatus status = ex.checkAnswer(string);
            if (status.isCorrect() && status.endsExercise()) {
                String response = status.getMessage() + " ";
                if (btnNext.isEnabled() && !getCurrentExFile().hasBeenCompleted()) {
                    response += "Press Return to go on to the next exercise.";
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
            
            repaintExerciseFileTree();
            
            setAnswerEnabledState(); // update enabled state of controls
            
            if (status.isCorrect())
                notifyUserHasUnsavedWork();
            
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

    public void repaintExerciseFileTree() {
        jTreeExerciseFile.repaint();
    }
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
        notifyUserHasUnsavedWork();
        jTreeExerciseFile.repaint();
        showExercise();
    }//GEN-LAST:event_btnDoAgainActionPerformed

    private void btnTransferActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferActionPerformed
        //make any changes also in NonterminalReductionPanel and ScratchPadWindow
        if (txtQuestion.getSelectedText() == null) {
            txtUserAnswer.setText(txtQuestion.getText());
        } else {
            txtUserAnswer.setText(txtQuestion.getSelectedText());
        }
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
        if (this.getCurrentExFile().getStudentName() == null) {
            String studentName = JOptionPane.showInputDialog
                    (this, "Please enter your first and last name: ",
                    "Lambda", JOptionPane.QUESTION_MESSAGE);
            if (studentName == null) return; // cancelled
            if (studentName.trim().length() == 0) return; // treat as cancelled, not a valid student name, would cause setStudentName to throw
            this.getCurrentExFile().setStudentName(studentName);
        }
        
        if(lambdacalc.gui.Util.isMac()) {
            // use the native file dialog on the mac
            FileDialog dialog = new FileDialog(this, "Save As", FileDialog.SAVE);
            dialog.setFilenameFilter(new FilenameFilter(){
               public boolean accept(File dir, String name){
                   return name.endsWith(SERIALIZED_FILE_SUFFIX);
               }
            });
            dialog.setFile(usersWorkFile.getName());
            dialog.setDirectory(usersWorkFile.getParent());
            dialog.setVisible(true);
            String targetName = dialog.getFile();
            String targetDir = dialog.getDirectory();
            File target = new File(targetDir + targetName);
            // Mac already asks for confirmation before saving
//            if (target.exists()) {
//                int n = JOptionPane.showOptionDialog(this,
//                "Overwrite " + target.getPath() + "?",
//                "Lambda Calculator",
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null, null, null);
//               if (n != JOptionPane.OK_OPTION)
//                   return;
//            }
            if (!target.toString().endsWith("."+SERIALIZED_FILE_SUFFIX)) {
                target = new File(target.toString()+"."+SERIALIZED_FILE_SUFFIX);
            }
            writeUsersWorkFile(target);
        } else {
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
            "At least some of your answers have not yet been saved. What should I do?",
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
        // switch back to non-presentation mode
        if (this.inPresentationMode) {
            jCheckBoxMenuItemPresentationMode.doClick();
        }
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
        
        if (lambdacalc.gui.Util.isMac()) {
            // If Mac, opens a native dialog window named "Open"
            FileDialog dialog = new FileDialog(this, "Open", FileDialog.LOAD);
            dialog.setFilenameFilter(new FilenameFilter(){
               public boolean accept(File dir, String name){
                   return (name.endsWith(".txt") || name.endsWith(SERIALIZED_FILE_SUFFIX));
               }
            });
            if (lastOpenDir != null) {
                dialog.setDirectory(System.getProperty(lastOpenDir));
            } else {
                if (new File(System.getProperty("user.home") + "/Downloads/").exists()){
                    dialog.setDirectory(System.getProperty("user.home") + "/Downloads/");
                } else {
                    dialog.setDirectory(System.getProperty("user.home"));
                }
            }
            dialog.setVisible(true);
            String file = dialog.getFile();

            if (file != null) {
                String path = dialog.getDirectory() + file;
                loadExerciseFile(new File(path));
                setTitle(file);
                lastOpenDir = dialog.getDirectory();

            }
        } else {
            jFileChooser1.setFileFilter(this.allRecognizedFiles);
            int returnVal = jFileChooser1.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                loadExerciseFile(jFileChooser1.getSelectedFile()); 
                setTitle(jFileChooser1.getSelectedFile().getName());
            }
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

    private void menuItemReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemReloadActionPerformed

//        int formerGroup = this.currentGroup;
//        int formerEx = this.currentEx;

        loadExerciseFile(this.currentFile);

//        this.currentGroup = formerGroup;
//        this.currentEx = formerEx;
//
//        showExercise();
}//GEN-LAST:event_menuItemReloadActionPerformed

    // This is set by any exercise that specifies types
    public static void setCurrentTypingConventions(IdentifierTyper typer) {
        currentTypingConventions = typer;
    }
    public static IdentifierTyper getCurrentTypingConventions() {
        return currentTypingConventions;
    }
    
    
    // called in:
    // btnDoAgainActionPerformed()
    // onCheckAnswer()
    // TreeExerciseWidget
    public void notifyUserHasUnsavedWork() {
        hasUnsavedWork = true;
        if (hasUserSaved)
            menuItemSave.setEnabled(true);
    }
    
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    public static ExerciseFile getCurrentExFile() {
        return currentExFile;
    }
    
    private class LexiconListChangeListener implements LexiconList.ChangeListener {
        public void changeMade() {
            notifyUserHasUnsavedWork();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckAnswer;
    private javax.swing.JButton btnDoAgain;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnTransfer;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemPresentationMode;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelAboveDirections;
    private javax.swing.JLabel jLabelAboveQuestion;
    private javax.swing.JList jListNodeHistory;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanelBareIndex;
    private javax.swing.JPanel jPanelBlank;
    private javax.swing.JPanel jPanelCardLayout;
    private javax.swing.JPanel jPanelDefault;
    private javax.swing.JPanel jPanelDummyTerminal;
    private javax.swing.JPanel jPanelEnterExpressions;
    private lambdacalc.gui.NonterminalReductionPanel jPanelLambdaConversion;
    private javax.swing.JPanel jPanelLatex;
    private javax.swing.JPanel jPanelLexicalTerminal;
    private javax.swing.JPanel jPanelLowerLeft;
    private javax.swing.JPanel jPanelLowerRight;
    private javax.swing.JPanel jPanelNavigationButtons;
    private javax.swing.JPanel jPanelNodeHistory;
    private javax.swing.JPanel jPanelNodeProperties;
    private javax.swing.JPanel jPanelNonBranchingNonterminal;
    private javax.swing.JPanel jPanelPronoun;
    private javax.swing.JPanel jPanelQuestion;
    private lambdacalc.gui.RuleSelectionPanel jPanelRuleSelection;
    private javax.swing.JPanel jPanelTrace;
    private javax.swing.JPanel jPanelTypesAndConversions;
    private javax.swing.JPanel jPanelUpperRight;
    private javax.swing.JScrollPane jScrollPaneDirections;
    private javax.swing.JScrollPane jScrollPaneEnterExpressions;
    private javax.swing.JScrollPane jScrollPaneFeedback;
    private javax.swing.JScrollPane jScrollPaneIdentifierTypes;
    private javax.swing.JScrollPane jScrollPaneLatex;
    private javax.swing.JScrollPane jScrollPaneNodeHistory;
    private javax.swing.JScrollPane jScrollPaneUpperLeft;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPaneLeftHalf;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JSplitPane jSplitPaneRightHalf;
    private javax.swing.JSplitPane jSplitPaneTrees;
    private javax.swing.JTextPane jTextPaneLatex;
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
    private javax.swing.JMenuItem menuItemReload;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveAs;
    private javax.swing.JMenuItem menuItemScratchPad;
    private javax.swing.JMenuItem menuItemTeacherTool;
    private javax.swing.JMenu menuTools;
    private javax.swing.JMenu menuView;
    private lambdacalc.gui.TreeExerciseWidget treeDisplay;
    private javax.swing.JTextArea txtFeedback;
    private lambdacalc.gui.LambdaEnabledTextField txtQuestion;
    private lambdacalc.gui.LambdaEnabledTextField txtUserAnswer;
    // End of variables declaration//GEN-END:variables
    
}
