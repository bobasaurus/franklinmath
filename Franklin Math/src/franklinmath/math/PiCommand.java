package franklinmath.math;

import franklinmath.executor.*;
import franklinmath.expression.*;
import java.util.Vector;
import java.math.*;

/**
 *
 * @author Allen Jordan
 */
public class PiCommand extends Command {
    public PiCommand(String functionName, boolean isMathFunction) {
        name = functionName;
        this.isMathFunction = isMathFunction;
    }
    
    @Override public FMResult Execute(Vector<Equation> args) throws CommandException {
        CheckArgsLength(args, 0);
        try {
            return new FMResult(new Factor(new BigDecimal(StrictMath.PI)));
        }
        catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
    }
}
