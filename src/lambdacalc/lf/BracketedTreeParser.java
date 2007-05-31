package lambdacalc.lf;

import java.util.*;

public class BracketedTreeParser {

    public static Nonterminal parse(String tree) throws SyntaxException {
        // A quick and dirty recurive cfg parser.
        
        Stack stack = new Stack();
        Nonterminal curnode = null;
        Terminal curterminal = null;
        
        int parseMode = 0;
        // 0 -> looking for a node:
        //      [ indicates start of nonterminal
        //      white space is skipped
        //      ] indicates end of node: add to parent
        //      . indicates start of nonterminal label (like qtree)
        //      = indicates the start of a name of a composition rule to use
        //        for this nonterminal, terminated by a semicolon
        //      anything else indicates start of terminal
        // 1 -> reading a nonterminal label:
        //      space indicates end
        //      [ ] indicate end, must back-track
        //      everything else gets added to label
        // 2 -> reading terminal label:
        //      space and brackets indicate end
        //      = is the start of a lambda expression to
        //        assign as the lexical entry, up until the next semicolon
        //      everything else gets added to the label
        
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
                    
                case '=':
                    if (curnode == null)
                        throw new SyntaxException("An equal sign cannot appear before the starting open-bracket of the root node.", i);
                    if (curnode.getChildren().size() != 0)
                        throw new SyntaxException("An equal sign to start a nonterminal composition rule cannot appear after a child node.", i);
                    // scan for ending semicolon
                    int semi = tree.indexOf(';', i);
                    if (semi == -1)
                        throw new SyntaxException("A semicolon must terminate the end of a composition rule being assigned to a nonterminal node with '='.", i);
                    String rulename = tree.substring(i+1, semi);
                    if (rulename.equals("fa"))
                        curnode.setCompositionRule(new FunctionApplicationRule());
                    else
                        throw new SyntaxException("The name '" + rulename + "' is invalid", i);
                    i = semi; // resume from next position (i is incremented at end of iteration)
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
                        i = semi; // resume from next position (i is incremented at end of iteration)
                        parseMode = 0; // reading of terminal label is complete
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

    public static void main(String[] args) throws SyntaxException, MeaningEvaluationException, lambdacalc.logic.TypeEvaluationException {
        Nonterminal root = parse(args[0]);
        System.out.println(root.toString());
        lambdacalc.logic.Expr expr = root.getMeaning();
        System.out.println(expr);
        
        while (true) {
            lambdacalc.logic.Expr.LambdaConversionResult r = expr.performLambdaConversion();
            if (r == null)
                break;
            expr = r.Result;
            System.out.println(expr);
        }
    }
}
