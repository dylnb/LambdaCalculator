package lambdacalc.lf;

import lambdacalc.logic.Expr;

public abstract class CompositionRule {

    String name;
    
    public CompositionRule(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
       
    /**
     * Returns whether this composition rule is applicable
     * to the given nonterminal node. If it cannot be determined
     * whether the rule is applicable, for instance because any
     * children cannot be evaluated, then false is returned.
     */
    public abstract boolean isApplicableTo(Nonterminal node);
    
    public Expr applyTo(Nonterminal node) throws MeaningEvaluationException {
        return applyTo(node, new AssignmentFunction());
    }
    public abstract Expr applyTo(Nonterminal node, 
            AssignmentFunction g) throws MeaningEvaluationException;
}
