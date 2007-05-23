/*
 * ExpressionParser.java
 *
 * Created on May 29, 2006, 3:02 PM
 */

package lambdacalc.logic;

import java.util.*;

/**
 * Parses an expression into an Expr object.
 */
public class ExpressionParser {
    /**
     * Options for parsing expressions.
     */
    public static class ParseOptions {
        /**
         * This turns on single-letter-identifier mode, so that
         * Pa is interpreted as P(a), rather than as a single
         * identifier.
         */
        public boolean SingleLetterIdentifiers;
        
        /**
         * This turns on ASCII mode.  These symbols become special:
         * A, E, L, ~, &, |, ->, <->
         * for
         * for all, exists, lambda, not, and, or, if, iff
         */
        public boolean ASCII;

        /*
         * This provides the types for identifiers encountered
         * in the expression.  Every identifier must be
         * identifiable as a constant or variable and with a type.
         * By default, this field contains an IdentifierTyper
         * with a default setup.
         */
        public IdentifierTyper Typer = new IdentifierTyper();
     }
    
    private static class ParseResult {
        public Expr Expression;
        public int Next;
        
        public ParseResult(Expr expression, int next) {
            Expression = expression;
            Next = next;
        }
    }
    
    private ExpressionParser() {
    }

    /**
     * Parses an expression with the given options.
     */
    public static Expr parse(String expression, ParseOptions options) throws SyntaxException {
        if (expression.length() == 0)
            throw new SyntaxException("Enter a lambda expression.", -1);
        
        ParseResult r = parseExpression(expression, 0, options, "an expression");
        if (r.Next != expression.length())
            throw new SyntaxException("\"" + expression.substring(r.Next) + "\" doesn't look like a lambda expression.", r.Next);
        
        return r.Expression;
    }
    
    private static int skipWhitespace(String expression, int start, String expected, boolean allowEOS) throws SyntaxException {
        while (true) {
            if (start == expression.length()) {
                if (allowEOS) return -1;
                throw new SyntaxException("You seem to be missing " + expected + " at the end of your answer.", start);
            }
            if (expression.charAt(start) != ' ')
                break;
            start++;
        }
        return start;
    }
    
    private static boolean isLetter(char c) {
        return Character.isLetter(c) && c != Lambda.SYMBOL;
    }
    
    private static char getChar(String expression, int index, ParseOptions context) {
        char c = expression.charAt(index);
        if (context.ASCII) {
            // All of the ASCII symbol replacements at this level are 
            // single character substitutions.
            switch (c) {
                case '~': c = Not.SYMBOL; break;
                case 'A': c = ForAll.SYMBOL; break;
                case 'E': c = Exists.SYMBOL; break;
                case 'L': c = Lambda.SYMBOL; break;
            }
        }
        return c;
    }
    
