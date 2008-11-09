package franklinmath.executor;

import java.util.Vector;

import franklinmath.expression.*;

/**
 * Abstract base class for all commands, including function calls.  Follows the command design pattern.  
 * @author Allen Jordan
 */
public abstract class Command {

    protected String name = "";
    protected boolean isMathFunction = false;

    public abstract FMResult Execute(Vector<Equation> args, ExpressionToolset expressionToolset) throws CommandException;

    //Typically used only during initialization.  
    public void SetName(String functionName) {
        name = functionName;
    }

    //Typically used only during initialization.  
    public void SetIsMathFunction(boolean isMathFunc) {
        isMathFunction = isMathFunc;
    }

    public String GetName() {
        return name;
    }

    public boolean IsMathFunction() {
        return isMathFunction;
    }

    protected void CheckArgsLength(Vector<Equation> args, int expectedSize) throws CommandException {
        if (args.size() != expectedSize) {
            throw new CommandException("Invalid function parameter list length", GetName());
        }
    }

    protected SingleExpression GetSingleArgument(Vector<Equation> args) throws CommandException {
        SingleExpression single = null;

        if (args.size() != 1) {
            throw new CommandException("Invalid function parameter list length", GetName());
        }
        Equation equ = args.get(0);
        if (!equ.IsExpression()) {
            throw new CommandException("Equations are invalid as parameters here", GetName());
        }
        Expression expr = equ.GetLHS();

        single = expr.GetSingle();
        if (single == null) {
            throw new CommandException("Problem obtaining single argument", GetName());
        }

        return single;
    }
}
