package lambdacalc.gui;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class Util {
    static Font unicodeFont;
    static float fontSizeFactor;
    
    private static Font getUnicodeFont() {
        if (unicodeFont == null) {
            // fall back
            unicodeFont = new Font("Serif", 0, 12);
            fontSizeFactor = 1.0F;
            
            /*try {
                FileInputStream file = new FileInputStream("/home/tauberer/dev/lambda/gentium/Gentium102/GenR102.TTF");
                unicodeFont = Font.createFont(Font.TRUETYPE_FONT, file);
                fontSizeFactor = 1.25F;
                file.close();
            } catch (Exception e) {
                System.err.println("Error loading unicode font: " + e.toString());
                e.printStackTrace();
            }*/
        }
        
        return unicodeFont;
    }
    
    public static Font getUnicodeFont(int size) {
        return getUnicodeFont().deriveFont(fontSizeFactor * (float)size);
    }
        
    public static void displayWarningMessage
            (Component parentComponent, String message, String windowTitle) {
        
        displayMessage
                (parentComponent,
                message,
                windowTitle,
                JOptionPane.WARNING_MESSAGE);
    }
    
    public static void displayErrorMessage
            (Component parentComponent, String message, String windowTitle) {
        
        displayMessage
                (parentComponent,
                message,
                windowTitle,
                JOptionPane.ERROR_MESSAGE);
    }

        public static void displayInformationMessage
            (Component parentComponent, String message, String windowTitle) {
        
        displayMessage
                (parentComponent,
                message,
                windowTitle,
                JOptionPane.INFORMATION_MESSAGE);
    }
        
    private static void displayMessage(Component parentComponent, String message, String windowTitle,
            int messageType) {
        JOptionPane p = new JOptionPane(message,
                messageType){
            public int getMaxCharactersPerLineCount() {
                return 72;
            }
            
        };
        p.setMessage(message);
        JDialog dialog = p.createDialog(parentComponent, windowTitle);
        dialog.setVisible(true);
    }
}
