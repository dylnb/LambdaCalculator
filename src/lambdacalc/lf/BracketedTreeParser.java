package lambdacalc.lf;

import java.util.*;

public class BracketedTreeParser {

    public static LFNode parse(String tree) {
        // A quick and dirty recurive cfg parser.
        
        Stack stack = new Stack();
        
        int parseMode = 0;
        // 0 -> looking for a node, [ indicates start of nonterminal
        //                          white space is skipped
        //                          anything else indicates start of terminal
        // 1 -> reading a nonterminal label, space indicates end
        //                                   brackets are invalid
        //                                   everything else gets added to label
        // 2 -> reading terminal label, space and brackets indicate end
        //                              everything else gets added to the label
        
        String readingLabel;
        
        for (int i = 0; i < tree.length(); i++) {
        
            char c = tree.charAt(i);
            
            if (parseMode == 0) {
                // Looking for a node.
                switch (c) {
                case '[':
                    // start a nonterminal
                    parseMode = 1;
                    readingLabel = "";
                    break;
                    
                case ' ':
                    // skip
                    break;
                    
                default:
                    if (stack.size() > 0)
                        ((Nonterminal)stack.peek()).getChildren().add(node);
                
                }
                
            } else if (parseMode == 1) {
                switch (c) {
                case '[':
                    throw new SyntaxException("");
                case ']':
                     Nonterminal node = new Nonterminal();
                    if (stack.size() > 0)
                        ((Nonterminal)stack.peek()).getChildren().add(node);
                    stack.push(node);
                   
                case ' ':
                    break;
                }
            }
        
        }
        
    }

}
