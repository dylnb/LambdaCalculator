package lambdacalc.gui;

import java.awt.*;
import java.io.*;

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
}
