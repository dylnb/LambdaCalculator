/*
 * FunApp.java
 *
 * Created on May 29, 2006, 4:25 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Set;

/**
 * Function application, in the case of both predicates
 * with arguments as well as lambda expressions applied
 * to arguments.
 * 
 * For example:
 *    pred-with-arg:   P(x,y) where (x,y) is an ArgList
 *    fun app:         (Lx.x) (a) where (a) is an ArgList
 * To create R(x,y): create (x,y) as an ArgList and then create R
 * as a constant of type <e*e, t>.
 */
public class FunApp extends Binary {
    /**
     * Constructs the function application.
     * @param func the function, usually a predicate (Const or Var)
     * or lambda expression (Lambda).
     * @param arg the argument
     */
    public FunApp(Expr func, Expr arg) {
        super(func, arg);
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        if (getFunc() instanceof Identifier)
            return 2;
        else
            return 8;
    }
    
    /**
     * Gets the function.
     */
    public Expr getFunc() {
        return getLeft();
    }
    
    /**
     * Gets the argument.
     */
    public Expr getArg() {
        return getRight();
    }
    
    public String toString() {
        String arg = getArg().toString();
        if (!(getArg() instanceof Parens) && !(getArg() instanceof ArgList))
            arg = "(" + arg + ")";
        if (!(getFunc() instanceof Identifier))
            arg = " " + arg;
        return getFunc().toString() + arg;
    }
    
    protected Binary create(Expr left, Expr right) {
        return new FunApp(left, right);
    }

    public Type getType() throws TypeEvaluationException {
        if (!(getFunc().getType() instanceof CompositeType))
            throw new TypeMismatchException(getFunc() + " cannot be applied to an argument because it is not a function.");
        
        CompositeType funcType = (CompositeType)getFunc().getType();
        Type domain = funcType.getLeft();
        
        Expr func = getFunc().stripAnyParens();
        String functype = (func instanceof Identifier ? "predicate" : "function");
        
        if (!(getArg() instanceof ArgList) && domain instanceof ProductType) {
            int arity = ((ProductType)domain).getArity();
            throw new TypeMismatchException(getFunc() + " is a " + functype + " that takes " + arity + " arguments but you provided only one argument.");
        } else if (getArg() instanceof ArgList && !(domain instanceof ProductType)) {
            if (functype.equals("predicate"))
                throw new TypeMismatchException(getFunc() + " is a one-place predicate but you provided more than one argument.");
            else
                throw new TypeMismatchException(getFunc() + " is a function which takes a single argument but you provided more than one argument.");
        } else if (getArg() instanceof ArgList && domain instanceof ProductType) {
            int actualarity = ((ArgList)getArg()).getArity();
            int formalarity = ((ProductType)domain).getArity();
            if (actualarity != formalarity)
                throw new TypeMismatchException(getFunc() + " is a " + functype + " that takes " + formalarity + " arguments but you provided " + actualarity + " arguments.");
            for (int i = 0; i < actualarity; i++) {
                Expr arg = ((ArgList)getArg()).getArgs()[i];
                Type actualtype = arg.getType();
                Type formaltype = ((ProductType)domain).getSubTypes()[i];
                if (!actualtype.equals(formaltype))
                    throw new TypeMismatchException(getFunc() + " is a " + functype + " whose " + getOrdinal(i) + " argument must be of type "
                            + formaltype + " but " + arg + " is of type " + actualtype + ".");
            }
        } else { // !ArgList and !ProductType
            Type actualtype = getArg().getType();
            Type formaltype = domain;
            if (functype.equals("predicate"))
                functype = "one-place " + functype;
            if (!actualtype.equals(formaltype))
                throw new TypeMismatchException(getFunc() + " is a " + functype + " whose argument must be of type "
                        + formaltype + " but " + getArg() + " is of type " + actualtype + ".");
        }
        
        return funcType.getRight();
    }
    
    private String getOrdinal(int index) {
        switch (index+1) {
            case 1: return "first";
            case 2: return "second";
            case 3: return "third";
            case 4: return "fourth";
            default:
                switch ((index+1) % 100) {
                    case 1: return (index+1) + "st";
                    case 2: return (index+1) + "nd";
                    case 3: return (index+1) + "rd";
                    default: return (index+1) + "th";
                }
        }
    }
    
