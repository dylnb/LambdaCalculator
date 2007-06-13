/*
 * JLexiconList.java
 *
 * Created on June 13, 2007, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lambdacalc.logic.*;
import lambdacalc.lf.*;

/**
 *
 * @author tauberer
 */
public class LexiconList extends JPanel
        implements TreeExerciseWidget.SelectionListener, ListSelectionListener, PropertyChangeListener {
    TreeExerciseWidget tewidget;
    
    JList listbox = new JList();
    DefaultComboBoxModel entries = new DefaultComboBoxModel();
    
    LFNode propChangeRegisteredNode;
    
    public LexiconList() {
        add(listbox);
        listbox.setModel(entries);
    }
    
    public void initialize(TreeExerciseWidget widget) {
        this.tewidget = widget;
        
        tewidget.addSelectionListener(this);
        listbox.addListSelectionListener(this);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
    
    public void selectionChanged(TreeExerciseWidget.SelectionEvent evt) {
        // Fired when the selected node in the TreeExerciseWidget changes.
        
        if (propChangeRegisteredNode != null)
            propChangeRegisteredNode.removePropertyChangeListener(this);
        
        LFNode curNode = tewidget.getSelectedNode();
        if (curNode == null)
            showLexiconForWord(null, null);
        else if (curNode instanceof LexicalTerminal)
            showLexiconForWord(curNode.getLabel(), (LexicalTerminal)curNode);
        else
            showLexiconForWord(null, null);
        
        propChangeRegisteredNode = curNode;
        propChangeRegisteredNode.addPropertyChangeListener(this);
        
    }
    
    private void showLexiconForWord(String orthoForm, LexicalTerminal node) {
        if (tewidget == null)
            throw new IllegalStateException("Widget has not been set.");
        
        entries.removeAllElements();
        
        if (orthoForm == null)
            return;
        
        Expr[] meanings = tewidget.getLexicon().getMeanings(orthoForm);
        for (int i = 0; i < meanings.length; i++)
            entries.addElement(meanings[i]);
        
        updateListSelection(node);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // Fired when the node we're viewing changes
        if (e.getPropertyName().equals("meaning")) {
            updateListSelection((LexicalTerminal)e.getSource());
        }
    }
    
    private void updateListSelection(LexicalTerminal node) {
        try {
            listbox.setSelectedValue(node.getMeaning(), true);
        } catch (MeaningEvaluationException mee) {
            listbox.clearSelection();
        }
    }
    
    
    public void valueChanged(ListSelectionEvent e) {
        // listening to the change event on our list
        if (listbox.getSelectedValue() == null) return;
        
        LFNode node = tewidget.getSelectedNode();
        if (node == null) return;
        if (!(node instanceof LexicalTerminal)) return;
        
        Expr item = (Expr)listbox.getSelectedValue();
        ((LexicalTerminal)node).setMeaning(item);
    }
}
