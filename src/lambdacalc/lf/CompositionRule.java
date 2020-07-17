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

import lambdacalc.logic.Expr;

public abstract class CompositionRule {

    private String name;
    
    public CompositionRule(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
       
    /**
     * Returns whether this composition rule is applicable
     * to the given nonterminal node. If it cannot be determined
     * whether the rule is applicable, for instance because any
     * children cannot be evaluated, then false is returned.
     */
    public abstract boolean isApplicableTo(Nonterminal node);
    
    /**
     * Applies this rule to a nonterminal using an empty assignment function.
     * Overriding implementations of this method should not alter the given node.
     *
     * @param onlyIfApplicable see other method of the same name as this one.
     */
    public Expr applyTo(Nonterminal node, boolean onlyIfApplicable) throws MeaningEvaluationException {
        return applyTo(node, new AssignmentFunction(), onlyIfApplicable);
    }
    
    /**
     * Applies this rule to a nonterminal using the given assignment function.
     * Implementations of this method should not alter the given node.
     *
     * @param onlyIfApplicable if this parameter is true, calls isApplicableTo and throws a 
     * MeaningEvaluationException if that method returns false. If the parameter
     * is false, we try as much as possible to "apply" this rule even if it's 
     * strictly speaking impossible, but we reserve the right to throw a 
     * MeaningEvaluationException if there is just no conceivable way of 
     * applying this rule. (Implementation note: This parameter is used in the
     * RuleSelectionPanel in order to present the user with bogus "applications"
     * of composition rules even in cases they don't apply.)
     *
     *
     */
    public abstract Expr applyTo(Nonterminal node, 
            AssignmentFunction g, boolean onlyIfApplicable) throws MeaningEvaluationException;
            
    public static void writeToStream(CompositionRule r, java.io.DataOutputStream output) throws java.io.IOException {
        output.writeByte(0); // versioning info for future use
        output.writeUTF(r.getClass().getName());
    }
    
    public static CompositionRule readFromStream(java.io.DataInputStream input) throws java.io.IOException {
        if (input.readByte() != 0) throw new java.io.IOException("Data format error."); // sanity check
        String name = input.readUTF();
        if (name.equals("lambdacalc.lf.FunctionApplicationRule"))
            return FunctionApplicationRule.INSTANCE;
        else if (name.equals("lambdacalc.lf.PredicateModificationRule"))
            return PredicateModificationRule.INSTANCE;
        else if (name.equals("lambdacalc.lf.NonBranchingRule"))
            return NonBranchingRule.INSTANCE;
        else if (name.equals("lambdacalc.lf.LambdaAbstractionRule"))
            return LambdaAbstractionRule.INSTANCE;
        else if (name.equals("lambdacalc.lf.FunctionCompositionRule"))
            return FunctionCompositionRule.INSTANCE;
        throw new java.io.IOException("Unrecognized composition rule name in file.");
    }
}
