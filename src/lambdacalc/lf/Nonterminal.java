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
    
    public LFNode getLeftChild() {
        return (LFNode) children.get(0);
    }
    
    public LFNode getRightChild() {
        return (LFNode) children.get(children.size()-1);
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

    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        //TODO don't ignore g
        if (compositor == null)
            throw new NonterminalLacksCompositionRuleException(this);
        return compositor.applyTo(this);
    }
    
    /**
     * Calls itself recursively on the children nodes, then
     * sets the composition rule of this nonterminal if it hasn't been 
     * set yet and if it's uniquely determined. 
     *
     * @param lexicon the lexicon
     * @param rules the rules
     */
    public void guessLexicalEntriesAndRules(Lexicon lexicon, RuleList rules) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessLexicalEntriesAndRules(lexicon, rules);
        
        if (compositor != null)
            return;
        
        for (int i = 0; i < rules.size(); i++) {
            CompositionRule rule = (CompositionRule) rules.get(i);
            if (rule.isApplicableTo(this)) {
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
        if (this.hasIndex()) {
            ret += LFNode.SEPARATOR+this.getIndex();
        }
        return ret;
    }
}