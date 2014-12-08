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
 * ExpressionParser.java
 *
 * Created on May 29, 2006, 3:02 PM
 */
package lambdacalc.logic;

import java.util.*;

/**
 * Parses a string into an Expr object.
 *
 * This is a simple recursive descent parser augmented with the possibility for
 * the detection of ambiguous strings. For each of the main functions,
 * parseFunctionApplication, parseInfixExpression, and parsePrefixExpression,
 * the caller is asking the callee to find every legitimate instance of function
 * application, an infix expression, or a prefix expression starting at the
 * indicated position, but ending anywhere. So, for infix expressions like A & B
 * & C, the callee returns A, A & B, and A & B & C. Likewise, for function
 * applications like P a b c, the callee returns P (the predicate alone), P a,
 * (P a) b, and ((P a) b) c. This allows for the scopes of infix operators to
 * pop up. For instance, Ax.P(x) & Q(x), two parsing paths are taken.
 * parseInfixExpression calls parseFunctionApplication which calls
 * parsePrefixExpression, which sees the binder and calls parseInfixExpression
 * to get its inner expression. parseInfixExpression returns a set comprising
 * the parses P(x) and P(x) & Q(x). parsePrefixExpression wraps each possibility
 * with the binder, so it returns the set: Ax.P(x) and Ax.P(x) & Q(x). Above
 * that, parseFunctionApplicationException scans to the right of each
 * possibility looking for an argument, but it finds none so it just passes up
 * those two possibilities. parseInfixExpression looks to the right of each
 * possibility for a connective. In the first case, it finds the ampersand, so
 * it creates a conjunction yielding [Ax.P(x)] & Q(x). In the second case, it
 * hits the end of the string and just returns Ax.P(x) & Q(x) unchanged.
 *
 * Each such call can return either 1) a set of one or more possible parses, or
 * 2) an error condition (SyntaxException) indicating why nothing could be
 * parsed. Further, each possible parse can be tagged with why the parse didn't
 * go any further than it did (HowToContinue, a SyntaxException). For instance,
 * in "[Lx.P(x)] ^", parseFunctionApplication will try to parse the caret as an
 * expression, but will fail, so it will return as much as it could parse, which
 * is "[Lx.P(x)]", but tagged with the SyntaxException that occurred parsing the
 * next bit. This is used as the message for the user when the remainder of the
 * string couldn't be parsed.
 */
public class ExpressionParser {

  /**
   * Testing routine. It prints out the various potential parses and the at the
   * end the actually accepted parse, or the error message that would be thrown
   * on failure.
   */
  public static void main(String[] args) {
    ParseOptions opts = new ParseOptions();
    opts.ASCII = true;

    ParseResultSet rs = parseExpression(args[0], 0, opts, "an expression");
    if (rs.Exception != null) {
      rs.Exception.printStackTrace();
      return;
    }
    for (int i = 0; i < rs.Parses.size(); i++) {
      ParseResult r = (ParseResult) rs.Parses.get(i);
      System.out.print(r.Next + "   " + r.Expression.toString());
      String type = "(type mismatch)";
      try {
        type = r.Expression.getType().toString();
      } catch (TypeEvaluationException tee) {
      }
      System.out.print(" " + type);
      if (r.HowToContinue != null) {
        System.out.print("  " + r.HowToContinue.getMessage());
      }
      System.out.println();
    }

    System.out.println();

    try {
      System.out.println(parse(args[0], opts));
    } catch (SyntaxException se) {
      System.err.println(se.getMessage());
    }
  }

  /**
   * Options for parsing expressions.
   */
  public static class ParseOptions {

    /**
     * This turns on single-letter-identifier mode, so that Pa is interpreted as
     * P(a), rather than as a single identifier.
     */
    public boolean singleLetterIdentifiers = false;

    /**
     * This turns on ASCII mode. These symbols become special: A, E, L, ~, &, |,
     * ->, <->
     * for for all, exists, lambda, not, and, or, if, iff
     */
    public boolean ASCII = false;

    /**
     * This provides the types for identifiers encountered in the expression.
     * Every identifier must be identifiable as a constant or variable and with
     * a type. By default, this field contains an IdentifierTyper with a default
     * setup.
     */
    public IdentifierTyper typer = IdentifierTyper.createDefault();

    /**
     * This map is populated during the course of parsing an expression and
     * lists some of the types assigned explicitly in the expression. One type
     * is remembered per name, so if a single name is used with different types
     * in different places, only the last one parsed will be listed in the Map.
     */
    public Map explicitTypes = new HashMap();

    public boolean hasExplicitTypes() {
      return !explicitTypes.isEmpty();
    }

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

  /**
   * This class represents the set of possible parses that could be read at a
   * given position in the string, taking into account that some strings are
   * legitimately ambiguous, to be resolved at the end. This class can also
   * represent a fatal error condition when no parses are possible. So, either
   * Exception is set at Parses is null, or else Parses is a vector of one or
   * more elements, and Exception is null.
   */
  private static class ParseResultSet {

    public final SyntaxException Exception; // if set, a fatal error, no parses available
    public final Vector Parses; // if set, one or more possible parses

    public ParseResultSet(SyntaxException ex) {
      Exception = ex;
      Parses = null;
    }

    public ParseResultSet(Vector parses) {
      Exception = null;
      Parses = parses;
    }

    public ParseResultSet(ParseResult singletonParse) {
      Exception = null;
      Parses = new Vector();
      Parses.add(singletonParse);
    }
  }

  /**
   * This class represents a single potential parse of a string at a given
   * location. Expression contains the parsed substring. Next is the next
   * character position after the substring read. HowToContinue is an
   * informative error message about why the parser stopped when it did. It is
   * normally used when we were able to parse a substring as something, but when
   * it is the best we can parse of the user input yet it is not the whole
   * expression given.
   */
  private static class ParseResult {

    public final Expr Expression; // parsed subexpression
    public final int Next; // next character position after subexpression
    public final SyntaxException HowToContinue; // if set, why we stopped here

    public ParseResult(Expr expr, int next) {
      this(expr, next, null);
    }

    public ParseResult(Expr expr, int next, SyntaxException continuation) {
      Expression = expr;
      Next = next;
      HowToContinue = continuation;
    }
  }

  /**
   * Private constructor. All the methods in this class are static.
   */
  private ExpressionParser() {
  }

  public static Expr parseAndSuppressExceptions(String expression, ParseOptions options) {
    Expr result = null;
    try {
      result = parse(expression, options);
    } catch (SyntaxException s) {
      //s.printStackTrace();
    }
    return result;
  }

  /**
   * Parses an expression with the given options. This is the entry point of
   * this class.
   *
   * @param expression the expression to be parsed
   * @param options global options for parsing, e.g. typing conventions
   * @return an Expr object representing the expression
   * @throws SyntaxException if a parse error occurs
   *
   */
  public static Expr parse(String expression, ParseOptions options) throws SyntaxException {
    options.explicitTypes.clear();

    if (expression.trim().length() == 0) {
      throw new SyntaxException("Enter a lambda expression.", 0);
    }

    ParseResult r = parse2(expression, 0, options, "an expression", true);

    return r.Expression;
  }

