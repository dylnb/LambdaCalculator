package lambdacalc.lf;

import lambdacalc.logic.Expr;

public abstract class LFNode {

    String label;
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }

    public abstract Expr getMeaning() throws MeaningEvaluationException;
}