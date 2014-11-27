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
 * ArgList.java
 *
 * Created on May 31, 2006, 4:14 PM
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a list of two or more arguments to a predicate, i.e. a vector.
 * ArgLists are not used for the arguments of one-place
 * predicates, which are represented by Identifiers.  
 */
public class ArgList extends NAry {
    /**
     * Constructs the ArgList.
     * @param innerExpressions an array of two or more expressions
     */
    public ArgList(Expr[] innerExpressions) {
        super(innerExpressions);
        if (innerExpressions.length <= 1) throw new IllegalArgumentException("ArgList must have more than one element.");
    }
    
    protected String getOpenSymbol() { return "("; }

    protected String getCloseSymbol() { return ")"; }
    
    public Type getType() throws TypeEvaluationException {
        Type[] t = new Type[getArity()];
        for (int i = 0; i < t.length; i++)
            t[i] = getElements()[i].getType();
        return new ProductType(t);
    }

    public Expr createFromSubExpressions(Expr[] subExpressions)
     throws IllegalArgumentException {
        return new ArgList(subExpressions);
    }
    
    ArgList(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

}
