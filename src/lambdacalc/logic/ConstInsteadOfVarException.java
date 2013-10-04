/*
 * ConstantInsteadOfVarException.java
 *
 * Created on June 6, 2006, 10:59 AM
 */

package lambdacalc.logic;

/**
 * Thrown by Expr.getType() when a constant is found where a variable
 * needs to be, such as as the variable of a binder.
 */
public class ConstInsteadOfVarException extends TypeEvaluationException {
    
    /** Creates a new instance of ConstantInsteadOfVarException */
    public ConstInsteadOfVarException(String message) {
        super(message);
    }
    
}
