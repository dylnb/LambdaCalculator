/*
 * Terminal.java
 *
 * Created on June 13, 2007, 1:44 PM
 *
 */

package lambdacalc.lf;

/**
 *
 * @author champoll
 */
public abstract class Terminal extends LFNode {
    protected Terminal() {
        super();
    }
    
    protected Terminal(String label, int index) {
        super(label, index);
    }

}
