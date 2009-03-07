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
public class RadiansToDegreesCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber value = GetNumberArgument(args, 0);
            
            Term term = new Term(new Power(new Factor(value)));
            term = term.AppendPower(new Power(new Factor(180)), PowerOperator.MULTIPLY);
            term = term.AppendPower(new Power(new Factor(new SymbolicFunction("Pi", new Vector<Equation>(), true))), PowerOperator.DIVIDE);
            
            Expression result = expressionToolset.Flatten(new Expression(term, TermOperator.NONE));
            FMNumber resultNumber = result.GetSingleNumber();
            return new FMResult(new Factor(resultNumber));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
