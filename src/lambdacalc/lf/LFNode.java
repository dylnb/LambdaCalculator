package lambdacalc.lf;

import lambdacalc.logic.Expr;
import java.beans.*;
import java.util.SortedMap;
import java.util.TreeMap;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

public abstract class LFNode {
    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    /**
     * The symbol used for separating the label from its index (if any)
     * in the #toString() method.
     */
    public static final char INDEX_SEPARATOR = '_'; 

    protected String label;
    
    protected int index = -1;
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        String oldLabel = this.label;
        this.label = label;
        changes.firePropertyChange("label", oldLabel, label);
    }
    
    /**
     * Returns -1 if no index has been set.
     */
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        int oldIndex = this.index;
        this.index = index;
        changes.firePropertyChange("index", oldIndex, index);
    }
    
    public void removeIndex() {
        setIndex(-1);
    }
    
    public boolean hasIndex() {
        return this.index != -1;
    }
    
    /**
     * Returns a map of properties. Keys are Strings and values are Objects.
     * Each entry represents a property-value pair. Properties include orthographic
     * strings, meanings, types, etc.
     *
     * @return a sorted map of properties
     */
    public SortedMap getProperties() {
        SortedMap m = new TreeMap();
        m.put("Text", this.getLabel());
        Type t = null;
        try {
            t = this.getMeaning().getType();
        } catch (TypeEvaluationException ex) {
            //ex.printStackTrace();
        } catch (MeaningEvaluationException ex) {
            //ex.printStackTrace();
        }
        m.put("Type", t); 
        Integer index = null;
        if (this.hasIndex()) { index = new Integer(this.getIndex()); }
        m.put("Index", index);
        return m;
    }

    
    public String toString() {
        String result = getLabel();
        if (hasIndex()) {
            result += INDEX_SEPARATOR + getIndex();
        } 
        return result;
    }
    
    public Expr getMeaning() throws MeaningEvaluationException {
        return getMeaning(new AssignmentFunction());
    }
    
    public abstract Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException;
    
    public abstract void guessLexicalEntriesAndRules
            (Lexicon lexicon, RuleList rules);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }    
}