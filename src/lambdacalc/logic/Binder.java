/*
 * Binder.java
 *
 * Created on May 29, 2006, 3:25 PM
 *
 * @author Lucas Champollion
 * @author Maribel Romero
 * @author Josh Tauberer
 */

package lambdacalc.logic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Abstract base class of the binders, including the propositional binders 
 * For All and Exists, the Iota operator, the Gamma operator, and Lambda.
 */
public abstract class Binder extends Expr implements VariableBindingExpr {
    
    private Identifier ident; 
    // ident = what the binder binds.
    // in "correct" lambda calculus this must be a variable.
    // but to capture student errors, we also allow it to be a constant.
    
    private Expr innerExpr;
    private boolean hasPeriod;

    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Binder(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        if (ident == null) throw new IllegalArgumentException();
        if (innerExpr == null) throw new IllegalArgumentException();
        
        this.ident=ident;
        this.innerExpr=innerExpr;
        this.hasPeriod = hasPeriod;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {
        
        // ignore parentheses for equality test
        e = e.stripOutermostParens();

        if (e instanceof Binder) {
            return this.equals((Binder) e, useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
        } else {
            return false;           
        }
    }
    
    
    public int hashCode() {
        return this.getVariable().hashCode() ^ super.hashCode();
    }    
    
    private boolean equals(Binder b, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, Map freeVarMap) {
        if (useMaps) {
            thisMap = (thisMap == null) ? new HashMap() : new HashMap(thisMap);
            otherMap = (otherMap == null) ? new HashMap() :  new HashMap(otherMap);

            // this represents a new fresh variable that both sides'
            // variables are equated with
            Object freshObj = new Object();

            // map both sides' variables to the new fresh variable
            thisMap.put(this.getVariable(), freshObj);
            otherMap.put(b.getVariable(), freshObj);
        }
        
        return (this.getClass() == b.getClass()) // same type of binder; lambda, exists, all...
             && (useMaps || this.getVariable().equals(b.getVariable())) // if not using maps, then variables must match
             && this.getInnerExpr().equals(b.getInnerExpr(),
                    useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
    }

    
    /**
     * Gets the unicode symbol associated with the binder.
     */
    public abstract String getSymbol(); // lambda, exists, forall

    public abstract String getLatexSymbol();
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of binder, with the given variable and inner expression,
     * and the same value as hasPeriod.
     */
    protected abstract Binder create(Identifier variable, Expr innerExpr);
    
    /**
     * Returns a List of all the subexpressions of this binder,
     * that is, the variable and the inner expression.
     * @return a list
     */
    public List getSubExpressions() {
        Vector result = new Vector(2);
        result.add(this.getVariable());
        result.add(this.getInnerExpr());
        return result;
    }
    
    /**
     * Creates a new Binder  using all the subexpressions given, taking the
     * value of the hasPeriod argument from this instance
     *
     * @param subExpressions the list of subexpressions
     * @throws IllegalArgumentException if the list does not contain exactly two
     * subexpressions 
     * @return a new expression of the same runtime type as this
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        if (subExpressions.size() != 2) 
            throw new IllegalArgumentException("List does not contain exactly two arguments");
        return create((Identifier) subExpressions.get(0), (Expr) subExpressions.get(1));
    }    
    
    /**
     * Gets the variable bound by the identifier.
     */
    public Identifier getVariable() { // before the dot (if any)
        return ident;
    }

    /**
     * Gets the inside expression of the binder.
     */
    public Expr getInnerExpr() { // after the dot (if any)
        return innerExpr;
    }

    /**
     * Gets whether the string representation of the binder
     * includes a period after the identifier.
     */
    public boolean hasPeriod() {
        return hasPeriod;
    }
    
    protected Set getVars(boolean unboundOnly) {
        Set ret = getInnerExpr().getVars(unboundOnly);
        if (unboundOnly)
            ret.remove(getVariable());
        else
            ret.add(getVariable()); // in case it is vacuous
        return ret;
    }
    
    /**
     * A convenience method for derived classes that throws the
     * ConstInsteadOfVarException precisely when the variable
     * bound by this binder is given as a Const, rather than Var.
     */
    protected void checkVariable() throws ConstInsteadOfVarException {
        if (getVariable() instanceof Const)
            throw new ConstInsteadOfVarException("The symbols " + Lambda.SYMBOL + ", " + Exists.SYMBOL + ", and " + ForAll.SYMBOL + " must be followed by a variable, but '" + getVariable() + "' is a constant.");
    }

    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda to convert, but even if this is a lambda, we don't
        // do anything special here. That's handled in FunApp.
        
        Expr inside = getInnerExpr().performLambdaConversion1(accidentalBinders);
        
        if (inside == null) // nothing happened, return null
            return null;
        
        return create(getVariable(), inside);
    }
    
    protected Expr performLambdaConversion2(Var var, Expr replacement, Set binders, Set accidentalBinders) throws TypeEvaluationException {
        if (getVariable().equals(var)) return this; // no binding of var occurs within this scope
        
        // Mark that this binder outscopes things in its scope, so that when we
        // get to a replacement, we know what variables would be accidentally
        // bound.
        Set binders2 = new HashSet(binders);
        binders2.add(this);
        
        return create(getVariable(), getInnerExpr().performLambdaConversion2(var, replacement, binders2, accidentalBinders));
    }

    protected Expr createAlphabeticalVariant(Set bindersToChange, Set variablesInUse, Map updates) {
        Identifier v = getVariable();

        if (bindersToChange.contains(this)) {
            // Choose a fresh variable
            if (v instanceof Const)
                v = new Var(v.getSymbol(), v.getType(), v.isTypeExplicit()); // very odd, but we need it to be a variable so that we can call createFreshVar
            v = createFreshVar((Var)v, variablesInUse);
            
            // Push the variable and mapping onto the stack
            variablesInUse = new HashSet(variablesInUse);
            variablesInUse.add(v);
            updates = new HashMap(updates);
            updates.put(getVariable(), v);
        }

        // Recurse
        return create(v, getInnerExpr().createAlphabeticalVariant(bindersToChange, variablesInUse, updates));
    }
    
    public Expr createAlphatypicalVariant(HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        Identifier v = getVariable();
        Type vtype = v.getType();
                
        if (vtype instanceof AtomicType) {
            if (alignments.containsKey(vtype)) {
                if (v instanceof Const)
                    v = new Var(v.getSymbol(), v.getType(), v.isTypeExplicit());
                variablesInUse = new HashSet(variablesInUse);
                variablesInUse.add(v);
                v = createFreshVar((Var)v, variablesInUse);
                v.setType(alignments.get(vtype));
                
                variablesInUse.add(v);
                updates = new HashMap(updates);
                updates.put(getVariable(), v);
            }
        } else if (vtype instanceof CompositeType) {
            CompositeType compvtype = (CompositeType)vtype;
            if (!Collections.disjoint(alignments.keySet(), compvtype.getAtomicTypes())) {
                Type newtype = getAlignedType(compvtype, alignments);
                variablesInUse = new HashSet(variablesInUse);
                variablesInUse.add(v);
                v = createFreshVar((Var)v, variablesInUse);
                v.setType(newtype);
                
                variablesInUse.add(v);
                updates = new HashMap(updates);
                updates.put(getVariable(), v);
            }
        }        
        return create(v, getInnerExpr().createAlphatypicalVariant(alignments, variablesInUse, updates));
    }
    
    
    public boolean bindsAny(Set vars) {
        Identifier bvi = getVariable();
        for (Iterator fvs = vars.iterator(); fvs.hasNext(); ) {
            Var fv = (Var)fvs.next();
            if (fv.equals(bvi))
                return true;
        }
        return false;
    }
    
    /**
     * Returns true if the prescriptively "right" way to write this binder
     * consists in using a dot, i.e. if this binder should be displayed with
     * a dot in program output.
     */
    public abstract boolean dotPolicy();

    protected String toString(int mode) {
        String inner = innerExpr.toString(mode);
        if (!(innerExpr instanceof Binder) 
          && innerExpr.getOperatorPrecedence() >= this.getOperatorPrecedence()) {
            inner = "[" + inner + "]";
            
//        } else if (hasPeriod || ExpressionParser.isIdentifierChar(inner.charAt(0))) {
//            if (dotPolicy()) {
//                inner = "." + inner;
//            } else {
//                inner = " " + inner;
//            }
//        }
        } else if (hasPeriod || ExpressionParser.isIdentifierChar(inner.charAt(0))) {
            if (!dotPolicy()) {
                inner = " " + inner;
            } 
        }
         
        // if this binder has a dot policy we prescriptively add a dot
        // no matter what the user did (or whoever entered the expression)
        if (dotPolicy()) {
            inner = "." + inner;
        }

        if (mode == TXT) {
            return getSymbol() + ident.toString(mode) + inner;
        } else if (mode == HTML) {
            return getSymbol() + escapeHTML(ident.toString(mode)) + inner;
        } else if (mode == LATEX) {
            return getLatexSymbol() + " " + ident.toString(mode) + inner;
        }
        // never reached
        throw new IllegalArgumentException();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        ident.writeToStream(output);
        innerExpr.writeToStream(output);
        output.writeBoolean(hasPeriod);
    }
    
    Binder(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        ident = (Identifier)Expr.readFromStream(input);
        innerExpr = Expr.readFromStream(input);
        hasPeriod = input.readBoolean();
    }
}
