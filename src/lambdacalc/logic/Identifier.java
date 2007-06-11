/*
 * Identifier.java
 *
 * Created on May 29, 2006, 3:14 PM
 */

package lambdacalc.logic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The abstract base class of constants (Const) and variables (Var).
 */
public abstract class Identifier extends Expr {
    
    /**
     * The prime symbol.
     */
    public static final char PRIME = '\u02B9'; // 0x2032 is another one
    
    String symbol;
    Type type;
    boolean typeIsExplicit;
    
    /** Creates a new instance of Identifier */
    public Identifier(String symbol, Type type, boolean isTypeExplicit) {
        this.symbol = symbol; 
        this.type = type;
        this.typeIsExplicit = isTypeExplicit;
        if (symbol == null) throw new IllegalArgumentException();
        if (type == null) throw new IllegalArgumentException();
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 1;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars) {
 
        // ignore parentheses for equality test
        e = e.stripAnyParens();

        if (e instanceof Identifier) {
            if (collapseAllVars) return true;
            return this.equals((Identifier) e, useMaps, thisMap, otherMap);
        } else {
            return false;           
        }
    }
    
    public int hashCode() {
        return this.symbol.hashCode()^super.hashCode();
    }
    
    protected abstract boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap);
    
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda. None here.
        return null;
    }
    
    public String toString() {
        if (!isTypeExplicit())
            return symbol;
        else
            return symbol + "_" + type.toString();
    }
    
    /**
     * Gets the string name of the identifier.
     */
    public String getSymbol() {
        return symbol;
    }
    
    public Type getType() {
        return type;
    }
    
    public boolean isTypeExplicit() {
        return typeIsExplicit;
    }
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of identifier, that is, to clone it.
     *
     * @return a copy of this
     */
    protected abstract Identifier create();
    
    /**
     * Returns an (empty) List representing the fact that identifiers
     * have no subexpressions.
     * @return an empty list
     */
    public List getSubExpressions() {
        return new Vector(0);
    }
    
    /**
     * Returns a copy of this Identifier.
     *
     * @param subExpressions the list of subexpressions (must be null or empty)
     * @throws IllegalArgumentException if the list is nonnull and nonempty
     * @return a copy of this
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        if (subExpressions != null && subExpressions.size() != 0) 
            throw new IllegalArgumentException("List is nonempty");
        return create();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(1); // data format version
        output.writeUTF(symbol);
        type.writeToStream(output);
        output.writeBoolean(typeIsExplicit);
    }
    
    Identifier(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 1) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readUTF();
        type = Type.readFromStream(input);
        typeIsExplicit = input.readBoolean();
    }
}
