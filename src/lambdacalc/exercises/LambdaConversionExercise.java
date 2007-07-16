/*
 * LambdaConversionExercise.java
 *
 * Created on May 31, 2006, 5:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lambdacalc.lf.MeaningBracketExpr;
import lambdacalc.lf.MeaningEvaluationException;

import lambdacalc.logic.*;

/**
 * This class represents a lambda conversion exercise.
 * @author tauberer
 */
public class LambdaConversionExercise extends Exercise implements HasIdentifierTyper {

    private static final String ALPHAVARY = "alphavary";
    private static final String BETAREDUCE = "betareduce";
    private static final String NOT_REDUCIBLE = "notreducible";
    private static final String MEANINGBRACKETS = "meaningbrackets"; // user is to replace meaning brackets with expressions
    
    private Expr expr;
    private IdentifierTyper types;
    
    private ArrayList steps = new ArrayList();
    private ArrayList steptypes = new ArrayList();
    
    private Expr lastAnswer;
    private int currentStep = 0;
    
    /**
     * Whether student's answers are parsed with the singleLetterIdentifiers option set.
     */
    private boolean parseSingleLetterIdentifiers = true;
    /**
     * Whether students are prohibited from skipping steps in multi-step problems.
     */
    private boolean notSoFast = lambdacalc.Main.NOT_SO_FAST; 
    // whether we force the user to proceed one step at a time
    
    /**
     * Initializes the exercise and works out beforehand what the student should do.
     * 
     * @param index if this exercise is part of an ExerciseGroup, the index is 
     * supposed to indicate where this exercise is located in the group
     *
     */
    //TODO  I noticed that the singleLetterIdentifiers
    //field is not set in this constructor (could be dangerous) --Lucas
    public LambdaConversionExercise(Expr expr, int index, IdentifierTyper types) 
    throws TypeEvaluationException {
        super(index);
        
        this.expr = expr;
        this.types = types;
        
        this.expr.getType(); // make sure it is well typed; will throw if not

        initialize();
    }
    
    /**
     * Initializes the exercise from an unparsed string, which is parsed 
     * and sent to the other constructor.
     */
    public LambdaConversionExercise
            (String expr, ExpressionParser.ParseOptions parseOptions, 
            int index, IdentifierTyper types) 
            throws SyntaxException, TypeEvaluationException {
        
        this(ExpressionParser.parse(expr, parseOptions), index, types);
        setParseSingleLetterIdentifiers(parseOptions.singleLetterIdentifiers);
        
        // If explicit types were assigned to identifiers in the expression,
        // add those conventions to the IdentifierTyper used for this exercise.
        // Note that since a variable can be used with different types in different
        // places, this doesn't guarantee that every identifier will be typed
        // according to a single set of conventions.
        if (parseOptions.hasExplicitTypes()) {
            //explicitTypes is a map from Strings to Identifiers
            this.types = this.types.cloneTyper(); //TODO Josh-- what does this line do? -Lucas
            for (Iterator i = parseOptions.explicitTypes.keySet().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                Identifier info = (Identifier)parseOptions.explicitTypes.get(name);
                this.types.addEntry(name, info instanceof Var, info.getType());
                //types is a map from Strings to Types
            }
        }
    }
    
    private void initialize() throws TypeEvaluationException {
        Expr e = expr;
        
        try {
            e = MeaningBracketExpr.replaceAllMeaningBrackets(expr);
            if (!e.equals(expr)) {
                steptypes.add(MEANINGBRACKETS);
                steps.add(e);
            }
        } catch (MeaningEvaluationException mee) {
            // just ignore-- the nonterminal lacks a valid meaning: so why did
            // we create this problem in the first place?
        }
        
        while (true) {
            // Attempt to perform a lambda conversion on the expression.
            Expr.LambdaConversionResult lcr = e.performLambdaConversion();
            
            // If there was nothing to do, we're done.
            if (lcr == null)
                break;
            
            // If an alphabetical variant was necessary, record that.
            if (lcr.alphabeticalVariant != null) {
                steptypes.add(ALPHAVARY);
                e = lcr.alphabeticalVariant;
            } else {
                steptypes.add(BETAREDUCE);
                e = lcr.result;
            }

            steps.add(e);
        }
        
        if (e == expr) {
            steptypes.add(NOT_REDUCIBLE);
            steps.add(e);
        }
    }    
    
