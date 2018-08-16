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
 * TypeParser.java
 *
 * Created on May 30, 2006, 10:54 AM
 */

package lambdacalc.logic;

import java.util.*;

/**
 * Parses strings representing semantic types, like e,
 * <et>, etc.
 */
public class TypeParser {
    
    private TypeParser() {
    }
    
    private static class ParseState {
        public boolean ReadBracket; // open bracket
        public boolean ReadComma;
        public boolean ReadParen;
        public boolean ReadStar;
        public boolean ParsingProduct;
        public boolean JustClosedProduct;
        public Type Left;
        public Type Right; // this is non-null only when we're waiting for a close-bracket
    }
    
    // This implements something like a pushdown automata to parse types.
    // Types are of the form:
    //    Atomic Type:      a single letter
    //    Composite Type:   AtomicType AtomicType
    //                      <Type,Type>
    //    Product Type:     AtomicType * AtomicType * ...
    //    Type Variable:    'a
    
    /**
     * Parses the string into a Type.
     * @throws SyntaxException if the string cannot be parsed
     */
    public static Type parse(String type) throws SyntaxException {
        return parseType(type, 0, false).result;
    }
    
    static class ParseResult {
        public final Type result;
        public final int end;
        public ParseResult(Type result, int end) {
            this.result = result;
            this.end = end;
        }
    }
    
