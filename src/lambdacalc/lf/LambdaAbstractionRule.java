/*
 * LambdaAbstractionRule.java
 *
 * Created on June 5, 2007, 8:36 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;


/**
 *
 * @author champoll
 */
public class LambdaAbstractionRule extends CompositionRule {
    public static final LambdaAbstractionRule INSTANCE
            = new LambdaAbstractionRule();
    
    private LambdaAbstractionRule() {
        super("Lambda abstraction");
    }
    
    /**
     * As a side effect, determines index and body.
     */
    public boolean isApplicableTo(Nonterminal node) {
        
        if (node.size() != 2) return false;

        if (node.getLeftChild() instanceof BareIndex) {
            return !(node.getRightChild() instanceof BareIndex);
        } else {
            return (node.getRightChild() instanceof BareIndex);
        }
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g) throws MeaningEvaluationException {
     
        if (!this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The lambda abstraction rule is only " +
                    "applicable on a nonterminal that has exactly " +
                    "two children of which one is a bare index.");
        }

        BareIndex index = null;
        LFNode body = null;

        LFNode left = node.getLeftChild();
        LFNode right = node.getRightChild();
        
        if (left instanceof BareIndex) {
            index = (BareIndex) left;
            body = right;
        } else {
            index = (BareIndex) right;
            body = left;
        }
        
        Var var;
        
        // Get a fresh variable based on the meaning that we know we will eventually get
        try {
            Expr bodyMeaning = body.getMeaning();
            try { bodyMeaning = bodyMeaning.simplifyFully(); } catch (TypeEvaluationException e) {} // shouldn't throw since getMeaning worked
            var = bodyMeaning.createFreshVar();
        
        // But if we can't get a meaning, choose a default variable
        } catch (MeaningEvaluationException mee) {
            var = new Var("x", lambdacalc.logic.Type.E, false);
        }

        // Copy the assignment function being given to us and add the
        // new mapping from the bare index to a fresh variable.
        AssignmentFunction g2 = (g == null ? new AssignmentFunction() : new AssignmentFunction(g));
        g2.put(index, var);
        
        return new Lambda(var, new MeaningBracketExpr(body, g2), true);
    }
}


