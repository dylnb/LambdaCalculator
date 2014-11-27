/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
    private boolean correct, endsExercise;
    private String message;
    
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
