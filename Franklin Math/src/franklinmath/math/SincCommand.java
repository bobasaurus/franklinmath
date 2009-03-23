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
 * The sine cardinal function
 * @author Allen Jordan
 */
public class SincCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber number = GetNumberArgument(args, 0);

            if (number.compareTo(FMNumber.ONE) == 0) {
                return new FMResult(new Factor(FMNumber.ONE));
            }
            SymbolicFunction sinFunc = new SymbolicFunction("Sin", args, true);
            Term term = new Term(new Power(new Factor(sinFunc)));
            term = term.AppendPower(new Power(new Factor(number)), PowerOperator.DIVIDE);

            Expression expr = new Expression(term, TermOperator.NONE);

            return new FMResult(expressionToolset.Flatten(expr));
        } catch (Exception ex) {
            try {
                Equation arg0 = args.get(0);
                if (!arg0.IsExpression()) {
                    throw new CommandException(ex.toString(), GetName());
                }

                SymbolicFunction sinFunc = new SymbolicFunction("Sin", args, true);
                Term term = new Term(new Power(new Factor(sinFunc)));
                term = term.AppendPower(new Power(new Factor(arg0.GetLHS())), PowerOperator.DIVIDE);

                return new FMResult(new Expression(term, TermOperator.NONE));
            } catch (Exception ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
