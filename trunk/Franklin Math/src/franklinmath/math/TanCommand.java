package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class TanCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            SingleExpression single = GetSingleArgument(args);

            Factor factor = single.SingleValue();
            FMNumber number = factor.GetNumber();
            if (single.IsSingleNegative()) {
                number = number.Negate(expressionToolset.GetMathContext());
            }
            double result = StrictMath.tan(number.doubleValue());
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
