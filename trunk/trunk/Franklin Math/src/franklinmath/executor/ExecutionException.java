package franklinmath.executor;

/**
 *
 * @author Allen Jordan
 */
public class ExecutionException extends Exception {
    protected String msg;
    
    public ExecutionException() {
        msg = "Execution error";
    }
    public ExecutionException(String message) {
        msg = "Execution error: " + message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
