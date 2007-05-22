/*
 * Parens.java
 *
 * Created on May 29, 2006, 3:52 PM
 */

package lambdacalc.logic;

import java.util.Map;

/**
 * Represents parenthesis, a unary operator.
 */
public class Parens extends Unary {
    
    /**
     * The constant for round parenthesis ( ).
     */
    public static final boolean ROUND = true;
    
    /**
     * The constant for square parenthesis [ ].
     */
    public static final boolean SQUARE = false;
    
    private Expr innerExpr;
    private boolean shape;
    
    /**
     * Constructs a parenthesis expression around then given
     * expression, with either ROUND or SQUARE parens.
     */
    public Parens(Expr innerExpr, boolean shape) {
        super(innerExpr);
        this.shape=shape;
        
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 0;
    }
    
    public String toString() {
        return getOpenSymbol() + getInnerExpr().toString() + getCloseSymbol();
    }
    
    String getOpenSymbol() { return shape == ROUND ? "(" : "["; }
    String getCloseSymbol() { return shape == ROUND ? ")" : "]"; }

    public Type getType() throws TypeEvaluationException {
        return getInnerExpr().getType();
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        
        // ignore parentheses for equality test
        // this line needs to be added to every equals method in every subclass of Expr
        e = e.stripAnyParens();

        return this.stripAnyParens().equals(e, useMaps, thisMap, otherMap, collapseAllVars);
    }
        
    protected Unary create(Expr inner) {
        return new Parens(inner, shape);
    }
}
