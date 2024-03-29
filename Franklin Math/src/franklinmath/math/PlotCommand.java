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
import franklinmath.util.*;
import franklinmath.plot.*;

/**
 * Command for plotting a function.  
 * @author Allen Jordan
 */
public class PlotCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        try {
            int numArgs = args.size();
            if ((numArgs < 2) || (numArgs > 9)) {
                throw new CommandException("Invalid number of arguments", name);
            }

            //validate the first parameter (function expression)
            Equation firstArg = args.get(0);
            if (!firstArg.IsExpression()) {
                throw new CommandException("Invalid first parameter (expression needed)", name);
            }
            Expression functionExpr = firstArg.GetLHS();

            //validate the second parameter (variable name and range)
            Equation secondArg = args.get(1);
            if (!secondArg.IsExpression()) {
                throw new CommandException("Invalid second parameter (expression needed)", name);
            }
            Expression varAndRangeExpr = secondArg.GetLHS();
            SingleExpression varAndRangeSingle = varAndRangeExpr.GetSingle();
            if (varAndRangeSingle == null) {
                throw new CommandException("Invalid second parameter (single list needed)", name);
            }
            Factor varAndRangeFactor = varAndRangeSingle.SingleValue();
            if (!varAndRangeFactor.IsExprList()) {
                throw new CommandException("Invalid second parameter (list needed)", name);
            }
            Vector<Expression> varAndRangeList = varAndRangeFactor.GetExprList();
            if (varAndRangeList.size() != 3) {
                throw new CommandException("Invalid second parameter (3 element list needed)", name);
            }
            SingleExpression variableNameSingleExpr = varAndRangeList.get(0).GetSingle();
            if (variableNameSingleExpr == null) {
                throw new CommandException("Invalid second parameter (first list element: string needed)", name);
            }
            String variableName = variableNameSingleExpr.SingleValue().GetSymbol();
            FMNumber lowXNum = varAndRangeList.get(1).GetSingleNumber();
            FMNumber highXNum = varAndRangeList.get(2).GetSingleNumber();
            if ((variableName == null) || (lowXNum == null) || (highXNum == null)) {
                throw new CommandException("Invalid second parameter (invalid element type)", name);
            }
            if ((lowXNum.IsImaginary()) || (highXNum.IsImaginary())) {
                throw new CommandException("Invalid second parameter (real numbers needed)", name);
            }
            if (lowXNum.compareTo(highXNum) > 0) {
                throw new CommandException("Invalid second parameter (range inverted)", name);
            }

            //build the series data
            SeriesInfo info = new SeriesInfo(functionExpr, variableName, lowXNum.RealValue().doubleValue(), highXNum.RealValue().doubleValue());
            SeriesData data = new SeriesData(info, expressionToolset);
            Plot plot = new Plot(data);

            return new FMResult(plot.GetPlotImage());
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString());
        }
    }
}
