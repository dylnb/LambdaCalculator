/*
 * TypeMismatchException.java
 *
 * Created on May 30, 2006, 5:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

/**
 * Thrown by Expr.getType() when the type of an expr can't be computed
 * because of a type mismatch, such as when an argument of an inappropriate
 * type is used as the argument of a function.
 */
public class TypeMismatchException extends TypeEvaluationException {
    public TypeMismatchException(String message) {
        super(message);
    }
}
