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
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {
        //TODO don't ignore g
        if (node.size() != 1)
            throw new MeaningEvaluationException
                    ("The non-branching node rule is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "one child.");
        
        return new MeaningBracketExpr(node.getChild(0), g);
    }
}
