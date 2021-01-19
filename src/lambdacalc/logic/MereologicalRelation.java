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
 * MereologicalRelation.java
 *
 * Created on May 29, 2006, 3:24 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the mereological part-of relation.
 */
public abstract class MereologicalRelation extends LogicalBinary {
        
    public MereologicalRelation(Expr left, Expr right) {
        super(left, right);
    }
    
    public Type getOperandType() {
        return null; // doesn't matter since we override getType()
    }
    
    protected abstract Type getResultingType() throws TypeEvaluationException;
    
    public Type getType() throws TypeEvaluationException {
        if (getLeft().getType().matches(Type.E) == null||
            getRight().getType().matches(Type.E) == null) {
            String msg = "The types of the expressions on the left and right " +
                         "of the multiplication operator must be type e, " +
                         "but " + getLeft() + " is of type " + getLeft().getType() +
                         " and " + getRight() + " is of type " + getRight().getType() + ".";
            throw new TypeMismatchException(msg);
        }
        return getResultingType();
    }

    MereologicalRelation(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

    public static class PartOf extends MereologicalRelation {
        public static final char SYMBOL = '\u2291'; // sqsubset or equal to symbol (plain subset is 2282)
        public static final String INPUT_SYMBOL = "<:";
        public static final String LATEX_REPR = "\\sqsubseteq";
        public static final int KEY_EVENT = KeyEvent.VK_SEMICOLON;
        public PartOf(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new PartOf(left, right); }
        PartOf(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
}
