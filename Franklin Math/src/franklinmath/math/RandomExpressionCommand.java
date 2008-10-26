/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package franklinmath.math;

import java.util.Vector;
import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 *
 * @author Allen Jordan
 */
public class RandomExpressionCommand extends Command {

    @Override
    public FMResult Execute(Vector<Equation> args) throws CommandException {
        try {
            Expression randomExpr = ExpressionTools.RandomExpression();
            return new FMResult(randomExpr);
        } catch (ExpressionException ex) {
            throw new CommandException(ex.toString());
        }
    }
}
