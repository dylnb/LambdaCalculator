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
 * Forall.java
 *
 * Created on May 29, 2006, 3:35 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * The universal quantifier.
 */
public class ForAll extends PropositionalBinder {
    /**
     * The unicode universal quantifier symbol.
     */
    public static final char SYMBOL = '\u2200';
    
    public static final char INPUT_SYMBOL = 'A';

    public static final String LATEX_REPR = "\\forall";
    
    public static final int KEY_EVENT = KeyEvent.VK_A;
    
    /**
     * Constructs the binder.
     * @param ident the identifier the binder binds, which may
     * be a constant to capture student errors.
     * @param innerExpr the inner expression
     * @param hasPeriod indicates whether this binder's string
     * representation includes a period after the identifier.
     */
    public ForAll(Identifier ident, Expr innerExpr, boolean hasPeriod) {
        super(ident, innerExpr, hasPeriod);
    }    

    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public String getLatexSymbol() {
        return this.LATEX_REPR;
    }
    
    public boolean dotPolicy() {
        return true;
    }

    protected Binder create(Identifier variable, Expr inner) {
        return new ForAll(variable, inner, hasPeriod());
    }

    ForAll(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