    public String getExerciseText() {
        return expr.toString();
    }
    
    public String getTipForTextField() {
        return "enter an expression";
    }
    
    public String getShortDirective() {
        return "Simplify the expression";
    }
    
    public String getShortTitle() {
        return Lambda.SYMBOL+"-Conv.";
    }

    public void reset() {
        super.reset();
        this.lastAnswer = null;
        this.currentStep = 0;
    }
    
    public boolean isNotReducible() {
        return steptypes.get(0).equals(NOT_REDUCIBLE);
    }

    /**
     * Parses an expression using the parser options of this exercise. Syntax errors
     * are left to be handled by the caller so it can position the text caret
     * in the text box appropriately. We also check if the user has inadvertently
     * entered a type.
     */
    public Expr parse(String exprString) throws SyntaxException {
    
        Expr result;
        
    
        ExpressionParser.ParseOptions exprParseOpts = new ExpressionParser.ParseOptions();
        exprParseOpts.ASCII = false;
        exprParseOpts.singleLetterIdentifiers = isParseSingleLetterIdentifiers();
        exprParseOpts.typer = types;

        try {
            result = ExpressionParser.parse(exprString, exprParseOpts);
        } catch (BadCharacterException exception) {
            boolean parsedAsType = false;
            try {
                TypeParser.parse(exprString);
                parsedAsType = true;
            } catch (SyntaxException ex2) {
            }
            if (parsedAsType)
                throw new SyntaxException(exception.getMessage() + " In this exercise you are supposed to enter a " + Lambda.SYMBOL + "-expression, not a type.", exception.getPosition());
           throw exception;
        }
        
        return result;
    }
    
    /**
     * Parses the user's answer into an expression and checks it.  Syntax errors
     * are left to be handled by the caller so it can position the text caret
     * in the text box appropriately. This is just a call to the other
     * #checkAnswer(Expr) method.
     */
    public AnswerStatus checkAnswer(String answer) throws SyntaxException {
        return checkAnswer(parse(answer));
    }
    
