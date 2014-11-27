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
 * ExerciseFileVersionException.java
 *
 * Created on May 21, 2007, 8:35 PM
 */

package lambdacalc.exercises;

/**
 * This exception is thrown when reading an exercise file from a previous
 * version of Lambda that can no longer be opened.
 */
public class ExerciseFileVersionException extends ExerciseFileFormatException {
    
    /** Creates a new instance of ExerciseFileVersionException */
    public ExerciseFileVersionException() {
        super("This exercise file was created in a previous or later version of Lambda and cannot be opened in this version.");
    }
    
}
