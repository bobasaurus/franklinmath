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
import java.math.*;

/**
 *
 * @author Allen Jordan
 */
public class FibonacciNumbersCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber seriesLength = GetSingleArgument(args).SingleValue().GetNumber();
            if (seriesLength.doubleValue() == 0) {
                return new FMResult(new Factor(0));
            }

            FMNumber prev1 = FMNumber.ZERO;
            FMNumber prev2 = FMNumber.ZERO;
            FMNumber value = FMNumber.ZERO;
            Vector<Expression> fibExprList = new Vector<Expression>();

            MathContext context = expressionToolset.GetMathContext();
            for (FMNumber i = FMNumber.ZERO; i.compareTo(seriesLength) < 0; i = i.Add(FMNumber.ONE, context)) {
                prev2 = new FMNumber(prev1);
                prev1 = new FMNumber(value);
                if (i.compareTo(FMNumber.ZERO) == 0) {
                    value = FMNumber.ZERO;
                } else if (i.compareTo(FMNumber.ONE) == 0) {
                    value = FMNumber.ONE;
                } else {
                    value = prev1.Add(prev2, context);
                }

                fibExprList.add(new Expression(new Term(new Power(new Factor(value))), TermOperator.NONE));

            }

            return new FMResult(new Factor(fibExprList));
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