    /**
     * Checks if the given expression is a correct answer.
     * It is recommended to produce the
     * argument using
     * this exercise's parser settings, e.g. by using #parse(Expr) in this class.
     */
    public AnswerStatus checkAnswer(Expr userAnswer) {

        // this is what the user was trying to simplify
        Expr prevStep = currentStep == 0 ? expr : (Expr)steps.get(currentStep-1);
        
        // let's check up front whether the user answered without changing
        // the question.  if the expression is not reducible, that's fine,
        // otherwise we need to tell the user to try to reduce the expression
        
        // TODO: Probably compare with lastAnswer, not prevStep, if lastAnswer
        // is not null, because the user's actual previous answer may be different
        // if any alphabetical variants have been made.
        
        if (userAnswer.equals(prevStep)) {
            // the user didn't do anything
            if (steptypes.get(0).equals(NOT_REDUCIBLE)) { // and that was the right thing to do
                currentStep++;
                setDone(true);
                return AnswerStatus.CorrectFinalAnswer("That is correct! " + prevStep.toString() + " is not reducible.");
                // By doing this, we display the canonical representation (rather than the student's input) in the feedback.
                
            } else { // the user should have done something
                return AnswerStatus.Incorrect("This expression can be simplified. 'Feed' the leftmost argument into the leftmost " + Lambda.SYMBOL + "-slot.");
            }
        }

        boolean correct = false;
        
        // See if the user gave an answer equaling (up to parens and the consistent 
        // renaming of bound vars, i.e. alpha-equivalent)
        // what we're expecting as the next answer, or some future answer
        // if the user gives something equaling a future answer but
        // notSoFast is true, then it's deemed an incorrect answer.
        for (int matched_step = currentStep; matched_step < steps.size(); matched_step++) {
            Expr correct_answer = (Expr)steps.get(matched_step);
            if (correct_answer.alphaEquivalent(userAnswer)) {
                if (matched_step > currentStep && isNotSoFast())
                    return AnswerStatus.Incorrect("Not so fast!  Do one " 
                            + Lambda.SYMBOL + "-conversion or alphabetical " +
                            "variant step at a time. (You're on the right track, but I'd " +
                            "like to see your individual steps.)");

                // When this step is to create an alphabetical variant, the user
                // must enter an expression that is alpha-equivalent
                // to the answer (checked above), different from the question (i.e.
                // some action was taken, also checked above), and also an
                // expression which now no longer needs further alphabetical
                // variation in order to be beta reduced.
                try {
                    Expr.LambdaConversionResult lcr = userAnswer.performLambdaConversion();
                    
                    if (steptypes.get(matched_step).equals(ALPHAVARY)
                        && lcr != null
                        && lcr.alphabeticalVariant != null)
                        continue; // continue stepping through the future expected answers -
                                //maybe it matches a future step that is OK (it shouldn't)
                } catch (TypeEvaluationException tee) {
                }
                
                currentStep = matched_step;
                correct = true;
                break;
            }
        }
        // - By now we know whether the answer is correct but we haven't yet taken an action based on that.

        
        Expr correct_answer = (Expr)steps.get(currentStep); // the expected correct answer, not the user's input
        String currentThingToDo = (String)steptypes.get(currentStep); // e.g. alphavary

        if (correct) {
            lastAnswer = userAnswer;
            
            currentStep++;
            
            if (currentStep == steps.size()) {
                setDone(true);
                return AnswerStatus.CorrectFinalAnswer("Correct!");
            } else if (currentThingToDo.equals(ALPHAVARY)) {
                return AnswerStatus.CorrectStep("That is a licit alphabetical variant.  " +
                        "Now see if it is the one you need by reducing the expression...");
            } else {
                return AnswerStatus.CorrectStep("Yes!  Now keep reducing the expression...");
            }

        } else { // incorrect
            String hint; //TODO rename into finalHint
            

            // Compile a list of messages about what we think the user did wrong.
            ArrayList responses = new ArrayList();
            Set diagnoses = new HashSet();

            //TODO the next three lines should be parametrized in their error messages
            //on whether or not lambda conversion is the correct next thing to do
            if (didUserAttemptLambdaConversion(prevStep, userAnswer)
                && currentThingToDo.equals(BETAREDUCE)) {
                didUserApplyTheRightArgument(prevStep, correct_answer, userAnswer, responses, diagnoses);
                didUserRemoveTheRightLambda(prevStep, userAnswer, responses, diagnoses);
                
                // See if the user did everything right but made a mistake in the names of variables.
                if (!correct_answer.alphaEquivalent(userAnswer) 
                && correct_answer.operatorEquivalent(userAnswer) 
                && !diagnoses.contains("leftmost-leftmost"))
                    responses.add("You made a mistake in your " + Lambda.SYMBOL + "-conversion. " +
                            "Remember to substitute the argument for all free instances of the " 
                            + Lambda.SYMBOL + " variable, and for nothing else. "
                            + "Check that you didn't make any substitutions of variables not bound by the " + Lambda.SYMBOL + ".");
                //TODO "check that you didn't..." is misleading in cases where the user didn't in fact
                //do any such substitutions
                
                // test if the number of removed lambdas doesn't equal the number of removed arguments
                // (this indicates that the user tried to do a beta reduction but was confused)
                //
                // test if user attempted to do a beta reduction by removing the lambda and the argument
                // but forgot to carry out the substitution inside the body of the lambda

            }
            
            if (!currentThingToDo.equals(ALPHAVARY) && prevStep.alphaEquivalent(userAnswer))
                responses.add("You've made a licit alphabetical variant, but you don't need an alphabetical variant here.");
            
            if (prevStep.operatorEquivalent(userAnswer))
                didUserRenameAFreeVariableOrDidntRenameConsistently(prevStep, userAnswer, responses, diagnoses);
            
            // This is a basically hint for what to do next.
            if (currentThingToDo.equals(ALPHAVARY)) {
                didUserSubstituteButNeedingAlphabeticalVariant(prevStep, userAnswer, responses);
                if (correct_answer.alphaEquivalent(userAnswer)) {
                    // the user has given a feasible alphabetical variant,
                    // but it is not the right one.
                    hint = "You have given a licit alphabetical variant, but it's not one that will help you.  (Do you see why?)  Try again.";
                } else if (diagnoses.contains("incorrect-alphavary")) {
                    hint = "Try making another alphabetical variant.";
                } else {
                    // the user has done something besides making an alphabetical variant
                    hint = "Go back and try to make an alphabetical variant.";
                }
            } else if (currentThingToDo.equals(BETAREDUCE) && getNumberOfLambdaConversions(prevStep) > 1)
                hint = "Perform the outermost or leftmost " + Lambda.SYMBOL + "-conversion first.";
            else if (currentThingToDo.equals(BETAREDUCE) && steptypes.contains(ALPHAVARY))
                hint = "Try applying " + Lambda.SYMBOL + "-conversion.";
            else if (currentThingToDo.equals(MEANINGBRACKETS))
                hint = "Replace the ocurrences of the interpretation function [[ ... ]] with the denotations of the indicated nodes in the tree.";
            else if (currentThingToDo.equals(BETAREDUCE) || currentThingToDo.equals(NOT_REDUCIBLE))
                hint = null; // nothing useful to say by default
            else
                throw new RuntimeException(); // not reachable
            
            // Check that the user's answer has no typing issues.  If there
            // is a type issue, we can flag that as well to help the user
            // figure out what went wrong.
            try {
                userAnswer.getType();
            } catch (TypeMismatchException e) {
                responses.add("Note that your expression " + (responses.size() > 0 ? "also " : "") + "has a problem with types: " + e.getMessage());
            } catch (TypeEvaluationException e) {
                // I think this is only a ConstInsteadOfVarException, so we'll put this error up front.
                responses.add(0, e.getMessage());
            }

            // This adds discourse connectives (i.e "also") between the responses,
            // but we purposefully haven't added the "hint" string
            // into the responses yet because it shouldn't get a
            // discourse connective because it is not an error the user made.
            for (int i = 1; i < responses.size(); i++) {
                String r = (String)responses.get(i);
                if (r.startsWith("You ")) {
                    r = "You also " + r.substring(4);
                } else {
                    String connective = "Also";
                    if (Math.random() < 0.5)
                        connective = "In addition";
                    else if (i == responses.size()-1 && responses.size() > 2)
                        connective = "And on top of it"; // "easter egg"
                    r = connective + ", " + Character.toLowerCase(r.charAt(0)) + r.substring(1);
                }
                responses.set(i, r);
            }

            if (responses.size() == 0 && hint == null) hint = "I'm afraid I can't help you here. Please try again."; // only use this if there is no other message
            
            // Assemble the response string
            String response = "";
            //if (responses.size() > 1)
                for (int i = 0; i < responses.size(); i++)
                    response += "\n  \u2023 " + (String)responses.get(i);
            
            if (hint != null) {
                if (responses.size() > 0)
                    response += "\n";
                else
                    response += " ";
                response += hint;
            }

            //System.err.println("User's answer: " + userAnswer.toString()); // the user's answer, for debugging!
            //System.err.println("Expecting: " + steps.get(currentStep).toString()); // the correct answer, for debugging!
            
            return AnswerStatus.Incorrect("That's not right. " + response.toString());
        }
    }

