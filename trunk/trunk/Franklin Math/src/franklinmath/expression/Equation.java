package franklinmath.expression;

import franklinmath.util.*;

/**
 * Represents an immutable equation consisting of two expressions separated by an (implied) equals sign.  
 * @author Allen Jordan
 */
public final class Equation implements LatexOutput {

    final private Expression lhs;
    final private Expression rhs;

    public Equation() {
        lhs = new Expression();
        rhs = null;
    }

    public Equation(Expression lhsExpr, Expression rhsExpr) {
        assert (lhsExpr != null);
        lhs = lhsExpr;
        rhs = rhsExpr;
    }

    public Expression GetLHS() {
        return lhs;
    }

    public Expression GetRHS() {
        return rhs;
    }

    public boolean IsExpression() {
        if ((lhs != null) && (rhs == null)) {
            return true;
        }
        return false;
    }

    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        if (lhs != null) {
            strBuilder.append(lhs.toString());
            if (rhs != null) {
                strBuilder.append(" == ");
                strBuilder.append(rhs.toString());
            }
        }
        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        Equation compareEquation = (Equation) obj;
        assert (lhs != null);
        assert compareEquation.GetLHS() != null;

        if (!compareEquation.GetLHS().equals(lhs)) {
            return false;
        }
        if (compareEquation.IsExpression() != IsExpression()) {
            return false;
        }
        if (rhs != null) {
            if (!compareEquation.GetRHS().equals(rhs)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        if (lhs != null) {
            hash = hash * primeNumber + lhs.hashCode();
        }
        if (rhs != null) {
            hash = hash * primeNumber + rhs.hashCode();
        }

        return hash;
    }
}
