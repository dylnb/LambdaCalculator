/*
 * Unary.java
 *
 * Created on May 29, 2006, 3:20 PM
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Abstract base class of unary operators, including
 * negation and parenthesis.
 */
public abstract class Unary extends Expr {
    
    private Expr innerExpr;
    
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
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {
        
        // ignore parentheses for equality test
        e = e.stripOutermostParens();

        if (e instanceof Unary) {
            return this.equals((Unary) e, useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
        } else {
            return false;
        }
    }
    
    private boolean equals(Unary u, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, Map freeVarMap) {
        return this.getClass() == u.getClass() 
            && this.getInnerExpr().equals(u.getInnerExpr(), useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
    }
    
    public Expr getInnerExpr() {
        return innerExpr;
    }

    protected Set getVars(boolean unboundOnly) {
        return getInnerExpr().getVars(unboundOnly);
    }

    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // Looking for a lambda...
        Expr inner = getInnerExpr().performLambdaConversion1(accidentalBinders);
        if (inner == null) // nothing happened
            return null;
        return create(inner);
    }

    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // In the scope of a lambda...
        return create(getInnerExpr().performLambdaConversion2(var, replacement, binders, accidentalBinders));
    }
    
    /**
     * Returns a List containing the unique subexpression of this expression.
     * @return a list
     */
    public List getSubExpressions() {
        Vector result = new Vector(1);
        result.add(this.getInnerExpr());
        return result;
    }
    
    /**
     * Creates a new unary expression using the subexpression given.
     *
     * @param subExpressions the list containing the subexpression
     * @throws IllegalArgumentException if the list does not contain exactly one
     * subexpression
     * @return a new expression of the same runtime type as this
     */
    public Expr createFromSubExpressions(List subExpression)
     throws IllegalArgumentException {
        if (subExpression.size() != 1) 
            throw new IllegalArgumentException("List does not contain exactly one argument");
        return create((Expr) subExpression.get(0));
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return create(getInnerExpr().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
    
    public Expr createAlphatypicalVariant(HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        return create(getInnerExpr().createAlphatypicalVariant(alignments, variablesInUse, updates));
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        innerExpr.writeToStream(output);
    }
    
    Unary(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        innerExpr = Expr.readFromStream(input);
    }
}
