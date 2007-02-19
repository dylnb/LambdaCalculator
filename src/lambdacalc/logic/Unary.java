/*
 * Unary.java
 *
 * Created on May 29, 2006, 3:20 PM
 */

package lambdacalc.logic;

import java.util.Map;
import java.util.Set;

/**
 * Abstract base class of unary operators, including
 * negation and parenthesis.
 */
public abstract class Unary extends Expr {
    
    Expr innerExpr;
    
    /**
     * Creates a new unary expression around the given expression.
     */
    public Unary(Expr innerExpr) {
        this.innerExpr=innerExpr;
        if (innerExpr == null) throw new IllegalArgumentException();

    }
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of unary operator, with the given inner expression.
     */
    protected abstract Unary create(Expr innerExpr);
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        
        // ignore parentheses for equality test
        e = e.stripAnyParens();

        if (e instanceof Unary) {
            return this.equals((Unary) e, useMaps, thisMap, otherMap, collapseAllVars);
        } else {
            return false;
        }
    }
    
    private boolean equals(Unary u, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        return this.getClass() == u.getClass() 
            && this.getInnerExpr().equals(u.getInnerExpr(), useMaps, thisMap, otherMap, collapseAllVars);
    }
    
    public Expr getInnerExpr() {
        return innerExpr;
    }

    protected Set getVars(boolean unboundOnly) {
        return getInnerExpr().getVars(unboundOnly);
    }

    protected Expr substitute(Var var, Expr replacement, Set unboundVars, Set potentialAccidentalBindings, java.util.Set accidentalBindings) {
        return create(getInnerExpr().substitute(var, replacement, unboundVars, potentialAccidentalBindings, accidentalBindings));
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return create(getInnerExpr().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
}
