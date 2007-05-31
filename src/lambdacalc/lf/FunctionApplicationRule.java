package lambdacalc.lf;

import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.TypeEvaluationException;

public class FunctionApplicationRule extends CompositionRule {

    public FunctionApplicationRule() {
        super("Function Application");
    }
    
    public Expr getMeaning(Nonterminal node) throws MeaningEvaluationException {
        if (node.getChildren().size() != 2)
            throw new MeaningEvaluationException("Function application is not applicable on a nonterminal that does not have exactly two children.");
        
        LFNode left = (LFNode)node.getChildren().get(0);
        LFNode right = (LFNode)node.getChildren().get(1);
        
        Expr leftMeaning = left.getMeaning();
        Expr rightMeaning = right.getMeaning();
        
        if (isFunctionOf(leftMeaning, rightMeaning))
            return apply(leftMeaning, rightMeaning);
        if (isFunctionOf(rightMeaning, leftMeaning))
            return apply(rightMeaning, leftMeaning);

        throw new MeaningEvaluationException("The children of the nonterminal " + node.toString() + " are not of compatible types for function application.");
    }
    
    private boolean isFunctionOf(Expr left, Expr right) throws MeaningEvaluationException {
        // Return true iif left is a composite type <X,Y>
        // and right is of type X.
        try {
            if (left.getType() instanceof CompositeType) {
                CompositeType t = (CompositeType)left.getType();
                if (t.getLeft().equals(right.getType()))
                    return true;
            }
            return false;
        } catch (TypeEvaluationException ex) {
            throw new MeaningEvaluationException("A type mismatch exists: " + ex.getMessage());
        }
    }
    
    private Expr apply(Expr left, Expr right) {
        return new FunApp(left, right);
    }
}
