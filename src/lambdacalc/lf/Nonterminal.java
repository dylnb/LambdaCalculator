package lambdacalc.lf;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

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
        changes.firePropertyChange("children", null, null);
    }
    
    public CompositionRule getCompositionRule() {
        return compositor;
    }
    
    public void setCompositionRule(CompositionRule rule) {
        CompositionRule oldRule = compositor;
        compositor = rule;
        changes.firePropertyChange("compositionRule", oldRule, compositor);
    }
    
    public String getDisplayName() {
        return "Nonterminal";
    }

    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        // TODO: For now, we will always try to guess the
        // nonterminal composition rule because we have no
        // way of choosing it.
        if (compositor == null || !compositor.isApplicableTo(this))
            guessCompositionRule(RuleList.HEIM_KRATZER);

        if (compositor == null)
            throw new NonterminalLacksCompositionRuleException(this);
        return compositor.applyTo(this, g);
    }
    
    /**
     * Returns a map of properties. Keys are Strings and values are Objects.
     * Each entry represents a property-value pair. Properties include orthographic
     * strings, meanings, types, etc.
     *
     * @return a sorted map of properties
     */
    public SortedMap getProperties() {
        SortedMap m = super.getProperties();
        m.put("Rule", this.getCompositionRule());
        return m;
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

        guessCompositionRule(rules);
    }
    
    private void guessCompositionRule(RuleList rules) {
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
            ret += String.valueOf(LFNode.INDEX_SEPARATOR)+this.getIndex();
        }
        return ret;
    }
}
