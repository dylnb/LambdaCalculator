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
 * LogicalBinary.java
 *
 * Created on June 1, 2006, 2:48 PM
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * Abstract base class of the logical binary connectives
 * (and, or, if, iff, equality, and the set and numeric relations).
 */
public abstract class LogicalBinary extends Binary {
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public LogicalBinary(Expr left, Expr right) {
        super(left, right);
    }
    
    protected String toString(int mode) {
        String symbol = getSymbol();
        if (mode == Expr.LATEX) {
            symbol = this.getLatexRepr();
        }
        return partToString(getLeft(), mode) + " " + symbol + " " + partToString(getRight(), mode);
    }
    
    private String partToString(Expr expr, int mode) {
        // And, Or, Intersect, and Union are associative, so we omit parens for nested these.
        if ((this instanceof And ||
             this instanceof Or ||
             this instanceof SetRelation.Intersect ||
             this instanceof SetRelation.Union)
            && expr.getClass() == getClass()) {
            return expr.toString(mode);
        }
        return nestedToString(expr, mode);
    }
    
    /**
     * Gets the unicode symbol associated with the binary connective.
     */
    public abstract String getSymbol();

    public abstract String getLatexRepr();

    /**
     * Gets the type that the operands must be. Not called if getType() is overridden.
     */
    public abstract Type getOperandType();
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     * Intersect and Union override this for a lower precendence
     */
    public int getOperatorPrecedence() {
        return 6;
    }
    
    public Type getType() throws TypeEvaluationException {
        // Our default implementation checks that the operands are of type t,
        // but this is overridden in Equality which only checks that the
        // types of the operands are the same.
        if (!getLeft().getType().equals(getOperandType()))
            throw new TypeMismatchException("The parts of the logical connective " + getSymbol() + " must be of type " + getOperandType() + ", but " + getLeft() + " is of type " + getLeft().getType() + ".");
        if (!getRight().getType().equals(getOperandType()))
            throw new TypeMismatchException("The parts of the logical connective " + getSymbol() + " must be of type " + getOperandType() + ", but " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }    
    
    LogicalBinary(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
