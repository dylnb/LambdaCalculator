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
 * BareIndex.java
 *
 * Created on June 5, 2007, 8:42 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;

/**
 *
 * @author champoll
 */
public class BareIndex extends Terminal {
    
    public BareIndex(String label, int i, Type type) {
        this.setLabel(label);
        this.setIndex(i);
        this.setType(type);
        this.switchOnExplicitType();
    }
    
    public BareIndex(String label, int i) {
        this.setLabel(label);
        this.setIndex(i);
    }
    
    public BareIndex(int i, Type type) {
        this.setIndex(i);
        this.setType(type);
        this.switchOnExplicitType();
    }
    
    public BareIndex(int i) {
        this.setIndex(i);
    }
    
//    public String getLabel() {
//        return this.getIndex()+"";
//    }
//    
//    public void setLabel(String label) {
//        throw new UnsupportedOperationException("Tried to set the label of a bare index.");
//    }
    
    public void setIndex(int index) {
        if (index == -1) {
            throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
        }
        super.setIndex(index);
    }
    
    public void removeIndex() {
        throw new UnsupportedOperationException("Tried to remove the index of a bare index.");
    }
    
    public String getDisplayName() {
        return "Bare index";
    }
    
    public boolean isMeaningful() {
        return false;
    }
    
    public Expr getMeaning(AssignmentFunction g) 
    throws MeaningEvaluationException {
        throw new MeaningEvaluationException("The bare index \"" + toShortString() + "\" has no denotation.");
    }
    
    /**
     * Nothing to do on a BareIndex.
     *
     * @param lexicon the lexicon
     */
    public void guessLexicalEntries(Lexicon lexicon) {
    
    }

    public String toLatexString() {
        return String.valueOf(this.getIndex());
    }
    
    
    public String toString() {
        if (this.getLabel() == null) {
            return Integer.toString(this.getIndex());
        } else {
            return super.toString();
        }
    }
}
