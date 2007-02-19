/*
 * ExerciseFileView.java
 *
 * Created on June 8, 2006, 11:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.gui;

import java.io.*;
import javax.swing.filechooser.*;
import java.util.Hashtable;
import javax.swing.Icon;
import lambdacalc.exercises.ExerciseFileFormatException;

/**
 *
 * @author ircsppc
 */
public class ExerciseFileView extends FileView {
    
    private Hashtable typeDescriptions = new Hashtable(5);

    /**
     * Creates a new instance of ExerciseFileView
     */
    public ExerciseFileView() {
    }

    /**
     * Adds a human readable type description for files. Based on "dot"
     * extension strings, e.g: ".gif". Case is ignored.
     */
    public void putTypeDescription(String extension, String typeDescription) {
	typeDescriptions.put(extension, typeDescription);
    }

    /**
     * Adds a human readable type description for files of the type of
     * the passed in file. Based on "dot" extension strings, e.g: ".gif".
     * Case is ignored.
     */
    public void putTypeDescription(File f, String typeDescription) {
	putTypeDescription(getExtension(f), typeDescription);
    }

    
    /**
     * A human readable description of the type of the file.
     *
     * @see FileView#getTypeDescription
     */
    public String getTypeDescription(File f) {
        return "hello";
	//return (String) typeDescriptions.get(getExtension(f));
    }
    public String getDescription(File f) {
        return "hello";
	//return (String) typeDescriptions.get(getExtension(f));
    }    
 
    public Icon getIcon(File f) {
//	Icon icon = null;
//	String extension = getExtension(f);
//	if(extension != null) {
//	    icon = (Icon) icons.get(extension);
//	}
//	return icon;
        if (!MainWindow.isSerialized(f)) {
            return null; // let the standard look and feel handle it
        } else {
            try {
                if (MainWindow.hasBeenCompleted(f)) {
                    return MainWindow.SOLVED_FILE_ICON;
                } else {
                    return MainWindow.UNSOLVED_FILE_ICON;
                }
            } catch (ExerciseFileFormatException ex) {
                ex.printStackTrace();
                return null; // let the look and feel handle it
            } catch (IOException ex) {
                // normally, this shouldn't occur because getIcon() only gets
                // called from within a JFileChooser
                ex.printStackTrace();
                return null; // let the look and feel handle it
            } 
        }
    }
    /**
     * Conveinience method that returnsa the "dot" extension for the
     * given file.
     */
    public static String getExtension(File f) {
	String name = f.getName();
	if(name != null) {
	    int extensionIndex = name.lastIndexOf('.');
	    if(extensionIndex < 0) {
		return null;
	    }
	    return name.substring(extensionIndex+1).toLowerCase();
	}
	return null;
    }    
    
}
