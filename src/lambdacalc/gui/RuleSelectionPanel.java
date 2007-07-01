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
import lambdacalc.lf.MeaningBracketExpr;
import lambdacalc.lf.MeaningEvaluationException;
import lambdacalc.lf.Nonterminal;
import lambdacalc.lf.PredicateModificationRule;
import lambdacalc.logic.TypeEvaluationException;

/**
 *
 * @author  champoll
 */
public class RuleSelectionPanel extends javax.swing.JPanel 
implements PropertyChangeListener, SelectionListener {
    
    private int value = -1;
    
    //private JDialog dialog;
    
    private TreeExerciseWidget teWidget = null;
    
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
        if (i == FUNCTION_APPLICATION) return FunctionApplicationRule.INSTANCE;
        if (i == PREDICATE_MODIFICATION) return PredicateModificationRule.INSTANCE;
        if (i == LAMBDA_ABSTRACTION) return LambdaAbstractionRule.INSTANCE;
        
        throw new IllegalArgumentException();
    }

//    public void setParentDialog(JDialog dialog) {
//        this.dialog = dialog;
//    }
    
    
    
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
    
    private void updateTree(int value) {
        //sanity check: we expect the selected node to be a branching nonterminal
        if (teWidget == null) return;
        if (!(teWidget.getSelectedNode() instanceof Nonterminal)) return;
        Nonterminal node = (Nonterminal) teWidget.getSelectedNode();
        if (!node.isBranching()) return;
        
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
        teWidget.startEvaluation(true);
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
        jLabelFA = new javax.swing.JLabel();
        jLabelPM = new javax.swing.JLabel();
        jLabelLA = new javax.swing.JLabel();
        jButtonFA = new javax.swing.JButton();
        jButtonPM = new javax.swing.JButton();
        jButtonLA = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder("Select a composition rule"));
        jLabelFA.setText("Function Application");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabelFA, gridBagConstraints);

        jLabelPM.setText("Predicate Modification");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabelPM, gridBagConstraints);

        jLabelLA.setText("Lambda Abstraction");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabelLA, gridBagConstraints);

        jButtonFA.setText("Select");
        jButtonFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFAActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 4);
        add(jButtonFA, gridBagConstraints);

        jButtonPM.setText("Select");
        jButtonPM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPMActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jButtonPM, gridBagConstraints);

        jButtonLA.setText("Select");
        jButtonLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLAActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jButtonLA, gridBagConstraints);

        jScrollPane1.setEnabled(false);
        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jTextPane2.setEditable(false);
        jTextPane2.setEnabled(false);
        jScrollPane2.setViewportView(jTextPane2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane2, gridBagConstraints);

        jTextPane3.setEditable(false);
        jTextPane3.setEnabled(false);
        jScrollPane3.setViewportView(jTextPane3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jScrollPane3, gridBagConstraints);

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    // End of variables declaration//GEN-END:variables
    
}
