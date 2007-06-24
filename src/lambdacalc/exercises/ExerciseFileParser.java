/*
 * ExerciseFileParser.java
 *
 * Created on May 31, 2006, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lambdacalc.exercises;

/*
 * File format:
 *
 * constants of type e : a b-c
 * constants of type <e,t> : P-Q
 * constant of type <e*e,t> : R
 * variables of type e : x-z
 * variables of type <et> : X Y
 *
 * exercise semantic types
 * title Semantic Types Groups 1 : The Easy Ones
 * Lx(x or y)
 *
 * exercise semantic types
 * title Semantic Types Groups 1 : The Hard Ones
 * 
 */

import java.io.*;
import java.text.*;
import java.util.regex.*;
import lambdacalc.logic.*;

/**
 * A parser for teacher-written exercise files in text format.
 */
public class ExerciseFileParser {
    private ExerciseFileParser() {
    }
    
    /**
     * Parses an exercise file from the given Reader.
     */
    public static ExerciseFile parse(Reader reader) 
        throws IOException, ExerciseFileFormatException {
        
        // distinguishes between variables and constants and knows their semantic types
        IdentifierTyper typer = IdentifierTyper.createDefault();
        boolean typerIsDefault = true;
        
        // options for parsing lambda expressions
        ExpressionParser.ParseOptions exprParseOpts = new ExpressionParser.ParseOptions();
        exprParseOpts.ASCII = true;
        exprParseOpts.singleLetterIdentifiers = true;
        exprParseOpts.typer = typer;
        
        // the file of exercises to be returned
        ExerciseFile file = new ExerciseFile();
        
        // some parsing state
        boolean hasreadtitle = false;
        String extype = null;
        String title = null;
        String directions = ""; // group level
        String instructions = ""; // exercise level
        ExerciseGroup group = null;
        java.math.BigDecimal pointage = java.math.BigDecimal.valueOf(1);
        
        int linectr = 0;
        int exindex = 0;
        
        BufferedReader b = new BufferedReader(reader);
        String line;
        while ((line = b.readLine()) != null) {
            linectr++;
            
            if (line.trim().equals("") || line.startsWith("#"))  continue;
            
            if (!hasreadtitle) {
                file.setTitle(line);
                hasreadtitle = true;
                continue;
            }
            
            if (line.startsWith("constants of type ") || line.startsWith("variables of type ") || line.startsWith("constant of type ") || line.startsWith("variable of type ")) {
                if (typerIsDefault)
                    typer.clear();
                typerIsDefault = false;
                
                if (line.startsWith("constants of type ")) {
                    parseTypeLine("constants of type ".length(), false, line, typer, linectr);
                } else if (line.startsWith("constant of type ")) {
                    parseTypeLine("constant of type ".length(), false, line, typer, linectr);
                } else if (line.startsWith("variables of type ")) {
                    parseTypeLine("variables of type ".length(), true, line, typer, linectr);
                } else if (line.startsWith("variable of type ")) {
                    parseTypeLine("variable of type ".length(), true, line, typer, linectr);
                }

            } else if (line.startsWith("points per exercise ")) {
                pointage = new java.math.BigDecimal(line.substring("points per exercise ".length()));
                
            } else if (line.startsWith("exercise ")) {
                extype = line.substring("exercise ".length());
                if (!(extype.equals("semantic types")
                    || extype.equals("lambda conversion")
                    || extype.equals("tree")))
                    throw new ExerciseFileFormatException("An exercise type must be 'semantic types', 'lambda conversion', or 'tree'", linectr, line);
                group = null;
                exindex = 0;
                instructions = "";
            
            } else if (line.startsWith("title ")) {
                title = line.substring("title ".length()).trim();
                if (title.equals(""))
                    throw new ExerciseFileFormatException("You must provide a title after the 'title' keyword", linectr, line);
                group = null;
                exindex = 0;
                
            } else if (line.startsWith("directions ")) {
                directions += line.substring("directions ".length()).trim() + " ";
                group = null;
                exindex = 0;

            } else if (line.startsWith("instructions ")) {
                instructions += line.substring("instructions ".length()).trim() + " ";
                
            } else if (line.equals("single letter identifiers")) {
                exprParseOpts.singleLetterIdentifiers = true;
            } else if (line.equals("multiple letter identifiers")) {
                exprParseOpts.singleLetterIdentifiers = false;
                
            } else if (line.startsWith("define ")) {
                parseLexiconLine(line, exprParseOpts, file, linectr);

            } else if (line.startsWith("use rule ")) {
                String rule = line.substring("use rule ".length());
                if (rule.equals("function application")) {
                    file.getRules().add(
                        lambdacalc.lf.FunctionApplicationRule.INSTANCE);
                } else if (rule.equals("non-branching nodes")) {
                    file.getRules().add(
                            lambdacalc.lf.NonBranchingRule.INSTANCE);
                } else if (rule.equals("predicate modification")) {
                    file.getRules().add(
                            lambdacalc.lf.PredicateModificationRule.INSTANCE);
                } else if (rule.equals("lambda abstraction")) {
                    file.getRules().add(
                            lambdacalc.lf.LambdaAbstractionRule.INSTANCE);
                } else
                    throw new ExerciseFileFormatException(
                       "'use rule' must be followed by 'function application' " +
                            "'non-branching nodes', or 'predicate modification'," +
                            "or 'lambda abstraction'", 
                            linectr, line);

            } else {
                // this is an exercise
                Exercise ex = null;
                
                if (extype == null) {
                    throw new ExerciseFileFormatException("Specify the exercise type with the 'exercise' keyword before giving any exercises", linectr, line);
                } else if (extype.equals("semantic types")) {
                    try {
                        ex = new TypeExercise(line, exprParseOpts, exindex++, typer.cloneTyper());
                    } catch (Exception e) {
                        throw new ExerciseFileFormatException(e.getMessage(), linectr, line);
                    }
                } else if (extype.equals("lambda conversion")) {
                    try {
                        ex = new LambdaConversionExercise(line, exprParseOpts, exindex++, typer.cloneTyper());
                    } catch (Exception e) {
                        throw new ExerciseFileFormatException(e.getMessage(), linectr, line);
                    }
                } else if (extype.equals("tree")) {
                    try {
                        ex = new TreeExercise(line, exindex++, typer.cloneTyper());
                        ((TreeExercise)ex).getTree().guessLexicalEntries(file.getLexicon());
                        boolean nonBranchingOnly = !lambdacalc.Main.GOD_MODE;
                        ((TreeExercise)ex).getTree().guessRules(file.getRules(), nonBranchingOnly);
                            
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ExerciseFileFormatException(e.getMessage(), linectr, line);
                    }
                }
                
                if (ex != null) {
                    if (group == null) {
                        if (title == null)
                            throw new ExerciseFileFormatException("Specify the title of the exercise group with the 'title' keyword before giving any exercises", linectr, line);
                        
                        group = file.addGroup();
                        group.setTitle(title);
                        group.setDirections(escapeDirections(directions, exprParseOpts));
                        title = null;
                        directions = "";
                    }
                    
                    group.addItem(ex);
                    
                    ex.setPoints(pointage);
                    
                    if (instructions.trim().equals(""))
                        ex.setInstructions(null);
                    else
                        ex.setInstructions(escapeDirections(instructions, exprParseOpts));
                    instructions = "";
                }
            }
        }
        b.close();
        return file;
    }
    
