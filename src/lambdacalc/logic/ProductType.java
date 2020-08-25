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
 * ProductType.java
 *
 * Created on May 30, 2006, 5:52 PM
 */

package lambdacalc.logic;

import java.util.*;

/**
 * Represents a cartesian product type.  This is the type
 * of vectors, i.e. the list of arguments to a predicate of
 * two or more arguments.  Product types are the types
 * of ArgLists.
 */
public class ProductType extends Type {
    /**
     * The unicode cross character.
     */
    public static final char SYMBOL = '\u00D7';

    public static final String LATEX_SYMBOL = "\\times";
    
    private Type[] subtypes;
    
    /**
     * Creates a new product type with the given sub-types.
     */
    public ProductType(Type[] subtypes) {
        this.subtypes = subtypes;
        if (subtypes.length <= 1) throw new IllegalArgumentException();
    }
    
    /**
     * Gets the sub-types of this product type.
     */
    public Type[] getSubTypes() {
        return subtypes;
    }
    
    /**
     * Gets the number of sub-types in this type.
     */
    public int getArity() {
        return subtypes.length;
    }
    
    protected boolean equals(Type t) {
	if (t instanceof ProductType) {
            Type[] a1 = getSubTypes();
            Type[] a2 = ((ProductType)t).getSubTypes();
            if (a1.length != a2.length) 
		return false;
            for (int i = 0; i < a1.length; i++) {
                Type l = a1[i];
                Type r = a2[i];
                boolean guard = a1[i].equals(a2[i]);
                if (!guard)
                    return false;
            }
            return true;
        } 
	
	return false;
    }
	/**
	 * Matches two Types
	 * @param t the Type to be matched with
	 * @return a MatchPair class containing mappings for each Type. 
	 */
	public MatchPair matches(Type t){
	    return matches2(t, false);
	}
	/**
	 * Helper method for matches. Only two productTypes can be matched together.
	 * For each component of the ProductType, match them. If successful insert the pairing into the parent ProductType
	 * If not successful, try matching from right to left.
	 * @param t the ProductType to be matched to
	 * @param RtoL whether the pass is right to Left
	 * @return A MatchPair class containing the mappings for each productType, or null if cannot be matched. 
	 */
        private MatchPair matches2(Type t, boolean RtoL){
	    
	    if(t instanceof ProductType){
		if (this.getSubTypes().length == ((ProductType) t).getSubTypes().length){
		    MatchPair pair;
		    if(RtoL)
			pair = new MatchPair(t, this);
		    else
			pair = new MatchPair(this, t);
		    for(int i = 0; i < this.getSubTypes().length; i++){
			MatchPair parts = this.getSubTypes()[i].matches(((ProductType) t).getSubTypes()[i]);
			if(parts != null){
			    
			    boolean pass;
			    pass = pair.insertMatch(parts.getMatches(parts.getLeft()), parts.getMatches(parts.getRight()));
			  
			    if(!(pass)){
				if(RtoL)
				    return null;
			    
				else
				    return ((ProductType) t).matches2(this, true);
			    }
			}
		    }
		    if(RtoL)
			return pair.flip();
		    return pair;
		}
	    }
	    return null;
	    /*
	    HashMap<Type, HashMap<Type, Type>> matchList = new HashMap<Type, HashMap<Type, Type> >();
	    HashMap<Type, HashMap<Type, Type>> subMatches = new HashMap<Type, HashMap<Type, Type> >();
	    if(t instanceof ProductType){
		//Make sure both product types are the same length
		if (this.subtypes.length != ((ProductType) t).subtypes.length)
		    return null;
		//For each part of the product type
		for(int i = 0; i < this.subtypes.length; i++){
		    /*Do matches on each subtype of the productType.
			returns hashmap mapping subType -> var -> const if available, e.g. subtype <'a, 'b>.matches(<'a, 'a>) 
		    	returns hashmap of <'a, 'b> : 'a -> 'a, 'b -> 'a, <'a, 'a> : 'a -> null
		    *
		    subMatches = this.subtypes[i].matches(((ProductType) t).subtypes[i]);
		    //if the subtypes are matched (or equal)
		    if(subMatches != null){
			//for each subtype 
			for(Type subType : subMatches.keySet()){
			    //if the subtype belongs to this, checking refernce, e.g. alpha can exist in both expressions. 
			    if(this.subtypes[i] == (subType)){
				//if ProductType isn't yet in our matchlist, add it. (potential change to add ProductType from beginning?)
				if(!matchList.containsKey(this))
				    matchList.put(this, new HashMap<Type, Type>());
				//for each var mapping in our subtype
				for(Type subMatch : subMatches.get(subType).keySet()){
				    //if this var is already in our matches (1 to many mapping), try other way, if already trying return
				    if(matchList.get(this).containsKey(subMatch)){
					if(!matchList.get(this).get(subMatch).equals(subMatches.get(subType).get(subMatch))){
					    if(RtoL == true)
						return null;
					    return ((ProductType) t).matches2(this, true);
					}
				    }
				    matchList.get(this).put(subMatch, subMatches.get(subType).get(subMatch));
				    //put all key-pair values of this.subtypes[i] into this hashmap   
				}
			    }
			    //same code as above, but for when subtype is part of type T. 
			    else{
				if(!matchList.containsKey(t))
				    matchList.put(t, new HashMap<Type, Type>());
				for(Type subMatch: subMatches.get(subType).keySet()){
				    if(matchList.get(t).containsKey(subMatch)){
					if(!matchList.get(t).get(subMatch).equals(subMatches.get(subType).get(subMatch))){
					    //potentially useless and should return null directly? 
					    if(RtoL == true)
						return null;
					    return ((ProductType) t).matches2(this, true);
					}
				    }
				    else
					matchList.get(t).put(subMatch, subMatches.get(subType).get(subMatch));
				}
			    }
			}
		    }
		}
		//All is well, return matchlist. Else return null for all other cases. 
		return matchList;
	    }
	    return null;
	*/
	}
    
