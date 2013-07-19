/*
 * PropositionalBinder.java
 *
 * Created on May 30, 2006, 5:28 PM
 */

package lambdacalc.logic;

/**
 * Abstract base class of the propositional binders (universal and existential quantification).
 */
public abstract class PropositionalBinder extends Binder {
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public PropositionalBinder(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 4;
    }
    
    public Type getType() throws TypeEvaluationException {
        checkVariable();
        if (!getInnerExpr().getType().equals(Type.T))
            throw new TypeEvaluationException("The inside of the propositional binder in " + toString() + " must be of type t.");
        return Type.T;
    }
    
    PropositionalBinder(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