    /*** 
     * Parses the prefix expression beginning at position start in expression.
     * These include parenthesis expressions, negation expressions, binding
     * expressions, and identifiers. If none of these expressions are present
     * at position start, a BadCharacterException is thrown.
     */
    private static ParseResult parsePrefixExpression(String expression, int start, ParseOptions context, String whatIsExpected) throws SyntaxException {
        start = skipWhitespace(expression, start, whatIsExpected, false);
        char c = getChar(expression, start, context);
        
        switch (c) {
            case '(':
            case '[':
                ParseResult parenr = parseExpression(expression, start+1, context, "an expression inside your parentheses or brackets");
                start = skipWhitespace(expression, parenr.Next, "a ')'", false);
                char closeChar = (c == '(') ? ')' : ']';
                if (getChar(expression, start, context) != closeChar)
                    throw new SyntaxException("You need a '" + closeChar + "' here but a '" + getChar(expression, start, context) + "' was found.", start);
                return new ParseResult(new Parens(parenr.Expression, c == '(' ? Parens.ROUND : Parens.SQUARE), start+1);
            
            case Not.SYMBOL:
                ParseResult negr = parsePrefixExpression(expression, start+1, context, "an expression after the negation operator");
                return new ParseResult(new Not(negr.Expression), negr.Next);
            
            case ForAll.SYMBOL:
            case Exists.SYMBOL:
            case Lambda.SYMBOL:
                ParseResult var = parseIdentifier(expression, start+1, context, "a variable", true, false);
                if (!(var.Expression instanceof Identifier))
                    throw new SyntaxException("After a binder, an identifier must come next: " + var.Expression + ".", start+1);
                start = var.Next;

                start = skipWhitespace(expression, start, "a '.' or an expression", false);
                boolean hadPeriod = false;
                if (getChar(expression, start, context) == '.') {
                    start++;
                    hadPeriod = true;
                }
                
                // Brackets immediately following binders (without periods) should indicate the scope of the binder. OTOH, binders
                // can immediately outscope conjunctions (Ex.a ^ b), and the first conjunct can be a Parens.
                // That means we have an ambiguity in the grammar. We need a special case so that
                // Lx[...] & [...] is treated as (Lx[...]) & ([...]).
                // In addition, lambdas inside lambdas and PropositionalBinders inside PropositionalBinders
                // should be taken the same way -- as outscoping any infix operators
                // (i.e. LxLy.a & b  is not  Lx[(Ly.a) & b]).
                
                ParseResult rhs = parsePrefixExpression(expression, start, context, "an expression in the scope of the lambda operator");
                if (
                       !(!hadPeriod && rhs.Expression instanceof Parens)
                    && !(c == Lambda.SYMBOL && rhs.Expression instanceof Lambda)
                    && !(c != Lambda.SYMBOL && rhs.Expression instanceof PropositionalBinder))
                    rhs = parseInfixExpression(expression, start, context, "an expression in the scope of the lambda operator", rhs);
                
                Binder bin;
                switch (c) {
                    case ForAll.SYMBOL: bin = new ForAll((Identifier)var.Expression, rhs.Expression, hadPeriod); break;
                    case Exists.SYMBOL: bin = new Exists((Identifier)var.Expression, rhs.Expression, hadPeriod); break;
                    case Lambda.SYMBOL: bin = new Lambda((Identifier)var.Expression, rhs.Expression, hadPeriod); break;
                    default:
                        throw new RuntimeException(); // unreachable
                }
                return new ParseResult(bin, rhs.Next);
            
            default:
                // Hope that it's an identifier. If not, a BadCharacterException is thrown.
                return parseIdentifier(expression, start, context, "an expression", false, true);
        }
    }
    
