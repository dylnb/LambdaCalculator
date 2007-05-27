/*
 * Identifier.java
 *
 * Created on May 29, 2006, 3:14 PM
 */

package lambdacalc.logic;

import java.util.Map;
import java.util.Set;

/**
 * The abstract base class of constants (Const) and variables (Var).
 */
public abstract class Identifier extends Expr {
    /*
     * The unicode prime character.
     */
    /**
     * The prime symbol.
     */
    public static final char PRIME = '\u02B9'; // 0x2032 is another one
    
    String symbol;
    Type type;
    
    /** Creates a new instance of Identifier */
    public Identifier(String symbol, Type type) {
        this.symbol=symbol; 
        this.type = type;
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
    
    protected abstract boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap);
    
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda. None here.
        return null;
    }

    public int hashCode() {
        return symbol.hashCode();
    }
    
    public String toString() {
        return symbol;
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
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        output.writeUTF(symbol);
        type.writeToStream(output);
    }
    
    Identifier(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readUTF();
        type = Type.readFromStream(input);
    }
}
