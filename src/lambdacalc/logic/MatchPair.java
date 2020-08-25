/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

import java.util.*;

/**
 * Class containing pairings of matches of two types given
 * @author Raefno1
 */
public class MatchPair
{
    private Type left;
    private Type right;
    private HashMap<Type, Type> leftMatches;
    private HashMap<Type, Type> rightMatches;
    
    /**
     * Constructs the MatchPair class with empty pairings
     * @param l the left, or function, Type
     * @param r the right, or argument, Type
     */
    public MatchPair(Type l, Type r){
	this.left = l;
	this.right = r;
	this.leftMatches = new HashMap<Type, Type>();
	this.rightMatches = new HashMap<Type, Type>();
    }
    
    /**
     * Constructs a MatchPair class with already provided matches
     * @param l the left, or function, Type
     * @param r the right, or argument, Type
     * @param leftMatches the list of matches from the left Type to the right Type
     * @param rightMatches the list of matches from the right Type to the left Type
     */
    public MatchPair(Type l, Type r, HashMap<Type, Type> leftMatches, HashMap<Type, Type> rightMatches){
	this.left = l;
	this.right = r;
	this.leftMatches = leftMatches;
	this.rightMatches = rightMatches;
    }
    
    //Returns the left half of the two matching functions

    /**
     * Gets the left Type
     * @return the left Type
     */
    public Type getLeft(){
	return this.left;
    }
    
    //Returns the right half of the two matching functions 

    /**
     * Gets the right Type
     * @return the right Type
     */
    public Type getRight(){
	return this.right;
    }
    
    //Returns the mapping of variables for a given Type side

    /**
     * Gets the match pairings of the given Type
     * @param side the Type whose match pairs are meant to be returned
     * @return the match pairings of the given type
     */
    public HashMap<Type, Type> getMatches(Type side){
	if(side == this.left) 
	    return this.leftMatches;
	return this.rightMatches;
    }
    
    //Add a matchPair for a specific side 
    //redundant and will delete later, can just do .getMatches(Type).put(key, value)

    /**
     * Sets a match pair for the given type
     * @param side The Type to add the match pair to
     * @param key The variable being mapped
     * @param value The value of the variable
     */
    public void setMatches(Type side, Type key, Type value){
	if(side == this.left)
	    this.leftMatches.put(key, value);
	else
	    this.rightMatches.put(key, value);
    }
    
    
    //TO make the final mapping, current issue is when alpha -> alpha it enters infinite recursion 
    //even if the two VarTypes have different references
    //I think this is because of VarType having a .equals now, and so the containsKey method looks for a compareTo method and not reference anymore
    //Originally, hashMaps containsKey looks for reference, since hashing objects even if they are created the same have different hashCodes, 
    //Somehow that is messing up here...
    private void finalAlignments(){
	for(Type key : this.getMatches(this.getLeft()).keySet()){
	    Type isVarType = this.getMatches(this.getLeft()).get(key);
		while(isVarType instanceof VarType){
		    if(this.getMatches(this.getLeft()).containsKey(isVarType))
			isVarType = this.getMatches(this.getLeft()).get(isVarType);
		    
		    else if(this.getMatches(this.getRight()).containsKey(isVarType))
			isVarType = this.getMatches(this.getRight()).get(isVarType);
		    
		    else
			break;
		}
	    this.getMatches(this.getLeft()).replace(key, isVarType);
	}
	
	for(Type key : this.getMatches(this.getRight()).keySet()){
	    Type isVarType = this.getMatches(this.getRight()).get(key);
		while(isVarType instanceof VarType){
		    if(this.getMatches(this.getLeft()).containsKey(isVarType))
			isVarType = this.getMatches(this.getLeft()).get(isVarType);
		    
		    else if(this.getMatches(this.getRight()).containsKey(isVarType))
			isVarType = this.getMatches(this.getRight()).get(isVarType);
		    
		    else
			break;
		}
	    this.getMatches(this.getLeft()).replace(key, isVarType);
	}
    }
   
    //Taking the mapping of a subType and adding it to the parents.
    //This adds the mappings for both, e.g. <a1, t> -> <e, a2> 
    //calling this on the mapping of a1, e will place a1 -> e this in <a1, t> mapping
    //calling this on the mapping of t, a2 will place a2 -> t this in <e, a2> mapping

