/*
 * IdentifierTypeUnknownException.java
 *
 * Created on May 30, 2006, 5:18 PM
 */

package lambdacalc.logic;

/**
 * Thrown by the IdentifierTyper class when the type of an
 * identifier cannot be determined.
 */
public class IdentifierTypeUnknownException extends Exception {
    
    String id;
    
    public IdentifierTypeUnknownException(String identifier) {
        super("The type of identifier " + identifier + " cannot be determined.");
        id = identifier;
    }
    
}
