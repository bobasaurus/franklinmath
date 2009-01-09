package franklinmath.math;

import java.util.Vector;

import franklinmath.executor.*;
import franklinmath.expression.*;

/**
 * The sine cardinal function
 * @author Allen Jordan
 */
public class SincCommand extends Command {

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

            if (number.compareTo(FMNumber.ONE) == 0) {
                return new FMResult(new Factor(FMNumber.ONE));
            }
            SymbolicFunction sinFunc = new SymbolicFunction("Sin", args, true);
            Term term = new Term(new Power(new Factor(sinFunc)));
            term = term.AppendPower(new Power(new Factor(number)), PowerOperator.DIVIDE);
            
            Expression expr = new Expression(term, TermOperator.NONE);
            
            return new FMResult(expressionToolset.Flatten(expr));
        } catch (ExpressionException ex) {
            try {
                SingleExpression single = GetSingleArgument(args);
                SymbolicFunction sinFunc = new SymbolicFunction("Sin", args, true);
                Term term = new Term(new Power(new Factor(sinFunc)));
                term = term.AppendPower(new Power(single.SingleValue()), PowerOperator.DIVIDE);
                
                return new FMResult(new Expression(term, TermOperator.NONE));
            } catch (Exception ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
        catch (ExecutionException ex) {
            try {
                SingleExpression single = GetSingleArgument(args);
                SymbolicFunction sinFunc = new SymbolicFunction("Sin", args, true);
                Term term = new Term(new Power(new Factor(sinFunc)));
                term = term.AppendPower(new Power(single.SingleValue()), PowerOperator.DIVIDE);
                
                return new FMResult(new Expression(term, TermOperator.NONE));
            } catch (Exception ex2) {
                throw new CommandException(ex2.toString(), GetName());
            }
        }
    }
}
