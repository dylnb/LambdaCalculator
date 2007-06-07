/*
 * Lambda.java
 *
 * Created on May 29, 2006, 3:34 PM
 */

package lambdacalc.logic;

/**
 * Represents the lambda binder.
 */
public class Lambda extends Binder {
    /**
     * The unicode Greek letter lambda.
     */
    public static final char SYMBOL = '\u03BB';
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Lambda(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident,innerExpr,hasPeriod);
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 4;
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public Type getType() throws TypeEvaluationException {
        checkVariable();
        return new CompositeType(getVariable().getType(), getInnerExpr().getType());
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new Lambda(variable, inner, hasPeriod());
    }
    
    Lambda(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
