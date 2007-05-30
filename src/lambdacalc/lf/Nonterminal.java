package lambdacalc.lf;

import java.util.Vector;
import lambdacalc.logic.Expr;

public class Nonterminal extends LFNode {

    CompositionRule compositor;
    Vector children = new Vector();
    
    public Vector getChildren() {
        return children;
    }
    
    public CompositionRule getCompositionRule() {
        return compositor;
    }
    
    public void setCompositionRule(CompositionRule rule) {
        compositor = rule;
    }

    public Expr getMeaning() throws MeaningEvaluationException {
        if (compositor == null)
            throw new NonterminalLacksCompositionRuleException(this);
        return compositor.getMeaning(this);
    }

    public String toString() {
        String ret = "[";
        if (getLabel() != null)
            ret += getLabel();
        for (int i = 0; i < children.size(); i++) {
            ret += " ";
            ret += children.get(i).toString();
        }
        ret += "]";
        return ret;
    }
}