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
 * MeaningBracketExpr.java
 */

package lambdacalc.lf;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private boolean topDown;
    
    public MeaningBracketExpr(LFNode node, AssignmentFunction g, boolean topDown) {
        this.node = node;
        this.g = g;
        this.topDown = topDown;
        if (g == null)
            throw new IllegalArgumentException("g cannot be null");
    }

    /**
     * @param g may be null
     */
    public MeaningBracketExpr(LFNode node, AssignmentFunction g) {
        this.node = node;
        this.g = g;
        this.topDown = (g != null);
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
    
    protected String toString(int mode) {
        String label = node.getLabel();
        if (label != null) {
            if (node.hasIndex()) {
                if (mode == TXT)
                    label += "_" + node.getIndex();
                else if (mode == HTML)
                    label += "<sub>" + node.getIndex() + "</sub>";
                else // mode == LATEX
                    label += "_{" + node.getIndex() + "}";
            }
        } else {
            label = node.toString();
        }

        if (mode == HTML) {
//            label = escapeHTML("&#10218;" + label + "&#10219;");
            label = escapeHTML(label + "&#8242;"); // prime (can also use &prime; )
        } else if (mode == TXT) {
//            label = "⟪" + label + "⟫";
            label = label + "'";
        } else { // mode == LATEX
//            label = "\\langle\\!\\!\\langle" + label + "\\rangle\\!\\!\\rangle";
            label = label + "'";
        }
//        if (g != null) {
//            if (mode == TXT) {
//                label += "^";
//            } else if (mode == HTML) {
//                label += "<sup>";
//            } else { // mode == LATEX
//                label += "^{";
//            }
//            label += "g";
//            for (Iterator i = g.keySet().iterator(); i.hasNext(); ) {
//                GApp gapp = (GApp)i.next();
//                Var var;
//                try {
//                    var = (Var) g.get(gapp.getIndex(),gapp.getType());
//                } catch (TypeEvaluationException ex) {
//                    // we don't expect this to happen because calling getType()
//                    // on GApp never fails
//                    throw new RuntimeException();
//                }
//                label += " " + var + "/" + gapp.getIndex();
//            }
//            if (mode == HTML) {
//                label += "</sup>";
//            } else if (mode == LATEX) {
//                label += "}";
//            }
//        }
        
        return label;
    }

    public Type getType() throws TypeEvaluationException {
        try {
            return evaluate().getType();
        } catch (MeaningEvaluationException mee) {
            throw new TypeEvaluationException("The type of " + this + " could not be determined: " + mee.getMessage());
        }
    }
    
    /**
     * Evaluates this expression by returning the denotation of the indicated node,
     * with all meaning brackets removed.
     */
    public Expr evaluate() throws TypeEvaluationException, MeaningEvaluationException {
        // When we evaluate this node, whether or not we can pass the assignment
        // function down is determined by whether this node was created for
        // top-down or bottom-up evaluation.
        
        if (topDown) {
            // In top-down evaluation, the assignment function contains variables
            // guaranteed not to conflict with variables higher up, and we pass
            // g down because the subnodes must choose variables that don't
            // conflict with the ones we've chosen higher up.
            
            Expr e = node.getMeaning(g); // with args is the top-down method
            e = replaceAllMeaningBrackets(e);
            return e;
            
        } else {
            // In bottom-up evaluation, the variables in the assignment function
            // have been chosen to not conflict with variables in use in the
            // simplified subexpression. Thus, we must get the subexpression's
            // denotation, replace any meaning brackets in it, simplify it,
            // and then do a replacement of g(n) according to the assignment
            // function.
            // See LambdaAbstractionRule.applyTo(...).
            
            Expr e = node.getMeaning(); // no args is the bottom-up method
            
            e = replaceAllMeaningBrackets(e);
            
            e = e.simplifyFully();
            
            if (g != null)
                e = g.applyTo(e);
            
            return e;
        }
    }
    
    protected boolean equals(Expr e, boolean collapseBoundVars, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {
        return (e instanceof MeaningBracketExpr) && (node == ((MeaningBracketExpr)e).node);
    }
    
    public static boolean hasMeaningBrackets(Expr expr) {
        ArrayList objs = new ArrayList();
        findMeaningBrackets(expr, objs);
        return objs.size() > 0;
    }
    
    public static Expr replaceAllMeaningBrackets(Expr expr) 
    throws TypeEvaluationException, MeaningEvaluationException {
        // First get a list of all MeaningBracketExpr objects in expr.
        ArrayList objs = new ArrayList();
        findMeaningBrackets(expr, objs);
        
        // Then for each replace it with its evaluated value.
        for (Iterator i = objs.iterator(); i.hasNext(); ) {
            MeaningBracketExpr mbe = (MeaningBracketExpr)i.next();
            Expr value = mbe.evaluate();
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
            output.writeBoolean(mb.getAssignmentFunction() != null);
            if (mb.getAssignmentFunction() != null)
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
            
            AssignmentFunction g = null;
            
            if (input.readBoolean()) {
	            g = new AssignmentFunction();
    	        g.readFromStream(input);
    	    }
            
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
    
    public Expr createAlphatypicalVariant(HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        try {
            return this.evaluate().createAlphatypicalVariant(alignments, variablesInUse, updates);
        } catch (TypeEvaluationException ex) {
            Logger.getLogger(MeaningBracketExpr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MeaningEvaluationException ex) {
            Logger.getLogger(MeaningBracketExpr.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        throw new java.io.IOException("This class cannot be serialized.");
    }
   
}
