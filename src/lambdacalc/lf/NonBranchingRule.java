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
        // Count up the children of node that are not DummyTerminal's. If there's
        // just one, then we are a non-branching node.
        int nChildren = 0;
        for (int i = 0; i < node.size(); i++)
            if (!(node.getChild(i) instanceof DummyTerminal))
                nChildren++;
        return nChildren == 1;
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {
        if (!isApplicableTo(node))
            throw new MeaningEvaluationException
                    ("The non-branching node rule is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "one child.");
        
        // Find first non-DummyTerminal.
        for (int i = 0; i < node.size(); i++)
            if (!(node.getChild(i) instanceof DummyTerminal))
                return new MeaningBracketExpr(node.getChild(i), g);
       
        throw new RuntimeException(); // not reachable
    }
}