    /**
     * Inserts the match pairs for each component of a child Type into the match pairs of the parent Type.
     * This occurs for both the left and right Types
     * @param left The match pairs of all the children of the left Type
     * @param right The match pairs of all the children of the right Type
     * @return True if the match pairs are able to be added to the parent, else there is a conflict of pairings and returns false. 
     */
    public boolean insertMatch(HashMap<Type, Type> left, HashMap<Type, Type> right){
	
	//cannot rewrite due to .getLeft, .getRight, unless changed. 
	
	/*
	First see if key contains a mapping
	    If so, see if the current mapping is a variable (that means variable hasn't been mapped yet)
		Then, if current incoming value is also variable, 
		    check if incoming variable has mapping
		    if so, set key and current value to incoming value mapping
		    if not, check that two variables are equal. 
	    if not variable, set current key and set other side mapping of variable to incoming value
	
	    If current mapping is not variable
		see if incoming value is variable
		Check if variable currently has a mapping
		if so, check if equal
		if not return false
	
	If mapped and incoming not variable
	check if equal
	
	
	If key is not mapped
	    Check if incoming is variable
	    check if incoming has a mapping
	    if so, map key to that
	    else map key to variable
	*/
	
	
	for(Type key : left.keySet()){
	    //get the key mapping 
	    Type varValue = left.get(key);
	    //if key mapped to variable
	    if(varValue instanceof VarType){
		//if variable is mapped to something, get it
		if(this.getMatches(this.getRight()).containsKey(varValue)){
		    varValue = this.getMatches(this.getRight()).get(varValue);   
		}
	    }
	    
	    //if current Type contains this key
	    if(this.getMatches(this.getLeft()).containsKey(key)){
		//if the mapping of the key is a variable
		if(this.getMatches(this.getLeft()).get(key) instanceof VarType){
		   //if the incoming value is a variable, return false if mapping is not equal
		    if(varValue instanceof VarType){
			if(!(varValue.equals(this.getMatches(this.getLeft()).get(key))))
			    return false; 
		    }    
		    //otherwise incoming value is not variable, set variable mapping to incoming value
		    //set current mapping to this value 
		    else{
			this.getMatches(this.getRight()).put(this.getMatches(this.getLeft()).get(key), varValue);
			this.getMatches(this.getLeft()).replace(key, varValue);
		    }
		}
		//mapping of key is not a variable
		else{
		    //if incoming value is variable, set incoming variable to current mapping
		    if(varValue instanceof VarType)
			this.getMatches(this.getRight()).put(varValue, this.getMatches(this.getLeft()).get(key));
		    
		    //incoming value is not variable, check equality
		    else{
			if(!(left.get(key).equals(this.getMatches(this.getLeft()).get(key))))
			    return false;
		    }
		}
	    }
	    //key does not contain mapping, put in varValue (variable or not)
	    else
		this.getMatches(this.getLeft()).put(key, varValue);
	}
	
	for(Type key : right.keySet()){
	    //get the key mapping 
	    Type varValue = right.get(key);
	    //if key mapped to variable
	    if(varValue instanceof VarType){
		//if variable is mapped to something, get it
		if(this.getMatches(this.getLeft()).containsKey(varValue)){
		    varValue = this.getMatches(this.getLeft()).get(varValue);   
		}
	    }
	    
	    //if current Type contains this key
	    if(this.getMatches(this.getRight()).containsKey(key)){
		//if the mapping of the key is a variable
		if(this.getMatches(this.getRight()).get(key) instanceof VarType){
		   //if the incoming value is a variable, return false if mapping is not equal
		    if(varValue instanceof VarType){
			if(!(varValue.equals(this.getMatches(this.getRight()).get(key))))
			    return false; 
		    }    
		    //otherwise incoming value is not variable, set variable mapping to incoming value
		    //set current mapping to this value 
		    else{
			this.getMatches(this.getLeft()).put(this.getMatches(this.getRight()).get(key), varValue);
			this.getMatches(this.getRight()).replace(key, varValue);
		    }
		}
		//mapping of key is not a variable
		else{
		    //if incoming value is variable, set incoming variable to current mapping
		    if(varValue instanceof VarType)
			this.getMatches(this.getLeft()).put(varValue, this.getMatches(this.getRight()).get(key));
		    
		    //incoming value is not variable, check equality
		    else{
			if(!(right.get(key).equals(this.getMatches(this.getRight()).get(key))))
			    return false;
		    }
		}
	    }
	    //key does not contain mapping, put in varValue (variable or not)
	    else
		this.getMatches(this.getRight()).put(key, varValue);
	}
	
	
	//OLD MATCHING CODE
	for(Type key : left.keySet()){
	    if(this.getMatches(this.getLeft()).containsKey(key)){
		if(!(left.get(key).equals(this.getMatches(this.getLeft()).get(key))))
		    return false;
	    }
	    this.getMatches(this.getLeft()).put(key, left.get(key));
	}
	
	for(Type key : right.keySet()){
	    if(this.getMatches(this.getRight()).containsKey(key)){
		if(!(right.get(key).equals(this.getMatches(this.getRight()).get(key))))
		    return false;
	    }
	    this.getMatches(this.getRight()).put(key, right.get(key));
	}
	finalAlignments();
	return true;
    }
        
