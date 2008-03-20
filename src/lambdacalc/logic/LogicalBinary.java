/*
 * LogicalBinary.java
 *
 * Created on June 1, 2006, 2:48 PM
 */

package lambdacalc.logic;

import java.util.List;
import java.util.Set;
import java.util.Vector;

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
    
    protected String toString(boolean html) {
        return partToString(getLeft(), html) + " " + getSymbol() + " " + partToString(getRight(), html);
    }
    
    private String partToString(Expr expr, boolean html) {
        // And and Or are associative, so we omit parens for nested Ands within Ands and Ors within Ors.
        if (((this instanceof And || this instanceof Or)) && expr.getClass() == getClass()) return expr.toString(html);
        return nestedToString(expr, html);
    }
    
    /**
     * Gets the unicode symbol associated with the binary connective.
     */
    public abstract String getSymbol();

    /**
     * Gets the type that the operands must be. Not called if getType() is overridden.
     */
    public abstract Type getOperandType();
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public final int getOperatorPrecedence() {
        return 4;
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
