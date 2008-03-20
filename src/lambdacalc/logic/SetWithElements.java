/*
 * SetWithElements.java
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of the form { a, b, c ... }.
 */
public class SetWithElements extends NAry {
    Type elementType;
    
    /**
     * Constructs the set. This constructor is only for sets with at least
     * one element, and the elements must be of the same type.
     * @param innerExpressions an array of two or more expressions
     */
    public SetWithElements(Expr[] innerExpressions) {
        super(innerExpressions);
        if (innerExpressions.length == 0)
            throw new IllegalArgumentException("This constructor is only for non-null sets.");
    }
    
    /**
     * Constructs an empty set for elements of a particular type.
     * @param innerExpressions an array of two or more expressions
     */
    public SetWithElements(Type elementType) {
        super(new Expr[0]);
        if (elementType == null)
            throw new IllegalArgumentException("elementType cannot be null");
        this.elementType = elementType;
    }
    
    protected String getOpenSymbol() { return "{"; }

    protected String getCloseSymbol() { return "}"; }
    
    public Type getType() throws TypeEvaluationException {
        Type t = elementType;
        if (elementType == null) {
            // non-null set, check that the types of the elements are the same
            Expr[] elems = getElements();
            t = elems[0].getType();
            for (int i = 1; i < elems.length; i++)
                if (!elems[i].getType().equals(t))
                    throw new TypeEvaluationException("The elements of a set must all have the same type. The type of '" + elems[0] + "' is not the same as the type of '" + elems[i] + "'.");
        }
        return new CompositeType(t, Type.T); // the type of the characteristic function
    }

    public Expr createFromSubExpressions(Expr[] subExpressions)
     throws IllegalArgumentException {
        return new SetWithElements(subExpressions);
    }
    
    SetWithElements(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
