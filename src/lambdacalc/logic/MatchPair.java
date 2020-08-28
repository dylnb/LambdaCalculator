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

//Class that contains the Type and whether it was visited, and which side from the parent it comes from. 
class Node{
    private Type node;
    //true = left, false = right
    private boolean side;
    
    public Node(Type t, boolean side){
	this.node = t;
	this.side = side;
    }
    
    public Type getNode(){
	return node;
    }
    
    public boolean getSide(){
	return side;
    }
    
    @Override
    public boolean equals(Object n){
	if(n instanceof Node)
	    return node.equals(((Node) n).getNode());
	return false;
    }
}
//Bidirectional Graph class to map all variables to other variables. Automatically maps from left side to right side
//Later in DFS all variables will be mapped properly. 
class Graph{
    private ArrayList<ArrayList <Node>> t;
    
    public Graph(){
	t = new ArrayList<ArrayList<Node>>();
	t.add(new ArrayList<Node>());
    }
    
    public ArrayList<ArrayList <Node>> getGraph(){
	return t;
    }
    //checks if a node is in the graph as a key to point to values
    public boolean containsKey(Node key){
	for(Node keys : t.get(0)){
	    if(keys.getNode() == key.getNode())
		return true;
	}
	return false;
    }
    //checks if a key already maps to value
    public boolean containsValue(Node key, Node value){
	for(Node keys : t.get(keyIndex(key))){
	    if(keys.getNode() == value.getNode())
		return true;
	}
	return false;
    }
    
    //returns the index of the value list for a given key
    public int keyIndex(Node key){
	int k = t.get(0).indexOf(key);
	k += 1;
	return k;
    }
    
    private void addEdgeBiDirectional(Node key, Node value){
	if(containsKey(key)){
	    int index = keyIndex(key);
	    if(!(containsValue(key, value)))
		t.get(index).add(value);
	}
	else{
	    t.get(0).add(key);
	    t.add(keyIndex(key), new ArrayList<Node>());
	    t.get(keyIndex(key)).add(value);
	}
    }
    
    //adds an edge from a key to a value, and vice versa via BiDirectional. 
    public void addEdge(Node key, Node value){
	if(containsKey(key)){
	    int index = keyIndex(key);
	    if(!(containsValue(key, value)))
		t.get(index).add(value);
	}
	else{
	    t.get(0).add(key);
	    t.add(keyIndex(key), new ArrayList<Node>());
	    t.get(keyIndex(key)).add(value);
	}
	addEdgeBiDirectional(value, key);
    }
}


