package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * Square root function.  
 * @author Allen Jordan
 */
public class SqrtCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber number = GetNumberArgument(args, 0);

            Power power = new Power(new Factor(number));
            power = power.AppendFactor(new Factor(0.5));
            Expression resultExpr = expressionToolset.Flatten(new Expression(new Term(power), TermOperator.NONE));

            return new FMResult(resultExpr);
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}