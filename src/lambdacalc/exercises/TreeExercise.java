/*
 * TreeExercise.java
 */

package lambdacalc.exercises;

import java.util.Vector;

import lambdacalc.logic.Expr;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.SyntaxException;
import lambdacalc.lf.*;

/**
 * An exercise that presents a LF tree and asks the student to provide
 * denotations of terminal nodes, to select the composition rules for
 * nonterminals, etc.
 */
public class TreeExercise extends Exercise implements HasIdentifierTyper {
    
    private Nonterminal treeroot;
    private IdentifierTyper types;
    
    private boolean hasBeenStarted = false;
    
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

    public void setHasBeenStarted(boolean b) {
        this.hasBeenStarted = b;
    }
    
    public boolean getHasBeenStarted() {
        return this.hasBeenStarted;
    }
    public String getExerciseText() {
//        return treeroot.toString();
        return treeroot.toStringTerminalsOnly();
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
        setDone(true);
        return AnswerStatus.CorrectFinalAnswer("Sure, why not.");
    }
    
    public String getLastAnswer() {
        return null;
    }
    
    public boolean hasBeenStarted() {
        return this.hasBeenStarted;
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

    
    public String getUserAnswers() {
        StringBuffer sb = new StringBuffer();
        writeUserChoicesToString(treeroot, sb);
        return sb.toString();
    }
    
    private void writeUserChoicesToString(LFNode node, StringBuffer output) {
        if (node instanceof Nonterminal) {
            Nonterminal nt = (Nonterminal)node;
            
            for (int i = 0; i < nt.size(); i++)
                writeUserChoicesToString(nt.getChild(i), output);
            
            output.append("\t");
            
            if (nt.getLabel() != null) {
                output.append(nt.getLabel());
                if (nt.hasIndex())
                    output.append("_" + nt.getIndex());
            } else {
                output.append(nt.toString());
            }
             
            
            if (nt.getUserMeaningSimplification() == null) {
                output.append(", no simplification performed");
            } else {
                output.append("\n");
                Vector steps = nt.getUserMeaningSimplification();
                for (int i = 0; i < steps.size(); i++)
                    output.append("\t\t" + steps.get(i) + "\n");
            }
            
            output.append("\n");
            
        } else if (node instanceof LexicalTerminal) {
            LexicalTerminal lt = (LexicalTerminal)node;
            output.append("\t");
            output.append(lt.toString() + ": ");
            if (!lt.hasMeaning()) {
                output.append("no denotation given");
            } else {
                try {
                    output.append(lt.getMeaning().toString());
                } catch (MeaningEvaluationException e) {
                    // on a terminal, this can't be reached, since hasMeaning() is true
                }
            }
            output.append("\n");
            
        } else {
            // skip other node types since the user doesn't do anything for them
        }
    }
        
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(2);
        // files saved before the "multiple reductions" switch were
        // saved with output.writeShort(1) here
        
        // write whether multiple reductions are allowed for this exercise
        output.writeBoolean(this.getNotSoFast());
        
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
            
            if (nt.getUserMeaningSimplification() == null) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                output.writeInt(nt.getUserMeaningSimplification().size());
                for (int i = 0; i < nt.getUserMeaningSimplification().size(); i++) {
                    Expr e = (lambdacalc.logic.Expr)nt.getUserMeaningSimplification().get(i);
                    
                    // We can't serialize e directly because we know it may contain MeaningBracketExpr objects.
                    MeaningBracketExpr.writeExpr(e, treeroot, output);
                }
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
        } else if (node instanceof Trace) {
            output.writeByte(2); // sanity check later
            Trace t = (Trace)node;
            if (t.getType() == null) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                try {
                    t.getType().writeToStream(output);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage()); // not reachable
                }
            }
        } else if (node instanceof BareIndex) {
            output.writeByte(2); // sanity check later
            BareIndex bi = (BareIndex)node;
            if (bi.getType() == null) {
                output.writeByte(0);
            } else {
                output.writeByte(1);
                try {
                    bi.getType().writeToStream(output);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage()); // not reachable
                }
            }
        } else {
            output.writeByte(3); // sanity check later
        }
    }
    
    TreeExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        int mr = input.readShort();
        if (!(mr == 1 || mr == 2)) throw new ExerciseFileVersionException();
        
        if (mr == 2) this.setNotSoFast(input.readBoolean());
        
        String bracketedtree = input.readUTF();
        try {
            this.treeroot = BracketedTreeParser.parse(bracketedtree);
        } catch (SyntaxException e) {
            throw new ExerciseFileFormatException("Could not read back saved bracketed tree within the exercise file: " + bracketedtree + ", " + e.getMessage());
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
            
            if (input.readByte() == 0) {
                nt.setUserMeaningSimplification(null);
            } else {
                Vector v = new Vector();
                int n = input.readInt();
                for (int i = 0; i < n; i++) {
                    Expr e = MeaningBracketExpr.readExpr(treeroot, input);
                    v.add(e);
                }
                nt.setUserMeaningSimplification(v);
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
        } else if (node instanceof Trace) {
            if (input.readByte() != 2) throw new java.io.IOException("Data format error.");
            Trace t = (Trace)node;
            if (input.readByte() == 0) {
                t.setType(null);
            } else {
                t.setType(lambdacalc.logic.Type.readFromStream(input));
            }
        } else if (node instanceof BareIndex) {
            if (input.readByte() != 2) throw new java.io.IOException("Data format error.");
            BareIndex bi = (BareIndex)node;
            if (input.readByte() == 0) {
                bi.setType(null);
            } else {
                bi.setType(lambdacalc.logic.Type.readFromStream(input));
            }
        } else {
            if (input.readByte() != 3) throw new java.io.IOException("Data format error.");
        }
    }
}
