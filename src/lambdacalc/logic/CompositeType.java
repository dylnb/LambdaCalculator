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
    public final static char LEFT_BRACKET = '<'; // '\u27E8'; // '\u2329'; '\u3008';
    public final static char RIGHT_BRACKET = '>'; // '\u232A';
    public final static char SEPARATOR = ',';
    
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

    public String toString() {
        return this.toStringHelper(
                String.valueOf(LEFT_BRACKET),
                String.valueOf(SEPARATOR),
                String.valueOf(RIGHT_BRACKET),
                false);
    }

    public String toLatexString() {
        return this.toStringHelper("\\langle ", ",", "\\rangle ", true);
    }

    // Examples: <e,t> <e,<et>>
    private String toStringHelper(String leftBracket, String separator, String rightBracket, boolean latex) {
        // Maribel wants the canonical form <e,t> to be used,
        // but in the Latex output we want to be able to give a shorter form
        // for embedded types like <et, t>.
        if (latex && left instanceof AtomicType && right instanceof AtomicType) {
            return String.valueOf(left)
            + String.valueOf(right);
        } else {
            String res = "";
            res += leftBracket;
            if (latex) {
                res += left.toLatexString();
            } else {
                res += left.toString();
            }
            res += (left instanceof ProductType ? " " : "");
            res += separator;
            res += (left instanceof ProductType ? " " : "");
            if (latex) {
                res += right.toLatexString();
            } else {
                res += right.toString();
            }
            res += rightBracket;
            return res;
        }
        
    }
    
    public String toShortString() {
        if (left instanceof AtomicType && right instanceof AtomicType) {
            return 
                    //String.valueOf(LEFT_BRACKET)
                    //+
                    String.valueOf(left)
            +String.valueOf(right)
            //+String.valueOf(RIGHT_BRACKET)
            ;
        } else {  
            return String.valueOf(LEFT_BRACKET)
            + left.toShortString()
            + (left instanceof ProductType ? " " : "")
            + String.valueOf(SEPARATOR)
            + (left instanceof ProductType ? " " : "")
            + right.toShortString()
            + String.valueOf(RIGHT_BRACKET);
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
