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
 * ExerciseFileFormatException.java
 *
 * Created on May 31, 2006, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

/**
 *
 * @author tauberer
 */
public class ExerciseFileFormatException extends Exception {
    
    public ExerciseFileFormatException() {
        super("This does not appear to be a valid exercise file.");
    }
    
    public ExerciseFileFormatException(String message) {
        super(message);
    }

    /** Creates a new instance of ExerciseFileFormatException */
    public ExerciseFileFormatException(String message, int linenumber, String line) {
        super("On line " + linenumber + ": " + line + " the following exception occurred: " + message);
    }
    
}
