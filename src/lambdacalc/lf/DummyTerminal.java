/*
 * DummyTerminal.java
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;

/**
 * This class represents a terminal node that is on the tree but is not really
 * meant to be a part of the semantic representation. It is usually parenthesized
 * terminals, at least in Maribel's way of doing things.
 * @author tauberer
 */
public class DummyTerminal extends Terminal {
    
    /** Creates a new instance of DummyTerminal */
    public DummyTerminal(String label) {
        setLabel(label);
    }
    
    public String getDisplayName() {
        return "Dummy terminal";
    }
    
    public boolean isMeaningful() {
        return false;
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        throw new MeaningEvaluationException("\"" + toShortString() +"\" does not have a denotation.");
    }
    
    public void guessLexicalEntries(Lexicon lexicon) {
    }

    public String toLatexString() {
        return "\\mbox{"+this.getLabel()+"}";
    }
}
