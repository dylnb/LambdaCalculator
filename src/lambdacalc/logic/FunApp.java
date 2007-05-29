/*
 * FunApp.java
 *
 * Created on May 29, 2006, 4:25 PM
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Set;

/**
 * This class models lambda expressions applied to arguments
 * (traditional function application) as well as predicates with
 * arguments.
 * 
 * For example:
 *    pred-with-arg:   P(x,y) where (x,y) is an ArgList
 *    fun app:         (Lx.x) (a) where (a) is an ArgList
 *
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
    
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda to convert...
        
        Expr func = getFunc().stripAnyParens(); // we need to strip parens to see what it really is
        Expr arg = getArg().stripAnyParens(); // undo the convention of parens around the argument
        
        // In the case of nested function applications, the structurally innermost one gets 
        // simplified first, so we just recurse down the tree. 
        // E.g. in Lx.Ly.body (a) (b) 
        // the structurally innermost FA is Lx.Ly.body (a)
        if (func instanceof FunApp) {
            Expr inside = func.performLambdaConversion1(accidentalBinders);
            if (inside != null)
                return new FunApp(inside, getArg());

        // If the function is in fact a Lambda, then we begin substitutions.
        } else if (func instanceof Lambda) {
            Lambda lambda = (Lambda)func;
            if (!(lambda.getVariable() instanceof Var))
                throw new ConstInsteadOfVarException("The bound identifier " + lambda.getVariable() + " must be a variable.");
            Var var = (Var)lambda.getVariable();
            
            Expr inside = lambda.getInnerExpr().stripAnyParens();
            
            Set binders = new HashSet(); // initialize for use down below
            return inside.performLambdaConversion2(var, arg, binders, accidentalBinders);
            
        // If the function is an identifier, it's OK, but we don't recurse into it.
        } else if (func instanceof Identifier) {

        } else {
            throw new TypeMismatchException("The left hand side of a function application must be a lambda expression or a function-typed constant or variable: " + func);
        }
        
        // If we've gotten here, then no lambda conversion took place within
        // our scope. That means that we must see if we can do any lambda conversion
        // in the argument.
        Expr arglc = getArg().performLambdaConversion1(accidentalBinders);
        
        // If even there no lambda conversions are possible, then return null to
        // signify that nothing happened.
        if (arglc == null)
            return null;
        
        // Otherwise, the arg did do a lambda conversion, so we reconstruct ourself.
        return new FunApp(getFunc(), arglc);
    }
    
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        // We're in the scope of a lambda. In that case, we keep performing substitutions
        // in our function and in our argument.
        return new FunApp(
                getFunc().performLambdaConversion2(var, replacement, binders, accidentalBinders),
                getArg().performLambdaConversion2(var, replacement, binders, accidentalBinders));
    }

    FunApp(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
