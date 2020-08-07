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
 * SetRelation.java
 *
 * Created on May 29, 2006, 3:24 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the subset and superset relations, the negated and proper
 * versions of each, and intersection and union.
 */
public abstract class SetRelation extends LogicalBinary {
    
    public SetRelation(Expr left, Expr right) {
        super(left, right);
    }
    
    public Type getOperandType() {
        return null; // doesn't matter since we override getType()
    }
    
    protected abstract Type getResultingType() throws TypeEvaluationException;
    
    public Type getType() throws TypeEvaluationException {
        Type lefttype = getLeft().getType();
        Type righttype = getRight().getType();
        if ((!(lefttype instanceof CompositeType) || !((CompositeType)lefttype).getRight().equals(Type.T))
            || (!(righttype instanceof CompositeType) || !((CompositeType)righttype).getRight().equals(Type.T)))
            throw new TypeMismatchException("The types of the expressions on the left and right of a set connective like '" + getSymbol() + "' must be a set type, i.e. the type of the characteristic function of a set, such as " + Type.ET + ", but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        if (((CompositeType)lefttype).getLeft().matches(((CompositeType)righttype).getLeft()) == null)
            throw new TypeMismatchException("The sets on the left and right of a set connective like '" + getSymbol() + "' must be sets over the same kind of element, but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        return getResultingType();
    }

    SetRelation(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

    public static class Subset extends SetRelation {
        public static final char SYMBOL = '\u2286'; // subset or equal to symbol (plain subset is 2282)
        public static final String INPUT_SYMBOL = "<<";
        public static final String LATEX_REPR = "\\subseteq";
        public static final int KEY_EVENT = KeyEvent.VK_COMMA; // shift+comma = <, at least on standard keyboards
        public Subset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new Subset(left, right); }
        Subset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class ProperSubset extends SetRelation {
        public static final char SYMBOL = '\u228A';
        public static final String LATEX_REPR = "\\subset";
        public ProperSubset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new ProperSubset(left, right); }
        ProperSubset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class NotSubset extends SetRelation {
        public static final char SYMBOL = '\u2284';
        public static final String LATEX_REPR = "\\nsubseteq";
        public NotSubset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new NotSubset(left, right); }
        NotSubset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }

    public static class Superset extends SetRelation {
        public static final char SYMBOL = '\u2287'; // superset or equal symbol (plain superset is 2283)
        public static final String INPUT_SYMBOL = ">>";
        public static final String LATEX_REPR = "\\supseteq";
        public static final int KEY_EVENT = KeyEvent.VK_PERIOD; // shift+period = >, at least on standard keyboards
        public Superset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new Superset(left, right); }
        Superset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class ProperSuperset extends SetRelation {
        public static final char SYMBOL = '\u228B';
        public static final String LATEX_REPR = "\\supset";
        public ProperSuperset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new ProperSuperset(left, right); }
        ProperSuperset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class NotSuperset extends SetRelation {
        public static final char SYMBOL = '\u2285';
        public static final String LATEX_REPR = "\\nsupset";
        public NotSuperset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return Type.T; }
        protected Binary create(Expr left, Expr right) { return new NotSuperset(left, right); }
        NotSuperset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }

    public static class Intersect extends SetRelation {
        public static final char SYMBOL = '\u2229';
        public static final String INPUT_SYMBOL = "@I";
        public static final String LATEX_REPR = "\\cap";
        public Intersect(Expr left, Expr right) { super(left, right); }
        public final int getOperatorPrecedence() { return 5; }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return getLeft().getType(); }
        protected Binary create(Expr left, Expr right) { return new Intersect(left, right); }
        Intersect(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class Union extends SetRelation {
        public static final char SYMBOL = '\u222A';
        public static final String INPUT_SYMBOL = "@U";
        public static final String LATEX_REPR = "\\cup";
        public Union(Expr left, Expr right) { super(left, right); }
        public final int getOperatorPrecedence() { return 5; }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        public String getLatexRepr() { return LATEX_REPR; }
        protected Type getResultingType() throws TypeEvaluationException { return getLeft().getType(); }
        protected Binary create(Expr left, Expr right) { return new Union(left, right); }
        Union(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
}