  /**
   * This method starts off the main work of the class. It tries to parse an
   * expression and then resolve the ambiguities, according to several
   * constraints. If the expression could not be parsed at all, a
   * SyntaxException is thrown. To resolve ambiguities, first only the parses
   * that read the longest amount of the expression are considered. Then, if any
   * parse is well-typed, discard the non-well-typed parses. Last, take only the
   * parses that have the least number of free variables. If more than one parse
   * still results, a SyntaxException is thrown altering the user to the
   * ambiguities remaining.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for a prefix
   * expression
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @param readFully true iff a failure to read to the end of the string should
   * cause an exception to be thrown.
   * @return a single ParseResult for the best parse
   * @throws SyntaxException if no parse is possible
   */
  private static ParseResult parse2(
    String expression, int start, ParseOptions context, String whatIsExpected, boolean readFully
  ) throws SyntaxException {
    // Parse the string into a number of potential ambiguous parses.
    ParseResultSet rs = parseExpression(expression, start, context, whatIsExpected);

    // If a fatal parsing error ocurred such that no parses were available, just
    // throw the error.
    if (rs.Exception != null) {
      throw rs.Exception;
    }

    // Use ordered constraints to filter out parses.
    // The first constraint is to drop all parses that don't go as far into the
    // string as the most extensive parse.
    int maxParse = -1;
    for (int i = 0; i < rs.Parses.size(); i++) { // get maximum
      ParseResult r = (ParseResult) rs.Parses.get(i);
      if (r.Next > maxParse) {
        maxParse = r.Next;
      }
    }
    for (int i = 0; i < rs.Parses.size(); i++) { // filter out non-maximal parses
      ParseResult r = (ParseResult) rs.Parses.get(i);
      if (r.Next < maxParse) {
        rs.Parses.remove(i);
        i--;
      } // decrement i to repeat iteration at same index
    }

    // If readFully is true, we are wanting to read the entire string. In that case,
    // if we haven't gotten to the end of the string, raise an exception.
    // (When parsing arguments to predicates, this method is called but we don't
    // want to read to the end of the string.) We'll just take the first error message
    // we can find in a HowToContinue field, or throw a generic one if there aren't any
    // prepared messages.
    if (readFully && maxParse != expression.length() && skipWhitespace(
        expression, maxParse) != -1) {
      for (int i = 0; i < rs.Parses.size(); i++) { // filter out non-maximal parses
        ParseResult r = (ParseResult) rs.Parses.get(i);
        if (r.HowToContinue != null) {
          throw r.HowToContinue;
        }
      }
      throw new SyntaxException("\"" + expression.substring(maxParse)
                                + "\" doesn't look like a complete lambda expression.",
                                maxParse);
    }

    // Next, if any parse is well typed, then drop the non-well-typed parses.
    boolean hasWellTyped = false;
    for (int i = 0; i < rs.Parses.size(); i++) {
      ParseResult r = (ParseResult) rs.Parses.get(i);
      try {
        r.Expression.getType(); // return value is not important
        hasWellTyped = true; // not executed if type evaluation fails
      } catch (TypeEvaluationException tee) {
      }
    }
    if (hasWellTyped) {
      for (int i = 0; i < rs.Parses.size(); i++) { // filter out non-maximal parses
        ParseResult r = (ParseResult) rs.Parses.get(i);
        try {
          r.Expression.getType(); // return value is not important
        } catch (TypeEvaluationException tee) {
          rs.Parses.remove(i);
          i--; // decrement i to repeat iteration at same index
        }
      }
    }

    /*
     // Lastly, minimize the number of free variables by tossing out parses
     // that have more free variables than the parse with the fewest free variables.
     int minFreeVars = -1;
     for (int i = 0; i < rs.Parses.size(); i++) { // scan for the fewest number of free vars
     ParseResult r = (ParseResult)rs.Parses.get(i);
     int n = r.Expression.getFreeVars().size();
     if (minFreeVars == -1 || n < minFreeVars) minFreeVars = n;
     }
     for (int i = 0; i < rs.Parses.size(); i++) { // filter out non-minimal parses
     ParseResult r = (ParseResult)rs.Parses.get(i);
     int n = r.Expression.getFreeVars().size();
     if (n > minFreeVars) { rs.Parses.remove(i); i--; } // decrement i to repeat iteration at same index
     }
     */
    // If more than one parse remains, tell the user his expression is ambiguous.
    if (rs.Parses.size() > 1) {
      Vector alternatives = new Vector();
      for (int i = 0; i < rs.Parses.size(); i++) {
        ParseResult r = (ParseResult) rs.Parses.get(i);
        alternatives.add(r.Expression.toString());
      }
      throw new AmbiguousStringException(
          "Your expression is ambiguous between the following possibilities and should be corrected by adding parentheses",
          alternatives);
    }

    return (ParseResult) rs.Parses.get(0);
  }

  /**
   * Skips any whitespace.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for an identifier
   * @return a position in the string after any whitespace, or -1 if whitespace
   * goes to the end of the expression.
   */
  private static int skipWhitespace(String expression, int start) {
    while (true) {
      if (start == expression.length()) {
        return -1;
      }
      if (expression.charAt(start) != ' ') {
        break;
      }
      start++;
    }
    return start;
  }

  /**
   * Tests if a character is a letter. Unlike Java, this method does *not* treat
   * the lambda symbol as a letter.
   *
   * @param c the character to be tested
   * @return whether it is a letter
   */
  private static boolean isLetter(char c) {
    return Character.isLetter(c) && c != Lambda.SYMBOL && !isPrime(c);
  }

