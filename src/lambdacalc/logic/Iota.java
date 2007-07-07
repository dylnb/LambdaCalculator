/*
 * Iota.java
 *
 * Created on May 29, 2006, 3:35 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

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
    public static final char SYMBOL = '\u03B9';  // small Greek iota
        // other characters, but missing from Times New Roman
        // the special curly, Math-style iota is \u2373
        // Latin small letter iota is \u0269
    
    public static final char INPUT_SYMBOL = 'I';
    
    public static final int KEY_EVENT = KeyEvent.VK_I;
    
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
            throw new TypeEvaluationException("The inside of the iota binder in " + toString() + " must be of type t.");
        return getVariable().getType();
    }    
    
    Iota(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
