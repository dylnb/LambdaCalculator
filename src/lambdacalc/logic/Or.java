/*
 * Or.java
 *
 * Created on May 29, 2006, 3:24 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the disjunction binary connective.
 */
public class Or extends LogicalBinary {
    /**
     * The unicode upside-down wedge character.
     */
    public static final char SYMBOL = '\u2228';
    
    public static final char INPUT_SYMBOL = 'V';
    
    public static final int KEY_EVENT = KeyEvent.VK_V;
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public Or(Expr left, Expr right) {
        super(left, right);
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public Type getOperandType() {
        return Type.T;
    }

    protected Binary create(Expr left, Expr right) {
        return new Or(left, right);
    }

    Or(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
