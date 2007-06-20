/*
 * TreeExercise.java
 */

package lambdacalc.exercises;

import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.SyntaxException;
import lambdacalc.lf.*;

/**
 * An exercise that presents a LF tree and asks the student to provide
 * denotations of terminal nodes, to select the composition rules for
 * nonterminals, etc.
 */
public class TreeExercise extends Exercise implements HasIdentifierTyper {
    
    Nonterminal treeroot;
    IdentifierTyper types;
    
    // persists the state of the tree, as derived by God-mode
    public java.util.Map derivationDisplayState = new java.util.HashMap();
    
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
        return "Solve this tree"; // never used
    }
    
    public String getShortTitle() {
        return "Tree";
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
    
    public Nonterminal getTree() {
        return treeroot;
    }

    public String toString() {
        return treeroot.toString();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(1); // for future use
        
        // write the tree & identifier typer in effect for this problem
        output.writeUTF(treeroot.toString());
        types.writeToStream(output);
        
        // write out the choices the user has made on the tree: terminal
        // lexical entries and nonterminal composition rules. It's written
        // in a pre-order traversal of the LF tree.
        writeUserChoicesToStream(treeroot, output);
    }
    
    private void writeUserChoicesToStream(LFNode node, java.io.DataOutputStream output) throws java.io.IOException {
        if (node instanceof Nonterminal) {
            output.writeByte(0); // sanity check later
            Nonterminal nt = (Nonterminal)node;
            if (nt.getCompositionRule() == null) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                CompositionRule.writeToStream(nt.getCompositionRule(), output);
            }
            
            for (int i = 0; i < nt.size(); i++)
                writeUserChoicesToStream(nt.getChild(i), output);
        } else if (node instanceof LexicalTerminal) {
            output.writeByte(1); // sanity check later
            LexicalTerminal lt = (LexicalTerminal)node;
            if (!lt.hasMeaning()) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                try {
                    lt.getMeaning().writeToStream(output);
                } catch (MeaningEvaluationException e) {
                    throw new RuntimeException(e.getMessage()); // not reachable since we already checked if it has a meaning assigned
                }
            }
        } else {
            output.writeByte(2); // sanity check later
        }
    }
    
    TreeExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        if (input.readShort() != 1) throw new ExerciseFileVersionException();
        
        String bracketedtree = input.readUTF();
        try {
            this.treeroot = BracketedTreeParser.parse(bracketedtree);
        } catch (SyntaxException e) {
            throw new ExerciseFileFormatException("Could not read back saved brackted tree within the exercise file: " + bracketedtree + ", " + e.getMessage());
        }
        this.types = new IdentifierTyper();
        this.types.readFromStream(input, fileFormatVersion);
        
        // read in the choices the user made
        readUserChoicesFromStream(treeroot, input);
    }
    
    private void readUserChoicesFromStream(LFNode node, java.io.DataInputStream input) throws java.io.IOException {
        if (node instanceof Nonterminal) {
            if (input.readByte() != 0) throw new java.io.IOException("Data format error.");
            Nonterminal nt = (Nonterminal)node;
            if (input.readByte() == 0) {
                nt.setCompositionRule(null);
            } else {
                nt.setCompositionRule(CompositionRule.readFromStream(input));
            }
            
            for (int i = 0; i < nt.size(); i++)
                readUserChoicesFromStream(nt.getChild(i), input);
        } else if (node instanceof LexicalTerminal) {
            if (input.readByte() != 1) throw new java.io.IOException("Data format error.");
            LexicalTerminal lt = (LexicalTerminal)node;
            if (input.readByte() == 0) {
                lt.setMeaning(null);
            } else {
                lt.setMeaning(lambdacalc.logic.Expr.readFromStream(input));
            }
        } else {
            if (input.readByte() != 2) throw new java.io.IOException("Data format error.");
        }
    }
}
