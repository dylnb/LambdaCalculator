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
 * GApp.java
 *
 * Created on June 7, 2007, 7:59 PM
 *
 */

package lambdacalc.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the application of the variable-assignment function g
 * to an integer index, e.g. g(1).
 *
 * @author champoll
 */
public class GApp extends Expr {
    
    public static final String SYMBOL = "g";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    
    private int index = -1;

    private Type type;
    
    private GApp() {}
    
    public GApp(int index,Type type) {
        if (index < 0) throw new IllegalArgumentException("Attempted to" +
                "create a GApp with a negative index");
        this.index=index;
        this.type=type;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public int hashCode() {
        return new Integer(this.getIndex()).hashCode() ^ super.hashCode();
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
        return this.type;
    }
    
    
    /**
     * Returns true iff this is equal to the given expression, which is the case iff
     * both are GApps with identical indices and types.
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
     * @return true iff both expressions are GApps and have the same index and type
     */
    protected boolean equals
            (Expr e, boolean collapseBoundVars, Map thisMap, 
            Map otherMap, boolean collapseAllVars, java.util.Map freeVarMap) {
        e = e.stripOutermostParens();
        return e instanceof GApp && this.getIndex() == ((GApp) e).getIndex()
                    && this.type.equals(((GApp) e).type);
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
    public Expr createAlphatypicalVariant
            (HashMap<Type,Type> alignments, Set variablesInUse, Map updates) {
        return this;
    }
    
    /**
     * Returns an (empty) List representing the fact that GApps
     * have no subexpressions.
     * @return an empty list
     */
    public List getSubExpressions() {
        return new Vector(0);
    }
    
    /**
     * Returns a copy of this GApp.
     *
     * @param subExpressions the list of subexpressions (must be null or empty)
     * @throws IllegalArgumentException if the list is nonnull and nonempty
     * @return a copy of this
     */
    public Expr createFromSubExpressions(List subExpressions)
     throws IllegalArgumentException {
        if (subExpressions != null && subExpressions.size() != 0) 
            throw new IllegalArgumentException("List is nonempty");
        return create();
    }    
    
    /**
     * Creates a new instance of this variable, that is, shallowly copies it.
     *
     * @return a copy of this
     */    
    protected GApp create() {
        return new GApp(this.getIndex(), this.type);
    }
    
    protected String toString(int mode) {
        return SYMBOL + OPEN_BRACKET + this.getIndex() + CLOSE_BRACKET;
    }
    
    /**
     * Writes a serialization of the expression to a DataOutputStream.
     * @param output the data stream to which the expression is written
     */
    public void writeToStream(java.io.DataOutputStream output)
        throws java.io.IOException {
        output.writeUTF(getClass().getName());
        output.writeShort(1); // data format version
        output.writeInt(index);
        type.writeToStream(output);
    }
   
   
    /**
     * Deserializing constructor.
     *
     * @param input the stream from which this class instance is to be read
     */
    GApp(java.io.DataInputStream input) throws java.io.IOException {
        // the class name has already been read
        if (input.readShort() != 1) throw new java.io.IOException("Invalid data."); // future version?
        index = input.readInt();
        type = Type.readFromStream(input);
    }
}
