package lambdacalc.lf;

public class TerminalLacksMeaningException extends MeaningEvaluationException {

    public TerminalLacksMeaningException(LexicalTerminal terminal) {
        super("The terminal \"" + terminal.getLabel() + "\" has not been assigned a semantic meaning.");
    }

}