    /**
     * Returns whether an alphabetical variant is needed in order
     * to simplify this function application.  This is applicable
     * (and returns true only) when the function is a lambda
     * expression.  An alphabetical variant is needed precisely
     * when substituting the argument (as is) for all unbound
     * instances of the lambda's variable would result in
     * a free variable in the argument being captured by a binder
     * scoping over an instance of the variable being substituted for.
     */
    public boolean needsAlphabeticalVariant() throws TypeEvaluationException {
        return simplification(0) == null;
    }
    
    /**
     * Simplifies the function application by replacing the argument for
     * all free occurrences of the Lambda's variable.  Returns null
     * if an alphabetical variant is needed first.  Returns itself
     * unchanged if the function application cannot be simplified because
     * the function is just a predicate (an Identifier).  Throws a
     * TypeMismatchException if the function is not of an appropriate
     * type to accept the argument (which is true if the function is
     * anything but a Lambda or an Identifier).
     */
    public Expr simplify() throws TypeEvaluationException {
        return simplification(1);
    }

    /**
     * Creates the alphabetical variant needed in order to perform
     * simplification.
     */
    public Expr createAlphabeticalVariant() throws TypeEvaluationException {
        return simplification(2);
    }

    /**
     * This does a simplification without checking whether any
     * free variables in the replacement would be accidentally
     * bound.
     */
    public Expr simplifyWithoutAlphabeticalVariant() throws TypeEvaluationException {
        return simplification(3);
    }
    
    /**
     * This performs a simplification but doesn't actually carry out the substitution
     * - e.g. Lx.x (a) simplifies to x
     */
    public Expr simplifyWithoutSubstitution() throws TypeEvaluationException {
        return simplification(4);
    }    

    // Mode is as follows:
    //  0 : test whether an alphabetical variant is needed
    //          return null if yes, non-null if no
    //  1 : perform a simplification
    //  2 : create alphabetical variant
    //  3 : simplify without checking for accidental bindings
    //  4 : perform a simplification but don't actually carry out the substitution
    //      e.g. Lx.x (a) simplifies to x
    private Expr simplification(int mode) throws TypeEvaluationException {
        assert mode >= 0 && mode <= 4;
        Expr func = getFunc();
        while (func instanceof Parens)
            func = ((Parens)func).getInnerExpr();
        
        // In the case of nested function applications, the structurally innermost one gets 
        // simplified first. 
        // E.g. in Lx.Ly.body (a) (b) 
        // the structurally innermost FA is Lx.Ly.body (a)
        if (func instanceof FunApp) {
            Expr inside = ((FunApp)func).simplification(mode);
            if (mode == 0) return inside;
            return new FunApp(inside, getArg());

            
        // Beta reduction
        } else if (func instanceof Lambda) {
            Lambda lambda = (Lambda)func;
            if (!(lambda.getVariable() instanceof Var))
                throw new ConstInsteadOfVarException("The bound identifier " + lambda.getVariable() + " must be a variable.");
            Var var =(Var)lambda.getVariable();
            
            // We undo the convention that the argument is often wrapped in parentheses
            Expr arg = getArg();
            if (arg instanceof Parens)
                arg = ((Parens)arg).getInnerExpr();
            
            if (mode == 0 || mode == 1) { // test if ok, or subst
                Expr result = lambda.getInnerExpr().substitute(var, arg); // returns null iff an alphavariant is needed
                if (mode == 0) return result;
                // else mode ==1, we're trying to perform a simplification
                if (result == null)
                    throw new RuntimeException("An alphabetical variant is needed.");
                return result;
            } else if (mode == 2) { // create variant
                return new FunApp(
                        new Lambda(var, lambda.getInnerExpr().createAlphabeticalVariant(var, arg), lambda.hasPeriod()),
                        getArg());
            } else if (mode == 3) {
                return lambda.getInnerExpr().substituteAll(var, arg);
            } else if (mode == 4) {
                return lambda.getInnerExpr();
            } else {
                throw new RuntimeException();
            }
            
        // P(a) -- can't be reduced further
        } else if (func instanceof Identifier) {
            return this; // non-null meaning simplification is possible (trivially possible, i.e. alphabetic variant not needed)

        } else {
            throw new TypeMismatchException("The left hand side of a function application must be a lambda expression or an identifier: " + func);
        }
    }
}