    public boolean containsVar() {
        for (Type subtype : subtypes) {
            if (subtype.containsVar()) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        int hc = 0;
        for (int i = 0; i < subtypes.length; i++)
            hc ^= subtypes[i].hashCode(); // XOR the hash codes
        return hc;
    }
    
    public String toShortString() {
        return toString();
    }

    public String toString() {
        return this.toStringHelper(String.valueOf(this.SYMBOL), false);
    }

    public String toLatexString() {
        return this.toStringHelper(this.LATEX_SYMBOL, true);
    }
    
    public String toStringHelper(String separator, boolean latex) {
        String ret = "";
        for (int i = 0; i < getSubTypes().length; i++) {
            if (i > 0)
                ret += " " + separator + " ";
            if (latex) {
                String subString = getSubTypes()[i].toLatexString();
                if (subString.length() > 1 && !subString.contains("<")) {
                    subString = "(" + subString + ")";
                }
                ret += subString;
            } else {
                String subString = getSubTypes()[i].toString();
                if (subString.length() > 1 && !subString.contains("<")) {
                    subString = "(" + subString + ")";
                }
                ret += subString;
            }
        }
        return ret;
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("ProductType");
        output.writeShort(0); // data format version
        output.writeInt(subtypes.length);
        for (int i = 0; i < subtypes.length; i++)
            subtypes[i].writeToStream(output);
    }
    
    ProductType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        int ntypes = input.readInt();
        if (ntypes <= 1 || ntypes > 25) // sanity checks
            throw new java.io.IOException("Invalid data.");
        subtypes = new Type[ntypes];
        for (int i = 0; i < ntypes; i++)
            subtypes[i] = Type.readFromStream(input);
    }
}
