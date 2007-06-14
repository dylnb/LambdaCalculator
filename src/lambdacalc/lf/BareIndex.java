/*
 * BareIndex.java
 *
 * Created on June 5, 2007, 8:42 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;

/**
 *
 * @author champoll
 */
public class BareIndex extends Terminal {
    
    
    /** Creates a new instance of BareIndex */
    private BareIndex() {
    }
    
    public BareIndex(int i) {
        this.setIndex(i);
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
        return "Lambda-abstraction index";
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        throw new MeaningEvaluationException("Tried to get the meaning of a BareIndex");
    }
    
    /**
     * Nothing to do on a BareIndex.
     *
     * @param lexicon the lexicon
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessLexicalEntriesAndRules(Lexicon lexicon, RuleList rules) {
     // nothing to do on a BareIndex
    }    
    
    public String toString() {
        if (this.getLabel() == null) {
            return Integer.toString(this.getIndex());
        } else {
            return super.toString();
        }
    }
}
