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
 * Terminal.java
 *
 * Created on June 13, 2007, 1:44 PM
 *
 */

package lambdacalc.lf;

import java.util.List;
import java.util.Vector;
import lambdacalc.logic.Expr;
import lambdacalc.logic.Type;

/**
 *
 * @author champoll
 */
public abstract class Terminal extends LFNode {
    
    private Type type = null;
    
    private boolean explicitType = false;
    
    protected Terminal() {
        super();
    }
    
    protected Terminal(String label, int index) {
        super(label, index);
        this.type = type.E; // default type E
    }
    
    protected Terminal(String label, int index, Type type) {
        super(label, index);
        this.type = type;
        this.explicitType = true;
    }
    
    /**
     * Nothing to do on a Terminal.
     *
     * @param rules this parameter is ignored 
     * (maybe later it can be used for type-shifting rules)
     */
    public void guessRules(RuleList rules, boolean nonBranchingOnly) {
    
    }    
    List children = new Vector(0);
    public List getChildren() {
        return children;
    }

    public abstract String toLatexString();
    
    public String toLatexString(int indent) {
        if (this instanceof LexicalTerminal) {
            return this.toLatexString(indent);
        }
        else {
            return this.toLatexString();
        }
    }
    
    public String toStringTerminalsOnly() {
//        if (this.getLabel() == null) return ""; else return this.getLabel();
        return this.toShortString();
    }
    
    public Type getType() {
        return this.type;
    }
        
    public void setType(Type t) {
        this.type = t;
    }
    
    public boolean hasExplicitType() {
        return this.explicitType;
    }
    
    public void switchOnExplicitType() {
        this.explicitType = true;
    }


}
