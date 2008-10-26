package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class PlotCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args) throws CommandException {
        int numArgs = args.size();
        if ((numArgs < 2) || (numArgs > 9)) throw new CommandException("Invalid number of arguments", name);
        
        
        
        return new FMResult("Plot Command Called :P");
    }
}
