package lambdacalc.lf;

public class SyntaxException extends Exception {

    int position;
    
    public SyntaxException(String message, int column) {
        super(message + " at column " + column);
        this.position = column;
    }

}
