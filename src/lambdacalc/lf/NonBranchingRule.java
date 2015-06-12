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
 * NonBranchingRule.java
 *
 * Created on June 5, 2007, 3:18 PM
 *
 */

package lambdacalc.lf;

import lambdacalc.logic.Expr;
import lambdacalc.logic.TypeEvaluationException;

/**
 *
 * @author champoll
 */
public class NonBranchingRule extends CompositionRule {
    public static final NonBranchingRule INSTANCE 
            = new NonBranchingRule();

    private NonBranchingRule() {
        super("Non-Branching Node");
    }
    
    public boolean isApplicableTo(Nonterminal node) {
        if (node instanceof DummyNonterminal) {
            return true;
        }
        // Count up the children of node that are not dummies. If there's
        // just one, then we are a non-branching node.
        int nChildren = 0;
        for (int i = 0; i < node.size(); i++)
            if (!(node.getChild(i) instanceof DummyTerminal ||
                  node.getChild(i) instanceof DummyNonterminal))
                nChildren++;
        return nChildren == 1;
    }
    
    public Expr applyTo(Nonterminal node, AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException {
        if (!isApplicableTo(node))
            throw new MeaningEvaluationException
                    ("The non-branching node rule is not " +
                    "applicable on a nonterminal that does not have exactly " +
                    "one child.");
        
        // Find first non-DummyTerminal.
        for (int i = 0; i < node.size(); i++) {
            if (!(node.getChild(i) instanceof DummyTerminal ||
                  node.getChild(i) instanceof DummyNonterminal)) {
                return new MeaningBracketExpr(node.getChild(i), g);
            }
        }
        // All nodes were dummy terminals; return the first
        return new MeaningBracketExpr(node.getChild(0), g);
    }
}
