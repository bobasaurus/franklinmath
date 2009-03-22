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

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;

/**
 * Calculate the golden ratio using the equation (1+5^.5)/2
 * @author Allen Jordan
 */
public class GoldenRatioCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 0);
        try {

            Expression expr = new Expression(new Term(new Power(new Factor(1))), TermOperator.NONE);
            Power power = new Power(new Factor(5));
            power = power.AppendFactor(new Factor(0.5));
            expr = expr.Add(new Term(power));

            Factor nestedExpr = new Factor(expr);
            Term term = new Term(new Power(nestedExpr));
            term = term.AppendPower(new Power(new Factor(2)), PowerOperator.DIVIDE);

            Expression finalExpr = new Expression(term, TermOperator.NONE);
            Expression flattenedResult = expressionToolset.Flatten(finalExpr);

            return new FMResult(flattenedResult);
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        } catch (ExecutionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
