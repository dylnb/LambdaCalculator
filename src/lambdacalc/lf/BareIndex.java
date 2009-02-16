/*
 * BareIndex.java
 *
 * Created on June 5, 2007, 8:42 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;

/**
 *
 * @author champoll
 */
public class BareIndex extends Terminal {

    private Type type = Type.E; // default
    
    public BareIndex(String label, int i,Type type) {
        this.setLabel(label);
        this.setIndex(i);
        this.type = type;
    }
    
    public BareIndex(int i,Type type) {
        this.setIndex(i);
        this.type = type;
    }
    
//    public String getLabel() {
//        return this.getIndex()+"";
//    }
//    
//    public void setLabel(String label) {
//        throw new UnsupportedOperationException("Tried to set the label of a bare index.");
//    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
    }
    
    public String getDisplayName() {
        return "Bare index";
    }
    
    public boolean isMeaningful() {
        return false;
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        throw new MeaningEvaluationException("The bare index \"" + toShortString() + "\" has no denotation.");
    }
    
    /**
     * Nothing to do on a BareIndex.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
    
    }

    public String toLatexString() {
        return String.valueOf(this.getIndex());
    }
    
    
    public String toString() {
        if (this.getLabel() == null) {
            return Integer.toString(this.getIndex());
        } else {
            return super.toString();
        }
    }
    /**
     * @return the type of this bare index
     */
    public Type getType() {
        return this.type;
    }
}