    public String getLastAnswer() {
        if (lastAnswer == null) return null;
        return lastAnswer.stripAnyDoubleParens().toString();
    }

    public IdentifierTyper getIdentifierTyper() {
        return types;
    }
    
    /**
     * Tests if it seems user tried a lambda conversion, which is when
     *    a) the user has removed a lambda from the expression, or
     *    b) the user has removed an argument from the expression
     */
    private boolean didUserAttemptLambdaConversion(Expr prev_step, Expr answer) {
        if (!(prev_step instanceof FunApp))
            return false;
        
        ArrayList correctargs = getFunAppArgs(prev_step);
        ArrayList userargs = getFunAppArgs(answer);
        
        if (correctargs.size() != userargs.size())
            return true;

        ArrayList correctvars = getLambdaVars(expr);
        ArrayList uservars = getLambdaVars(answer);

        if (correctvars.size() != uservars.size())
            return true;
        
        return false;
    }
    
    /**
     * Error diagnostic. Given that the user has either removed a lambda or an argument, check
     * that it was the innermost argument that was removed. 
     * This method should normally only be called if currentThingToDo == BETAREDUCE
     *
     */
    private void didUserApplyTheRightArgument(Expr prevStep, Expr correctAnswer, Expr answer, ArrayList hints, Set diagnoses) {
        // This check is actually kind of complicated due to the following situations:
        //   Lx.P(x) a
        //   Lx.[Ly.P(y) a] b
        // After lambda conversion, the FunApp *inside* the lambda becomes the root
        // of the expression, and so the correct answer actually has, in these cases,
        // just as many arguments on the right edge of the expression as the previous
        // step, rather than one less like in the basic examples.
        
        
        
        prevStep = prevStep.stripOutermostParens();
        correctAnswer = correctAnswer.stripOutermostParens();
        answer = answer.stripOutermostParens();
        
        ArrayList prevargs = getFunAppArgs(prevStep);
        ArrayList correctargs = getFunAppArgs(correctAnswer);
        ArrayList userargs = getFunAppArgs(answer);

        // Did user remove exactly one argument?
        if (correctargs.size() != userargs.size()) {
            
            hints.add("After each " + Lambda.SYMBOL + "-conversion, exactly one argument should be gone on the right hand side.");
            return;
            
        }

        // Test if the user did the right thing.  We have to do this test
        // first because removing the first and the last arguments might
        // result in the same list (for duplicated arguments), and that
        // means the user did the right thing.
        if (!userargs.equals(correctargs)) {
            hints.add("The leftmost " + Lambda.SYMBOL + "-slot corresponds with the leftmost argument to be " + Lambda.SYMBOL + "-converted.  Start with the argument '" +  prevargs.get(prevargs.size()-1) + "'.");
            diagnoses.add("leftmost-leftmost");
        }
        
        return;
    }
    
