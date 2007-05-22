/*
 * AssignmentFn.java
 *
 * Created on May 31, 2006, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.logic;

import java.util.HashMap;

/**
 * Assignment function from variables to values. This is a type-safe HashMap.
 * @author lucas
 */
public class AssignmentFn extends HashMap {
    
    // returns previous value associated with specified key, or null if 
    // there was no mapping for key. (Taken from javadoc Map)
    public Object put(Object key, Object value) {
        
        if (key == null || value == null) 
            throw new IllegalArgumentException();
        if ((key instanceof Var) && (value instanceof Expr)) {
            return put((Var) key, (Expr) value);
        } else {
            throw new IllegalArgumentException
              ("Tried to assign something else than a variable to an expression.");
        }
    }
    
    public Var put(Var key, Expr value) { 
 
        if (key == null || value == null) 
            throw new IllegalArgumentException();
         
        return (Var) super.put((Var) key, (Expr) value);
       
    }
    
    public Object get(Object key) {
        
        if (key == null || (!(key instanceof Var))) 
            throw new IllegalArgumentException();
        
        return get((Var) key);
    }
    
    public Var get(Var key) {
        
        if (key == null || (!(key instanceof Var))) 
            throw new IllegalArgumentException();
        
        return (Var) super.get((Var) key);
    }
    
}