    /***
     * Parses an identifier at position start in expression.
     */
    private static ParseResult parseIdentifier(String expression, int start, ParseOptions context, String whatIsExpected, boolean lookingForVariable, boolean allowPredicate) throws SyntaxException {
        start = skipWhitespace(expression, start, whatIsExpected, false);
        char c = expression.charAt(start);
            
        if (!isLetter(c)) {
            if (lookingForVariable)
                throw new BadCharacterException("I'm expecting a variable here, but variables must start with a letter.", start);
            else
                throw new BadCharacterException("I'm expecting an expression here, but '" + c + "' can't be the beginning of an expression.", start);
        }
    
        // Read in the identifier until the first non-letter-or-number
        // If we're doing single letter identifiers, then stop before
        // the next letter too.
        String id = String.valueOf(c);
        start++;
        while (start < expression.length()) {
            char ic = getChar(expression, start, context);
            if (!isIdentifierChar(ic) || (context.SingleLetterIdentifiers && isLetter(ic)))
                break;
            id += expression.charAt(start++);
        }

        if (start == expression.length() || !allowPredicate) {
            Identifier ident = loadIdentifier(id, context, start, null);
            return new ParseResult(ident, start);
        }

        // If parens, or another identifier, follow immediately, it is a predicate.
        // We parse such predicates here. If neither of those conditions holds, then
        // we return the identifier we found.
        if (!(
                getChar(expression, start, context) == '('
                || (context.SingleLetterIdentifiers && isLetter(getChar(expression, start, context))))) {
            Identifier ident = loadIdentifier(id, context, start, null);
            return new ParseResult(ident, start);
        }

        boolean parens = false;
        if (getChar(expression, start, context) == '(') {
            start++;
            parens = true;
        }

        ArrayList arguments = new ArrayList();
        boolean first = true;
        while (true) {
            if (parens) {
                // skip whitespace and look for close parens
                start = skipWhitespace(expression, start, 
                        first ? "a comma, expression, or close parenthesis"
                           : "a comma or close parenthesis", false);
                if (getChar(expression, start, context) == ')') { start++; break; }
            } else {
                if (start == expression.length())
                    break;
                if (!isLetter(getChar(expression, start, context)))
                    break;
            }

            if (parens) {
                if (!first) {
                    // With parentheses, we need commas between the arguments.
                    if (getChar(expression, start, context) != ',')
                        throw new SyntaxException("A comma must be used to separate arguments to a predicate.", start);
                    start++;
                }
                first = false;

                ParseResult arg = parseExpression(expression, start, context, "the next argument to the predicate " + id);
                arguments.add(arg.Expression);
                start = arg.Next;
            } else {
                char cc = getChar(expression, start, context);
                if (!isLetter(cc))
                    throw new SyntaxException("Invalid identifier as an argument to " + id + ".", start);
                arguments.add(loadIdentifier(String.valueOf(cc), context, start, null));
                start++;
            }
        }
        
        if (arguments.size() == 0) // we treat "P()" as "P"
            return new ParseResult(loadIdentifier(id, context, start, null), start);

        // If the type of the identifier is not known to the IdentifierTyper,
        // we'll infer its type from the types of the arguments, and assume
        // it is a constant and a function that yields a truth value.
        Type inferType = null;
        try {
            if (arguments.size() == 1) {
                inferType = new CompositeType(((Expr)arguments.get(0)).getType(), Type.T);
            } else {
                Type[] argtypes = new Type[arguments.size()];
                for (int i = 0; i < arguments.size(); i++)
                    argtypes[i] = ((Expr)arguments.get(i)).getType();
                inferType = new CompositeType(new ProductType(argtypes), Type.T);
            }
        } catch (TypeEvaluationException e) {
        }

        Identifier ident = loadIdentifier(id, context, start, inferType);

        if (arguments.size() == 1) // P(a) : a is an identifier; there is no ArgList
            return new ParseResult(new FunApp(ident, (Expr)arguments.get(0)), start);
        else // P(a,b) : (a,b) is an ArgList
            return new ParseResult(new FunApp(ident, new ArgList((Expr[])arguments.toArray(new Expr[0]))), start);
    }
    
    public static boolean isIdentifierChar(char ic) {
        return isLetter(ic) || Character.isDigit(ic)
                || ic == '\''
                || ic == '`' // alternate prime character
                || ic == '"' // as if double prime
                || ic == '_'
                || ic == Identifier.PRIME;
    }
    
    private static Identifier loadIdentifier(String id, ParseOptions context, int start, Type inferType) throws SyntaxException {
        boolean isvar;
        Type type;
        try {
            isvar = context.Typer.isVariable(id);
            type = context.Typer.getType(id);
        } catch (IdentifierTypeUnknownException e) {
            if (inferType == null)
                throw new SyntaxException(e.getMessage(), start);
            
            isvar = false;
            type = inferType;
        }

        Identifier ident;
        if (isvar)
            ident = new Var(id, type);
        else
            ident = new Const(id, type);
        
        return ident;
    }
    
