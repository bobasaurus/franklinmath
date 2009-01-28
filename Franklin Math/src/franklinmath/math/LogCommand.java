package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * Calculates the logarithm with given base of a number
 * @author Allen Jordan
 */
public class LogCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 2);
        try {
            FMNumber base = GetNumberArgument(args, 0);
            FMNumber number = GetNumberArgument(args, 1);
            
            double result = StrictMath.log(number.doubleValue())/StrictMath.log(base.doubleValue());
            return new FMResult(new Factor(new FMNumber(result)));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (Exception ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
