/*
 * IdentifierTyper.java
 *
 * Created on May 29, 2006, 3:45 PM
 */

package lambdacalc.logic;

import java.util.*;
import lambdacalc.exercises.Exercise;
import lambdacalc.exercises.ExerciseFile;
import lambdacalc.exercises.ExerciseFileFormatException;
import lambdacalc.exercises.ExerciseFileVersionException;

/**
 * Says whether an Identifier is a variable or a constant
 * and provides the semantic type of the Identifier.
 */
public class IdentifierTyper {
    private class Entry {
        public char start, end;
        public boolean var;
        public Type type;
        
        public Entry(char s, char e, boolean v, Type t) {
            start = s;
            end = e;
            var = v;
            type = t;
        }
    }
    
    ArrayList entries = new ArrayList();
    
    /**
     * Creates a new IdentifierTyper with no type mappings.
     */
    public IdentifierTyper() {
    }
    
    /**
     * Creates an IdentifierTyper with the usual defaults.
     * The defaults are as follows:
     *    a-e : consants of type e
     *    P-Q : constants of type et (one place predicates)
     *    R-S : constants of type <e*e,t> (two place predicates: from a vector of two e's to a t)
     *    u-z : variables of type e
     *    U-Z : variables of type et
     */
    public static IdentifierTyper createDefault() {
        IdentifierTyper typer = new IdentifierTyper();
        typer.addEntry('a', 'e', false, Type.E);
        typer.addEntry('P', 'Q', false, Type.ET);
        typer.addEntry('R', 'S', false, Type.ExET);
        typer.addEntry('u', 'z', true, Type.E);
        typer.addEntry('U', 'Z', true, Type.ET);
        return typer;
    }
    
    /**
     * Clears the mappings.
     */
    public void clear() {
        entries.clear();
    }
    
    /**
     * Sets the type of identifiers starting with the given character,
     * overriding previous settings.
     */
    public void addEntry(char c, boolean isVariable, Type type) {
        addEntry(c, c, isVariable, type);
    }
    
    /**
     * Sets the type of identifiers starting a character in the given range,
     * overriding previous settings.
     */
    public void addEntry(char start, char end, boolean isVariable, Type type) {
        if (!Character.isLetter(start) || !Character.isLetter(end))
            throw new IllegalArgumentException("The range must be over letters.");
        if (Character.isLowerCase(start) != Character.isLowerCase(end))
            throw new IllegalArgumentException("The start and end of the letter range must be both uppercase or both lowercase.");
            
        entries.add(new Entry(start, end, isVariable, type));
    }
    
