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
 * RuleSelectionPanel.java
 *
 * Created on June 18, 2007, 7:55 PM
 */


// jPanelCardLayout and jPanelNodeProperties are the places to change the
// preferredSize and minimumSize in order to resize this component.
// Open the Design view of TrainingWindow in Netbeans and then use the
// Inspector (under Window/Navigating) to access these components in the
// GUI editor.

package lambdacalc.gui;

import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import lambdacalc.gui.TreeExerciseWidget.SelectionEvent;
import lambdacalc.gui.TreeExerciseWidget.SelectionListener;
import lambdacalc.lf.CompositionRule;
import lambdacalc.lf.FunctionApplicationRule;
import lambdacalc.lf.FunctionCompositionRule;
import lambdacalc.lf.IntensionalFunctionApplicationRule;
import lambdacalc.lf.LFNode;
import lambdacalc.lf.LambdaAbstractionRule;
import lambdacalc.lf.MeaningEvaluationException;
import lambdacalc.lf.Nonterminal;
import lambdacalc.lf.PredicateModificationRule;
import lambdacalc.lf.RuleList;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

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
    public static final IntensionalFunctionApplicationRule IFA_RULE = IntensionalFunctionApplicationRule.INSTANCE;
    public static final FunctionCompositionRule FC_RULE = FunctionCompositionRule.INSTANCE;

    public static final int FUNCTION_APPLICATION = 1;
    public static final int PREDICATE_MODIFICATION = 2;
    public static final int LAMBDA_ABSTRACTION = 3;
    public static final int INTENSIONAL_FUNCTION_APPLICATION = 4;
    public static final int FUNCTION_COMPOSITION = 5;

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
        if (i == INTENSIONAL_FUNCTION_APPLICATION) return IFA_RULE;
        if (i == FUNCTION_COMPOSITION) return FC_RULE;
        throw new IllegalArgumentException();
    }

