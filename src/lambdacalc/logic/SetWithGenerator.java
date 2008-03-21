/*
 * SetWithGenerator.java
 */

package lambdacalc.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of the form { x | P(x) }. Any unbound variables on the
 * left side of the pipe (the 'template') bind occurrences of that variable
 * in the right hand side (the 'filter'), and so we never do lambda conversion
 * replacement in the left hand side.
 * 
 * This notion of binding prevents expressions like:
 *    Lx.{ (x,y)  | loves(x,y) }
 * A function from individuals to pairs consisting of that individual plus
 * someone he loves. It could be written instead as:
 *    Lz.{ (x,y)  | x = z & loves(x,y) }
 * On the other hand, we don't have pair expressions anyway so this particular
 * example is irrelevant, but you could imagine something else.
 */
public class SetWithGenerator extends Binary implements VariableBindingExpr {

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

    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // This is adapted from Binder's implementations of this method.
        
        // If var is bound by the template of the generator, then we just return ourself directly
        // because var won't be replaced by the lambda conversion argument within this expression.
        if (getTemplate().getFreeVars().contains(var))
            return this;
        
        // Mark that this binder outscopes things in its scope, so that when we
        // get to a replacement, we know what variables would be accidentally
        // bound.
        Set binders2 = new HashSet(binders);
        binders2.add(this);
        
        // We're in the scope of a lambda conversion. Just recurse.
        return create(getLeft(), getRight().performLambdaConversion2(var, replacement, binders2, accidentalBinders));
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        // This is adapted from Binder's implementations of this method.
        
        Expr left = getLeft();
        
        if (bindersToChange.contains(this)) {
            // Choose a fresh variable replacement for each variable on the left hand side.
            for (Iterator i = getLeft().getFreeVars().iterator(); i.hasNext(); ) {
                Var v = (Var)i.next();
                Var vnew = createFreshVar(v, variablesInUse);
                
                // Push the variable and mapping onto the stack
                variablesInUse = new HashSet(variablesInUse);
                variablesInUse.add(vnew);
                updates = new HashMap(updates);
                updates.put(v, vnew);
                
                // Replace v with vnew in the left hand side
                left = left.replace(v, vnew);
            }
        }

        // Recurse
        return create(left, getRight().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
    
    public boolean bindsAny(Set vars) {
        Set boundvars = getTemplate().getFreeVars();
        for (Iterator fvs = vars.iterator(); fvs.hasNext(); ) {
            if (boundvars.contains(fvs.next()))
                return true;
        }
        return false;
    }
    
    SetWithGenerator(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
