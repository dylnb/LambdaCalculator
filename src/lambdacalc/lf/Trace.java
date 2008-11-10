/*
 * Trace.java
 *
 * Created on June 5, 2007, 5:42 PM
 *
 */

package lambdacalc.lf;

import java.util.logging.Level;
import java.util.logging.Logger;
import lambdacalc.logic.Expr;
import lambdacalc.logic.GApp;
import lambdacalc.logic.TypeEvaluationException;

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

    public String toLatexString() {
        try {
            // TODO should this be changed as in this.getMeaning?
            return this.getLabel() + "_{" + this.getIndex() + "}" + "\\\\" + this.getMeaning(null).getType().toLatexString() + "\\\\$" + this.getMeaning(null).toLatexString() + "$";
        } catch (MeaningEvaluationException ex) {
            // we don't expect this to occur
            ex.printStackTrace();
            return "";
        } catch (TypeEvaluationException ex) {
            // we don't expect this to occur
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Nothing to do on a Trace.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
    
    }
    
}
