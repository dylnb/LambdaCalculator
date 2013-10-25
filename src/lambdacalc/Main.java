/*
 * Main.java
 *
 * Created on May 29, 2006, 11:54 AM
 */

package lambdacalc;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import lambdacalc.gui.*;
import lambdacalc.lf.MeaningEvaluationException;
import lambdacalc.logic.SyntaxException;
import lambdacalc.logic.TypeEvaluationException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.lang.reflect.*;
//import lambdacalc.MacAdapter;


//import org.simplericity.macify.eawt.*;

/**
 * Here's the main entry point of the program.
 */

// Macify class definition
//public class Main extends JFrame implements ApplicationListener {

public class Main {
    // When changing these values, make sure to do a full rebuild (i.e. clean first)
    // because it would seem that other class files hold onto the values here
    // at compile time rather than getting them at run time. (An overzealous
    // optimization probably.)
    
    public static final boolean GOD_MODE = false;
    
    public static final boolean NOT_SO_FAST = !GOD_MODE; 
    // true means we force the user to do one step at a time in lambda conversions
    
    public static final String VERSION = "2.0 Beta";

    public static final String AUTHORS_AND_YEAR =
            "by Lucas Champollion, Joshua Tauberer,  Maribel Romero (2007-2009)," +
            "and Dylan Bumford (2013)";

    public static final String AFFILIATION =
            "The University of Pennsylvania, New York University";

    public static final String WEBSITE = "http://www.ling.upenn.edu/lambda";
    
    
    /**
     * The main entry point.  Show the main GUI window, or if the single 
     * command line argument <pre>--version</pre> is given, prints the
     * version number and mode (student edition, teacher edition) and exits.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        if (args.length == 1 && args[0].equals("--version")) {
            System.out.print("Lambda Calculator, version " + VERSION + ", ");
            if (GOD_MODE) {
                System.out.println("teacher edition");
            } else {
                System.out.println("student edition");
            }
            return;
        }
        
        // for debugging BracketedTreeParser
        if (args.length == 2 && args[0].equals("--BParser")) {
            try {
                System.out.println("treeparsing\n");
                System.out.println("input: " + args[1] + "\n");
                lambdacalc.lf.BracketedTreeParser.main(args[1]);
            } catch (SyntaxException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MeaningEvaluationException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TypeEvaluationException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        
        // for debugging Polymorphism
        if (args.length == 2 && args[0].equals("--TypeChecker")) {
            try {
                System.out.println("typechecking\n");
                System.out.println("input: " + args[1]);
                lambdacalc.logic.CompositeType type = (lambdacalc.logic.CompositeType)lambdacalc.logic.TypeParser.parse(args[1]);
                ArrayList<lambdacalc.logic.Type> types = type.getAtomicTypes();
                System.out.println("atomic types: " + types + "\n");
                
                HashMap<lambdacalc.logic.Type,lambdacalc.logic.Type> alignments = null;
                try {
                    lambdacalc.logic.Type leftType = lambdacalc.logic.TypeParser.parse("<a*a*e*s,t>");
                    lambdacalc.logic.Type rightType = lambdacalc.logic.TypeParser.parse("<n*n*e*s,t>");
                    System.out.println("types match?: " + leftType.equals(rightType) + "\n");
                    
                    System.out.println("attempting to align regardless...");
                    alignments = lambdacalc.logic.Expr.alignTypes(leftType, rightType);
                    System.out.println("alignments: " + alignments + "\n");

                    System.out.println("converting...");
                    lambdacalc.logic.Type newtype = lambdacalc.logic.Binder.getAlignedType((lambdacalc.logic.CompositeType)type, alignments);
                    System.out.println("new type: " + newtype);
                } catch (MeaningEvaluationException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SyntaxException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } 
            return;
        }
        
        // else...
        new Main();
    }   
     
    public Main() {
   
        if(lambdacalc.gui.Util.isMac()) {
            // take the menu bar off the jframe
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // set the name of the application menu item
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Lambda Calculator");

            try {
                Object app = Class.forName("com.apple.eawt.Application").getMethod("getApplication",
                 (Class[]) null).invoke(null, (Object[]) null);

                Object al = Proxy.newProxyInstance(Class.forName("com.apple.eawt.AboutHandler")
                        .getClassLoader(), new Class[] { Class.forName("com.apple.eawt.AboutHandler") },
                            new AboutListener());
                app.getClass().getMethod("setAboutHandler", new Class[] {
                    Class.forName("com.apple.eawt.AboutHandler") }).invoke(app, new Object[] { al });
            }
            catch (Exception e) {
                //fail quietly
            }            
        }
        
        try {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    TrainingWindow.showWindow();
                }
            });
        } catch (Exception e) {
            Util.displayErrorMessage(
                    WelcomeWindow.getSingleton(),
                    e.toString(),
                    e.getMessage());
        }
    }
    
    public class AboutListener implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) {
            //Show About Dialog
            String edition = "Student";
            if (GOD_MODE) {
                edition = "Teacher";
            }
            JOptionPane.showMessageDialog(null,
            "<html><b>Lambda Calculator</b></html>\n" +
            edition + "Edition, Version 2.0 Beta\n" +
            "Developed at The University of Pennsylvania and New York University\n"
            + "by Lucas Champollion, Joshua Tauberer,  Maribel Romero, and Dylan Bumford",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
    }
}
