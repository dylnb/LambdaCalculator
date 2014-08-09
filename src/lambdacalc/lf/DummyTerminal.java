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
 * DummyTerminal.java
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;

/**
 * This class represents a terminal node that is on the tree but is not really
 * meant to be a part of the semantic representation. It is usually parenthesized
 * terminals, at least in Maribel's way of doing things.
 * @author tauberer
 */
public class DummyTerminal extends Terminal {
    
    protected Expr meaning = null;
    
    /** Creates a new instance of DummyTerminal */
    public DummyTerminal(String label) {
        setLabel(label);
    }
    
    public String getDisplayName() {
        return "Dummy terminal";
    }
    
    public boolean isMeaningful() {
        return false;
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        throw new MeaningEvaluationException("\"" + toShortString() +"\" does not have a denotation.");
    }
    
    public void guessLexicalEntries(Lexicon lexicon) {
    }

    public String toLatexString() {
        return "\\mbox{"+this.getLabel()+"}";
    }
}
