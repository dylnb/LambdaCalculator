/*
 * Main.java
 *
 * Created on May 29, 2006, 11:54 AM
 */

package lambdacalc;

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                new MainWindow().setVisible(true);
                //new TeacherToolWindow().setVisible(true);

            }
        });
    }
}
