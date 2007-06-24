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
    
    public static final boolean GOD_MODE = false;

    public static String breakIntoLines(String s, int n) {
        for (int i = 0; i < s.length(); i = i + n) {
            while (s.charAt(i) != ' ' && i < s.length()) {i++;}
            s = s.substring(i)+"\n"+s.substring(i,s.length()); 
        }
        return s;
    }
    
    /**
     * The main entry point.  Show the main GUI window.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
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
