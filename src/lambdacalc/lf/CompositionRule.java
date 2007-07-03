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
     * MeaningEvaluationException if that method returns falls. If the parameter
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
        throw new java.io.IOException("Unrecognized composition rule name in file.");
    }
}
