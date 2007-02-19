/*
 * If.java
 *
 * Created on May 29, 2006, 3:39 PM
 */

package lambdacalc.logic;

/**
 * Represents the material implication binary connective.
 */
public class If extends LogicalBinary {
    /**
     * The unicode right arrow.
     */
    public static final char SYMBOL = '\u2192';

    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public If(Expr left, Expr right) {
        super(left, right);
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 6;
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    protected Binary create(Expr left, Expr right) {
        return new If(left, right);
    }
}