    /***
     * Parse an infix expression at position start in expression.
     * These are a series of prefix expressions separated by
     * conjunction, disjunction, implication, and biconditional.
     * Operator precedence is taken care of here.
     */
    private static ParseResult parseInfixExpression(String expression, int start, ParseOptions context, String whatIsExpected, ParseResult left) throws SyntaxException {
        // We allow our caller to do a parsePrefixExpression before calling us so it can
        // look ahead and see what type of expression is next. If it didn't do any look-ahead
        // it passes left as null and we fetch the first conjunct here.
        if (left == null)
            left = parsePrefixExpression(expression, start, context, whatIsExpected);
        
        ArrayList operators = new ArrayList();
        ArrayList operands = new ArrayList();
        
        operands.add(left.Expression);
        
        start = left.Next;
        
        while (true) {
            int start2 = skipWhitespace(expression, start, null, true);
            if (start2 == -1) break;
            start = start2;

            char c = getChar(expression, start, context);
            
            if (context.ASCII) {
                char cnext = (start+1 < expression.length()) ? expression.charAt(start+1) : (char)0;
                char cnextnext = (start+2 < expression.length()) ? expression.charAt(start+2) : (char)0;
                if (c == '&')
                    c = And.SYMBOL;
                else if (c == '|')
                    c = Or.SYMBOL;
                else if (c == '-' && cnext == '>')
                    { c = If.SYMBOL; start++; }
                else if (c == '<' && cnext == '-' && cnextnext == '>')
                    { c = Iff.SYMBOL; start+=2; }
            }
            
            // If the next operator is and, or, if, or iff, then parse the succeeding
            // expression and return a binary expression.
            if (!(c == And.SYMBOL || c == Or.SYMBOL || c == If.SYMBOL || c == Iff.SYMBOL))
                break;
            
            ParseResult right = parsePrefixExpression(expression, start+1, context, "another expression after the connective");
            
            operators.add(String.valueOf(c));
            operands.add(right.Expression);
            
            start = right.Next;
        }
        
        if (operands.size() == 1) return left;
        
        // group operands by operator precedence
        
        groupOperands(operands, operators, If.SYMBOL);
        groupOperands(operands, operators, Iff.SYMBOL);
        groupOperands(operands, operators, And.SYMBOL);
        groupOperands(operands, operators, Or.SYMBOL);
        
        return new ParseResult((Expr)operands.get(0), start);
    }
    
    private static void groupOperands(ArrayList operands, ArrayList operators, char op) throws SyntaxException {
        for (int i = 0; i+1 < operands.size(); i++) {
            while (i+1 < operands.size() && ((String)operators.get(i)).charAt(0) == op) {
                Expr left = (Expr)operands.get(i);
                Expr right = (Expr)operands.get(i+1);
                
                if ((op == If.SYMBOL || op == Iff.SYMBOL) && (left instanceof If || left instanceof Iff))
                    throw new SyntaxException("Your expression is ambiguous because it has adjacent conditionals without parenthesis.", -1);
                
                Expr binary;
                switch (op) {
                    case And.SYMBOL: binary = new And(left, right); break;
                    case Or.SYMBOL: binary = new Or(left, right); break;
                    case If.SYMBOL: binary = new If(left, right); break;
                    case Iff.SYMBOL: binary = new Iff(left, right); break;
                    default: throw new RuntimeException(); // unreachable
                }
                operands.set(i, binary);
                operators.remove(i);
                operands.remove(i+1);
            }
        }
    }

    /***
     * Parses all sorts of expressions starting at position start in expression.
     * It looks first for an infix/prefix expression (anything but function application),
     * and if that's followed by a space and then an identifier or parenthetical expression,
     * take it as an argument to a function application expression.
     */
    private static ParseResult parseFunctionApplicationExpression(String expression, int start, ParseOptions context, String whatIsExpected) throws SyntaxException {
        ParseResult left = parseInfixExpression(expression, start, context, whatIsExpected, null);
        start = left.Next;
        
        Expr expr = left.Expression;
        
        while (true) {
            int start2 = skipWhitespace(expression, start, null, true);
            if (start2 == -1) { break; }
            start = start2;

            char c = getChar(expression, start, context);

            // If we find the next thing is a letter, parenthesis, or bracket,
            // then this is function application.
            if (isLetter(c) || c == '(' || c == '[') {
                ParseResult right = parsePrefixExpression(expression, start, context, "a variable or parenthesized expression"); // message never used
                expr = new FunApp(expr, right.Expression); // left associativity
                start = right.Next;
            } else {
                break;
            }
        }
        
        return new ParseResult(expr, start);
    }

    private static ParseResult parseExpression(String expression, int start, ParseOptions context, String whatIsExpected) throws SyntaxException {
        return parseFunctionApplicationExpression(expression, start, context, whatIsExpected);
    }
}
