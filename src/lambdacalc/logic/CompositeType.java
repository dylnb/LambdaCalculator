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
 * CompositeType.java
 *
 * Created on May 30, 2006, 10:42 AM
 */

package lambdacalc.logic;

import java.util.ArrayList;
import java.util.*;

/**
 * Represents a composite (function) type, like &lt;et&gt;.
 */
public class CompositeType extends Type {
    public final static char LEFT_BRACKET = '<'; // '\u27E8'; // '\u2329'; '\u3008';
    public final static char RIGHT_BRACKET = '>'; // '\u232A';
    public final static char SEPARATOR = ',';
    
    private Type left;
    private Type right;
    
    /**
     * Creates a new instance of CompositeType
     * @param left the type of the domain of the function
     * @param right the type of the range of the function
     */
    public CompositeType(Type left, Type right) {
        this.left=left;
        this.right=right;
    }
    
    /**
     * Gets the type of the domain of the function.
     */
    public Type getLeft() {
        return this.left;
    }
    
    /**
     * Gets the type of the range of the function.
     */
    public Type getRight() {
        return this.right;
    }
    
    public ArrayList<Type> getAtomicTypes() {
        ArrayList<Type> l = new ArrayList<Type>();        
        ArrayList<Type> r = new ArrayList<Type>();
        if (this.left instanceof CompositeType) {
            l.addAll(((CompositeType)this.left).getAtomicTypes());
        } else {
            l.add(this.left);
        }
        if (this.right instanceof CompositeType) {
            r.addAll(((CompositeType)this.right).getAtomicTypes());
        } else {
            r.add(this.right);
        }
        l.addAll(r);
        return l;
    }
    
    protected boolean equals(Type t) {
	
	if(t instanceof CompositeType){
	    boolean leftTrue = this.getLeft().equals(((CompositeType) t).getLeft());
	    boolean rightTrue = this.getRight().equals(((CompositeType) t).getRight());
	    if(leftTrue != rightTrue)
		return false;
	    return leftTrue;
	}
	return false;
    }
    
    //sets all instances of a variable for a given CompositeType to the same reference. Uses getAlignedType in Matchpair logic
    public CompositeType renameVariables(ArrayList<Type> varList){
	Type oldLeft = this.getLeft();
	    Type oldRight = this.getRight();
	    Type newLeft;
	    Type newRight;
	    if (oldLeft instanceof CompositeType) {
		this.left = ((CompositeType) oldLeft).renameVariables(varList);
	    }
	    else if(oldLeft instanceof ProductType){
		this.left = ((ProductType) oldLeft).renameVariables(varList);
	    }
	    else if(oldLeft instanceof VarType){
		    if(varList.contains(oldLeft))
			this.left = varList.get(varList.indexOf(oldLeft));
		    
		    else{
			varList.add(oldLeft);
			this.left = oldLeft;
		    }
	    }
	    else
		this.left = oldLeft;
	    
	    if (oldRight instanceof CompositeType) {
		this.right = ((CompositeType) oldRight).renameVariables(varList);
	    } 
	     else if(oldRight instanceof VarType){
		    if(varList.contains(oldRight)){
			this.right = varList.get(varList.indexOf(oldRight));
		    }
		    else{
			varList.add(oldRight);
			this.right = oldRight;
		    }
		}
	    else
		this.right = oldRight;
	    
	    return new CompositeType(this.left, this.right);
    }
    
    /**
     * Tests whether two types can be unified. 
     * @param t The Type to be unified with.
     * @return A matchPair class containing the variable mappings (unifier) for each Type,
     * or null if cannot be unified.
     */
    public MatchPair matches(Type t){
	return matches2(t, false);
    }
    
