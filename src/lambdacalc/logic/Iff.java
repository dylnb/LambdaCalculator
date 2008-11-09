/*
 * Iff.java
 *
 * Created on May 29, 2006, 3:41 PM
 */

package lambdacalc.logic;

/**
 * Represents the biconditional binary connective.
 */
public class Iff extends LogicalBinary {
    /**
     * The unicode double arrow.
     */
    public static final char SYMBOL = '\u2194';

    public static final String LATEX_REPR = "\\leftrightarrow";
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public Iff(Expr left, Expr right) {
        super(left,right);
    }

    public String getLatexRepr() {
        return LATEX_REPR;
    }

    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public Type getOperandType() {
        return Type.T;
    }

    protected Binary create(Expr left, Expr right) {
        return new Iff(left, right);
    }

    Iff(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
