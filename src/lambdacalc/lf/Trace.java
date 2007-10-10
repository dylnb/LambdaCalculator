/*
 * Trace.java
 *
 * Created on June 5, 2007, 5:42 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.GApp;

/**
 * A trace or a pronoun.
 *
 * @author champoll
 */
public class Trace extends Terminal {
    
    public static final String SYMBOL = "t";
    
    public Trace(String label, int index) {
        super(label, index);
    }
    
    public Trace(int index) {
        this(SYMBOL, index);
    }
    
    public boolean isMeaningful() {
        return true;
    }
    
    public boolean isActualTrace() {
        return this.getLabel().equals(this.SYMBOL);
    }
    
    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        if (g == null)
            return new GApp(this.getIndex());
        else
            return (Expr)g.get(getIndex());
    }    

    public void setLabel(String label) {
        throw new UnsupportedOperationException("Tried to set the label of a trace.");
    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a trace.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a trace.");
    }
    
    public String getDisplayName() {
        return "Trace";
    }

    /**
     * Nothing to do on a Trace.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
    
    }
    
}