  /**
   * Gets a character in the string at the indicated position, but maps certain
   * capital letters into special symbols if the ASCII parsing option is turned
   * on (like A to the for-all symbol, etc.).
   *
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
        case Not.INPUT_SYMBOL:
          c = Not.SYMBOL;
          break;
        case ForAll.INPUT_SYMBOL:
          c = ForAll.SYMBOL;
          break;
        case Exists.INPUT_SYMBOL:
          c = Exists.SYMBOL;
          break;
        case Lambda.INPUT_SYMBOL:
          c = Lambda.SYMBOL;
          break;
        case Iota.INPUT_SYMBOL:
          c = Iota.SYMBOL;
          break;
      }
    }
    return c;
  }

  /**
   *
   * Parses the prefix expression beginning at position start in expression.
   * Prefix expressions are expressions whose first character permits the parser
   * to recognize them. This includes parenthesis expressions, negation
   * expressions, binding expressions, and predicates (which include
   * identifiers). If none of these are present a BadCharacterException is
   * returned.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for a prefix
   * expression
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @return all prefix expressions that could be parsed at this point
   */
  private static ParseResultSet parsePrefixExpression(
    String expression, int start, ParseOptions context, String whatIsExpected
  ) {
    System.out.println("parsePrefixExpression: " + start);
    start = skipWhitespace(expression, start);
    if (start == -1) {
      return new ParseResultSet(
        new SyntaxException(
          "You seem to be missing " + whatIsExpected + " at the end of your expression.",
          expression.length() - 1
        )
      );
    }
    char c = getChar(expression, start, context);

    switch (c) {
      case '(':
      case '[':
      case '|':
        String bracketname = null;
        char closeChar = ' ';
        if (c == '(') {
          bracketname = "parentheses";
          closeChar = ')';
        }
        if (c == '[') {
          bracketname = "brackets";
          closeChar = ']';
        }
        if (c == '|') {
          bracketname = "cardinality bars";
          closeChar = '|';
        }
        System.out.println("got a left brace: " + c);
        ParseResultSet parenrs = parseExpression(
          expression, start + 1, context, "an expression inside your " + bracketname
        );
        if (parenrs.Exception != null) {
          System.out.println("parenrs isn't null");
          return parenrs; // return any fatal errors directly
        }
        int needCloseParenAt = -1; // a position that we need a close paren at, if no
        // possible parse of a subclass ends with the close paren character
        SyntaxException orFixError = null; // corresponding to needCloseParenAt, the
        // HowToContinue exception that resulted in
        // not parsing more of the inner expression

        Vector result = new Vector(); // possible parses

        // Wrap each possible parse of the subexpression in Parens.
        for (int i = 0; i < parenrs.Parses.size(); i++) {
          ParseResult parenr = (ParseResult) parenrs.Parses.get(i);

          // Does the subexpression get followed by closeChar?
          int newstart = skipWhitespace(expression, parenr.Next);
          if (newstart == -1) {
            // If we hit the end of the string, we need a close paren there.
            needCloseParenAt = expression.length() - 1;
            orFixError = null;
            continue;
          }
          if (getChar(expression, newstart, context) != closeChar) {
            needCloseParenAt = newstart;
            orFixError = parenr.HowToContinue; // why did the subexpression end there
            continue;
          }

          // If it is followed by closeChar, wrap it in Parens and
          // consider it a possible parse.
          Expr e = null;
          if (c == '(') {
            e = new Parens(parenr.Expression, Parens.ROUND);
          }
          if (c == '[') {
            e = new Parens(parenr.Expression, Parens.SQUARE);
          }
          if (c == '|') {
            e = new Cardinality(parenr.Expression);
          }
          result.add(new ParseResult(e, newstart + 1));
        }

        // If no possible parses are followed by closeChar, raise an exception
        // and give the user one of the possible positions where closeChar
        // would have been a good idea.
        if (result.size() == 0) {
          if (orFixError == null) {
            return new ParseResultSet(
                new SyntaxException(
                  "You need a '" + closeChar + "' at the indicated location.",
                  needCloseParenAt
                )
            );
          } else {
            return new ParseResultSet(
                new SyntaxException(
                  orFixError.getMessage() + " Or, perhaps add a '" +
                  closeChar + "' at the indicated location.",
                  needCloseParenAt
                )
            );
          }
        }

        return new ParseResultSet(result);
      //break

      case Not.SYMBOL:
        // Get the possibilities for the subexpression
        ParseResultSet negrs = parsePrefixExpression(
          expression, start + 1, context, "an expression after the negation operator"
        );
        if (negrs.Exception != null) {
          return negrs; // return any fatal errors directly
        }
        // By parsing a prefix expression as opposed to just any expression here,
        // we achieve the effect that negation binds more strongly than any other infix operator, 
        // and more strongly than function application.
        // E.g. ~A & B is parsed as [~A] & B                

        // Wrap each possible parse of the subexpression in negation
        for (int i = 0; i < negrs.Parses.size(); i++) {
          ParseResult negr = (ParseResult) negrs.Parses.get(i);
          negrs.Parses.set(i, new ParseResult(new Not(negr.Expression),
                                              negr.Next));
        }

        // Return the wrapped possible parses
        return negrs;
        //break

      case ForAll.SYMBOL: //fall through
      case Exists.SYMBOL: //fall through
      case Lambda.SYMBOL: //fall through
      case Iota.SYMBOL:
        System.out.println("got binder: " + c);
        // Get the identifier that follows the binder.
        ParseResultSet vars = parseIdentifier(expression, start + 1, context, "a variable");
        if (vars.Exception != null) {
          return vars; // return any fatal errors directly
        }
        ParseResult var = (ParseResult) vars.Parses.get(0); // parseIdentifier always returns a singleton, if anything
        if (!(var.Expression instanceof Identifier)) { // should never occur??
          return new ParseResultSet(
            new SyntaxException(
              "After a binder, a variable must come next: " + var.Expression + ".",
              start + 1
            )
          );
        }
        int start1 = var.Next;

        // See if a period follows and remember whether one does.
        start = skipWhitespace(expression, start1);
        if (start == -1) {
          return new ParseResultSet(
            new SyntaxException(
              "You seem to be missing an expression following the binder at the end of your expression.",
              expression.length() - 1
            )
          );
        }

        boolean hadWhiteSpace = (start1 != start);

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
        Identifier varid = (Identifier) var.Expression;
        context2.typer.addEntry(varid.getSymbol(), varid instanceof Var, varid.getType());
        
        System.out.println("index of inner exp: " + start);

        // Get the possible parses of the inner expression.
        // First look to see if we can parse parens right there, because parens
        // always indicate the scope of the binder.
//        ParseResultSet insides = parsePrefixExpression(
//          expression, start, context2, "the expression in the scope of the " + c + " binder"
//        );

        // Check if any of the expressions we parse on the inside is a parenthesized expression.
//        boolean insideIsParen = false;
//        if (insides.Exception == null) {
//          for (Iterator i = insides.Parses.iterator(); i.hasNext();) {
//            ParseResult e = (ParseResult) i.next();
//            if (e.Expression instanceof Parens) {
//              insideIsParen = true;
//              break;
//            }
//          }
//        }

//        if (insideIsParen) {
//          // this is the inner expression
//
//          // If there was no space or period, then only prefix expressions may be
//          // in the scope of the binder. We've already parsed it, hopefully.
//        } else if (!hadWhiteSpace && !hadPeriod) {
//          if (insides.Exception != null) {
//            return insides; // return any fatal errors immediately
//          }
//          // However, unless a paren or bracket followed (which we've already covered above),
//          // in which case we can be sure
//          // that the scope of the binder was indicated by the bracketing, we want to
//          // make sure that the user hasn't given us something that might mean he
//          // wanted an infix expression in the scope of the binder. That is,
//          // LxP(x) & Q(x) means the conjunction takes wide scope (i.e. only parse
//          // a prefix expression in the scope of the binder), but, crucially,
//          // LxP(x)&Q(x) is just strange, and we want to reject it outright. (If we
//          // treat it ambiguously, the type-sanity preference will rule out one
//          // of the parses, but we want the end result to be an ambiguity error, so
//          // we must treat it as a fatal error.)
//          // In order to test this, we attempt to parse an infix expression in the
//          // scope of the binder, but flagged as "testSpaceRequired". If the first infix
//          // operator is not surrounded by spaces, parseInfixExpression returns an
//          // error, and we pass it on. If it succeeds, we discard the result because
//          // in fact we want the infix operator to have wide scope.
//
//          ParseResultSet infixes = parseInfixExpression(
//            expression, start, context2,
//            "the expression in the scope of the " + c + " binder", true, false
//          );
//          if (infixes.Exception != null) {
//            return infixes; // return any fatal errors immediately
//          }
//        } else {
//          // Just parse anything inside the scope of the lambda, but don't allow function
//          // application with a space between the function and the argument.
//
//          insides = parseInfixExpression(
//            expression, start, context2,
//            "the expression in the scope of the " + c + " binder", false, false
//          );
//          if (insides.Exception != null) {
//            return insides; // return any fatal errors immediately
//          }
//        }

        // Just parse anything inside the scope of the binder, but don't allow function
        // application with a space between the function and the argument.

        ParseResultSet insides = parseInfixExpression(
          expression, start, context2,
          "the expression in the scope of the " + c + " binder", true, true
        );
        if (insides.Exception != null) {
          System.out.println("inside of binder " + c + " didn't work");
          return insides; // return any fatal errors immediately
        }

        System.out.println("insides:");
        // Wrap each possible parse inside a Binder expression
        for (int i = 0; i < insides.Parses.size(); i++) {
          ParseResult inside = (ParseResult) insides.Parses.get(i);
          System.out.println(inside.Expression.toString());

          Binder bin;
          switch (c) {
            case ForAll.SYMBOL:
              bin = new ForAll((Identifier) var.Expression, inside.Expression, hadPeriod);
              break;
            case Exists.SYMBOL:
              bin = new Exists((Identifier) var.Expression, inside.Expression, hadPeriod);
              break;
            case Lambda.SYMBOL:
              bin = new Lambda((Identifier) var.Expression, inside.Expression, hadPeriod);
              break;
            case Iota.SYMBOL:
              bin = new Iota((Identifier) var.Expression, inside.Expression, hadPeriod);
              break;
            default:
              throw new RuntimeException(); // unreachable
          }

          insides.Parses.set(i, new ParseResult(bin, inside.Next, inside.HowToContinue));
        }

        // return the possible parses
        return insides;
        //break

      case '{':
        Vector elements = new Vector();

        int next = start + 1;
        boolean gotPipe = false;
        Expr rightExpr = null;

        while (true) {
          // Read an expression.
          ParseResultSet elemrs = parseExpression(expression, next, context, "an expression");
          if (elemrs.Exception != null) {
            return elemrs; // return fatal errors immediately
          }
          // Be greedy and take the longest parseable expression at this location.
          Expr e = null;
          for (int i = 0; i < elemrs.Parses.size(); i++) {
            ParseResult elemr = (ParseResult) elemrs.Parses.get(i);
            if (e == null || elemr.Next > next) {
              e = elemr.Expression;
              next = elemr.Next;
            }
          }

          if (gotPipe) {
            // this is the part after the pipe
            rightExpr = e;
          } else {
            // Add this to the elements list.
            elements.add(e);
          }

          next = skipWhitespace(expression, next);
          if (next == -1) {
            return new ParseResultSet(
              new SyntaxException("You need a '}' to complete the set.", expression.length())
            );
          }

          char c2 = getChar(expression, next, context);

          if (c2 == '}') {
            next++;
            break;
          }

          // If we've gotten an expression after the pipe, we had better have been done.
          if (rightExpr != null) {
            return new ParseResultSet(
              new SyntaxException("You need a '}' to complete the set.", next)
            );
          }

          if (c2 == '|') {
            if (elements.size() > 1) {
              return new ParseResultSet(
                new SyntaxException(
                  "A vertical bar cannot be used in a set that also has a list of elements.",
                  next
                )
              );
            }
            gotPipe = true;
          } else if (c2 != ',') {
            return new ParseResultSet(
              new SyntaxException("You need a ',' between elements in a set.", next)
            );
          }

          next++;
        }

        Vector results = new Vector();
        if (rightExpr == null) {
          results.add(
            new ParseResult(new SetWithElements((Expr[]) elements.toArray(new Expr[0])), next)
          );
        } else {
          results.add(
            new ParseResult(new SetWithGenerator((Expr) elements.get(0), rightExpr), next)
          );
        }
        return new ParseResultSet(results);
        //break

      default:
        // Hope that it's an identifier or predicate. If not, a BadCharacterException is returned.
        return parsePredicate(expression, start, context,
                              whatIsExpected == null ? "an expression" : whatIsExpected,
                              false);
    }
  }

