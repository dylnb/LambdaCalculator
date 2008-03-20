/*
 * ArgList.java
 *
 * Created on May 31, 2006, 4:14 PM
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a list of two or more arguments to a predicate, i.e. a vector.
 * ArgLists are not used for the arguments of one-place
 * predicates, which are represented by Identifiers.  
 */
public class ArgList extends NAry {
    /**
     * Constructs the ArgList.
     * @param innerExpressions an array of two or more expressions
     */
    public ArgList(Expr[] innerExpressions) {
        super(innerExpressions);
    }
    
    protected String getOpenSymbol() { return "("; }

    protected String getCloseSymbol() { return ")"; }
    
    public Type getType() throws TypeEvaluationException {
        Type[] t = new Type[getArity()];
        for (int i = 0; i < t.length; i++)
            t[i] = getElements()[i].getType();
        return new ProductType(t);
    }

    public Expr createFromSubExpressions(Expr[] subExpressions)
     throws IllegalArgumentException {
        return new ArgList(subExpressions);
    }
    
    ArgList(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
