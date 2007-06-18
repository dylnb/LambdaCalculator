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
        super("The type of the constant or variable '" + identifier + "' is not known. Check the typing conventions that are in effect, or use subscript (underscore) notation to give its type explicitly, such as '" + identifier + "_e' to make it type e.");
        id = identifier;
    }
    
    public String getIdentifier() {
        return id;
    }
}
