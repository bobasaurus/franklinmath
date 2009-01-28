package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class ArcTan2Command extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 2);
        try {
            FMNumber number1 = GetNumberArgument(args, 0);
            FMNumber number2 = GetNumberArgument(args, 1);
            
            double result = StrictMath.atan2(number1.doubleValue(), number2.doubleValue());
            return new FMResult(new Factor(new FMNumber(result)));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
