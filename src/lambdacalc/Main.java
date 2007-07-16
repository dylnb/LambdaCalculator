/*
 * Main.java
 *
 * Created on May 29, 2006, 11:54 AM
 */

package lambdacalc;

import javax.swing.JOptionPane;
import lambdacalc.gui.*;

/**
 * Here's the main entry point of the program.
 */
public class Main {
    // When changing these values, make sure to do a full rebuild (i.e. clean first)
    // because it would seem that other class files hold onto the values here
    // at compile time rather than getting them at run time. (An overzealous
    // optimization probably.)
    
    public static final boolean GOD_MODE = false;
    
    public static final boolean NOT_SO_FAST = !GOD_MODE; 
    // true means we force the user to do one step at a time in lambda conversions
    
    public static final String VERSION = "1.0.4";

    public static String breakIntoLines(String s, int n) {
        for (int i = 0; i < s.length(); i = i + n) {
            while (s.charAt(i) != ' ' && i < s.length()) {i++;}
            s = s.substring(i)+"\n"+s.substring(i,s.length()); 
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
        
        // else...
        
        try {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {

                    WelcomeWindow.showWindow();

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
