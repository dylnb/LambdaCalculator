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
        public boolean singleLetterIdentifiers = false;
        
        /**
         * This turns on ASCII mode.  These symbols become special:
         * A, E, L, ~, &, |, ->, <->
         * for
         * for all, exists, lambda, not, and, or, if, iff
         */
        public boolean ASCII = false;

        /**
         * This provides the types for identifiers encountered
         * in the expression.  Every identifier must be
         * identifiable as a constant or variable and with a type.
         * By default, this field contains an IdentifierTyper
         * with a default setup.
         */
        public IdentifierTyper typer = IdentifierTyper.createDefault();
        
        /**
         * This map is populated during the course of parsing an
         * expression and lists some of the types assigned explicitly
         * in the expression. One type is remembered per name, so
         * if a single name is used with different types in multiple
         * places (i.e. bound variables with different scopes), only
         * the last one parsed will be listed in the Map.
         */
        public Map explicitTypes = new HashMap();
        
        public ParseOptions() {
            
        }
        
        public ParseOptions(boolean singleLetterIdentifiers,
                boolean ASCII,
                IdentifierTyper typer) {
            this.singleLetterIdentifiers = singleLetterIdentifiers;
            this.ASCII = ASCII;
            this.typer = typer;
        }
        
        public ParseOptions(boolean singleLetterIdentifiers,
                boolean ASCII) {
            this.singleLetterIdentifiers = singleLetterIdentifiers;
            this.ASCII = ASCII;
        }
        
        ParseOptions cloneContext() {
             ParseOptions ret = new ParseOptions();
             ret.singleLetterIdentifiers = singleLetterIdentifiers;
             ret.ASCII = ASCII;
             ret.typer = typer.cloneTyper();
             ret.explicitTypes = explicitTypes;
             return ret;
         }
     }
    
    private static class ParseResult {
        public Expr Expression;
        public int Next;
        
        /**
         * 
         * @param expression 
         * @param next 
         */
        public ParseResult(Expr expression, int next) {
            Expression = expression;
            Next = next;
        }
    }
    
    /**
     * Private constructor. All the methods in this class are static.
     */
    private ExpressionParser() {
    }
    
    
    public static Expr parseAndSuppressExceptions
            (String expression, ParseOptions options) {
        Expr result = null;
        try {
            result = parse(expression, options);
        } catch (SyntaxException s) {
            //s.printStackTrace();
        }
        return result;
    }
    
    /**
     * Parses an expression with the given options. This is the entry point of this class.
     *
     * @param expression the expression to be parsed
     * @param options global options for parsing, e.g. typing conventions
     * @return an Expr object representing the expression
     * @throws SyntaxException if a parse error occurs
     *
     */
    public static Expr parse(String expression, ParseOptions options) throws SyntaxException {
        options.explicitTypes.clear();
        
        if (expression.length() == 0)
            throw new SyntaxException("Enter a lambda expression.", -1);
        
        ParseResult r = parseExpression(expression, 0, options, "an expression");
        if (r.Next != expression.length())
            throw new SyntaxException("\"" + expression.substring(r.Next) + "\" doesn't look like a lambda expression.", r.Next);
        
        return r.Expression;
    }
    
    /**
     * Skips any whitespace.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for an identifier
     * @param expected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @param allowEOS if true, returns -1 if it hits the end of the string. Otherwise,
     * an exception is raised in that case.
     * @return a position in the string after any whitespace.
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static int skipWhitespace(String expression, int start, String expected, boolean allowEOS) throws SyntaxException {
        while (true) {
            if (start == expression.length()) {
                if (allowEOS) return -1;
                throw new SyntaxException("You seem to be missing " + expected + " at the end of your expression.", start);
            }
            if (expression.charAt(start) != ' ')
                break;
            start++;
        }
        return start;
    }
    
    /**
     * Tests if a character is a letter. Unlike Java, this method does *not* 
     * treat the lambda symbol as a letter.
     * @param c the character to be tested
     * @return whether it is a letter
     */
    private static boolean isLetter(char c) {
        return Character.isLetter(c) && c != Lambda.SYMBOL && c != Identifier.PRIME;
    }
    
    /**
     * Gets a character in the string at the indicated position, but maps
     * certain capital letters into special symbols if the ASCII parsing option
     * is turned on (like A to the for-all symbol, etc.).
     * @param expression the expression string
     * @param index the index in the expression string to get the character at
     * @param context global parsing options
     * @return the letter, mapped to a special character if necessary
     */
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
                case 'I': c = Iota.SYMBOL; break;
            }
        }
        return c;
    }
    
    /**
     * 
     * Parses the prefix expression beginning at position start in expression.
     * Prefix expressions are expressions whose first character permits the 
     * parser to recognize them. This includes parenthesis expressions, 
     * negation expressions, binding
     * expressions, and predicates (which include identifiers). 
     * If none of these are present
     * at position start, a BadCharacterException is thrown.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for a prefix expression
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @return a prefix expression
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
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
                //break
                
            case Not.SYMBOL:
                ParseResult negr = parsePrefixExpression(expression, start+1, context, "an expression after the negation operator");
                // By parsing a prefix expression as opposed to just any expression here,
                // we achieve the effect that negation binds more strongly than any other infix operator, 
                // and more strongly than function application.
                // E.g. ~A & B is parsed as [~A] & B                
                
                return new ParseResult(new Not(negr.Expression), negr.Next);
                //break
                
            case ForAll.SYMBOL: //fall through
            case Exists.SYMBOL: //fall through
            case Lambda.SYMBOL: //fall through
            case Iota.SYMBOL:
                ParseResult var = parseIdentifier(expression, start+1, context, "a variable");
                if (!(var.Expression instanceof Identifier))
                    throw new SyntaxException("After a binder, an identifier must come next: " + var.Expression + ".", start+1);
                start = var.Next;

                start = skipWhitespace(expression, start, "a period, open bracket, or another binder", false);
                
                boolean hadPeriod = false;
                if (getChar(expression, start, context) == '.') {
                    start++;
                    hadPeriod = true;
                }
                
                // Remember the type of the variable, since it might have been given explicitly,
                // so that when we encounter it within our scope, we can give it the same type.
                // Cloning the context clones the IdentifierTyper, so we can modify it
                // in context2 and it will be unchanged when we pop out of this scope.
                ParseOptions context2 = context.cloneContext();
                Identifier varid = (Identifier)var.Expression;
                context2.typer.addEntry(varid.getSymbol(), varid instanceof Var, varid.getType());
                
                // The inside of a binder can be one of two things:
                //   a bracketed expression (Parens)
                //   another binder (and inside that eventually (recursively) we'll get our brackets)
                // This issue is that without an explicit scope marker at the end of the scope
                // of the binder, it's impossible to know whether infix operators take wide
                // or narrow scope, as in:  Ax.P(x) & Q(x).  This is ambiguous since we allow
                // variables to be free. It could be: [Ax.P(x)] & Q(x). While we could just
                // parse binders allowing for only prefix (i.e. not infix) expressions in
                // their scope, for pedagogical reasons we want to enforce the use of clear
                // syntax. Otherwise we would parse as given above, but the user probably will
                // not think that that is what we did. So, we require brackets after binders,
                // which makes the scope clear, except when a binder is followed by another binder,
                // which is also permitted since it just pushes the brackets requirement down
                // into the inner expression.
                ParseResult inside = parsePrefixExpression(expression, start, context2, "the expression in the scope of the operator");
                
                if (!(inside.Expression instanceof Parens)
                      && !(inside.Expression instanceof Binder)) 
                    throw new SyntaxException("After a binder, either a bracket or another binder must come next.", start+1);
                 
                Binder bin;
                switch (c) {
                    case ForAll.SYMBOL: bin = new ForAll((Identifier)var.Expression, inside.Expression, hadPeriod); break;
                    case Exists.SYMBOL: bin = new Exists((Identifier)var.Expression, inside.Expression, hadPeriod); break;
                    case Lambda.SYMBOL: bin = new Lambda((Identifier)var.Expression, inside.Expression, hadPeriod); break;
                    case Iota.SYMBOL: bin = new Iota((Identifier)var.Expression, inside.Expression, hadPeriod); break;
                    default:
                        throw new RuntimeException(); // unreachable
                }
                return new ParseResult(bin, inside.Next);
                //break
                
            default:
                // Hope that it's an identifier or predicate. If not, a BadCharacterException is thrown.
                return parsePredicate(expression, start, context, "an expression", false);
        }
    }
    
    /**
     * Parses an identifier at position start in expression.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for an identifier
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @return an identifier
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static ParseResult parseIdentifier(String expression, int start,
            ParseOptions context, String whatIsExpected) throws SyntaxException {
        return parsePredicate(expression, start, context,
                whatIsExpected, true);
        
    }
    
    /**
     * Parses a predicate at position start in expression.
     * A predicate is an identifier followed by
     * an argument list. If no argument list follows, as a fallback we try to parse 
     * an identifier by itself.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for an identifier
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @param isRightAfterBinder whether we're looking for the variable immediately after a binder, in which
     * case: 1) predicates are not allowed, so we stop reading immediately after the identifier, 2) if
     * a type on the identifier is specified (i.e. x:e), then we know to load the identifier as a variable,
     * and 3) the error message reflects that we're looking for a variable.
     * @return a predicate
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static ParseResult parsePredicate(String expression, int start, ParseOptions context, String whatIsExpected, boolean isRightAfterBinder) throws SyntaxException {
        start = skipWhitespace(expression, start, whatIsExpected, false);
        char c = expression.charAt(start);
        
        // 0 and 1 are parsed as constants of type t.
        if (c == '0' || c == '1') {
            start++;
            return new ParseResult(new Const(String.valueOf(c), Type.T, false), start);
        }
            
        if (!isLetter(c)) {
            if (isRightAfterBinder)
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
            if (!isIdentifierChar(ic) || (context.singleLetterIdentifiers && isLetter(ic)))
                break;
            id += expression.charAt(start++);
        }
        
        // If a colon follows the name of the identifier, then the identifier's
        // type follows the colon.
        Type specifiedType = null;
        if (start < expression.length() && getChar(expression, start, context) == ':') {
            start++;
            TypeParser.ParseResult tr = TypeParser.parseType(expression, start, true);
            start = tr.end + 1;
            specifiedType = tr.result;
        }

        if (start == expression.length() || isRightAfterBinder) {
            Identifier ident = loadIdentifier(id, context, start, null, specifiedType, isRightAfterBinder);
            return new ParseResult(ident, start);
        }

        // If parens, or another identifier, follow immediately, it is a predicate.
        // We parse such predicates here. If neither of those conditions holds, then
        // we return the identifier we found.
        if (!(
                getChar(expression, start, context) == '('
                || (context.singleLetterIdentifiers && isLetter(getChar(expression, start, context))))) {
            Identifier ident = loadIdentifier(id, context, start, null, specifiedType, isRightAfterBinder);
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
                arguments.add(loadIdentifier(String.valueOf(cc), context, start, null, null, false));
                start++;
            }
        }
        
        if (arguments.size() == 0) // we treat "P()" as "P"
            return new ParseResult(loadIdentifier(id, context, start, null, specifiedType, false), start);

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

        Identifier ident = loadIdentifier(id, context, start, inferType, specifiedType, false);

        if (arguments.size() == 1) // P(a) : a is an identifier; there is no ArgList
            return new ParseResult(new FunApp(ident, (Expr)arguments.get(0)), start);
        else // P(a,b) : (a,b) is an ArgList
            return new ParseResult(new FunApp(ident, new ArgList((Expr[])arguments.toArray(new Expr[0]))), start);
    }
    
    
    // TODO write a formal semantic paper about the difference between
    // "whether the character is used in identifiers" and
    // "whether the character is one that is used in identifiers"
    /**
     * Returns whether the character is one that is used in identifiers, which
     * includes letters (which must be the start of an identifier), numbers,
     * underscores, and primes of various sorts.
     * @param ic the character
     * @return whether the character can be used in an identifier
     */
    public static boolean isIdentifierChar(char ic) {
        return isLetter(ic) || Character.isDigit(ic)
                || ic == '\''
                || ic == '`' // alternate prime character
                || ic == '"' // as if double prime
                || ic == '_'
                || ic == Identifier.PRIME;
    }
    
    /**
     * Creates an instance of Const or Var for an identifier named by id,
     * using the typing conventions of the global parser options
     * @param id the name of the identifier
     * @param context global parsing options
     * @param start the start position of the identifier, used for error messages
     * @param inferType if null and the typing conventions cannot provide a type
     * for the identifier, an exception is thrown; otherwise, if the typing conventions
     * cannot provide a type for the identifier, inferType is used
     * @throws lambdacalc.logic.SyntaxException if a parsing error occurs
     * @return the new Identifier instance
     */
    private static Identifier loadIdentifier(String id, ParseOptions context, int start, Type inferredType, Type specifiedType, boolean isRightAfterBinder) throws SyntaxException {
        boolean isvar;
        Type type;
        
        if (specifiedType == null) {
            try {
                isvar = context.typer.isVariable(id);
                type = context.typer.getType(id);
            } catch (IdentifierTypeUnknownException e) {
                if (inferredType == null)
                    throw new SyntaxException(e.getMessage(), start);
            
                isvar = false;
                type = inferredType;
            }
        } else {
            type = specifiedType;
            isvar = isRightAfterBinder;
        }

        Identifier ident;
        if (isvar)
            ident = new Var(id, type, specifiedType != null);
        else
            ident = new Const(id, type, specifiedType != null);
        
        if (specifiedType != null)
            context.explicitTypes.put(id, ident); // ident because it stores both type and isVar

        return ident;
    }
    
    /***
     * Parse an infix expression at position start in expression.
     * An infix expression in the sense of this method is a series of 
     * prefix expressions in the sense of parsePrefixExpression separated by
     * conjunction, disjunction, implication, and biconditional.
     * If we only find one such prefix expression, we return the result of 
     * parsePrefixExpression as a fallback.
     * This includes the case where we have more than two prefix expressions.
     * Such an expression, e.g. X & Y | Z -> Q, is not parsed recursively,
     * but rather as a simple list of expressions separated by operators
     * as in [X, &, Y, |, Z, -> , Q]. Then the operator precedence is taken
     * care of. The operator precendence is:
     *   Not Equal -- binds strongest
     *   Equal
     *   If
     *   Iff
     *   And
     *   Or -- binds weakest
     *
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for an infix expression
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @param firstConjunct if null, ignored; otherwise, this is the first conjunct (scanning
     * the first conjunct is skipped); this is used for look-ahead in parsing quantifiers
     * @return an infix expression or something lesser as fallback
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static ParseResult parseInfixExpression(String expression, int start, ParseOptions context, String whatIsExpected, ParseResult firstConjunct) throws SyntaxException {
        // We allow our caller to do a parsePrefixExpression before calling us so it can
        // look ahead and see what type of expression is next. If it didn't do any look-ahead
        // it passes left as null and we fetch the first conjunct here.
        if (firstConjunct == null)
            firstConjunct = parsePrefixExpression(expression, start, context, whatIsExpected);
        
        ArrayList operators = new ArrayList();
        ArrayList operands = new ArrayList();
        
        operands.add(firstConjunct.Expression);
        
        start = firstConjunct.Next;
        
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
                
                // TODO: synchronize this with LambdaEnabledTextField
                
                else if (c == '|')
                    c = Or.SYMBOL;
                
                else if (c == '-' && cnext == '>')
                    { c = If.SYMBOL; start++; }
                
                else if (c == '<' && cnext == '-' && cnextnext == '>')
                    { c = Iff.SYMBOL; start+=2; }
                
                else if (c == '!' && cnext == '=')
                    { c = Equality.NEQ_SYMBOL; start++; }
            }
            
            // If the next operator is and, or, if, or iff, then parse the succeeding
            // expression and return a binary expression.
            if (!(c == And.SYMBOL || c == Or.SYMBOL || c == If.SYMBOL || c == Iff.SYMBOL || c == Equality.EQ_SYMBOL || c == Equality.NEQ_SYMBOL))
                break;
            
            ParseResult right = parsePrefixExpression(expression, start+1, context, "another expression after the connective");
            
            operators.add(String.valueOf(c));
            operands.add(right.Expression);
            
            start = right.Next;
        }
        
        if (operands.size() == 1) return firstConjunct;
        
        // group operands by operator precedence, tighter operators first
        
        groupOperands(operands, operators, Equality.NEQ_SYMBOL);
        groupOperands(operands, operators, Equality.EQ_SYMBOL);
        groupOperands(operands, operators, If.SYMBOL);
        groupOperands(operands, operators, Iff.SYMBOL);
        groupOperands(operands, operators, And.SYMBOL);
        groupOperands(operands, operators, Or.SYMBOL);
        
        return new ParseResult((Expr)operands.get(0), start);
    }
    
    /**
     * 
     * @param operands 
     * @param operators 
     * @param op 
     * @throws lambdacalc.logic.SyntaxException 
     */
    private static void groupOperands(ArrayList operands, ArrayList operators, char op) throws SyntaxException {
        for (int i = 0; i+1 < operands.size(); i++) {
            while (i+1 < operands.size() && ((String)operators.get(i)).charAt(0) == op) {
                Expr left = (Expr)operands.get(i);
                Expr right = (Expr)operands.get(i+1);
                
                if ((op == If.SYMBOL || op == Iff.SYMBOL) && (left instanceof If || left instanceof Iff))
                    throw new SyntaxException("Your expression is ambiguous because it has adjacent conditionals without parenthesis.", -1);
                
                if ((op == Equality.EQ_SYMBOL || op == Equality.NEQ_SYMBOL) && (left instanceof Equality))
                    throw new SyntaxException("Your expression is ambiguous because it has adjacent equality operators without parenthesis.", -1);
                
                Expr binary;
                switch (op) {
                    case And.SYMBOL: binary = new And(left, right); break;
                    case Or.SYMBOL: binary = new Or(left, right); break;
                    case If.SYMBOL: binary = new If(left, right); break;
                    case Iff.SYMBOL: binary = new Iff(left, right); break;
                    case Equality.EQ_SYMBOL: binary = new Equality(left, right, true); break;
                    case Equality.NEQ_SYMBOL: binary = new Equality(left, right, false); break;
                    default: throw new RuntimeException(); // unreachable
                }
                operands.set(i, binary);
                operators.remove(i);
                operands.remove(i+1);
            }
        }
    }

    /**
     * Parses all sorts of expressions starting at position start in expression.
     * It looks first for an infix/prefix expression (anything returned by
     * parseInfixExpression), and if that's followed by an identifier or parenthetical expression,
     * take it as an argument to a function application expression.
     * Parses an expression at position start in expression.
     * We first try to parse a function application because it is the operator with the
     * lowest precedence. This method will fall back appropriately
     * if no function application is present.
     * A function application in the sense of this method consists of a list of expressions E1, ..., En, with n>=1.
     * If n = 1 then we fallback and call parseInfixExpression on E1. (Infix operators
     * bind least strongly except for function application.)
     * (If n > 2 then we have a series of function applications, which we associate left-to-right.)
     * We always parse E1 using parseInfixExpression and any Ei for i>=1 using parsePrefixExpression.
     * The reason for using the less inclusive method parsePrefixExpression on Ei i>1 is that
     * we don't allow any of those Ei to be infix expressions. E.g. we disallow
     * Lx. ~x a & b and require the user to disambiguate thus: Lx. ~x (a & b)
     * In the former formula, a & b is an infix expression. In the latter one, (a & b) is a prefix expression.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for a function application
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @return a function application expression or something lesser as fallback
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static ParseResult parseFunctionApplicationExpression(String expression, int start, ParseOptions context, String whatIsExpected) throws SyntaxException {
        ParseResult left = parseInfixExpression(expression, start, context, whatIsExpected, null);
        start = left.Next;
        
        Expr expr = left.Expression;
        
        while (true) {
            // Skip any white space after the first infix expression we parse
            // (following the left hand side). If we hit the end of the string
            // while skipping white space, skipWhitespace returns -1, and we
            // just break out.
            int start2 = skipWhitespace(expression, start, null, true);
            if (start2 == -1) { break; }
            start = start2;

            char c = getChar(expression, start, context);

            // We have to know when to terminate parsing a function application
            // otherwise when parsing an embedded function application, after the
            // function application is actually finished, we'll try doing a
            // parsePrefixExpression below, and it'll throw an exception because
            // what follows isn't a prefix expression. Example:
            //    [Lx.P(x) (a)] & P(a)
            // What follows is a close bracket, not a prefix expression, so we
            // better not parse a prefix expression.
            // Except at top-level scope, this method is only called when parsing
            // the inside of a parenthesis expression or arguments to a predicate,
            // so we know that we must be done just when we encounter a close bracket
            // or a comma. (It can't match up
            // with an open bracket *within* the function application because
            // those have already been parsed. Thus, it must correspond to the
            // parenthesis outside.)
            if (c == ')' || c == ']' || c == ',')
                break;
            
            ParseResult right = parsePrefixExpression(expression, start, context, "a variable or parenthesized expression"); // message never used
            expr = new FunApp(expr, right.Expression); // left associativity
            start = right.Next;
        }
        
        return new ParseResult(expr, start);
    }

    /**
     * Parses an expression at position start in expression.
     * We first try to parse a function application because it is the operator with the
     * lowest precedence. The method parseFunctionApplication will fall back appropriately
     * if no function application is present.
     * @param expression the text string being parsed
     * @param start the position at which to start scanning for an expression
     * @param context global options for parsing
     * @param whatIsExpected a string describing what kind of expression is expected to occur at this position,
     * for error messages
     * @return an expression
     * @throws lambdacalc.logic.SyntaxException if there is a parse error
     */
    private static ParseResult parseExpression(String expression, int start, ParseOptions context, String whatIsExpected) throws SyntaxException {
        return parseFunctionApplicationExpression(expression, start, context, whatIsExpected);
    }
}
