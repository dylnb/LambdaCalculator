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
        //TODO don't ignore g
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
        
        Expr bodyMeaning = body.getMeaning();
        
        // Simplify the body.
        // simplify() can throw TypeEvaluationException when
        // a type mismatch occurs, but we don't expect this to
        // happen within subnodes.
        // TODO Josh: I copied this comment from FunctionApplication
        // TODO: why don't we expect this to happen?
        try { bodyMeaning = bodyMeaning.simplify(); } catch (TypeEvaluationException e) {}
 
        Var var = bodyMeaning.createFreshVar();
        
        // update assignment function
        g.put(index, var);
        
        // apply it
        bodyMeaning = bodyMeaning.replaceAll(g);
        return new Lambda(var, bodyMeaning, true);
    }
}


