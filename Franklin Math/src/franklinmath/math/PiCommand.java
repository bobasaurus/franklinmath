package franklinmath.math;

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;

/**
 *
 * @author Allen Jordan
 */
public class PiCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 0);
        try {
            return new FMResult(new Factor(new FMNumber(StrictMath.PI)));
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
