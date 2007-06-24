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
    LFNode node;
    AssignmentFunction g;
    
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
