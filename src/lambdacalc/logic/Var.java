/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * Var.java
 *
 * Created on May 29, 2006, 3:04 PM
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a variable.
 */
public class Var extends Identifier {

    public static final Var X = 
            new Var("x", Type.E, false);

    public static final Var Z = 
            new Var("z", Type.E, false);
    
    /**
     * Constructs a variable with the given name and type.
     */
    public Var(String repr, Type type, boolean isTypeExplicit) {
        super(repr, type, isTypeExplicit);
    }
    public Var(String repr, Type type, boolean isTypeExplicit, boolean starred) {
        super(repr, type, isTypeExplicit, starred);
    }

    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        ret.add(this);
        return ret;
    }

    protected boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap, Map freeVarMap, boolean matching) {
        // we use the map here...
        if (i instanceof Var) {
            if (this.getType().matches(i.getType()) == null) {
                return false;
            } // else...

            Object thisside = (thisMap == null) ? null : thisMap.get(this);
            Object otherside = (otherMap == null) ? null : otherMap.get(i);

            if (thisside == null && otherside == null) {
                if (freeVarMap == null) {
                    // This variable is free on both sides. If we are not allowing for free variable
                    // renaming, then just check that the symbols are the same.
                    return getSymbol().equals(i.getSymbol());
                } else {
                    // We are allowing for the consistent renaming of free variables. See if we
                    // already have a renaming in place for this variable.
                    Var n = (Var)freeVarMap.get(this);
                    if (n == null) {
                        // No renaming yet. If something else maps to i, then we don't have a
                        // consistent 1:1 renaming.
                        if (freeVarMap.values().contains(i))
                            return false;

                        // Add this renaming and return true to indicating that
                        // the consistent renaming is OK so far.
                        freeVarMap.put(this, i);
                        return true;
                    } else {
                        // We have a renaming of this variable already. Return whether the renaming
                        // is consistent with this case.
                        return i.equals(n);
                    }
                }
            }

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
        
        Set freevars = replacement.getFreeVars();
        
        // In any case, we'll just return our replacement. However, we must check
        // if any free variables in the replacement would be accidentally bound
        // by any of the binders that scope over this variable. We'll do this
        // inefficiently because expressions ought to be fairly small.
        for (Iterator bi = binders.iterator(); bi.hasNext(); ) {
            VariableBindingExpr b = (VariableBindingExpr)bi.next();
            if (b.bindsAny(freevars))
                accidentalBinders.add(b);
        }
        
        return replacement;
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        if (updates.containsKey(this))
            return (Expr)updates.get(this);
        return this;
    }
    
    public Expr createAlphatypicalVariant(HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        if (updates.containsKey(this)) {
            return (Expr)updates.get(this);
        }
        return this;
    }

    /**
     * Creates a new instance of this variable, that is, shallowly copies it.
     *
     * @return a copy of this
     */    
    protected Identifier create() {
        return new Var(this.getSymbol(), this.getType(), this.isTypeExplicit());
    }
    
    Var(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
