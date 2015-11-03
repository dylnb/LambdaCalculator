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
 * Fusion.java
 *
 * Created on March 20, 2008, 7:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package lambdacalc.logic;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents plural sum formation, as in the collective interpretation of
 * 'John and Mary'
 */

public class Fusion extends Binary {
    public static final char SYMBOL = '\u2295'; // circled plus
    public static final char INPUT_SYMBOL = '+'; // plus
    public static final String LATEX_SYMBOL = "\\oplus"; // central dot
    
    public Fusion(Expr left, Expr right) {
        super(left, right);
    }
    
    protected String toString(int mode) {
        if (mode == LATEX) {
            return getLeft().toString(mode) + LATEX_SYMBOL + getRight().toString(mode);
        } else { // mode == HTML || mode == TXT
            return getLeft().toString(mode) + SYMBOL + getRight().toString(mode);
        }
    }

    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public final int getOperatorPrecedence() {
        return 5;
    }
    
    protected Binary create(Expr left, Expr right) {
        return new Fusion(left, right);
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!getLeft().getType().equals(Type.E) ||
            !getRight().getType().equals(Type.E)) {
            String msg = "The types of the expressions on the left and right " +
                         "of the mulitiplication operator must be type <e>, " +
                         "but " + getLeft() + " is of type " + getLeft().getType() +
                         " and " + getRight() + " is of type " + getRight().getType() + ".";
            throw new TypeMismatchException(msg);
        }
        return Type.E;
    }
    
    
    protected boolean equals(Expr e, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {

        // ignore parentheses for equality test
        e = e.stripOutermostParens();

        if (e instanceof Fusion) {
            return this.equals((Fusion) e, useMaps, thisMap, otherMap, collapseAllVars, freeVarMap);
        } else {
            return false;
        }
    }
    
    private boolean equals(Fusion b, boolean useMaps, Map thisMap, Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {
        // If the immediate daughters are equal, then this node is equal
        if (this.getClass() != b.getClass()) {
            return false;
        } else if (this.getLeft().equals(b.getLeft(), useMaps, thisMap, otherMap, collapseAllVars, freeVarMap)
                   && this.getRight().equals(b.getRight(), useMaps, thisMap, otherMap, collapseAllVars, freeVarMap)
                  ) {
            return true;
        } else {
            // If they're not equal, it might be due to some difference in bracketing,
            // which doesn't ultimately matter, since this operation is associative
            // So we flatten the expression as much as we can (until we hit a
            // descendent with a different class), and then compare the flat arrays of juncts
            Expr[] junctsA = equalsRec(this);
            Expr[] junctsB = equalsRec(b);
            if (junctsA.length != junctsB.length) {
                return false;
            } else {
                for (int i = 0; i < junctsA.length; i++) {
                    if (!junctsA[i].equals(junctsB[i], useMaps, thisMap, otherMap, collapseAllVars, freeVarMap)) {
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
                     Arrays.stream(equalsRec(((Fusion)b).getLeft())),
                     Arrays.stream(equalsRec(((Fusion)b).getRight()))
                   ).toArray(Expr[]::new);
        }
    }     
    
    Fusion(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

}

