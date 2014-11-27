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
        
        // TODO move isSerialized, hasBeenCompleted etc. from 
        // TrainingWindow to some other more generic place
        
        if (!TrainingWindow.isSerialized(f)) {
            return null; // let the standard look and feel handle it
        } else {
            try {
                if (TrainingWindow.hasBeenCompleted(f)) {
                    return TrainingWindow.SOLVED_FILE_ICON;
                } else {
                    return TrainingWindow.UNSOLVED_FILE_ICON;
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
