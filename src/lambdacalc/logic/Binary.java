/*
 * Binary.java
 *
 * Created on May 29, 2006, 3:18 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Abstract base class of the binary connectives, including
 * the logical binary connectives and function application.
 */
public abstract class Binary extends Expr {
    
    private Expr left;
    private Expr right;
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public Binary(Expr left, Expr right) {
        this.left=left;
        this.right=right;
        if (left == null) throw new IllegalArgumentException();
        if (right == null) throw new IllegalArgumentException();
    }
    
    /**
     * Gets the left side of the connective.
     */
    public Expr getLeft() {
        return left;
    }
    
    /**
     * Gets the right side of the connective.
     */
    public Expr getRight() {
        return right;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {

        // ignore parentheses for equality test
        e = e.stripAnyParens();

        if (e instanceof Binary) {
            return this.equals((Binary) e, useMaps, thisMap, otherMap, collapseAllVars);
        } else {
            return false;
        }
    }
    
    private boolean equals(Binary b, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
        return equalsHelper(b)
            && this.getLeft().equals(b.getLeft(), useMaps, thisMap, otherMap, collapseAllVars)
            && this.getRight().equals(b.getRight(), useMaps, thisMap, otherMap, collapseAllVars);
    }
    
    protected boolean equalsHelper(Binary b) {
        return this.getClass() == b.getClass();
    }

    protected Set getVars(boolean unboundOnly) {
        HashSet ret = new HashSet();
        ret.addAll(getLeft().getVars(unboundOnly));
        ret.addAll(getRight().getVars(unboundOnly));
        return ret;
    }
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of binary connective, with the given expressions on the
     * left and right.
     */
    protected abstract Binary create(Expr left, Expr right);

    public Expr replace(Expr expr1, Expr expr2) {
        if (this.equals(expr1)) return expr2;
        Expr newLeft = this.getLeft();
        if (newLeft.equals(expr1)) newLeft = expr2;
        Expr newRight = this.getRight();
        if (newRight.equals(expr1)) newRight = expr2;
        return create(newLeft, newRight);
    }
    
    /**
     * Returns a List of the two subexpressions of this expression.
     * @return a list
     */
    public List getSubExpressions() {
        Vector result = new Vector(2);
        result.add(this.getLeft());
        result.add(this.getRight());
        return result;
    }
    
    /**
     * Creates a new binary expression using all the subexpressions given.
     *
     * @param subExpressions the list of subexpressions
     * @throws IllegalArgumentException if the list does not contain exactly two
     * subexpressions
     * @return a new expression of the same runtime type as this
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        if (subExpressions.size() != 2) 
            throw new IllegalArgumentException("List does not contain exactly two arguments");
        return create((Expr) subExpressions.get(0), (Expr) subExpressions.get(1));
    }
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return create(getLeft().createAlphabeticalVariant(bindersToChange, variablesInUse, updates),
                getRight().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        left.writeToStream(output);
        right.writeToStream(output);
    }
    
    Binary(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        left = Expr.readFromStream(input);
        right = Expr.readFromStream(input);
    }

}
