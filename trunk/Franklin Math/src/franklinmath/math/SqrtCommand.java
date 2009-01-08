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
            SingleExpression single = GetSingleArgument(args);

            Factor factor = single.SingleValue();
            FMNumber number = factor.GetNumber();
            if (single.IsSingleNegative()) {
                number = number.Negate(expressionToolset.GetMathContext());
            }
            
            Power power = new Power(new Factor(number));
            power = power.AppendFactor(new Factor(0.5));
            Expression resultExpr = expressionToolset.Flatten(new Expression(new Term(power), TermOperator.NONE));
            
            return new FMResult(resultExpr);
        } catch (ExpressionException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        } 
        catch (ExecutionException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
        catch (CommandException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw ex;
            }
        }
    }
}
