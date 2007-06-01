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
public abstract class Expr {
    
    /**
     * Gets an integer representing the (outermost) expression's operator precedence:
     *
     *   8   FunApp over Lambda Expression -- weakest
     *   7   Lambda
     *   6   If, Iff
     *   5   And, Or, Equality
     *   4   PropositionalBinder (ForAll, Exists), Iota, but not Lambda (because they usually have brackets.)
     *   3   Not
     *   2   FunApp over Predicate
     *   1   Identifier
     *   0   Parens, ArgList (because ArgList is always parenthesized) -- strongest
     * 
     * The parser implements this operator precedence independently so don't change it.
     * It is used only for the toString method.
     *
     * @return an integer which represents the expression's operator precedence
     */
    public abstract int getOperatorPrecedence();
    
    /**
     * This is a helper method for toString() implementations.
     * @returns the result of toString() on nestedExpr, except that when nestedExpr
     * has a higher or equal operator precedence than this, it is wrapped with parens.
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
     * @return true iff the expressions are equal up to parens
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
     * Thus, Lx.P(x) equals Lx.[P(x)] equals Ly.P(y).
     *
     * @param obj the other expression to compare
     * @return true iff the expressions are equivalent up to parens and bound variables
     */
    public final boolean alphaEquivalent(Expr obj) {
        // call equals and specify to collapse bound variables
        // (useMap=true)
        return equals(obj, true, null, null, false); // null maps
    }

    /**
     * Tests if two expressions are equal, modulo parens and the identity
     * of identifiers.  That is, any identifier matches any other
     * identifier. In other words, no distinction is made between free variables,
     * bound variables, and constants.
     * Thus, the following expressions are all equivalent:
     * Lx.P(x,y)
     * Lx.[P(x,y)]
     * Ly.P(y,y)
     * Ly.P(a,b)
     *
     * @param obj the other expression to compare
     * @return true iff the expressions are equivalent modulo parens and identifiers
     */
    public final boolean operatorEquivalent(Expr obj) {
        return equals(obj, false, null, null, true);
    }

    /**
     * This method returns this Expr with any parentheses removed.
     * This is used by equality tests, which all ignore parenthesis.
     * @return this expression, or the next-outermost non-paren expression if this expression is enclosed in parens
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
     * Gets the variables of this expression.
     * @return a set of all of the variables used within this expression
     */
    public final Set getAllVars() {
        return getVars(false);
    }
    
    /**
     * Gets the free variables of this expression. If this expression is a subexpression of a 
     * larger expression, then any variables that are not bound by any binders in this
     * subexpression are returned.
     * @returns a set of all of the free (unbound) variables within this expression
     */
    public final Set getFreeVars() {
        return getVars(true);
    }

    /**
     * Returns the variables used in the expression, possibly only the free ones.
     * @param unboundOnly true if only the free variables should be returned
     * @see getAllVars()
     * @see getFreeVars()
     * @return a set of either all variables or all free variables used in this expression
     */
    protected abstract Set getVars(boolean unboundOnly);
    
    /**
     * Holds the result of a lambda conversion operation. In addition to the converted expression
     * itself, this class records any alphabetical variants chosen during the conversion.
     * It also records the incorrect result we would have obtained if we hadn't performed any
     * alphabetical variants.
     */
    public class LambdaConversionResult {
        /**
         * The expression resulting from the lambda conversion.
         */
        public final Expr result;

        /**
         * If an alphabetical variant was needed to perform the lambda conversion, this
         * holds the alphabetical variant that was chosen before computing result.
         */
        public final Expr alphabeticalVariant;
 
        /**
         * If an alphabetical variant was needed, for pedagogical purposes this field holds
         * the result of performing the lambda conversion without first creating an
         * alphabetical variant.
         */
        public final Expr substitutionWithoutAlphabeticalVariant;
        
        /*
         * Creates a new instance of LambdaConversionResult.
         * @param r the result of the lambda conversion
         * @param a the alphabetical variant used during the conversion, or null if none was needed
         * @param the result of performing the conversion without creating an alphabetical variant,
         * or null if o variant was needed
         */
        public LambdaConversionResult(Expr r, Expr a, Expr s) {
            result = r;
            alphabeticalVariant = a;
            substitutionWithoutAlphabeticalVariant = s;
        }
    }
    
    /**
     * Performs one application of lambda conversion if possible. If an
     * alphabetical variant is necessary, one is created. If no lambda
     * conversions are possible, it returns null.
     * The first lambda-within-a-FunApp found in a top-down
     * left-to-right search is simplified.
     * @return null if no lambda conversion took place, otherwise the lambda-converted
     * expression 
     */
    public final LambdaConversionResult performLambdaConversion() throws TypeEvaluationException {
    	// performLambdaConversion1 carries out one beta reduction, irrespective
    	// of accidental binding that may occur. In that case, it does an incorrect
    	// conversion in the sense that free variables in the argument expression
    	// got 'accidentally' bound by a binder that outscoped some instance of
    	// the lambda variable being replaced.
    	// However, in the process it does check if accidental binding
    	// is ocurring, and it fills accidentalBinders in with all Binder
    	// instances that caused accidental binding in the incorrectly converted
    	// expression. If therse was no accidental binding, the set remains
    	// empty.
        Set accidentalBinders = new HashSet();
        Expr result = performLambdaConversion1(accidentalBinders);
        
        // Check if any lambda conversion took place.
        if (result == null)
            return null;
                
        // If no accidental binding ocurred, we're set -- return the new expr.
        if (accidentalBinders.size() == 0)
            return new LambdaConversionResult(result, null, null);
        
        Expr originalResult = result;
        
        // We need to make an alphabetical variant by fixing the binders in the
        // accidentalBinders set. We rename these binders' variables so they do
        // not bind anything accidentally after lambda conversion.
        Set varsInUse = result.getAllVars();
        Map varMap = new HashMap(); // scratch space for createAlphabeticalVariant
        Expr alphaVary = createAlphabeticalVariant(accidentalBinders, varsInUse, varMap);
        
        // Now try to lambda convert the alphabetical variant. Clear out
        // accidentalBinders and let it be filled in again. If it gets
        // filled in with anything, this program has made a mistake.
        accidentalBinders = new HashSet();
        result = alphaVary.performLambdaConversion1(accidentalBinders);
        if (accidentalBinders.size() != 0)
            throw new RuntimeException("Internal error: An alphabetical variant was still needed after creating one: " + alphaVary.toString());
         
        return new LambdaConversionResult(result, alphaVary, originalResult);
    }
    
