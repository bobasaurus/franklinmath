/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
*/

package franklinmath.expression;

import java.util.*;
import java.math.*;

import franklinmath.util.*;

/**
 * This class represents an immutable math factor (which could be a number, symbol, etc).  
 * @author Allen Jordan
 */
public final class Factor implements LatexOutput {

    final private FactorType type;
    final private FMNumber numValue;
    final private String symbolID;
    final private String stringValue;
    final private Expression nestedExpr;
    final private Vector<Expression> exprList;
    final private SymbolicFunction symbolicFunction;

    public Factor() {
        type = FactorType.EMPTY;
        numValue = null;
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        exprList = null;
        symbolicFunction = null;
    }

    public Factor(long number) {
        type = FactorType.NUMBER;
        numValue = new FMNumber(number);
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        exprList = null;
        symbolicFunction = null;
    }

    public Factor(double number) {
        type = FactorType.NUMBER;
        numValue = new FMNumber(number);
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        exprList = null;
        symbolicFunction = null;
    }

    public Factor(FMNumber number) {
        type = FactorType.NUMBER;
        numValue = number;
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        exprList = null;
        symbolicFunction = null;
    }

    public Factor(String value, boolean isSymbol) {
        if (isSymbol) {
            type = FactorType.SYMBOL;
            symbolID = value;
            numValue = null;
            stringValue = null;
            nestedExpr = null;
            exprList = null;
            symbolicFunction = null;
        } else {
            type = FactorType.STRING;
            stringValue = value;
            numValue = null;
            symbolID = null;
            nestedExpr = null;
            exprList = null;
            symbolicFunction = null;
        }
    }

    public Factor(Expression ex) {
        type = FactorType.NESTED_EXPR;
        nestedExpr = ex;
        numValue = null;
        symbolID = null;
        stringValue = null;
        exprList = null;
        symbolicFunction = null;
    }

    public Factor(Vector<Expression> exList) {
        type = FactorType.EXPR_LIST;
        exprList = (Vector<Expression>) exList.clone();
        numValue = null;
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        symbolicFunction = null;
    }

    public Factor(SymbolicFunction sf) {
        type = FactorType.SYMBOLIC_FUNCTION;
        symbolicFunction = sf;
        numValue = null;
        symbolID = null;
        stringValue = null;
        nestedExpr = null;
        exprList = null;
    }

    public FactorType GetType() {
        return type;
    }

    public boolean IsNumber() {
        return (type == FactorType.NUMBER);
    }

    public boolean IsSymbol() {
        return (type == FactorType.SYMBOL);
    }

    public boolean IsString() {
        return (type == FactorType.STRING);
    }

    public boolean IsNestedExpr() {
        return (type == FactorType.NESTED_EXPR);
    }

    public boolean IsExprList() {
        return (type == FactorType.EXPR_LIST);
    }

    public boolean IsSymbolicFunction() {
        return (type == FactorType.SYMBOLIC_FUNCTION);
    }

    public boolean IsEmpty() {
        return (type == FactorType.EMPTY);
    }

    public FMNumber GetNumber() throws ExpressionException {
        CheckType(FactorType.NUMBER);
        return numValue;
    }

    public String GetSymbol() throws ExpressionException {
        CheckType(FactorType.SYMBOL);
        return symbolID;
    }

    public String GetString() throws ExpressionException {
        CheckType(FactorType.STRING);
        return stringValue;
    }

    public Expression GetNestedExpr() throws ExpressionException {
        CheckType(FactorType.NESTED_EXPR);
        return nestedExpr;
    }

    public Vector<Expression> GetExprList() throws ExpressionException {
        CheckType(FactorType.EXPR_LIST);
        return (Vector<Expression>) exprList.clone();
    }

    public SymbolicFunction GetSymbolicFunction() throws ExpressionException {
        CheckType(FactorType.SYMBOLIC_FUNCTION);
        return symbolicFunction;
    }

    protected void CheckType(FactorType check) throws ExpressionException {
        if (check.compareTo(type) != 0) {
            throw new ExpressionException("Factor type incompatibility.  Given: " + check + " Needed: " + type);
        }
    }

    public String toLatexString() {
        return "";
    }

    /**
     * Fancy string formatting of factors, using the system properties for numbers.  
     * @return  The formatted string.  
     */
    @Override
    public String toString() {
        if (IsNumber()) {
            return numValue.toString();
        }
        if (IsSymbol()) {
            return symbolID;
        }
        if (IsString()) {
            return "\"" + stringValue + "\"";
        }
        if (IsNestedExpr()) {
            return "(" + nestedExpr.toString() + ")";
        }
        if (IsExprList()) {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("{");
            for (int i = 0; i < exprList.size(); i++) {
                if (i != 0) {
                    strBuilder.append(", ");
                }
                strBuilder.append(exprList.get(i).toString());
            }
            strBuilder.append("}");
            return strBuilder.toString();
        }
        if (IsSymbolicFunction()) {
            return symbolicFunction.toString();
        }
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        try {
            Factor compareFactor = (Factor) obj;
            if (compareFactor.type.compareTo(type) != 0) {
                return false;
            }

            if (type.compareTo(FactorType.NUMBER) == 0) {
                return (compareFactor.GetNumber().compareTo(numValue) == 0);
            } else if (type.compareTo(FactorType.SYMBOL) == 0) {
                return compareFactor.GetSymbol().equals(symbolID);
            } else if (type.compareTo(FactorType.STRING) == 0) {
                return compareFactor.GetString().equals(stringValue);
            } else if (type.compareTo(FactorType.NESTED_EXPR) == 0) {
                return compareFactor.GetNestedExpr().equals(nestedExpr);
            } else if (type.compareTo(FactorType.EXPR_LIST) == 0) {
                Vector<Expression> compareExprList = compareFactor.GetExprList();
                for (int i = 0; i < exprList.size(); i++) {
                    if (!compareExprList.get(i).equals(exprList.get(i))) {
                        return false;
                    }
                }
                return true;
            } else if (type.compareTo(FactorType.SYMBOLIC_FUNCTION) == 0) {
                return (compareFactor.GetSymbolicFunction().equals(symbolicFunction));
            }
        } catch (ExpressionException ex) {
            return false;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        if (type.compareTo(FactorType.NUMBER) == 0) {
            hash = hash * primeNumber + ((numValue == null) ? 0 : numValue.hashCode());
        } else if (type.compareTo(FactorType.SYMBOL) == 0) {
            hash = hash * primeNumber + symbolID.hashCode();
        } else if (type.compareTo(FactorType.STRING) == 0) {
            hash = hash * primeNumber + stringValue.hashCode();
        } else if (type.compareTo(FactorType.NESTED_EXPR) == 0) {
            hash = hash * primeNumber + nestedExpr.hashCode();
        } else if (type.compareTo(FactorType.EXPR_LIST) == 0) {
            for (int i = 0; i < exprList.size(); i++) {
                hash = hash * primeNumber + exprList.get(i).hashCode();
            }
        } else if (type.compareTo(FactorType.SYMBOLIC_FUNCTION) == 0) {
            hash = hash * primeNumber + symbolicFunction.hashCode();
        }

        return hash;
    }
}
