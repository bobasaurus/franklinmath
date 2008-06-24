package franklinmath.executor;

import java.util.*;

/**
 * This class represents a lookup table for functions.  
 * @author Allen Jordan
 */
public class FunctionTable {

    protected Hashtable<String, Command> table = new Hashtable<String, Command>();

    public FunctionTable() {
    }

    public void Set(String name, Command value) {
        table.put(name, value);
    }

    public Command Get(String name) throws Exception {
        Command command = table.get(name);
        if (command == null) {
            throw new ExecutionException("Function " + name + " does not exist");
        }
        return command;
    }

    public boolean Exists(String name) {
        return table.containsKey(name);
    }
}
