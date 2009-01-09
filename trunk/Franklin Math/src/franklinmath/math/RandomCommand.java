package franklinmath.math;

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;

/**
 * Generate a random number between 0 (inclusive) and 1 (exclusive).  
 * @author Allen Jordan
 */
public class RandomCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 0);
        try {
            return new FMResult(new Factor(new FMNumber(StrictMath.random())));
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
