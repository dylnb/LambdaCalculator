/*
 * TypeEvaluationException.java
 *
 * Created on June 6, 2006, 10:56 AM
 */

package lambdacalc.logic;

/**
 * This exception (or subclasses) is thrown by Expr.getType()
 * when there is a problem in an Expr preventing the
 * type from being determined.  This has nothing to do with
 * the exercises where the user enters a type.
 */
public class TypeEvaluationException extends Exception {
    
    public TypeEvaluationException(String message) {
        super(message);
    }
    
}
