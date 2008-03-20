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
        if (!getLeft().getType().equals(Type.I) || !getRight().getType().equals(Type.I))
            throw new TypeMismatchException("The types of the expressions on the left and right of a numeric relation connective like '" + getSymbol() + "' must be type i, but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }

    NumericRelation(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

    public static class LessThan extends NumericRelation {
        public static final char SYMBOL = '<';
        public LessThan(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new LessThan(left, right); }
        LessThan(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class LessThanOrEqual extends NumericRelation {
        public static final char SYMBOL = '\u2264';
        public LessThanOrEqual(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new LessThanOrEqual(left, right); }
        LessThanOrEqual(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class GreaterThan extends NumericRelation {
        public static final char SYMBOL = '>';
        public GreaterThan(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new GreaterThan(left, right); }
        GreaterThan(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class GreaterThanOrEqual extends NumericRelation {
        public static final char SYMBOL = '\u2265';
        public GreaterThanOrEqual(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new GreaterThanOrEqual(left, right); }
        GreaterThanOrEqual(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
}
