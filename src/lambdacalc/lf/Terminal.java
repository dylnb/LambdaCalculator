package lambdacalc.lf;

import lambdacalc.logic.Expr;

public class Terminal extends LFNode {

    protected Expr meaning;
    
    public void setMeaning(Expr meaning) {
        Expr oldMeaning = this.meaning;
        this.meaning = meaning;
        changes.firePropertyChange("meaning", oldMeaning, this.meaning);
    }

    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        //TODO don't ignore g
        if (meaning == null)
            throw new TerminalLacksMeaningException(this);
        return meaning;
    }
    
    /**
     * If the meaning of this terminal hasn't been set yet, 
     * and if the terminal is unambiguous in the lexicon,
     * assigns the meaning it finds in the lexicon to this
     * terminal.
     *
     * @param lexicon the lexicon
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessLexicalEntriesAndRules(Lexicon lexicon, RuleList rules) {
        if (meaning != null)
            return;
        
        Expr[] meanings = lexicon.getMeanings(getLabel());
        if (meanings.length == 1)
            meaning = meanings[0];
    
    }
    
}