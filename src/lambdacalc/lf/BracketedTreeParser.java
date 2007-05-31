package lambdacalc.lf;

import java.util.*;

public class BracketedTreeParser {

    public static Nonterminal parse(String tree) throws SyntaxException {
        // A quick and dirty recurive cfg parser.
        
        Stack stack = new Stack();
        Nonterminal curnode = null;
        Terminal curterminal = null;
        
        int parseMode = 0;
        // 0 -> looking for a node, [ indicates start of nonterminal
        //                          white space is skipped
        //                          ] indicates end of node: add to parent
        //                          . indicates start of nonterminal label (like qtree)
        //                          anything else indicates start of terminal
        // 1 -> reading a nonterminal label, space indicates end
        //                                   [ ] indicate end, must back-track
        //                                   everything else gets added to label
        // 2 -> reading terminal label, space and brackets indicate end
        //                              everything else gets added to the label
        
        for (int i = 0; i < tree.length(); i++) {
        
            char c = tree.charAt(i);
            
            if (parseMode == 0) {
                // Looking for a node.
                switch (c) {
                case '[':
                    // start a nonterminal
                    Nonterminal nt = new Nonterminal();
                    if (curnode != null) {
                        curnode.getChildren().add(nt);
                        stack.push(curnode);
                    }
                    curnode = nt;
                    break;

                case '.':
                    if (curnode == null)
                        throw new SyntaxException("A period cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.getChildren().size() != 0)
                        throw new SyntaxException("A period to start a nonterminal node label cannot appear after a child node.", i);
                    parseMode = 1;
                    break;      
                                                            
                case ']':
                    if (curnode == null)
                        throw new SyntaxException("A close-bracket cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.getChildren().size() == 0)
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
                        return curnode;
                    }
                    break;
                    
                case ' ':
                    // skip
                    break;
                    
                default:
                    // this is the start of a terminal
                    if (curnode == null)
                        throw new SyntaxException("An open bracket for the root node must be the first thing in the tree.", i);
                    curterminal = new Terminal();
                    curterminal.setLabel(String.valueOf(c));
                    curnode.getChildren().add(curterminal);
                    parseMode = 2;
                    break;
                }
                
            } else if (parseMode == 1) {
                // Reading the label of the nonterminal.
                switch (c) {
                    case ' ':
                        parseMode = 0;
                        break;
                
                    case ']':
                    case '[':
                        parseMode = 0;
                        i--; // back track so they are parsed in parseMode 0
                        break;
                        
                    default:
                        String curlabel = curnode.getLabel();
                        if (curlabel == null)
                            curlabel = "";
                        curnode.setLabel(curlabel + c);
                        break;
                }
            
            } else if (parseMode == 2) {
                // Reading the label of a terminal.
                // Space and brackets indicate end. We'll back track in all
                // cases so they are parsed in parseMode 0.
                
                switch (c) {
                    case ' ':
                    case ']':
                    case '[':
                        parseMode = 0;
                        curterminal = null;
                        i--; // back track so they are parsed in parseMode 0
                        break;
                        
                    default:
                        curterminal.setLabel(curterminal.getLabel() + c);
                        break;
                }   
            }
        
        }
        
        // We return successfully when we encounter the close bracket of the
        // root node. If we get here, the tree is bad.
        throw new SyntaxException("Not enough close-brackets at the end of the tree.", tree.length() - 1);
        
    }

    public static void main(String[] args) throws SyntaxException {
        Nonterminal root = parse(args[0]);
        System.out.println(root.toString());
    }
}
