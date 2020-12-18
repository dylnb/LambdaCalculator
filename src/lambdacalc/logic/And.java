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
 * And.java
 *
 * Created on May 29, 2006, 3:22 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents the conjunction binary connective.
 */
public class And extends LogicalBinary {
    
    /**
     * The wedge character
     */
    public static final char SYMBOL = '\u2227';
    
    public static final char INPUT_SYMBOL = '&';

    public static final String LATEX_REPR = "\\land";
    
    public static final char ALTERNATE_INPUT_SYMBOL = '^';
    
    public static final int KEY_EVENT = KeyEvent.VK_AMPERSAND;

    /**
     * Constructs the connective.
     * @param left the expression on the left side of the connective
     * @param right the expression on the right side of the connective
     */
    public And(Expr left, Expr right) {
       super(left,right);
    }
    
    public String getSymbol() {
        return String.valueOf(SYMBOL);
    }

    public String getLatexRepr() {
        return LATEX_REPR;
    }

    public Type getOperandType() {
        return Type.T;
    }
    
    protected Binary create(Expr left, Expr right) {
        return new And(left, right);
    }
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {

        // ignore parentheses for equality test
        e = e.stripOutermostParens();

        if (e instanceof And) {
            return this.equals((And) e, useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
        } else {
            return false;
        }
    }
    
    private boolean equals(And b, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap, boolean matching) {
        if (this.getClass() != b.getClass()) {
            return false;
        } else if (this.getLeft().equals(b.getLeft(), useMaps, thisMap, otherMap, collapseAllVars, freeVarMap, matching)
                   && this.getRight().equals(b.getRight(), useMaps, thisMap, otherMap, collapseAllVars, freeVarMap, matching)
                  ) {
            return true;
        } else {
            Expr[] junctsA = equalsRec(this);
            Expr[] junctsB = equalsRec(b);
            if (junctsA.length != junctsB.length) {
                return false;
            } else {
            // If they're not equal, it might be due to some difference in bracketing,
            // which doesn't ultimately matter, since this operation is associative
            // So we flatten the expression as much as we can (until we hit a
            // descendent with a different class), and then compare the flat arrays of juncts
                for (int i = 0; i < junctsA.length; i++) {
                    if (!junctsA[i].equals(junctsB[i], useMaps, thisMap, otherMap, collapseAllVars, freeVarMap, matching)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }
    
    protected Expr[] equalsRec(Expr b) {
        if (b.getClass() != this.getClass()) {
            Expr[] junct = {b};
            return junct;
        } else {
            return Stream.concat(
                     Arrays.stream(equalsRec(((And)b).getLeft())),
                     Arrays.stream(equalsRec(((And)b).getRight()))
                   ).toArray(Expr[]::new);
        }
    }

    
    And(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
    
}
