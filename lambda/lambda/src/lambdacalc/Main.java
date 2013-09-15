/*
 * Main.java
 *
 * Created on May 29, 2006, 11:54 AM
 */

package lambdacalc;

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
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import lambdacalc.MacAdapter;

/**
 * Here's the main entry point of the program.
 */
public class Main {
    // When changing these values, make sure to do a full rebuild (i.e. clean first)
    // because it would seem that other class files hold onto the values here
    // at compile time rather than getting them at run time. (An overzealous
    // optimization probably.)
    
    public static final boolean GOD_MODE = true;
    
    public static final boolean NOT_SO_FAST = !GOD_MODE; 
    // true means we force the user to do one step at a time in lambda conversions
    
    public static final String VERSION = "1.2";

    public static final String AUTHORS_AND_YEAR =
            "by Lucas Champollion, Joshua Tauberer,  Maribel Romero (2007-2009)," +
            "and Dylan Bumford (2013)";

    public static final String AFFILIATION =
            "The University of Pennsylvania, New York University";

    public static final String WEBSITE = "http://www.ling.upenn.edu/lambda";

    public static String breakIntoLines(String s, int n) {
        for (int i = 0; i < s.length(); i = i + n) {
            while (i < s.length() && s.charAt(i) != ' ') {i++;}
            String pre = s.substring(0,i);
            String post = s.substring(i,s.length());
            s = pre+"\n"+post; 
        }
        return s;
    }
    
    
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
            // create an instance of the Mac Application class, so i can handle the 
            // mac quit event with the Mac ApplicationAdapter
            Application macApplication = Application.getApplication();
            MacAdapter macAdapter = new MacAdapter(this);
            macApplication.addApplicationListener(macAdapter);
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
}
