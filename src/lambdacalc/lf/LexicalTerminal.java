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

package lambdacalc.lf;

import java.util.HashSet;
import java.util.Iterator;
import lambdacalc.logic.Expr;
import lambdacalc.logic.Var;

public class LexicalTerminal extends Terminal {

    protected Expr meaning;
    
    public void setMeaning(Expr meaning) {
        Expr oldMeaning = this.meaning;
        this.meaning = meaning;
        changes.firePropertyChange("meaning", oldMeaning, this.meaning);
    }
    
    public boolean hasMeaning() {
        return meaning != null;
    }

    public boolean isMeaningful() {
        return true;
    }
    
    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        if (meaning == null)
            throw new TerminalLacksMeaningException(this);
        
        if (g == null)
            return meaning;
        
        // If an assignment function is in use, then we must make sure that
        // no variables in our denotation are in the range of the assignment
        // function, or else 1) we may accidentally bind a g(n) and 2) we
        // may have a variable accidentally bound from above.
        
        // Collect a list of all variables to avoid.
        HashSet varsInUse = new HashSet(meaning.getAllVars());
        for (Iterator i = g.values().iterator(); i.hasNext(); )
            varsInUse.add((Var)i.next());
        
        // For each variable in the range of g, do any needed substitutions:
        Expr m = meaning;
        
        for (Iterator i = g.values().iterator(); i.hasNext(); ) {
            Var v = (Var)i.next();
            if (m.getAllVars().contains(v)) {
                Var v2 = Expr.createFreshVar(v, varsInUse);
                m = m.replace(v, v2);
                varsInUse.add(v2);
            }
        }
        
        return m;
    }
    
    public String getDisplayName() {
        return "Lexical terminal";
    }

    public String toLatexString() {
        if (hasMeaning()) {
            String type = "Type unknown";
            try {
                type = "$" + this.meaning.getType().toLatexString() + "$";
            } catch (lambdacalc.logic.TypeEvaluationException t) {
                type = "\\emph{Type unknown}";  
            }
            return "{" + this.getLabel() + " \\\\ " + type + " \\\\ $" + this.meaning.toLatexString() + "$}";
        } else {
            return "{" + this.getLabel() + "}";
        }
        //TODO include indices
    }
    
    public String toLatexString(int indent) {
        if (hasMeaning()) {
            String type = "Type unknown";
            try {
                type = "$" + this.meaning.getType().toLatexString() + "$";
            } catch (lambdacalc.logic.TypeEvaluationException t) {
                type = "\\emph{Type unknown}";  
            }
            return "{" + this.getLabel() + " \\\\ "
                    + type + " \\\\\n"
                    + (new String(new char[indent + this.getLabel().length() + 5]).replace("\0", " "))
                    + "$" + this.meaning.toLatexString() + "$}";
        } else {
            return "{" + this.getLabel() + "}";
        }
        //TODO include indices
    }
    
    /**
     * If the meaning of this terminal hasn't been set yet, 
     * and if the terminal is unambiguous in the lexicon,
     * assigns the meaning it finds in the lexicon to this
     * terminal.
     *
     * @param lexicon the lexicon
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessLexicalEntries(Lexicon lexicon) {
        if (meaning != null)
            return;
        
        Expr[] meanings = lexicon.getMeanings(this.getLabel());
        if (meanings.length == 1)
            meaning = meanings[0];    
    }
    
 
}