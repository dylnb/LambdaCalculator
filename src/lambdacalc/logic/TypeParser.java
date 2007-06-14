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
        public Type Left;
        public Type Right; // this is non-null only when we're waiting for a close-bracket
    }
    
    // This implements something like a pushdown automata to parse types.
    // Types are of the form:
    //    Atomic Type:      a single letter
    //    Composite Type:   AtomicType AtomicType
    //                      <Type,Type>
    //    Product Type:     AtomicType * AtomicType * ...
    
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
    
    static ParseResult parseType(String type, int start, boolean stopSoon) throws SyntaxException {
        Stack stack = new Stack();
        ParseState current = new ParseState();
        
        boolean isParsingProduct = false;
        
        for (int i = start; i < type.length(); i++) {
            char c = type.charAt(i);
            
            if (isParsingProduct) {
                if (current.Right != null)
                    current.Right = AddProduct(current.Right, new AtomicType(c));
                else
                    current.Left = AddProduct(current.Left, new AtomicType(c));
                isParsingProduct = false;
                continue;
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
                            throw new SyntaxException("You can't have an open bracket here.  A close bracket is needed to finish the type.", i);
                        else
                            // <ab<
                            throw new SyntaxException("You can't have an open bracket here.  You seem to be missing a comma or close bracket.", i);
                    } else {
                        // a,b< or ab<
                        throw new SyntaxException("You can't have an open bracket here.", i);
                    }
                } else if (!current.ReadBracket) {
                    throw new SyntaxException("You can't start a complex type here.  Enclose the outer type with brackets.", i);
                } else {
                    stack.push(current);
                    current = new ParseState();
                    current.ReadBracket = true;
                }
                            
            } else if (c == '>' || c == CompositeType.RIGHT_BRACKET) {
                if (current.Left == null) { // still on the left side
                    if (!current.ReadBracket)
                        throw new SyntaxException("You can't have a close bracket at the beginning of a type.", i);
                    else
                        throw new SyntaxException("Insert a pair of types within the brackets.", i);
                } else if (current.Right == null) {
                    throw new SyntaxException("These brackets are unnecessary.  Remove them.", i);
                } else {
                    if (!current.ReadBracket)
                        throw new SyntaxException("You can't have a close bracket here.", i);
                    current = CloseType(stack, current);
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
                        throw new SyntaxException("A pair of complex types must be surrounded by brackets. Add brackets where needed.", i);
                    } else if (current.ReadBracket && !current.ReadComma) {
                        current.Left = new CompositeType(current.Left, current.Right);
                        current.Right = null;
                        current.ReadComma = true;
                    } else if (current.ReadBracket && current.ReadComma) {
                        throw new SyntaxException("You can't have a comma again. Are you missing brackets?", i);
                    }
                } else if (!current.ReadBracket) {
                    throw new SyntaxException("I can only understand a complex type with a comma when the type is surrounded by brackets. Add brackets where needed.", i);
                } else if (current.ReadComma) {
                    throw new SyntaxException("You can't have a comma again. Are you missing brackets?", i);
                } else {
                    current.ReadComma = true;
                }
            
            } else if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
                AtomicType at = new AtomicType(c);
                if (current.Left == null) {
                    if (stopSoon && stack.size() == 0 && !current.ReadBracket)
                        return new ParseResult(at, i);
                    current.Left = at;
                } else if (current.Right == null) {
                    if (!current.ReadBracket && !(current.Left instanceof AtomicType))
                        throw new SyntaxException("Add a comma to separate these types, and add the corresponding brackets.", i);
                    current.Right = at;
                } else {
                    if (!current.ReadBracket && current.ReadComma)
                        throw new SyntaxException("I can only understand a complex type with a comma when the type is surrounded by brackets. Add brackets where needed.", i);
                    else if (!current.ReadBracket && !current.ReadComma)
                        // ett
                        throw new SyntaxException("The expression is ambiguous. Add some brackets.", i);
                    else if (current.ReadBracket && current.ReadComma && current.Right instanceof AtomicType)
                        // <e, et>
                        current.Right = new CompositeType(current.Right, at);
                    else
                        throw new SyntaxException("The expression is ambiguous. Add some brackets.", i);
                }
                
            } else if (c == '*' || c == ProductType.SYMBOL) {
                if (current.Left == null || (current.ReadComma && current.Right == null))
                    throw new SyntaxException("'*' is used to create a type like e" + ProductType.SYMBOL + "e.  It cannot be used at the start of a type.", i);
                if ((current.Right != null && current.Right instanceof CompositeType) || (current.Left != null && current.Left instanceof CompositeType))
                    throw new SyntaxException("'*' is used to create a type like e" + ProductType.SYMBOL + "e over atomic types.  It cannot be used after a composite type.", i);
                isParsingProduct = true;
                
            } else if (Character.isWhitespace(c)) {
                // do nothing
              
            } else if (c == '(' || c == ')') {
                throw new SyntaxException("Instead of parentheses, use the angled brackets '<' and '>'.", i);
            } else if (c == '[' || c == ']') {
                throw new SyntaxException("Instead of square brackets, use the angled brackets '<' and '>'.", i);
            } else if (c == '{' || c =='}') {
                throw new SyntaxException("Instead of braces, use the angled brackets '<' and '>'.", i);
               
            } else {
                throw new BadCharacterException("The '" + c + "' character is not allowed in a type.", i);
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
                 throw new SyntaxException("I can only understand a complex type with a comma when the type is surrounded by brackets. Add brackets around the whole type.", start);
            return new ParseResult(new CompositeType(current.Left, current.Right), type.length()-1);
        }
    }
    
    private static ParseState CloseType(Stack domains, ParseState current) {
        Type ct;
        while (true) {
            ct = new CompositeType(current.Left, current.Right);
            if (domains.size() == 0) {
                current = new ParseState();
                current.Left = ct;
                return current;
            }

            current = (ParseState)domains.pop();
            if (current.Left == null) {
                current.Left = ct;
                return current;
            } else {
                current.Right = ct;
                if (current.ReadBracket) return current;
            }
        }
    }
    
    private static ProductType AddProduct(Type t, AtomicType at) {
        if (t instanceof ProductType) {
            ProductType pt = (ProductType)t;
            Type[] st = new Type[pt.getSubTypes().length + 1];
            for (int i = 0; i < pt.getSubTypes().length; i++)
                st[i] = pt.getSubTypes()[i];
            st[st.length-1] = at;
            return new ProductType(st);
        } else if (t instanceof AtomicType) {
            return new ProductType(new Type[] { t, at });
        }
        throw new RuntimeException(); // not reachable
    }
}
