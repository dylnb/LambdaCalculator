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
            JOptionPane.showMessageDialog(
                    WelcomeWindow.getSingleton(),
                    e.toString(),
                    e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
