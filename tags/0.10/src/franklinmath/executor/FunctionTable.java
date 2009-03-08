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
