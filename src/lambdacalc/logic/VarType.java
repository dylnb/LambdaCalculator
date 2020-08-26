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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

import java.util.*;

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
        switch(this.symbol) {
            case 'a':
                // alpha
                return "\u03B1";
            case 'A':
                // Alpha
                return "\u0391";
            case 'b':
                // beta
                return "\u03B2";
            case 'B':
                // Beta
                return "\u0392";    
            case 'c':
                // chi
                return "\u03C7";
            case 'C':
                // Chi
                return "\u03A7";    
            case 'd':
                // delta
                return "\u03B4";    
            case 'D':
                // Delta
                return "\u0394";    
            case 'e':
                // epsilon
                return "\u03B5";   
            case 'E':
                // Epsilon
                return "\u0395";
            case 'f':
                // phi
                return "\u03C6";
            case 'F':
                // Phi
                return "\u03A6";    
            case 'g':
                // gamma
                return "\u03B3";
            case 'G':
                // Gamma
                return "\u0393";
            case 'h':
                // theta
                return "\u03B8";
            case 'H':
                // Theta
                return "\u0398";
            case 'i':
                // iota
                return "\u03B9";
            case 'I':
                // Iota
                return "\u0399";
            case 'j':
                // eta
                return "\u03B7";
            case 'J':
                // Eta
                return "\u0397";
            case 'k':
                // kappa
                return "\u03BA";
            case 'K':
                // Kappa
                return "\u039A";
            case 'l':
                // lambda
                return "\u03BB";
            case 'L':
                // Lambda
                return "\u039B";
            case 'm':
                // mu
                return "\u03BC";
            case 'M':
                // Mu
                return "\u039C";
            case 'n':
                // nu
                return "\u03BD";
            case 'N':
                // Nu
                return "\u039D";
            case 'o':
                // omicron
                return "\u03BF";
            case 'O':
                // Omicron
                return "\u039F";
            case 'p':
                // pi
                return "\u03C0";
            case 'P':
                // Pi
                return "\u03A0";
            case 'q':
                // psi
                return "\u03C8";
            case 'Q':
                // Psi
                return "\u03A8";
            case 'r':
                // rho
                return "\u03C1";
            case 'R':
                // Rho
                return "\u03A1";
            case 's':
                // sigma
                return "\u03C3";
            case 'S':
                // Sigma
                return "\u03A3";
            case 't':
                // tau
                return "\u03C4";
            case 'T':
                // Tau
                return "\u03A4";
            case 'u':
                // upsilon
                return "\u03C5";
            case 'U':
                // Upsilon
                return "\u03A5";
            case 'v':
                // phi'
                return "\u03C6" + "'";
            case 'V':
                // Phi'
                return "\u03A6" + "'";
            case 'w':
                // omega
                return "\u03C9";
            case 'W':
                // Omega
                return "\u03A9";
            case 'x':
                // xi
                return "\u03BE";
            case 'X':
                // Xi
                return "\u039E";
            case 'y':
                // upsilon'
                return "\u03C5" + "'";
            case 'Y':
                // Upsilon'
                return "\u03A5" + "'";
            case 'z':
                // zeta
                return "\u03B6";
            case 'Z':
                // Zeta
                return "\u0396";
            default:
                return VarTypeSignifier + String.valueOf(this.symbol);
        }
    }

    
    public String toShortString() {
        return toString();
    }

    public String toLatexString() {
        return toString();
    }
    
    protected boolean equals(Type t) {
	if(t instanceof VarType){
	    return (this.getSymbol() == ((VarType) t).getSymbol());
	}
	return false;
//        if (t instanceof AtomicType) {
//            AtomicType at = (AtomicType) t;
//            return (this.getSymbol() == at.getSymbol());
//        } else { 
//            return false;
//        }

	/*
        return true;
	*/
    }  
    
    /**
     * Tests whether a VarType can be unified with another type.
     * @param t The Type to be unified with.
     * @return A MatchPair containing the variable mappings (unifier) of the two types, 
     * so long as the other Type is not productType, in which case it returns null. 
     */
    public MatchPair matches(Type t){
	if(t instanceof ProductType)
	    return null;

	MatchPair pair = new MatchPair(this, t);
	pair.setMatches(this, this, t);
	return pair;
	/*
	if(t instanceof ProductType)
	    return null;
	HashMap<Type, HashMap<Type, Type>> matchlist = new HashMap<Type, HashMap<Type, Type>>();
	matchlist.put(this, new HashMap<Type, Type>());
	matchlist.get(this).put(this, t);
	return matchlist;
	*/
    }
    
    public boolean containsVar() {
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
