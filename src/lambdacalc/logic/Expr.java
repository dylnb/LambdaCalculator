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
import java.util.IdentityHashMap;
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
    protected final String nestedToString(Expr nestedExpr) {
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
    public final boolean equals(Object obj) {
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
    public final boolean alphaEquivalent(Expr obj) {
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
    public final boolean operatorEquivalent(Expr obj) {
        return equals(obj, false, null, null, true);
    }

    /**
     * For the purposes of equality tests, (Expr) is equal to Expr. 
     * That is, Parens are equal to the Expr they contain.
     * This method is overwritten in the Parens method and only there.
     */
    public final Expr stripAnyParens() {
        if (this instanceof Parens)
            return ((Parens)this).getInnerExpr().stripAnyParens();
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
    public final Set getAllVars() {
        return getVars(false);
    }
    
    /**
     * Gets a set of all of the free (unbound) variables used within this expression.
     */
    public final Set getFreeVars() {
        return getVars(true);
    }

    /**
     * Returns the variables used in the expression.
     * @param unboundOnly true if only the free variables should be returned
     */
    protected abstract Set getVars(boolean unboundOnly);
    
    public class LambdaConversionResult {
        public final Expr Result;
        public final Expr AlphabeticalVariant;
        public final Expr SubstitutionWithoutAlphabeticalVariant;
        
        public LambdaConversionResult(Expr r, Expr a, Expr s) {
            Result = r;
            AlphabeticalVariant = a;
            SubstitutionWithoutAlphabeticalVariant = s;
        }
    }
    
    /**
     * Performs one application of lambda conversion if possible. If an
     * alphabetical variant is necessary, one is created. If no lambda
     * conversions are possible, it returns null.
     * The first lambda-within-a-FunApp found in a top-down
     * left-to-right search is simplified.
     */
    public final LambdaConversionResult performLambdaConversion() throws TypeEvaluationException {
        Set binders = new HashSet();
        Set accidentalBinders = new HashSet();
        Expr result = performLambdaConversion1(binders, accidentalBinders);
        
        // Check if any lambda conversion took place.
        if (result == null)
            return null;
                
        // If no accidental binding ocurred, we're set -- return the new expr.
        if (accidentalBinders.size() == 0)
            return new LambdaConversionResult(result, null, null); // substitution was possible
        
        Expr originalResult = result;
        
        // We need to make an alphabetical variant by fixing the binders in the
        // accidentalBinders set.
        Set varsInUse = result.getAllVars();
        Map varMap = new HashMap();
        Expr alphaVary = createAlphabeticalVariant(accidentalBinders, varsInUse, varMap);
        
        // Now try to simplify this.
        accidentalBinders = new HashSet();
        result = alphaVary.performLambdaConversion1(binders, accidentalBinders);
        if (accidentalBinders.size() != 0)
            throw new RuntimeException("Internal error: An alphabetical variant was still needed after creating one: " + alphaVary.toString());
         
        return new LambdaConversionResult(result, alphaVary, originalResult);
    }
    
    /**
     * Helper method for performLambdaConversion. This method is called recursively
     * down the tree to perform lambda conversion, looking for the lambda to convert.
     * The lambda that we are converting may not be at top scope
     * (e.g. Ax[ Ly.P(y) (x) ].
     * Once we find the lambda we are converting, performLambdaConversion2 takes
     * over and goes down the rest of the subtree.
     *
     * We have to track which binders have scope over this subexpression as we go
     * down the tree because of accidental binding, i.e. when a free variable in
     * the replacement would get accidentally bound when it is put into the main
     * expression. This occurs in: LxAy[P(x)] (y)
     * The binders that scope over this expression are in the 'binders' parameter.
     * The caller sets that.
     * 
     * Once we are in the scope of the lambda being converted and we start performing
     * substitutions we have to track which of the binders that scope over us
     * cause an accidental binding of a formerly free variable in the replacement
     * expression.
     * Those binders that cause accidental binding are put into the 'accidentalBinders'
     * parameter, which is filled in by the *callee*. It is an out-parameter of sorts.
     * 
     * This method only performs a single lambda conversion, so we have to be
     * careful that in n-ary operators, if a lambda conversion
     * ocurrs within one operand, we must not do any conversions in the other operands.
     *
     * This method returns false to signify that no conversion took place.
     *
     * @param binders the binders that have scope over this expression
     * @param accidentalBinders as we perform substitution, we record here
     * those binders whose variables must be modified so that they don't accidentally
     * capture free variable in the replacement
     * @return null if no lambda conversion took place, otherwise the lambda-converted
     * expression 
     */
    protected abstract Expr performLambdaConversion1(Set binders, Set accidentalBinders) throws TypeEvaluationException;
       
    /**
     * Helper method for performLambdaConversion. This method is called by
     * performLambdaConversion1 once we enter into the scope of the lambda that
     * we are converting.
     *
     * 'var' is set to its bound variable. If we get to a binder that binds the
     * same variable, we know that nothing will happen in that scope, and the Expr
     * is returned immediately.
     * 
     * If we arrive at var itself, we check if any free
     * variables in replacement would be captured by any outscope binders, and if
     * so we add those binders to the accidentalBinders set. But we proceed
     * with the substitution anyway, and return 'replacement'.
     *
     * This method only performs a single lambda conversion, so we have to be
     * note that if we hit another lambda expression, we aren't supposed to be
     * lambda-converting it. We just treat it like any other binder.
     *
     * @param var the variable to replace with 'replacement' when we find it, or
     * null if we haven't yet found the lambda being converted
     * @param replacement the expression that replaces var
     * @param binders the binders that have scope over this expression
     * @param accidentalBinders as we perform substitution, we record here
     * those binders whose variables must be modified so that they don't accidentally
     * capture free variable in the replacement
     * @return the expression with substitutions performed
     */
    protected abstract Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException;
    
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

    // This method creates an alphabetical variant by altering the variables used by
    // each of the binders in bindersToChange to a fresh variable.  Binders implement
    // this method, if they are in bindersToChange, by choosing a fresh variable based
    // on variablesInUse, adding that variable to variablesInUse when it passes it down,
    // and adding a mapping from the old variable to the new variable in updates, passing
    // that down as well.  Variables implement this method by replacing themselves with
    // another variable according to updates.
    protected abstract Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates);
}
