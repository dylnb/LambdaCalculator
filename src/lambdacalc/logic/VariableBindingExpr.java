/*
 * VariableBindingExpr.java
 *
 * Created on March 21, 2008, 7:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

import java.util.Set;

/**
 * An interface implemented by Expr's that bind variables.
 */
public interface VariableBindingExpr {
    
    /**
     * Returns whether any variables in the set vars are bound by this binder.
     */
    boolean bindsAny(Set vars);
    
}
