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
 * PredicateModificationRule.java
 *
 * Created on June 5, 2007, 3:32 PM
 *
 */

package lambdacalc.lf;

import java.util.HashMap;
import java.util.Map;
import lambdacalc.gui.TrainingWindow;
import lambdacalc.logic.And;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.ConstType;
import lambdacalc.logic.Type;
import lambdacalc.logic.Expr;
import lambdacalc.logic.ExpressionParser;
import lambdacalc.logic.FunApp;
import lambdacalc.logic.IdentifierTyper;
import lambdacalc.logic.Lambda;
import lambdacalc.logic.TypeEvaluationException;
import lambdacalc.logic.Var;
import lambdacalc.logic.MatchPair;

/**
 *
 * @author champoll
 */
public class PredicateModificationRule extends CompositionRule {
    public static final PredicateModificationRule INSTANCE 
            = new PredicateModificationRule();
    
//    public static final Var VARIABLE = Var.Z;
    public static Var VARIABLE = null;
    
//    private static final ExpressionParser.ParseOptions options
//            = new ExpressionParser.ParseOptions
//            (true, // single letter identifier mode is on
//            true // certain ASCII sequences become special
//            );
    
//    private static final Expr engine = 
//            ExpressionParser.parseAndSuppressExceptions
//            ("LX.LY.Lx.[X(x)&Y(x)]", options);

    private PredicateModificationRule() {
        super("Predicate Modification");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        try {
            Type ltype = node.getLeftChild().getMeaning().getType();
            Type rtype = node.getRightChild().getMeaning().getType();
//            boolean l = ltype.equals(Type.ET);
//            boolean r = rtype.equals(Type.ET);
            boolean correctSize = node.size() == 2;
            boolean correctTypes = false;
            if (ltype instanceof CompositeType) {
                CompositeType lt = (CompositeType)ltype;
                Type ltR = lt.getRight();
                if (ltR instanceof ConstType && ltR.equals(Type.T)) {
                    if (rtype instanceof CompositeType) {
                        CompositeType rt = (CompositeType)rtype;
                        Type rtR = rt.getRight();
                        if (rtR instanceof ConstType && rtR.equals(Type.T)) {
                            if (ltype.matches(rtype) != null) {
                                correctTypes = true;
                            }
                        }
                    }
                }
            }
            return (correctSize && correctTypes);
                
        // If either child could not be evaluated (or was not composite),
        // then we just return false.
        } catch (Exception e) {
            return false;
        }            
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {

        if (!this.isApplicableTo(node)) {
            throw new MeaningEvaluationException
                    ("The predicate modification rule is only " +
                    "applicable on a nonterminal that has exactly " +
                    "two children of type <'a,t>, for some type 'a");
        }

//        return new FunApp(new FunApp(engine, new MeaningBracketExpr(node.getLeftChild(), g)),
//            new MeaningBracketExpr(node.getRightChild(), g));
        
        LFNode left = node.getLeftChild();
        LFNode right = node.getRightChild();
        Expr leftMeaning = left.getMeaning();
        Expr rightMeaning = right.getMeaning();
//        HashMap<Type,Type> typeMatches = new HashMap<Type,Type>();
        Type commonArgType = Type.E;

        try {
            CompositeType lt = (CompositeType)leftMeaning.getType();
            CompositeType rt = (CompositeType)rightMeaning.getType();
            
            MatchPair typeMatches = lt.matches(rt);
            commonArgType = typeMatches.getAlignedType(lt.getLeft());     
//            typeMatches = Expr.alignTypes(lt,rt);
//            commonArgType = ((CompositeType)Expr.getAlignedType(lt, typeMatches)).getLeft();
        } catch (TypeEvaluationException ex) {
            throw new MeaningEvaluationException(ex.getMessage());
        }
        
        MeaningBracketExpr leftM = new MeaningBracketExpr(left, g);
        MeaningBracketExpr rightM = new MeaningBracketExpr(right, g);

//        System.out.println("commonArgType: " + commonArgType);
//        VARIABLE = new Var("z", commonArgType, true);
        IdentifierTyper typingConventions = TrainingWindow.getCurrentTypingConventions();

        Var VARIABLE = typingConventions.getVarForType(commonArgType, false);
        
        FunApp leftFA = new FunApp(leftM, VARIABLE);
        FunApp rightFA = new FunApp(rightM, VARIABLE);
        
//        if (!typeMatches.isEmpty()) {
//            Map updates = new HashMap();
//            leftFA = (FunApp) leftFA.createAlphatypicalVariant(typeMatchesleftFA.getAllVars(), updates);
//            Map updates2 = new HashMap();
//            rightFA = (FunApp) rightFA.createAlphatypicalVariant(typeMatches, leftFA.getAllVars(), updates2);
//        }
        
        And and = new And(leftFA, rightFA);
        
        Lambda result = new Lambda(VARIABLE, and, true); // has period
        
        return result;
    }

}

