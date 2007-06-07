package lambdacalc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import lambdacalc.logic.*;
import lambdacalc.lf.*;
import lambdacalc.exercises.*;
import lambdacalc.gui.tree.TreeCanvas;

/**
 * This widget wraps a TreeCanvas and controls the user interaction
 * with the LF tree, revealing the propostional content of nodes as
 * space and enter are pressed.
 * 
 * Each node in the tree can be in one of a few states:
 *   - Not evaluated yet; no lambda expression is displayed
 *   - Evaluation attempted but failed; error message is displayed
 *   - Evaluated; lambda expression is displayed, but not simplified
 *   - Simplified, through one or more applications of lambda conversion
 * A nonterminal is (attempted to be) evaluated only if:
 *    All child nodes are evaluated
 *    The node has a composition rule assigned
 */
public class TreeExerciseWidget extends JPanel {
    TreeCanvas canvas; // this is the display widget
    Nonterminal lftree; // this is the tree we're displaying
    
    // Maps from LFNodes in lftree to controls being displayed and other state.
    Map lfToOrthoLabel = new HashMap(); // orthographic label
    Map lfToMeaningLabel = new HashMap(); // propositional content label
    Map lfToMeaningState = new HashMap(); // state of the propositional label, or null if node is not evaluated yet
    Map lfToParent = new HashMap(); // parent LFNode
    
    // At any given time, at most one LFNode is the current
    // evaluation node, which is highlighted and represents
    // the node that is affected by pressing space or enter.
    // This is null if no node is the curret evaluation node.
    LFNode curEvaluationNode = null;
    
    // This class encapsulates the evaluated/simplified state of a node.
    // If the evaluation resulted in an error, evaluationError is set
    // to a message and the other fields are unfilled. Otherwise,
    // exprs represents the evaluated meaning (index 0), and successive
    // steps of lambda conversion simplifications (indices >= 1).
    // curexpr indicates the index of the Expr in exprs that is currently
    // shown on screen. The user may be able to step back and forward
    // through the simplification steps.
    private class MeaningState {
        public Vector exprs = new Vector(); // of Expr objects, simplification steps
        public int curexpr = 0; // step currently shown on screen
        public String evaluationError; // error message if evaluation failed
        
        public MeaningState(String error) {
            evaluationError = error;
        }
        
        public MeaningState(Expr meaning) {
            // Add the expression and simplification steps of it to exprs.
            while (true) {
                exprs.add(meaning);
                try {
                    Expr.LambdaConversionResult r = meaning.performLambdaConversion();
                    if (r == null) break;
                    meaning = r.result;
                } catch (TypeEvaluationException tee) {
                    evaluationError = tee.getMessage();
                    return;
                }
            }
        }
    }

