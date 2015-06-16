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
 * LambdaAbstractionRule.java
 *
 * Created on June 5, 2007, 8:36 PM
 *
 */

package lambdacalc.lf;

import java.util.HashSet;
import java.util.Set;
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.Expr;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;


/**
 *
 * @author champoll
 */
public class LambdaAbstractionRule extends CompositionRule {
    public static final LambdaAbstractionRule INSTANCE
            = new LambdaAbstractionRule();
    
    private LambdaAbstractionRule() {
        super("Lambda abstraction");
    }
    
    /**
     * As a side effect, determines index and body.
     */
    public boolean isApplicableTo(Nonterminal node) {
        
        if (node.size() != 2) return false;

        if (node.getLeftChild() instanceof BareIndex) {
            return !(node.getRightChild() instanceof BareIndex);
        } else {
            return (node.getRightChild() instanceof BareIndex);
        }
    }
    
    //We ignore the parameter onlyIfApplicable because there is just no way to "apply" this
    //rule in non-applicable cases.
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {
     
        if (!this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The lambda abstraction rule is only " +
                    "applicable on a nonterminal that has exactly " +
                    "two children of which one is a bare index or a " +
                    "word like \"which\" or \"such\".");
        }

        BareIndex index = null;
        LFNode body = null;
        Type type = null;

        LFNode left = node.getLeftChild();
        LFNode right = node.getRightChild();
        
        if (left instanceof BareIndex) {
            index = (BareIndex) left;
            body = right;
        } else if (right instanceof BareIndex) {
            index = (BareIndex) right;
            body = left;
        } else { //should never be reached
            throw new RuntimeException();
        }

        type = index.getType();
        
        IdentifierTyper typingConventions = TrainingWindow.getCurrentTypingConventions();

        // Choose a default variable --
        // first using the current typing conventions and the type of the bare index,
        // otherwise (if that fails) using type E -- trying to get the typing conventions for it

//        Var var = typingConventions.getVarForType(type, false);
//        if (var == null) {
//            var = typingConventions.getVarForType(Type.E, false);
//        }
//        if (var == null) {
//            var = new Var("x", Type.E, false);
//        }
        
//        System.out.println("creating LA var of type " + type + ": " + index.getIndex());
        
        if (type == null) {
            type = Type.E;
        }
        Var var = new Var("v" + index.getIndex(), type, false);
        
        if (g == null) {
            // We are evaluating bottom-up.
            // Get a fresh variable based on the meaning that we know we will eventually get.
            // Choose a variable that is not in use in the simplified inner expression.
            // See MeaningBracketExpr.evaluate().
            try {
                Expr bodyMeaning = body.getMeaning();
                try { bodyMeaning = 
                        MeaningBracketExpr.
                        replaceAllMeaningBrackets(bodyMeaning).simplifyFully(); 
                } catch (TypeEvaluationException e) {
                } // shouldn't throw since getMeaning worked
                var = bodyMeaning.createFreshVar(var);

            } catch (MeaningEvaluationException mee) {

                // If we can't get the meaning, choose the default

            }
            
        } else {
            // We are evaluating top-down.
            // Choose a variable that is not in the range of the assignment function
            // being passed to us. Since we will add to the assignment function,
            // expressions within us will be sure to not create independent variables
            // that conflict with the one we choose.

            Set variablesInUse = new HashSet(g.values());

            var = Expr.createFreshVar(typingConventions.getVarForType(type, false),
                    variablesInUse);
            // fallback: use symbol x if we don't know what else to use:
            if (var == null) {
                var = Expr.createFreshVar(Var.X, variablesInUse);
            }
        }
        
        // Copy the assignment function being given to us and add the
        // new mapping from the bare index to a fresh variable.
        AssignmentFunction g2 = (g == null ? new AssignmentFunction() : new AssignmentFunction(g));
        g2.put(index, var);
        
        // When we evaluate the meaning bracket expression, we need to know whether
        // we've chosen a fresh variable based on what's above (top-down) or
        // below (bottom-up).
        boolean topDown = (g != null);
        
        return new Lambda(var, new MeaningBracketExpr(body, g2, topDown), true);
    }
}


