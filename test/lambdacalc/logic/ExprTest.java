/*
 * ExprTest.java
 * JUnit based test
 *
 * Created on June 2, 2006, 12:24 PM
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
public class ExprTest extends TestCase {
    
    public ExprTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ExprTest.class);
        
        return suite;
    }


    /**
     * Test of equals method, of class lambdacalc.logic.Expr.
     */
    public void testEquals() {
        System.out.println("equals");
        
        // innerExpr1 = x
        Expr innerExpr1 = new Var("x", new AtomicType('e'));
        // innerExpr2 = Lx.x
        Lambda innerExpr2 = new Lambda(new Var("x", new AtomicType('e')), innerExpr1, true);
        // lambda = Lx.(Lx.x)
        Lambda lambda = new Lambda(new Var("x", new AtomicType('e')), innerExpr2, true);
        
        Parens p = new Parens(lambda, Parens.ROUND);
        //Parens p2 = new Parens(lambda, Parens.SQUARE);
        
        boolean expResult = true;
        boolean result = (lambda.equals(p) && p.equals(lambda));
        assertEquals(expResult, result);
        
    }



    
}
