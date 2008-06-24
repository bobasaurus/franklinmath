package franklinmath.executor;

import java.util.*;

import franklinmath.expression.*;

/**
 * This class represents a lookup table for variable/user-defined expressions.  
 * @author Allen Jordan
 */
public class LookupTable {

    protected Hashtable<String, Expression> table = new Hashtable<String, Expression>();

    public LookupTable() {
    }

    public void Set(String name, Expression value) {
        table.put(name, value);
    }

    public Expression Get(String name) throws ExecutionException {
        Expression expression = table.get(name);
        if (expression == null) {
            throw new ExecutionException("Variable " + name + " does not exist");
        }
        
        return expression;
    }

    public boolean Exists(String name) {
        return table.containsKey(name);
    }
}
