package franklinmath.math;

import java.util.Vector;
import java.math.*;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class ArcTan2Command extends Command {

    public ArcTan2Command(String functionName, boolean isMathFunction) {
        name = functionName;
        this.isMathFunction = isMathFunction;
    }

    @Override
    public FMResult Execute(Vector<Equation> args) throws CommandException {
        CheckArgsLength(args, 2);
        try {

            Equation equ1 = args.get(0);
            Equation equ2 = args.get(1);

            if ((!equ1.IsExpression()) || (!equ2.IsExpression())) {
                throw new CommandException("Equations are invalid arguments for ArcTan2", GetName());
            }

            SingleExpression single1 = equ1.GetLHS().GetSingle();
            SingleExpression single2 = equ2.GetLHS().GetSingle();

            Factor factor1 = single1.SingleValue();
            Factor factor2 = single2.SingleValue();
            FMNumber number1 = factor1.GetNumber();
            FMNumber number2 = factor2.GetNumber();
            if (single1.IsSingleNegative()) {
                number1 = number1.multiply(new FMNumber(-1));
            }
            if (single2.IsSingleNegative()) {
                number2 = number2.multiply(new FMNumber(-1));
            }

            double result = StrictMath.atan2(number1.doubleValue(), number2.doubleValue());
            return new FMResult(new Factor(new FMNumber(result)));
        } catch (ExpressionException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        } catch (CommandException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw ex;
            }
        }
    }
}
