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
package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * Find the minimum number in a list.
 * @author Allen Jordan
 */
public class MinCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {

        SingleExpression single = GetSingleArgument(args);
        if (single.IsSingleNegative()) {
            throw new CommandException("Invalid negative single expression", GetName());
        }
        Factor factor = single.SingleValue();
        if (!factor.IsExprList()) {
            throw new CommandException("Argument must be an expression list", GetName());
        }

        try {
            Vector<Expression> exprList = factor.GetExprList();
            if (exprList.size() <= 0) throw new CommandException("Input expression list is empty", GetName());
            FMNumber smallest = exprList.get(0).GetSingleNumber();
            for (int i=1; i<exprList.size(); i++) {
                FMNumber value = exprList.get(i).GetSingleNumber();
                if (value.compareTo(smallest) < 0) smallest = value;
            }
            return new FMResult(new Factor(smallest));
        }
        catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
