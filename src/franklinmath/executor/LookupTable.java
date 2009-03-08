/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

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
