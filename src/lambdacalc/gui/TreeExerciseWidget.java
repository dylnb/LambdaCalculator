package lambdacalc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import lambdacalc.logic.*;
import lambdacalc.lf.*;
import lambdacalc.exercises.*;
import lambdacalc.gui.tree.TreeCanvas;

public class TreeExerciseWidget extends JPanel {
    TreeCanvas canvas;
    Nonterminal lftree;
    Map lfToOrthoLabel = new HashMap();
    Map lfToMeaningLabel = new HashMap();
    Map lfToMeaningState = new HashMap();
    Map lfToParent = new HashMap();
    
    LFNode curEvaluationNode = null;
    
    private class MeaningState {
        public Vector exprs = new Vector();
        public int curexpr = 0;
        public String evaluationError;
        
        public MeaningState(String error) {
            evaluationError = error;
        }
        
        public MeaningState(Expr meaning) {
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
        setLayout(new FlowLayout());
        
        canvas = new TreeCanvas();
        add(canvas);
        
        JButton bspace = new JButton("Space");
        bspace.addActionListener(new SpaceActionListener());
        add(bspace);
        JButton benter = new JButton("Enter");
        benter.addActionListener(new EnterActionListener());
        add(benter);

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
        lftree.guessLexicalChoices(file.getLexicon());
        
        lfToOrthoLabel.clear();
        lfToMeaningLabel.clear();
        lfToMeaningState.clear();
        lfToParent.clear();
        
        canvas.getRoot().clearChildren();
        buildTree(canvas.getRoot(), lftree);
    }
    
    void buildTree(TreeCanvas.TreeNode treenode, LFNode lfnode) {
        Panel label = new Panel();
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
        
        if (lfnode instanceof Terminal) {
           Terminal t = (Terminal)lfnode;
           try {
               Expr m = t.getMeaning();
               lfToMeaningState.put(t, new MeaningState(m));
           } catch (Exception e) {
           }
        }
        
        updateNode(lfnode);
        
        if (lfnode instanceof Nonterminal) {
            Nonterminal nt = (Nonterminal)lfnode;
            for (int i = 0; i < nt.size(); i++) {
                lfToParent.put(nt.getChild(i), nt);
                buildTree(treenode.addChild(), nt.getChild(i));
            }
        }
    }
    
    void updateNode(LFNode node) {
        String label = node.getLabel();
        if (node == curEvaluationNode)
            label = ">>" + label + "<<";
    
        JLabel orthoLabel = (JLabel)lfToOrthoLabel.get(node);
        orthoLabel.setText(label);
        
        JLabel meaningLabel = (JLabel)lfToMeaningLabel.get(node);
        if (lfToMeaningState.containsKey(node)) {
            MeaningState ms = (MeaningState)lfToMeaningState.get(node);
            if (ms.evaluationError == null)
                meaningLabel.setText(ms.exprs.get(ms.curexpr).toString());
            else
                meaningLabel.setText(ms.evaluationError);
        }
        canvas.doLayout();
    }
    
    void moveTo(LFNode node) {
        if (curEvaluationNode != null) {
            LFNode oldcurEvaluationNode = curEvaluationNode;
            curEvaluationNode = null;
            updateNode(oldcurEvaluationNode);
        }
        
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
                    moveTo(child);
                    return;
                }
                MeaningState ms = (MeaningState)lfToMeaningState.get(child);
                if (ms.evaluationError != null) {
                    moveTo(child);
                    return;
                }
                if (ms.curexpr < ms.exprs.size()-1) {
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
        
        if (curEvaluationNode == null)
            return;
            
        if (!lfToMeaningState.containsKey(curEvaluationNode)) {
            try {
                Expr m = curEvaluationNode.getMeaning();
                lfToMeaningState.put(curEvaluationNode, new MeaningState(m));
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
                frame.add(new TreeExerciseWidget());
                frame.setVisible(true);
            }
        });
    }
}