    //Returns the new Type based upon the mapping for a given type T
    /**
     * Helper method for GetAlignedTypeHelper. Sets the new compositeType side to the correct match.
     * @param oldTypeParent The parent CompositeType
     * @param oldTypeSide The child CompositeType, either the left or the right.
     * @return The mapped side for the oldTypeSide. 
     */
    private Type setAlignments(Type oldTypeParent, Type oldTypeSide){
	Type newSide;
	if (this.getMatches(oldTypeParent).containsKey(oldTypeSide)) {
		Type isVarType = this.getMatches(oldTypeParent).get(oldTypeSide);
		while(isVarType instanceof VarType){
		    if(this.getMatches(this.getLeft()).containsKey(isVarType))
			isVarType = this.getMatches(this.getLeft()).get(isVarType);
		    
		    else if(this.getMatches(this.getRight()).containsKey(isVarType))
			isVarType = this.getMatches(this.getRight()).get(isVarType);
		    
		    else
			break;
		}
                newSide = isVarType;
            } 
	    else {
                newSide = oldTypeSide;
            }
	return newSide;
        }
    /**
     * Helper function for getAlignedType. Keeps track of the parent compositeType so that it can set alignments properly
     * @param oldtype the old compositeType to be worked on
     * @param oldTypeParent the parent compositeType
     * @return a new compositeType with the most concrete mappings. 
     */
    private Type getAlignedTypeHelper(Type oldtype, Type oldTypeParent){
	if(oldtype instanceof CompositeType){
	    Type oldLeft = ((CompositeType) oldtype).getLeft();
	    Type oldRight = ((CompositeType) oldtype).getRight();
	    Type newLeft;
	    Type newRight;
	    if (oldLeft instanceof CompositeType) {
		newLeft = getAlignedTypeHelper(((CompositeType)oldLeft), oldTypeParent);
	    } else {
		newLeft = setAlignments(oldTypeParent, oldLeft);
	    }
	    if (oldRight instanceof CompositeType) {
		newRight = getAlignedTypeHelper(((CompositeType)oldRight), oldTypeParent);
	    } 
	    else {
		newRight = setAlignments(oldTypeParent, oldRight);
	    }
	    return new CompositeType(newLeft, newRight);
	}
	
	return oldtype;
    }
    
    /**
     * Creates a new Type that is based on its matches.
     * This method will create a type as concrete as possible, and continue to map until no more mappings are possible. 
     * @param oldtype The old compositeType that has abstract components
     * @return The new compositeType that is as concrete as possible. 
     */
    public Type getAlignedType(Type oldtype) {
	return getAlignedTypeHelper(oldtype, oldtype);
    }
    
    
    //Flips a matchPair to make the first function the second, and second first
    //This was needed for doing RtoL, as the matchPair will do the logic correctly but
    //reverse the ordering, so we have to fix the ordering to what we originally did

    /**
     * Flips the matchPair class. Sets the left Type and left matches as right, and right Type and right matches as left.
     * Useful for when needing to compare Types in the opposite direction if matching in the first direction failed. 
     * @return A new MatchPair that is flipped. 
     */
    
    public MatchPair flip(){
	return new MatchPair(this.getRight(), this.getLeft(), this.getMatches(this.getRight()), this.getMatches(this.getLeft()));
    }
    
    
}
