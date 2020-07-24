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
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.And;
import lambdacalc.logic.AtomicType;
import lambdacalc.logic.Binder;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.ConstType;
import lambdacalc.logic.Expr;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.ProductType;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;
import lambdacalc.logic.VarType;

public class FunctionCompositionRule extends CompositionRule {
    public static final FunctionCompositionRule INSTANCE 
            = new FunctionCompositionRule();

    private FunctionCompositionRule() {
        super("Function Composition");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        if (node.size() != 2)
            return false;
        
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        
        try {
            Type ltype = node.getLeftChild().getMeaning().getType();
            Type rtype = node.getRightChild().getMeaning().getType();
            
            CompositeType lt = (CompositeType)ltype;
            Type ltR = lt.getRight();
            Type ltL = lt.getLeft();
            
            CompositeType rt = (CompositeType)rtype;
            Type rtR = rt.getRight();
            Type rtL = rt.getLeft();
            
            if (ltR.equals(rtL) || rtR.equals(ltL)) 
                return true;
        
        // If either child could not be evaluated (or was not composite),
        // then we just return false.    
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
        if (!this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The function composition rule is only " +
                    "applicable on a nonterminal that has exactly " +
                    "two children of types <'a,'b> and <'b,'c>, for arbitrary types 'a, 'b, and c'.");
        }
          
        LFNode left = node.getChild(0);
        LFNode right = node.getChild(1);
        Expr leftMeaning = left.getMeaning();
        Expr rightMeaning = right.getMeaning();   
        
        HashMap<Type,Type> typeMatches = new HashMap<>();

        if (isFCFunctionOf(leftMeaning, rightMeaning)) {
            try {
                // lt: <'a,'b>, rt: <'c,'a>
                CompositeType lt = (CompositeType)leftMeaning.getType();
                CompositeType rt = (CompositeType)rightMeaning.getType();
                typeMatches = Expr.alignTypes(lt.getLeft(),rt.getRight());
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(left, right, g, typeMatches);
        } else if (isFCFunctionOf(rightMeaning, leftMeaning)) {
            try {
                // lt: <'c,'a>, rt: <'a,'b>
                CompositeType lt = (CompositeType)leftMeaning.getType();
                CompositeType rt = (CompositeType)rightMeaning.getType();
                typeMatches = Expr.alignTypes(rt.getLeft(),lt.getRight());
            } catch (TypeEvaluationException ex) {
                throw new MeaningEvaluationException(ex.getMessage());
            }
            return apply(right, left, g, typeMatches);
        } else {
            throw new MeaningEvaluationException("The children of the nonterminal "
                    + "are not of compatible types for function composition.");
        }
    }
     
    private boolean isFCFunctionOf(Expr left, Expr right) {
        // Return true iff left is a composite type <X,Y>
        // and right is of type <Z,X>.
        try {
            CompositeType lt = (CompositeType)left.getType();
            CompositeType rt = (CompositeType)right.getType();
            
            if (lt.getLeft().equals(rt.getRight())) {
                // Call to alignTypes ensures that an error is thrown if the same VarType
                // is matched to multiple constant types
                HashMap<Type,Type> typeMatches = Expr.alignTypes(lt.getLeft(),rt.getRight());
                return true;
            }
            
        } catch (TypeEvaluationException ex) {
        } catch (MeaningEvaluationException me) {
        }
        return false;
    }    
    
    
    private Expr apply(LFNode fun, LFNode app, AssignmentFunction g, HashMap<Type,Type> typeMatches) throws MeaningEvaluationException {
        Expr leftMeaning = fun.getMeaning();
        Expr rightMeaning = app.getMeaning();
//        HashMap<Type,Type> typeMatches = new HashMap<Type,Type>();
        Type ArgType = Type.E;

        try {        
            // lt: <'a,'b>
            CompositeType lt = (CompositeType)leftMeaning.getType();

            // rt: <'c,'a>
            CompositeType rt = (CompositeType)rightMeaning.getType();

            // ArgType: 'c
            ArgType = ((CompositeType)Expr.getAlignedType(rt, typeMatches)).getLeft();
        } catch (TypeEvaluationException ex) {
            throw new MeaningEvaluationException(ex.getMessage());
        }
            
        MeaningBracketExpr leftM = new MeaningBracketExpr(fun, g);
        MeaningBracketExpr rightM = new MeaningBracketExpr(app, g);

        IdentifierTyper typingConventions = TrainingWindow.getCurrentTypingConventions();

        Var VARIABLE = typingConventions.getVarForType(ArgType, false);
        
        // apply the right node to a variable
        FunApp rightFA = new FunApp(rightM, VARIABLE, typeMatches);
        
        if (!typeMatches.isEmpty()) {
//            Map updates = new HashMap();
//            leftFA = (FunApp) leftFA.createAlphatypicalVariant(typeMatches, leftFA.getAllVars(), updates);
            Map updates2 = new HashMap();
            rightFA = (FunApp) rightFA.createAlphatypicalVariant(typeMatches, rightFA.getAllVars(), updates2);
        }
        
        // apply the left node to the resulting rightFA
        FunApp leftFA = new FunApp(leftM, rightFA, typeMatches);
        
        Lambda result = new Lambda(VARIABLE, leftFA, true); // has period
        
        return result; 
    }
}