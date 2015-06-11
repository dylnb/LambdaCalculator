/*
 * Copyright (C) 2007-2014 Dylan Bumford, Lucas Champollion, Maribel Romero
 * and Joshua Tauberer
 * 
 * This file is part of The Lambda Calculator.
 * 
 * The Lambda Calculator is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Lambda Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with The Lambda Calculator.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * FunApp.java
 *
 * Created on May 29, 2006, 4:25 PM
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    
    public FunApp(Expr func, Expr arg, HashMap<Type,Type> alignments) {
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
            return 2;
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
    
    protected String toString(int mode) {
        String arg = getArg().toString(mode);
        String func = getFunc().toString(mode);
        if (!(getArg() instanceof Parens) && !(getArg() instanceof ArgList)) {
            arg = "(" + arg + ")";
        }
        if (!(getFunc() instanceof Identifier || getFunc() instanceof FunApp)) {
            arg = " " + arg;
            if (!(getFunc() instanceof Parens || getFunc() instanceof FunApp)) {
              func = "[" + func + "]";
            } else {
              if (getFunc() instanceof Parens) {
                ((Parens)getFunc()).setSquare();
              }
            }
        }
        return func + arg;
    }
    
    protected Binary create(Expr left, Expr right) {
        return new FunApp(left, right);
    }

    public Type getType() throws TypeEvaluationException {
        if (!(getFunc().getType() instanceof CompositeType))
            throw new TypeMismatchException
                    (getFunc() + " cannot be applied as a function " +
                    "to what looks like an argument to its right ("
                    + getArg().stripOutermostParens() + ") because " + getFunc()
                    + " is of type " + getFunc().getType()
                    + " according to the typing conventions in effect " +
                    "and therefore is not a function.");
        
        CompositeType funcType = (CompositeType)getFunc().getType();
        Type domain = funcType.getLeft();
        Type range = funcType.getRight();
        
        Expr func = getFunc().stripOutermostParens();
        String functype = (func instanceof Identifier ? "predicate" : "function");
        
        if (!(getArg() instanceof ArgList) && domain instanceof ProductType) {
            int arity = ((ProductType)domain).getArity();
            throw new TypeMismatchException
                    (getFunc() + " is a " + functype + " that takes " + arity + 
                    " arguments but you provided only one argument.");
        } else if (getArg() instanceof ArgList && !(domain instanceof ProductType)) {
            String str;
            if (functype.equals("predicate")) {
                str = "predicate";
            } else {
                str = "function";
            }


            if (range instanceof AtomicType) {
                throw new TypeMismatchException
                        (getFunc() + " is a " + str + " that takes a single " 
                        + domain + "-type argument, but you provided " +
                        "more than one argument.");
            } else {
                throw new TypeMismatchException
                        (getFunc() + " is a " + str + " that takes (first) a single " 
                        + domain + "-type argument alone, but you provided " +
                        "more than one argument. Rewrite your expression " +
                        "so that " + getFunc() + " is Sch\u00F6nfinkelized " +
                        "(i.e. each argument to " + getFunc() + " is " +
                        "surrounded by a separate " +
                        "pair of brackets).");
            }
        } else if (getArg() instanceof ArgList && domain instanceof ProductType) {
            int actualarity = ((ArgList)getArg()).getArity();
            int formalarity = ((ProductType)domain).getArity();
            if (actualarity != formalarity)
                throw new TypeMismatchException
                        (getFunc() + " is a " + functype + " that takes " 
                        + formalarity + " arguments but you provided " 
                        + actualarity + " arguments.");
            for (int i = 0; i < actualarity; i++) {
                Expr arg = ((ArgList)getArg()).getElements()[i];
                Type actualtype = arg.getType();
                Type formaltype = ((ProductType)domain).getSubTypes()[i];
                if (!actualtype.equals(formaltype))
                    throw new TypeMismatchException
                            (getFunc() + " is a " + functype + " whose " 
                            + getOrdinal(i) + " argument must be of type "
                            + formaltype + " but " + arg + " is of type " 
                            + actualtype + ".");
            }
        } else { // !ArgList and !ProductType
            Type actualtype = getArg().getType();
            Type formaltype = domain;
            if (functype.equals("predicate"))
                functype = "one-place " + functype;
            if (!actualtype.equals(formaltype))
                throw new TypeMismatchException(getFunc() + " is a " + functype 
                        + " whose argument must be of type "
                        + formaltype + " but " + getArg() + " is of type " 
                        + actualtype + ".");
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
        
        Expr func = getFunc().stripOutermostParens(); // we need to strip parens to see what it really is
        Expr arg = getArg().stripOutermostParens(); // undo the convention of parens around the argument
        
        // In the case of nested function applications, the structurally innermost one gets 
        // simplified first, so we just recurse down the tree. 
        // E.g. in Lx.Ly.body (a) (b) 
        // the structurally innermost FA is Lx.Ly.body (a)
        if (func instanceof FunApp) {
            Expr inside = func.performLambdaConversion1(accidentalBinders);
            if (inside != null)
                return new FunApp(inside, getArg()); // TODO why are we using getArg() and not just arg here?

        // If the function is in fact a Lambda, then we begin substitutions.
        } else if (func instanceof Lambda) {
            Lambda lambda = (Lambda)func;
            if (!(lambda.getVariable() instanceof Var))
                throw new ConstInsteadOfVarException
                        ("A variable must be bound by the " + Lambda.SYMBOL
                        + ", but " + lambda.getVariable() + " is a constant " +
                        "according to the typing conventions in effect.");
            Var var = (Var)lambda.getVariable();
            
            Expr inside = lambda.getInnerExpr().stripOutermostParens();
            
            Set binders = new HashSet(); // initialize for use down below
            return inside.performLambdaConversion2(var, arg, binders, accidentalBinders);
            
        // If the function is an identifier, it's OK, but we don't recurse into it.
        } else if (func instanceof Identifier) {

        // The same thing happens if the function is a GApp (from a higher-type trace).
        } else if (func instanceof GApp) {

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
