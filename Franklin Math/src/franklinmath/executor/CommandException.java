package franklinmath.executor;

/**
 * An exception related to commands.  
 * @author Allen Jordan
 */
public class CommandException extends Exception {
    private String msg;
    
    public CommandException() {
        msg = "";
    }
    
    public CommandException(String message) {
        msg = message;
    }
    
    public CommandException(String message, String functionName) {
        msg = "Error in function " + functionName + ": " + message;
    }
    
    @Override public String getMessage() {
        return msg;
    }
    
    @Override public String toString() {
        return getMessage();
    }
}
