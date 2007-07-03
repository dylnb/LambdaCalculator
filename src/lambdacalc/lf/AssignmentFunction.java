/*
 * AssignmentFunction.java
 *
 * Created on June 7, 2007, 8:47 PM
 *
 */

package lambdacalc.lf;

import java.util.HashMap;
import java.util.Iterator;
import lambdacalc.logic.GApp;
import lambdacalc.logic.Var;

/**
 * A function from 
 * @author champoll
 */
public class AssignmentFunction extends HashMap {
    
    /** Creates a new instance of AssignmentFunction */
    public AssignmentFunction() {
    }
    
    /** Creates a new instance of AssignmentFunction based on another AssignmentFunction. */
    public AssignmentFunction(AssignmentFunction copyFrom) {
        super(copyFrom);
    }
    
    public AssignmentFunction put(int key, Var value) {
        return (AssignmentFunction) put(new GApp(key), value);
    }
    
    public Object put(Integer key, Var value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        return super.put(new GApp((Integer) key), value);
    }
    
    public Object put(BareIndex key, Var value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        return super.put(new GApp(key.getIndex()), value);
    }

    public Object put(GApp key, Var value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        return super.put(key, value);
    }
    public Object put(Object key, Object value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        if (!(key instanceof Integer) 
        && !(key instanceof GApp)
        && !(key instanceof BareIndex)) throw new IllegalArgumentException();
        if (!(value instanceof Var)) throw new IllegalArgumentException();
        
        if (key instanceof Integer) {
            return this.put((Integer) key, (Var) value);
        } else if (key instanceof GApp) {
            return this.put((GApp) key, (Var) value);
        } else if (key instanceof BareIndex) {
            return this.put((BareIndex) key, (Var) value);
        }
            { // can't get here
            throw new RuntimeException(); 
        }
    }
    
    public boolean containsKey(Object key) {
        if (key instanceof Integer) {
            return super.containsKey(new GApp((Integer) key));
        } else if (key instanceof BareIndex) {
            return super.containsKey(new GApp(((BareIndex) key).getIndex()));
        } else {
            return super.containsKey(key);
        }
    }
    
    public boolean containsKey(int key) {
        return super.containsKey(new GApp(key));
    }        
    
    public Object get(Object key) {
        if (key instanceof Integer) {
            return super.get(new GApp((Integer) key));
        } else {
            return super.get(key);
        }
    }
    
    public Object get(int key) {
        return super.get(new GApp(key));
    }
    
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }
    
    public String toString() {
        return "toString() not yet implemented";
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // version info
        output.writeInt(size());
        for (Iterator i = keySet().iterator(); i.hasNext(); ) {
            GApp index = (GApp)i.next();
            Var var = (Var)get(index.getIndex());
            output.writeInt(index.getIndex());
            var.writeToStream(output);
        }
    }
    
    public void readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        if (input.readByte() != 0)
            throw new java.io.IOException("Data format error.");
        
        int n = input.readInt();
        for (int i = 0; i < n; i++) {
            Integer index = new Integer(input.readInt());
            Var var = (Var)lambdacalc.logic.Expr.readFromStream(input);
            put(index, var);
        }
    }
}
