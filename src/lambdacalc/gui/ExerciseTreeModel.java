/*
 * ExerciseTreeModel.java
 *
 * Created on June 2, 2006, 11:19 AM
 */

package lambdacalc.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import lambdacalc.exercises.*;


/**
 * This class implements a TreeModel over an exercise file for
 * the tree view in the main window.  It just forms a hierarchical
 * model over the file, with the file as root, ExerciseGroups as
 * children wrapped by ExerciseGroupWrapper, and Exercises within
 * the groups wrapped by ExerciseWrapper.  Wrappers are used
 * to alter the toString() results for display in the tree.
 */
public class ExerciseTreeModel implements TreeModel {
    public static final String CHECKMARK = "\u2713";
    public static final String BALLOTBOX = "\u2610";
    public static final String BALLOT_EX = "\u2717";
    
    ExerciseFile file;
    ArrayList listeners = new ArrayList();
    
    public ExerciseTreeModel(ExerciseFile file) {
        this.file = file;
    }
    
    public Object getRoot() {
        return file;
    }
    
    public Object getChild(Object parent, int index) {
        if (parent instanceof ExerciseFile) {
            return new ExerciseGroupWrapper( ((ExerciseFile)parent).getGroup(index)) ;
        } else if (parent instanceof ExerciseGroupWrapper) {
            return new ExerciseWrapper( ((ExerciseGroupWrapper)parent).group.getItem(index) );
        } else {
            return null;
        }
    }
    
    public int getChildCount(Object parent) {
        if (parent instanceof ExerciseFile) {
            return ((ExerciseFile)parent).size();
        } else if (parent instanceof ExerciseGroupWrapper) {
            return ((ExerciseGroupWrapper)parent).group.size();
        } else {
            return 0;
        }
    }
    
    public boolean isLeaf(Object node) {
        return node instanceof ExerciseWrapper;
    }
    
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new RuntimeException();
    }
    
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == null || child == null) return -1;
        if (child instanceof ExerciseGroupWrapper) return ((ExerciseGroupWrapper)child).group.getIndex();
        if (child instanceof ExerciseWrapper) return ((ExerciseWrapper)child).ex.getIndex();
        throw new RuntimeException(child.getClass().getName());
    }
    
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }
    
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void onChange() {
        for (int i = 0; i < listeners.size(); i++)
            ((TreeModelListener)listeners.get(i)).treeNodesChanged(null);
    }
    
    public static class ExerciseGroupWrapper {
        public ExerciseGroup group;
        
        public ExerciseGroupWrapper (ExerciseGroup g) { group = g; }
        
        public String toString() {
            return (char)((int)'A' + group.getIndex()) + ". " + group.getTitle();
        }
    }
    
    public static class ExerciseWrapper {
        public Exercise ex;
        
        public ExerciseWrapper (Exercise e) { ex = e; }
        
        public String toString() {
                               // checkmark // empty ballot box
 //           return (ex.isDone() ? CHECKMARK : BALLOTBOX) + "  " + (ex.getIndex()+1) + ". " + ex.getExerciseText();    
            return (ex.isDone() ? CHECKMARK : BALLOTBOX) + "  " + (ex.getIndex()+1) + ". " + ex.getShortTitle();   
        }
    }
}
