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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import lambdacalc.logic.AtomicType;
import lambdacalc.logic.Binder;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.ProductType;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.VarType;

public class FunctionApplicationRule extends CompositionRule {
    public static final FunctionApplicationRule INSTANCE 
            = new FunctionApplicationRule();

    private FunctionApplicationRule() {
        super("Function Application");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        if (node.size() != 2)
            return false;
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        try {
            Expr leftMeaning = left.getMeaning();
            Expr rightMeaning = right.getMeaning();

            if (isFunctionOf(leftMeaning, rightMeaning))
                return true;
            if (isFunctionOf(rightMeaning, leftMeaning))
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
            throw new MeaningEvaluationException("Function application is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "two children.");
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        if (onlyIfApplicable && left instanceof BareIndex)
            throw new MeaningEvaluationException("The left child of this node is" +
                    " an index for lambda abstraction. Function application is " +
                    "undefined on such a node.");
        if (onlyIfApplicable && right instanceof BareIndex)
            throw new MeaningEvaluationException("The right child of this node is" +
                    " an index for lambda abstraction. Function application is " +
                    "undefined on such a node.");
        
        Expr leftMeaning, rightMeaning;
        HashMap<Type,Type> typeMatches = new HashMap<>();
        try {
            leftMeaning = left.getMeaning();
            rightMeaning = right.getMeaning();
        } catch (MeaningEvaluationException mee) {
            if (onlyIfApplicable)
               throw mee;
            else if (defaultApplyLeftToRight)
                return apply(left, right, g);
            else
                return apply(right, left, g);
        }

        if (isFunctionOf(leftMeaning, rightMeaning)) {
            try {
                CompositeType lt = (CompositeType)leftMeaning.getType();
                typeMatches = Expr.alignTypes(lt.getLeft(),rightMeaning.getType());
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(left, right, g, typeMatches);
        } else if (isFunctionOf(rightMeaning, leftMeaning)) {
            try {
                CompositeType rt = (CompositeType)rightMeaning.getType();
                typeMatches = Expr.alignTypes(rt.getLeft(),leftMeaning.getType());
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(right, left, g, typeMatches);
        }

        if (onlyIfApplicable) {
            throw new MeaningEvaluationException("The children of the nonterminal "
                + (node.getLabel() == null ? node.toString() : node.getLabel())+
                " are not of compatible types for function application.");
        } else {
            if (defaultApplyLeftToRight)
                return apply(left, right, g);
            else
                return apply(right, left, g);
        }
    }
    
    private boolean isFunctionOf(Expr left, Expr right) {
        // Return true iff left is a composite type <X,Y>
        // and right is of type X.
        try {
            Type l = left.getType();
            Type r = right.getType();
            if (l instanceof CompositeType) {
                CompositeType t = (CompositeType)l;
                if (t.getLeft().equals(r))
                    return true;
                else if (t.getLeft() instanceof ProductType){
                    ProductType pt = (ProductType)t.getLeft();
                    if (Arrays.asList(pt.getSubTypes()).contains(r)) {
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
        FunApp fa = new FunApp(new MeaningBracketExpr(fun, g), new MeaningBracketExpr(app, g), alignments);
        if (!alignments.isEmpty()) {
            Map updates = new HashMap();
            fa = (FunApp) fa.createAlphatypicalVariant(alignments, fa.getAllVars(), updates);
        }
        return fa;
    }
    

}
