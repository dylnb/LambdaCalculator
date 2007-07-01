package lambdacalc.lf;

import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.TypeEvaluationException;

public class FunctionApplicationRule extends CompositionRule {
    public static final FunctionApplicationRule INSTANCE 
            = new FunctionApplicationRule();

    private FunctionApplicationRule() {
        super("Function Application");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        if (node.size() != 2)
            return false;
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        try {
            Expr leftMeaning = left.getMeaning();
            Expr rightMeaning = right.getMeaning();

            if (isFunctionOf(leftMeaning, rightMeaning))
                return true;
            if (isFunctionOf(rightMeaning, leftMeaning))
                return true;
        } catch (Exception e) {
        }

        return false;
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g) 
    throws MeaningEvaluationException {
        if (node.size() != 2)
            throw new MeaningEvaluationException("Function application is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "two children.");
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        if (left instanceof BareIndex || right instanceof BareIndex)
            throw new MeaningEvaluationException("The left and right children of a function application node must not be lambda-abstraction indices.");
        
        Expr leftMeaning, rightMeaning;
        try {
            leftMeaning = left.getMeaning();
            rightMeaning = right.getMeaning();
        } catch (MeaningEvaluationException mee) {
            return apply(left, right, g); // when we can't get meanings of subparts, take a default order
        }

        if (isFunctionOf(leftMeaning, rightMeaning))
            return apply(left, right, g);
        if (isFunctionOf(rightMeaning, leftMeaning))
            return apply(right, left, g);

        throw new MeaningEvaluationException("The children of the nonterminal "
                + (node.getLabel() == null ? node.toString() : node.getLabel())+ " are not of compatible types for function " +
                "application.");
    }
    
    private boolean isFunctionOf(Expr left, Expr right) {
        // Return true iff left is a composite type <X,Y>
        // and right is of type X.
        try {
            if (left.getType() instanceof CompositeType) {
                CompositeType t = (CompositeType)left.getType();
                if (t.getLeft().equals(right.getType()))
                    return true;
            }
        } catch (TypeEvaluationException ex) {
        }
        return false;
    }
    
    private Expr apply(LFNode left, LFNode right, AssignmentFunction g) {
        return new FunApp(new MeaningBracketExpr(left, g), new MeaningBracketExpr(right, g));
    }
}
