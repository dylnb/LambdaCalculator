/*
 * AssignmentFunction.java
 *
 * Created on June 7, 2007, 8:47 PM
 *
 */

package lambdacalc.lf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lambdacalc.logic.Expr;
import lambdacalc.logic.GApp;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;


/**
 * A function from 
 * @author champoll
 */
public class AssignmentFunction {

    private Map map = new HashMap();

    private Map getUnderlyingMap() {
        return this.map;
    }
    
    /** Creates a new instance of AssignmentFunction */
    public AssignmentFunction() {
    }
    
    /** Creates a new instance of AssignmentFunction based on another AssignmentFunction. */
    public AssignmentFunction(AssignmentFunction copyFrom) {
        map = new HashMap(copyFrom.getUnderlyingMap());
    }

    public Expr applyTo(Expr e) {
        return e.replaceAll(map);
    }

    public Collection keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }
    
    public void put(int key, Var value) {
        map.put(new GApp(key,value.getType()), value);
    }
    
    public void put(BareIndex key, Var value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        map.put(new GApp(key.getIndex(),value.getType()), value);
    }

    public void put(GApp key, Var value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        map.put(key, value);
    }
    public void put(Object key, Object value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        if (!(key instanceof Integer) 
        && !(key instanceof GApp)
        && !(key instanceof BareIndex)) throw new IllegalArgumentException();
        if (!(value instanceof Var)) throw new IllegalArgumentException();
        
        if (key instanceof Integer) {
            this.put(((Integer) key).intValue(), (Var) value);
        } else if (key instanceof GApp) {
            this.put((GApp) key, (Var) value);
        } else if (key instanceof BareIndex) {
            this.put((BareIndex) key, (Var) value);
        }
            { // can't get here
            throw new RuntimeException(); 
        }
    }
    

    
    
    public Object get(Object key, Type type) {
        if (key instanceof Integer) {
            return map.get(new GApp(((Integer) key).intValue(),type));
        } else {
            return map.get(key);
        }
    }
    
    public Object get(int key, Type type) {
        return map.get(new GApp(key,type));
    }
    
    
    public String toString() {
        return "toString() not yet implemented";
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // version info
        output.writeInt(map.size());
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            GApp index = (GApp)i.next();
            Var var;
            try {
                var = (Var) get(index.getIndex(), index.getType());
            } catch (TypeEvaluationException ex) {
                // we don't expect it because getType on GApp never fails
                throw new RuntimeException();
            }
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
            map.put(index, var);
        }
    }
}
