/*
 * LogicalBinary.java
 *
 * Created on June 1, 2006, 2:48 PM
 */

package lambdacalc.logic;

/**
 * Abstract base class of the logical binary connectives
 * (and, or, if, iff).
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
    
    public String toString() {
        return partToString(getLeft()) + " " + getSymbol() + " " + partToString(getRight());
    }
    
    private String partToString(Expr expr) {
        // And and Or are associative, so we omit parens for nested Ands within Ands and Ors within Ors.
        if (((this instanceof And || this instanceof Or)) && expr.getClass() == getClass()) return expr.toString();
        return nestedToString(expr);
    }
    
    /**
     * Gets the unicode symbol associated with the binary connective.
     */
    public abstract String getSymbol();

    public Type getType() throws TypeEvaluationException {
        if (!getLeft().getType().equals(Type.T))
            throw new TypeMismatchException("The parts of a logical connective must be of type t, but " + getLeft() + " is of type " + getLeft().getType() + ".");
        if (!getRight().getType().equals(Type.T))
            throw new TypeMismatchException("The parts of a logical connective must be of type t, but " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }
}
