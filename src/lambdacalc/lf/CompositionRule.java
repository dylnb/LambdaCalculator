package lambdacalc.lf;

import lambdacalc.logic.Expr;

public abstract class CompositionRule {

    String name;
    
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
    
    public Expr applyTo(Nonterminal node) throws MeaningEvaluationException {
        return applyTo(node, new AssignmentFunction());
    }
    public abstract Expr applyTo(Nonterminal node, 
            AssignmentFunction g) throws MeaningEvaluationException;
            
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
