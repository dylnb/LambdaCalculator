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
 *
 * @author champoll
 */
public class Trace extends Terminal {
    
    public static final String SYMBOL = "t";
    
    /** Creates a new instance of Trace */
    private Trace() {
    }
    
    
    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        //TODO don't ignore g
        //return (Expr) g.get(this.getIndex());
        return new GApp(this.getIndex());
    }    
    
    public Trace(int index) {
        this.index = index;
        this.label = SYMBOL;
    }
    
    public String getLabel() {
        return SYMBOL;
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
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessLexicalEntriesAndRules(Lexicon lexicon, RuleList rules) {
    
    }
    

    
}
