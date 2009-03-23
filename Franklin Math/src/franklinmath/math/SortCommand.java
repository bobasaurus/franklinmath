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

import java.util.*;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * Sort the numbers in a list.  
 * @author Allen Jordan
 */
public class SortCommand extends Command {

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

            //build a list of numbers from the expressions
            Vector<FMNumber> numberList = new Vector<FMNumber>();
            for (int i=0; i<exprList.size(); i++) {
                FMNumber value = exprList.get(i).GetSingleNumber();
                numberList.add(value);
            }

            //sort the list of numbers
            Collections.sort(numberList);
            
            //build a list of expressions from the sorted number list
            Vector<Expression> resultExprList = new Vector<Expression>();
            for (int i=0; i<numberList.size(); i++) {
                resultExprList.add(new Expression(new Term(new Power(new Factor(numberList.get(i)))), TermOperator.NONE));
            }
            
            return new FMResult(new Factor(resultExprList));
        }
        catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
