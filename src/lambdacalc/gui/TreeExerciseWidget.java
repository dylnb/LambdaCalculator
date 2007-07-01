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
    private TreeExercise exercise; // this is the exercise we're displaying
    Nonterminal lftree; // this is the tree we're displaying
    
    JScrollPane scrollpane;
    TreeCanvas canvas; // this is the display widget
    
    // Maps from LFNodes in lftree to controls being displayed and other state.
    Map lfToTreeLabelPanel = new HashMap(); // panel containing ortho label, propositional content
    Map lfToOrthoLabel = new HashMap(); // orthographic label
    Map lfToMeaningLabel = new HashMap(); // propositional content label (for nonterminals only if we're using dropdowns for terminals)
    Map lfToMeaningState; // state of the propositional label, or null if node is not evaluated yet
    Map lfToParent = new HashMap(); // parent LFNode
    
    JTextArea errorLabel = new JTextArea(" "); // label containing error messages
    // we initialize it with a whitespace to make sure it takes the vertical place it needs later
    // (not sure if this is really necessary but just to be on the safe side) 
    
    // At any given time, at most one LFNode is the currently
    // selected node, which is highlighted and represents
    // the node that is affected by the buttons.
    // This is null if no node is the current evaluation node.
    LFNode selectedNode = null;
    
    // This listener needs to be an instance variable so we can access and remove
    // it in the subclass FullScreenTreeExerciseWidget    
    protected FullScreenActionListener fullScreenActionListener 
            = new FullScreenActionListener();
    
    // Buttons
    JButton btnSimplify = new JButton("Simplify Node");
    JButton btnUnsimplify = new JButton("Undo Simplify");
    JButton btnNextStep = new JButton("Evaluate Node Fully");
    JButton btnPrevStep = new JButton("Undo Evaluation");
    JButton btnFullScreen = new JButton("Full Screen");
    JButton btnFontIncrease = new JButton("A\u2191");
    JButton btnFontDecrease = new JButton("A\u2193");
    
    // Selection listeners
    Vector listeners = new Vector();
    
    NodePropertyChangeListener nodeListener = new NodePropertyChangeListener();
    
    int curFontSize = 14;
    
    /**
     * This class encapsulates the evaluated/simplified state of a node.
     * If the evaluation resulted in an error, evaluationError is set
     * to a message and the other fields are unfilled. Otherwise,
     * exprs represents the evaluated meaning (index 0), and successive
     * steps of lambda conversion simplifications (indices >= 1).
     * curexpr indicates the index of the Expr in exprs that is currently
     * shown on screen. The user may be able to step back and forward
     * through the simplification steps.
     *
     * When we create and remove meaning states from ltToMeaningState, we have
     * to also keep in sync the exprs vector here and the one in the Nonterminal
     * this is for (if it's for a nonterminal), so when we save and load the
     * nonterminals from .lbd files, we save the simplification states.
     */

    private class MeaningState {
        public Vector exprs = new Vector(); // of Expr objects, simplification steps
        public int curexpr = 0; // step currently shown on screen
        public String evaluationError; // error message if evaluation failed
        
        public MeaningState(String error) {
            evaluationError = error;
        }
        
        public MeaningState(Expr meaning) {
            // Add the expression and simplification steps of it to exprs.
            
            exprs.add(meaning);

            try {
                Expr meaning2 = MeaningBracketExpr.replaceAllMeaningBrackets(meaning);
                if (!meaning.equals(meaning2))
                    exprs.add(meaning2);
                meaning = meaning2;
            } catch (TypeEvaluationException tee) {
                evaluationError = tee.getMessage();
                return;
            } catch (MeaningEvaluationException mee) {
                evaluationError = mee.getMessage();
                return;
            }
            
            // When we're in God mode, we pre-simplify the expression so we know
            // all of the steps in the simplification ahead of time. When not in
            // God mode, we stop immediately at the first step, after the freebie
            // above of taking away the meaning brackets. Additional methods
            // are provided for advancing the simplification state, which appends
            // simplification steps into this state.
            if (!lambdacalc.Main.GOD_MODE)
                return;
            
            while (true) {
                try {
                    Expr.LambdaConversionResult r = meaning.performLambdaConversion();
                    if (r == null) break;
                    meaning = r.result;
                    exprs.add(meaning);
                } catch (TypeEvaluationException tee) {
                    evaluationError = tee.getMessage();
                    return;
                }
            }
        }
        
        public MeaningState(Vector steps) {
            exprs = steps;
            curexpr = exprs.size() - 1;
        }
    }
    

    
    public interface SelectionListener {
        void selectionChanged(SelectionEvent evt);
    }
    
    public class SelectionEvent extends EventObject {
        
        public SelectionEvent(LFNode source) {
            super(source);
        }
    }

    public TreeExerciseWidget() {
        
        setLayout(new BorderLayout());
        

        
        scrollpane = new JScrollPane();
        canvas = new TreeCanvas();
        scrollpane.setViewportView(canvas);
        add(scrollpane, BorderLayout.CENTER);
        
        errorLabel.setForeground(java.awt.Color.RED);
        errorLabel.setLineWrap(true);
        errorLabel.setWrapStyleWord(true);
        errorLabel.setEditable(false);
        errorLabel.setMargin(new Insets(3,3,3,3));
        
        add(errorLabel, BorderLayout.PAGE_END);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        
        btnSimplify.addActionListener(new SimplifyActionListener());
        buttons.add(btnSimplify);
        btnSimplify.setToolTipText("Perform one evaluation step on the selected node.");
        
        btnUnsimplify.addActionListener(new UnsimplifyActionListener());
        buttons.add(btnUnsimplify);
        btnUnsimplify.setToolTipText("Undo one simplification step on the selected node.");
        
        btnNextStep.addActionListener(new NextStepActionListener());
        buttons.add(btnNextStep);
        btnNextStep.setToolTipText("Fully evaluate the current node and move up the tree.");

        btnPrevStep.addActionListener(new PrevStepActionListener());
        buttons.add(btnPrevStep);
        btnPrevStep.setToolTipText("Undo the evaluation of the current node and go backwards on the tree.");

        btnFontIncrease.addActionListener(new FontIncreaseActionListener());
        buttons.add(btnFontIncrease);
        btnFontIncrease.setToolTipText("Increase font size.");

        btnFontDecrease.addActionListener(new FontDecreaseActionListener());
        buttons.add(btnFontDecrease);
        btnFontDecrease.setToolTipText("Decrease font size.");
        
        // fullScreenActionListener needs to be an instance var so we can access and remove it in the 
        // FullScreenTreeExerciseWidget
        btnFullScreen.addActionListener(fullScreenActionListener);
        buttons.add(btnFullScreen);
        btnFullScreen.setToolTipText("Show the tree in full screen view.");
        
        add(buttons, BorderLayout.PAGE_START);
    }
    
    public void setBackground(java.awt.Color color) {
        super.setBackground(color);
        if (scrollpane == null) return;
        scrollpane.setBackground(color);
        scrollpane.getViewport().setBackground(color);
        canvas.setBackground(color);
    }
    
    public void addSelectionListener(SelectionListener sl) {
        if (!listeners.contains(sl))
            listeners.add(sl);
    }
    
    public void removeSelectionListener(SelectionListener sl) {
        listeners.remove(sl);
    }
    
    public void clear() {
        // Remove us as a property change listener from all nodes
        for (Iterator lfnodes = lfToTreeLabelPanel.keySet().iterator(); lfnodes.hasNext(); ) {
            LFNode node = (LFNode)lfnodes.next();
            node.removePropertyChangeListener(nodeListener);
        }
        
        lftree = null;
        selectedNode = null;
        
        lfToTreeLabelPanel.clear();
        lfToOrthoLabel.clear();
        lfToMeaningLabel.clear();
        lfToMeaningState = null;
        lfToParent.clear();
        
        canvas.clear();
        
        updateButtonEnabledState();
    }
    
    public void initialize(TreeExercise ex) {
        clear();
        
        exercise = ex;
        lftree = ex.getTree();
        lfToMeaningState = ex.derivationDisplayState;
        
        buildTree(canvas.getRoot(), lftree);
        
        // Ensure tree layout is adjusted due to changes to node label.
        // This ought to be automatic, but isn't.
        canvas.invalidate();
        
        moveTo(lftree);
    }
    
    private class JHTMLLabel extends JTextPane {
        public JHTMLLabel() {
            setEditable(false);
            setContentType("text/html");
            setFocusable(false);
            setMargin(new Insets(0,0,0,0));
        }
    }
    
    // Recursively construct the TreeCanvas structure to reflect
    // the structure of the LFNode subtree.
    void buildTree(TreeCanvas.JTreeNode treenode, LFNode lfnode) {
        lfnode.addPropertyChangeListener(nodeListener);
    
        JPanel label = new JPanel(); // this is the control made the node label for this node
        label.setBackground(getBackground());
        BoxLayout bl = new BoxLayout(label, BoxLayout.Y_AXIS);
        label.setLayout(bl);
        lfToTreeLabelPanel.put(lfnode, label);
        
        label.addMouseListener(new NodeClickListener(lfnode));
        
        JHTMLLabel orthoLabel = new JHTMLLabel();
        label.add(orthoLabel);
        orthoLabel.setAlignmentX(.5F);
        orthoLabel.addMouseListener(new NodeClickListener(lfnode));
        lfToOrthoLabel.put(lfnode, orthoLabel);
        
        JLabel meaningLabel = new JLabel();
        label.add(meaningLabel);
        meaningLabel.setAlignmentX(.5F);
        //meaningLabel.addMouseListener(new NodeClickListener(lfnode));
        lfToMeaningLabel.put(lfnode, meaningLabel);

        // If the Terminal already has a lexical entry assigned,
        // initialize its meaning state.
        if (lfnode instanceof Terminal) {
           Terminal t = (Terminal)lfnode;
           try {
               Expr m = t.getMeaning();
               lfToMeaningState.put(t, new MeaningState(m));
           } catch (Exception e) {
           }
        }
        
        // If the nonterminal has saved simplification steps, restore them.
        if (lfnode instanceof Nonterminal) {
            Nonterminal nt = (Nonterminal)lfnode;
            if (nt.getUserMeaningSimplification() != null)
               lfToMeaningState.put(nt, new MeaningState(nt.getUserMeaningSimplification()));
        }
        
        treenode.setLabel(label);
        
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
    
    private void onUserChangedNodeMeaning(LFNode node) {
        // If the node was a terminal, its lexical value
        // may have changed, so we have to reset its
        // meaning state to the beginning.
        if (node instanceof Terminal) {
           Terminal t = (Terminal)node;
           try {
               Expr m = t.getMeaning();
               lfToMeaningState.put(t, new MeaningState(m));
           } catch (Exception e) {
           }
           
        } else {
           // For nonterminals, we just clear the meaning state.
            lfToMeaningState.remove(node);
            ((Nonterminal)node).setUserMeaningSimplification(null);
        }

        updateNode(node);
    
        // Clear the meaning states of the parent nodes.
        Nonterminal ancestor = (Nonterminal)lfToParent.get(node);
        while (ancestor != null) {
            lfToMeaningState.remove(ancestor);
            ancestor.setUserMeaningSimplification(null);
            updateNode(ancestor);
            ancestor = (Nonterminal)lfToParent.get(ancestor);
        }

            
        // Ensure tree layout is adjusted due to changes to node label.
        // This ought to be automatic, but isn't.
        canvas.invalidate();
    }
    
    private class NodeClickListener extends MouseAdapter {
        LFNode node;
        
        public NodeClickListener(LFNode node) { this.node = node; }
    
        public void mouseClicked(MouseEvent e) {
            setSelectedNode(node);
        }
    }
    
    public LFNode getSelectedNode() {
        return selectedNode;
    }
    
    public void setSelectedNode(LFNode node) {
        // Update the display of the previous current node so that
        // it dislays as non-current.
        if (selectedNode != null) {
            LFNode oldselectedNode = selectedNode;
            selectedNode = null;
            updateNode(oldselectedNode);
        }
        
        selectedNode = node;
        
        // Update the display of the newly selected node.
        if (selectedNode != null) {
            updateNode(selectedNode);
        }
        
        curErrorChanged();
        
        updateButtonEnabledState();
        
        fireSelectedNodeChanged();
    }
    
    private void fireSelectedNodeChanged() {
        // Notify listeners that the selected node changed.
        for (int i = 0; i < listeners.size(); i++) {
            SelectionListener sl = (SelectionListener)listeners.get(i);
            sl.selectionChanged(new SelectionEvent(selectedNode));
        }
    }
    
    void updateButtonEnabledState() {
        // Check what buttons should be enabled now
        btnSimplify.setEnabled(doSimplify(true));
        btnUnsimplify.setEnabled(doUnsimplify(true));
        btnNextStep.setEnabled(doNextStep(true));
        btnPrevStep.setEnabled(doPrevStep(true));
        
        btnNextStep.setVisible(lambdacalc.Main.GOD_MODE);
        btnPrevStep.setVisible(lambdacalc.Main.GOD_MODE);
        
        String simplifyText = "Simplify Node";
        if (lambdacalc.Main.GOD_MODE) {
            if (selectedNode != null && selectedNode instanceof Terminal)
                simplifyText = "Go To Next Node";
            else if (selectedNode != null && !lfToMeaningState.containsKey(selectedNode))
                simplifyText = "Evaluate Node";
        }
        btnSimplify.setText(simplifyText);
    }
    
    void curErrorChanged() {
        String evalError = "";
        if (selectedNode != null && lfToMeaningState.containsKey(selectedNode)) { // has the node been evaluated?
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.evaluationError != null)
                evalError = ms.evaluationError;
        }
        
        this.setErrorMessage(evalError);
    }
    
    // Update the visual display of the node. Called to
    // update the label, meaning, and focus state of a node
    // when it changes.
    void updateNode(LFNode node) {
        JPanel nodePanel = (JPanel)lfToTreeLabelPanel.get(node);

        java.awt.Color borderColor = getBackground();
        if (node == selectedNode)
            borderColor = java.awt.Color.BLUE;
        nodePanel.setBorder(new javax.swing.border.LineBorder(borderColor, 2, true));
    
        JTextPane orthoLabel = (JTextPane)lfToOrthoLabel.get(node);
        String labeltext = node.toHTMLString();
        if (labeltext == null || labeltext.trim().length() == 0)
            labeltext = "&nbsp;-&nbsp;";
        orthoLabel.setText("<center style=\"font-size: " + curFontSize + "pt\">" + labeltext + "</font></center>");
        
        // Update the lambda expression displayed, if it's been evaluated.
        // If an error ocurred during evaluation, display it. Otherwise
        // display the lambda expression.
        JLabel meaningLabel = (JLabel)lfToMeaningLabel.get(node);
        meaningLabel.setFont(lambdacalc.gui.Util.getUnicodeFont(curFontSize));
        if (lfToMeaningState.containsKey(node)) { // has the node been evaluated?
            MeaningState ms = (MeaningState)lfToMeaningState.get(node);
            java.awt.Color meaningColor;
            if (ms.evaluationError == null) { // was there an error?
                //meaningLabel.setText("<center><font color=blue>" + ((Expr)ms.exprs.get(ms.curexpr)).toHTMLString() + "</font></center>");
                meaningLabel.setText(((Expr)ms.exprs.get(ms.curexpr)).toString());
                meaningColor = java.awt.Color.BLUE;
            } else {
                //meaningLabel.setText("<center><font color=red>Problem!</font></center>");
                meaningLabel.setText("Problem!");
                meaningColor = java.awt.Color.RED;
            }
            meaningLabel.setForeground(meaningColor);
            meaningLabel.setVisible(true);
        } else {
            meaningLabel.setVisible(false);
            meaningLabel.setText("");
        }
    }
    
    // Move the current evaluation node to the node indicated, but only
    // if all of its children have been fully evaluated and simplified. If they
    // haven't, move to the first not fully simplified child.
    void moveTo(LFNode node) {
        // If we're not making any new node current, end here.
        if (node == null) {
            setSelectedNode(null);
            return;
        }
        
        if (node instanceof Nonterminal) {
            // If any of this node's children haven't maxed out their
            // meaning simplifications, move to them.
            for (int i = 0; i < ((Nonterminal)node).size(); i++) {
                LFNode child = ((Nonterminal)node).getChild(i);
                
                // BareIndex nodes dont have meanings.
                if (child instanceof BareIndex)
                    continue;
                
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
        setSelectedNode(node);
    }
    
    boolean ensureChildrenEvaluated() {
        // Make sure all of the children of the selected node
        // are fully evaluated, and move to the first unevaluated
        // node if there is one. To do this, we make use of
        // the moveTo method. If we moveTo the selected node
        // and any of its children are unevaluated, it will
        // take us there.
        
        LFNode node = selectedNode;
        
        while (true) {
            moveTo(node);
            
            // If we didn't move, that's because all children are
            // evaluated, so we are good to go.
            if (node == selectedNode) return true;
            
            // We must be at a child that isn't fully evaluated.
            if (!isNodeEvaluated(selectedNode))
                evaluateNode();
                
            if (nodeHasError(selectedNode))
                return false;
        
            // Move the simplification state to the last step.
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            ms.curexpr = ms.exprs.size()-1;
            updateNode(selectedNode);
            
            // Try again to move to the node we want to be at...
        }
    }
    
    boolean doSimplify(boolean testOnly) {
        // This evaluates the meaning of a node, if it hasn't been evaluated,
        // and steps through the simplifications of the meaning, but never
        // moves on to another node. If testOnly, just check whether there
        // is any simplification to be done and return that.
        
        if (selectedNode == null) // nothing is selected?
            return false;
        
        // Make all of the children fully evaluated, and if that's not possible,
        // don't do anything further here. If we're just testing, don't perform
        // it.
        if (!testOnly && !ensureChildrenEvaluated())
            return false;
        
        // Node hasn't been evaluated yet. Evaluate it.
        if (!isNodeEvaluated(selectedNode)) {
            if (!lambdacalc.Main.GOD_MODE) return false;
            if (testOnly) return true;
            evaluateNode();
            canvas.invalidate();
        } else if (nodeHasError(selectedNode)) {
            return false; // can't go further
        } else if (!isNodeFullyEvaluated()) {
            // Node is evaluated but not fully simplified, so go to the next
            // simplification step.
            if (testOnly) return true;
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            ms.curexpr++;
            updateNode(selectedNode);
            canvas.invalidate();
        } else {
            if (!lambdacalc.Main.GOD_MODE) return false;
            // Node is fully evaluated, so move to the next node.
            if (!lfToParent.containsKey(selectedNode))
                return false;
            if (testOnly) return true;
            moveTo((LFNode)lfToParent.get(selectedNode));
        }
        
        return false;
    }
    
    boolean doUnsimplify(boolean testOnly) {
        if (selectedNode == null) // nothing is selected?
            return false;
        
        if (!isNodeEvaluated(selectedNode)) {
            // nothing to do
            return false;
        } else if (selectedNode instanceof Terminal) {
            return false;
        } else if (nodeHasError(selectedNode)) {
            if (testOnly) return true;
            ((Nonterminal)selectedNode).setCompositionRule(null); // clear composition rule too so it can be chosen again by user
            onUserChangedNodeMeaning(selectedNode);
            
            /*
            lfToMeaningState.remove(selectedNode);
            updateNode(selectedNode);
            canvas.invalidate();
             */
        } else {
            if (testOnly) return true;
            
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.curexpr > 0) {
                ms.curexpr--;
                updateNode(selectedNode);
                canvas.invalidate();
            } else {
                ((Nonterminal)selectedNode).setCompositionRule(null); // clear composition rule too so it can be chosen again by user
                onUserChangedNodeMeaning(selectedNode);
            }
        }
        
        return false;
    }

    boolean doNextStep(boolean testOnly) {
        // This fully evaluates the current node, if it has not been
        // evaluated yet, but if it has been fully evaluated, then
        // we move to the next node and evaluate it, but don't
        // simplify it.
        if (selectedNode == null)
            return false;
            
        // Make all of the children fully evaluated, and if that's not possible,
        // don't do anything further here. If we're just testing, don't actually
        // do it.
        if (!testOnly && !ensureChildrenEvaluated())
            return false;
        
        if (!isNodeFullyEvaluated()) {
            if (testOnly) return !nodeHasError(selectedNode);
        
            if (!isNodeEvaluated(selectedNode))
                evaluateNode();
                
            canvas.invalidate();
            
            if (nodeHasError(selectedNode))
                return false;
        
            // Move the simplification state to the last step.
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.curexpr < ms.exprs.size()-1) {
                // Move the evaluation to the end.
                ms.curexpr = ms.exprs.size()-1;
                updateNode(selectedNode);
                canvas.invalidate();
            }
        }
        
        // This expression is fully evaluated. 
        if (lfToParent.containsKey(selectedNode)) {
            if (testOnly) return true;
            Nonterminal parent = (Nonterminal)lfToParent.get(selectedNode);
            moveTo(parent);
        }
        
        return false;
    }
            
    boolean doPrevStep(boolean testOnly) {
        if (selectedNode == null)
            return false;
            
        if (!isNodeEvaluated(selectedNode)) {
            // move to last nonterminal child that is evaluated
            if (selectedNode instanceof Nonterminal) {
                Nonterminal n = (Nonterminal)selectedNode;
                for (int i = n.size()-1; i >= 0; i--) {
                    LFNode child = n.getChild(i);
                    if (child instanceof Terminal)
                        continue;
                    if (testOnly) return true;
                    setSelectedNode(child);
                    if (isNodeEvaluated(selectedNode))
                        return false;
                }
            }
            
            // no children are evaluated, so we go to the first, ehm,
            // preceding c-commanding node, which is (I hope) the
            // previous one we evaluated
            // Move to previous sibling
            LFNode child = selectedNode;
            Nonterminal parent = (Nonterminal)lfToParent.get(child);
            while (parent != null) {
                // look at the siblings before child
                int i = parent.size() - 1;
                while (i >= 0) {
                    if (parent.getChild(i) == child)
                        break;
                    i--;
                }
                i--;
                while (i >= 0) {
                    if (parent.getChild(i) instanceof Nonterminal && isNodeEvaluated(parent.getChild(i))) {
                        if (testOnly) return true;
                        setSelectedNode(parent.getChild(i));
                        return false;
                    }
                    i--;
                }
                child = parent;
                parent = (Nonterminal)lfToParent.get(parent);
            }
            
            return false;
        }
        
        if (selectedNode instanceof Terminal)
            return false;
        
        if (testOnly) return true;
        lfToMeaningState.remove(selectedNode);
        ((Nonterminal)selectedNode).setUserMeaningSimplification(null);
        onUserChangedNodeMeaning(selectedNode);
        canvas.invalidate();
        
        return false;
    }
    
    public boolean isNodeEvaluated(LFNode node) {
        return lfToMeaningState.containsKey(node);
    }
    
    public boolean nodeHasError(LFNode node) {
        if (!isNodeEvaluated(node))
            return false; // definitely not if it hasn't been evaluated at all
        MeaningState ms = (MeaningState)lfToMeaningState.get(node);
        return ms.evaluationError != null;
    }
        
    boolean isNodeFullyEvaluated() {
        //TODO should check if we're in god mode, and if yes should check
        //if this node is done by the user, i.e. contains any meaning brackets
        // and is well typed
        if (!isNodeEvaluated(selectedNode))
            return false; // definitely not fully evaluated if it hasn't been evaluated at all
        
        MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
        return ms.curexpr == ms.exprs.size()-1;
    }
    
    private void evaluateNode() {
        try {
            Expr m = selectedNode.getMeaning();
            MeaningState s = new MeaningState(m);
            if (selectedNode instanceof Nonterminal)
                ((Nonterminal)selectedNode).setUserMeaningSimplification(s.exprs);
            lfToMeaningState.put(selectedNode, s); // no error ocurred
        } catch (MeaningEvaluationException e) {
            lfToMeaningState.put(selectedNode, new MeaningState(e.getMessage()));
            if (selectedNode instanceof Nonterminal)
                ((Nonterminal)selectedNode).setUserMeaningSimplification(null);
        }
        updateNode(selectedNode);
        canvas.invalidate();
        curErrorChanged();
    }
    
    /*
     * In non-God-mode, this is called by the nonterminal composition rule selection
     * panel after the user chooses a composition rule to begin simplifying the node.
     * The node is evaluated.
     */
    public void startEvaluation(boolean skipMeaningBracketsState) {
        evaluateNode();

        if (skipMeaningBracketsState) {
            // skip the meaning brackets state?
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms != null && ms.evaluationError == null) {
                ms.curexpr = ms.exprs.size() - 1;
                updateNode(selectedNode);
                canvas.invalidate();
            }
        }        
        updateButtonEnabledState();
        fireSelectedNodeChanged();
    }
    
    public Expr getNodeExpressionState(LFNode node) {
        MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
        if (ms == null) return null;
        if (ms.evaluationError != null) return null;
        return (Expr)ms.exprs.get(ms.curexpr);
    }
   
    /**
     */
    public void advanceSimplification(Expr parsedMeaning) {
        MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
        
        // truncate the list of pre-computed simplification
        // steps and discard "future" steps that haven't
        // been gotten to yet (only because the user may have taken a step
        // back by un-simplifying)
        ms.exprs.setSize(ms.curexpr + 1);

        // append the user's simplification to the end
        ms.exprs.add(parsedMeaning);

        // and then advance the cursor
        ms.curexpr++;

        updateNode(selectedNode);
        canvas.invalidate();
        curErrorChanged();
        updateButtonEnabledState();
        fireSelectedNodeChanged();
    }
    
    
    public void setFontSize(int size) {
        curFontSize = size;
        
        for (Iterator i = lfToOrthoLabel.keySet().iterator(); i.hasNext(); )
            updateNode((LFNode)i.next());
        
        canvas.invalidate();
        
        btnFontDecrease.setEnabled(curFontSize > 10);
        btnFontIncrease.setEnabled(curFontSize < 48);
    }
            
        
    class SimplifyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doSimplify(false);
            updateButtonEnabledState();
            fireSelectedNodeChanged(); // not that a different node was necessarily selected (which might have already fired the event), but that the meaning state changed
        }
    }
    class UnsimplifyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doUnsimplify(false);
            updateButtonEnabledState();
            fireSelectedNodeChanged(); // not that a different node was necessarily selected (which might have already fired the event), but that the meaning state changed
        }
    }
    class NextStepActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doNextStep(false);
            updateButtonEnabledState();
            fireSelectedNodeChanged(); // not that a different node was necessarily selected (which might have already fired the event), but that the meaning state changed
        }
    }
    class PrevStepActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doPrevStep(false);
            updateButtonEnabledState();
            fireSelectedNodeChanged(); // not that a different node was necessarily selected (which might have already fired the event), but that the meaning state changed
        }
    }
    class FontIncreaseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setFontSize(curFontSize+3);
        }
    }
    class FontDecreaseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setFontSize(curFontSize-3);
        }
    }
    class FullScreenActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openFullScreenWindow();
        }
    }
    
    class NodePropertyChangeListener implements java.beans.PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("label") || e.getPropertyName().equals("index"))
                updateNode((LFNode)e.getSource());
            else if (e.getPropertyName().equals("meaning") || e.getPropertyName().equals("compositionRule"))
                onUserChangedNodeMeaning((LFNode)e.getSource());
            updateButtonEnabledState();
        }
    }


    
    public void openFullScreenWindow() {
        FullScreenTreeExerciseWidget fs = new FullScreenTreeExerciseWidget(this);
        fs.display();
    }
                                      
    public static void main(String[] args) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TreeExerciseWidget w = new TreeExerciseWidget();
                try {
                    ExerciseFile file = ExerciseFileParser.parse(new java.io.FileReader("examples/example2.txt"));
                    w.initialize((TreeExercise)file.getGroup(0).getItem(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JFrame frame = new JFrame();
                frame.setSize(640, 480);
                frame.getContentPane().add(w);
                frame.setVisible(true);
            }
        });
    }

    public TreeExercise getExercise() {
        return exercise;
    }

    public String getErrorMessage() {
        return this.errorLabel.getText();
    }

    public void setErrorMessage(String statusMessage) {
        this.errorLabel.setText(statusMessage);
        
    }
    
    public boolean isTreeFullyEvaluated() {
        MeaningState s = (MeaningState)lfToMeaningState.get(lftree);
        if (s == null) return false;
        if (s.evaluationError != null) return false;
        
        if (lambdacalc.Main.GOD_MODE) {
            // Has user simplified to the last step?
            return s.curexpr == s.exprs.size() - 1;
        } else {
            // Has user provided an expression that can no longer be simplified?
            // If a type evaluation error occurs, we'll just take that to mean
            // the expression can no longer be simplified, and so the user has
            // reached the end, although to an incorrect answer.
            // performLambdaConversion returns null when expression can't be 
            // simplified.
            try {
                return ((Expr)s.exprs.get(s.exprs.size()-1)).performLambdaConversion() == null;
            } catch (TypeEvaluationException tee) {
                return true;
            }
        }
    }
}
