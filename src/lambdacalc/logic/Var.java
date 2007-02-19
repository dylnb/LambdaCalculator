/*
 * Var.java
 *
 * Created on May 29, 2006, 3:04 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a variable.
 */
public class Var extends Identifier {

    /**
     * Constructs a variable with the given name and type.
     */
    public Var(String repr, Type type) {
        super(repr, type);
    }

    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        ret.add(this);
        return ret;
    }

    protected Expr substitute(Var var, Expr replacement, Set unboundVars, Set potentialAccidentalBindings, java.util.Set accidentalBindings) {
        if (!this.equals(var)) return this;
        for (Iterator i = potentialAccidentalBindings.iterator(); i.hasNext(); )
             accidentalBindings.add(i.next());
        return replacement;
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        if (updates.containsKey(this))
            return (Expr)updates.get(this);
        return this;
    }

    protected boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap) {
 
        
        // we use the map here...
        if (i instanceof Var) {
            if (!this.getType().equals(i.getType())) {
                return false;
            } // else...
            
            Object thisside = (thisMap == null) ? null : thisMap.get(this);
            Object otherside = (otherMap == null) ? null : otherMap.get(i);
                    
            if (thisside == null && otherside == null)
                return getSymbol().equals(i.getSymbol());
            
            // one side is bound but the other is not
            if (thisside == null || otherside == null)
                return false;
            
            // are they bound by the same binder, i.e. do they
            // map to the same fresh variable
            return thisside == otherside;
                
         } else // i is not Var
            return false;
    }
        
    
}
