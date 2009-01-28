package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class ModCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 2);
        try {
            Equation arg0 = args.get(0);
            Equation arg1 = args.get(1);
            if ((!arg0.IsExpression()) || (!arg1.IsExpression())) throw new Exception("Equation arguments invalid for this function");
            
            FMNumber firstNumber = arg0.GetLHS().GetSingleNumber();
            FMNumber secondNumber = arg1.GetLHS().GetSingleNumber();
            
            return new FMResult(new Factor(new FMNumber(firstNumber.doubleValue() % secondNumber.doubleValue())));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.getMessage(), GetName());
            }
        }
    }
}
