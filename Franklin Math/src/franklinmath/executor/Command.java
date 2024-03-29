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

import java.util.Vector;

import franklinmath.expression.*;

/**
 * Abstract base class for all commands, including function calls.  Follows the command design pattern.  
 * @author Allen Jordan
 */
public abstract class Command {

    protected String name = "";
    protected boolean isMathFunction = false;

    public abstract FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException;

    //Typically used only during initialization.  
    public void SetName(String functionName) {
        name = functionName;
    }

    //Typically used only during initialization.  
    public void SetIsMathFunction(boolean isMathFunc) {
        isMathFunction = isMathFunc;
    }

    public String GetName() {
        return name;
    }

    public boolean IsMathFunction() {
        return isMathFunction;
    }

    protected void CheckArgsLength(Vector<Equation> args, int expectedSize) throws CommandException {
        if (args.size() != expectedSize) {
            throw new CommandException("Invalid function parameter list length", GetName());
        }
    }

    protected SingleExpression GetSingleArgument(Vector<Equation> args) throws CommandException {
        SingleExpression single = null;

        if (args.size() != 1) {
            throw new CommandException("Invalid function parameter list length", GetName());
        }
        Equation equ = args.get(0);
        if (!equ.IsExpression()) {
            throw new CommandException("Equations are invalid as parameters here", GetName());
        }
        Expression expr = equ.GetLHS();

        single = expr.GetSingle();
        if (single == null) {
            throw new CommandException("Problem obtaining single argument", GetName());
        }

        return single;
    }

    protected FMNumber GetNumberArgument(Vector<Equation> args, int index) throws CommandException {
        if (args.size() <= index) {
            throw new CommandException("Too few arguments", GetName());
        }
        Equation argument = args.get(index);
        if (!argument.IsExpression()) {
            throw new CommandException("Equation invalid as argument", GetName());
        }
        try {
            FMNumber result = argument.GetLHS().GetSingleNumber();
            return result;
        } catch (ExpressionException ex) {
            throw new CommandException(ex.getMessage(), GetName());
        }
    }
}
