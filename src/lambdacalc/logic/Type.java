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
 * Type.java
 *
 * Created on May 30, 2006, 10:37 AM
 */

package lambdacalc.logic;

import java.util.*;

/**
 * Represents a semantic type, an AtomicType like e and t,
 * a CompositeType like &lt;e,t&gt;, or a ProductType like (e x e).
 */
public abstract class Type implements Comparable {
    
    /**
     * Type variables identified by preceding apostrophe
     */
    public static final char VarTypeSignifier = '\'';
    
    /**
     * The type of entities, e.
     */
    public static final Type E = new ConstType('e');

     /**
     * The type of truth values, t.
     */
    public static final Type T = new ConstType('t');

    /**
     * The type of worlds used for Intensional Function Application, s.
     */
    public static final Type S = new ConstType('s');

    /**
     * The type of one place predicates, &lt;e,t&gt;.
     */
    public static final Type ET = new CompositeType(new ConstType('e'), new ConstType('t'));

    /**
     * The type of two place predicates, &lt;e x e , t&gt;.
     */
    public static final Type ExET = new CompositeType(
            new ProductType(new Type[] { new ConstType('e'), new ConstType('e') }),
            new ConstType('t'));
    
    /**
     * The type of an integer.
     */
    public static final Type N = new ConstType('n');
    
    public static final Type WILD = new VarType('_');
        
    /**
     * Tests two types for equality (as you would expect).
     */
    public boolean equals(Object obj) {
        if (obj instanceof Type)
            return equals((Type)obj);
        else
            return false;
    }
    
    /**
     * Implemented by subclasses to test for equality between types.
     */
    protected abstract boolean equals(Type t);
    
    /**
    * Implemented by subclasses to test whether two types can be unified. 
    * This method supports composition of polymorphic functions (combinators) by 
    * checking whether two types that possibly contain variables can be matched.
    * For example, 'a,t and e,t can be unified, with 'a mapping to e, while 
    * 'a,'a and e,t cannot be unified because 'a cannot be mapped to two concrete types.
    * Outputs a MatchPair, which contains the unifier of the two types if there is one.
    */
    public abstract MatchPair matches(Type t);
    
    /**
     * Compares two types.  The natural ordering of types is as follows:
     *   First, atomic types, ordered by their character names.
     *   Then product types, ordered by arity.
     *   Finally composite types, ordered first according to domain, then range.
     */
    public int compareTo(Object other) {
        if (this instanceof AtomicType) {
            if (other instanceof AtomicType) {
                char x = ((AtomicType)this).getSymbol();
                char y = ((AtomicType)other).getSymbol();
                if (x < y) return -1;
                if (x > y) return 1;
                return 0;
            }
            return -1;
        } else if (this instanceof ProductType) {
            if (other instanceof AtomicType)
                return 1;
            if (other instanceof ProductType) {
                int a = ((ProductType)this).getArity();
                int b = ((ProductType)other).getArity();
                if (a < b) return -1;
                if (a > b) return 1;
                return 0;
            }
            return -1;
            
        } else if (this instanceof CompositeType) {
            if (other instanceof AtomicType || other instanceof ProductType)
                return 1;
            Type al = ((CompositeType)this).getLeft();
            Type ar = ((CompositeType)this).getRight();
            Type bl = ((CompositeType)other).getLeft();
            Type br = ((CompositeType)other).getRight();
            
            int c = al.compareTo(bl);
            if (c != 0) return c;
            return ar.compareTo(br);
        }
        throw new RuntimeException(); // unreachable
    }
    
    /**
     * Will be overridden by subclasses
     */
    public boolean containsVar() {
        return false;
    };
    
    /**
     * A string representation that doesn't display more commas than needed.
     */
    public abstract String toShortString();

    public abstract String toLatexString();
    
    /**
     * Writes a serialization of the type to a DataOutputStream.
     * In implementations of this method in subclasses, the first thing written
     * must be the name of the class as a string (i.e. "AtomicType").
     */
    public abstract void writeToStream(java.io.DataOutputStream output) throws java.io.IOException;
   
    /**
     * Reads a serialization of the types from a DataInputStream.
     */
    public static Type readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        String exprType = input.readUTF();
        
        if (exprType.equals("ConstType")) return new ConstType(input);
        if (exprType.equals("VarType")) return new VarType(input);
        if (exprType.equals("CompositeType")) return new CompositeType(input);
        if (exprType.equals("ProductType")) return new ProductType(input);
        
        throw new java.io.IOException("Invalid data.");
    }
}
