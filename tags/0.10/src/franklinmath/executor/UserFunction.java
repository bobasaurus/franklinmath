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
 *
 * @author Allen Jordan
 */
public class UserFunction extends Command {

    public UserFunction() {
        name = "Unknown user function";
    }

    @Override
    public String GetName() {
        return name;
    }
    protected Expression expr = new Expression();
    protected Vector<String> symbolArgs = new Vector<String>();

//    public UserFunction(Expression expression, Vector<String> symbolArguments) {
//        expr = expression;
//        symbolArgs = symbolArguments;
//    }
    public UserFunction(Expression expression, Vector<Equation> symbolArguments) throws ExpressionException {
        expr = expression;

        //check for valid arguments
        for (int i = 0; i < symbolArguments.size(); i++) {
            Equation equ = symbolArguments.get(i);
            if (!equ.IsExpression()) {
                throw new ExpressionException("Invalid function definition argument");
            }
            Expression exp = equ.GetLHS();
            SingleExpression single = exp.GetSingle();
            if (single == null) {
                throw new ExpressionException("Invalid symbol for creating user function: " + exp.toString());
            }
            String symbol = single.SingleValue().GetSymbol();
            if (single.IsSingleNegative()) {
                throw new ExpressionException("Symbols arguments can't be zero when defining a function");
            }

            //make sure that the function's RHS expression contains all the specified symbols ( helps prevent confusion over f[x_] = x )
            if (!expr.ContainsSymbol(symbol)) {
                throw new ExpressionException("RHS expression does not contain the variable: " + symbol);
            }

            symbolArgs.add(symbol);
        }
    }

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        if (args.size() != symbolArgs.size()) {
            throw new CommandException("User Function argument count mismatch");
        }

        try {
            Expression resultExpr = expr;
            for (int i = 0; i < args.size(); i++) {
                String symbol = symbolArgs.get(i);
                Equation equ = args.get(i);
                if (!equ.IsExpression()) {
                    throw new CommandException("Equations are invalid arguments to user functions");
                }
                Expression lhsExpr = equ.GetLHS();
                resultExpr = resultExpr.Replace(symbol, lhsExpr);
            }

            return new FMResult(resultExpr);
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString());
        }
    }
}
