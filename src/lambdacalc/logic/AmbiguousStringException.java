/*
 * AmbiguousStringException.java
 */

package lambdacalc.logic;

import java.util.Vector;

/**
 * This subclass of SyntaxException is thrown by the ExpressionParser
 * when it is given an ambiguous string to parse.
 */
public class AmbiguousStringException extends SyntaxException {
    Vector alternatives;

    /**
     * Constructs an instance with the given message and a set
     * of possible resolutions of the ambiguity.
     * @param message the message explaining the ambiguity
     * @param alternatives a Vector of suggested alternatives
     * to the input that would resolve the ambiguity. The elements
     * in the Vector must be strings.
     */
    public AmbiguousStringException(String message, Vector alternatives) {
        super(message
            + (alternatives != null && alternatives.size() > 0 ?
                  ": " + stringify(alternatives) : "")
            , -1);
        this.alternatives = alternatives;
    }
    
    private static String stringify(Vector alternatives) {
        String ambiguity = "";
        for (int i = 0; i < alternatives.size(); i++) {
            if (i > 0) ambiguity += ", ";
            ambiguity += (String)alternatives.get(i);
        }
        return ambiguity;
    }
    
    /**
     * Gets the character position in which the syntax
     * error occurred.
     */
    public int getPosition() {
        return position;
    }
}
