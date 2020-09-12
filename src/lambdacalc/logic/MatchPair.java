/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.logic;

import java.util.*;

/**
 * Class containing pairings of matches of two types given
 * @author Raef Khan
 */

/**
 * Class containing Type and whether it is from Function or Argument. This class is used as the nodes for the graph class.
 * @author Raef Kham
 */
class Node{
    private Type node;
    //true = left, false = right
    private boolean side;
    
    /**
     * Constructor for Node Class. Takes in the type and whether it is from the function or argument.
     * @param t The Type for this node.
     * @param side set to TRUE if from function or left, FALSE if from argument or right.
     */
    public Node(Type t, boolean side){
	this.node = t;
	this.side = side;
    }
    
    /**
     * Returns the Type in this node.
     * @return The Type of the node.
     */
    public Type getNode(){
	return node;
    }
    
    /**
     * Returns whether Node is part of function or argument.
     * @return TRUE if from function or left, FALSE if from argument or right.
     */
    public boolean getSide(){
	return side;
    }
    
    /**
     * Overrides Object Equals function. Returns equality if the two types within nodes are equal. 
     * @param n
     * @return 
     */
    @Override
    public boolean equals(Object n){
	if(n instanceof Node)
	    return node.equals(((Node) n).getNode());
	return false;
    }
}

/**
 * Graph Class. A bidirectional graph that maps all variables from each side of the unification to each other. 
 * The graph is made of an arraylist of arraylists, where the outer arraylist[0] is the list of nodes or keys in the graph,
 * and each subsequent arraylist[1-n] are arraylists containing the neighbors of node n, where n is the index from arraylist[0][n-1]. 
 * Ex. matching <a, b, b> to <d, d, e> will return a graph mapping a <-> d <-> b <-> e.
 * @author Raef Khan
 */
class Graph{
    private ArrayList<ArrayList <Node>> t;

    /**
     * Constructor for the graph. Creates the arraylist of arraylists and adds the arraylist of keys to the 0th position. 
     */
    public Graph(){
	t = new ArrayList<ArrayList<Node>>();
	t.add(new ArrayList<Node>());
    }
    
    /**
     * Returns the graph.
     * @return the graph T. 
     */
    public ArrayList<ArrayList <Node>> getGraph(){
	return t;
    }
    /**
     * Checks if graph currently contains a node as a key.
     * @param key the Node to be contained as a key
     * @return true if the key exist, false otherwise. 
     */
    public boolean containsKey(Node key){
	for(Node keys : t.get(0)){
	    if(keys.getNode() == key.getNode())
		return true;
	}
	return false;
    }
    /**
     * Checks if a key has a neighbor value. This checks if one variable has already been mapped to another.
     * @param key The Node key, or the variable Type mapping towards. 
     * @param value The value, or the variable Type to be mapped to.
     * @return True if the Key maps to Value, false otherwise
     */
    public boolean containsValue(Node key, Node value){
	for(Node keys : t.get(keyIndex(key))){
	    if(keys.getNode() == value.getNode())
		return true;
	}
	return false;
    }
    
    /**
     * Returns the index of a Key. Returns the correct index for the list of neighbors for a given node, as there is an offset of 1. 
     * @param key the Node for which we want the list of neighbors. 
     * @return The index of the list of neighbors for a given key. 
     */
    //returns the index of the value list for a given key
    public int keyIndex(Node key){
	int k = t.get(0).indexOf(key);
	k += 1;
	return k;
    }
    
    /**
     * Helper Method for addEdge(). Adds the edge between two variables in the opposite direction. 
     * @param key The variable to be given the edge.
     * @param value The variable to be added to a key's edgeList. 
     */
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
    
    /**
     * Adds Edges for given variables. Takes two variables and adds edges to both of them, using addEdgeBiDirectional().
     * @param key The first variable to be mapped to.
     * @param value The second variable to be mapped to.
     */
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

/**
 * Class that contains matches for two Types being Unified. Contains the two types, and 
 * hashmaps for variables to the most-general-unifier for each type. 
 * @author Raef Khan
 */
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

    /**
     * Gets the function, or left, Type
     * @return the left Type
     */
    public Type getLeft(){
	return this.left;
    }

    /**
     * Gets the argument, or right, Type
     * @return the right Type
     */
    public Type getRight(){
	return this.right;
    }

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
    
