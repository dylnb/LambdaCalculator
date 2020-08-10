/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

import java.util.*;

/**
 *
 * @author Raefno1
 */
public class MatchPair
{
    private Type left;
    private Type right;
    private HashMap<Type, Type> leftMatches;
    private HashMap<Type, Type> rightMatches;
    
    public MatchPair(Type l, Type r){
	this.left = l;
	this.right = r;
	this.leftMatches = new HashMap<Type, Type>();
	this.rightMatches = new HashMap<Type, Type>();
    }
    
    public MatchPair(Type l, Type r, HashMap<Type, Type> left, HashMap<Type, Type> right){
	this.left = l;
	this.right = r;
	this.leftMatches = left;
	this.rightMatches = right;
    }
    
    //Returns the left half of the two matching functions
    public Type getLeft(){
	return this.left;
    }
    
    //Returns the right half of the two matching functions 
    public Type getRight(){
	return this.right;
    }
    
    //Returns the mapping of variables for a given Type side
    public HashMap<Type, Type> getMatches(Type side){
	if(side == this.left) 
	    return this.leftMatches;
	return this.rightMatches;
    }
    
    //Add a matchPair for a specific side 
    //redundant and will delete later, can just do .getMatches(Type).put(key, value)
    public void setMatches(Type side, Type key, Type value){
	if(side == this.left)
	    this.leftMatches.put(key, value);
	else
	    this.rightMatches.put(key, value);
    }
   
    //Taking the mapping of a subType and adding it to the parents.
    //This adds the mappings for both, e.g. <a1, t> -> <e, a2> 
    //calling this on the mapping of a1, e will place a1 -> e this in <a1, t> mapping
    //calling this on the mapping of t, a2 will place a2 -> t this in <e, a2> mapping
    public boolean insertMatch(HashMap<Type, Type> left, HashMap<Type, Type> right){
	
	//cannot rewrite due to .getLeft, .getRight, unless changed. 
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
	
	return true;
    }
    
    
    //Returns the new Type based upon the mapping for a given type T
    public Type getAlignedType(CompositeType oldtype) {
        Type oldLeft = oldtype.getLeft();
        Type oldRight = oldtype.getRight();
        Type newLeft;
        Type newRight;
        if (oldLeft instanceof CompositeType) {
            newLeft = getAlignedType(((CompositeType)oldLeft));
        } else {
            if (this.getMatches(oldtype).containsKey(oldLeft)) {
                newLeft = this.getMatches(oldtype).get(oldLeft);
            } else {
                newLeft = oldLeft;
            }
        }
        if (oldRight instanceof CompositeType) {
            newRight = getAlignedType(((CompositeType)oldRight));
        } else {
            if (this.getMatches(oldtype).containsKey(oldRight)) {
                newRight = this.getMatches(oldtype).get(oldRight);
            } else {
                newRight = oldRight;
            }
        }
        return new CompositeType(newLeft, newRight);
    }
    
    
    //Flips a matchPair to make the first function the second, and second first
    //This was needed for doing RtoL, as the matchPair will do the logic correctly but
    //reverse the ordering, so we have to fix the ordering to what we originally did
    
    public MatchPair flip(){
	return new MatchPair(this.getRight(), this.getLeft(), this.getMatches(this.getRight()), this.getMatches(this.getLeft()));
    }
    
    
}
