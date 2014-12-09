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
 * AmbiguousStringException.java
 */

package lambdacalc.logic;

import java.util.Vector;

/**
 * This subclass of SyntaxException is thrown by the ExpressionParser
 * when it is given an ambiguous string to parse.
 */
public class AmbiguousStringException extends SyntaxException {
    private Vector alternatives;

    /**
     * Constructs an instance with the given message and a set
     * of possible resolutions of the ambiguity.
     * @param message the message explaining the ambiguity
     * @param alternatives a Vector of suggested alternatives
     * to the input that would resolve the ambiguity. The elements
     * in the Vector must be strings.
     */
   public AmbiguousStringException(String message, Vector alternatives) {
        super(message
            + (alternatives != null && alternatives.size() > 0 ?
                  ":\n " + stringify(alternatives) : "")
            , -1);
        this.alternatives = alternatives;
    }
    
    private static String stringify(Vector alternatives) {
        String ambiguity = "";
        for (int i = 0; i < alternatives.size(); i++) {
            if (i > 0) ambiguity += ",\n ";
            ambiguity += (String)alternatives.get(i);
        }
        return ambiguity;
    }

}
