package lambdacalc.lf;

public class NonterminalLacksCompositionRuleException extends MeaningEvaluationException {

    public NonterminalLacksCompositionRuleException(Nonterminal  nonterminal) {
        super("The nonterminal " + (nonterminal.getLabel() == null ? nonterminal.toString() : nonterminal.getLabel()) 
        + " cannot be computed. Check the denotations in its subtree for errors.");
    }

    public NonterminalLacksCompositionRuleException(Nonterminal  nonterminal, String message) {
        super(message);
    }
}