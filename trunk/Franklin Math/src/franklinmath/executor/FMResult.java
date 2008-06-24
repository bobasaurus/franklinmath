package franklinmath.executor;

import franklinmath.expression.*;
import java.awt.Image;

/**
 *
 * @author Allen Jordan
 */
public class FMResult {
    protected FMResultType type;
    protected Expression expression;
    protected Equation equation;
    protected Image image;
    protected String string;
    
    public FMResult() {
    }
    
    public FMResult(Factor factor) throws ExpressionException {
        type = FMResultType.EXPRESSION;
        expression = new Expression(new Term(new Power(factor)), TermOperator.NONE);
    }
    public FMResult(Expression ex) {
        type = FMResultType.EXPRESSION;
        expression = ex;
    }
    public FMResult(Equation eq) {
        type = FMResultType.EQUATION;
        equation = eq;
    }
    public FMResult(Image img) {
        type = FMResultType.IMAGE;
        image = img;
    }
    public FMResult(String str) {
        type = FMResultType.STRING;
        string = str;
    }
    
    public FMResultType GetType() {
        return type;
    }
    
    public boolean IsExpression() {
        return (type.compareTo(FMResultType.EXPRESSION) == 0);
    }
    
    public boolean IsEquation() {
        return (type.compareTo(FMResultType.EQUATION) == 0);
    }
    
    public boolean IsImage() {
        return (type.compareTo(FMResultType.IMAGE) == 0);
    }
    
    public boolean IsString() {
        return (type.compareTo(FMResultType.STRING) == 0);
    }
    
    public Expression GetExpression() throws ExecutionException {
        CheckType(FMResultType.EXPRESSION);
        return expression;
    }
    
    public Equation GetEquation() throws ExecutionException {
        CheckType(FMResultType.EQUATION);
        return equation;
    }
    
    public Image GetImage() throws ExecutionException {
        CheckType(FMResultType.IMAGE);
        return image;
    }
    
    public String GetString() throws ExecutionException {
        CheckType(FMResultType.STRING);
        return string;
    }
    
    protected void Clear() {
        expression = null;
        equation = null;
        image = null;
        string = "";
    }
    
    public void SetExpression(Expression ex) {
        Clear();
        type = FMResultType.EXPRESSION;
        expression = ex;
    }
    
    public void SetEquation(Equation eq) {
        Clear();
        type = FMResultType.EQUATION;
        equation = eq;
    }
    
    public void SetImage(Image img) {
        Clear();
        type = FMResultType.IMAGE;
        image = img;
    }
    
    public void SetString(String str) {
        Clear();
        type = FMResultType.STRING;
        string = str;
    }
    
    protected void CheckType(FMResultType check) throws ExecutionException {
        if (type.compareTo(check) != 0) throw new ExecutionException("Result type incompatibility.  Given: " + check + " Needed: " + type);
    }
}
