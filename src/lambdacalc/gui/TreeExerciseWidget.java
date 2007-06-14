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
    
    boolean inFullScreenMode = false;
    
    JScrollPane scrollpane;
    TreeCanvas canvas; // this is the display widget
    Nonterminal lftree; // this is the tree we're displaying
    
    // Maps from LFNodes in lftree to controls being displayed and other state.
    Map lfToTreeLabelPanel = new HashMap(); // panel containing ortho label, propositional content
    Map lfToOrthoLabel = new HashMap(); // orthographic label
    Map lfToMeaningLabel = new HashMap(); // propositional content label (for nonterminals only if we're using dropdowns for terminals)
    Map lfToMeaningState = new HashMap(); // state of the propositional label, or null if node is not evaluated yet
    //Map lfToMeaningChooser = new HashMap(); // meaning JComboBox: lexical entry for terminals, composition rule for nonterminals
    Map lfToParent = new HashMap(); // parent LFNode
    
    JLabel errorLabel = new JLabel(); // label containing error messages
    
    // At any given time, at most one LFNode is the currently
    // selected node, which is highlighted and represents
    // the node that is affected by the buttons.
    // This is null if no node is the current evaluation node.
    LFNode selectedNode = null;
    
    // Buttons
    JButton btnSimplify = new JButton("Simplify");
    JButton btnUnsimplify = new JButton("Unsimplify");
    JButton btnNextStep = new JButton("Next Step");
    JButton btnPrevStep = new JButton("Previous Step");
    
    // Selection listeners
    Vector listeners = new Vector();
    
    NodePropertyChangeListener nodeListener = new NodePropertyChangeListener();
    
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
        
        addKeyListener(new KeyListener() {
                public void keyPressed(KeyEvent event) {}
                public void keyReleased(KeyEvent event) {
                    if (event.getKeyChar() == KeyEvent.VK_ESCAPE) {
                        if (inFullScreenMode) {
                            System.exit(0); //TODO change
                        }
                    }
                }
                public void keyTyped(KeyEvent event) {}
            }
            );
        
        scrollpane = new JScrollPane();
        canvas = new TreeCanvas();
        scrollpane.setViewportView(canvas);
        add(scrollpane, BorderLayout.CENTER);
        
        add(errorLabel, BorderLayout.PAGE_END);
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        
        btnSimplify.addActionListener(new SimplifyActionListener());
        buttons.add(btnSimplify);
        
        btnUnsimplify.addActionListener(new UnsimplifyActionListener());
        buttons.add(btnUnsimplify);
        
        btnNextStep.addActionListener(new NextStepActionListener());
        buttons.add(btnNextStep);

        btnPrevStep.addActionListener(new PrevStepActionListener());
        buttons.add(btnPrevStep);

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
        //lfToMeaningChooser.clear();
        lfToMeaningState.clear();
        lfToParent.clear();
        
        canvas.clear();
        
        updateButtonEnabledState();
    }
    
    public void initialize(TreeExercise ex) {
        clear();
        
        lftree = ex.getTree();
        
        buildTree(canvas.getRoot(), lftree);
        
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
        
        /*
        // For terminals, give them comboboxes to choose the lexical entry.
        if (lfnode instanceof Terminal) {
            JComboBox lexicalChoices = new JComboBox();
            lexicalChoices.setFont(lambdacalc.gui.Util.getUnicodeFont(14));
            label.add(lexicalChoices);
            lexicalChoices.setAlignmentX(.5F);
            lfToMeaningChooser.put(lfnode, lexicalChoices);
            updateTerminalLexicalChoices((Terminal)lfnode);
            
        // For nonterminals, give them JLabels for their meanings.
        } else {
        */
            JLabel meaningLabel = new JLabel();
            meaningLabel.setFont(lambdacalc.gui.Util.getUnicodeFont(14));
            label.add(meaningLabel);
            meaningLabel.setAlignmentX(.5F);
            lfToMeaningLabel.put(lfnode, meaningLabel);

        /*    JComboBox compositionRuleChoices = new JComboBox();
            compositionRuleChoices.setFont(lambdacalc.gui.Util.getUnicodeFont(14));
            label.add(compositionRuleChoices);
            compositionRuleChoices.setAlignmentX(.5F);
            lfToMeaningChooser.put(lfnode, compositionRuleChoices);
            updateNonterminalCompositionRules((Nonterminal)lfnode);
        }*/
        
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
    
    /*
    private void updateTerminalLexicalChoices(Terminal node) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        
        int selectIndex = -1;
        if (node.getLabel() != null) {
            Expr[] meanings = lexicon.getMeanings(node.getLabel());
            if (meanings.length > 0)
                selectIndex = 0;
            for (int i = 0; i < meanings.length; i++) {
                model.addElement(meanings[i]);
            }
        }
        
        JComboBox lexicalChoices = (JComboBox)lfToMeaningChooser.get(node);
        lexicalChoices.setModel(model);
        
        lexicalChoices.setSelectedIndex(selectIndex); // TODO: If terminal is assiged a meaning, select it
        lexicalChoices.addActionListener(new LexicalChoiceActionListener(node, lexicalChoices));
    }
        
    private void updateNonterminalCompositionRules(Nonterminal node) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < rules.size(); i++) {
            model.addElement(rules.get(i));
        }
        
        JComboBox choices = (JComboBox)lfToMeaningChooser.get(node);
        choices.setModel(model);
        
        if (rules.size() > 0) // TODO: If nonterminal is assiged a meaning, select it
            choices.setSelectedIndex(0);
        choices.addActionListener(new LexicalChoiceActionListener(node, lexicalChoices));
    }
    
    private class LexicalChoiceActionListener implements ActionListener {
        Terminal terminal;
        JComboBox combobox;
        
        public LexicalChoiceActionListener(Terminal terminal, JComboBox combobox) {
            this.terminal = terminal;
            this.combobox = combobox;
        }
        
        public void actionPerformed(ActionEvent e) {
            // If no choices are in the box, the event might fire with index -1.
            if (combobox.getSelectedIndex() == -1)
                return;
                
            // Initialize the meaning state of the node.
            Expr selection = (Expr)combobox.getSelectedItem();
            terminal.setMeaning(selection);
            lfToMeaningState.put(terminal, new MeaningState(selection));
            onUserChangedNodeMeaning(terminal);
        }
    }
    */
    
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
        }

        updateNode(node);
    
        // Clear the meaning states of the parent nodes.
        LFNode ancestor = (LFNode)lfToParent.get(node);
        while (ancestor != null) {
            lfToMeaningState.remove(ancestor);
            updateNode(ancestor);
            ancestor = (LFNode)lfToParent.get(ancestor);
        }
    }
    
    private class NodeClickListener extends MouseAdapter {
        LFNode node;
        
        public NodeClickListener(LFNode node) { this.node = node; }
    
        public void mouseClicked(MouseEvent e) {
            selectNode(node);
        }
    }
    
    public LFNode getSelectedNode() {
        return selectedNode;
    }
    
    public void selectNode(LFNode node) {
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
    }
    
    void curErrorChanged() {
        String evalError = "";
        if (selectedNode != null && lfToMeaningState.containsKey(selectedNode)) { // has the node been evaluated?
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.evaluationError != null)
                evalError = ms.evaluationError;
        }
        
        errorLabel.setText(evalError);
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
    
        // Change the label if it's the current evaluation node.
        JTextPane orthoLabel = (JTextPane)lfToOrthoLabel.get(node);
        orthoLabel.setText("<center>" + node.toHTMLString() + "</center>");
        
        // Update the lambda expression displayed, if it's been evaluated.
        // If an error ocurred during evaluation, display it. Otherwise
        // display the lambda expression.
        JLabel meaningLabel = (JLabel)lfToMeaningLabel.get(node);
        if (lfToMeaningState.containsKey(node)) { // has the node been evaluated?
            java.awt.Color meaningColor;

            MeaningState ms = (MeaningState)lfToMeaningState.get(node);
            if (ms.evaluationError == null) { // was there an error?
                meaningLabel.setText(ms.exprs.get(ms.curexpr).toString());
                meaningColor = java.awt.Color.BLUE;
            } else {
                meaningLabel.setText("Problem!");
                meaningColor = java.awt.Color.RED;
            }
            meaningLabel.setForeground(meaningColor);
            meaningLabel.setVisible(true);
        } else {
            meaningLabel.setVisible(false);
        }
        
        // Ensure tree layout is adjusted due to changes to node label.
        // This ought to be automatic, but isn't.
        canvas.invalidate();
    }
    
    // Move the current evaluation node to the node indicated, but only
    // if all of its children have been fully evaluated and simplified. If they
    // haven't, move to the first not fully simplified child.
    void moveTo(LFNode node) {
        // If we're not making any new node current, end here.
        if (node == null) {
            selectNode(null);
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
        selectNode(node);
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
                
            if (nodeHasError())
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
            if (testOnly) return true;
            evaluateNode();
        } else if (nodeHasError()) {
            return false; // can't go further
        } else if (!isNodeFullyEvaluated()) {
            // Node is evaluated but not fully simplified, so go to the next
            // simplification step.
            if (testOnly) return true;
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            ms.curexpr++;
            updateNode(selectedNode);
        } else {
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
        } else if (nodeHasError()) {
            if (testOnly) return true;
            lfToMeaningState.remove(selectedNode);
            updateNode(selectedNode);
        } else if (selectedNode instanceof Terminal) {
            return false;
        } else {
            if (testOnly) return true;
            
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.curexpr > 0) {
                ms.curexpr--;
            } else {
                lfToMeaningState.remove(selectedNode);
            }
            updateNode(selectedNode);
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
            if (testOnly) return !nodeHasError();
        
            if (!isNodeEvaluated(selectedNode))
                evaluateNode();
                
            if (nodeHasError())
                return false;
        
            // Move the simplification state to the last step.
            MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
            if (ms.curexpr < ms.exprs.size()-1) {
                // Move the evaluation to the end.
                ms.curexpr = ms.exprs.size()-1;
                updateNode(selectedNode);
            }
        } else {
            // This expression is fully evaluated. 
            if (lfToParent.containsKey(selectedNode)) {
                if (testOnly) return true;
                Nonterminal parent = (Nonterminal)lfToParent.get(selectedNode);
                moveTo(parent);
            }
            return false;
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
                    selectNode(child);
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
                    if (isNodeEvaluated(parent.getChild(i))) {
                        if (testOnly) return true;
                        selectNode(parent.getChild(i));
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
        updateNode(selectedNode);
        
        return false;
    }
    
    boolean isNodeEvaluated(LFNode node) {
        return lfToMeaningState.containsKey(node);
    }
    
    boolean nodeHasError() {
        if (!isNodeEvaluated(selectedNode))
            return false; // definitely not if it hasn't been evaluated at all
        MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
        return ms.evaluationError != null;
    }
        
    boolean isNodeFullyEvaluated() {
        if (!isNodeEvaluated(selectedNode))
            return false; // definitely not fully evaluated if it hasn't been evaluated at all
        
        MeaningState ms = (MeaningState)lfToMeaningState.get(selectedNode);
        return ms.curexpr == ms.exprs.size()-1;
    }
    
    void evaluateNode() {
        try {
            Expr m = selectedNode.getMeaning();
            lfToMeaningState.put(selectedNode, new MeaningState(m)); // no error ocurred
        } catch (MeaningEvaluationException e) {
            lfToMeaningState.put(selectedNode, new MeaningState(e.getMessage()));
        }
        updateNode(selectedNode);
        curErrorChanged();
    }
        
    class SimplifyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doSimplify(false);
            updateButtonEnabledState();
        }
    }
    class UnsimplifyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doUnsimplify(false);
            updateButtonEnabledState();
        }
    }
    class NextStepActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doNextStep(false);
            updateButtonEnabledState();
        }
    }
    class PrevStepActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            doPrevStep(false);
            updateButtonEnabledState();
        }
    }
    class FullScreenActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            enterFullScreenMode();
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

    public void enterFullScreenMode() {
        
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.getContentPane().add(this);
        
        GraphicsDevice theScreen =
                GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getDefaultScreenDevice();
        if (!theScreen.isFullScreenSupported()) {
            System.err.println("Warning: Full screen mode not supported," +
                    "emulating by maximizing the window...");
        }
        
        try {
            theScreen.setFullScreenWindow(frame);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            theScreen.setFullScreenWindow(null);
        }
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
}
