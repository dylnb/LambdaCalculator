package lambdacalc.lf;

import lambdacalc.logic.Expr;

public abstract class LFNode {
    
    /**
     * The symbol used for separating the label from its index (if any)
     * in the #toString() method.
     */
    public static final String SEPARATOR = "_"; 

    protected String label;
    
    protected int index = -1;
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    /**
     * Returns -1 if no index has been set.
     */
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public void removeIndex() {
        setIndex(-1);
    }
    
    public boolean hasIndex() {
        return this.index!=-1;
    }

    public String toString() {
        String result = getLabel();
        if (hasIndex()) {
            result += SEPARATOR + getIndex();
        } 
        return result;
    }
        
    public Expr getMeaning() throws MeaningEvaluationException {
        return getMeaning(null);
    }
    
    public abstract Expr getMeaning(AssignmentFunction a) 
    throws MeaningEvaluationException;
    
    public abstract void guessLexicalEntriesAndRules
            (Lexicon lexicon, RuleList rules);
    
}