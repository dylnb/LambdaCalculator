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
       
    public abstract boolean isApplicable(Nonterminal node);
    
    public abstract Expr getMeaning(Nonterminal node) throws MeaningEvaluationException;
}