    private Entry findEntry(String identifier) throws IdentifierTypeUnknownException {
        char c = identifier.charAt(0);
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry e = (Entry)entries.get(i);
            if (c >= e.start && c <= e.end) return e;
        }
        throw new IdentifierTypeUnknownException(identifier);
    }
    
    /**
     * Gets whether the identifier is a variable.
     * @throws IdentifierTypeUnknownException if the identifier cannot be
     * typed because it starts with a character not mapped
     */
    public boolean isVariable(String identifier) throws IdentifierTypeUnknownException {
        return findEntry(identifier).var;
    }
    
    /**
     * Gets the semantic type of the identifier.
     * @throws IdentifierTypeUnknownException if the identifier cannot be
     * typed because it starts with a character not mapped
     */
    public Type getType(String identifier) throws IdentifierTypeUnknownException {
        return findEntry(identifier).type;
    }
    
    /**
     * Clones this instance.
     */
    public IdentifierTyper cloneTyper() {
        IdentifierTyper ret = new IdentifierTyper();
        for (int j = 0; j < entries.size(); j++) {
            Entry e = (Entry)entries.get(j);
            ret.addEntry(e.start, e.end, e.var, e.type);
        }
        return ret;
    }
    
    public String toString() {
        String ret = "";
        TypeMapping[] m = getMapping();
        for (int i = 0; i < m.length; i++) {
            if (ret != "") ret += "\n";
            for (int k = 0; k < m[i].ranges.length; k++) {
                if (k > 0) ret += " ";
                if (m[i].ranges[k].start == m[i].ranges[k].end)
                    ret += m[i].ranges[k].start;
                else
                    ret += m[i].ranges[k].start + "-" + m[i].ranges[k].end;
            }
            ret += " : ";
            if (m[i].var)
                ret += "variables";
            else
                ret += "constants";
            ret += " of type ";
            if (m[i].type.equals(Type.ExET))
                ret += "two place predicate";
            else
                ret += m[i].type.toString();

            if (m[i].type.equals(Type.ET))
                ret += " (one place predicate)";
        }
        return ret;
    }
    
    public TypeMapping[] getMapping() {
        // Get a unique list of the types involved
        Set types = new HashSet();
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.get(i);
            types.add(new TypeMapping(e.type, e.var));
        }
        
        // Create the type mapping array and put the types into a natural order
        TypeMapping[] ret = (TypeMapping[])types.toArray(new TypeMapping[0]);
        Arrays.sort(ret);
        
        // Build the return value; loop through distinct types
        for (int i = 0; i < ret.length; i++) {
            // Mark off which letters are covered by this type, in the ASCII range
            boolean[] letters = new boolean[256];
            
            for (int j = 0; j < entries.size(); j++) {
                Entry e = (Entry)entries.get(j);
                if (!e.type.equals(ret[i].type) || e.var != ret[i].var) continue;
                
                for (int k = e.start; k <= e.end; k++)
                    if (k >= 0 && k < letters.length)
                        letters[k] = true;
            }
            
            // Collapse consecutive letters into ranges
            ArrayList ranges = new ArrayList();
            CharRange lastRange = null;
            for (int k = 0; k < letters.length; k++) {
                if (!letters[k]) continue;
                
                // If this is the first letter we've seen, or if this letter
                // does not extend the previous range we added, put a new
                // singleton range into the list.
                if (lastRange == null || k != lastRange.end + 1) {
                    lastRange = new CharRange((char)k, (char)k);
                    ranges.add(lastRange);
                    
                // Otherwise, this letter extends the previous range, so
                // just extend it.  (Since lastRange is an object, we're modifying
                // the object we last put into the list.
                } else {
                    lastRange.end = (char)k;
                }
            }
            
            ret[i].ranges = (CharRange[])ranges.toArray(new CharRange[0]);
            Arrays.sort(ret[i].ranges); // is already be sorted, actually
        }

        return ret;
    }
            
    public class TypeMapping implements Comparable {
        public boolean var;
        public Type type;
        public CharRange[] ranges;
        
        TypeMapping(Type t, boolean v) {
            type = t; var = v;
        }
        
        public int hashCode() { return type.hashCode(); }
        
        public boolean equals(Object other) {
            return compareTo(other) == 0;
        }

        public int compareTo(Object other) {
            TypeMapping m = (TypeMapping)other;
            int c = type.compareTo(m.type);
            if (c != 0) return c;
            if (var != m.var) return (var ? 1 : -1);
            return 0;
        }
    }
    
    public class CharRange implements Comparable {
        public char start, end;
        CharRange(char start, char end) {
            this.start = start; this.end = end;
        }
        public int compareTo(Object other) {
            CharRange r = (CharRange)other;
            if (start < r.start) return -1;
            if (start > r.start) return 1;
            if (end < r.end) return -1;
            if (end > r.end) return 1;
            return 0;
        }
    }

    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeShort(0); // for future use
        output.writeShort(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            Entry e = (Entry)entries.get(i);
            output.writeChar(e.start);
            output.writeChar(e.end);
            output.writeBoolean(e.var);
            output.writeUTF(e.type.toString());
        }
    }
    public void readFromStream(java.io.DataInputStream input, int fileFormatVersion) throws java.io.IOException, ExerciseFileFormatException {
        
        if (input.readShort() != 0) throw new ExerciseFileVersionException();
        
        int nEntries = input.readShort();
        for (int i = 0; i < nEntries; i++) {
            char start = input.readChar();
            char end = input.readChar();
            boolean var = input.readBoolean();
            Type type;
            
            try {
                type = TypeParser.parse(input.readUTF());
            } catch (Exception e) {
                System.err.println(e);
                throw new ExerciseFileFormatException();
            }
            
            Entry e = new Entry(start, end, var, type);
            entries.add(e);
        }
    }
}
