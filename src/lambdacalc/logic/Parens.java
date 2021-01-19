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
 * Parens.java
 *
 * Created on May 29, 2006, 3:52 PM
 */

package lambdacalc.logic;

import java.util.Map;

/**
 * Represents parenthesis, a unary operator.
 */
public class Parens extends Unary {
    
    /**
     * The constant for round parenthesis ( ).
     */
    public static final boolean ROUND = true;
    
    /**
     * The constant for square parenthesis [ ].
     */
    public static final boolean SQUARE = false;
    
    private boolean shape;
    
    /**
     * Constructs a parenthesis expression around then given
     * expression, with either ROUND or SQUARE parens.
     */
    public Parens(Expr innerExpr, boolean shape) {
        super(innerExpr);
        this.shape=shape;
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 0;
    }
    
    protected String toString(int mode) {
        return getOpenSymbol() + getInnerExpr().toString(mode) + getCloseSymbol();
    }
    
    String getOpenSymbol() { return shape == ROUND ? "(" : "["; }
    String getCloseSymbol() { return shape == ROUND ? ")" : "]"; }

    public Type getType() throws TypeEvaluationException {
        return getInnerExpr().getType();
    }
    
    public void setSquare() {
        this.shape = SQUARE;
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap, boolean matching) {
        
        // ignore parentheses for equality test
        // this line needs to be added to every equals method in every subclass of Expr
        e = e.stripOutermostParens();

        return this.stripOutermostParens().equals(e, useMaps, thisMap, otherMap, collapseAllVars, freeVarMap, matching);
    }
        
    protected Unary create(Expr inner) {
        return new Parens(inner, shape);
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        super.writeToStream(output);
        output.writeShort(0); // data format version
        output.writeBoolean(shape);
    }
    
    Parens(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        shape = input.readBoolean();
    }
}
