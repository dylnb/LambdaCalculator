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
 * Not.java
 *
 * Created on May 29, 2006, 3:51 PM
 */

package lambdacalc.logic;

/**
 * Represents the negation unary operator.
 */
public class Not extends Unary {
    /**
     * The unicode negation symbol.
     */
    public static final char SYMBOL = '\u00AC';
    
    public static final char INPUT_SYMBOL = '~';

    public static final String LATEX_REPR = "\\lnot";
    
    /**
     * Constructs negation around the given expression.
     */
    public Not(Expr expr) {
        super(expr);
    }
    
    /**
     * Gets the operator precedence of this operator.
     * All values are documented in Expr, so don't change the value here
     * without changing it there.
     */
    public int getOperatorPrecedence() {
        return 3;
    }
    
    protected String toString(int mode) {
        String prefix;
        if (mode == LATEX) {
            prefix = this.LATEX_REPR;
        } else { // mode == HTML || mode == TXT
            prefix = String.valueOf(SYMBOL);
        }
        // As a special case, we don't need to put parens around binders since it's unambiguous.
        if (getInnerExpr() instanceof Binder)
            return prefix + getInnerExpr().toString(mode);
        else
            return prefix + nestedToString(getInnerExpr(), mode);
    }
    
    public Type getType() throws TypeEvaluationException {
        if (!getInnerExpr().getType().equals(Type.T))
            throw new TypeMismatchException("Negation can only be applied to something of type t, but " + getInnerExpr() + " is of type " + getInnerExpr().getType() + ".");
        return Type.T;
    }
    
    public Boolean dominatesBinder() {
      Expr ie = getInnerExpr();
      if (ie instanceof Binder) {
        //System.out.println("dominates binder: " + ie.toString());
        return true;
      } else if (ie instanceof Not && ((Not)ie).dominatesBinder()) {
        return true;
      }
      return false;
    }
    
    

    protected Unary create(Expr inner) {
        return new Not(inner);
    }

    Not(java.io.DataInputStream input) throws java.io.IOException {
        super(input);
    }
}
