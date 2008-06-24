package franklinmath.expression;

/**
 *
 * @author Allen Jordan
 */
public class ExpressionException extends Exception {
    protected String msg;
    
    public ExpressionException() {
        msg = "Expression error";
    }
    public ExpressionException(String message) {
        msg = "Expression error: " + message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
