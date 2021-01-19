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

//To do: Most of this class has been copied from FunctionApplication. Generalize?
package lambdacalc.lf;

import java.util.HashMap;
import java.util.Map;
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.Type;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.ConstType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;
import lambdacalc.logic.MatchPair;

public class IntensionalFunctionApplicationRule extends CompositionRule {
    public static final IntensionalFunctionApplicationRule INSTANCE 
            = new IntensionalFunctionApplicationRule();

    private IntensionalFunctionApplicationRule() {
        super("Intensional Function Application");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        if (node.size() != 2)
            return false;
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        try {
            Expr leftMeaning = left.getMeaning();
            Expr rightMeaning = right.getMeaning();

            if (isIntensionalFunctionOf(leftMeaning, rightMeaning))
                return true;
            if (isIntensionalFunctionOf(rightMeaning, leftMeaning))
                return true;
        } catch (Exception e) {
        }

        return false;
    }
    
    public Expr applyTo(Nonterminal node, boolean onlyIfApplicable, boolean 
            defaultApplyLeftToRight) 
    throws MeaningEvaluationException {
        return this.applyTo(node, new AssignmentFunction(), onlyIfApplicable, 
                defaultApplyLeftToRight);
    }     

    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) 
    throws MeaningEvaluationException {
        return this.applyTo(node, g, onlyIfApplicable, true);
    }     
    
    //the defaultApplyLeftToRight parameter is ignored if onlyIfApplicable is true
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable,
            boolean defaultApplyLeftToRight) 
    throws MeaningEvaluationException {
        if (node.size() != 2)
            throw new MeaningEvaluationException("Intensional function application is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "two children.");
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        if (onlyIfApplicable && left instanceof BareIndex) {
            throw new MeaningEvaluationException("The left child of this node is" +
                    " an index for predicate abstraction. Intensional function application is " +
                    "undefined on such a node.");
        }
        if (onlyIfApplicable && right instanceof BareIndex) {
            throw new MeaningEvaluationException("The right child of this node is" +
                    " an index for predicate abstraction. Intensional function application is " +
                    "undefined on such a node.");
        }
        
        Expr leftMeaning, rightMeaning;
        HashMap<Type,Type> typeMatches = new HashMap<>();
        try {
            leftMeaning = left.getMeaning();
            rightMeaning = right.getMeaning();
        } catch (MeaningEvaluationException mee) {
           if (onlyIfApplicable) {
               throw mee;
           } else if (defaultApplyLeftToRight) {
               return apply(left, right, g);
            } else {
               return apply(right, left, g);
            }
        }

        if (isIntensionalFunctionOf(leftMeaning, rightMeaning)) {
            try {
                CompositeType lt = (CompositeType)leftMeaning.getType();
                CompositeType rt = new CompositeType(Type.S, rightMeaning.getType());
                typeMatches = ((lt.getLeft()).matches(rt)).getMatches(lt.getLeft());
//                typeMatches = Expr.alignTypes(lt.getLeft(), rt);
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(left, right, g, typeMatches);
        } else if (isIntensionalFunctionOf(rightMeaning, leftMeaning)) {
            try {
                CompositeType rt = (CompositeType)rightMeaning.getType();
                CompositeType lt = new CompositeType(Type.S, leftMeaning.getType());
                typeMatches = ((rt.getLeft()).matches(lt)).getMatches(rt.getLeft());
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(right, left, g, typeMatches);
        }

        if (onlyIfApplicable) {
            throw new MeaningEvaluationException("The children of the nonterminal "
                    + (node.getLabel() == null ? node.toString() : node.getLabel())+ " are not of compatible types for intensional function " +
                    "application.");
        } else {
            if (defaultApplyLeftToRight) {
                return apply(left, right, g);
            } else {
                return apply(right, left, g);
            }
        }
    }
    
    private boolean isIntensionalFunctionOf(Expr left, Expr right) {
        // Return true iff left is a composite type <<s,X>,Y>
        // and right is of type X.

        try {
            Type l = left.getType();
            Type r = right.getType();
            if (l instanceof CompositeType) {
                CompositeType t = (CompositeType)l; // t = <<s,X>,Y>
                Type tl = t.getLeft();
                if (tl instanceof CompositeType) {
                    CompositeType t2 = (CompositeType)tl; // t2 = <s,X>
                    Type t2l = t2.getLeft(); // t2l = s
                    Type t2r = t2.getRight(); // t2r = X
                    if (t2l instanceof ConstType && t2l.equals(Type.S)
                            && t2r.matches(r) != null) {
                        return true;
                    }
                }
            }
        } catch (TypeEvaluationException ex) {
        }
        return false;
    }

    
    private Expr apply(LFNode fun, LFNode app, AssignmentFunction g) {
        return new FunApp(new MeaningBracketExpr(fun, g), new MeaningBracketExpr(app, g));
    }
    
    private Expr apply(LFNode fun, LFNode app, AssignmentFunction g, HashMap<Type,Type> alignments) {

        IdentifierTyper typingConventions = TrainingWindow.getCurrentTypingConventions();

        // Choose a default variable for binding the world --
        // first using the current typing conventions, and failing that, just
        // creates one using the letter w

        Var var = typingConventions.getVarForType(Type.S, false, "w");


//        if (g == null) {
//            // We are evaluating bottom-up.
//            // Get a fresh variable based on the meaning that we know we will eventually get.
//            // Choose a variable that is not in use in the simplified inner expression.
//            // See MeaningBracketExpr.evaluate().
//            try {
//                Expr bodyMeaning = body.getMeaning();
//                try { bodyMeaning =
//                        MeaningBracketExpr.
//                        replaceAllMeaningBrackets(bodyMeaning).simplifyFully();
//                } catch (TypeEvaluationException e) {
//                } // shouldn't throw since getMeaning worked
//                var = bodyMeaning.createFreshVar(var);
//
//            } catch (MeaningEvaluationException mee) {
//
//                // If we can't get the meaning, choose the default
//
//            }
//
//        } else {
//            // We are evaluating top-down.
//            // Choose a variable that is not in the range of the assignment function
//            // being passed to us. Since we will add to the assignment function,
//            // expressions within us will be sure to not create independent variables
//            // that conflict with the one we choose.
//
//            Set variablesInUse = new HashSet(g.values());
//
//            var = Expr.createFreshVar(typingConventions.getVarForType(type, false),
//                    variablesInUse);
//            // fallback: use symbol x if we don't know what else to use:
//            if (var == null) {
//                var = Expr.createFreshVar(Var.X, variablesInUse);
//            }
//        }
//
//        // Copy the assignment function being given to us and add the
//        // new mapping from the bare index to a fresh variable.
//        AssignmentFunction g2 = (g == null ? new AssignmentFunction() : new AssignmentFunction(g));
//        g2.put(index, var);
//
//        // When we evaluate the meaning bracket expression, we need to know whether
//        // we've chosen a fresh variable based on what's above (top-down) or
//        // below (bottom-up).
//        boolean topDown = (g != null);
//
//        return new Lambda(var, new MeaningBracketExpr(body, g2, topDown), true);
        
        Expr app2 = new Lambda(var, new MeaningBracketExpr(app, g), true);

        FunApp fa = new FunApp(new MeaningBracketExpr(fun, g), app2);
        if (!alignments.isEmpty()) {
            Map updates = new HashMap();
            fa = (FunApp) fa.createAlphatypicalVariant(alignments, fa.getAllVars(), updates);
        }
        return fa;
        
        // create Lambda w.[[app]]^g 1/w
//        Expr app2 = new Lambda(var, new MeaningBracketExpr(app, g), true);
//
//        return new FunApp(new MeaningBracketExpr(fun, g), app2);
    }
}