    public TreeExerciseWidget() {
        
        setLayout(new BorderLayout());
        
        canvas = new TreeCanvas();
        add(canvas, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        
        JButton bspace = new JButton("Space");
        bspace.addActionListener(new SpaceActionListener());
        buttons.add(bspace);
        JButton benter = new JButton("Enter");
        benter.addActionListener(new EnterActionListener());
        buttons.add(benter);

        add(buttons, BorderLayout.PAGE_END);
        
        try {
            ExerciseFile file = ExerciseFileParser.parse(new java.io.FileReader("examples/example2.txt"));
            initialize(file, (TreeExercise)file.getGroup(0).getItem(0));
            moveTo(lftree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void initialize(ExerciseFile file, TreeExercise ex) {
        lftree = ex.getTree();
        lftree.guessLexicalChoices(file.getLexicon(), file.getRules());
        
        lfToOrthoLabel.clear();
        lfToMeaningLabel.clear();
        lfToMeaningState.clear();
        lfToParent.clear();
        
        canvas.getRoot().clearChildren();
        buildTree(canvas.getRoot(), lftree);
    }
    
    // Recursively construct the TreeCanvas structure to reflect
    // the structure of the LFNode subtree.
    void buildTree(TreeCanvas.JTreeNode treenode, LFNode lfnode) {
        Panel label = new Panel(); // this is the control made the node label for this node
        BoxLayout bl = new BoxLayout(label, BoxLayout.Y_AXIS);
        label.setLayout(bl);
        
        JLabel orthoLabel = new JLabel(lfnode.getLabel());
        label.add(orthoLabel);
        lfToOrthoLabel.put(lfnode, orthoLabel);
        
        JLabel meaningLabel = new JLabel();
        meaningLabel.setFont(lambdacalc.gui.Util.getUnicodeFont(14));
        label.add(meaningLabel);
        lfToMeaningLabel.put(lfnode, meaningLabel);
        
        treenode.setLabel(label);
        
        // If we're adding a terminal node, and if a lexical entry
        // has been assigned to it, then make that node immediately
        // "evaluated".
        if (lfnode instanceof Terminal) {
           Terminal t = (Terminal)lfnode;
           try {
               Expr m = t.getMeaning();
               lfToMeaningState.put(t, new MeaningState(m));
           } catch (Exception e) {
           }
        }
        
        // Update the display of the node.
        updateNode(lfnode);
        
        // Recursively build child nodes.
        if (lfnode instanceof Nonterminal) {
            Nonterminal nt = (Nonterminal)lfnode;
            for (int i = 0; i < nt.size(); i++) {
                lfToParent.put(nt.getChild(i), nt);
                buildTree(treenode.addChild(), nt.getChild(i));
            }
        }
    }
    
    // Update the visual display of the node.
    void updateNode(LFNode node) {
        // Change the label if it's the current evaluation node.
        String label = node.getLabel();
        if (node == curEvaluationNode)
            label = ">>" + label + "<<";
    
        JLabel orthoLabel = (JLabel)lfToOrthoLabel.get(node);
        orthoLabel.setText(label);
        
        // Update the lambda expression displayed, if it's been evaluated.
        // If an error ocurred during evaluation, display it. Otherwise
        // display the lambda expression.
        JLabel meaningLabel = (JLabel)lfToMeaningLabel.get(node);
        if (lfToMeaningState.containsKey(node)) { // has the node been evaluated?
            MeaningState ms = (MeaningState)lfToMeaningState.get(node);
            if (ms.evaluationError == null) // was there an error?
                meaningLabel.setText(ms.exprs.get(ms.curexpr).toString());
            else
                meaningLabel.setText(ms.evaluationError);
        }
        
        // Ensure tree layout is adjusted due to changes to node label.
        // This ought to be automatic, but isn't.
        canvas.doLayout();
    }
    
    // Move the current evaluation node to the node indicated, but only
    // if all of its children have been fully evaluated and simplified. If they
    // haven't, move to the first not fully simplified child.
    void moveTo(LFNode node) {
        if (curEvaluationNode != null) {
            // Update the display of the previous current node so that
            // it dislays as non-current.
            LFNode oldcurEvaluationNode = curEvaluationNode;
            curEvaluationNode = null;
            updateNode(oldcurEvaluationNode);
        }
        
        // If we're not making any new node current, end here.
        if (node == null) {
            curEvaluationNode = null;
            return;
        }
        
        if (node instanceof Nonterminal) {
            // If any of this node's children haven't maxed out their
            // meaning simplifications, move to them.
            for (int i = 0; i < ((Nonterminal)node).size(); i++) {
                LFNode child = ((Nonterminal)node).getChild(i);
                if (!lfToMeaningState.containsKey(child)) {
                    // Child not evaluated yet: move to the child
                    moveTo(child);
                    return;
                }
                MeaningState ms = (MeaningState)lfToMeaningState.get(child);
                if (ms.evaluationError != null) {
                    // Child evaluation had an error: move to the child.
                    moveTo(child);
                    return;
                }
                if (ms.curexpr < ms.exprs.size()-1) {
                    // Child not simplified: move to the child.
                    moveTo(child);
                    return;
                }
            }
        }
        
        // All of the children are fully computed, so we can move
        // to this node.
        curEvaluationNode = node;
        updateNode(curEvaluationNode);
    }
    
    void doSpace() {
        // This evaluates the meaning of a node, if it hasn't been evaluated,
        // and steps through the simplifications of the meaning, but never
        // moves on to another node.
        
        if (curEvaluationNode == null) // nothing is selected?
            return;
        
        // Node hasn't been evaluated yet. Evaluate it.
        if (!lfToMeaningState.containsKey(curEvaluationNode)) {
            try {
                Expr m = curEvaluationNode.getMeaning();
                lfToMeaningState.put(curEvaluationNode, new MeaningState(m)); // no error ocurred
            } catch (MeaningEvaluationException e) {
                lfToMeaningState.put(curEvaluationNode, new MeaningState(e.getMessage()));
            }
            updateNode(curEvaluationNode);
            return;
        }

        MeaningState ms = (MeaningState)lfToMeaningState.get(curEvaluationNode);
        if (ms.evaluationError != null) {
            // Can't resolve error. User must probably edit something
            // in the tree.
            return;
        }
        
        // Node is evaluated but not fully simplified, so go to the next
        // simplification step.
        if (ms.curexpr < ms.exprs.size()-1) {
            ms.curexpr++;
            updateNode(curEvaluationNode);
            return;
        }
        
        // This expression is fully evaluated. 
    }
    
    void doEnter() {
        // This fully evaluates the current node, if it has not been
        // evaluated yet, but if it has been fully evaluated, then
        // we move to the next node and evaluate it, but don't
        // simplify it.
        if (curEvaluationNode == null)
            return;
            
        if (!lfToMeaningState.containsKey(curEvaluationNode)) {
            // Start the evaluation of this node.
            doSpace();

            MeaningState ms = (MeaningState)lfToMeaningState.get(curEvaluationNode);
            if (ms.evaluationError != null) {
                // Can't resolve error. User must probably edit something
                // in the tree.
                return;
            } else {
                // Move the evaluation to the end.
                ms.curexpr = ms.exprs.size()-1;
                updateNode(curEvaluationNode);
                return;
            }
        }
        
        MeaningState ms = (MeaningState)lfToMeaningState.get(curEvaluationNode);
        if (ms.curexpr < ms.exprs.size()-1) {
            // Move the evaluation to the end.
            ms.curexpr = ms.exprs.size()-1;
            updateNode(curEvaluationNode);
            return;
        }
        
        // This expression is fully evaluated. 
        if (lfToParent.containsKey(curEvaluationNode)) {
            Nonterminal parent = (Nonterminal)lfToParent.get(curEvaluationNode);
            moveTo(parent);
        }
    }
    
    class SpaceActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doSpace();
        }
    }
    class EnterActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doEnter();
        }
    }
    
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setSize(640, 480);
                frame.getContentPane().add(new TreeExerciseWidget());
                frame.setVisible(true);
            }
        });
    }
}
