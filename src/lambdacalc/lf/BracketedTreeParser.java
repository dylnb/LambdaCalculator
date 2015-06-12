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

package lambdacalc.lf;

import java.util.*;
import lambdacalc.logic.CompositeType;
import lambdacalc.logic.SyntaxException;
import lambdacalc.logic.Type;
import lambdacalc.logic.TypeParser;

/**
 * Parses bracketed tree expressions into LFNode trees.
 *
 * A bracketed tree expression is:
 *
 * S -> NODE
 * NODE -> NONTERMINAL | TERMINAL
 * NONTERMINAL -> `[` (.LABEL)? (=RULE;)? NODE+ `]`
 * TERMINAL -> LABEL(=EXPR;)?
 * LABEL => STRING((_INDEX)?(TYPE) | (TYPE)?(_INDEX))
 * STRING => a text string, no white space
 * TYPE => a type as defined by lambdacalc.logic.TypeParser; however it must be surrounded
 * by angle brackets; an overt type is only allowed if the terminal is a trace or a pronoun
 * INDEX => a sequence of one or more digits
 * RULE => `fa`     (i.e. function application)
 * EXPR -> a predicate logic expression parsed by lambdacalc.logic.ExpressionParser.
 *
 * Implementation notes:
 * This is a simple recursive descent parser. One pass is made over the input
 * string from left to right. A state variable (parseMode) holds onto what the
 * parser is currently doing, and a stack holds the path of nonterminals from
 * the root to the node currently being processed.
 */
public class BracketedTreeParser {
    
    public static final String[] PRONOUNS = {
        "he", "she", "it", "him", "her", "himself", "herself", "itself", "his", "hers", "its", "theirs"
    };
    
    public static final String[] INDEX_WORDS = {
        "such", "that", "what", "which", "who"
    };

    
    public static boolean isPronoun(String word) {
        for (int i = 0; i < PRONOUNS.length; i++) {
            if (PRONOUNS[i].equals(word)) return true;
        }
        return false;
    }

    public static boolean isIndexWord(String word) {
        for (int i = 0; i < INDEX_WORDS.length; i++) {
            if (INDEX_WORDS[i].equals(word)) return true;
        }
        return false;
    }    

    // Stores the types of traces, so that abstraction indices don't need
    // to be explicitly typed
    public static HashMap traceTypes = new HashMap();