    /**
     * Helper method for matches. For each half of the CompositeType, unify them. 
     * If successful, insert the pairs into the parent CompositeType
     * If not successful, try the unification procedure from right to left.
     * @param t the Type to be unified with.
     * @param RtoL Whether the pass is right to left. 
     * @return A MatchPair class containing the variable mappings for each productType, 
     * or null if cannot be unified. 
     */
    public MatchPair matches2(Type t, boolean RtoL){
	//set to void if using
	if(t instanceof VarType)
	    ((VarType) t).matches(this);
	else if(t instanceof CompositeType){
	    CompositeType curr = this.renameVariables(new ArrayList<Type>());
	    CompositeType newT = ((CompositeType) t).renameVariables(new ArrayList<Type>());
	    Type thisLeft = curr.getLeft();
	    Type thisRight = curr.getRight();
	    Type expLeft = newT.getLeft();
	    Type expRight = newT.getRight();
	    
	    MatchPair leftHalf = thisLeft.matches(expLeft);
	    MatchPair rightHalf = thisRight.matches(expRight);
	   
	    if(leftHalf != null && rightHalf != null){
		MatchPair pair = new MatchPair(this, t);
		
		boolean leftHalfPass;
		boolean rightHalfPass;
		

		    leftHalfPass = pair.insertMatch(leftHalf.getMatches(thisLeft), leftHalf.getMatches(expLeft), leftHalf.getGraph());

		    rightHalfPass = pair.insertMatch(rightHalf.getMatches(thisRight), rightHalf.getMatches(expRight), rightHalf.getGraph());
		
		if(!(leftHalfPass && rightHalfPass)){
		    if(RtoL)
			return null;
		    else
			return ((CompositeType) t).matches2(this, true);
		}
		if(RtoL)
		    return pair.flip();
		return pair;
	    }
	}
	return null;
    }
    
    public boolean containsVar() {
        return (left.containsVar() || right.containsVar());
    }
    
    public int hashCode() {
        return left.hashCode() ^ right.hashCode(); // XOR of hash codes
    }

    public String toString() {
        return this.toStringHelper(
                String.valueOf(LEFT_BRACKET),
                String.valueOf(SEPARATOR),
                String.valueOf(RIGHT_BRACKET),
                false);
    }

    public String toLatexString() {
        return this.toStringHelper("\\langle ", ",", "\\rangle ", true);
    }

    // Examples: <e,t> <e,<et>>
    private String toStringHelper(String leftBracket, String separator, String rightBracket, boolean latex) {
        // Maribel wants the canonical form <e,t> to be used,
        // but in the Latex output we want to be able to give a shorter form
        // for embedded types like <et, t>.
        if (latex && left instanceof AtomicType && right instanceof AtomicType) {
            return String.valueOf(left)
            + String.valueOf(right);
        } else {
            String res = "";
            res += leftBracket;
            if (latex) {
                res += left.toLatexString();
            } else {
                res += left.toString();
            }
            res += (left instanceof ProductType ? " " : "");
            res += separator;
            res += (left instanceof ProductType ? " " : "");
            if (latex) {
                res += right.toLatexString();
            } else {
                res += right.toString();
            }
            res += rightBracket;
            return res;
        }
        
    }
    
    public String toShortString() {
        if (left instanceof AtomicType && right instanceof AtomicType) {
            return 
                    //String.valueOf(LEFT_BRACKET)
                    //+
                    String.valueOf(left)
            +String.valueOf(right)
            //+String.valueOf(RIGHT_BRACKET)
            ;
        } else {  
            return String.valueOf(LEFT_BRACKET)
            + left.toShortString()
            + (left instanceof ProductType ? " " : "")
            + String.valueOf(SEPARATOR)
            + (left instanceof ProductType ? " " : "")
            + right.toShortString()
            + String.valueOf(RIGHT_BRACKET);
        }     
    }

    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("CompositeType");
        output.writeShort(0); // data format version
        left.writeToStream(output);
        right.writeToStream(output);
    }
    
    CompositeType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        left = Type.readFromStream(input);
        right = Type.readFromStream(input);
    }
}
