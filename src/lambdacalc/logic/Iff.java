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
 * Iff.java
 *
 * Created on May 29, 2006, 3:41 PM
 */

package lambdacalc.logic;

/**
 * Represents the biconditional binary connective.
 */
public class Iff extends LogicalBinary {
    /**
     * The unicode double arrow.
     */
    public static final char SYMBOL = '\u2194';

    public static final String LATEX_REPR = "\\leftrightarrow";
    
    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public Iff(Expr left, Expr right) {
        super(left,right);
    }

    public String getLatexRepr() {
        return LATEX_REPR;
    }

    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public Type getOperandType() {
        return Type.T;
    }

    protected Binary create(Expr left, Expr right) {
        return new Iff(left, right);
    }

    Iff(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
