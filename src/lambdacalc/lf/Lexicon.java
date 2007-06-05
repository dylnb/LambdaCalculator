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
    //(see CompositionRuleList)
    
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
                if (orthoForm.equals(entry.orthoForms[j])) {
                    exprs.add(entry.meaning);
                    break;
                }
            }
        }
        return (Expr[])exprs.toArray(new Expr[0]);
    }

    public class Entry {
        public final String[] orthoForms;
        public final Expr meaning;
        
        public Entry(String[] orthoForms, Expr meaning) {
            this.orthoForms = orthoForms;
            this.meaning = meaning;
        }
    }
}
