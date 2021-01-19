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
 * Cardinality.java
 */

package lambdacalc.logic;

/**
 * Represents the cardinality | ... | operator.
 */
public class Cardinality extends Unary {
    /**
     * The unicode negation symbol.
     */
    public static final char SYMBOL = '|'; // \u007C is a vertical bar, but is it different from a pipe?
    
    public static final char INPUT_SYMBOL = '|';
    
    /**
     * Constructs the cardinality operator around the given expression.
     */
    public Cardinality(Expr expr) {
        super(expr);
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 3;
    }
    
    protected String toString(int mode) {
        if (mode == LATEX) {
            return SYMBOL + getInnerExpr().toString(mode) + SYMBOL;
        } else {
            return SYMBOL + getInnerExpr().toString(mode) + SYMBOL;
        }
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!(getInnerExpr().getType() instanceof CompositeType) || (((CompositeType)getInnerExpr().getType()).getRight().matches(Type.T) == null))
            throw new TypeMismatchException("The cardinality operator can only be applied to something that has the type of a set, i.e. the type of the characteristic function of a set, such as " + Type.ET + ", but " + getInnerExpr() + " is of type " + getInnerExpr().getType() + ".");
        return Type.N;
    }

    protected Unary create(Expr inner) {
        return new Cardinality(inner);
    }

    Cardinality(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
