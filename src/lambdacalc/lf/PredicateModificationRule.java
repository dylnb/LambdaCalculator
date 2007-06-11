/*
 * PredicateModificationRule.java
 *
 * Created on June 5, 2007, 3:32 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Type;
import lambdacalc.logic.Expr;
import lambdacalc.logic.ExpressionParser;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.TypeEvaluationException;

/**
 *
 * @author champoll
 */
public class PredicateModificationRule extends CompositionRule {
    public static final PredicateModificationRule INSTANCE 
            = new PredicateModificationRule();
    
    
    private static final ExpressionParser.ParseOptions options
            = new ExpressionParser.ParseOptions
            (true, // single letter identifier mode is on
            true // certain ASCII sequences become special
            );
    
    private static final Expr engine = 
            ExpressionParser.parseAndSuppressExceptions
            ("LX.LY.Lx.[X(x)&Y(x)]", options);

    public PredicateModificationRule() {
        super("Predicate Modification");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        boolean result = false;
        try {
            result = (node.size() == 2
                && node.getLeftChild().getMeaning().getType().equals(Type.ET)
                && node.getRightChild().getMeaning().getType().equals(Type.ET));
        } catch (TypeEvaluationException t) {
            // TODO what do we do? There is a semantic type mismatch
            t.printStackTrace();
        } catch (MeaningEvaluationException m) {
            // TODO maybe we want to compute the types before the meanings
            m.printStackTrace();
        }
        return result;
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g) throws MeaningEvaluationException {
        //TODO don't ignore g
        if (!this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The predicate modification rule is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "two children of type <e,t>.");
        }
        Expr leftMeaning = node.getLeftChild().getMeaning();
        Expr rightMeaning = node.getRightChild().getMeaning();
        
        
        // Simplify the left and right before combining them.
        // simplify() can throw TypeEvaluationException when
        // a type mismatch occurs, but we don't expect this to
        // happen within subnodes.
        // TODO Josh: I copied this comment from FunctionApplication
        // TODO: why don't we expect this to happen?
        try { leftMeaning = leftMeaning.simplify(); } catch (TypeEvaluationException e) {}
        try { rightMeaning = rightMeaning.simplify(); } catch (TypeEvaluationException e) {}

        return new FunApp(new FunApp(engine, leftMeaning), rightMeaning);
        
    }
}

