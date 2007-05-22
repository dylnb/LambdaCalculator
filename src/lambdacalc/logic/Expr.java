/*
 * Expr.java
 *
 * Created on May 29, 2006, 3:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An expression in the Lambda calculus.  This is the abstract
 * base class of all expression subclasses.
 */
public abstract class Expr implements java.io.Serializable {
    
    /**
     * Gets an integer representing the expression's operator precedence:
     *
     *   8   FunApp over Lambda Expression
     *   7   Lambda
     *   6   If, Iff
     *   5   And, Or
     *   4   PropositionalBinder (ForAll, Exists, but not Lambda because they usually have brackets.)
     *   3   Not
     *   2   FunApp over Predicate
     *   1   Identifier
     *   0   Parens, ArgList (because ArgList is always parenthesized)
     *
     */
    public abstract int getOperatorPrecedence();
    
    /**
     * This returns to toString() on nestedExpr, except when nestedExpr
     * has a higher or equal operator precedence, it is wrapped with parens.
     */
    protected String nestedToString(Expr nestedExpr) {
        if (nestedExpr.getOperatorPrecedence() >= this.getOperatorPrecedence())
            return "(" + nestedExpr.toString() + ")";
        return nestedExpr.toString();
    }
    
    /**
     * Gets the semantic type of the expression, or throws a
     * TypeEvaluationException if there is a type mismatch.
     */
    public abstract Type getType() throws TypeEvaluationException;
    
    /**
     * Tests if two expressions are equal, up to parens. Two expressions 
     * which differ only in a bound variable are already considered unequal.
     * Thus, Lx.P(x) equals Lx.[P(x)], but does not equal Ly.P(y).
     *
     * @param obj the other expression to compare
     * @return true if the types are equivalent
     */
    public boolean equals(Object obj) {
        if (obj instanceof Expr)
            // call equals and specify not to collapse bound variables
            // (useMap=false)
            return equals((Expr)obj, false, null, null, false); // null maps
        else
            return false;
    }
    
    /**
     * Tests if two expressions are equal, modulo parens and the consistent
     * renaming of bound variables.
     *
     * @param obj the other expression to compare
     * @return true if the types are equivalent
     */
    public boolean alphaEquivalent(Expr obj) {
        // call equals and specify to collapse bound variables
        // (useMap=true)
        return equals(obj, true, null, null, false); // null maps
    }

    /**
     * Tests if two expressions are equal, modulo parens and the identity
     * of identifiers.  That is, any identifier matches any other
     * identifier.
     *
     * @param obj the other expression to compare
     * @return true if the types are equivalent
     */
    public boolean operatorEquivalent(Expr obj) {
        return equals(obj, false, null, null, true);
    }

    /**
     * For the purposes of equality tests, (Expr) is equal to Expr. 
     * That is, Parens are equal to the Expr they contain.
     * This method is overwritten in the Parens method and only there.
     */
    public Expr stripAnyParens() {
        return this;
    }
    
    /**
     * Implemented by subclasses to test for equivalence of
     * two expressions. Note that the two expressions may be created from strings that are
     * not completely equal, because some normalization has already happened in the parsing.
     * Specifically, non-meaningful whitespaces get collapsed, and periods after binders get normalized.
     * In the following, equivalence "to the letter" will always mean equivalence after this normalization step,
     * because we are not comparing strings but parsed expressions.
     * 
     * In the simplest case, collapseBoundVars is false, thisMap and otherMap are null, and 
     * collapseAllVars is false. This setting checks for equivalence to the letter modulo parentheses.
     * 
     * In the case where collapseBoundVars is true, and the other params are as above, we test for
     * alpha-equivalence, that is equivalence to the letter modulo parentheses and consistent renaming
     * of bound variables.
     *
     * By convention, the map variables are always set to null when an external caller calls this function. They
     * are only used for recursion.
     *
     * Finally, if collapseAllVars is set to true, then the method will regard two expressions as equal even
     * if they differ in some variable (free or bound). For example, the following expressions will all be equal:
     *
     * Lx.R(x,y)
     * Ly.R(y,z)
     * Ly.R(z,x)
     *
     * @param e the other expression to compare
     *
     * @param collapseBoundVars If set to true, then bound variables with different names but in structurally
     * equivalent positions are collapsed (that is, they are considered equal).
     * Example: Lx.x is equal to Ly.y iff this parameter is true.
     * Mappings can be provided from higher calls using the two Map parameters.
     * 
     * @param collapseAllVars see above.
     *
     * @param thisMap A map from variables to fresh variables, to be applied on this expression iff 
     * the boolean parameter is set to true.
     *
     * @param otherMap A map from variables to fresh variables, to be applied on the other expression
     * iff the boolean parameter is set to false.
     *
     *
     * @return true iff both expressions are equal, abstracting over parens, and possibly
     * abstracting over bound variables (depending on the parameters)
     */
    
