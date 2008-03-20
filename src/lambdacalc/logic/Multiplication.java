/*
 * Multiplication.java
 *
 * Created on March 20, 2008, 7:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

/**
 * Represents numeric multiplication, as in the denotation of the 'most'
 * generalized quantifier.
 */
public class Multiplication extends Binary {
    public static final char SYMBOL = '\u22C5'; // dot
    public static final char INPUT_SYMBOL = '*'; // asterisk
    
    public Multiplication(Expr left, Expr right) {
        super(left, right);
    }
    
    protected String toString(boolean html) {
        return getLeft().toString(html) + SYMBOL + getRight().toString(html);
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public final int getOperatorPrecedence() {
        return 3;
    }
    
    protected Binary create(Expr left, Expr right) {
        return new Multiplication(left, right);
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!getLeft().getType().equals(Type.I) || !getRight().getType().equals(Type.I))
            throw new TypeMismatchException("The types of the expressions on the left and right of the mulitiplication operator must be type i, but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.I;
    }
    
    Multiplication(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
