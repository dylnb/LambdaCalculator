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
 * Identifier.java
 *
 * Created on May 29, 2006, 3:14 PM
 */

package lambdacalc.logic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The abstract base class of constants (Const) and variables (Var).
 */
public abstract class Identifier extends Expr {
    
    /**
     * The prime symbol. Changed to apostrophe on 8/12/2019.
     */
    public static final char PRIME = '\''; // previously '\u02B9' (0x2032 is another one)
    
    public static final char PRIME_INPUT_SYMBOL = '\'';

    public static final String LATEX_PRIME_REPR = "'"; // previously "^{\\prime}"
    
    private String symbol;
    private Type type;
    private boolean typeIsExplicit;
    private boolean starred;
    
    public Identifier(String symbol, Type type, boolean isTypeExplicit) {
        this(symbol, type, isTypeExplicit, false);
    }
    /** Creates a new instance of Identifier */
    public Identifier(String symbol, Type type, boolean isTypeExplicit, boolean starred) {
        this.symbol = symbol; 
        this.type = type;
        this.typeIsExplicit = isTypeExplicit;
        this.starred = starred;
        if (symbol == null) throw new IllegalArgumentException();
        if (type == null) throw new IllegalArgumentException();
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 1;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap, boolean matching) {
 
        // ignore parentheses for equality test
        e = e.stripOutermostParens();

        if (e instanceof Identifier) {
            if (collapseAllVars) return true;
            return this.equals((Identifier) e, useMaps, thisMap, otherMap, freeVarMap, matching);
        } else {
            return false;           
        }
    }
    
    public int hashCode() {
        return this.symbol.hashCode()^super.hashCode();
    }
    
    protected abstract boolean equals(Identifier i, boolean useMaps, Map thisMap, Map otherMap, Map freeVarMap, boolean matching);
    
    protected Expr performLambdaConversion1(Set accidentalBinders) throws TypeEvaluationException {
        // We're looking for a lambda. None here.
        return null;
    }
    
    protected String toString(int mode) {
        if (!isTypeExplicit()) {
            if (mode == HTML) {
//                return escapeHTML((this.starred ? "*" : "") + this.symbol);
                return escapeHTML(this.symbol);
            } else if (mode == TXT ) {
//                return (this.starred ? "*" : "") + this.symbol;
                return this.symbol;
            } else  { // mode == LATEX
//                String res = (this.starred ? "*" : "") + this.symbol
                String res = this.symbol
                        .replace(String.valueOf(PRIME), LATEX_PRIME_REPR)
                        .replace(String.valueOf(PRIME_INPUT_SYMBOL), LATEX_PRIME_REPR)
                        .replace("\\prime}^{","\\prime ");// hack to merge multiple primes
                String resTruncated = res.replace(LATEX_PRIME_REPR, "");
                if (resTruncated.length() > 1) { // after stripping away primes, we still have multiple letters
                    res = "\\mbox{" + res + "}";
                }
                return res;
            }
        } else if (mode == HTML) {
//            return escapeHTML((this.starred ? "*" : "") + symbol) + "<sub>" + escapeHTML(type.toString()) + "</sub>";
            return escapeHTML(this.symbol) + "<sub>" + escapeHTML(type.toString()) + "</sub>";
        } else if (mode == LATEX) {
//            return (this.starred ? "{}^*" : "") + this.symbol + "_{" + type.toLatexString() + "}";
            return this.symbol + "_{" + type.toLatexString() + "}";

        } else { // mode == TXT
//            return (this.starred ? "*" : "") + this.symbol + "_" + type.toShortString();
            return this.symbol + "_" + type.toShortString();
        }
    }
    
    /**
     * Gets the string name of the identifier.
     */
    public String getSymbol() {
        return symbol;
    }
    
    public Type getType() {
        return type;
    }
    
    @Override
    public void setType(Type t) {
        type = t;
    }
    
    public boolean isTypeExplicit() {
        return typeIsExplicit;
    }
    
    public boolean isStarred() {
        return starred;
    }
    
    /**
     * Overriden in derived classes to create a new instance of this
     * type of identifier, that is, to clone it.
     *
     * @return a copy of this
     */
    protected abstract Identifier create();
    
    /**
     * Returns an (empty) List representing the fact that identifiers
     * have no subexpressions.
     * @return an empty list
     */
    public List getSubExpressions() {
        return new Vector(0);
    }
    
    /**
     * Returns a copy of this Identifier.
     *
     * @param subExpressions the list of subexpressions (must be null or empty)
     * @throws IllegalArgumentException if the list is nonnull and nonempty
     * @return a copy of this
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        if (subExpressions != null && subExpressions.size() != 0) 
            throw new IllegalArgumentException("List is nonempty");
        return create();
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(1); // data format version
        output.writeUTF(symbol);
        type.writeToStream(output);
        output.writeBoolean(typeIsExplicit);
        output.writeBoolean(starred);
    }
    
    Identifier(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 1) throw new java.io.IOException("Invalid data."); // future version?
        symbol = input.readUTF();
        type = Type.readFromStream(input);
        typeIsExplicit = input.readBoolean();
        starred = input.readBoolean();
    }
}
