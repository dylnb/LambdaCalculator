package lambdacalc.lf;

import java.io.*;
import java.util.*;
import lambdacalc.logic.Expr;
import lambdacalc.logic.ExpressionParser;
import lambdacalc.logic.ExpressionParser.ParseOptions;

/**
 * Records the lexical entries of words.
 */
public class Lexicon {
    
    //TODO retrofit to extend HashMap or ArrayList  and thereby implement Collection interface
    //(see CompositionRuleList) --- the interfaces should be implemented directly, not by
    // extending those classes
    
    //IdentifierTyper typer; // Do we want this? TODO: Consistency check?
    // Implement Expr.getEffectiveIdentifierTyper
    // Implement are two IdentifierTypers consistent?
    //   and unify them

    Vector entries = new Vector(); // lexical entries for words
        
    public Vector getEntries() {
        // TODO: Return a read-only wrapper so entries can't be modified?
        return entries;
    }

    public void addLexicalEntry(String orthoForm, Expr meaning) {
        addLexicalEntry(new String[] { orthoForm }, meaning);
    }

    public void addLexicalEntry(String[] orthoForms, Expr meaning) {
        if (orthoForms.length == 0)
            throw new IllegalArgumentException("orthoForms must have length at least once");
        Entry entry = new Entry(orthoForms, meaning);
        entries.add(entry);
    }
    
    public Expr[] getMeanings(String orthoForm) {
        Vector exprs = new Vector();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = (Entry)entries.get(i);
            for (int j = 0; j < entry.orthoForms.length; j++) {
                if (orthoForm == null || orthoForm.equals(entry.orthoForms[j])) {
                    exprs.add(entry.meaning);
                    break;
                }
            }
        }
        return (Expr[])exprs.toArray(new Expr[0]);
    }
    
    public void removeEntry(String orthoForm, Expr meaning) {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = (Entry)entries.get(i);
            if (!entry.meaning.equals(meaning))
                continue;
            
            Vector newOrthoForms = new Vector();
            for (int j = 0; j < entry.orthoForms.length; j++)
                if (!entry.orthoForms[j].equals(orthoForm)) 
                    newOrthoForms.add(entry.orthoForms[j]);
            
            if (newOrthoForms.size() == 0) {
                // remove this lexical entry
                entries.removeElementAt(i);
                i--; // resume at this index again next iteration
            } else if (newOrthoForms.size() != entry.orthoForms.length) {
                // update the list of orthoforms for this entry
                entries.set(i, new Entry((String[])newOrthoForms.toArray(new String[0]), entry.meaning));
            }
        }
    }

    public class Entry {
        public final String[] orthoForms;
        public final Expr meaning;
        
        public Entry(String[] orthoForms, Expr meaning) {
            this.orthoForms = orthoForms;
            this.meaning = meaning;
        }
    }
    
    public void writeToStream(java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // versioning info for future use
        output.writeInt(entries.size());
        
        for (Iterator i = entries.iterator(); i.hasNext(); ) {
            Entry e = (Entry)i.next();
            output.writeInt(e.orthoForms.length);
            for (int j = 0; j < e.orthoForms.length; j++)
                output.writeUTF(e.orthoForms[j]);
            e.meaning.writeToStream(output);
        }
    }
    
    public void readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        if (input.readByte() != 0) throw new java.io.IOException("Data format error."); // sanity check
        
        int nEntries = input.readInt();
        
        for (int i = 0; i < nEntries; i++) {
            String[] orthoForms = new String[ input.readInt() ];
            for (int j = 0; j < orthoForms.length; j++)
                orthoForms[j] = input.readUTF();
            Expr meaning = Expr.readFromStream(input);
            
            entries.add(new Entry(orthoForms, meaning));
        }
    }
}