    static ParseResult parseType(String type, int start, boolean stopSoon) 
    throws SyntaxException {
        Stack stack = new Stack();
        ParseState current = new ParseState();
        
//        boolean isParsingProduct = false;
        boolean isParsingVarType = false;
        
        for (int i = start; i < type.length(); i++) {
            char c = type.charAt(i);

            if (current.ParsingProduct) {
                if ('a' < c && c < 'z' || 'A' < c && 'Z' < c) {
                    Type newType;
                    if (isParsingVarType) {
                        newType = new VarType(c);
                        isParsingVarType = false;
                    } else {
                        newType = new ConstType(c);
                    }
                    if (current.Right != null) {
                        if (current.JustClosedProduct) {
                            current.Right = new ProductType(new Type[] {current.Right, newType});
                            current.ReadParen = false;
                        } else {
                            current.Right = addProduct(current.Right, newType);
                        }
                        current.ParsingProduct = false;
                        continue;
                    } else {
                        if (current.JustClosedProduct) {
                            current.Left = new ProductType(new Type[] {current.Left, newType});
                            current.ReadParen = false;
                        } else {
                            current.Left = addProduct(current.Left, newType);
                        }
                        current.ParsingProduct = false;
                        continue;
                    }
                } else if (c == Type.VarTypeSignifier) {
                    System.out.println("Got Type Variable");
                    isParsingVarType = true;
                    continue;
//                } else {
//                    throw new SyntaxException("Product subtypes must be atomic", i);
                }
//                continue;
            }


            if (c == '<' || c == CompositeType.LEFT_BRACKET) {
                if (current.Left == null) { // still on the left side
                    if (!current.ReadBracket) {
                        current.ReadBracket = true;
                    } else {
                        stack.push(current);
                        current = new ParseState();
                        current.ReadBracket = true;
                    }
                } else if (current.Right != null) {
                    if (current.ReadBracket) {
                        if (current.ReadComma)
                            // <a,b<
                            throw new SyntaxException("You can't have an open " +
                                    "bracket here.  A close bracket is needed " +
                                    "to finish the type.", i);
                        else
                            // <ab<
                            throw new SyntaxException("You can't have an open " +
                                    "bracket here.  You seem to be missing a comma or close bracket.", i);
                    } else {
                        // a,b< or ab<
                        throw new SyntaxException("You can't have an open bracket here.", i);
                    }
                } else if (!current.ReadBracket && !current.ReadStar) {
                    throw new SyntaxException("You can't start a complex type " +
                            "here.  Enclose the outer type with angle brackets <>.", i);
                } else {
                    stack.push(current);
                    current = new ParseState();
                    current.ReadBracket = true;
                }

            } else if (c == '>' || c == CompositeType.RIGHT_BRACKET) {
                if (current.Left == null) { // still on the left side
                    if (!current.ReadBracket)
                        throw new SyntaxException("You can't have a close bracket" +
                                " at the beginning of a type.", i);
                    else
                        throw new SyntaxException("Insert a pair of types within" +
                                " the brackets.", i);
                } else if (current.Right == null) {
                    if (current.Left instanceof AtomicType)
                        throw new SyntaxException("You cannot have brackets " +
                                "around an atomic type. Brackets only surround " +
                                "function types, like <e,t>. Remove these brackets.", i);
                    else if (current.Left instanceof CompositeType)
                        throw new SyntaxException("You have an extra pair of " +
                                "brackets around " + current.Left + " at the " +
                                "indicated location.  Remove these brackets.", i);
                    else
                        throw new SyntaxException("You cannot have brackets at " +
                                "the indicated location. Brackets only surround" +
                                " function types, like <e,t>.  Remove these brackets.", i);
                } else {
                    if (!current.ReadBracket)
                        throw new SyntaxException("You can't have a close bracket here.", i);
                    current = closeType(stack, current);
                    if (stopSoon && stack.size() == 0 && current.Right == null)
                        return new ParseResult(current.Left, i);
                }

            } else if (c == ',') {
                if (current.Left == null) { // still on the left side
                    throw new SyntaxException("You can't have a comma at the beginning of a type.", i);
                } else if (current.Right != null) {
                    if (!current.ReadBracket && current.ReadComma) {
                        throw new SyntaxException("You can't have a comma again. Are you missing brackets?", i);
                    } else if (!current.ReadBracket && !current.ReadComma) {
                        throw new SyntaxException("A pair of complex types must be surrounded by angle brackets < >. Add brackets where needed.", i);
                    } else if (current.ReadBracket && !current.ReadComma) {
                        current.Left = new CompositeType(current.Left, current.Right);
                        current.Right = null;
                        current.ReadComma = true;
                    } else if (current.ReadBracket && current.ReadComma) {
                        throw new SyntaxException("You can't have a comma again. Are you missing brackets?", i);
                    }
                } else if (!current.ReadBracket) {
                    throw new SyntaxException("I can only understand a complex " +
                            "type with a comma when the type is surrounded by " +
                            "angle brackets < >. Add brackets where needed, or " +
                            "remove the comma if that doesn't introduce an " +
                            "ambiguity.", i);
                } else if (current.ReadComma) {
                    throw new SyntaxException("You can't have a comma again. Are" +
                            " you missing brackets?", i);
                } else {
                    current.ReadComma = true;
                }

            } else if (c == Type.VarTypeSignifier) {
                isParsingVarType = true;

            } else if ('a' < c && c < 'z' || 'A' < c && 'Z' < c) {
                AtomicType at;
                if (isParsingVarType) {
                    at = new VarType(c);
                    isParsingVarType = false;
                } else {
                    at = new ConstType(c);
                }
                if (current.Left == null) {
                    if (stopSoon && stack.size() == 0 && !current.ReadBracket)
                        return new ParseResult(at, i);
                    current.Left = at;
                } else if (current.Right == null) {
                    if (!current.ReadBracket && !(current.Left instanceof AtomicType))
                        throw new SyntaxException("Add a comma to separate " +
                                "these types, and add the corresponding " +
                                "angle brackets <>.", i);
                    current.Right = at;
                } else {
                    if (!current.ReadBracket && current.ReadComma)
                    throw new SyntaxException("I can only understand a complex " +
                            "type with a comma when the type is surrounded by " +
                            "angle brackets < >. Add brackets where needed, or " +
                            "remove the comma if that doesn't introduce an " +
                            "ambiguity.", i);
                    else if (!current.ReadBracket && !current.ReadComma)
                        // ett
                        throw new SyntaxException("What you wrote is ambiguous. Add some angle brackets <> " +
                                "in order to indicate what you mean.", i);
                    else if (current.ReadBracket && current.ReadComma && current.Right instanceof AtomicType)
                        // <e, et>
                        current.Right = new CompositeType(current.Right, at);
                    else
                        throw new SyntaxException("The expression is ambiguous. Add some angle brackets <>.", i);
                }

            } else if (c == '*' || c == ProductType.SYMBOL) {
                if (current.Left == null || (current.ReadComma && current.Right == null))
                    throw new SyntaxException("'*' is used to create a type " +
                            "like e" + ProductType.SYMBOL + "e.  It cannot be " +
                            "used at the start of a type.", i);
//                if ((current.Right != null && current.Right instanceof CompositeType)
//                || (current.Left != null && current.Left instanceof CompositeType))
//                    throw new SyntaxException("'*' is used to create a type like e"
//                            + ProductType.SYMBOL + "e over atomic types.  " +
//                            "It cannot be used after a composite type.", i);
                current.ParsingProduct = true;
                current.ReadStar = true;

            } else if (Character.isWhitespace(c)) {
                // do nothing





            } else if (c == '(' ) {
                if (current.Left == null) { // still on the left side
                    if (!current.ReadParen) {
                        current.ReadParen = true;
                    } else {
                        stack.push(current);
                        current = new ParseState();
                        current.ReadParen = true;
                    }
                } else if (current.Right != null) {
                    if (current.ReadParen) {
                        if (current.ReadComma)
                            // <a,b<
                            throw new SyntaxException("You can't have an open " +
                                    "bracket here.  A close bracket is needed " +
                                    "to finish the type.", i);
                        else
                            // <ab<
                            throw new SyntaxException("You can't have an open " +
                                    "bracket here.  You seem to be missing a comma or close bracket.", i);
                    } else {
                        // a,b< or ab<
                        throw new SyntaxException("You can't have an open bracket here.", i);
                    }
                } else {
                    stack.push(current);
                    current = new ParseState();
                    current.ReadParen = true;
                }

            } else if (c == ')') {
                if (!current.ReadStar) {
                    throw new SyntaxException("Instead of parentheses, use " +
                            "the angle brackets '<' and '>'.", i);
                }
                if (current.Left == null) { // still on the left side
                    if (!current.ReadParen)
                        throw new SyntaxException("You can't have a close bracket" +
                                " at the beginning of a type.", i);
                    else
                        throw new SyntaxException("Insert a pair of types within" +
                                " the brackets.", i);
//                } else if (current.Right == null) {
//                    if (current.Left instanceof AtomicType)
//                        throw new SyntaxException("You cannot have brackets " +
//                                "around an atomic type. Brackets only surround " +
//                                "function types, like <e,t>. Remove these brackets.", i);
//                    else if (current.Left instanceof CompositeType)
//                        throw new SyntaxException("You have an extra pair of " +
//                                "brackets around " + current.Left + " at the " +
//                                "indicated location.  Remove these brackets.", i);
//                    else
//                        throw new SyntaxException("You cannot have brackets at " +
//                                "the indicated location. Brackets only surround" +
//                                " function types, like <e,t>.  Remove these brackets.", i);
                } else {
                    if (!current.ReadParen)
                        throw new SyntaxException("You can't have a close bracket here.", i);
                    current = closeProductType(stack, current);
                    if (stopSoon && stack.size() == 0 && current.Right == null)
                        return new ParseResult(current.Left, i);
                }









            } else if (c == '[' || c == ']') {
                throw new SyntaxException("Instead of square brackets, use " +
                        "the angle brackets '<' and '>'.", i);
            } else if (c == '{' || c =='}') {
                throw new SyntaxException("Instead of braces, use " +
                        "the angle brackets '<' and '>'.", i);
               
            } else {
                if (c == '\u2192') { // unicode right arrow
                    throw new BadCharacterException("In this program, please " +
                            "write comma (\",\") instead of \"\u2192\".", i);
                } else {
                    throw new BadCharacterException
                            ("The '" + c + "' character is not allowed in a type.", i);
                }
            }
        }
        
        if (stack.size() != 0)
            throw new SyntaxException("Your brackets are not balanced.", type.length()-1);
            
        if (current.Left == null) {
            throw new SyntaxException("Enter a type.", start);
        } else if (current.Right == null) {
            if (current.ReadBracket)
                throw new SyntaxException("You're missing the right side of the type.", type.length());
            else if (current.ReadComma)
                throw new SyntaxException("Go on typing after the comma.", type.length());
            else
                return new ParseResult(current.Left, type.length()-1);
        } else {
            if (current.ReadBracket)
                throw new SyntaxException("You're missing a closing bracket.", type.length());
            if (current.ReadComma) // comma but no brackets
                    throw new SyntaxException("I can only understand a complex " +
                            "type with a comma when the type is surrounded by " +
                            "angle brackets < >. Add brackets around the whole type, or " +
                            "remove the comma.", start);
            return new ParseResult(new CompositeType(current.Left, current.Right), type.length()-1);
        }
    }
    
