/*
 * BareIndex.java
 *
 * Created on June 5, 2007, 8:42 PM
 *
 */

package lambdacalc.lf;

/**
 *
 * @author champoll
 */
public class BareIndex extends Terminal {
    
    
    /** Creates a new instance of BareIndex */
    private BareIndex() {
    }
    
    public String getLabel() {
        return "";
    }
    
    public void setLabel(String label) {
        throw new UnsupportedOperationException("Tried to set the label of a bare index.");
    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
    }
}
