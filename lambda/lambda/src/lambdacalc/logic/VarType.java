/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

/**
 *
 * @author dylnb
 */
public class VarType extends AtomicType {
    
    private char symbol;
    
    /**
     * Creates a new instance of VarType
     * @param symbol the type, like e or t
     */
    public VarType(char symbol) {
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
//        if (t instanceof AtomicType) {
//            AtomicType at = (AtomicType) t;
//            return (this.getSymbol() == at.getSymbol());
//        } else { 
//            return false;
//        }
        return true;
    }  
    
    public int hashCode() {
        return String.valueOf(symbol).hashCode(); // better way of doing this?
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("VarType");
        output.writeShort(0); // data format version
        output.writeChar(symbol);
    }
    
    VarType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readChar();
    }
    
}
