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
    
    protected String toString(boolean html) {
        return SYMBOL + getInnerExpr().toString(html) + SYMBOL;
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!(getInnerExpr().getType() instanceof CompositeType) || !((CompositeType)getInnerExpr().getType()).getRight().equals(Type.T))
            throw new TypeMismatchException("The cardinality operator can only be applied to something that has the type of a set, i.e. the type of the characteristic function of a set, such as " + Type.ET + ", but " + getInnerExpr() + " is of type " + getInnerExpr().getType() + ".");
        return Type.I;
    }    

    protected Unary create(Expr inner) {
        return new Cardinality(inner);
    }

    Cardinality(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
