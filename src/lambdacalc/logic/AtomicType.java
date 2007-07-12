/*
 * AtomicType.java
 *
 * Created on May 30, 2006, 10:44 AM
 */

package lambdacalc.logic;

/**
 * Represents an atomic type, such as e or t.
 */
public class AtomicType extends Type {
    
    private char symbol;
    
    /**
     * Creates a new instance of AtomicType
     * @param symbol the type, like e or t
     */
    public AtomicType(char symbol) {
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
    
    protected boolean equals(Type t) {
        if (t instanceof AtomicType) {
            AtomicType at = (AtomicType) t;
            return (this.getSymbol() == at.getSymbol());
        } else { 
            return false;
        }
    }  
    
    public int hashCode() {
        return String.valueOf(symbol).hashCode(); // better way of doing this?
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("AtomicType");
        output.writeShort(0); // data format version
        output.writeChar(symbol);
    }
    
    AtomicType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readChar();
    }
            
}
