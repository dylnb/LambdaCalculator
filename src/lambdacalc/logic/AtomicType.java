/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * AtomicType.java
 *
 * Created on May 30, 2006, 10:44 AM
 */

package lambdacalc.logic;

/**
 * Represents an atomic type, such as e or t.
 */
public abstract class AtomicType extends Type {
    
    private char symbol;
    
    /**
     * Creates a new instance of AtomicType
     * @param symbol the type, like e or t
     */
//    public AtomicType(char symbol) {
//        this.symbol=symbol;
//    }
        
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
    
//    AtomicType(java.io.DataInputStream input) throws java.io.IOException {
//        // the class string has already been read
//        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
//        symbol = input.readChar();
//    }
    
}
