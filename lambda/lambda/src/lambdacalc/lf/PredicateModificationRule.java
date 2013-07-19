/*
 * PredicateModificationRule.java
 *
 * Created on June 5, 2007, 3:32 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.And;
import lambdacalc.logic.Type;
import lambdacalc.logic.Expr;
import lambdacalc.logic.ExpressionParser;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.Var;

/**
 *
 * @author champoll
 */
public class PredicateModificationRule extends CompositionRule {
    public static final PredicateModificationRule INSTANCE 
            = new PredicateModificationRule();
    
    public static final Var VARIABLE = Var.Z;
    
//    private static final ExpressionParser.ParseOptions options
//            = new ExpressionParser.ParseOptions
//            (true, // single letter identifier mode is on
//            true // certain ASCII sequences become special
//            );
    
//    private static final Expr engine = 
//            ExpressionParser.parseAndSuppressExceptions
//            ("LX.LY.Lx.[X(x)&Y(x)]", options);

    private PredicateModificationRule() {
        super("Predicate Modification");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        try {
            Type ltype = node.getLeftChild().getMeaning().getType();
            Type rtype = node.getRightChild().getMeaning().getType();
            boolean l = ltype.equals(Type.ET);
            boolean r = rtype.equals(Type.ET);
            boolean s = node.size() == 2;
            boolean ret = s && l && r;
            return (ret);
                
        // If either child could not be evaluated, then we
        // just return false.
        } catch (Exception e) {
            return false;
        }            
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {

        if (onlyIfApplicable && !this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The predicate modification rule is only " +
                    "applicable on a nonterminal that has exactly " +
                    "two children of type <e,t>.");
        }

//        return new FunApp(new FunApp(engine, new MeaningBracketExpr(node.getLeftChild(), g)),
//            new MeaningBracketExpr(node.getRightChild(), g));
        
        MeaningBracketExpr left = new MeaningBracketExpr(node.getLeftChild(), g);
        MeaningBracketExpr right = new MeaningBracketExpr(node.getRightChild(), g);
        
        FunApp leftFA = new FunApp(left, VARIABLE);
        FunApp rightFA = new FunApp(right, VARIABLE);
        
        And and = new And(leftFA, rightFA);
        
        Lambda result = new Lambda(VARIABLE, and, true); // has period
        
        return result;
    }

}

