package franklinmath.executor;

/**
 * An exception related to execution.  
 * @author Allen Jordan
 */
public class ExecutionException extends Exception {
    protected String msg;
    
    public ExecutionException() {
        msg = "";
    }
    public ExecutionException(String message) {
        msg = message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
