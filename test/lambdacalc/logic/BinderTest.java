/*
 * BinderTest.java
 * JUnit based test
 *
 * Created on June 1, 2006, 7:28 PM
 */

package lambdacalc.logic;

import junit.framework.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ircsppc
 */
public class BinderTest extends TestCase {
    
    public BinderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(BinderTest.class);
        
        return suite;
    }

    /**
     * Test of equals method, of class lambdacalc.logic.Binder.
     */
    public void testEquals() {
        System.out.println("equals");
        
        // innerExpr1 = x
        Expr innerExpr1 = new Var("x", new AtomicType('e'));
        // innerExpr2 = Lx.x
        Lambda innerExpr2 = new Lambda(new Var("x", new AtomicType('e')), innerExpr1, true);
        // lambda1 = Lx.(Lx.x)
        Lambda lambdaA = new Lambda(new Var("x", new AtomicType('e')), innerExpr2, true);
 
       // innerExpr11 = y
        Expr innerExpr3 = new Var("y", new AtomicType('e'));
        // innerExpr22 = Ly.y
        Lambda innerExpr4 = new Lambda(new Var("y", new AtomicType('e')), innerExpr3, true);
        // lambda2 = Ly.(Ly.y)
        Lambda lambdaB = new Lambda(new Var("y", new AtomicType('e')), innerExpr4, true);
          
        
        boolean expResult = true; // Lx.(Lx.x) equals Ly.(Ly.y)
        boolean result = (lambdaA.equals(lambdaB) && lambdaB.equals(lambdaA));
        // should go both ways
        assertEquals(expResult, result);

        // x
        Expr innerExpr5 = new Var("x", new AtomicType('e'));
        // Lx.x
        Lambda innerExpr6 = new Lambda(new Var("x", new AtomicType('e')), innerExpr5, true);
        // Lx.(Lx.x)
        Lambda lambdaC = new Lambda(new Var("x", new AtomicType('e')), innerExpr6, true);
 
        // z
        Expr innerExpr7 = new Var("z", new AtomicType('e'));
        // Lz.z
        Lambda innerExpr8 = new Lambda(new Var("z", new AtomicType('e')), innerExpr7, true);
        // Ly.(Lz.z)
        Lambda lambdaD = new Lambda(new Var("y", new AtomicType('e')), innerExpr8, true);
          
        
        boolean expResult2 = true; // Lx.(Lx.x) equals Ly.(Lz.z)
        boolean result2 = (lambdaC.equals(lambdaD) && lambdaD.equals(lambdaC));
        // should go both ways
        assertEquals(expResult2, result2);
        
        // x
        Expr innerExpr9 = new Var("x", new AtomicType('e'));
        // Ly.x
        Lambda innerExpr10 = new Lambda(new Var("y", new AtomicType('e')), innerExpr9, true);
        // Lx.(Ly.x)
        Lambda lambdaE = new Lambda(new Var("x", new AtomicType('e')), innerExpr10, true);
 
        // y
        Expr innerExpr11 = new Var("y", new AtomicType('e'));
        // Lx.y
        Lambda innerExpr12 = new Lambda(new Var("x", new AtomicType('e')), innerExpr11, true);
        // Ly.(Lx.y)
        Lambda lambdaF = new Lambda(new Var("y", new AtomicType('e')), innerExpr12, true);
          
        
        boolean expResult3 = true; // Lx.(Ly.x) equals Ly.(Lx.y)
        boolean result3 = (lambdaC.equals(lambdaD) && lambdaD.equals(lambdaC));
        // should go both ways
        assertEquals(expResult3, result3); 

        // (x,y)
        Expr[] xyarray = new Expr[] { new Var("x", Type.E), new Var("y", Type.E)};
        Expr xy = new ArgList(xyarray);
        Expr Rxy = new FunApp(new Const("R", Type.ExET), xy);
        // Ly.R(x,y)
        Lambda LyRxy = new Lambda(new Var("y", Type.E), Rxy, true);
        // Ex.Ly.R(x,y)
        Exists ExLyRxy = new Exists(new Var("x", Type.E), LyRxy, true);

        // (y,y)
        Expr[] yyarray = new Expr[] { new Var("y", Type.E), new Var("y", Type.E)};
        Expr yy = new ArgList(yyarray);
        Expr Ryy = new FunApp(new Const("R", Type.ExET), yy);
        // Ly.R(y,y)
        Lambda LyRyy = new Lambda(new Var("y", Type.E), Ryy, true);
        // Ey.Ly.R(y,y)
        Exists EyLyRyy = new Exists(new Var("y", Type.E), LyRyy, true);
        
        boolean expResult4 = false; // Ex.Ly.R(x,y) != Ey.Ly.R(y,y)
        boolean result4 = (ExLyRxy.equals(EyLyRyy));
        assertEquals(expResult4, result4);
        
        boolean expResult5 = false; // same thing both ways
        boolean result5 = (EyLyRyy.equals(ExLyRxy));
        assertEquals(expResult5, result5);
    }
    
    public void testEquals2() {
        assertEquals(parseExpr("LxEy'R(y,x) (y)").equals(parseExpr("LxEyR(y,x) (y)")), false);

    }
    
    private Expr parseExpr(String expr) {
        ExpressionParser.ParseOptions opts = new ExpressionParser.ParseOptions();
        opts.SingleLetterIdentifiers = true;
        opts.ASCII = true;
        opts.Typer = new IdentifierTyper();
        try {
            return ExpressionParser.parse(expr, opts);
        } catch (SyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
        
    }

    /**
     * Test of toString method, of class lambdacalc.logic.Binder.
     */
    public void testToString() {
        System.out.println("toString");
        
        Binder instance = null;
        
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSymbol method, of class lambdacalc.logic.Binder.
     */
    public void testGetSymbol() {
        System.out.println("getSymbol");
        
        Binder instance = null;
        
        String expResult = "";
        String result = instance.getSymbol();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of create method, of class lambdacalc.logic.Binder.
     */
    public void testCreate() {
        System.out.println("create");
        
        Identifier variable = null;
        Expr innerExpr = null;
        Binder instance = null;
        
        Binder expResult = null;
        Binder result = instance.create(variable, innerExpr);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVariable method, of class lambdacalc.logic.Binder.
     */
    public void testGetVariable() {
        System.out.println("getVariable");
        
        Binder instance = null;
        
        Identifier expResult = null;
        Identifier result = instance.getVariable();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInnerExpr method, of class lambdacalc.logic.Binder.
     */
    public void testGetInnerExpr() {
        System.out.println("getInnerExpr");
        
        Binder instance = null;
        
        Expr expResult = null;
        Expr result = instance.getInnerExpr();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasPeriod method, of class lambdacalc.logic.Binder.
     */
    public void testGetHasPeriod() {
        System.out.println("getHasPeriod");
        
        Binder instance = null;
        
        boolean expResult = true;
        boolean result = instance.hasPeriod();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVars method, of class lambdacalc.logic.Binder.
     */
    public void testGetVars() {
        System.out.println("getVars");
        
        boolean unboundOnly = true;
        Binder instance = null;
        
        Set expResult = null;
        Set result = instance.getVars(unboundOnly);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of substitute method, of class lambdacalc.logic.Binder.
     */
    public void testSubstitute() {
        System.out.println("substitute");
        
        Var var = null;
        Expr replacement = null;
        Set unboundVars = null;
        Set potentialAccidentalBindings = null;
        Set accidentalBindings = null;
        Binder instance = null;
        
        Expr expResult = null;
        Expr result = instance.substitute(var, replacement, unboundVars, potentialAccidentalBindings, accidentalBindings);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createAlphabeticalVariant method, of class lambdacalc.logic.Binder.
     */
    public void testCreateAlphabeticalVariant() {
        System.out.println("createAlphabeticalVariant");
        
        Set bindersToChange = null;
        Set variablesInUse = null;
        Map updates = null;
        Binder instance = null;
        
        Expr expResult = null;
        Expr result = instance.createAlphabeticalVariant(bindersToChange, variablesInUse, updates);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Generated implementation of abstract class lambdacalc.logic.Binder. Please fill dummy bodies of generated methods.
     */
    private class BinderImpl extends Binder {

        BinderImpl(lambdacalc.logic.Identifier ident, lambdacalc.logic.Expr innerExpr, boolean hasPeriod) {
            super(ident, innerExpr, hasPeriod);
        }

        public java.lang.String getSymbol() {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }

        protected lambdacalc.logic.Binder create(lambdacalc.logic.Identifier variable, lambdacalc.logic.Expr innerExpr) {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }

        public lambdacalc.logic.Type getType() {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }

        protected boolean equals(lambdacalc.logic.Expr e, java.util.Map map) {
            // TODO fill the body in order to provide useful implementation
            
            return false;
        }

        protected java.util.Set getVars(boolean unboundOnly) {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }

        protected lambdacalc.logic.Expr substitute(lambdacalc.logic.Var var, lambdacalc.logic.Expr replacement, java.util.Set unboundVars, java.util.Set potentialAccidentalBindings, java.util.Set accidentalBindings) {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }

        protected lambdacalc.logic.Expr createAlphabeticalVariant(java.util.Set bindersToChange, java.util.Set variablesInUse, java.util.Map updates) {
            // TODO fill the body in order to provide useful implementation
            
            return null;
        }
    }

    
}
