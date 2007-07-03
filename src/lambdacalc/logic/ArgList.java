/*
 * ArgList.java
 *
 * Created on May 31, 2006, 4:14 PM
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a list of two or more arguments to a predicate, i.e. a vector.
 * ArgLists are not used for the arguments of one-place
 * predicates, which are represented by Identifiers.  
 */
public class ArgList extends Expr {
    private Expr[] exprs;
    
    /**
     * Constructs the ArgList.
     * @param innerExpressions an array of two or more expressions
     */
    public ArgList(Expr[] innerExpressions) {
        if (innerExpressions == null) throw new IllegalArgumentException("null argument");
        if (innerExpressions.length <= 1) throw new IllegalArgumentException("ArgList must have more than one element.");
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
    
    protected String toString(boolean html) {
        String ret = null;
        for (int i = 0; i < exprs.length; i++) {
            if (ret == null) ret = "(";
            else ret += ",";
            ret += exprs[i].toString(html); // note that we don't ever wrap it with parens because the comma separator here makes this unambiguous
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

    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // Looking for a lambda, but only do conversion in the first arg!
        Expr[] e = new Expr[exprs.length];
        boolean didConversion = false;
        for (int i = 0; i < exprs.length; i++) {
            if (!didConversion) {
                e[i] = exprs[i].performLambdaConversion1(accidentalBinders);
                if (e[i] != null)
                    didConversion = true;
                else
                    e[i] = exprs[i];
            }
        }
        
        if (!didConversion) // nothing happened
            return null;
        
        return new ArgList(e);
    }

    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // In the scope of a lambda. Do substitutions everywhere.
        Expr[] e = new Expr[exprs.length];
        for (int i = 0; i < exprs.length; i++)
            e[i] = exprs[i].performLambdaConversion2(var, replacement, binders, accidentalBinders);
        return new ArgList(e);
    }
 
    /**
     * Returns a List of all the subexpressions of this expression.
     * @return a list
     */
    public List getSubExpressions() {
        return Arrays.asList(this.getArgs());
    }
    
    /**
     * Creates a new ArgList using all the subexpressions given.
     *
     * @param subExpressions the list of subexpressions
     * @throws IllegalArgumentException not implemented
     * @return a new ArgList
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        return new ArgList((Expr[]) subExpressions.toArray(new Expr[]{}));
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        Expr[] e = new Expr[exprs.length];
        for (int i = 0; i < exprs.length; i++)
            e[i] = exprs[i].createAlphabeticalVariant(bindersToChange, variablesInUse, updates);
        return new ArgList(e);
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        output.writeInt(exprs.length);
        for (int i = 0; i < exprs.length; i++)
            exprs[i].writeToStream(output);
    }
    
    ArgList(java.io.DataInputStream input) throws java.io.IOException {
        // class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        int nexprs = input.readInt();
        if (nexprs < 2 || nexprs > 25) // sanity checks
            throw new java.io.IOException("Invalid data.");
        exprs = new Expr[nexprs];
        for (int i = 0; i < nexprs; i++)
            exprs[i] = Expr.readFromStream(input);
    }
}