  /**
   * Parses an identifier at position start in expression.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for an identifier
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @return an identifier or an error condition
   */
  private static ParseResultSet parseIdentifier(
    String expression, int start, ParseOptions context, String whatIsExpected
  ) {
    return parsePredicate(expression, start, context, whatIsExpected, true);
  }

  /**
   * Parses a predicate at position start in expression. A predicate is an
   * identifier followed by an argument list. If no argument list follows, as a
   * fallback we try to parse an identifier by itself.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for an identifier
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @param isRightAfterBinder whether we're looking for the variable
   * immediately after a binder, in which case: 1) predicates are not allowed,
   * so we stop reading immediately after the identifier, 2) if a type on the
   * identifier is specified (i.e. x<e>), then we know to load the identifier as
   * a variable, and 3) the error message reflects that we're looking for a
   * variable.
   * @return an identifier, predicate, or error condition
   */
  private static ParseResultSet parsePredicate(
    String expression, int start, ParseOptions context, String whatIsExpected, boolean isRightAfterBinder
  ) {
    // If there's no more here, return an error condition.
    start = skipWhitespace(expression, start);
    if (start == -1) {
      return new ParseResultSet(
        new SyntaxException(
          "You seem to be missing " + whatIsExpected + " at the end of your expression.",
          expression.length() - 1
        )
      );
    }

    int realStart = start;
    char c = expression.charAt(start);

    if (!isLetter(c) && !(context.ASCII && c == '\\') &&
        !Character.isDigit(c) && c != SetWithElements.EMPTY_SET_SYMBOL) {
      if (isRightAfterBinder) {
        return new ParseResultSet(
          new BadCharacterException(
            "I'm expecting a variable at the indicated location, " +
            "but variables must start with a letter.",
            start
          )
        );
      } else {
        return new ParseResultSet(
          new BadCharacterException(
            "You cannot have a '" + c + "' at the indicated location. I'm expecting to find " +
            whatIsExpected + " there.",
              start
          )
        );
      }
    }

    // Read in the identifier until the first non-letter-or-number
    // If we're doing single letter identifiers, then stop before
    // the next letter too.
    String id = String.valueOf(c);
    start++;

    // If the first character was a backslash, we have to read
    // in the next character literally. In the loop below, it is
    // already translating characters according to our conventions.
    // But we keep the backslash for later, since we're not sure if
    // literal escaping of the next character was wanted, or else
    // something like \alpha.
    if (id.equals("\\") && start < expression.length()) {
      id += expression.charAt(start++);
    }

    while (start < expression.length()) {
      char ic = getChar(expression, start, context);
      if (ic == '\\' && context.ASCII) {
        // escape next character (and this time, trash the backslash!)
        ic = expression.charAt(++start);
      }
      if (!isIdentifierChar(ic) || context.singleLetterIdentifiers && isLetter(ic)) {
        break;
      }
      id += ic;
      start++;
    }

    // If the identifier starts with a backslash, the user meant
    // one of two things: either he is giving an escape code
    // like \alpha, or he means to escape just the first letter,
    // as in \LIKES to prevet the L from becomming a lambda.
    // If the whole thing is a valid escape sequence, use it.
    if (id.startsWith("\\")) {
      String code = id.substring(1);
      String decoded = translateEscapeCode(code);
      if (decoded != code) {
        id = decoded;
      } else {
        // it wasn't a valid escape sequence so just lop off
        // the initial backslash, since it already did its job
        // of escaping the next character.
        id = code;
      }
    }

    // If an underscore follows the name of the identifier, then the identifier's
    // type follows.
    Type specifiedType = null;
    boolean specifiedTypeIsReallySpecified = true;
    if (start < expression.length() && getChar(expression, start, context) == '_') {
      start++;
      try {
        TypeParser.ParseResult tr = TypeParser.parseType(expression, start, true);
        start = tr.end + 1;
        specifiedType = tr.result;
      } catch (SyntaxException se) {
        return new ParseResultSet(se);
      }
    }

    boolean parsePredicate = true;

    // For the emptyset symbol, we don't expect it to be defined in the typing
    // conventions. Further, take an underscore type specification as the element
    // type T, not its type <T, t>. Also don't allow parsing arguments after this
    // symbol as if it were a predicate.
    if (id.equals(Character.toString(SetWithElements.EMPTY_SET_SYMBOL))) {
      if (specifiedType == null) {
        specifiedType = Type.E; // default element type
      }
      specifiedType = new CompositeType(specifiedType, Type.T);
      parsePredicate = false;
      specifiedTypeIsReallySpecified = false;
    }

    // If the identifier looks like an integer, type it by default as type N.
    if (specifiedType == null) {
      try {
        int asInt = Integer.parseInt(id);
        specifiedType = Type.N;
        parsePredicate = false;
        specifiedTypeIsReallySpecified = false;
      } catch (Exception e) {
        // whatever
      }
    }

    // If we're at the end of the expression, or if our caller does not permit us
    // to parse a predicate, we won't parse a predicate.
    if (start == expression.length() || isRightAfterBinder) {
      parsePredicate = false;
    }
    // If parens, or another identifier, follow immediately, it is a predicate.
    // We parse such predicates here. If neither of those conditions holds, then
    // we return the identifier we found.
    else if (!(getChar(expression, start, context) == '(' ||
               context.singleLetterIdentifiers && isLetter(getChar(expression, start, context)))) {
      parsePredicate = false;
    }

    if (!parsePredicate) {
      try {
        Identifier ident = loadIdentifier(
          id, context, start, null, specifiedType, specifiedTypeIsReallySpecified, isRightAfterBinder
        );
        return new ParseResultSet(new ParseResult(ident, start));
      } catch (IdentifierTypeUnknownException itue) {
        return new ParseResultSet(new SyntaxException(itue.getMessage(), realStart));
      }
    }

    boolean parens = false;
    if (getChar(expression, start, context) == '(') {
      start++;
      parens = true;
    }

    // If we found the identifier 'g' and an open parenthesis,
    // then if we find a close parenthesis, and further if
    // what comes in the middle is an integer, then parse
    // this predicate as a GApp of type e. Otherwise, we fall through
    // and parse this like a normal predicate.
    //
    // TODO: If we read in a GApp of type other than e then we don't
    // notice this. Is this a problem?
    boolean mightBeG = false;
    if (id.equals("g") && parens) {
      int closeparen = expression.indexOf(')', start);
      if (closeparen != -1) {
        mightBeG = true;
        String param = expression.substring(start, closeparen).trim();
        try {
          int idx = Integer.valueOf(param).intValue();
          return new ParseResultSet(new ParseResult(new GApp(idx, Type.E), closeparen + 1));
        } catch (NumberFormatException e) {
          // fall through to treating this
          // like a normal predicate
        }
      }
    }

    ArrayList arguments = new ArrayList();
    boolean first = true;
    while (true) {
      if (parens) {
        // skip whitespace and look for close parens
        start = skipWhitespace(expression, start);
        if (start == -1) {
          return new ParseResultSet(
            new SyntaxException(
              "You seem to be missing " +
              (first ? "a comma, expression, or close parenthesis" : "a comma or close parenthesis") +
              " at the end of your expression.",
              expression.length() - 1
            )
          );
        }
        if (getChar(expression, start, context) == ')') {
          start++;
          break;
        }
      } else {
        if (start == expression.length()) {
          break;
        }
        if (!isLetter(getChar(expression, start, context))) {
          break;
        }
      }

      if (parens) {
        if (!first) {
          // With parentheses, we need commas between the arguments.
          if (getChar(expression, start, context) != ',') {
            return new ParseResultSet(
              new SyntaxException(
                "If another argument to the predicate " + id +
                " starts at the indicated location, use a comma to separate it from the " +
                "previous argument. Otherwise, close your parentheses.",
                start
              )
            );
          }
          start++;
        }
        first = false;

        try {
          String exp;
          if (arguments.size() == 0) {
            if (!mightBeG) {
              exp = "the first argument to the predicate " + id;
            } else {
              exp = "an index (1, 2, etc.) for the assignment function 'g'" +
                    "or the first argument to a predicate 'g'";
            }
          } else {
            exp = "the next argument to the predicate " + id;
          }
          ParseResult arg = parse2(expression, start, context, exp, false);
          arguments.add(arg.Expression);
          start = arg.Next;
        } catch (SyntaxException se) {
          String mes = "Argument " + (arguments.size() + 1) + " to the predicate " + id +
                       " at the indicated location had the following problem: " + se.getMessage();
          return new ParseResultSet(new SyntaxException(mes, se.getPosition()));
        }
      } else {
        char cc = getChar(expression, start, context);
        if (!isLetter(cc)) {// should not reach here...?
          return new ParseResultSet(
            new SyntaxException("Invalid identifier as an argument to " + id + ".", start)
          );
        }
        try {
          arguments.add(loadIdentifier(String.valueOf(cc), context, start, null, null, false, false));
        } catch (IdentifierTypeUnknownException itue) {
          return new ParseResultSet(
              new SyntaxException(itue.getMessage(), start));
        }
        start++;
      }
    }

    if (arguments.size() == 0) { // "P()" is not valid
      return new ParseResultSet(
        new SyntaxException(
          "Within the parenthesis of a predicate, one or more expressions must appear.",
          start
        )
      );
    }

    // If the type of the identifier is not known to the IdentifierTyper,
    // we'll infer its type from the types of the arguments, and assume
    // it is a constant and a function that yields a truth value.
    Type inferType = null;
    try {
      if (arguments.size() == 1) {
        inferType = new CompositeType(((Expr) arguments.get(0)).getType(), Type.T);
      } else {
        Type[] argtypes = new Type[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
          argtypes[i] = ((Expr) arguments.get(i)).getType();
        }
        inferType = new CompositeType(new ProductType(argtypes), Type.T);
      }
    } catch (TypeEvaluationException e) {
    }

    Identifier ident;

    try {
      ident = loadIdentifier(
        id, context, start, inferType, specifiedType, specifiedTypeIsReallySpecified, false
      );
    } catch (IdentifierTypeUnknownException itue) {
      return new ParseResultSet(
        new SyntaxException(itue.getMessage(), realStart)
      );
    }

    if (arguments.size() == 1) { // P(a) : a is an identifier; there is no ArgList
      ParseResult pr = new ParseResult(new FunApp(ident, (Expr) arguments.get(0)), start);
      System.out.println("got term: " + pr.Expression.toString());
      return new ParseResultSet(pr);
    } else { // P(a,b) : (a,b) is an ArgList
      ParseResult pr = new ParseResult(
        new FunApp(ident, new ArgList((Expr[]) arguments.toArray(new Expr[0]))),
        start
      );
      System.out.println("got term: " + pr.Expression.toString());
      return new ParseResultSet(pr);
    }
  }

