/*
 * SetWithGenerator.java
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of the form { x | P(x) }.
 */
public class SetWithGenerator extends Binary {

    public SetWithGenerator(Expr template, Expr filter) {
        super(template, filter);
    }
    
    public Expr getTemplate() { return getLeft(); }
    
    public Expr getFilter() { return getRight(); }

    protected String toString(boolean html) {
        return "{ " + getTemplate() + " | " + getFilter() + " }";
    }

    public final int getOperatorPrecedence() {
        return 0;
    }

    public Type getType() throws TypeEvaluationException {
        if (!getFilter().getType().equals(Type.T))
            throw new TypeMismatchException("The right-hand part of the set " + toString() + " must have type t.");
        return new CompositeType(getTemplate().getType(), Type.T);
    }
    
    protected Binary create(Expr left, Expr right) {
        return new SetWithGenerator(left, right);
    }
    
    // copied from LogicalBinary
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda to convert. If we can do a conversion on the left,
        // don't do a conversion on the right!
        Expr a = getLeft().performLambdaConversion1(accidentalBinders);
        if (a != null)
            return create(a, getRight());
        
        Expr b = getRight().performLambdaConversion1(accidentalBinders);
        if (b != null)
            return create(getLeft(), b);
        
        return null;
    }    

    // copied from LogicalBinary
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're in the scope of a lambda conversion. Just recurse.
        return create(getLeft().performLambdaConversion2(var, replacement, binders, accidentalBinders),
                getRight().performLambdaConversion2(var, replacement, binders, accidentalBinders));
    }

    SetWithGenerator(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
