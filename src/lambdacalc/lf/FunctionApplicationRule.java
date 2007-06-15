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
        
        Expr leftMeaning = left.getMeaning();
        Expr rightMeaning = right.getMeaning();
        
        // Simplify the left and right before combining them.
        // simplify() can throw TypeEvaluationException when
        // a type mismatch occurs, but we don't expect this to
        // happen within subnodes.
        try { leftMeaning = leftMeaning.simplify(); } catch (TypeEvaluationException e) { }
        try { rightMeaning = rightMeaning.simplify(); } catch (TypeEvaluationException e) { }
        
        if (isFunctionOf(leftMeaning, rightMeaning))
            return apply(leftMeaning, rightMeaning);
        if (isFunctionOf(rightMeaning, leftMeaning))
            return apply(rightMeaning, leftMeaning);

        throw new MeaningEvaluationException("The children of the nonterminal "
                + (node.getLabel() == null ? node.toString() : node.getLabel())+ " are not of compatible types for function " +
                "application.");
    }
    
    private boolean isFunctionOf(Expr left, Expr right) 
    throws MeaningEvaluationException {
        // Return true iff left is a composite type <X,Y>
        // and right is of type X.
        try {
            if (left.getType() instanceof CompositeType) {
                CompositeType t = (CompositeType)left.getType();
                if (t.getLeft().equals(right.getType()))
                    return true;
            }
            return false;
        } catch (TypeEvaluationException ex) {
            throw new MeaningEvaluationException("A type mismatch has occurred: " 
                    + ex.getMessage());
        }
    }
    
    private Expr apply(Expr left, Expr right) {
        return new FunApp(left, right);
    }

}
