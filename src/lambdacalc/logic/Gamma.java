/*
 * Gamma.java
 *
 * Created on Nov 20, 2014
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * The Gamma operator.
 * This operator is like the other operators, but its semantic type
 * is the same as the type of the variable it binds, so it is not a subclass
 * of PropositionalBinder.
 */

public class Gamma extends Binder {
    /**
     * The curly gamma operator symbol.
     */
    public static final char SYMBOL = '\u03B3';  // small Greek gamma
        // other characters, but missing from Times New Roman
        // the special curly, Math-style gamma is \u03B3
        // Latin small letter gamma is \u03B3
    
    public static final char INPUT_SYMBOL = 'G';

    public static final String LATEX_REPR = "\\gamma";
    
    public static final int KEY_EVENT = KeyEvent.VK_G;
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Gamma(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }

    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public String getLatexSymbol() {
        return this.LATEX_REPR;
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new Gamma(variable, inner, hasPeriod());
    }
    
    public boolean dotPolicy() {
        return false;
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
        if (!getInnerExpr().getType().equals(Type.T))
            throw new TypeEvaluationException("The inside of the gamma binder in " + toString() + " must be of type t.");
        return getVariable().getType();
    }
    
    Gamma(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}