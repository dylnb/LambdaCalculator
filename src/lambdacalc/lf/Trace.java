/*
 * Trace.java
 *
 * Created on June 5, 2007, 5:42 PM
 *
 */

package lambdacalc.lf;

/**
 *
 * @author champoll
 */
public class Trace extends Terminal {
    
    public static final String SYMBOL = "t";
    
    /** Creates a new instance of Trace */
    private Trace() {
    }
    
    public Trace(int index) {
        this.index = index;
        this.label = SYMBOL;
    }
    
    public String getLabel() {
        return SYMBOL;
    }
    
    public void setLabel(String label) {
        throw new UnsupportedOperationException("Tried to set the label of a trace.");
    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a trace.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a trace.");
    }
    
}
