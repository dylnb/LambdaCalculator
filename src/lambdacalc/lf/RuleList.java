/*
 * RuleList.java
 *
 * Created on June 5, 2007, 1:21 PM
 *
 */

package lambdacalc.lf;

import java.util.*;

/**
 *
 * @author champoll
 */
public class RuleList extends Vector {
    
    public static final RuleList HEIM_KRATZER 
            = new RuleList(new CompositionRule[] {
            FunctionApplicationRule.INSTANCE,
            NonBranchingRule.INSTANCE,
            PredicateModificationRule.INSTANCE,
            LambdaAbstractionRule.INSTANCE
          // add other Heim & Kratzer rules here as we implement them
    });
  
    public RuleList() {
        super();
    }
            
    public RuleList(CompositionRule[] rules) {
        super(Arrays.asList(rules));
    }
    
    public boolean add(Object o) {
        if (!(o instanceof CompositionRule)) throw new IllegalArgumentException();
        if (contains(o)) return false;
        return super.add(o);
    }
    
    public boolean addAll(Collection c) {
        for (Iterator i = c.iterator(); i.hasNext(); )
            add(i.next());
        return true;
    }
        
    public boolean contains(Object o) {
        if (!(o instanceof CompositionRule)) {
            return false;
        } // else
        return super.contains(o);
    }
        
    public boolean equals(Object o) {
        if (!(o instanceof RuleList)) return false;
        return super.equals(o);
    }
            
    private boolean isCompositionRuleCollection(Collection c) {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (!(o instanceof CompositionRule)) {
                return false;
            }
        }
        return true;
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // versioning info for future use
        output.writeInt(size());
        
        for (Iterator i = iterator(); i.hasNext(); ) {
            CompositionRule r = (CompositionRule)i.next();
            CompositionRule.writeToStream(r, output);
        }
    }
    
    public void readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        if (input.readByte() != 0) throw new java.io.IOException("Data format error."); // sanity check
        
        int nRules = input.readInt();
        
        for (int i = 0; i < nRules; i++) {
            add(CompositionRule.readFromStream(input));
        }
    }
}
