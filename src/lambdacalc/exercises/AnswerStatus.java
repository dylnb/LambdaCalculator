/*
 * AnswerStatus.java
 *
 * Created on May 30, 2006, 4:37 PM
 */

package lambdacalc.exercises;

/**
 * This class represents the status of an answer to an exercise
 * provided by the user.
 */
public class AnswerStatus {
    boolean correct, endsExercise;
    String message;
    
    private AnswerStatus(boolean correct, boolean endsExercise, String message) {
        this.correct = correct;
        this.endsExercise = endsExercise;
        this.message = message;
    }
    
    /**
     * Gets whether this status represents a correct answer.
     */
    public boolean isCorrect() { return correct; }
    
    /**
     * Gets whether this status represents an answer that completes
     * an exercise, i.e. that it isn't an intermediate step.
     */
    public boolean endsExercise() { return endsExercise; }
    
    /**
     * Gets a message associated with the answer.  This doesn't apply
     * for correct, endsExercise answers, only intermediate correct
     * answers and wrong answers.
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Creates an AnswerStatus for a correct final answer.
     */
    public static AnswerStatus CorrectFinalAnswer(String message) {
        return new AnswerStatus(true, true, message);
    }

    /**
     * Creates an AnswerStatus for a correct intermediate step answer,
     * with a message indicating what the user should do next.
     */
    public static AnswerStatus CorrectStep(String message) {
        return new AnswerStatus(true, false, message);
    }

    /**
     * Creates an AnswerStatus for an incorrect answer, with a message
     * indicating what the user did wrong.
     */
    public static AnswerStatus Incorrect(String message) {
        return new AnswerStatus(false, false, message);
    }
}