    /**
     * Parses a line that indicates the semantic type of an identifier.
     */
    private static void parseTypeLine(int chop, boolean variable, String line, IdentifierTyper typer, 
            int linenum) throws ExerciseFileFormatException {
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new ExerciseFileFormatException("A type line looks like \"constants of type e : a b-c\"", linenum, line);
        
        String typestr = line.substring(chop, colon).trim();
        String ranges = line.substring(colon+1).trim();
        
        String[] parts = ranges.split(" +");
        for (int i = 0; i < parts.length; i++) {
            String range = parts[i];
            if (range.equals("")) continue;
            
            char charstart, charend;

            if (range.length() == 1) {
                charstart = range.charAt(0);
                charend = charstart;
            } else if (range.length() == 3 && range.charAt(1) == '-') {
                charstart = range.charAt(0);
                charend = range.charAt(2);
            } else {
                throw new ExerciseFileFormatException("You must have a letter or letter range in a type line", linenum, line);
            }

            if (!Character.isLetter(charstart) || !Character.isLetter(charend))
                throw new ExerciseFileFormatException("Identifiers start with letters", linenum, line);

            try {
                Type type = TypeParser.parse(typestr);
                typer.addEntry(String.valueOf(charstart), String.valueOf(charend), variable, type);
            } catch (SyntaxException e) {
                throw new ExerciseFileFormatException(e.getMessage(), linenum, line);
            }
        }
    }
    
