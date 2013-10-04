/*
 * BadCharacterException.java
 *
 * Created on June 8, 2006, 2:37 PM
 */

package lambdacalc.logic;

/**
 * This subclass of SyntaxException is thrown in the parser classes
 * upon encountering an invalid character.
 */
public class BadCharacterException extends SyntaxException {
    
    /** Creates a new instance of BadCharacterException */
    public BadCharacterException(String message, int position) {
        super(message, position);
    }
    
}
