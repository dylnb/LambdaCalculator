/*
 * JLexiconList.java
 *
 * Created on June 13, 2007, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.gui;

import java.awt.event.*;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lambdacalc.logic.*;
import lambdacalc.lf.*;
import lambdacalc.exercises.*;

/**
 *
 * @author tauberer
 */
public class LexiconList extends JPanel
        implements TreeExerciseWidget.SelectionListener, ListSelectionListener, PropertyChangeListener {
    
    ExerciseFile exFile;
    Exercise exercise;
    TreeExerciseWidget tewidget;
    
    JList listbox = new JList();
    DefaultComboBoxModel entries = new DefaultComboBoxModel();
    
    LambdaEnabledTextField lambdaEditor = new LambdaEnabledTextField();
    JButton buttonSetDenotation = new JButton("Assign Denotation");
    JLabel labelWhatToDo = new JLabel("Enter the denotation for the selected terminal node, or select a denotation from the list.");

    LexicalTerminal currentNode;
    
    Vector listeners = new Vector();
    
    public interface ChangeListener {
        void changeMade();
    }
    
    public LexiconList() {
        //setLayout(new BorderLayout());
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        labelWhatToDo.setAlignmentX(0F);
        add(labelWhatToDo);
        
        JPanel addpanel = new JPanel();
        add(addpanel);
        addpanel.setLayout(new BorderLayout());
        addpanel.add(lambdaEditor, BorderLayout.CENTER);
        addpanel.add(buttonSetDenotation, BorderLayout.EAST);
        
        add(new JScrollPane(listbox));
        listbox.setModel(entries);
        listbox.addListSelectionListener(this);
        
        lambdaEditor.addActionListener(new SetDenotationListener());
        buttonSetDenotation.addActionListener(new SetDenotationListener());
        
        lambdaEditor.setTemporaryText("enter an expression");
    }
    
    public void addListener(ChangeListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    public void removeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
    private void fireChangeMade() {
        for (Iterator i = listeners.iterator(); i.hasNext(); )
            ((ChangeListener)i.next()).changeMade();
    }
    
    public void initialize(ExerciseFile exFile, Exercise exercise, TreeExerciseWidget widget) {
        if (this.tewidget != null)
            this.tewidget.removeSelectionListener(this);
        
        this.exFile = exFile;
        this.exercise = exercise;
        this.tewidget = widget;
        
        tewidget.addSelectionListener(this);
        selectionChanged(null);
    }

    public void selectionChanged(TreeExerciseWidget.SelectionEvent evt) {
        // Fired when the selected node in the TreeExerciseWidget changes.
        // Ignore 'evt': We call it with null above.
        
        if (currentNode != null)
            currentNode.removePropertyChangeListener(this);
        
        LFNode curNode = tewidget.getSelectedNode();
        if (curNode == null
                || !(curNode instanceof LexicalTerminal)
                || ((LexicalTerminal)curNode).getLabel() == null) {
            showLexiconForWord(null);
            labelWhatToDo.setEnabled(false);
            buttonSetDenotation.setEnabled(false);
            lambdaEditor.setEnabled(false);
            lambdaEditor.setTemporaryText("enter an expression");
            return;
        }
        
        showLexiconForWord((LexicalTerminal)curNode);
        
        currentNode = (LexicalTerminal)curNode;
        currentNode.addPropertyChangeListener(this);

        labelWhatToDo.setEnabled(true);
        buttonSetDenotation.setEnabled(true);
        lambdaEditor.setEnabled(true);
    }
    
    private void showLexiconForWord(LexicalTerminal node) {
        if (tewidget == null)
            throw new IllegalStateException("Widget has not been set.");
        
        if (node != null && node.hasMeaning()) {
            try {
                lambdaEditor.setText(node.getMeaning().toString());
            } catch (MeaningEvaluationException mee) {
                // not fired by LexicalTerminal if hasMeaning() is true
            }
        } else {
            lambdaEditor.setTemporaryText("enter an expression");
        }
        
        entries.removeAllElements();
        
        if (node == null)
            return;

        String orthoForm = node.getLabel();
        if (orthoForm == null)
            return;
        
        // Put first the lexical entries that are listed for the
        // terminal node's label.
        HashSet seenExprs = new HashSet();
        Expr[] meanings = exFile.getLexicon().getMeanings(orthoForm);
        for (int i = 0; i < meanings.length; i++) {
            entries.addElement(meanings[i]);
            seenExprs.add(meanings[i]);
        }
        
        // Then put all other lexical entries, since some might be
        // close to what the user wants.
        Expr[] meanings2 = exFile.getLexicon().getMeanings(null);
        for (int i = 0; i < meanings2.length; i++)
            if (!seenExprs.contains(meanings2[i]))
                entries.addElement(meanings2[i]);
            
        updateListSelection(node);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // Fired when the node we're viewing changes
        if (e.getPropertyName().equals("meaning")) {
            updateListSelection((LexicalTerminal)e.getSource());
        }
    }
    
    private void updateListSelection(LexicalTerminal node) {
        listbox.clearSelection();
        try {
            listbox.setSelectedValue(node.getMeaning(), true);
        } catch (MeaningEvaluationException mee) {
        }
    }
    
    
    public void valueChanged(ListSelectionEvent e) {
        // Fired when the list box's selection changes
        
        if (listbox.getSelectedValue() == null) return;
        
        LFNode node = tewidget.getSelectedNode();
        if (node == null) return;
        if (!(node instanceof LexicalTerminal)) return;
        
        Expr item = (Expr)listbox.getSelectedValue();
        ((LexicalTerminal)node).setMeaning(item);
        lambdaEditor.setText(item.toString());
        fireChangeMade();
    }
    
    class SetDenotationListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (currentNode == null)
                return;
            
            ExpressionParser.ParseOptions opts = new ExpressionParser.ParseOptions();
            opts.ASCII = false;
            opts.singleLetterIdentifiers = false; // TODO!
            opts.typer = ((HasIdentifierTyper)exercise).getIdentifierTyper();
            
            try {
                Expr ex = ExpressionParser.parse(lambdaEditor.getText(), opts);
                ex.getType(); // just checking if an exception is thrown

                // Add the meaning to the lexicon for this exercise file if it's
                // not already in the lexical list for the label of this terminal.
                boolean isInLexicon = false;
                Expr[] existingMeanings = exFile.getLexicon().getMeanings(currentNode.getLabel());
                for (int i = 0; i < existingMeanings.length; i++)
                    if (existingMeanings[i].equals(ex))
                        isInLexicon = true;
                
                if (!isInLexicon) {
                    exFile.getLexicon().addLexicalEntry(currentNode.getLabel(), ex);
                    showLexiconForWord(currentNode);
                }
                    
                // Set the meaning of the selected node.
                currentNode.setMeaning(ex);
                fireChangeMade();
            
            } catch (SyntaxException se) {
                Util.displayErrorMessage(TrainingWindow.getSingleton(), se.getMessage(), "Add Lexical Entry");
                if (se.getPosition() != -1)
                    lambdaEditor.setCaretPosition(se.getPosition());
            } catch (TypeEvaluationException tee) {
                Util.displayErrorMessage(TrainingWindow.getSingleton(), tee.getMessage(), "Add Lexical Entry");
            }
            
            lambdaEditor.requestFocus();
        }
    }
}
