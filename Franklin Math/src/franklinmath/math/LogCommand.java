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
            Equation argEqu1 = args.get(0);
            Equation argEqu2 = args.get(1);
            if ((!argEqu1.IsExpression()) || (!argEqu2.IsExpression())) throw new ExecutionException("Invalid argument: equation parameters not allowed");
            FMNumber arg1 = argEqu1.GetLHS().GetSingleNumber();
            FMNumber arg2 = argEqu2.GetLHS().GetSingleNumber();
            
            double result = StrictMath.log(arg2.doubleValue())/StrictMath.log(arg1.doubleValue());
            
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
