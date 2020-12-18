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
import java.util.Map;
import java.util.Set;

/**
 * Represents a constant.  Constants are considered equal if
 * their names and types are equal.
 */
public class Const extends Identifier {
  
    /**
     * Creates a constant.
     * 
     * @param symbol the name of the constant
     * @param type the type of the constant
     */
    public Const(String symbol, Type type, boolean isTypeExplicit) {
        super(symbol, type, isTypeExplicit);
    }
    public Const(String symbol, Type type, boolean isTypeExplicit, boolean starred) {
        super(symbol, type, isTypeExplicit, starred); 
    }    

    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        return ret;
    }

    protected boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap, Map freeVarMap, boolean matching) {
        // ignore maps in all cases, since it only applies to variables
        if (i instanceof Const) {
            // Possibly replace this.getType().equals(...) with this.getType().matches(...)
            boolean typeEquals = this.getType().equals(i.getType());
            boolean symbolEquals = this.getSymbol().equals(i.getSymbol());
            return typeEquals && symbolEquals;
        }
        else
            return false;
    }
    
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're doing substitutions. Clearly, not applicable to a constant.
        return this;
    }

    /**
     * Creates a new instance of this constant, that is, shallowly copies it.
     *
     * @return a copy of this
     */    
    protected Identifier create() {
        return new Const(this.getSymbol(), this.getType(), this.isTypeExplicit());
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return this;
    }
    
    public Expr createAlphatypicalVariant(HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        return this;
    }

    Const(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
