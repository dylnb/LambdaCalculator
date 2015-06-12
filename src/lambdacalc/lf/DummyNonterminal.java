/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambdacalc.lf;

import lambdacalc.logic.Expr;

/**
 *
 * @author dylan
 */
public class DummyNonterminal extends Nonterminal {
    protected Expr meaning = null;
    
    /** Creates a new instance of DummyTerminal */
    public DummyNonterminal(String label) {
        setLabel(label);
    }
    
    public DummyNonterminal(Nonterminal node) {
        setLabel(node.getLabel());
        for (int i = 0; i < node.getChildren().size(); i++) {
            this.addChild(node.getChild(i));
        }
    }
    
    public String getDisplayName() {
        return "Dummy nonterminal";
    }
    
    public boolean isMeaningful() {
        return false;
    }
    
    public Expr getMeaning(AssignmentFunction g)
        throws MeaningEvaluationException {
        throw new MeaningEvaluationException("\"" + toShortString() +"\" does not have a denotation.");
    }
    
    public void guessLexicalEntries(Lexicon lexicon) {
    }

    public String toLatexString() {
        return "\\mbox{"+this.getLabel()+"}";
    }  
    
    
}
