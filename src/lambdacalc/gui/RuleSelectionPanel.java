/*
 * RuleSelectionPanel.java
 *
 * Created on June 18, 2007, 7:55 PM
 */

package lambdacalc.gui;

import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import lambdacalc.gui.TreeExerciseWidget.SelectionEvent;
import lambdacalc.gui.TreeExerciseWidget.SelectionListener;
import lambdacalc.lf.CompositionRule;
import lambdacalc.lf.FunctionApplicationRule;
import lambdacalc.lf.LFNode;
import lambdacalc.lf.LambdaAbstractionRule;
import lambdacalc.lf.MeaningEvaluationException;
import lambdacalc.lf.Nonterminal;
import lambdacalc.lf.PredicateModificationRule;
import lambdacalc.lf.RuleList;

/**
 *
 * @author  champoll
 */
public class RuleSelectionPanel extends javax.swing.JPanel 
implements PropertyChangeListener, SelectionListener {
    
    private int value = -1;
    
    //private JDialog dialog;
    
    private TreeExerciseWidget teWidget = null;
    
    public static final FunctionApplicationRule FA_RULE = FunctionApplicationRule.INSTANCE;
    public static final PredicateModificationRule PM_RULE = PredicateModificationRule.INSTANCE;
    public static final LambdaAbstractionRule LA_RULE = LambdaAbstractionRule.INSTANCE;
    
    public static final int FUNCTION_APPLICATION = 1;
    public static final int PREDICATE_MODIFICATION = 2;
    public static final int LAMBDA_ABSTRACTION = 3;
    
    /** Creates new form RuleSelectionPanel */
    public RuleSelectionPanel() {
        initComponents();
    }
    
    public void initialize(TreeExerciseWidget teWidget) {
        if (this.teWidget != null)
            this.teWidget.removeSelectionListener(this);
        this.teWidget = teWidget;
        teWidget.addSelectionListener(this);
    }
    
    public static CompositionRule forValue(int i) {
        if (i == FUNCTION_APPLICATION) return FA_RULE;
        if (i == PREDICATE_MODIFICATION) return PM_RULE;
        if (i == LAMBDA_ABSTRACTION) return LA_RULE;
        
        throw new IllegalArgumentException();
    }

//    public void setParentDialog(JDialog dialog) {
//        this.dialog = dialog;
//    }
    
    public void setVisibleRules(RuleList r) {
        setVisibleFA(r.contains(FA_RULE));
        setVisiblePM(r.contains(PM_RULE));
        setVisibleLA(r.contains(LA_RULE));
    }
    
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // Fired when the node we're viewing changes
//        if (e.getPropertyName().equals("compositionRule")) {
//            TrainingWindow.getSingleton().
//                    updateNodePropertyPanel((Nonterminal)e.getSource());
//        }
    }    
   
    public void selectionChanged(SelectionEvent e) {
        LFNode source = (LFNode) e.getSource();
        source.addPropertyChangeListener(this);
        

        Nonterminal node = getSelectedBranchingNodeIfAny();
        if (node == null) return;
        try {
            if (FA_RULE.isApplicableTo(node)) {
                txtFA.setText(FA_RULE.applyTo(node, true, true).toString());
            } else {
                
                txtFA.setText
                    ("\""+FA_RULE.applyTo(node, false, true).toString()
                    + "\" or \"" +
                    FA_RULE.applyTo(node, false, false).toString()
                    +"\"");
            }
            
        } catch (MeaningEvaluationException ex) {
            txtFA.setText(ex.getMessage());
            //txtFA.setText("Not applicable here");
        }
        try {
            txtPM.setText(PM_RULE.applyTo(node, false).toString());
        } catch (MeaningEvaluationException ex) {
            //txtPM.setText(ex.getMessage());
            txtPM.setText("Not applicable here");
        }
        try {
            txtLA.setText(LA_RULE.applyTo(node, false).toString());
        } catch (MeaningEvaluationException ex) {
            //txtLA.setText(ex.getMessage());
            txtLA.setText("Not applicable here");
        }
        
    }
    
    private void setVisibleFA(boolean b) {
        this.jButtonFA.setVisible(b);
        this.txtFA.setVisible(b);
        this.jLabelFA.setVisible(b);
    }
    
    private void setVisiblePM(boolean b) {
        this.jButtonPM.setVisible(b);
        this.txtPM.setVisible(b);
        this.jLabelPM.setVisible(b);
    }
    
    private void setVisibleLA(boolean b) {
        this.jButtonLA.setVisible(b);
        this.txtLA.setVisible(b);
        this.jLabelLA.setVisible(b);
    }

    public JButton getFAButton() {
        return this.jButtonFA;
    }
    
    public JButton getPMButton() {
        return this.jButtonPM;
    }
    
    public JButton getLAButton() {
        return this.jButtonLA;
    }

    public int getValue() {
        return value;
    }
    
    private Nonterminal getSelectedBranchingNodeIfAny() {
        //sanity check: we expect the selected node to be a branching nonterminal
        if (teWidget == null) return null;
        if (!(teWidget.getSelectedNode() instanceof Nonterminal)) return null;
        Nonterminal node = (Nonterminal) teWidget.getSelectedNode();
        if (!node.isBranching()) return null;
        // else
        return node;
    }
    
    private void updateTree(int value) {

        Nonterminal node = getSelectedBranchingNodeIfAny();
        
        
        
        if (node == null) return;
        
        // We want to make sure that any children of the selected node
        // that ought to have a meaning have been fully simplified by the
        // user.
        for (java.util.Iterator i = node.getChildren().iterator(); i.hasNext(); ) {
            LFNode child = (LFNode)i.next();
            if (child.isMeaningful()) {
                if (!teWidget.isNodeFullyEvaluated(child)) {
                    teWidget.setSelectedNode(child);
                    teWidget.setErrorMessage("You must first complete all children of this node before you can start working on it.");
                    return;
                }
            }
        }
       
        node.setCompositionRule(forValue(value));
//<<<<<<< .mine
//        try {
//            // freebie: we replace all meaning brackets
//            teWidget.startEvaluation(MeaningBracketExpr.replaceAllMeaningBrackets(node.getMeaning()));
//        } catch (MeaningEvaluationException ex) {
//            teWidget.setErrorMessage(ex.getMessage());
//        } catch (TypeEvaluationException ex) {
//            teWidget.setErrorMessage(ex.getMessage());
//        }
////        teWidget.doSimplify(false);
//=======
        teWidget.startEvaluation(node, true);
//>>>>>>> .r216
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jButtonFA = new javax.swing.JButton();
        jButtonPM = new javax.swing.JButton();
        jButtonLA = new javax.swing.JButton();
        jLabelFA = new javax.swing.JLabel();
        jLabelPM = new javax.swing.JLabel();
        jLabelLA = new javax.swing.JLabel();
        jLabelSelect = new javax.swing.JLabel();
        txtFA = new lambdacalc.gui.LambdaEnabledTextField();
        txtPM = new lambdacalc.gui.LambdaEnabledTextField();
        txtLA = new lambdacalc.gui.LambdaEnabledTextField();

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButtonFA.setText("Select");
        jButtonFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFAActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jButtonFA, gridBagConstraints);

        jButtonPM.setText("Select");
        jButtonPM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPMActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jButtonPM, gridBagConstraints);

        jButtonLA.setText("Select");
        jButtonLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLAActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jButtonLA, gridBagConstraints);

        jLabelFA.setText("Function Application");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(jLabelFA, gridBagConstraints);

        jLabelPM.setText("Predicate Modification");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(jLabelPM, gridBagConstraints);

        jLabelLA.setText("Lambda Abstraction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(jLabelLA, gridBagConstraints);

        jLabelSelect.setFont(new java.awt.Font("Lucida Grande", 1, 16));
        jLabelSelect.setText("Select a composition rule");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 20, 0);
        jPanel1.add(jLabelSelect, gridBagConstraints);

        txtFA.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(txtFA, gridBagConstraints);

        txtPM.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(txtPM, gridBagConstraints);

        txtLA.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(txtLA, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLAActionPerformed
        
        value = LAMBDA_ABSTRACTION;
        updateTree(value);
    //    this.dialog.setVisible(false);
    }//GEN-LAST:event_jButtonLAActionPerformed

    private void jButtonPMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPMActionPerformed
         
        value = PREDICATE_MODIFICATION;
        updateTree(value);
    //    this.dialog.setVisible(false);
    }//GEN-LAST:event_jButtonPMActionPerformed

    private void jButtonFAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFAActionPerformed

        value = FUNCTION_APPLICATION;
        updateTree(value);
    //    this.dialog.setVisible(false);
    }//GEN-LAST:event_jButtonFAActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonFA;
    private javax.swing.JButton jButtonLA;
    private javax.swing.JButton jButtonPM;
    private javax.swing.JLabel jLabelFA;
    private javax.swing.JLabel jLabelLA;
    private javax.swing.JLabel jLabelPM;
    private javax.swing.JLabel jLabelSelect;
    private javax.swing.JPanel jPanel1;
    private lambdacalc.gui.LambdaEnabledTextField txtFA;
    private lambdacalc.gui.LambdaEnabledTextField txtLA;
    private lambdacalc.gui.LambdaEnabledTextField txtPM;
    // End of variables declaration//GEN-END:variables
    
}
