package lambdacalc.lf;

import lambdacalc.logic.Expr;
import java.beans.*;

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