  // TODO write a formal semantic paper about the difference between
  // "whether the character is used in identifiers" and
  // "whether the character is one that is used in identifiers"
  /**
   * Returns whether the character is one that is used in identifiers, which
   * includes letters (which must be the start of an identifier), numbers,
   * underscores, and primes of various sorts.
   *
   * @param ic the character
   * @return whether the character can be used in an identifier
   */
  public static boolean isIdentifierChar(char ic) {
    return isLetter(ic) ||
           Character.isDigit(ic) ||
           isPrime(ic) ||
           ic == '-';
  }

  public static boolean isPrime(char ic) {
    return ic == '\'' ||
           ic == '`' || // alternate prime character
           ic == '"' || // as if double prime
           ic == Identifier.PRIME;
  }

  /**
   * Creates an instance of Const or Var for an identifier named by id, using
   * the typing conventions of the global parser options
   *
   * @param id the name of the identifier
   * @param context global parsing options
   * @param start the start position of the identifier, used for error messages
   * @param inferType if null and the typing conventions cannot provide a type
   * for the identifier, an exception is thrown; otherwise, if the typing
   * conventions cannot provide a type for the identifier, inferType is used
   * @throws IdentifierTypeUnknownException if the type of the identifier could
   * not be determined
   * @return the new Identifier instance
   */
  private static Identifier loadIdentifier(
    String id, ParseOptions context, int start, Type inferredType,
    Type specifiedType, boolean specifiedTypeIsReallySpecified, boolean isRightAfterBinder
  ) throws IdentifierTypeUnknownException {
    boolean isvar;
    Type type;

    if (specifiedType == null) {
      try {
        isvar = context.typer.isVariable(id);
        type = context.typer.getType(id);
      } catch (IdentifierTypeUnknownException e) {
        if (inferredType == null) {
          throw e;
        }
        isvar = false;
        type = inferredType;
      }
    } else {
      type = specifiedType;
      isvar = isRightAfterBinder;
      if (!isRightAfterBinder) {
        // We're not after a binder, but that doesn't mean it's *not* a variable.
        // Let's check our typing conventions for that.
        try {
          isvar = context.typer.isVariable(id);
        } catch (IdentifierTypeUnknownException e) {
        }
      }
    }

    Identifier ident;
    if (isvar) {
      ident = new Var(id, type, specifiedType != null && specifiedTypeIsReallySpecified);
    } else {
      ident = new Const(id, type, specifiedType != null && specifiedTypeIsReallySpecified);
    }

    if (specifiedType != null && specifiedTypeIsReallySpecified) {
      context.explicitTypes.put(id, ident); // ident because it stores both type and isVar
    }
    return ident;
  }

