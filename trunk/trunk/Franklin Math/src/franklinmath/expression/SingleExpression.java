package franklinmath.expression;

/**
 * A simple class representing an immutable "single" expression that contains only one factor and its sign
 * @author Allen Jordan
 */
public final class SingleExpression {
    final private Factor singleFactor;
    final private boolean isFactorNegative;
    
    public SingleExpression(Factor factor, boolean isNegative) {
        singleFactor = factor;
        isFactorNegative = isNegative;
    }
    
    public Factor SingleValue() {
        return singleFactor;
    }
    
    public boolean IsSingleNegative() {
        return isFactorNegative;
    }
}
