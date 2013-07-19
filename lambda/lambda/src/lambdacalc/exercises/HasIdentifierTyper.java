/*
 * HasIdentifierTyper.java
 *
 * Created on June 5, 2006, 6:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

import lambdacalc.logic.IdentifierTyper;

/**
 * An interface implemented by Exercises that are associated with an IdentifierTyper
 * for naming conventions of identifiers.
 */
public interface HasIdentifierTyper {
    /**
     * Gets the naming conventions for the exercise.
     */
    IdentifierTyper getIdentifierTyper();
}
