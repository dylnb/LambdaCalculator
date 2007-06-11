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
       
    public abstract boolean isApplicableTo(Nonterminal node);
    
    public Expr applyTo(Nonterminal node) throws MeaningEvaluationException {
        return applyTo(node, new AssignmentFunction());
    }
    public abstract Expr applyTo(Nonterminal node, 
            AssignmentFunction g) throws MeaningEvaluationException;
}