package franklinmath.math;

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;

/**
 * Calculate the golden ratio using the equation (1+5^.5)/2
 * @author Allen Jordan
 */
public class GoldenRatioCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 0);
        try {

            Expression expr = new Expression(new Term(new Power(new Factor(1))), TermOperator.NONE);
            Power power = new Power(new Factor(5));
            power = power.AppendFactor(new Factor(0.5));
            expr = expr.Add(new Term(power));

            Factor nestedExpr = new Factor(expr);
            Term term = new Term(new Power(nestedExpr));
            term = term.AppendPower(new Power(new Factor(2)), PowerOperator.DIVIDE);

            Expression finalExpr = new Expression(term, TermOperator.NONE);
            Expression flattenedResult = expressionToolset.Flatten(finalExpr);

            return new FMResult(flattenedResult);
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        } catch (ExecutionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