    public static Nonterminal parse(String tree) throws SyntaxException {
        // A quick and dirty recursive cfg parser.
        
        // A stack of the nonterminal nodes from the root down to (but not
        // including) the nonterminal node currently being processed.
        Stack stack = new Stack();
        
        // The nonterminal node currently being processed. null if we have not
        // yet encountered the root node.
        Nonterminal curnode = null;
        
        // The terminal node currently being processed while parseMode==2,
        // i.e. when we're reading a terminal label. null if parseMode!=2.
        LexicalTerminal curterminal = null;
        
        // The node that we're currently setting the index of.
        LFNode curNodeForIndex = null;

        // The node that we're currently setting the type of.
        Terminal curNodeForType = null;

        // Holds the last type read in.
        // Type type = null;
        
        // The current state of the parser.
        int parseMode = 0;
        // 0 -> looking for a node:
        //      [ indicates start of nonterminal
        //      white space is skipped
        //      ] indicates end of node, pop the stack to get current node's parent
        //      . indicates start of nonterminal label (like qtree)
        //      = indicates the start of a name of a composition rule to use
        //        for this nonterminal, terminated by a semicolon
        //      anything else indicates start of terminal
        // 1 -> reading a nonterminal label:
        //      space indicates end
        //      [, ], = indicate end, must back-track
        //      _ indicates start of index
        //      everything else gets added to label
        // 2 -> reading terminal label, possibly including type:
        //      space and brackets indicate end
        //      = is the start of a lambda expression to
        //        assign as the lexical entry, up until the next semicolon
        //      _ indicates start of index
        //      everything else gets added to the label
        // 3 -> reading the index of curNode
        //      a non-digit indicates the end, must back track so it's
        //       parsed in the right state
        //      all digits go into the index
        
        for (int i = 0; i < tree.length(); i++) {
        
            char c = tree.charAt(i);

            String rest = tree.substring(i);
            
            if (parseMode == 0) {
                // Looking for a node.
                switch (c) {
                case '[':
                    // start a nonterminal
                    Nonterminal nt = new Nonterminal();
                    if (curnode != null) {
                        curnode.addChild(nt);
                        stack.push(curnode);
                    }
                    curnode = nt;
                    break;

                case '.':
                    if (curnode == null)
                        throw new SyntaxException("A period cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.size() != 0)
                        throw new SyntaxException("A period to start a nonterminal node label cannot appear after a child node.", i);
                    parseMode = 1;
                    break;      
                                                            
                case ']':
                    if (curnode == null)
                        throw new SyntaxException("A close-bracket cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.size() == 0)
                        throw new SyntaxException("A nonterminal node must have at least one child.", i);
                    if (stack.size() > 0) {
                        curnode = (Nonterminal)stack.pop();
                    } else {
                        // We're at the end of the root node.
                        // Verify that only white space follows.
                        for (int j = i + 1; j < tree.length(); j++) {
                            if (!Character.isWhitespace(tree.charAt(j))) {
                                if (tree.charAt(j) == ']')
                                    throw new SyntaxException("There are too many close-brackets at the end of the tree.", j);
                                else
                                    throw new SyntaxException("Nothing can follow the end of the root element.", j);
                            }
                        }
                        // post-process the tree
                        typeBareIndices(curnode);
                        scrubDummies(curnode);
                        return curnode;
                    }
                    break;
                    
                case '=':
                    if (curnode == null)
                        throw new SyntaxException("An equal sign cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.size() != 0)
                        throw new SyntaxException("An equal sign to start a nonterminal composition rule cannot appear after a child node.", i);
                    // scan for ending semicolon
                    int semi = tree.indexOf(';', i);
                    if (semi == -1)
                        throw new SyntaxException("A semicolon must terminate the end of a composition rule being assigned to a nonterminal node with '='.", i);
                    String rulename = tree.substring(i+1, semi);
                    if (rulename.equals("fa"))
                        curnode.setCompositionRule(FunctionApplicationRule.INSTANCE);
                    else
                        throw new SyntaxException("The name '" + rulename + "' is invalid", i);
                    i = semi; // resume from next position (i is incremented at end of iteration)
                    break;
                    
                case LFNode.INDEX_SEPARATOR:
                    parseMode = 3;
                    curNodeForIndex = curnode;
                    break;
                        
                case ' ':
                    // skip
                    break;
                    
                default:
                    // this is the start of a terminal
                    if (curnode == null)
                        throw new SyntaxException("An open bracket for the root node must appear before any other text.", i);
                    curterminal = new LexicalTerminal(); 
                    curNodeForType = curterminal;
                    curNodeForIndex = curterminal;
                    // we always start by assuming that the current terminal is
                    // a lexical terminal; if necessary, we convert it later
                    // (in finishTerminal)
                    curterminal.setLabel(String.valueOf(c));
                    parseMode = 2;
                    break;
                }
                
            } else if (parseMode == 1) {
                // Reading the label of the nonterminal.
                switch (c) {
                    case ' ':
                        unescapeLabel(curnode);
                        parseMode = 0;
                        break;
                
                    case ']':
                    case '[':
                    case '=':
                        unescapeLabel(curnode);
                        parseMode = 0;
                        i--; // back track so they are parsed in parseMode 0
                             // (i gets incremented at end of iteration, so we
                             // set it back one here so that on next iteration
                             // we haven't moved)
                        break;


                    case LFNode.INDEX_SEPARATOR: // i.e. _
                        unescapeLabel(curnode);
                        curNodeForIndex = curnode;
                        parseMode = 3;
                        break;
                        
                    default:
                        String curlabel = curnode.getLabel();
                        if (curlabel == null)
                            curlabel = "";
                        curnode.setLabel(curlabel + transformChar(c));
                        break;
                }
            
            } else if (parseMode == 2) {
                // Reading the label of a terminal.
                // Space and brackets indicate end. We'll backtrack in all
                // cases so they are parsed in parseMode 0.
                
                switch (c) {
                    case ' ':
                    case ']':
                    case '[':
                        finishTerminal(curnode, curterminal);
                        parseMode = 0;
                        curterminal = null;
                        //type = null;
                        i--; // back track so they are parsed in parseMode 0
                        break;
                    
                    case '=':
                        // scan for ending semicolon
                        int semi = tree.indexOf(';', i);
                        if (semi == -1)
                            throw new SyntaxException("A semicolon must terminate the end of a predicate logic expression being assigned to a terminal node with '='.", i);
                        String lambda = tree.substring(i+1, semi);
                        try {
                            lambdacalc.logic.ExpressionParser.ParseOptions popts = new lambdacalc.logic.ExpressionParser.ParseOptions();
                            popts.ASCII = true;
                            popts.singleLetterIdentifiers = false;
                            lambdacalc.logic.Expr expr = lambdacalc.logic.ExpressionParser.parse(lambda, popts);
                            curterminal.setMeaning(expr);
                        } catch (lambdacalc.logic.SyntaxException ex) {
                            throw new SyntaxException("The lambda expression being assigned to '" + curterminal.getLabel() + "' is invalid: " + ex.getMessage(), i);
                        }
                        finishTerminal(curnode, curterminal);
                        i = semi; // resume from next position (i is incremented at end of iteration)
                        parseMode = 0; // reading of terminal label is complete
                        //type = null;
                        break;

                    case CompositeType.LEFT_BRACKET:
//                        curNodeForType = curterminal;
                        parseMode = 3;
                        i--;
                        break;

                    case LFNode.INDEX_SEPARATOR: // i.e. _
                        //curNodeForIndex = finishTerminal(curnode, curterminal, type);
//                        curNodeForIndex = curterminal;
                        parseMode = 3;
                        //curterminal = null;
                        break;
                        
                    default:
                        curterminal.setLabel(curterminal.getLabel() + transformChar(c));
                        break;
                }
                
            } else if (parseMode == 3) {
                // Reading the index or type of a node.
                if (LFNode.INDEX_SEPARATOR == c) {
//                    curNodeForIndex = curterminal;
                    
                } else if (Character.isDigit(c)) {
                    int idx = Integer.valueOf(String.valueOf(c)).intValue();
                    if (curNodeForIndex.getIndex() == -1) {
                        curNodeForIndex.setIndex(idx);
                                               
                    } else // perform arithmetic to do string concatenation
                        curNodeForIndex.setIndex(curNodeForIndex.getIndex() * 10 + idx);
                        
                } else if (c == CompositeType.LEFT_BRACKET) {
                    if ("Nonterminal".equals(curNodeForIndex.getDisplayName())) {
                        throw new SyntaxException("Can't explicitly type a nonterminal.", i);
                    } else {
                        // Reading the type of the terminal.
                        // scan for right bracket
                        int tstack = 1;
                        int offset = 0;
                        while (tstack != 0) {
                            offset++;
                            if (i+offset == tree.length()) {
                                throw new SyntaxException("Unmatched open angle bracket.", i);
                            }
                            char d = tree.charAt(i+offset);
                            if (d == CompositeType.LEFT_BRACKET) {
                                tstack++;
                            } else if (d == CompositeType.RIGHT_BRACKET) {
                                tstack--;
                            }
                        }
                        String typeString = tree.substring(i, i+offset+1);
                        if (typeString.length() == 3) {
                            typeString = typeString.substring(1,2); // hack to transform <e> into e to avoid stupid parsing error
                        }
                    //type = null;
                        try {
                        Type type = TypeParser.parse(typeString);
                        curNodeForType.setType(type);
                        curNodeForType.switchOnExplicitType();
                        } catch (SyntaxException s) {
                            throw new SyntaxException("Error reading type: " + s.getMessage(), i + s.getPosition());
                        }
                        i = i+offset; // resume from next position (i is incremented at end of iteration)
                    }

                } else {
                    if ("Nonterminal".equals(curNodeForIndex.getDisplayName())) {
                        parseMode = 0;
                        i--;
                    } else {
                        finishTerminal(curnode, curterminal);
                        parseMode = 0;
                        curterminal = null;
                        //type = null;
                        i--; // back track so they are parsed in parseMode 0
                    }
                }
            }
        
        }
        
        // We return successfully when we encounter the close bracket of the
        // root node. If we get here, the tree is bad.
        throw new SyntaxException("Not enough close-brackets at the end of the tree.", tree.length() - 1);
        
    }
    
    private static char transformChar(char c) {
        if (c == '\'') return lambdacalc.logic.Identifier.PRIME;
        return c;
    }
    
    private static void unescapeLabel(LFNode node) {
        String label = node.getLabel();
        if (label != null && label.startsWith("\\")) {
            String code = label.substring(1);
            String decode = lambdacalc.logic.ExpressionParser.translateEscapeCode(code);
            if (!decode.equals(code))
                node.setLabel(decode);
        }
    }
    
    private static Terminal finishTerminal(Nonterminal parent, Terminal child) {   
        unescapeLabel(child);
        if (child.getLabel() != null && child.hasIndex()) {
            
            // If the terminal label was "t" and it has an index,
            // load it as a Trace object.
            if (child.getLabel().equals(Trace.SYMBOL)) {
                if (child.hasExplicitType())
                    child = new Trace(child.getIndex(), child.getType());
                else
                    child = new Trace(child.getIndex());
                traceTypes.put(child.getIndex(), child.getType());
            }
            
            // If the label was a personal pronoun and it has an index,
            // load it as a Trace too.
            else if (isPronoun(child.getLabel())) {
                //child = new Trace(child.getIndex());
                if (child.hasExplicitType())
                    child = new Trace(child.getLabel(), child.getIndex(), child.getType());
                else
                    child = new Trace(child.getLabel(), child.getIndex());          
                
                //child = new Trace(child.getLabel(), 0, child.getType());
                // we temporarily set the index to zero --
                // index arithmetic will take care of setting the actual index for us later
            }
            
            // If the label was a relative pronoun and it has an index,
            // return it as a BareIndex.
            else if (isIndexWord(child.getLabel())) {
                if (child.hasExplicitType())
                    child = new BareIndex(child.getLabel(), child.getIndex(), child.getType());
                else
                    child = new BareIndex(child.getLabel(), child.getIndex()); 
//                child = new BareIndex(child.getLabel(), 0, child.getType());
                // child = new BareIndex(child.getLabel(), child.getIndex());
            }
        }
        
        if (child.getLabel() != null && !child.hasIndex() &&
            child.getLabel().startsWith("(") && child.getLabel().endsWith(")")) {
            child = new DummyTerminal(child.getLabel());
        }
              
        // If the terminal label was just an integer,
        // load it as a BareIndex object.
        try {
            int idx = Integer.valueOf(child.getLabel()).intValue();
            // child = new BareIndex(idx, type);
            if (child.hasExplicitType())
                child = new BareIndex(idx, child.getType());
            else
                child = new BareIndex(idx);  
        } catch (NumberFormatException e) {
            // ignore parsing error: it's not a bare index
        }
        parent.addChild(child);
        return child;
    }

    // use the types of traces to infer the types of co-indexed abstraction indices
    private static void typeBareIndices(Nonterminal node) {
        List kids = node.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            LFNode kid = (LFNode)kids.get(i);
            if ("Bare index".equals(kid.getDisplayName()) && !((BareIndex)kid).hasExplicitType()) {
                int bI = kid.getIndex();
                Type bIType = (Type)traceTypes.get(bI);
                ((BareIndex)kid).setType(bIType);
            }
            else if ("Nonterminal".equals(kid.getDisplayName()))
                typeBareIndices((Nonterminal)kid);
        }
    } 

    public static Nonterminal scrubDummies(Nonterminal node) {
        List children = node.getChildren();
        if (children.size() == 1 && node.getChild(0) instanceof DummyTerminal) {
            DummyNonterminal dnt = new DummyNonterminal(node);
//            dnt.addChild(node.getChild(0));
            System.out.println();
            System.out.println("got dummyNT: " + dnt);
            System.out.println();
            return dnt;
        } else {
            for (int i=0; i < children.size(); i++) {
                if (!(node.getChild(i) instanceof Terminal)) {
                    node.setChild(i, scrubDummies((Nonterminal)node.getChild(i)));
                }
            }
            return node;
        }
    }
    // For debugging.
    public static void main(String arg) throws SyntaxException, MeaningEvaluationException, lambdacalc.logic.TypeEvaluationException {
        Nonterminal root = parse(arg);
        System.out.println(root.toString());
        printInstances(root);
        printTypes(root); 
    }

    // for debugging
    private static void printTypes(LFNode node) {
        List kids = node.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            LFNode kid = (LFNode)kids.get(i);
            System.out.print(kid.toString());
            if ("Nonterminal".equals(kid.getDisplayName())) {
                System.out.println();
                printTypes(kid);
            }
            else {
                if (((Terminal)kid).hasExplicitType())
                    System.out.print(", expTyped");
                System.out.print(", Type: ");
                System.out.println(((Terminal)kid).getType());
            }
            System.out.println();
        }
    }

    // for debugging
    private static void printInstances(LFNode node) {
        List kids = node.getChildren();
        for (int i = 0; i < kids.size(); i++) {
            LFNode kid = (LFNode)kids.get(i);
            System.out.print(kid.toString());
            if (kid instanceof Nonterminal) {
                System.out.println();
                printInstances(kid);
            }
        }
    }  
}
