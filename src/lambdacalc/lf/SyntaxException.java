package lambdacalc.lf;

public class SyntaxException extends Exception {

    int position;
    
    public SyntaxException(String message, int position) {
        super(message + " at column " + position);
        this.position = position;
    }

}
