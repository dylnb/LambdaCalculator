/*
 * Terminal.java
 *
 * Created on June 13, 2007, 1:44 PM
 *
 */

package lambdacalc.lf;

import java.util.List;
import java.util.Vector;

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
    List children = new Vector(0);
    public List getChildren() {
        return children;
    }
    
    public String toStringTerminalsOnly() {
//        if (this.getLabel() == null) return ""; else return this.getLabel();
        return this.toShortString();
    }

}
