package lambdacalc.lf;

import lambdacalc.logic.Expr;
import java.beans.*;
import java.util.SortedMap;
import java.util.TreeMap;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

public abstract class LFNode {
    protected PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    /**
     * The symbol used for separating the label from its index (if any)
     * in the #toString() method.
     */
    public static final char INDEX_SEPARATOR = '_'; 

    private String label;
    
    private int index = -1;
    
    protected LFNode() {
    }
    
    protected LFNode(String label, int index) {
        this.label = label;
        this.index = index;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        String oldLabel = this.label;
        this.label = label;
        changes.firePropertyChange("label", oldLabel, label);
    }
    
    /**
     * Returns -1 if no index has been set.
     */
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        int oldIndex = this.index;
        this.index = index;
        changes.firePropertyChange("index", oldIndex, index);
    }
    
    public void removeIndex() {
        setIndex(-1);
    }
    
    public boolean hasIndex() {
        return this.index != -1;
    }
    
    /**
     * Returns a user-friendly version of the name of this class
     * (e.g. "terminal", "nonterminal", etc.)
     */
    public abstract String getDisplayName();
    
    /**
     * Returns a map of properties. Keys are Strings and values are Objects.
     * Each entry represents a property-value pair. Properties include orthographic
     * strings, meanings, types, etc.
     *
     * @return a sorted map of properties
     */
    public SortedMap getProperties() {
        SortedMap m = new TreeMap();
        m.put("Kind", this.getDisplayName());
        m.put("Text", this.getLabel());
        Type t = null;
        try {
            t = this.getMeaning().getType();
        } catch (TypeEvaluationException ex) {
            //ex.printStackTrace();
        } catch (MeaningEvaluationException ex) {
            //ex.printStackTrace();
        }
        m.put("Type", t); 
        Integer index = null;
        if (this.hasIndex()) { index = new Integer(this.getIndex()); }
        m.put("Index", index);
        
        //m.put("toString()", this.toString());
        return m;
    }

    public String toShortString() {
        if (getLabel() != null) {
            if (!hasIndex())
                return getLabel();
            else
                return getLabel() + "_" + getIndex();
        } else if (hasIndex()) {
            return "" + getIndex();
        } else {
            return toString();
        }
    }
    
    public String toString() {
        String result = getLabel();
        if (hasIndex()) {
            if (result != null)
                result += String.valueOf(INDEX_SEPARATOR);
            result += getIndex();
        } 
        return result;
    }
    
    public String toHTMLString() {
        if (getLabel() != null) {
            String result = escapeHTML(getLabel());
            int caret = result.indexOf('^');
            if (caret != -1)
                result = result.substring(0, caret) + "<sup>" + result.substring(caret+1) + "</sup>";
            if (hasIndex())
                result += "<sub>" + getIndex() + "</sub>";
            return result;
        } else if (hasIndex()) {
            return "" + getIndex();
        } else {
            return "";
        }
    }
    private String escapeHTML(String text) {
        // remember first arg to replaceAll is a regular expression
        return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
    
    public Expr getMeaning() throws MeaningEvaluationException {
        return getMeaning(new AssignmentFunction());
    }
    
    public abstract Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException;

    /**
     * Sets composition rules of nonterminals in the tree where they haven't been 
     * set yet and are uniquely determined. Note that calling this will usually
     * be without effect unless guessLexicalEntries is called first.
     *
     * @param rules the rules
     */    
    public abstract void guessRules(RuleList rules, boolean nonBranchingOnly);
    
    public abstract void guessLexicalEntries(Lexicon lexicon);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }    
}