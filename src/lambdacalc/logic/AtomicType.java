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
}