    /**
     * Returns the graph for variable to variable mappings. 
     * @return The graph containing the variable mappings.
     */
    //returns the graph of variable mappings. 
    public Graph getGraph(){
	return this.g;
    }
    
    /**
     * Merges a graph from a subType to a parentType. This is used for compositeType and productType after mapping each of their
     * subparts, to then add those mappings into the parent graph.
     * @param g The graph of the subType to be merged.
     */
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
    
    /**
     * Overriding Map.containsKey(key). Returns whether a key is within a hashmap using reference rather than equality. 
     * This is due to the allowance of multiple variables of the same name to exist in both Types attempting to unify, and checking
     * for variable mappings within one half using equality could cause issues. Currently, all variables for each Type are re-referenced
     * to the first occurance of the variable name, so that this method can function properly. A future fix is to rename 
     * the variables for each Type to some internal, fully different, variable names, and then rename them back at the end before returning. 
     * @param map The hashmap to be checked.
     * @param key The variable key.
     * @return True if the key is the same reference as the keyset within the hashmap, false otherwise. 
     */
    //checks for a key in a hashmap via reference instead of .equals (as all instances of a variable in a Complex Type are the same)
    private boolean isKey(HashMap<Type, Type> map, Type key){
	for(Type keys : map.keySet()){
	    if(key == keys)
		return true;
	}
	return false;
    }
    
    /**
     * Helper Method for DFSSetter. Adds the mapping of a variable to a final Most-General-Unifier found from DFSConcrete to the proper hashmap.
     * @param key The variable from the graph to get the final mapping.
     * @param t The MGU for the variable.
     * @return True if there is no conflict of variable to multiple concretes (no one to many mappings), false otherwise. 
     */
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
		    passing = this.getMatches(this.getRight()).get(key.getNode()).equals(t);
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
    
    /**
     * Helper Method for DFSSetter. Continues DFS for each neighbor of a starting variable, to map to the final MGU.
     * @param graph The overall graph of variables
     * @param g The arrayList of neighbors for a given variable from DFSSetter
     * @param t The final MGU
     * @param isVisited The arrayList checking if a variable has already had DFS done on it. 
     * @return True if there are no conflicts in mapping variables to their MGU, false otherwise. 
     */
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
    
    /**
     * DFS Method that sets variables to their final Most-General-Unifier (MGU). Takes a list of mappings from DFSConcrete and begins DFS again in
     * the same order to preserve the correct final mappings. One way to make this more secure is to use another HashMap from
     * variables to their final MGU and then merge that into the overall hashmaps. 
     * @param g The overall graph of variables
     * @param finalMappings The list of MGU's for each variable, based on index for each component of the graph
     * @return True if there are no conflicts for variable final mappings, false otherwise. 
     */
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
    
    /**
     * Helper method for DFSConcrete. Continues DFS on each neighbor of a given variable, checking for an MGU.
     * @param graph The overall graph.
     * @param g The list of neighbors for a variable from DFSConcrete.
     * @param concrete The final MGU, starts as null (no MGU found)
     * @param isVisited List of nodes that have been visited for DFS
     * @return Concrete, either a final MGU, or null if none are found (and thus the variable is the final MGU). 
     */
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
    
    /**
     * DFS Method that finds the final MGU for each component of a graph. Runs DFS on the graph, 
     * and for every component (as the graph may be disconnected), chooses a final MGU to map every variable to. 
     * DFSSetter will check if there conflicts with variables mapping to multiple concrete types. 
     * @param g The graph of variables.
     * @return A list of MGU's for each component of the graph. 
     */
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
 
    
    /**
     * Sets variables to their final Most-General-Unifier. Calls DFSConcrete and DFSSetter.
     * Ex. <a, e> mapping to <b, b> will return a -> e and b -> e. 
     * @return True if no variable conflict occurs, false otherwise. 
     */
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


    /**
     * Flips the matchPair class. Sets the left Type and left matches as right, and right Type and right matches as left.
     * Useful for when needing to compare Types in the opposite direction if matching in the first direction failed. 
     * @return A new MatchPair that is flipped. 
     */
    
    public MatchPair flip(){
	return new MatchPair(this.getRight(), this.getLeft(), this.getMatches(this.getRight()), this.getMatches(this.getLeft()), this.getGraph());
    }
}
