/*
 * Var.java
 *
 * Created on May 29, 2006, 3:04 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a constant.  Constants are considered equal if
 * their names and types are equal.
 */
public class Const extends Identifier {
  
    /**
     * Creates a constant.
     * @param repr the name of the constant
     * @param type the type of the constant
     */
    public Const(String repr, Type type) {
        super(repr, type); 
    }    

    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        return ret;
    }

    protected boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap) {
        // ignore maps in all cases, since it only applies to variables
        if (i instanceof Const)
            return this.getType().equals(i.getType()) 
                && this.getSymbol().equals(i.getSymbol());
        else
            return false;
    }
    
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're doing substitutions. Clearly, not applicable to a constant.
        return this;
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return this;
    }
}
