//To do: Most of this class has been copied from FunctionApplication. Generalize?
package lambdacalc.lf;

import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.Type;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;

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
                    " an index for lambda abstraction. Intensional function application is " +
                    "undefined on such a node.");
        }
        if (onlyIfApplicable && right instanceof BareIndex) {
            throw new MeaningEvaluationException("The right child of this node is" +
                    " an index for lambda abstraction. Intensnional function application is " +
                    "undefined on such a node.");
        }
        
        Expr leftMeaning, rightMeaning;
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
            return apply(left, right, g);
        }
        if (isIntensionalFunctionOf(rightMeaning, leftMeaning)) {
            return apply(right, left, g);
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
            if (left.getType() instanceof CompositeType) {
                CompositeType t = (CompositeType)left.getType(); // t = <<s,X>,Y>
                if (t.getLeft() instanceof CompositeType) {
                    CompositeType t2 = (CompositeType) t.getLeft(); // t2 = <s,X>
                    if (t2.getLeft().equals(Type.S)  // t2.getLeft = s
                            && t2.getRight().equals(right.getType())) { // t2.getRight = X
                        return true;
                    }
                }
            }
        } catch (TypeEvaluationException ex) {
        }
        return false;
    }

    
    private Expr apply(LFNode fun, LFNode app, AssignmentFunction g) {

        IdentifierTyper typingConventions = TrainingWindow.getCurrentTypingConventions();

        // Choose a default variable for binding the world --
        // first using the current typing conventions, and failing that, just
        // creates one using the letter w

        Var var = typingConventions.getVarForType(Type.S, false);
        if (var == null) {
            var = new Var("w", Type.S, false);
        }

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

        // create Lambda w.[[app]]^g 1/w
        Expr app2 = new Lambda(var, new MeaningBracketExpr(app, g), true);

        return new FunApp(new MeaningBracketExpr(fun, g), app2);
    }
}
