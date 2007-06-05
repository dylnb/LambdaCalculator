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
            PredicateModificationRule.INSTANCE
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
        return super.add(o);
    }
    
    public boolean addAll(Collection c) {
        if (!isCompositionRuleCollection(c)) {
            throw new IllegalArgumentException();
        }
        return super.addAll(c);
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
}
