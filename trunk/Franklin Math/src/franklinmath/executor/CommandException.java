package franklinmath.executor;

/**
 *
 * @author Allen Jordan
 */
public class CommandException extends Exception {
    private String msg;
    
    public CommandException() {
        msg = "Command error";
    }
    
    public CommandException(String message) {
        msg = "Command error: " + message;
    }
    
    public CommandException(String message, String functionName) {
        msg = "Command error in function " + functionName + ": " + message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
