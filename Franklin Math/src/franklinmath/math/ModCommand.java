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
 *
 * @author Allen Jordan
 */
public class ModCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 2);
        try {
            Equation arg0 = args.get(0);
            Equation arg1 = args.get(1);
            if ((!arg0.IsExpression()) || (!arg1.IsExpression())) throw new Exception("Equation arguments invalid for this function");
            
            FMNumber firstNumber = arg0.GetLHS().GetSingleNumber();
            FMNumber secondNumber = arg1.GetLHS().GetSingleNumber();
            
            return new FMResult(new Factor(new FMNumber(firstNumber.doubleValue() % secondNumber.doubleValue())));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.getMessage(), GetName());
            }
        }
    }
}
