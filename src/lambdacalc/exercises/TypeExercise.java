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
        output.writeShort(0); // for future use
        output.writeUTF(expr.toString());
        types.writeToStream(output);
        if (last_answer == null) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            output.writeUTF(last_answer.toString());
        }
        // TODO: We're outputting a canonicalized version of what the student answered.
    }
    
    public TypeExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        if (input.readShort() != 0) throw new ExerciseFileVersionException();
        
        String saved_expr = input.readUTF();
        
        this.types = new IdentifierTyper();
        this.types.readFromStream(input, fileFormatVersion);

        ExpressionParser.ParseOptions expParserOptions = new ExpressionParser.ParseOptions();
        expParserOptions.Typer = this.types;
        
        try {
            this.expr = ExpressionParser.parse(saved_expr, expParserOptions);
        
            this.type = expr.getType();

            if (input.readByte() == 1)
                this.last_answer = TypeParser.parse(input.readUTF());
        } catch (Exception e) {
            System.err.println(e);
            throw new ExerciseFileFormatException();
        }
    }
}
