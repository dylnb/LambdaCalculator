/*
 * ExerciseFileVersionException.java
 *
 * Created on May 21, 2007, 8:35 PM
 */

package lambdacalc.exercises;

/**
 * This exception is thrown when reading an exercise file from a previous
 * version of Lambda that can no longer be opened.
 */
public class ExerciseFileVersionException extends ExerciseFileFormatException {
    
    /** Creates a new instance of ExerciseFileVersionException */
    public ExerciseFileVersionException() {
        super("This exercise file was created in a previous or later version of Lambda and cannot be opened in this version.");
    }
    
}
