/*
 * Iota.java
 *
 * Created on May 29, 2006, 3:35 PM
 */

package lambdacalc.logic;

/**
 * The iota operator.
 * This operator is like the other operators, but its semantic type
 * is the same as the type of the variable it binds, so it is not a subclass
 * of PropositionalBinder.
 */
public class Iota extends Binder {
    /**
     * The curly iota operator symbol.
     */
    public static final char SYMBOL = '\u2373';  // \u0269 would be a regular lower case iota
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Iota(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }

    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new Iota(variable, inner, hasPeriod());
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 4;
    }
    
    public Type getType() throws TypeEvaluationException {
        checkVariable();
        return getVariable().getType();
    }    
    
}
