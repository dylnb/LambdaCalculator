/*
 * NonBranchingRule.java
 *
 * Created on June 5, 2007, 3:18 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.TypeEvaluationException;

/**
 *
 * @author champoll
 */
public class NonBranchingRule extends CompositionRule {
    public static final NonBranchingRule INSTANCE 
            = new NonBranchingRule();

    private NonBranchingRule() {
        super("Non-Branching Node");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        return node.size() == 1;
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g) throws MeaningEvaluationException {
        //TODO don't ignore g
        if (node.size() != 1)
            throw new MeaningEvaluationException
                    ("The non-branching node rule is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "one child.");
        
        Expr childMeaning = node.getChild(0).getMeaning();
        
        
        // Simplify the left and right before combining them.
        // simplify() can throw TypeEvaluationException when
        // a type mismatch occurs, but we don't expect this to
        // happen within subnodes.
        // TODO Josh: I copied this comment from FunctionApplication
        // TODO: why don't we expect this to happen?
        try { childMeaning = childMeaning.simplify(); 
        } catch (TypeEvaluationException e) {
        }

        return childMeaning;
        
    }
}
