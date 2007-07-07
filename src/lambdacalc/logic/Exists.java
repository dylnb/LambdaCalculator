/*
 * Exists.java
 *
 * Created on May 29, 2006, 3:35 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * The existential quantifier.
 */
public class Exists extends PropositionalBinder {
    /**
     * The backwards E character.
     */
    public static final char SYMBOL = '\u2203';
    
    public static final char INPUT_SYMBOL = 'E';
    
    public static final int KEY_EVENT = KeyEvent.VK_E;
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Exists(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }

    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new Exists(variable, inner, hasPeriod());
    }
    
    public boolean dotPolicy() {
        return false;
    }
    
    Exists(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
