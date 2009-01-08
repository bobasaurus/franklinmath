package franklinmath.math;

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;
import java.math.*;

/**
 *
 * @author Allen Jordan
 */
public class FibonacciNumbersCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber seriesLength = GetSingleArgument(args).SingleValue().GetNumber();
            if (seriesLength.doubleValue() == 0) return new FMResult(new Factor(0));
            
            FMNumber prev1 = FMNumber.ZERO;
            FMNumber prev2 = FMNumber.ZERO;
            FMNumber value = FMNumber.ZERO;
            Vector<Expression> fibExprList = new Vector<Expression>();
            
            MathContext context = expressionToolset.GetMathContext();
            for (FMNumber i = FMNumber.ZERO; i.compareTo(seriesLength) < 0; i = i.Add(FMNumber.ONE, context)) {
                prev2 = new FMNumber(prev1);
                prev1 = new FMNumber(value);
                if (i.compareTo(FMNumber.ZERO) == 0) value = FMNumber.ZERO;
                else if (i.compareTo(FMNumber.ONE) == 0) value = FMNumber.ONE;
                else {
                    value = prev1.Add(prev2, context);
                }
                
                fibExprList.add(new Expression(new Term(new Power(new Factor(value))), TermOperator.NONE));
                
            }
            
            return new FMResult(new Factor(fibExprList));
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
