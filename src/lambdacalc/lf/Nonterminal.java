package lambdacalc.lf;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

public class Nonterminal extends LFNode {

    private Vector children = new Vector();
    
    private CompositionRule compositor;
    private Vector userProvidedMeaningSimplification;
    
    
    public int size() {
        return children.size();
    }
    
    public boolean isBranching() {
        return children.size() >= 2;
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
    
    public boolean knowsCompositionRule() {
        return compositor != null;
    }
    
    public void setCompositionRule(CompositionRule rule) {
        CompositionRule oldRule = compositor;
        compositor = rule;
        changes.firePropertyChange("compositionRule", oldRule, compositor);
    }
    
    public Vector getUserMeaningSimplification() {
        return userProvidedMeaningSimplification;
    }
    
    public void setUserMeaningSimplification(Vector simplificationSteps) {
        userProvidedMeaningSimplification = simplificationSteps;
    }
    
    public String getDisplayName() {
        return "Nonterminal";
    }

    public boolean isMeaningful() {
        return true;
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {

        if (lambdacalc.Main.GOD_MODE) {
            // Guess a composition rule, and if we don't find any, tell the user none seem to apply.
            if (compositor == null || !compositor.isApplicableTo(this))
                guessCompositionRule(RuleList.HEIM_KRATZER);
            if (compositor == null) {
                throw new NonterminalLacksCompositionRuleException(this, 
        "I do not know how to combine the children of the " + getLabel() + " node." +
        " For instance, function application does not apply because neither child's " +
        "denotation is a function whose domain is the type of the denotation of the other child.");
            }
        
        } else if (compositor == null && NonBranchingRule.INSTANCE.isApplicableTo(this)) {
            // We are always allowed to guess the non-branching rule, even when not in
            // God mode.
            compositor = NonBranchingRule.INSTANCE;
            
        } else {
            if (compositor == null)
                throw new NonterminalLacksCompositionRuleException
                        (this, "Select a composition rule for the nonterminal " 
                        + toShortString() + " before you try" +
                        " to combine the children of this node.");
        }
            
        return compositor.applyTo(this, g, true);
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
     * @param rules the rules
     */
    public void guessRules(RuleList rules, boolean nonBranchingOnly) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessRules(rules, nonBranchingOnly);
        
        if (compositor != null)
            return;

        if (nonBranchingOnly && this.isBranching()) return;
        
        guessCompositionRule(rules);
    }
    
    /**
     * Does nothing and calls itself recursively on the children nodes.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
        for (int i = 0; i < children.size(); i++)
            getChild(i).guessLexicalEntries(lexicon);
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
        if (getLabel() != null) {
            ret += "." + getLabel();
            if (this.hasIndex())
                ret += String.valueOf(LFNode.INDEX_SEPARATOR) + this.getIndex();
            ret += " ";
        }
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) ret += " ";
            ret += children.get(i).toString();
        }
        ret += "]";
        return ret;
    }
}
