package lambdacalc.lf;

import java.io.*;
import java.util.*;
import lambdacalc.logic.Expr;
import lambdacalc.logic.ExpressionParser;
import lambdacalc.logic.ExpressionParser.ParseOptions;

public class Lexicon {

    Vector entries = new Vector();

    public void addLexicalEntry(String[] orthoForms, Expr meaning) {
        Entry entry = new Entry();
        entry.orthoForms = orthoForms;
        entry.meaning = meaning;
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

    class Entry {
        public String[] orthoForms;
        public Expr meaning;
    }
    
    public void readFrom(Reader reader) throws IOException, SyntaxException {
        ExpressionParser.ParseOptions popts = new ExpressionParser.ParseOptions();
        // TODO: Set options...
    
        BufferedReader br = new BufferedReader(reader);
        int linectr = 0;
        String line;
        while ((line = br.readLine()) != null) {
            linectr++;
            if (line.trim().equals(""))
                continue;
            if (line.charAt(0) == '#')
                continue;
            int colon = line.indexOf(':');
            if (colon == -1)
                throw new SyntaxException("Every line in a lexicon file must contain a colon.", linectr);
            
            String orthos = line.substring(0, colon).trim();
            String exprform = line.substring(colon+1).trim();
            
            try {
                Expr expr = ExpressionParser.parse(exprform, popts);
            } catch (lambdacalc.logic.SyntaxException ex) {
                throw new SyntaxException(ex.getMessage(), linectr);
            }
        }
    }
    
}