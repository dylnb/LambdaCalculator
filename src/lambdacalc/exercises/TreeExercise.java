/*
 * TreeExercise.java
 */

package lambdacalc.exercises;

import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.SyntaxException;
import lambdacalc.lf.*;

/**
 * An exercise that asks the student to give the semantic type
 * of a lambda expression.
 * @author tauberer
 */
public class TreeExercise extends Exercise implements HasIdentifierTyper {
    
    Nonterminal treeroot;
    IdentifierTyper types;
    
    public TreeExercise(Nonterminal treeroot, int index, IdentifierTyper types) throws SyntaxException {
        super(index);
        this.treeroot = treeroot;
        this.types = types;
    }
    
    public TreeExercise(String tree, int index, IdentifierTyper types) throws SyntaxException {
        this(BracketedTreeParser.parse(tree), index, types);
    }

    public String getExerciseText() {
        return treeroot.toString();
    }
    
    public String getTipForTextField() {
        return "enter the answer";
    }
    
    public String getShortDirective() {
        return "Do the right thing.";
    }

    public void reset() {
        super.reset();
    }

    public AnswerStatus checkAnswer(String answer) throws SyntaxException  {
        setDone();
        return AnswerStatus.CorrectFinalAnswer("Sure, why not.");
    }
    
    public String getLastAnswer() {
        return null;
    }
    
    public IdentifierTyper getIdentifierTyper() {
        return types;
    }

    public String toString() {
        return treeroot.toString();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(1); // for future use
        output.writeUTF(treeroot.toString());
        types.writeToStream(output);
    }
    
    TreeExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        if (input.readShort() != 1) throw new ExerciseFileVersionException();
        
        try {
            this.treeroot = BracketedTreeParser.parse(input.readUTF());
        } catch (SyntaxException e) {
            throw new ExerciseFileFormatException("Could not read back saved brackted tree: " + e.getMessage());
        }
        this.types = new IdentifierTyper();
        this.types.readFromStream(input, fileFormatVersion);
    }
}