    /**
     * Helper method for performLambdaConversion. This method is called recursively
     * down the tree to perform lambda conversion, looking for the lambda to convert.
     * The lambda that we are converting may not be at top scope
     * (it may be embedded as in e.g. Ax[ Ly.P(y) (x) ].)
     * Once we find the lambda we are converting, performLambdaConversion2 takes
     * over and goes down the rest of the subtree.
     *
     * Once we are in the scope of the lambda being converted and we start performing
     * substitutions we have to track which of the binders that scope over us
     * cause an accidental binding of a formerly free variable in the replacement
     * expression.
     * Those binders that cause accidental binding are recorded in the 'accidentalBinders'
     * parameter.
     * 
     * This method only performs a single lambda conversion, so we have to be
     * careful that in n-ary operators, if a lambda conversion
     * occurs within one operand, we must not do any conversions in the other operands.
     *
     * This method returns null to signify that no conversion took place.
     *
     * @param accidentalBinders as we perform substitution, we record here
     * those binders whose variables must be modified so that they don't accidentally
     * capture free variables in the replacement
     * @return null if no lambda conversion took place, otherwise the lambda-converted
     * expression 
     */
    protected abstract Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException;
       
    /**
     * Helper method for performLambdaConversion. This method is called by
     * performLambdaConversion1 once we enter into the scope of the lambda that
     * we are converting.
     *
     * We have to track which binders have scope over this subexpression as we go
     * down the tree because of accidental binding, i.e. when a free variable in
     * the replacement would get accidentally bound when it is put into the main
     * expression. This occurs in: LxAy[P(x)] (y)
     * The binders that scope over this expression are in the 'binders' parameter.
     * The caller sets that.
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
     * @throws TypeEvaluationException if a type inconsistency is found in any subexpression
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
     * @param return a fresh variable
     */
    public static Var createFreshVar(Var v, Set variablesInUse) {
        while (variablesInUse.contains(v))
            v = new Var(v.getSymbol() + Identifier.PRIME, v.getType(), v.isTypeExplicit());
        return v;
    }

   /**
    * This method creates an alphabetical variant by altering the variables used by
    * each of the binders in bindersToChange to a fresh variable.  Binders implement
    * this method by doing the following: If they are in bindersToChange, they choose a fresh variable based
    * on variablesInUse, add that variable to variablesInUse when they pass it down,
    * and add a mapping from the old variable to the new variable in updates, passing
    * that down as well.  Variables implement this method by replacing themselves with
    * another variable according to updates.
    * @param bindersToChange the set of binders whose variables are to be replaced with fresh ones
    * @param variablesInUse the variables which cannot be used as fresh variables
    * @param updates a replacement mapping from variables in use to fresh variables
    * @return an expression with alphabetical variant performed
    */
    protected abstract Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates);
    
    /**
     * Writes a serialization of the expression to a DataOutputStream.
     * In implementations of this method in subclasses, the first thing written
     * must be the name of the class as a string (i.e. "lambdacalc.logic.And").
     * @param output the data stream to which the expression is written
     */
    public abstract void writeToStream(java.io.DataOutputStream output) throws java.io.IOException;
   
    /**
     * Reads a serialized expression from a DataInputStream.
     * @param input the data source from which we read the expression instance
     * @return a deserialized expression
     */
    public static Expr readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        String exprType = input.readUTF();
        
        if (exprType.equals("lambdacalc.logic.And")) return new And(input);
        if (exprType.equals("lambdacalc.logic.ArgList")) return new ArgList(input);
        if (exprType.equals("lambdacalc.logic.Const")) return new Const(input);
        if (exprType.equals("lambdacalc.logic.Equality")) return new Equality(input);
        if (exprType.equals("lambdacalc.logic.Exists")) return new Exists(input);
        if (exprType.equals("lambdacalc.logic.ForAll")) return new ForAll(input);
        if (exprType.equals("lambdacalc.logic.FunApp")) return new FunApp(input);
        if (exprType.equals("lambdacalc.logic.If")) return new If(input);
        if (exprType.equals("lambdacalc.logic.Iff")) return new Iff(input);
        if (exprType.equals("lambdacalc.logic.Iota")) return new Iota(input);
        if (exprType.equals("lambdacalc.logic.Lambda")) return new Lambda(input);
        if (exprType.equals("lambdacalc.logic.Not")) return new Not(input);
        if (exprType.equals("lambdacalc.logic.Or")) return new Or(input);
        if (exprType.equals("lambdacalc.logic.Parens")) return new Parens(input);
        if (exprType.equals("lambdacalc.logic.Var")) return new Var(input);
        
        throw new java.io.IOException("Invalid data.");
    }

}
