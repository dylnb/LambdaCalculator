/*
 * Binary.java
 *
 * Created on May 29, 2006, 3:18 PM
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
