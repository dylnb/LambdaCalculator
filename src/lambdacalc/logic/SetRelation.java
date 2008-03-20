/*
 * SetRelation.java
 *
 * Created on May 29, 2006, 3:24 PM
 */

package lambdacalc.logic;

import java.awt.event.KeyEvent;

/**
 * Represents the subset and superset relations, and the negated and proper
 * versions of each.
 */
public abstract class SetRelation extends LogicalBinary {
    
    public SetRelation(Expr left, Expr right) {
        super(left, right);
    }
    
    public Type getOperandType() {
        return null; // doesn't matter since we override getType()
    }
    
    public Type getType() throws TypeEvaluationException {
        Type lefttype = getLeft().getType();
        Type righttype = getRight().getType();
        if ((!(lefttype instanceof CompositeType) || !((CompositeType)lefttype).getRight().equals(Type.T))
            || (!(righttype instanceof CompositeType) || !((CompositeType)righttype).getRight().equals(Type.T)))
            throw new TypeMismatchException("The types of the expressions on the left and right of a set relation connective like '" + getSymbol() + "' must be a set type, i.e. the type of the characteristic function of a set, such as " + Type.ET + ", but " + getLeft() + " is of type " + getLeft().getType() + " and " + getRight() + " is of type " + getRight().getType() + ".");
        return Type.T;
    }

    SetRelation(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }

    public static class Subset extends SetRelation {
        public static final char SYMBOL = '\u2286'; // subset or equal to symbol (plain subset is 2282)
        public static final String INPUT_SYMBOL = "<<";
        public static final int KEY_EVENT = KeyEvent.VK_COMMA; // shift+comma = <, at least on standard keyboards
        public Subset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new Subset(left, right); }
        Subset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class ProperSubset extends SetRelation {
        public static final char SYMBOL = '\u228A';
        public ProperSubset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new ProperSubset(left, right); }
        ProperSubset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class NotSubset extends SetRelation {
        public static final char SYMBOL = '\u2284';
        public NotSubset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new NotSubset(left, right); }
        NotSubset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }

    public static class Superset extends SetRelation {
        public static final char SYMBOL = '\u2287'; // superset or equal symbol (plain superset is 2283)
        public static final String INPUT_SYMBOL = ">>";
        public static final int KEY_EVENT = KeyEvent.VK_PERIOD; // shift+period = >, at least on standard keyboards
        public Superset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new Superset(left, right); }
        Superset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class ProperSuperset extends SetRelation {
        public static final char SYMBOL = '\u228B';
        public ProperSuperset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new ProperSuperset(left, right); }
        ProperSuperset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
    public static class NotSuperset extends SetRelation {
        public static final char SYMBOL = '\u2285';
        public NotSuperset(Expr left, Expr right) { super(left, right); }
        public String getSymbol() { return String.valueOf(SYMBOL); }
        protected Binary create(Expr left, Expr right) { return new NotSuperset(left, right); }
        NotSuperset(java.io.DataInputStream input) throws java.io.IOException { super(input); }
    }
}
