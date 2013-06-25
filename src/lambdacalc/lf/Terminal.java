/*
 * Terminal.java
 *
 * Created on June 13, 2007, 1:44 PM
 *
 */

package lambdacalc.lf;

import java.util.List;
import java.util.Vector;
import lambdacalc.logic.Type;

/**
 *
 * @author champoll
 */
public abstract class Terminal extends LFNode {
    
    private Type type = null;
    
    private boolean explicitType = false;
    
    protected Terminal() {
        super();
    }
    
    protected Terminal(String label, int index) {
        super(label, index);
        this.type = type.E; // default type E
    }
    
    protected Terminal(String label, int index, Type type) {
        super(label, index);
        this.type = type;
        this.explicitType = true;
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

    public abstract String toLatexString();
    
    public String toStringTerminalsOnly() {
//        if (this.getLabel() == null) return ""; else return this.getLabel();
        return this.toShortString();
    }
    
    public Type getType() {
        return this.type;
    }
        
    public void setType(Type t) {
        this.type = t;
    }
    
    public boolean hasExplicitType() {
        return this.explicitType;
    }
    
    public void switchOnExplicitType() {
        this.explicitType = true;
    }


}
