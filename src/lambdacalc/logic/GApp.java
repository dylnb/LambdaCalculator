/*
 * GApp.java
 *
 * Created on June 7, 2007, 7:59 PM
 *
 */

package lambdacalc.logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the application of the variable-assignment function g
 * to an integer index, e.g. g(1). This class contains no notion of
 * whichever variable g maps 1 to.
 *
 * @author champoll
 */
public class GApp extends Expr {
    
    public static final String SYMBOL = "g";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    
    private int index = -1;
    
    private GApp() {}
    
    public GApp(int index) {
        if (index < 0) throw new IllegalArgumentException("Attempted to" +
                "create a GApp with a negative index");
        this.index=index;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    /**
     * Returns zero since g(1) has the strongest operator precedence.
     * @return 0
     */
    public int getOperatorPrecedence() {
        return 0;
    }
    
    
    /**
     * Gets the semantic type of this expression, that is, e.
     */
    public Type getType() throws TypeEvaluationException {
        return Type.E;
    }
    
    
    /**
     * Returns true iff this is equal to the given expression, which is the case iff
     * both are GApps with identical indices.
     *
     * @param e the other expression to compare
     *
     * @param collapseBoundVars this parameter is ignored
     * 
     * @param collapseAllVars this parameter is ignored
     *
     * @param thisMap this parameter is ignored
     *
     * @param otherMap this parameter is ignored
     *
     *
     * @return true iff both expressions are GApps and have the same index
     */
    protected boolean equals
            (Expr e, boolean collapseBoundVars, Map thisMap, 
            Map otherMap, boolean collapseAllVars) {
        return (e instanceof GApp && this.getIndex()==((GApp) e).getIndex());
    }
    
    /**
     * Returns the empty set since no variables are contained in this expression.
     * @param unboundOnly this parameter is ignored
     * @see getAllVars()
     * @see getFreeVars()
     * @return the empty set
     */
    protected Set getVars(boolean unboundOnly) {
        return new HashSet();
    }


    
    /**
     * Helper method for performLambdaConversion, always returns null
     * since there is nothing to convert.
     *
     * @param accidentalBinders this parameter is ignored
     * @throws TypeEvaluationException never thrown
     * @return null if no lambda conversion took place, otherwise the lambda-converted
     * expression 
     */
    protected Expr performLambdaConversion1(Set accidentalBinders)
    throws TypeEvaluationException {
        return null;
    }
       
    /**
     * Helper method for performLambdaConversion, always returns this 
     * expression itself since no actual lambda conversion can take place
     * inside of a GApp.
     *
     * @param var this parameter is ignored
     * @param replacement this parameter is ignored
     * @param binders this parameter is ignored
     * @param accidentalBinders this parameter is ignored
     * @throws TypeEvaluationException never thrown
     * @return the expression unchanged
     */
    protected Expr performLambdaConversion2
            (Var var, Expr replacement, Set binders, Set accidentalBinders) 
            throws TypeEvaluationException {
        return this;
    }
    

   /**
    * Always returns this expression unchanged since there are no variables
    * that would have to be changed.
    *
    * @param bindersToChange this parameter is ignored
    * @param variablesInUse this parameter is ignored
    * @param updates this parameter is ignored
    * @return the expression unchanged
    */
    protected Expr createAlphabeticalVariant
            (Set bindersToChange, Set variablesInUse, Map updates) {
        return this;
    }
    
    public String toString() {
        return SYMBOL + OPEN_BRACKET + this.getIndex() + CLOSE_BRACKET;
    }
    
    /**
     * Writes a serialization of the expression to a DataOutputStream.
     * @param output the data stream to which the expression is written
     */
    public void writeToStream(java.io.DataOutputStream output)
        throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(0); // data format version
        output.writeInt(index);
    }
   
   
    /**
     * Deserializing constructor.
     *
     * @param input the stream from which this class instance is to be read
     */
    GApp(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 0) throw new java.io.IOException("Invalid data."); // future version?
        index = input.readInt();
    }
}
