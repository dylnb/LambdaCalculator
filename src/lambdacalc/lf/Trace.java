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
 * Trace.java
 *
 * Created on June 5, 2007, 5:42 PM
 *
 */

package lambdacalc.lf;


import lambdacalc.logic.Expr;
import lambdacalc.logic.GApp;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;

/**
 * A trace or a pronoun.
 *
 * @author champoll
 */
public class Trace extends Terminal {
    
    public static final String SYMBOL = "t";
    
    protected Expr meaning = null;

    public Trace(String label, int index, Type type) {
        super(label, index, type);
    }

    public Trace(int index, Type type) {
        this(SYMBOL, index, type);
    }
    
    public Trace(String label, int index) {
        super(label, index);
    }
    
    public Trace(int index) {
        super(SYMBOL, index);
    }
    
    public boolean isMeaningful() {
        return true;
    }
    
    public boolean isActualTrace() {
        return this.getLabel().equals(SYMBOL);
    }
    
    public Expr getMeaning(AssignmentFunction g) throws MeaningEvaluationException {
        if (this.meaning != null) return this.meaning;
        Expr m;
        if (g == null) {
            m = new GApp(this.getIndex(),this.getType());
            setMeaning(m);
            return m;
        }
        else {
            m = (Expr)g.get(getIndex(), getType());
            setMeaning(m);
            return m;
        }
    }
    
    public void setMeaning(Expr meaning) {
        Expr oldMeaning = this.meaning;
        this.meaning = meaning;
        changes.firePropertyChange("meaning", oldMeaning, this.meaning);
    }

    public void setLabel(String label) {
        throw new UnsupportedOperationException("Tried to set the label of a trace.");
    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a trace.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a trace.");
    }
    
    public String getDisplayName() {
        return "Trace";
    }

    public String toLatexString() {
        try {
            // TODO should this be changed as in this.getMeaning?
            return this.getLabel()
                    + "_{" + this.getIndex() + "}"
                    + "\\\\" + this.getMeaning(null).getType().toLatexString()
                    + "\\\\$" + this.getMeaning(null).toLatexString() + "$";
        } catch (MeaningEvaluationException ex) {
            // we don't expect this to occur
            ex.printStackTrace();
            return "";
        } catch (TypeEvaluationException ex) {
            // we don't expect this to occur
            ex.printStackTrace();
            return "";
        }
    }

    /**
     * Nothing to do on a Trace.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
    
    }


    
}
