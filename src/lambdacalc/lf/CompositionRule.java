package lambdacalc.lf;

import lambdacalc.logic.Expr;

public abstract class CompositionRule {

    String name;
    
    public CompositionRule(String name) {
        this.name = name;
    }
    
    public abstract Expr getMeaning(Nonterminal node) throws MeaningEvaluationException;
}