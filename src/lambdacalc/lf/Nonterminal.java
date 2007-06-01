package lambdacalc.lf;

import java.util.Vector;
import lambdacalc.logic.Expr;

public class Nonterminal extends LFNode {

    CompositionRule compositor;
    Vector children = new Vector();
    
    public int size() {
        return children.size();
    }
    
    public LFNode getChild(int index) {
        return (LFNode)children.get(index);
    }
    
    public void addChild(LFNode node) {
        children.add(node);
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
    
    public void guessLexicalChoices(Lexicon lexicon) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessLexicalChoices(lexicon);
        
        if (compositor != null)
            return;
        
        for (int i = 0; i < lexicon.getCompositionRules().size(); i++) {
            CompositionRule rule = (CompositionRule)lexicon.getCompositionRules().get(i);
            if (rule.isApplicable(this)) {
                if (compositor == null) {
                    // The first time we hit a compatible composition rule, 
                    // assign it to ourself.
                    compositor = rule;
                } else {
                    // But on the next time we hit a compatible rule, clear
                    // out what we set and return. We thus don't actually set
                    // compositor unless there is a uniquely applicable rule.
                    compositor = null;
                    return;
                }
            }
        }
    }

    public String toString() {
        String ret = "[";
        if (getLabel() != null)
            ret += "." + getLabel() + " ";
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) ret += " ";
            ret += children.get(i).toString();
        }
        ret += "]";
        return ret;
    }
}