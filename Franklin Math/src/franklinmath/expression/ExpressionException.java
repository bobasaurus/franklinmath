package franklinmath.expression;

/**
 * An exception related to expressions.  
 * @author Allen Jordan
 */
public class ExpressionException extends Exception {
    protected String msg;
    
    public ExpressionException() {
        msg = "";
    }
    public ExpressionException(String message) {
        msg = message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
