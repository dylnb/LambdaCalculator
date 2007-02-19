/*
 * And.java
 *
 * Created on May 29, 2006, 3:22 PM
 */

package lambdacalc.logic;

/**
 * Represents the conjunction binary connective.
 */
public class And extends LogicalBinary {
    
    /**
     * The wedge character
     */
    public static final char SYMBOL = '\u2227';
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public And(Expr left, Expr right) {
       super(left,right);
    }    
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 5;
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }
    
    protected Binary create(Expr left, Expr right) {
        return new And(left, right);
    }
}
