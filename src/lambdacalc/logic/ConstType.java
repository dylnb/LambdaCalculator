/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

/**
 *
 * @author dylnb
 */
public class ConstType extends AtomicType {
    
    private char symbol;
    
    /**
     * Creates a new instance of ConstType
     * @param symbol the type, like e or t
     */
    public ConstType(char symbol) {
        this.symbol=symbol;
    }
        
    public char getSymbol() {
        return this.symbol;
    }
    
    public String toString() {
        return String.valueOf(this.symbol);
    }

    
    public String toShortString() {
        return toString();
    }

    public String toLatexString() {
        return toString();
    }
    
    protected boolean equals(Type t) {
        if (t instanceof VarType) {
            return true;
        } else if (t instanceof ConstType) {
            ConstType at = (ConstType) t;
            return (this.getSymbol() == at.getSymbol());
        } else { 
            return false;
        }
    }
    
    public boolean containsVar() {
        return false;
    }
    
    public int hashCode() {
        return String.valueOf(symbol).hashCode(); // better way of doing this?
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("ConstType");
        output.writeShort(0); // data format version
        output.writeChar(symbol);
    }
    
    ConstType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readChar();
    }
}