    /**
     * This method returns the arguments, striped of parens,
     * going outside-in, in expr.  For example, in:
     * Lx.1. (a) (b) (c)
     * This method returns (c, b, a).
     */
    private ArrayList getFunAppArgs(Expr expr) {
        ArrayList ret = new ArrayList();
        expr = expr.stripOutermostParens();
        while (expr instanceof FunApp) {
            Expr func = ((FunApp)expr).getFunc();
            Expr arg = ((FunApp)expr).getArg();
            ret.add(arg.stripOutermostParens());
            expr = func.stripOutermostParens();
        }
        return ret;
    }
    
    private ArrayList pop(ArrayList list) {
        list = (ArrayList)list.clone();
        list.remove(list.size()-1);
        return list;
    }
    private ArrayList shift(ArrayList list) {
        list = (ArrayList)list.clone();
        list.remove(0);
        return list;
    }

    /**
     * This method should normally only be called if currentThingToDo == BETAREDUCE
     */
    private void didUserRemoveTheRightLambda(Expr expr, Expr answer, ArrayList hints, Set diagnoses) {
    	// TODO: We need same fix as in method above.
    	
        expr = expr.stripOutermostParens();
        answer = answer.stripOutermostParens();

        ArrayList correctvars = getLambdaVars(expr);
        ArrayList uservars = getLambdaVars(answer);

        // Did the user remove exactly one lambda?
        if (correctvars.size()-1 != uservars.size()) {
            hints.add("After each " + Lambda.SYMBOL + "-conversion, the expression has to \"lose\" its first " + Lambda.SYMBOL + "-slot.");
            return;
        }

        if (!uservars.equals(shift(correctvars))) {
            String response = "When doing " + Lambda.SYMBOL + "-conversion, start with the outermost " + Lambda.SYMBOL + ".";
            if (!diagnoses.contains("leftmost-leftmost"))
                response += " Remember, the leftmost " + Lambda.SYMBOL + "-slot corresponds with the leftmost argument to be " + Lambda.SYMBOL + "-converted.";
            hints.add(response);
        }
        
        return;
    }

    /**
     * This method returns the lambda variables from outside to in.
     * For example, in:
     * Lx.Ly.Lz ...
     * This method returns (x,y,z).
     */
    private ArrayList getLambdaVars(Expr expr) {
        ArrayList ret = new ArrayList();
        expr = expr.stripOutermostParens();
        while (expr instanceof FunApp)
            expr = ((FunApp)expr).getFunc().stripOutermostParens();
        
        while (expr instanceof Lambda) {
            Expr var = ((Lambda)expr).getVariable();
            Expr inside = ((Lambda)expr).getInnerExpr();
            ret.add(var);
            expr = inside.stripOutermostParens();
        }
        return ret;
    }

