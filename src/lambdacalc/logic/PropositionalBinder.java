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
 * PropositionalBinder.java
 *
 * Created on May 30, 2006, 5:28 PM
 */

package lambdacalc.logic;

/**
 * Abstract base class of the propositional binders (universal and existential quantification).
 */
public abstract class PropositionalBinder extends Binder {
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public PropositionalBinder(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 8;
    }
    
    public Type getType() throws TypeEvaluationException {
        checkVariable();
        if (getInnerExpr().getType().matches(Type.T) == null)
            throw new TypeEvaluationException("The inside of the propositional binder in " + toString() + " must be of type t.");
        return Type.T;
    }
    
    PropositionalBinder(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
