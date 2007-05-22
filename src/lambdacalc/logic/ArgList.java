/*
 * ArgList.java
 *
 * Created on May 31, 2006, 4:14 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a list of two or more arguments to a predicate, i.e. a vector.
 * ArgLists are not used for the arguments of one-place
 * predicates, which are represented by Identifiers.  
 */
public class ArgList extends Expr {
    Expr[] exprs;
    
    /**
     * Constructs the ArgList.
     * @param innerExpressions an array of two or more expressions
     */
    public ArgList(Expr[] innerExpressions) {
        if (innerExpressions == null) throw new IllegalArgumentException("null argument");
        if (innerExpressions.length == 1) throw new IllegalArgumentException("ArgList must have more than one element.");
        exprs = innerExpressions;
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 0;
    }
    
    /**
     * Gets the elements of the ArgList.
     */
    public Expr[] getArgs() {
        return exprs;
    }
    
    /**
     * Gets the number of elements in the ArgList.
     */
    public int getArity() {
        return exprs.length;
    }
    
    public String toString() {
        String ret = null;
        for (int i = 0; i < exprs.length; i++) {
            if (ret == null) ret = "(";
            else ret += ",";
            ret += exprs[i].toString(); // note that we don't ever wrap it with parens because the comma separator here makes this unambiguous
        }
        ret += ")";
        return ret;
    }
    
    public Type getType() throws TypeEvaluationException {
        Type[] t = new Type[exprs.length];
        for (int i = 0; i < t.length; i++)
            t[i] = exprs[i].getType();
        return new ProductType(t);
    }

    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {

        // ignore parentheses for equality test
        e = e.stripAnyParens();

        if (e instanceof ArgList)
            return equals((ArgList)e, useMaps, thisMap, otherMap, collapseAllVars);
        else
            return false;
    }
    
    private boolean equals(ArgList a, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        if (a.exprs.length != exprs.length)
            return false;
        for (int i = 0; i < exprs.length; i++)
            if (!exprs[i].equals(a.exprs[i], useMaps, thisMap, otherMap, collapseAllVars))
                return false;
        return true;
    }
    
    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        for (int i = 0; i < exprs.length; i++)
            ret.addAll(exprs[i].getVars(unboundOnly));
        return ret;
    }

    protected Expr substitute(Var var, Expr replacement, Set unboundVars, Set potentialAccidentalBindings, Set accidentalBindings) {
        Expr[] e = new Expr[exprs.length];
        for (int i = 0; i < exprs.length; i++)
            e[i] = exprs[i].substitute(var, replacement, unboundVars, potentialAccidentalBindings, accidentalBindings);
        return new ArgList(e);
    }
 
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        Expr[] e = new Expr[exprs.length];
        for (int i = 0; i < exprs.length; i++)
            e[i] = exprs[i].createAlphabeticalVariant(bindersToChange, variablesInUse, updates);
        return new ArgList(e);
    }

    public boolean canSimplify() {
        for (int i = 0; i < exprs.length; i++)
            if (exprs[i].canSimplify())
                return true;
        return false;
    }

    public boolean needsAlphabeticalVariant() throws TypeEvaluationException  {
        for (int i = 0; i < exprs.length; i++)
            if (exprs[i].canSimplify())
                return exprs[i].needsAlphabeticalVariant();
        return false;
    }

    public Expr createAlphabeticalVariant() throws TypeEvaluationException  {
        // We may only perform a single simplification, so we
        // have to do it only on one sub-expr. Note that we also
        Expr[] e = new Expr[exprs.length];
        boolean didSimp = false;
        for (int i = 0; i < exprs.length; i++) {
            if (!didSimp && exprs[i].canSimplify()) {
                e[i] = exprs[i].createAlphabeticalVariant();
                didSimp = true;
            } else {
                e[i] = exprs[i];
            }
        }
        return new ArgList(e);
    }

    public Expr simplify() throws TypeEvaluationException {
        // We may only perform a single simplification, so we
        // have to do it only on one sub-expr.
        Expr[] e = new Expr[exprs.length];
        boolean didSimp = false;
        for (int i = 0; i < exprs.length; i++) {
            if (!didSimp && exprs[i].canSimplify()) {
                e[i] = exprs[i].simplify();
                didSimp = true;
            } else {
                e[i] = exprs[i];
            }
        }
        return new ArgList(e);
    }

}
