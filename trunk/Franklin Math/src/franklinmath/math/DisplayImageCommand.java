package franklinmath.math;

import java.util.Vector;
import java.awt.*;
import javax.swing.*;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * Display an image specified by a URL
 * @author Allen Jordan
 */
public class DisplayImageCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException {
        CheckArgsLength(args, 1);
        try {
            SingleExpression single = GetSingleArgument(args);
            String imageURL = single.SingleValue().GetString();
            Image image = new ImageIcon(imageURL).getImage();
            return new FMResult(image);
        } catch (ExpressionException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        } catch (CommandException ex) {
            try {
                return new FMResult(new Factor(new SymbolicFunction(GetName(), args, isMathFunction)));
            } catch (ExpressionException ex2) {
                throw ex;
            }
        }
    }
}
