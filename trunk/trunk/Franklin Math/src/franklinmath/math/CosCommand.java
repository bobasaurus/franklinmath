package franklinmath.math;

import java.util.Vector;
import java.math.*;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class CosCommand extends Command {

    public CosCommand(String functionName, boolean isMathFunction) {
        name = functionName;
        this.isMathFunction = isMathFunction;
    }

    @Override
    public FMResult Execute(Vector<Equation> args) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            SingleExpression single = GetSingleArgument(args);

            Factor factor = single.SingleValue();
            BigDecimal number = factor.GetNumber();
            if (single.IsSingleNegative()) {
                number = number.multiply(new BigDecimal(-1));
            }

            double result = StrictMath.cos(number.doubleValue());
            return new FMResult(new Factor(new BigDecimal(result)));
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
