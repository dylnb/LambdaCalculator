/*
 * ExerciseFileFormatException.java
 *
 * Created on May 31, 2006, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

/**
 *
 * @author tauberer
 */
public class ExerciseFileFormatException extends Exception {
    
    public ExerciseFileFormatException() {
        super("This does not appear to be a valid exercise file.");
    }
    
    public ExerciseFileFormatException(String message) {
        super(message);
    }

    /** Creates a new instance of ExerciseFileFormatException */
    public ExerciseFileFormatException(String message, int linenumber, String line) {
        super("On line " + linenumber + ": " + line + " the following exception occurred: " + message);
    }
    
}
