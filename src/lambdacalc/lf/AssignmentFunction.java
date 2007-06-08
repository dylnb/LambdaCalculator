/*
 * AssignmentFunction.java
 *
 * Created on June 7, 2007, 8:47 PM
 *
 */

package lambdacalc.lf;

import java.util.HashMap;
import lambdacalc.logic.Var;

/**
 *
 * @author champoll
 */
public class AssignmentFunction extends HashMap {
    
    /** Creates a new instance of AssignmentFunction */
    public AssignmentFunction() {
    }
    
    public AssignmentFunction put(int key, Var value) {
        return (AssignmentFunction) put(new Integer(key), value);
    }
    
    public Object put(Object key, Object value) {
        if (!(key instanceof Integer)) throw new IllegalArgumentException();
        if (!(value instanceof Var)) throw new IllegalArgumentException();
        
        return super.put(key, value);
    }
    
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    public String toString() {
        return "Not yet implemented";
    }
    
}
