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
 * ProductType.java
 *
 * Created on May 30, 2006, 5:52 PM
 */

package lambdacalc.logic;

/**
 * Represents a cartesian product type.  This is the type
 * of vectors, i.e. the list of arguments to a predicate of
 * two or more arguments.  Product types are the types
 * of ArgLists.
 */
public class ProductType extends Type {
    /**
     * The unicode cross character.
     */
    public static final char SYMBOL = '\u00D7';

    public static final String LATEX_SYMBOL = "\\times";
    
    private Type[] subtypes;
    
    /**
     * Creates a new product type with the given sub-types.
     */
    public ProductType(Type[] subtypes) {
        this.subtypes = subtypes;
        if (subtypes.length <= 1) throw new IllegalArgumentException();
    }
    
    /**
     * Gets the sub-types of this product type.
     */
    public Type[] getSubTypes() {
        return subtypes;
    }
    
    /**
     * Gets the number of sub-types in this type.
     */
    public int getArity() {
        return subtypes.length;
    }
    
    protected boolean equals(Type t) {
        if (t instanceof VarType) {
            return true;
        } else if (t instanceof ProductType) {
            Type[] a1 = getSubTypes();
            Type[] a2 = ((ProductType)t).getSubTypes();
            if (a1.length != a2.length) return false;
            for (int i = 0; i < a1.length; i++) {
                Type l = a1[i];
                Type r = a2[i];
                boolean guard = a1[i].equals(a2[i]);
                if (!guard)
                    return false;
            }
            return true;
        } else { 
            return false;
        }
    }
    
    public boolean containsVar() {
        for (Type subtype : subtypes) {
            if (subtype.containsVar()) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        int hc = 0;
        for (int i = 0; i < subtypes.length; i++)
            hc ^= subtypes[i].hashCode(); // XOR the hash codes
        return hc;
    }
    
    public String toShortString() {
        return toString();
    }

    public String toString() {
        return this.toStringHelper(String.valueOf(this.SYMBOL), false);
    }

    public String toLatexString() {
        return this.toStringHelper(this.LATEX_SYMBOL, true);
    }
    
    public String toStringHelper(String separator, boolean latex) {
        String ret = "";
        for (int i = 0; i < getSubTypes().length; i++) {
            if (i > 0)
                ret += " " + separator + " ";
            if (latex) {
                String subString = getSubTypes()[i].toLatexString();
                if (subString.length() > 1 && !subString.contains("<")) {
                    subString = "(" + subString + ")";
                }
                ret += subString;
            } else {
                String subString = getSubTypes()[i].toString();
                if (subString.length() > 1 && !subString.contains("<")) {
                    subString = "(" + subString + ")";
                }
                ret += subString;
            }
        }
        return ret;
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeUTF("ProductType");
        output.writeShort(0); // data format version
        output.writeInt(subtypes.length);
        for (int i = 0; i < subtypes.length; i++)
            subtypes[i].writeToStream(output);
    }
    
    ProductType(java.io.DataInputStream input) throws java.io.IOException {
        // the class string has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        int ntypes = input.readInt();
        if (ntypes <= 1 || ntypes > 25) // sanity checks
            throw new java.io.IOException("Invalid data.");
        subtypes = new Type[ntypes];
        for (int i = 0; i < ntypes; i++)
            subtypes[i] = Type.readFromStream(input);
    }
}
