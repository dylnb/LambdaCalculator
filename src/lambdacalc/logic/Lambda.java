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
 * Lambda.java
 *
 * Created on May 29, 2006, 3:34 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the lambda binder.
 */
public class Lambda extends Binder {
    /**
     * The unicode Greek letter lambda.
     */
    public static final char SYMBOL = '\u03BB';
    
    public static final char INPUT_SYMBOL = 'L';

    public static final String LATEX_REPR = "\\lambda";
    
    public static final int KEY_EVENT = KeyEvent.VK_L;
    
    private boolean starred;
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public Lambda(Identifier ident, Expr innerExpr, boolean hasPeriod, boolean starred) {
        super(ident,innerExpr,hasPeriod);
        this.starred = starred;
    }
    public Lambda(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident,innerExpr,hasPeriod);
        this.starred = false;
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 8;
    }
    
    public boolean dotPolicy() {
        return true;
    }
    
    public boolean isStarred() {
        return starred; 
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public String getLatexSymbol() {
        return this.LATEX_REPR;
    }

    public Type getType() throws TypeEvaluationException {
        checkVariable();
        return new CompositeType(getVariable().getType(), getInnerExpr().getType());
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new Lambda(variable, inner, hasPeriod());
    }
    
    Lambda(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
