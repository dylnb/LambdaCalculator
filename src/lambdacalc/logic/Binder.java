/*
 * Binder.java
 *
 * Created on May 29, 2006, 3:25 PM
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class of the binders, including the propositional binders 
 * For All and Exists, the Iota operator, and Lambda.
 */
public abstract class Binder extends Expr {
    
    Identifier ident; 
    // ident = what the binder binds.
    // in "correct" lambda calculus this must be a variable.
    // but to capture student errors, we also allow it to be a constant.
    
    Expr innerExpr;
    boolean hasPeriod;

    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Binder(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        if (ident == null) throw new IllegalArgumentException();
        if (innerExpr == null) throw new IllegalArgumentException();
        
        this.ident=ident;
        this.innerExpr=innerExpr;
        this.hasPeriod = hasPeriod;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        
        // ignore parentheses for equality test
        e = e.stripAnyParens();

        if (e instanceof Binder) {
            return this.equals((Binder) e, useMaps, thisMap, otherMap, collapseAllVars);
        } else {
            return false;           
        }
    }
    
    private boolean equals(Binder b, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        if (useMaps) {
            thisMap = (thisMap == null) ? new HashMap() : new HashMap(thisMap);
            otherMap = (otherMap == null) ? new HashMap() :  new HashMap(otherMap);

            // this represents a new fresh variable that both sides'
            // variables are equated with
            Object freshObj = new Object();

            // map both sides' variables to the new fresh variable
            thisMap.put(this.getVariable(), freshObj);
            otherMap.put(b.getVariable(), freshObj);
        }
        
        return (this.getClass() == b.getClass()) // same type of binder; lambda, exists, all...
             && (useMaps || this.getVariable().equals(b.getVariable())) // if not using maps, then variables must match
             && this.getInnerExpr().equals(b.getInnerExpr(),
                    useMaps, thisMap, otherMap, collapseAllVars);
    }

    
    /**
     * Gets the unicode symbol associated with the binder.
     */
    public abstract String getSymbol(); // lambda, exists, forall
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of binder, with the given variable and inner expression,
     * and the same value as hasPeriod.
     */
    protected abstract Binder create(Identifier variable, Expr innerExpr);
    
    /**
     * Gets the variable bound by the identifier.
     */
    public Identifier getVariable() { // before the dot (if any)
        return ident;
    }

    /**
     * Gets the inside expression of the binder.
     */
    public Expr getInnerExpr() { // after the dot (if any)
        return innerExpr;
    }

    /**
     * Gets whether the string representation of the binder
     * includes a period after the identifier.
     */
    public boolean hasPeriod() {
        return hasPeriod;
    }
    
    protected Set getVars(boolean unboundOnly) {
        Set ret = getInnerExpr().getVars(unboundOnly);
        if (unboundOnly)
            ret.remove(getVariable());
        else
            ret.add(getVariable()); // in case it is vacuous
        return ret;
    }
    
    /**
     * A convenience method for derived classes that throws the
     * ConstInsteadOfVarException precisely when the variable
     * bound by this binder is given as a Const, rather than Var.
     */
    protected void checkVariable() throws ConstInsteadOfVarException {
        if (getVariable() instanceof Const)
            throw new ConstInsteadOfVarException("The symbols " + Lambda.SYMBOL + ", " + Exists.SYMBOL + ", and " + ForAll.SYMBOL + " must be followed by a variable, but '" + getVariable() + "' is a constant.");
    }

    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda to convert, but even if this is a lambda, we don't
        // do anything special here. That's handled in FunApp.
        
        Expr inside = getInnerExpr().performLambdaConversion1(accidentalBinders);
        
        if (inside == null) // nothing happened, return null
            return null;
        
        return create(getVariable(), inside);
    }
    
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        if (getVariable().equals(var)) return this; // no binding of var occurs within this scope
        
        // Mark that this binder outscopes things in its scope, so that when we
        // get to a replacement, we know what variables would be accidentally
        // bound.
        Set binders2 = new HashSet(binders);
        binders2.add(this);
        
        return create(getVariable(), getInnerExpr().performLambdaConversion2(var, replacement, binders2, accidentalBinders));
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        Identifier v = getVariable();

        if (bindersToChange.contains(this)) {
            // Choose a fresh variable
            if (v instanceof Const)
                v = new Var(v.getSymbol(), v.getType()); // very odd, but we need it to be a variable so that we can call createFreshVar
            v = createFreshVar((Var)v, variablesInUse);
            
            // Push the variable and mapping onto the stack
            variablesInUse = new HashSet(variablesInUse);
            variablesInUse.add(v);
            updates = new HashMap(updates);
            updates.put(getVariable(), v);
        }

        // Recurse
        return create(v, getInnerExpr().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
    
    public String toString() {
        String inner = innerExpr.toString();
        if (innerExpr.getOperatorPrecedence() > this.getOperatorPrecedence()) {
            inner = "[" + inner + "]";
        } else if (hasPeriod || ExpressionParser.isIdentifierChar(inner.charAt(0))) {
            inner = "." + inner;
        }
        return getSymbol() + ident.toString() + inner;
    }
    
}
