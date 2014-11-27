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
 * NumericRelation.java
 *
 * Created on May 29, 2006, 3:24 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the numeric less than and greater than (or equal) relations.
 */
public abstract class NumericRelation extends LogicalBinary {
    
    public NumericRelation(Expr left, Expr right) {
        super(left, right);
    }
    
    public Type getOperandType() {
        return null; // doesn't matter since we override getType()
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!getLeft().getType().equals(Type.N) || !getRight().getType().equals(Type.N))
            throw new TypeMismatchException("The types of the expressions on the left and right of a numeric relation connective like '" + getSymbol() + "' must be type i, but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }

    NumericRelation(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

    public static class LessThan extends NumericRelation {
        public static final char SYMBOL = '<';
        public static final String LATEX_REPR = "<";
        public LessThan(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Binary create(Expr left, Expr right) { return new LessThan(left, right); }
        LessThan(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class LessThanOrEqual extends NumericRelation {
        public static final char SYMBOL = '\u2264';
        public static final String LATEX_REPR = "\\leq";
        public LessThanOrEqual(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Binary create(Expr left, Expr right) { return new LessThanOrEqual(left, right); }
        LessThanOrEqual(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class GreaterThan extends NumericRelation {
        public static final char SYMBOL = '>';
        public static final String LATEX_REPR = ">";
        public GreaterThan(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Binary create(Expr left, Expr right) { return new GreaterThan(left, right); }
        GreaterThan(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class GreaterThanOrEqual extends NumericRelation {
        public static final char SYMBOL = '\u2265';
        public static final String LATEX_REPR = "\\geq";
        public GreaterThanOrEqual(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Binary create(Expr left, Expr right) { return new GreaterThanOrEqual(left, right); }
        GreaterThanOrEqual(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
}