  /**
   * *
   * Parse an infix expression at position start in expression. An infix
   * expression in the sense of this method is a series of prefix expressions in
   * the sense of parsePrefixExpression separated by conjunction, disjunction,
   * implication, and biconditional. If we only find one such prefix expression,
   * we return the result of parsePrefixExpression as a fallback. This includes
   * the case where we have more than two prefix expressions. Such an
   * expression, e.g. X & Y | Z -> Q, is not parsed recursively, but rather as a
   * simple list of expressions separated by operators as in [X, &, Y, |, Z, ->
   * , Q]. Then the operator precedence is taken care of. The operator
   * precedence is: Equal, Not Equal -- binds strongest And, Or If, Iff -- bind
   * weakest
   *
   * Ambiguity of expressions like Lx.P & Q as Vx.[P & Q] or [Vx.P] & Q are
   * handled by returning not a single result, but for expressions like A & B &
   * C, a list of possible parses: { A, A & B, A & B & C }.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for an infix
   * expression
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @param firstConjunct if null, ignored; otherwise, this is the first
   * conjunct (scanning the first conjunct is skipped); this is used for
   * look-ahead in parsing quantifiers
   * @param testSpaceRequired if true, just test that if an infix operator is
   * found, that it is surrounded by spaces
   * @return the possible infix expressions (or something lesser as fallback)
   * that could be parsed at this location
   */
  private static ParseResultSet parseInfixExpression(
    String expression, int start, ParseOptions context, String whatIsExpected,
    boolean testSpaceRequired, boolean allowFunctionApplicationSpaceInTrivialReturn
  ) {
    System.out.println("parseInfixExpression: " + start);
    Vector results = new Vector();

    // The first thing to do is parse the first operand. However, if the
    // allowFunctionApplicationSpaceInTrivialReturn parameter is false,
    // this means that if we are returning a prefix expression directly and
    // not within an infix expression, then we must have parsed the function
    // application expression with allowSpace set to false.
        
    ParseResultSet trivialReturn, firstConjuncts;

    firstConjuncts = parseFunctionApplicationExpression(
      expression, start, context, whatIsExpected, true
    );
    if (firstConjuncts.Exception != null) {
      return firstConjuncts; // return any fatal errors immediately
    }
    if (allowFunctionApplicationSpaceInTrivialReturn) {
      trivialReturn = firstConjuncts;
    } else {
      trivialReturn = parseFunctionApplicationExpression(
        expression, start, context, whatIsExpected, false
      );
      if (trivialReturn.Exception != null) {
        return trivialReturn; // shouldn't happen if the first one succeeds
      }
    }

    // Then, for the result set in trivialReturn, record those nondeterministic parsing paths.
    for (Iterator i = trivialReturn.Parses.iterator(); i.hasNext();) {
      results.add(i.next());
    }

    // When we parse the first conjunct, we can get any number of possible parses back,
    // and for each we continue trying to parse the rest of the expression looking
    // for a following infix operator.
    for (int i = 0; i < firstConjuncts.Parses.size(); i++) {
      ParseResult firstConjunct = (ParseResult) firstConjuncts.Parses.get(i);
      
      if (firstConjunct.Expression instanceof Binder) { // binders outscope conjunctions
        continue;
      }

      ArrayList operators = new ArrayList();
      ArrayList operands = new ArrayList();

      operands.add(firstConjunct.Expression);

      // Continue parsing the rest and return any error conditions immediately.
      SyntaxException err = parseInfixExpressionRemainder(
        expression, firstConjunct.Next, context, operators, operands, results, testSpaceRequired
      );
      if (err != null) {
        return new ParseResultSet(err);
      }
    }

    return new ParseResultSet(results);
  }

  /**
   * Parse the remainder of an infix expression, returning any error conditions.
   */
  private static SyntaxException parseInfixExpressionRemainder(
    String expression, int start, ParseOptions context,
    ArrayList operators, ArrayList operands, Vector results, boolean testSpaceRequired
  ) {
    // Skip any white space after the previous expression to where we expect an operator
    int pstart = start;
    start = skipWhitespace(expression, start);
    boolean wsBefore = (pstart != start);

    // We know we've reached the end of this infix expression if we've hit the
    // end of the string.
    if (start == -1) {
      return null;
    }

    char c = getChar(expression, start, context);

    // If we're in ASCII mode, convert the ASCII character
    // to a unicode character, and possibly read a few more
    // characters to get the symbol.
    if (context.ASCII) {
      char cnext = (start + 1 < expression.length()) ? expression.charAt(start + 1) : (char) 0;
      char cnextnext = (start + 2 < expression.length()) ? expression.charAt(start + 2) : (char) 0;

      if (c == And.INPUT_SYMBOL) { // '&' //todo: also And.ALTERNATE_INPUT_SYMBOL?
        c = And.SYMBOL; // wedge
      } else if (c == Or.INPUT_SYMBOL) {
        c = Or.SYMBOL;
      } else if (c == Multiplication.INPUT_SYMBOL) {
        c = Multiplication.SYMBOL;
      } else if (c == '-' && cnext == '>') {
        c = If.SYMBOL;
        start++;
      } else if (c == '<' && cnext == '-' && cnextnext == '>') {
        c = Iff.SYMBOL;
        start += 2;
      } else if (c == '!' && cnext == '=') {
        c = Equality.NEQ_SYMBOL;
        start++;
      } // numeric connectives
      else if (c == '<' && cnext == '=') {
        c = NumericRelation.LessThanOrEqual.SYMBOL;
        start++;
      } else if (c == '>' && cnext == '=') {
        c = NumericRelation.GreaterThanOrEqual.SYMBOL;
        start++;
      } // all of the set connectives are doubled characters
      // set intersection: double carets or double v's.
      else if (c == '^' && cnext == '^') {
        c = SetRelation.Intersect.SYMBOL;
        start++;
      } else if (c == 'V' && cnext == 'V') {
        c = SetRelation.Intersect.SYMBOL;
        start++;
      } else if (c == '!' && cnext == '<' && cnextnext == '<') {
        c = SetRelation.NotSubset.SYMBOL;
        start++;
      } else if (c == '<' && cnext == '<' && cnextnext == '<') {
        c = SetRelation.ProperSubset.SYMBOL;
        start++;
      } else if (c == '<' && cnext == '<') {
        c = SetRelation.Subset.SYMBOL;
        start++;
      } else if (c == '!' && cnext == '>' && cnextnext == '>') {
        c = SetRelation.NotSuperset.SYMBOL;
        start++;
      } else if (c == '>' && cnext == '>' && cnextnext == '>') {
        c = SetRelation.ProperSuperset.SYMBOL;
        start++;
      } else if (c == '>' && cnext == '>') {
        c = SetRelation.Superset.SYMBOL;
        start++;
      }
    }

    start++;

    // If the next character isn't and, or, if, iff, eq, or neq, then we're at the end of
    // our expression. Since we've found something complete already, and we have no indication
    // that the user intended a connective, there's no need to return any error status.
    if (!(c == And.SYMBOL||
          c == Or.SYMBOL ||
          c == If.SYMBOL ||
          c == Iff.SYMBOL ||
          c == Equality.EQ_SYMBOL ||
          c == Equality.NEQ_SYMBOL ||
          c == Multiplication.SYMBOL ||
          c == NumericRelation.LessThan.SYMBOL ||
          c == NumericRelation.LessThanOrEqual.SYMBOL ||
          c == NumericRelation.GreaterThan.SYMBOL ||
          c == NumericRelation.GreaterThanOrEqual.SYMBOL ||
          c == SetRelation.Subset.SYMBOL ||
          c == SetRelation.ProperSubset.SYMBOL ||
          c == SetRelation.NotSubset.SYMBOL ||
          c == SetRelation.Superset.SYMBOL ||
          c == SetRelation.ProperSuperset.SYMBOL ||
          c == SetRelation.NotSuperset.SYMBOL ||
          c == SetRelation.Intersect.SYMBOL ||
          c == SetRelation.Union.SYMBOL)
        ) {
      return null;
    }

    // See if any white space is after the connective
    int pstart2 = start;
    boolean wsAfter = (pstart2 != skipWhitespace(expression, start));

    // If we're testing whether spaces are required around the operators,
    // if no space was found on either side, return an error condition.
    // Never require spaces around multiplication.
    if (testSpaceRequired && (!wsBefore || !wsAfter) &&
        c != Multiplication.SYMBOL) {
      return new SyntaxException(
        "Spaces are required around '" + c + "' connectives.", pstart
      );
    }

    // Try to parse the right operand.
    ParseResultSet nextoperands = parseFunctionApplicationExpression(
      expression, start, context,
      "another expression after the " + c + " connective", true
    );

    // If parsing the right operand failed completely, then we return the reason. Because
    // a failure to parse that expression is fatal, since we've already gotten the connective,
    // we are right to return it fatally.
    if (nextoperands.Exception != null) {
      return nextoperands.Exception;
    }

    // For each possible right operand, record what we have so far as the end of a
    // nondeterministic path, and recursively parse for more operands.
    for (int i = 0; i < nextoperands.Parses.size(); i++) {
      ParseResult right = (ParseResult) nextoperands.Parses.get(i);

      // Clone the list of operators and operands that we have so
      // far and add our latest operator/operand to them.
      ArrayList operators2 = new ArrayList(operators);
      ArrayList operands2 = new ArrayList(operands);

      operators2.add(String.valueOf(c));
      operands2.add(right.Expression);

      // Record the nondeterministic path of ending here.
      SyntaxException err2 = parseInfixExpressionFinish(
        operators2, operands2, right.Next, right.HowToContinue, results
      );
      if (err2 != null) {
        return err2;
      }

      // Try to parse more infix operators...
      SyntaxException err = parseInfixExpressionRemainder(
        expression, right.Next, context,
        operators2, operands2, results, testSpaceRequired
      );
      if (err != null) {
        return err;
      }
    }

    return null;
  }

