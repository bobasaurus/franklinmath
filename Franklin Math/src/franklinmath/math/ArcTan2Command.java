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
public class ArcTan2Command extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 2);
        try {
            FMNumber number1 = GetNumberArgument(args, 0);
            FMNumber number2 = GetNumberArgument(args, 1);
            
            double result = StrictMath.atan2(number1.doubleValue(), number2.doubleValue());
            return new FMResult(new Factor(new FMNumber(result)));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
