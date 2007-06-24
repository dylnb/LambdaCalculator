/*
 * Terminal.java
 *
 * Created on June 13, 2007, 1:44 PM
 *
 */

package lambdacalc.lf;

/**
 *
 * @author champoll
 */
public abstract class Terminal extends LFNode {
    protected Terminal() {
        super();
    }
    
    protected Terminal(String label, int index) {
        super(label, index);
    }
    
    /**
     * Nothing to do on a Terminal.
     *
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessRules(RuleList rules, boolean nonBranchingOnly) {
    
    }    

}