  /**
   * Finish parsing an infix expression. Handle operator precedence and record
   * the result. An exception is returned just when grouping the operators is
   * impossible because two operators of equal precedence (i.e. -> and <->) are
   * used next to each other.
   */
  private static SyntaxException parseInfixExpressionFinish(
    ArrayList operators, ArrayList operands, int next,
    SyntaxException continuationException, Vector results
  ) {
    // Group the operands we found by operator precedence, tighter operators first.
    // After these calls, only a single operand will be left, the one with
    // lowest precedence.

    // Make copies of the lists before grouping since grouping modifies the list,
    // and we came here nondeterminisitically. (Is this necessary?)
    operands = new ArrayList(operands);
    operators = new ArrayList(operators);

    if (operands.size() > 1) {
      // Group first the eq/neq's, then the and/or's, and lastly the if/iffs.
      // But we do them in pairs because if we find that and and or, for instance,
      // are on the same level, then there's a problem because without an operator
      // precedence convention, it is ambiguous.
      char[][] operator_precedence = {
        new char[]{
          Multiplication.SYMBOL,
          SetRelation.Intersect.SYMBOL,
          SetRelation.Union.SYMBOL
        },
        new char[]{
          Equality.NEQ_SYMBOL,
          Equality.EQ_SYMBOL,
          NumericRelation.LessThan.SYMBOL,
          NumericRelation.LessThanOrEqual.SYMBOL,
          NumericRelation.GreaterThan.SYMBOL,
          NumericRelation.GreaterThanOrEqual.SYMBOL,
          SetRelation.Subset.SYMBOL,
          SetRelation.ProperSubset.SYMBOL,
          SetRelation.NotSubset.SYMBOL,
          SetRelation.Superset.SYMBOL,
          SetRelation.ProperSuperset.SYMBOL,
          SetRelation.NotSuperset.SYMBOL
        },
        new char[]{And.SYMBOL, Or.SYMBOL},
        new char[]{If.SYMBOL, Iff.SYMBOL}};

      for (int i = 0; i < operator_precedence.length; i++) {
        SyntaxException ex = groupOperands(operands, operators, operator_precedence[i]);
        if (ex != null) {
          return ex;
        }
      }
    }

    results.add(new ParseResult((Expr) operands.get(0), next, continuationException));

    return null;
  }

  /**
   * Finds each occurrence of the given operator (op) and groups the operand on
   * its left and right together in a new instance of the appropriate Expr
   * class. Left associativity is assumed. The operands/operators lists are
   * modified in place and after this method returns, operands contains a single
   * Expr object that represents the whole sequence of operands/operators.
   *
   * @param operands the sequence of operand in the string
   * @param operators the sequence of operators in the string (one less than the
   * number of operand)
   * @param ops the operators to group: only one of these operators better be
   * present, or else an exception is thrown for having an ambiguity, since
   * these operators have the same precedence.
   * @throws SyntaxException when operators of the same precedence (-> and <->)
   * are used next to eachother.
   */
  private static SyntaxException groupOperands(ArrayList operands, ArrayList operators, char[] ops) {
    // Make sure that only one of the operators in ops is used, since they
    // are at the same precedence level and would have ambiguous bracketing.
    int op_idx = -1;
    for (int i = 0; i + 1 < operands.size(); i++) {
      // Is this operator one that is listed in ops?
      for (int j = 0; j < ops.length; j++) {
        if (((String) operators.get(i)).charAt(0) == ops[j]) {
          if (op_idx == -1) {
            // This is the first operator in ops we found
            op_idx = j;
          } else {
            // We've encountered an operator in ops before.
            if (op_idx == j) {
              // The operator we encountered before is this one,
              // so we're ok.
            } else {
              // We encountered a different operator in the past,
              // which means we have an ambiguous bracketing situation.
              return new SyntaxException(
                "Your expression is ambiguous because it has adjacent " +
                ops[op_idx] + " and " + ops[j] +
                " connectives without parenthesis. Add parentheses.",
                -1
              );
            }
          }
          break;
        }
      }
    }

    // If op_idx == -1, then we haven't found any instances of these operators.
    if (op_idx == -1) {
      return null;
    }

    // Otherwise, we found just one operator in ops, the one at op_idx.
    char op = ops[op_idx];

    boolean associative = (
      op == And.SYMBOL ||
      op == Or.SYMBOL ||
      op == Multiplication.SYMBOL ||
      op == SetRelation.Intersect.SYMBOL ||
      op == SetRelation.Union.SYMBOL
    );

    for (int i = 0; i + 1 < operands.size(); i++) {
      boolean groupedLast = false;

      while (i + 1 < operands.size() && ((String) operators.get(i)).charAt(0) == op) {
        Expr left = (Expr) operands.get(i);
        Expr right = (Expr) operands.get(i + 1);

        if (!associative && groupedLast) {
          return new SyntaxException(
            "Your expression is ambiguous because it has adjacent " + op +
            " connectives without parenthesis, and this connective is not associative. Add parenthesis.",
            -1
          );
        }

        Expr binary;
        switch (op) {
          case And.SYMBOL:
            binary = new And(left, right);
            break;
          case Or.SYMBOL:
            binary = new Or(left, right);
            break;
          case If.SYMBOL:
            binary = new If(left, right);
            break;
          case Iff.SYMBOL:
            binary = new Iff(left, right);
            break;
          case Equality.EQ_SYMBOL:
            binary = new Equality(left, right, true);
            break;
          case Equality.NEQ_SYMBOL:
            binary = new Equality(left, right, false);
            break;
          case Multiplication.SYMBOL:
            binary = new Multiplication(left, right);
            break;
          case NumericRelation.LessThan.SYMBOL:
            binary = new NumericRelation.LessThan(left, right);
            break;
          case NumericRelation.LessThanOrEqual.SYMBOL:
            binary = new NumericRelation.LessThanOrEqual(left, right);
            break;
          case NumericRelation.GreaterThan.SYMBOL:
            binary = new NumericRelation.GreaterThan(left, right);
            break;
          case NumericRelation.GreaterThanOrEqual.SYMBOL:
            binary = new NumericRelation.GreaterThanOrEqual(left, right);
            break;
          case SetRelation.Subset.SYMBOL:
            binary = new SetRelation.Subset(left, right);
            break;
          case SetRelation.ProperSubset.SYMBOL:
            binary = new SetRelation.ProperSubset(left, right);
            break;
          case SetRelation.NotSubset.SYMBOL:
            binary = new SetRelation.NotSubset(left, right);
            break;
          case SetRelation.Superset.SYMBOL:
            binary = new SetRelation.Superset(left, right);
            break;
          case SetRelation.ProperSuperset.SYMBOL:
            binary = new SetRelation.ProperSuperset(left, right);
            break;
          case SetRelation.NotSuperset.SYMBOL:
            binary = new SetRelation.NotSuperset(left, right);
            break;
          case SetRelation.Intersect.SYMBOL:
            binary = new SetRelation.Intersect(left, right);
            break;
          case SetRelation.Union.SYMBOL:
            binary = new SetRelation.Union(left, right);
            break;
          default:
            throw new RuntimeException(); // unreachable
        }
        operands.set(i, binary);
        operators.remove(i);
        operands.remove(i + 1);

        groupedLast = true;
      }
    }

    return null;
  }

