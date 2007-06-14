/*
 * Not.java
 *
 * Created on May 29, 2006, 3:51 PM
 */

package lambdacalc.logic;

/**
 * Represents the negation unary operator.
 */
public class Not extends Unary {
    /**
     * The unicode negation symbol.
     */
    public static final char SYMBOL = '\u00AC';
    
    /**
     * Constructs negation around the given expression.
     */
    public Not(Expr expr) {
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
    
    protected String toString(boolean html) {
        // As a special case, we don't need to put parens around binders since it's unambiguous.
        if (getInnerExpr() instanceof Binder)
            return SYMBOL + getInnerExpr().toString(html);
        else
            return SYMBOL + nestedToString(getInnerExpr(), html);
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!getInnerExpr().getType().equals(Type.T))
            throw new TypeMismatchException("Negation can only be applied to something of type t, but " + getInnerExpr() + " is of type " + getInnerExpr().getType() + ".");
        return Type.T;
    }    

    protected Unary create(Expr inner) {
        return new Not(inner);
    }

    Not(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
