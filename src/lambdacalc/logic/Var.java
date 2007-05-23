/*
 * Var.java
 *
 * Created on May 29, 2006, 3:04 PM
 */

package lambdacalc.logic;

import java.awt.ItemSelectable;
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
        
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're doing substitutions in a lambda conversion. If this is the variable
        // we're doing substitutions on, we have to think carefully.
        if (!this.equals(var))
            return this;
        
        // In any case, we'll just return our replacement. However, we must check
        // if any free variables in the replacement would be accidentally bound
        // by any of the binders that scope over this variable. We'll do this
        // inefficiently because expressions ought to be fairly small.
        for (Iterator bi = binders.iterator(); bi.hasNext(); ) {
            Binder b = (Binder)bi.next();
            Identifier bvi = b.getVariable();
            if (!(bvi instanceof Var))
                continue;
            
            Var bv = (Var)bvi;
            
            for (Iterator fvs = replacement.getFreeVars().iterator(); fvs.hasNext(); ) {
                Var fv = (Var)fvs.next();
                
                // If this free variable matches the bound variable, mark the binder
                // as an accidental binder that will elsewhere have to get fixed up.
                if (fv.equals(bv))
                    accidentalBinders.add(b);
            }   
        }
        
        return replacement;
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        if (updates.containsKey(this))
            return (Expr)updates.get(this);
        return this;
    }

    Var(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