  /**
   * Parses all sorts of expressions starting at position start in expression.
   * It looks first for an infix/prefix expression (anything returned by
   * parseInfixExpression), and if that's followed by another expression, take
   * it as an argument to a function application expression. Parses an
   * expression at position start in expression. We first try to parse a
   * function application because it is the operator with the lowest precedence.
   * This method will fall back appropriately if no function application is
   * present. A function application in the sense of this method consists of a
   * list of expressions E1, ..., En, with n>=1. If n = 1 then we fallback and
   * call parseInfixExpression on E1. (Infix operators bind least strongly
   * except for function application.) (If n > 2 then we have a series of
   * function applications, which we associate left-to-right.) We always parse
   * E1 using parseInfixExpression and any Ei for i>=1 using
   * parsePrefixExpression. The reason for using the less inclusive method
   * parsePrefixExpression on Ei i>1 is that we don't allow any of those Ei to
   * be infix expressions. E.g. we disallow Lx. ~x a & b and require the user to
   * disambiguate thus: Lx. ~x (a & b) In the former formula, a & b is an infix
   * expression. In the latter one, (a & b) is a prefix expression.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for a function
   * application
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @return a set of possible parses of function application expressions or
   * something lesser as fallback, or an error condition
   */
  private static ParseResultSet parseFunctionApplicationExpression(
    String expression, int start, ParseOptions context, String whatIsExpected, boolean allowSpace
  ) {
    // Parse the left-hand side of the function application, which can be
    // any type of expression besides function application.
    System.out.println("parseFunctionApplicationExpression: " + start);
    ParseResultSet lefts = parsePrefixExpression(expression, start, context, whatIsExpected);
    if (lefts.Exception != null) {
      return lefts; // return any fatal errors immediately
    }
    // Collect possible parses here. Delay adding the left expression until later.
    Vector results = new Vector();

    // For each possible parse of the left hand side, try to parse an expression
    // after it as its argument.
    for (int i = 0; i < lefts.Parses.size(); i++) {
      ParseResult left = (ParseResult) lefts.Parses.get(i);
      parseFunctionApplicationRemainder(expression, context, left, results, allowSpace);
    }

    return new ParseResultSet(results);
  }


  /**
   * Try to parse the right-hand-side of a function application. If the string
   * ends, we just yield the left hand side directly. Otherwise, we parse a
   * second expression and wrap the left and right sides in a FunApp.
   * Left-associativity is accomplished by taking this result and using it as
   * the left-hand-side of a further attempt to find a right-hand-side/FunApp.
   * Possible parses --- both for the left-hand-side alone and for the whole
   * thing as a FunApp --- are collected in results.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for a function
   * application
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @param left the left-hand-side of the function application
   * @param results the list into which possible parses of function application
   * (ending at any point) are added
   */
  private static void parseFunctionApplicationRemainder(
    String expression, ParseOptions context, ParseResult left, Vector results, boolean allowSpace
  ) {
    // Attempt to parse a second expression, and if we get one,
    // we create a FunApp between the first (left) and second.

    // Skip any white space after the previous expression.
    // If we hit the end of the string, skipWhitespace returns -1, and we
    // record the possible parse of just the left hand expression and then break out.
    int start = skipWhitespace(expression, left.Next);
    if (start == -1) {
      results.add(left);
      return;
    }
    
    if (left.Expression instanceof Binder) {
      // binders have lower precedence than FA or infixes, so shouldn't
      // appear as the left hand side of either binary expression
      results.add(left);
      return;
    }

    // If we don't allow spaces between the function and argument,
    // but a space was encountered, we're done.
    if (!allowSpace && left.Next != start) {
      results.add(left);
      return;
    }

    // Parse a prefix expression to our right.
    ParseResultSet rights = parsePrefixExpression(
      expression, start, context, "an argument to the function " + left.Expression.toString()
    );

    // If parsing what we think might be an argument failed, we want to alter our
    // error message depending on the type of the left argument. If indeed it is
    // a function-typed thing, or if we can't determine its type, then indicate
    // in the HowToContinue field of the left-hand-side how parsing the argument
    // failed. However, if the left-thing isn't a function, then if parsing
    // the argument failed (because there is no argument; how's that for presupposition
    // cancellation!), don't bother passing that information up. The user
    // probably didn't intend to parse an argument.
    if (rights.Exception != null) {
      try {
        Type t = left.Expression.getType();
        if (!(t instanceof CompositeType)) {
          results.add(left);
          return;
        }
      } catch (TypeEvaluationException tee) {
        // nevermind
      }
    }

    // We return the nondeterministic path up to
    // the left expression and note in its HowToContinue
    // field the error that prevents us from continuing
    // it with the text that follows.
    // If !allowSpace, which means we're right in the scope of a binder,
    // and if there wasn't space (there wasn't, we checked already) and we
    // were able to parse the next argument, then
    // we always parse the function application low, so we *don't* allow
    // the nondeterministic parsing path that ends after the function, which
    // allows the argument to be parsed higher up.
    if (allowSpace || rights.Exception != null) {
      results.add(new ParseResult(left.Expression, left.Next, rights.Exception));
    }

    // And if indeed we had an error getting the argument, we have to stop.
    if (rights.Exception != null) {
      return;
    }

    // For each possible parse to our right, assemble a FunApp, and then
    // recursively attempt to parse yet another argument to our right.
    // We don't add the parsed FunApp to results here. Rather, we delay
    // that until later in the recursive call so that if it has a failed
    // parse of a further argument, it can note in the ParseResult what
    // the error was.
    for (int j = 0; j < rights.Parses.size(); j++) {
      ParseResult right = (ParseResult) rights.Parses.get(j);
      Expr expr = new FunApp(left.Expression, right.Expression); // left associativity
      parseFunctionApplicationRemainder(
        expression, context, new ParseResult(expr, right.Next), results, allowSpace
      );
    }
  }

  /**
   * Parses an expression at position start in expression. We first try to parse
   * a function application because it is the operator with the lowest
   * precedence. The method parseFunctionApplication will fall back
   * appropriately if no function application is present.
   *
   * @param expression the text string being parsed
   * @param start the position at which to start scanning for an expression
   * @param context global options for parsing
   * @param whatIsExpected a string describing what kind of expression is
   * expected to occur at this position, for error messages
   * @return the set of possible expressions parsed starting at this location
   * (and ending wherever), or an error condition
   */
  private static ParseResultSet parseExpression(
    String expression, int start, ParseOptions context, String whatIsExpected
  ) {
    System.out.println("parse Expression");
    return parseInfixExpression(expression, start, context, whatIsExpected, false, true);
  }

  public static String translateEscapeCode(String code) {
    if (code.equals("alpha")) {
      return "\u03B1";
    }
    if (code.equals("beta")) {
      return "\u03B2";
    }
    if (code.equals("gamma")) {
      return "\u03B3";
    }
    if (code.equals("delta")) {
      return "\u03B4";
    }
    if (code.equals("epsilon")) {
      return "\u03B5";
    }
    if (code.equals("theta")) {
      return "\u03B8";
    }
    if (code.equals("pi")) {
      return "\u03C0";
    }
    if (code.equals("rho")) {
      return "\u03C1";
    }
    if (code.equals("sigma")) {
      return "\u0C31";
    }
    if (code.equals("phi")) {
      return "\u03C6";
    }
    if (code.equals("psi")) {
      return "\u03C8";
    }
    if (code.equals("omega")) {
      return "\u03C9";
    }
    if (code.equals("sigma")) {
      return "\u03A3";
    }
    if (code.equals("tau")) {
      return "\u03A4";
    }

    if (code.equals("Gamma")) {
      return "\u03B1";
    }
    if (code.equals("Delta")) {
      return "\u03B1";
    }
    if (code.equals("Theta")) {
      return "\u0398";
    }
    if (code.equals("Pi")) {
      return "\u03A0";
    }
    if (code.equals("Rho")) {
      return "\u03A1";
    }
    if (code.equals("Sigma")) {
      return "\u03A3";
    }
    if (code.equals("Phi")) {
      return "\u03A6";
    }
    if (code.equals("Psi")) {
      return "\u03A8";
    }
    if (code.equals("Omega")) {
      return "\u03A9";
    }
    if (code.equals("Sigma")) {
      return "\u03C3";
    }
    if (code.equals("Tau")) {
      return "\u03C4";
    }

    if (code.equals("emptyset")) {
      return Character.toString(SetWithElements.EMPTY_SET_SYMBOL);
    }

    return code;
  }
}
