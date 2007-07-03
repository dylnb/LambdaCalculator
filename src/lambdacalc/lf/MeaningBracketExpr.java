/*
 * MeaningBracketExpr.java
 */

package lambdacalc.lf;

import java.util.*;
import lambdacalc.logic.*;

/**
 * A meaning-bracketed expression within the Lambda calculus, i.e.
 * [[VP]] used as a placeholder for the denotation of a VP node.
 * These placeholders also hold onto an assignment function.
 * Expressions that use objects of this class cannot be serialized,
 * nor can their toString()'s be parsed back.
 */
public class MeaningBracketExpr extends Expr {
    
    public static final char LEFT_BRACKET = '\uu301a'; //Unicode left white square bracket
    public static final char RIGHT_BRACKET = '\uu301b'; //Unicode right white square bracket
    
    private LFNode node;
    private AssignmentFunction g;
    
    /**
     * @param g may be null
     */
    public MeaningBracketExpr(LFNode node, AssignmentFunction g) {
        this.node = node;
        this.g = g;
    }

    public int getOperatorPrecedence() {
        return 1;
    }
    
    public LFNode getNode() {
        return node;
    }
    
    public AssignmentFunction getAssignmentFunction() {
        return g;
    }
    
    protected String toString(boolean html) {
        String label = node.getLabel();
        if (label != null) {
            if (node.hasIndex()) {
                if (!html)
                    label += "_" + node.getIndex();
                else
                    label += "<sub>" + node.getIndex() + "</sub>";
            }
        } else {
            label = node.toString();
        }
        
        label = escapeHTML("[[" + label + "]]", html);
        
        if (g != null) {
            if (!html)
                label += "^";
            else
                label += "<sup>";
            label += "g";
            for (Iterator i = g.keySet().iterator(); i.hasNext(); ) {
                GApp gapp = (GApp)i.next();
                Var var = (Var)g.get(gapp);
                label += " " + var + "/" + gapp.getIndex();
            }
            if (html)
                label += "</sup>";
        }
        
        return label;
    }

    public Type getType() throws TypeEvaluationException {
        try {
            return node.getMeaning(g).getType();
        } catch (MeaningEvaluationException mee) {
            throw new TypeEvaluationException("The type of " + this + " could not be determined: " + mee.getMessage());
        }
    }
    
    protected boolean equals(Expr e, boolean collapseBoundVars, Map thisMap, Map otherMap, boolean collapseAllVars) {
        return (e instanceof MeaningBracketExpr) && (node == ((MeaningBracketExpr)e).node);
    }
    
    public static Expr replaceAllMeaningBrackets(Expr expr) 
    throws TypeEvaluationException, MeaningEvaluationException {
        // First get a list of all MeaningBracketExpr objects in expr.
        ArrayList objs = new ArrayList();
        findMeaningBrackets(expr, objs);
        
        // Then for each relace it with its evaluated value.
        for (Iterator i = objs.iterator(); i.hasNext(); ) {
            MeaningBracketExpr mbe = (MeaningBracketExpr)i.next();

            Expr value = mbe.node.getMeaning(mbe.g);
            value = replaceAllMeaningBrackets(value);
            value = value.replaceAll(mbe.g); // really we want to replace g(1) with gx/1(1) only, and leave it to someone else to replace gx/1(1) with x
            value = value.simplifyFully();
            
            expr = expr.replace(mbe, value);
        }
        
        return expr;
    }
    
    private static void findMeaningBrackets(Expr expr, ArrayList objs) {
        if (expr instanceof MeaningBracketExpr) {
            objs.add(expr);
        } else {
            List subexprs = expr.getSubExpressions();
            for (Iterator i = subexprs.iterator(); i.hasNext(); )
                findMeaningBrackets((Expr)i.next(), objs);
        }
    }
    