    private void didUserRenameAFreeVariableOrDidntRenameConsistently(Expr expr, Expr answer, ArrayList hints, Set diagnoses) {
        expr = expr.stripOutermostParens();
        answer = answer.stripOutermostParens();
        if (!(expr instanceof FunApp) || !(answer instanceof FunApp)) {
            // exactly one of them is a FunApp
            if (expr instanceof FunApp || answer instanceof FunApp) return; // make sure user didn't try a lambda conversion
            
            // we know at this point that neither of them is a FunApp

            // this function gets called recursively, therefore
            // expr (and answer) is the innermost function of a series of embedded function applications

            if (!expr.alphaEquivalent(answer)) {
                // either they differ in the renaming of some free var or in some more radical way
                
                // we guess the following...
                hints.add("This is an incorrect alphabetical variant. Remember that only bound variables can be renamed and that you must rename variables consistently, paying attention to how each variable is bound.");
                diagnoses.add("incorrect-alphavary");
            }
            return;
        }
        
        // both of them are FunApps
        
        FunApp fexpr = (FunApp)expr;
        FunApp fanswer = (FunApp)answer;
        if (fexpr.getFunc().alphaEquivalent(fanswer.getFunc())
                && !fexpr.getArg().alphaEquivalent(fanswer.getArg())) {
            hints.add("This is an incorrect alphabetical variant. Only bound variables can be rewritten as other variables while preserving truth conditions.");
            diagnoses.add("incorrect-alphavary");
        } else {
            didUserRenameAFreeVariableOrDidntRenameConsistently(fexpr.getFunc(), fanswer.getFunc(), hints, diagnoses);
        }
    }
    
    /**
     * This checks if the user did a beta reduction without doing a needed
     * alphabetical variant.
     *
     *
     */
    private void didUserSubstituteButNeedingAlphabeticalVariant(Expr expr, Expr answer, ArrayList hints) {
        try {
            Expr.LambdaConversionResult lcr = expr.performLambdaConversion();
            if (lcr == null) return;
            if (lcr.alphabeticalVariant == null) return;
            if (lcr.substitutionWithoutAlphabeticalVariant.alphaEquivalent(answer))
                hints.add("Your answer changed the truth conditions of the expression because a free variable was accidentally bound during substitution.");
        } catch (TypeEvaluationException ex) {
        }
    }
    
    /**
     * Returns the number of potential lambda conversions that could take place in
     * this expression.
     */
    private int getNumberOfLambdaConversions(Expr expr) {
        int convs = 0;
        
        if (expr instanceof FunApp) {
            FunApp fa = (FunApp)expr;
            if (fa.getFunc() instanceof Lambda)
                convs++;
        }
        
        java.util.List subexprs = expr.getSubExpressions();
        for (int i = 0; i < subexprs.size(); i++)
            convs += getNumberOfLambdaConversions((Expr)subexprs.get(i));
        
        return convs;
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(1); // format version marker
        expr.writeToStream(output);
        types.writeToStream(output);
        if (lastAnswer == null) {
            output.writeByte(0);
        } else {
            output.writeByte(1);
            lastAnswer.writeToStream(output);
        }
        output.writeShort(currentStep);
        output.writeBoolean(isParseSingleLetterIdentifiers());
        output.writeBoolean(isNotSoFast());
        // TODO: We're outputting a canonicalized version of what the student answered.
    }
    
    LambdaConversionExercise(java.io.DataInputStream input, int fileFormatVersion, int index) throws java.io.IOException, ExerciseFileFormatException {
        super(index);
        
        if (input.readShort() != 1) throw new ExerciseFileVersionException();
        
        this.expr = Expr.readFromStream(input);
        
        this.types = new IdentifierTyper();
        this.types.readFromStream(input, fileFormatVersion);

        if (input.readByte() == 1)
            this.lastAnswer = Expr.readFromStream(input);
        
        this.currentStep = input.readShort();
           
        setParseSingleLetterIdentifiers(input.readBoolean());
        setNotSoFast(input.readBoolean());
        
        try {
            initialize();
        } catch (Exception e) {
            System.err.println(e);
            throw new ExerciseFileFormatException();
        }
    }

    /**
     * Whether students are prohibited from skipping steps in multi-step problems.
     */
    public boolean isNotSoFast() {
        return notSoFast;
    }

    public void setNotSoFast(boolean notSoFast) {
        this.notSoFast = notSoFast;
    }

    public boolean isParseSingleLetterIdentifiers() {
        return parseSingleLetterIdentifiers;
    }

    public void setParseSingleLetterIdentifiers(boolean parseSingleLetterIdentifiers) {
        this.parseSingleLetterIdentifiers = parseSingleLetterIdentifiers;
    }
}
