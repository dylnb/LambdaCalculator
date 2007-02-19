/*
 * Exercise.java
 *
 * Created on May 30, 2006, 4:30 PM
 */

package lambdacalc.exercises;

import lambdacalc.logic.*;

/**
 * An Exercise is the abstract base class of all individual exercises.
 * @author tauberer
 */
public abstract class Exercise implements java.io.Serializable {
    int index;
    boolean done;
    
    /**
     * Creates an Exercise with the given index in its ExerciseGroup.
     */
    public Exercise(int index) {
        this.index = index;
    }
    
    /**
     * Gets the zero-baed index of this Exercise in its ExerciseGroup.
     */
    public int getIndex() {
        return index;
    }
    
    public String toString() {
        return getExerciseText();
    }
    
    /**
     * Gets the text associated with the exercise.
     */
    public abstract String getExerciseText();
    
    /**
     * Gets a tip to be displayed grayed in the user input text field,
     * like "enter your answer here".
     */
    public abstract String getTipForTextField();
    
    /**
     * Gets a very short directive for the problem, like "Simplify the expression".
     */
    public abstract String getShortDirective();

    /**
     * Checks the status of an answer to the exercise.  Throws a SyntaxException
     * if the answer could not be understood at all.
     */
    public abstract AnswerStatus checkAnswer(String answer) throws SyntaxException;
    
    /**
     * Gets whether this Exercise has been totally completed.
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Called by Exercise implementations to indicate that the exercise has
     * been completed.
     */
    protected void setDone() {
        this.done = true;
    }
    
    public boolean hasBeenStarted() {
        return getLastAnswer() != null;
    }
    
    /**
     * If the exercise has been started, get's the last correct answer
     * given by the user.  If the exercise is finished, this is the
     * correct final answer.  Otherwise it is the last correct
     * intermediate answer given.  If the exercise hasn't been started
     * yet, returns null.
     */
    public abstract String getLastAnswer();

    public abstract void writeToStream(java.io.DataOutputStream output) throws java.io.IOException;
}
