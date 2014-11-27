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
 * IdentifierTypeUnknownException.java
 *
 * Created on May 30, 2006, 5:18 PM
 */

package lambdacalc.logic;

/**
 * Thrown by the IdentifierTyper class when the type of an
 * identifier cannot be determined.
 */
public class IdentifierTypeUnknownException extends Exception {
    
    private String id;
    
    public IdentifierTypeUnknownException(String identifier) {
        super("The type of the constant or variable '" + identifier + "' is not known. Check the typing conventions that are in effect, or use subscript (underscore) notation to give its type explicitly, such as '" + identifier + "_e' to make it type e.");
        id = identifier;
    }
    
    public String getIdentifier() {
        return id;
    }
}
