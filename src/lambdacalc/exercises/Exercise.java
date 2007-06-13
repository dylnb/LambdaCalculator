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
public abstract class Exercise {
    int index;
    boolean done;
    java.math.BigDecimal points = java.math.BigDecimal.valueOf(1); // because floats might do weird rounding
    String instructions;
    
    /**
     * Creates an Exercise with the given index in its ExerciseGroup.
     */
    public Exercise(int index) {
        this.index = index;
    }
    
    /**
     * Gets the zero-based index of this Exercise in its ExerciseGroup.
     */
    public int getIndex() {
        return index;
    }
    
    public String toString() {
        return getExerciseText();
    }
    
    /**
     * Gets the number of points associated with the exercise.
     */
    public java.math.BigDecimal getPoints() {
        return this.points;
    }
    
    /**
     * Sets the number of points associated with the exercise.
     * @param points the number of points awarded for a correct answer on this problem
     */
    public void setPoints(java.math.BigDecimal points) {
        this.points = points;
    }
    
    /**
     * Gets optional instruction text for the exercise.
     * @return instructional text, or null
     */
    public String getInstructions() {
        return instructions;
    }
    
    /**
     * Sets optional instruction text for the exercise.
     * @param instructions instructional text, or null
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
    
    /**
     * Returns whether the user has provided a correct answer for this exercise.
     * For one-step exercises, this returns true just when the exercise has been
     * correctly completed. For multi-step exercises, this returns just when the user
     * has given a correct intermediate step.
     */
    public boolean hasBeenStarted() {
        return getLastAnswer() != null;
    }
    
    /**
     * Resets an exercise to its pristine unanswered state.
     */
    public void reset() {
        this.done = false;
    }
    
    /**
     * If the exercise has been started, gets the last correct answer
     * given by the user.  If the exercise is finished, this is the
     * correct final answer.  Otherwise it is the last correct
     * intermediate answer given.  If the exercise hasn't been started
     * yet, returns null.
     */
    public abstract String getLastAnswer();

    /**
     * Serialzies the exercise to a stream.
     */
    public abstract void writeToStream(java.io.DataOutputStream output) throws java.io.IOException;
}
