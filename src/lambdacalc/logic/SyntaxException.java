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
 * SyntaxException.java
 *
 * Created on May 29, 2006, 3:19 PM
 */

package lambdacalc.logic;

/**
 * This class is thrown by the ExpressionParser and TypeParser
 * when they encounter a syntax error in the input.
 * The 'position' argument may be -1 if no position is relevant,
 * otherwise the character index at the point the problem occurred.
 */
public class SyntaxException extends Exception {
    private int position;
    
    /**
     * Constructs a SyntaxException with the given message for
     * a problem occurring at the given position in the string
     * being parsed.
     */
    public SyntaxException(String message, int position) {
        super(message + ". Pos: " + position);
        this.position = position;
    }
    
    /**
     * Gets the character position in which the syntax
     * error occurred.
     */
    public int getPosition() {
        return position;
    }
}
