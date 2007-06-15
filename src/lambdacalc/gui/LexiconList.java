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
    JButton buttonAdd = new JButton("Add lexical entry");

    LexicalTerminal currentNode;
    
    public LexiconList() {
        setLayout(new BorderLayout());
        
        //add(new JLabel("Lexicon"), BorderLayout.NORTH);
        
        add(new JScrollPane(listbox), BorderLayout.CENTER);
        listbox.setModel(entries);
        listbox.addListSelectionListener(this);
        
        JPanel addpanel = new JPanel();
        add(addpanel, BorderLayout.SOUTH);
        addpanel.setLayout(new BorderLayout());
        addpanel.add(lambdaEditor, BorderLayout.CENTER);
        addpanel.add(buttonAdd, BorderLayout.EAST);
        
        lambdaEditor.addActionListener(new AddButtonListener());
        buttonAdd.addActionListener(new AddButtonListener());
        
        lambdaEditor.setTemporaryText("enter a new lexical entry");
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
            buttonAdd.setEnabled(false);
            lambdaEditor.setEnabled(false);
            return;
        }
        
        showLexiconForWord((LexicalTerminal)curNode);
        
        currentNode = (LexicalTerminal)curNode;
        currentNode.addPropertyChangeListener(this);

        buttonAdd.setEnabled(true);
        lambdaEditor.setEnabled(true);
    }
    
    private void showLexiconForWord(LexicalTerminal node) {
        if (tewidget == null)
            throw new IllegalStateException("Widget has not been set.");
        
        entries.removeAllElements();
        
        if (node == null)
            return;

        String orthoForm = node.getLabel();
        if (orthoForm == null)
            return;
        
        Expr[] meanings = exFile.getLexicon().getMeanings(orthoForm);
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
        // Fired when the selected item in the list changes
        
        if (listbox.getSelectedValue() == null) return;
        
        LFNode node = tewidget.getSelectedNode();
        if (node == null) return;
        if (!(node instanceof LexicalTerminal)) return;
        
        Expr item = (Expr)listbox.getSelectedValue();
        ((LexicalTerminal)node).setMeaning(item);
    }
    
    class AddButtonListener implements ActionListener {
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

                exFile.getLexicon().addLexicalEntry(currentNode.getLabel(), ex);
                showLexiconForWord(currentNode);

                currentNode.setMeaning(ex);
            
            } catch (SyntaxException se) {
                Util.displayErrorMessage(LexiconList.this, se.getMessage(), "Add Lexical Entry");
                if (se.getPosition() != -1)
                    lambdaEditor.setCaretPosition(se.getPosition());
            } catch (TypeEvaluationException tee) {
                Util.displayErrorMessage(LexiconList.this, tee.getMessage(), "Add Lexical Entry");
            }
            
            lambdaEditor.requestFocus();
        }
    }
}