    private static void parseLexiconLine(String line, ExpressionParser.ParseOptions exprParseOpts, ExerciseFile file, int linenum) throws ExerciseFileFormatException {
        // lexicon lines start with "define "
    
            int colon = line.indexOf(':');
            if (colon == -1)
                throw new ExerciseFileFormatException("Every lexical entry 'define' line must contain a colon.", linenum, line);
            
            // get the orthographic forms after "define " and before the colon
            String orthos = line.substring("define ".length(), colon).trim();
            String exprform = line.substring(colon+1).trim();
            
            // Before the colon, we can have multiple orthographic
            // forms associated with the lexical entry, separated by
            // commas. After splitting it on the comma, trim each entry
            // to eliminate white space around commas and before the colon.
            String[] orthoForms = orthos.split(",");
            for (int i = 0; i < orthoForms.length; i++)
                orthoForms[i] = orthoForms[i].trim();
            
            // if there was nothing before the colon (note: it has already
            // been trimmed), then raise an error.
            if (orthoForms[0].length() == 0)
                throw new ExerciseFileFormatException("One or more words separated by commas must precede the colon in the lexical entry.", linenum, line);
            
            // Parse the expression
            Expr expr;
            try {
                expr = ExpressionParser.parse(exprform, exprParseOpts);
            } catch (lambdacalc.logic.SyntaxException ex) {
                throw new ExerciseFileFormatException(ex.getMessage(), linenum, line);
            }
            
            // Add this lexical entry into our database.
            file.getLexicon().addLexicalEntry(orthoForms, expr);
    }
    
    private static String escapeDirections(String directions, ExpressionParser.ParseOptions exprParseOpts) {
        if (directions == null) return null;
        
        directions = directions.trim();
        
        // Turn two backslashes (plus any trailing whitespace) into a hard line break.
        // replaceAll's first argument is a regular expression, so "\\" is
        // complicated to represent. Each slash is escaped in the regex,
        // and then escaped again to represent in source code.
        directions = directions.replaceAll("\\\\\\\\ *", "\n");
        
        // Various escape sequence are also supported. (Note again the four
        // backslashes which in the regex means just a single literal backslash.)
        // As we do it now, the escape sequence ends on the first non-letter,
        // so unlike LaTeX, \alphawhatever does not translate into awhatever.
        Pattern p = Pattern.compile("\\\\([a-zA-Z]+)");
        Matcher m = p.matcher(directions);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String code = m.group(1);
            code = ExpressionParser.translateEscapeCode(code);
            m.appendReplacement(sb, code);
        }
        m.appendTail(sb);
        directions = sb.toString();

        // Allow formulas to be surrounded in braces, parse them, and then
        // toString() them so that special symbols like lambdas are interpreted.
        p = Pattern.compile("\\{([^}]+)\\}");
        m = p.matcher(directions);
        sb = new StringBuffer();
        while (m.find()) {
            String exprstr = m.group(1);
            try {
                Expr expr = ExpressionParser.parse(exprstr, exprParseOpts);
                // if successful...
                m.appendReplacement(sb, expr.toString());
            } catch (SyntaxException e) {
                // if not successful, don't do any replacement; keep the braces
            }
        }
        m.appendTail(sb);
        directions = sb.toString();
        
        return directions;
    }
}
