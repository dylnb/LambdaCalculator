package lambdacalc.lf;

import lambdacalc.logic.Expr;

public class LexicalTerminal extends Terminal {

    protected Expr meaning;
    
    public void setMeaning(Expr meaning) {
        Expr oldMeaning = this.meaning;
        this.meaning = meaning;
        changes.firePropertyChange("meaning", oldMeaning, this.meaning);
    }
    
    public boolean hasMeaning() {
        return meaning != null;
    }

    public boolean isMeaningful() {
        return true;
    }
    
    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        if (meaning == null)
            throw new TerminalLacksMeaningException(this);
        return meaning;
    }
    
    public String getDisplayName() {
        return "Lexical terminal";
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
    public void guessLexicalEntries(Lexicon lexicon) {
        if (meaning != null)
            return;
        
        Expr[] meanings = lexicon.getMeanings(getLabel());
        if (meanings.length == 1)
            meaning = meanings[0];    
    }
    
 
}