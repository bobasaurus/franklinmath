package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class RadiansToDegreesCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            FMNumber value = GetNumberArgument(args, 0);
            
            Term term = new Term(new Power(new Factor(value)));
            term = term.AppendPower(new Power(new Factor(180)), PowerOperator.MULTIPLY);
            term = term.AppendPower(new Power(new Factor(new SymbolicFunction("Pi", new Vector<Equation>(), true))), PowerOperator.DIVIDE);
            
            Expression result = expressionToolset.Flatten(new Expression(term, TermOperator.NONE));
            FMNumber resultNumber = result.GetSingleNumber();
            return new FMResult(new Factor(resultNumber));
        } catch (Exception ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
