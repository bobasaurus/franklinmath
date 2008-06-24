package franklinmath.executor;

import java.util.Vector;
import java.math.*;

import franklinmath.expression.*;

/**
 * Abstract base class for all commands, including function calls.  Follows the command design pattern.  
 * @author Allen Jordan
 */
public abstract class Command {
    protected String name;
    protected boolean isMathFunction;
    
    public abstract FMResult Execute(Vector<Equation> args) throws CommandException;
    
    public String GetName() {
        return name;
    }
    
    public boolean IsMathFunction() {
        return isMathFunction;
    }
    
    protected void CheckArgsLength(Vector<Equation> args, int expectedSize) throws CommandException {
        if (args.size() != expectedSize) throw new CommandException("Invalid function parameter list length", GetName());
    }
    
    protected SingleExpression GetSingleArgument(Vector<Equation> args) throws CommandException {
        SingleExpression single = null;
        
        if (args.size() != 1) throw new CommandException("Invalid function parameter list length", GetName());
        Equation equ = args.get(0);
        if (!equ.IsExpression()) throw new CommandException("Equations are invalid as parameters here", GetName());
        Expression expr = equ.GetLHS();
        
        try {
            single = expr.GetSingle();
        }
        catch (ExpressionException ex) {
            throw new CommandException(ex.toString(), GetName());
        }
        
        return single;
    }
}
