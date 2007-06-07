/*
 * LogicalBinary.java
 *
 * Created on June 1, 2006, 2:48 PM
 */

package lambdacalc.logic;

import java.util.Set;

/**
 * Abstract base class of the logical binary connectives
 * (and, or, if, iff, equality).
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

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public final int getOperatorPrecedence() {
        return 5;
    }
    
    public Type getType() throws TypeEvaluationException {
        // Our default implementation checks that the operands are of type t,
        // but this is overridden in Equality which only checks that the
        // types of the operands are the same.
        if (!getLeft().getType().equals(Type.T))
            throw new TypeMismatchException("The parts of a logical connective must be of type t, but " + getLeft() + " is of type " + getLeft().getType() + ".");
        if (!getRight().getType().equals(Type.T))
            throw new TypeMismatchException("The parts of a logical connective must be of type t, but " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }
    
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda to convert. If we can do a conversion on the left,
        // don't do a conversion on the right!
        Expr a = getLeft().performLambdaConversion1(accidentalBinders);
        if (a != null)
            return create(a, getRight());
        
        Expr b = getRight().performLambdaConversion1(accidentalBinders);
        if (b != null)
            return create(getLeft(), b);
        
        return null;
    }    

    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're in the scope of a lambda conversion. Just recurse.
        return create(getLeft().performLambdaConversion2(var, replacement, binders, accidentalBinders),
                getRight().performLambdaConversion2(var, replacement, binders, accidentalBinders));
    }

    LogicalBinary(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