//    public void setParentDialog(JDialog dialog) {
//        this.dialog = dialog;
//    }

    public void setVisibleRules(RuleList r) {
        setVisibleFA(r.contains(FA_RULE));
        setVisiblePM(r.contains(PM_RULE));
        setVisibleLA(r.contains(LA_RULE));
        setVisibleIFA(r.contains(IFA_RULE));
        setVisibleFC(r.contains(FC_RULE));
        if (r.contains(FA_RULE)) {
            this.getRootPane().setDefaultButton(this.jButtonFA);
            this.jButtonFA.requestFocus();
        } else if (r.contains(PM_RULE)) {
            this.getRootPane().setDefaultButton(this.jButtonPM);
            this.jButtonPM.requestFocus();
        } else if (r.contains(LA_RULE)) {
            this.getRootPane().setDefaultButton(this.jButtonLA);
            this.jButtonLA.requestFocus();
        } else if (r.contains(IFA_RULE)) {
            this.getRootPane().setDefaultButton(this.jButtonIFA);
            this.jButtonIFA.requestFocus();
        } else if (r.contains(FC_RULE)) {
            this.getRootPane().setDefaultButton(this.jButtonFC);
            this.jButtonFC.requestFocus();
        }
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
            if (IFA_RULE.isApplicableTo(node)) {
                txtIFA.setText(IFA_RULE.applyTo(node, true, true).toString());
            } else {

                txtIFA.setText
                    ("\""+IFA_RULE.applyTo(node, false, true).toString()
                    + "\" or \"" +
                    IFA_RULE.applyTo(node, false, false).toString()
                    +"\"");
            }

        } catch (MeaningEvaluationException ex) {
            txtIFA.setText(ex.getMessage());
            //txtIFA.setText("Not applicable here");
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
        try {
             txtFC.setText(FC_RULE.applyTo(node, true, true).toString());

        } catch (MeaningEvaluationException ex) {
//            txtFC.setText(ex.getMessage());
            txtFC.setText("Not applicable here");
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

    private void setVisibleIFA(boolean b) {
        this.jButtonIFA.setVisible(b);
        this.txtIFA.setVisible(b);
        this.jLabelIFA.setVisible(b);
    }

    private void setVisibleFC(boolean b) {
        this.jButtonFC.setVisible(b);
        this.txtFC.setVisible(b);
        this.jLabelFC.setVisible(b);
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

    public JButton getIFAButton() {
        return this.jButtonIFA;
    }

    public JButton getFCButton() {
        return this.jButtonFC;
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
        teWidget.startEvaluation(node, true);
//>>>>>>> .r216
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
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
        jButtonIFA = new javax.swing.JButton();
        txtIFA = new lambdacalc.gui.LambdaEnabledTextField();
        jLabelIFA = new javax.swing.JLabel();
        txtFC = new lambdacalc.gui.LambdaEnabledTextField();
        jLabelFC = new javax.swing.JLabel();
        jButtonFC = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(499, 350));

        jPanel1.setMinimumSize(new java.awt.Dimension(310, 160));
        jPanel1.setMixingCutoutShape(null);
        jPanel1.setPreferredSize(new java.awt.Dimension(310, 160));
        GridBagLayout gbl_jPanel1 = new GridBagLayout();
        gbl_jPanel1.columnWeights = new double[]{0.0, 1.0, 0.0};
        jPanel1.setLayout(gbl_jPanel1);

        jButtonFA.setText("Select");
        jButtonFA.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonFAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 5, 5, 0);
        jPanel1.add(jButtonFA, gridBagConstraints);

        jButtonPM.setText("Select");
        jButtonPM.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonPMActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 0);
        jPanel1.add(jButtonPM, gridBagConstraints);

        jButtonLA.setText("Select");
        jButtonLA.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 0);
        jPanel1.add(jButtonLA, gridBagConstraints);

        jLabelFA.setText("Function Application");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        jPanel1.add(jLabelFA, gridBagConstraints);

        jLabelPM.setText("Predicate Modification");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        jPanel1.add(jLabelPM, gridBagConstraints);

        jLabelLA.setText("Lambda Abstraction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        jPanel1.add(jLabelLA, gridBagConstraints);

        jLabelSelect.setFont(new java.awt.Font("Lucida Grande", 1, 16)); // NOI18N
        jLabelSelect.setText("Select a composition rule");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 20, 0);
        jPanel1.add(jLabelSelect, gridBagConstraints);

        txtFA.setEditable(false);
        gridBagConstraints_1 = new java.awt.GridBagConstraints();
        gridBagConstraints_1.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints_1.gridx = 1;
        gridBagConstraints_1.gridy = 1;
        gridBagConstraints_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_1.weightx = 1.0;
        jPanel1.add(txtFA, gridBagConstraints_1);

        txtPM.setEditable(false);
        gridBagConstraints_2 = new java.awt.GridBagConstraints();
        gridBagConstraints_2.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints_2.gridx = 1;
        gridBagConstraints_2.gridy = 2;
        gridBagConstraints_2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_2.weightx = 1.0;
        jPanel1.add(txtPM, gridBagConstraints_2);

        txtLA.setEditable(false);
        gridBagConstraints_3 = new java.awt.GridBagConstraints();
        gridBagConstraints_3.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints_3.gridx = 1;
        gridBagConstraints_3.gridy = 3;
        gridBagConstraints_3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_3.weightx = 1.0;
        jPanel1.add(txtLA, gridBagConstraints_3);

        jButtonIFA.setText("Select");
        jButtonIFA.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonIFAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 5, 5, 0);
        jPanel1.add(jButtonIFA, gridBagConstraints);

        txtIFA.setEditable(false);
        gridBagConstraints_4 = new java.awt.GridBagConstraints();
        gridBagConstraints_4.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints_4.gridx = 1;
        gridBagConstraints_4.gridy = 4;
        gridBagConstraints_4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints_4.weightx = 1.0;
        jPanel1.add(txtIFA, gridBagConstraints_4);

        jLabelIFA.setText("Intensional Function Application");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        jPanel1.add(jLabelIFA, gridBagConstraints);

        txtFC.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(txtFC, gridBagConstraints);

        jLabelFC.setText("Function Composition");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(jLabelFC, gridBagConstraints);

        jButtonFC.setText("Select");
        jButtonFC.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonFCActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jButtonFC, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        jLabelFC = new JLabel("Function Composition");
        GridBagConstraints gbc_jLabelFC = new GridBagConstraints();
        gbc_jLabelFC.insets = new Insets(0, 0, 0, 5);
        gbc_jLabelFC.anchor = GridBagConstraints.EAST;
        gbc_jLabelFC.gridx = 0;
        gbc_jLabelFC.gridy = 5;
        jPanel1.add(jLabelFC, gbc_jLabelFC);

        txtFC = new LambdaEnabledTextField();
        GridBagConstraints gbc_txtFC = new GridBagConstraints();
        gbc_txtFC.insets = new Insets(0, 0, 0, 5);
        gbc_txtFC.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtFC.gridx = 1;
        gbc_txtFC.gridy = 5;
        jPanel1.add(txtFC, gbc_txtFC);

        jButtonFC = new JButton("Select");
        jButtonFC.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		jButtonFCActionPerformed(e);
        	}
        });
        GridBagConstraints gbc_jButtonFC = new GridBagConstraints();
        gbc_jButtonFC.gridx = 2;
        gbc_jButtonFC.gridy = 5;
        jPanel1.add(jButtonFC, gbc_jButtonFC);
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

    private void jButtonIFAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIFAActionPerformed
        value = INTENSIONAL_FUNCTION_APPLICATION;
        updateTree(value);
    //    this.dialog.setVisible(false);
}//GEN-LAST:event_jButtonIFAActionPerformed

    private void jButtonFCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFCActionPerformed
        value = FUNCTION_COMPOSITION;
        updateTree(value);
    //    this.dialog.setVisible(false);
}//GEN-LAST:event_jButtonFCActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButtonFA;
    private javax.swing.JButton jButtonFC;
    private javax.swing.JButton jButtonIFA;
    private javax.swing.JButton jButtonLA;
    private javax.swing.JButton jButtonPM;
    private javax.swing.JLabel jLabelFA;
    private javax.swing.JLabel jLabelFC;
    private javax.swing.JLabel jLabelIFA;
    private javax.swing.JLabel jLabelLA;
    private javax.swing.JLabel jLabelPM;
    private javax.swing.JLabel jLabelSelect;
    private javax.swing.JPanel jPanel1;
    private lambdacalc.gui.LambdaEnabledTextField txtFA;
    private lambdacalc.gui.LambdaEnabledTextField txtFC;
    private lambdacalc.gui.LambdaEnabledTextField txtIFA;
    private lambdacalc.gui.LambdaEnabledTextField txtLA;
    private lambdacalc.gui.LambdaEnabledTextField txtPM;
    private GridBagConstraints gridBagConstraints_1;
    private GridBagConstraints gridBagConstraints_2;
    private GridBagConstraints gridBagConstraints_3;
    private GridBagConstraints gridBagConstraints_4;
    // End of variables declaration//GEN-END:variables

}
