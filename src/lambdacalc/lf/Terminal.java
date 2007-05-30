package lambdacalc.lf;

import lambdacalc.logic.Expr;

public class Terminal extends LFNode {

    Expr meaning;
    
    public void setMeaning(Expr meaning) {
        this.meaning = meaning;
    }

    public Expr getMeaning() throws MeaningEvaluationException {
        if (meaning == null)
            throw new TerminalLacksMeaningException(this);
        return meaning;
    }
    
    public String toString() {
        return getLabel();
    }
}