    protected abstract boolean equals(Expr e, boolean collapseBoundVars, Map thisMap, Map otherMap, boolean collapseAllVars);
    // IMPLEMENTATION NOTES -- TODO cleanup
    /*    
     * , where variables in this and the other expression
     * are mapped to other fresh objects according to the map (= assignment function)
     * parameters (each of which may be null to indicate no mapping
     * has been set yet). 
     * 
     * 
     * A binder (lambda, for all, exists)
     * adds a mapping (for the duration of evaluating its sub-parse-tree)
     * from its variable and the corresponding variable in the other binder
     * to a new fresh variable represented by "new Object()". The Var
     * class overrides equals in the following way: If the variable is
     * not mapped, then it is free, and so it compares itself with the
     * corresponding variable on the other side by comparing the symbols.
     * But if a mapping is present, the variable is bound, and it consults
     * the mapping to make sure that the variables on both sides map to the
     * same Object marker -- although the variables themselves may be different.
     */
    
    /**
     * Gets a set of all of the variables used within this expression.
     */
    public Set getAllVars() {
        return getVars(false);
    }
    
    /**
     * Gets a set of all of the free (unbound) variables used within this expression.
     */
    public Set getFreeVars() {
        return getVars(true);
    }

    /**
     * Returns the variables used in the expression.
     * @param unboundOnly true if only the free variables should be returned
     */
    protected abstract Set getVars(boolean unboundOnly);

    /**
     * Creates a fresh variable based on the given variable and the 
     * set of variables in use.  The new variable has the same
     * type and prefix as the given variable, but with as many
     * prime characters appended as needed until it does not appear
     * in variablesInUse.
     * @param v a variable to base the new variable on
     * @param variablesInUse a set of all variables in use
     */
    public static Var createFreshVar(Var v, Set variablesInUse) {
        while (variablesInUse.contains(v))
            v = new Var(v.getSymbol() + Identifier.PRIME, v.getType());
        return v;
    }

    /**
     * Substitutes free occurrences of a variable with another expression,
     * provided the substitution does not cause what was a free variable
     * in the replacement to be incorrectly/"accidentally" bound by
     * a binder taking scope over an occurance of the variable.  When
     * this happens, an alphabetical variant is needed in order to
     * perform the replacement, and null is returned.
     * @param var the variable to be replaced
     * @param replacement the expression to replace var with
     * @return the expression with free occurrences of var
     * replaced by replacement, or null if the substitution could not be
     * completed because an alphabetical variant should be created.
     */
    public Expr substitute(Var var, Expr replacement) {
        Set unboundVars = replacement.getVars(true);
        Set pab = new HashSet();
        Set ab = new HashSet();
        Expr result = substitute(var, replacement, unboundVars, pab, ab);
        if (ab.size() > 0)
            return null; // an alphabetical variant is needed
        else
            return result; // substitution was possible
    }
    
    /**
     * Substitutes all free occurrences of a variable with another expression,
     * not checking if any free variables in replacement would be accidentally
     * captured by a binder taking scope over a replacement.
     * @param var the variable to be replaced
     * @param replacement the expression to replace var with
     * @return the expression with free occurrences of var
     * replaced by replacement
     */
    public Expr substituteAll(Var var, Expr replacement) {
        return substitute(var, replacement, new HashSet(), new HashSet(), null);
    }
    
    /**
     * Returns whether there are any lambda conversions to simplify() in the expression.
     * Simplification may still fail because of a type mismatch.
     */
    public abstract boolean canSimplify();
    