    private static ParseState closeType(Stack domains, ParseState current) {
        Type ct = new CompositeType(current.Left, current.Right);
        if (domains.size() == 0) {
            current = new ParseState();
            current.Left = ct;
            return current;
        } else {
            current = (ParseState)domains.pop();
            if (current.ParsingProduct) {
                if (current.JustClosedProduct) {
                    current.Left = new ProductType(new Type[]{current.Left, ct});
                } else {
                    current.Left = addProduct(current.Left, ct);
                }
                current.ParsingProduct = false;
            }
            return current;
        }
//
//        while (true) {
//            if (current.ParsingProduct) {
//                if (current.JustClosedProduct) {
//                    ct = new ProductType(new Type[]{current.Left, ct});
//                } else {
//                    ct = addProduct(current.Left, ct);
//                }
//            } else {
//                ct = new CompositeType(current.Left, current.Right);
//            }
//            if (domains.size() == 0) {
//                current = new ParseState();
//                current.Left = ct;
//                return current;
//            }
//
//            current = (ParseState)domains.pop();
//            if (current.Left == null) {
//                current.Left = ct;
//                return current;
//            } else {
//                current.Right = ct;
//                if (current.ReadBracket) return current;
//            }
//        }
    }

    private static ParseState closeProductType(Stack domains, ParseState current) {
        Type pt = current.Left;
        if (domains.size() == 0) {
            current = new ParseState();
            current.Left = pt;
            current.JustClosedProduct = true;
            return current;
        }

        current = (ParseState)domains.pop();
        if (current.ParsingProduct) {
            if (current.JustClosedProduct) {
                current.Left = new ProductType(new Type[]{current.Left, pt});
            } else {
                current.Left = addProduct(current.Left, pt);
            }
            current.ParsingProduct = false;
        }
        return current;
//            } else {
//                current.Right = pt;
//                if (current.ReadBracket) return current;
        }


    
    private static ProductType addProduct(Type t, Type t2) {
        if (t instanceof ProductType) {
            ProductType pt = (ProductType)t;
            Type[] st = new Type[pt.getSubTypes().length + 1];
            for (int i = 0; i < pt.getSubTypes().length; i++)
                st[i] = pt.getSubTypes()[i];
            st[st.length-1] = t2;
            return new ProductType(st);
        } else {
            return new ProductType(new Type[] { t, t2 });
        }
    }
}
