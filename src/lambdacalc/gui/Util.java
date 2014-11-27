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

package lambdacalc.gui;

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

// Hello world

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

    private static String osName = System.getProperty("os.name");
    public static String getOSName() { return osName; }

    private static boolean isWin = osName.startsWith("Windows");
    public static boolean isWin() { return isWin; }
    
    private static boolean isMac = !isWin && osName.startsWith("Mac");
    public static boolean isMac() { return isMac; }
    
    private static boolean isLinux = osName.startsWith("Linux");
    public static boolean isLinux() { return isLinux; }
    
    private static boolean isVista = isWin && osName.indexOf("Vista")!=-1;
    public static boolean isVista() { return isVista; }
    
}
