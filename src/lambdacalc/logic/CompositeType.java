/*
 * CompositeType.java
 *
 * Created on May 30, 2006, 10:42 AM
 */

package lambdacalc.logic;

/**
 * Represents a composite (function) type, like &lt;et&gt;.
 */
public class CompositeType extends Type {
    
    private Type left;
    private Type right;
    
    /**
     * Creates a new instance of CompositeType
     * @param left the type of the domain of the function
     * @param right the type of the range of the function
     */
    public CompositeType(Type left, Type right) {
        this.left=left;
        this.right=right;
    }
    
    /**
     * Gets the type of the domain of the function.
     */
    public Type getLeft() {
        return this.left;
    }
    
    /**
     * Gets the type of the range of the function.
     */
    public Type getRight() {
        return this.right;
    }
    
    protected boolean equals(Type t) {
        if (t instanceof CompositeType) {
//            CompositeType ct = (CompositeType) t;
            return (this.getLeft().equals(((CompositeType) t).getLeft())
                    && (this.getRight().equals(((CompositeType) t).getRight())));
        } else { 
            return false;
        }
    }
    
    public int hashCode() {
        return left.hashCode() ^ right.hashCode(); // XOR of hash codes
    }
    
    // Examples: <e,t> <e,<et>>
    public String toString() {
        // Maribel wants the canonical form <e,t> to be used,
        // but maybe we want to be able to give a shorter form
        // for embedded types like <et, t>.
        /*if (left instanceof AtomicType && right instanceof AtomicType) {
            return String.valueOf(Type.LEFTBRACKET)
            +String.valueOf(left)
            +String.valueOf(right)
            +String.valueOf(Type.RIGHTBRACKET);
        } else*/ {
            return String.valueOf(Type.LEFTBRACKET)
            + left.toString()
            + (left instanceof ProductType ? " " : "")
            + String.valueOf(Type.SEPARATOR)
            + (left instanceof ProductType ? " " : "")
            + right.toString()
            + String.valueOf(Type.RIGHTBRACKET);
        }
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("CompositeType");
        output.writeShort(0); // data format version
        left.writeToStream(output);
        right.writeToStream(output);
    }
    
    CompositeType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        left = Type.readFromStream(input);
        right = Type.readFromStream(input);
    }
    
}
