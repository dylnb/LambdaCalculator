/*
 * TypeExercise.java
 *
 * Created on May 30, 2006, 4:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

import lambdacalc.logic.*;

/**
 * An exercise that asks the student to give the semantic type
 * of a lambda expression.
 * @author tauberer
 */
public class TypeExercise extends Exercise implements HasIdentifierTyper {
    
    Expr expr;
    IdentifierTyper types;
    
    Type type;
    
    Type last_answer;
    
    public TypeExercise(Expr expression, int index, IdentifierTyper types) throws TypeEvaluationException {
        super(index);
        expr = expression;
        type = expr.getType();
        this.types = types;

        expr.getType(); // make sure it is well typed; will throw if not
    }
    
    public TypeExercise(String expression, ExpressionParser.ParseOptions parseOptions, int index, IdentifierTyper types) throws SyntaxException, TypeEvaluationException {
        this(ExpressionParser.parse(expression, parseOptions), index, types);
    }

    public String getExerciseText() {
        return expr.toString();
    }
    
    public String getTipForTextField() {
        return "enter a type";
    }
    
    public String getShortDirective() {
        return "Give the semantic type";
    }

    public void reset() {
        super.reset();
        this.last_answer = null;
    }

    public AnswerStatus checkAnswer(String answer) throws SyntaxException  {
        Type answertype;
        
        try {
            answertype = TypeParser.parse(answer);
        } catch (BadCharacterException exception) {
            boolean parsedAsExpr = false;
            try {
                ExpressionParser.parse(answer, new ExpressionParser.ParseOptions());
                parsedAsExpr = true;
            } catch (SyntaxException ex2) {
            }
            if (parsedAsExpr)
                throw new SyntaxException(exception.getMessage() + " In this exercise you are supposed to enter a type, not a " + Lambda.SYMBOL + "-expression.", exception.getPosition());
            throw exception;
        }

        if (type.equals(answertype)) {
            setDone();
            last_answer = answertype;
            return AnswerStatus.CorrectFinalAnswer(type.toString() + " is correct!");
        } else {
            return AnswerStatus.Incorrect(answertype.toString() + " is not right.  Try again.");
        }
    }
    
    public String getLastAnswer() {
        if (last_answer == null) return null;
        return last_answer.toString();
    }
    
    public IdentifierTyper getIdentifierTyper() {
        return types;
    }

    public String toString() {
        return expr.toString() + " : " + type.toString();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(1); // for future use
        expr.writeToStream(output);
        types.writeToStream(output);
        if (last_answer == null) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            last_answer.writeToStream(output);
        }
        // TODO: We're outputting a canonicalized version of what the student answered.
    }
    
    TypeExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        if (input.readShort() != 1) throw new ExerciseFileVersionException();
        
        this.expr = Expr.readFromStream(input);
        try {
            this.type = expr.getType();
        } catch (TypeEvaluationException e) {
            throw new ExerciseFileFormatException("Error reading file: " + e.getMessage()); // better not happen
        }
        this.types = new IdentifierTyper();
        this.types.readFromStream(input, fileFormatVersion);

        if (input.readByte() == 1)
            this.last_answer = Type.readFromStream(input);
    }
}