    /**
     * Returns whether an alphabetical variant is needed in order
     * to simplify this expression.  See FunApp.needsAlphabeticalVariant.
     */
    public abstract boolean needsAlphabeticalVariant() throws TypeEvaluationException;
    
    /**
     * Creates the alphabetical variant needed to simplify the expression.
     * If no alphabetical variant is needed, returns itself unchanged.
     * True is returned only if the very next simplification performed
     * by simplify() needs it, not any future simplifications.
     */
    public abstract Expr createAlphabeticalVariant() throws TypeEvaluationException;

    /**
     * Simplifies the expression by performing at most one lambda conversion
     * and returns the new Expr. For nested function applications, we do
     * the innermost (leftmost) first, but if FunApps are embedded in the
     * expression somewhere, we evaluate the first we find in a top-down,
     * left-to-right search. If an alphabetical variant is needed, one is
     * created. If a type incompatibility is found while
     * lambda-converting, a TypeEvaluationException is thrown.
     */
    public abstract Expr simplify() throws TypeEvaluationException;

    // var and replacement are not the variable being altered by 
    // createAlphabeticalVariant, but rather the variable that
    // wants to be replaced by replacement but can't because of
    // 'accidental' binding.  Only call this method when accidental
    // binding makes creating an alphabetical variant necessary.
    // (Actually, this method is called from FunApp::createAlphabeticalVariant,
    // and shouldn't need to be called elsewhere.)
    Expr createAlphabeticalVariant(Var var, Expr replacement) {
        Set unboundVars = replacement.getVars(true);
        Set pab = new HashSet();
        Set ab = new HashSet();
        substitute(var, replacement, unboundVars, pab, ab);
        
        if (ab.size() == 0)
            return this; // no alphabetical variant is needed
        
        // The Binder objects in ab will 'accidentally' bind free variables
        // upon substitution.  Thus, an alphabetical variant must be
        // created by replacing the variable of each of these binders
        // with a fresh variable.
        return createAlphabeticalVariant(ab, getAllVars(), new HashMap());
    }
    
    // This method performs a replacement of free instances of var by replacement,
    // while checking that in doing so, no free variables in replacement get
    // accidentally bound by binders scoping over it.  The method is called
    // recursively down the parse tree, but stops at binders whose variable
    // is var, since that blocks further replacement.
    //
    // The free variables in replacement are expected to be
    // already put into the unboundVars set argument.  (so that the set of free vars
    // doesn't need to be recomputed at every node in the parse tree)
    //
    // To check that no accidental (i.e. incorrect) binding occurs, when this
    // method is called on a binder whose variable is free
    // in replacement (it's in unboundVars) -- meaning that if var occurs
    // in the binder's scope, the binder would incorrectly capture one of those variables
    // when the replacement actually occurs
    // -- the binder pushes itself onto the potentialAccidentalBindings list
    // (for the sake of the decendents in the parse tree of this binder).
    // It will not cause accidental binding if var doesn't occur in the
    // binder's scope, which is why we put it in a 'potential' list.
    //
    // When this method is called on a variable equalling var, then the method
    // returns the replacement, thereby indicating to the caller that var should be
    // replaced by replacement.  (i.e. it's up to the caller to create a new
    // version of itself with var replaced by the return of the method).
    // But, if at this point there are binders in the potentialAccidentalBindings
    // list, then all of those binders are in fact going to capture free variables
    // in replacement, and so they are put into the accidentalBindings list,
    // and the substitution in the end fails (though we go on to get a complete
    // list of accidentalBinders).
    //
    // accidentalBindings is really an "out-parameter", meaning its purpose is to
    // receive the list of binders that surely will cause problems if not alphabetically
    // varied.  After the method returns, it contains those binders.
    protected abstract Expr substitute(Var var, Expr replacement, Set unboundVars, Set potentialAccidentalBindings, Set accidentalBindings);
    
    // This method creates an alphabetical variant by altering the variables used by
    // each of the binders in bindersToChange to a fresh variable.  Binders implement
    // this method, if they are in bindersToChange, by choosing a fresh variable based
    // on variablesInUse, adding that variable to variablesInUse when it passes it down,
    // and adding a mapping from the old variable to the new variable in updates, passing
    // that down as well.  Variables implement this method by replacing themselves with
    // another variable according to updates.
    protected abstract Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates);
}