public class MatchPair
{
    private Type left;
    private Type right;
    private HashMap<Type, Type> leftMatches;
    private HashMap<Type, Type> rightMatches;
    private Graph g;
    
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
	this.g = new Graph();
    }
    
    /**
     * Constructs a MatchPair class with already provided matches
     * @param l the left, or function, Type
     * @param r the right, or argument, Type
     * @param leftMatches the list of matches from the left Type to the right Type
     * @param rightMatches the list of matches from the right Type to the left Type
     */
    public MatchPair(Type l, Type r, HashMap<Type, Type> leftMatches, HashMap<Type, Type> rightMatches, Graph graph){
	this.left = l;
	this.right = r;
	this.leftMatches = leftMatches;
	this.rightMatches = rightMatches;
	this.g = graph;
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
    
    //returns the graph of variable mappings. 
    public Graph getGraph(){
	return this.g;
    }
    
    //merges subgraph into current parent graph
    public void mergeGraphs(Graph g){
	for(Node key: g.getGraph().get(0)){
	    for(Node value : g.getGraph().get(g.keyIndex(key))){
		this.getGraph().addEdge(key, value);
	    }
	}
    }
    
    /**
     * Sets a match pair for the given type. Either adds to hashMap of mappings or to graph of variable mappings.
     * @param side The Type to add the match pair to
     * @param key The variable being mapped
     * @param value The value of the variable
     */
    public void setMatches(Type side, Type key, Type value){
	if(side == this.left){
	    if(value instanceof VarType)
		this.getGraph().addEdge(new Node(key, true), new Node(value, false));
	    else
		this.leftMatches.put(key, value);
	}
	else{
	    if(value instanceof VarType)
		this.getGraph().addEdge(new Node(key, false), new Node(value, true));
	    else
		this.rightMatches.put(key, value);
	}
    }
    
    //checks for a key in a hashmap via reference instead of .equals (as all instances of a variable in a Complex Type are the same)
    private boolean isKey(HashMap<Type, Type> map, Type key){
	for(Type keys : map.keySet()){
	    if(key == keys)
		return true;
	}
	return false;
    }
    
    //Given a final concrete, sets the key to map to that final Type
    private boolean setMapping(Node key, Type t){
	boolean passing = true;
	if(key.getNode() != t){
	    
	    //If the key is in the left, and mapped to a variable, change it to map to this concrete
	    if(isKey(this.getMatches(this.getLeft()), key.getNode())){
		if(this.getMatches(this.getLeft()).get(key.getNode()) instanceof VarType)
		    this.getMatches(this.getLeft()).replace(key.getNode(), t);
		else 
		    passing = this.getMatches(this.getLeft()).get(key.getNode()).equals(t);
	    }
	    
	    //If the key is in the right, and mapped to a variable, change it to map to this concrete. 
	    else if(isKey(this.getMatches(this.getRight()), key.getNode())){
		if(this.getMatches(this.getRight()).get(key.getNode()) instanceof VarType)
		    this.getMatches(this.getRight()).replace(key.getNode(), t);
		else 
		    passing = this.getMatches(this.getRight()).get(key.getNode()).equals(t);;
	    }
	    
	    //If the key is not in left or right, add it to the right hashmap based on the side it is from
	    else {
		if(key.getSide())
		    this.getMatches(this.getLeft()).put(key.getNode(), t);
		else
		    this.getMatches(this.getRight()).put(key.getNode(), t);
	    }
	}
	return passing;
    }
    
    //set all vars to concrete type, if exists. 
    private boolean DFSSetterHelper(Graph graph, ArrayList<Node> g, Type t, ArrayList<Type> isVisited){
	boolean passing = true;
	for(Node key : g){
	    if(!passing) return passing;
	    
	    if(!(isVisited.contains(key.getNode()))){
		isVisited.add(key.getNode());
		passing = this.setMapping(key, t);
		if(graph.containsKey(key))
		    passing = DFSSetterHelper(graph, graph.getGraph().get(graph.keyIndex(key)), t, isVisited);
	    }
	}
	return passing;
    }
    
    //Sets the graph variables to the given concrete types in finalMappings, assuming graph is DFS'd the same 
    private boolean DFSSetter(Graph g, ArrayList<Type> finalMappings){
	ArrayList<Type> isVisited = new ArrayList<Type>();
	boolean passing = true;
	int i = 0;
	for(Node key : g.getGraph().get(0)){
	    if(!passing) return passing;
	    
	    if(!(isVisited.contains(key.getNode()))){
		isVisited.add(key.getNode());
		passing = this.setMapping(key, finalMappings.get(i));
		passing = DFSSetterHelper(g, g.getGraph().get(g.keyIndex(key)), finalMappings.get(i), isVisited);
		i += 1;
	    }
	}
	return passing;
    }
    
    //create DFS to set all nodes to concrete item. 
    private Type DFSConcreteHelper(Graph graph, ArrayList<Node> g, Type concrete, ArrayList<Type> isVisited){
	for(Node key : g){
	    if(!(isVisited.contains(key.getNode()))){
		isVisited.add(key.getNode());
		if(this.isKey(this.getMatches(this.getLeft()), key.getNode()))
		    concrete = this.getMatches(this.getLeft()).get(key.getNode());
		else if(this.isKey(this.getMatches(this.getRight()), key.getNode()))
		    concrete = this.getMatches(this.getRight()).get(key.getNode());
		
		if(graph.containsKey(key))
		    concrete = DFSConcreteHelper(graph, graph.getGraph().get(graph.keyIndex(key)), concrete, isVisited);
	    }
	}
	return concrete;
    }
    
    //Returns a list of concrete types for every component of a graph. 
    private ArrayList<Type> DFSConcrete(Graph g){
	ArrayList<Type> isVisited = new ArrayList<Type>();
	ArrayList<Type> concretes = new ArrayList<Type>();
	for(Node key : g.getGraph().get(0)){
	    if(!(isVisited.contains(key.getNode()))){
		isVisited.add(key.getNode());
		Type c = DFSConcreteHelper(g, g.getGraph().get(g.keyIndex(key)), null, isVisited);
		if(c == null)
		    if(this.isKey(this.getMatches(this.getLeft()), key.getNode()))
			concretes.add(this.getMatches(this.getLeft()).get(key.getNode()));
		    else if(this.isKey(this.getMatches(this.getRight()), key.getNode()))
			concretes.add(this.getMatches(this.getRight()).get(key.getNode()));
		    else
			concretes.add(key.getNode());
		else
		    concretes.add(c);
	    }
	}
	return concretes;
    }
 
    //Calls DFS on the graph to find the final concrete types and then maps it, assuming all mappings pass. 
    private boolean finalAlignments(){
	ArrayList<Type> finalConcretes = DFSConcrete(this.getGraph());
	boolean finalPass = DFSSetter(this.getGraph(), finalConcretes);
	return finalPass;
	
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
    public boolean insertMatch(HashMap<Type, Type> left, HashMap<Type, Type> right, Graph g){
	
	for(Type key : left.keySet()){
	    //get the key mapping 
	    Type varValue = left.get(key);
	    
	    //if key mapped to variable, ignore
	    if(varValue instanceof VarType)
		continue;
	    
	    //otherwise check if contains this key, and check for equality or map it. 
	    else if(this.getMatches(this.getLeft()).containsKey(key)){
		if(!(this.getMatches(this.getLeft()).get(key) instanceof VarType)){
		    if(!(left.get(key).equals(this.getMatches(this.getLeft()).get(key))))
			return false;
		}
		else
		    this.getMatches(this.getLeft()).replace(key, right.get(key));
	    }
	    else
		this.getMatches(this.getLeft()).put(key, left.get(key));
	} 
	    
	for(Type key : right.keySet()){
	    //get the key mapping 
	    Type varValue = right.get(key);
	    
	    //if key mapped to variable, ignore
	    if(varValue instanceof VarType)
		continue;
	    
	    //otherwise check if contains this key, and check for equality or map it. 
	    else if(this.getMatches(this.getRight()).containsKey(key)){
		if(!(this.getMatches(this.getRight()).get(key) instanceof VarType)){
		    if(!(right.get(key).equals(this.getMatches(this.getRight()).get(key))))
			return false;
		}
		else
		    this.getMatches(this.getRight()).replace(key, right.get(key));
	    }
	    else
		this.getMatches(this.getRight()).put(key, right.get(key));
	}
	
	//merge the old graph into current
	this.mergeGraphs(g);
	
	//grab the final concrete type for all variables and map properly, or return false. 
	boolean test = finalAlignments();
	return test;
    }
        
    //Returns the new Type based upon the mapping for a given type T
    /**
     * Helper method for GetAlignedTypeHelper. Sets the new compositeType side to the correct match.
     * @param oldTypeParent The parent CompositeType
     * @param oldTypeSide The child CompositeType, either the left or the right.
     * @return The mapped side for the oldTypeSide. 
     */
    private Type setAlignments(Type oldTypeParent, Type oldTypeSide){
	Type newSide = oldTypeSide;
	if(isKey(this.getMatches(oldTypeParent), oldTypeSide)) {
		Type isVarType = this.getMatches(oldTypeParent).get(oldTypeSide);
		while(isVarType instanceof VarType){
		    if(isKey(this.getMatches(this.getLeft()), isVarType))
			    isVarType = this.getMatches(this.getLeft()).get(isVarType);
		    
		    else if(isKey(this.getMatches(this.getLeft()), isVarType))
			    isVarType = this.getMatches(this.getRight()).get(isVarType);
		    
		    
		    else
			break;
		}
                newSide = isVarType;
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
            oldtype = ((CompositeType)oldtype).renameVariables(new ArrayList<Type>());
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
	return new MatchPair(this.getRight(), this.getLeft(), this.getMatches(this.getRight()), this.getMatches(this.getLeft()), this.getGraph());
    }
}
