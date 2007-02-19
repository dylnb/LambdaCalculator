/*
 * Iff.java
 *
 * Created on May 29, 2006, 3:41 PM
 */

package lambdacalc.logic;

/**
 * Represents the biconditional binary connective.
 */
public class Iff extends LogicalBinary {
    /**
     * The unicode double arrow.
     */
    public static final char SYMBOL = '\u2194';
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public Iff(Expr left, Expr right) {
        super(left,right);
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
        return new Iff(left, right);
    }
}