    /**
     * MeaningBracketExpr objects can't be serialized because they have references
     * to nodes in a tree, and it's impossible to serialize and deserialize that
     * object reference. This method and the corresponding readExpr method provide
     * a way to save and load expressions that deep down may contain MeaningBracketExpr
     * objects.
     */
    public static void writeExpr(Expr expr, Nonterminal treeroot, java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // version info
        
        // Get a list of all meaning bracket objects in the expression.
        ArrayList mbs = new ArrayList();
        findMeaningBrackets(expr, mbs);
        
        // Replace each with a special variable, and maintain a mapping
        // of the original meaning bracket exprs to the special varaibles.
        Map replacements = new HashMap();
        for (Iterator i = mbs.iterator(); i.hasNext(); ) {
            MeaningBracketExpr mb = (MeaningBracketExpr)i.next();
            Var key = expr.createFreshVar();
            expr = expr.replace(mb, key);
            replacements.put(mb, key);
        }
        
        // Create a mapping from nodes to paths (and paths to nodes, but we don't need that here).
        Map nodeToPath = new HashMap(), pathToNode = new HashMap();
        getNodePaths(treeroot, nodeToPath, pathToNode, "R");
        
        // Write out the expr that has the meaning brackets replaced with variables.
        expr.writeToStream(output);
        
        // Write out the mapping from special variables to meaning bracket exprs.
        output.writeInt(mbs.size());
        for (Iterator i = mbs.iterator(); i.hasNext(); ) {
            MeaningBracketExpr mb = (MeaningBracketExpr)i.next();
            
            // Write out the meaning bracket. To write the node that it refers
            // to, write out the path to the node.
            output.writeUTF((String)nodeToPath.get(mb.getNode()));
            
            // And write out the assignment function applied to the node.
            mb.getAssignmentFunction().writeToStream(output);
            
            // And the special variable.
            Var key = (Var)replacements.get(mb);
            key.writeToStream(output);
        }
    }
    
    public static Expr readExpr(Nonterminal treeroot, java.io.DataInputStream input) throws java.io.IOException {
        if (input.readByte() != 0)
            throw new java.io.IOException("Data format error.");

        // Create a mapping from nodes to paths (which we don't need) and paths to nodes.
        Map nodeToPath = new HashMap(), pathToNode = new HashMap();
        getNodePaths(treeroot, nodeToPath, pathToNode, "R");
        
        // Read the expr that has the meaning brackets replaced with variables.
        Expr expr = Expr.readFromStream(input);
        
        // Read in the mapping from special variables to meaning brackets.
        Map replacements = new HashMap();
        int n = input.readInt();
        for (int i = 0; i < n; i++) {
            LFNode node = (LFNode)pathToNode.get(input.readUTF());
            
            AssignmentFunction g = new AssignmentFunction();
            g.readFromStream(input);
            
            MeaningBracketExpr mb = new MeaningBracketExpr(node, g);
            
            Var key = (Var)Expr.readFromStream(input);
            
            replacements.put(key, mb);
        }
        
        return expr.replaceAll(replacements);
    }

    private static void getNodePaths(LFNode node, Map nodeToPath, Map pathToNode, String path) {
        nodeToPath.put(node, path);
        pathToNode.put(path, node);
        
        if (node instanceof Nonterminal) {
            Nonterminal nt = (Nonterminal)node;
            for (int i = 0; i < nt.size(); i++)
                getNodePaths(nt.getChild(i), nodeToPath, pathToNode, path + "," + i);
        }
    }
    
    public int hashCode() {
        return node.hashCode();
    }
    
    protected Set getVars(boolean unboundOnly) {
        return new HashSet();
    }

    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        return null;
    }
       
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        return this;
    }
    
    public List getSubExpressions() {
        return new ArrayList();
    }
    
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        return new MeaningBracketExpr(node, g);
    }
    
    
    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        return this;
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        throw new java.io.IOException("This class cannot be serialized.");
    }
   
}
