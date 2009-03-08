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
 * This class represents an immutable math expression.  Terms are added or subtracted.  
 * @author Allen Jordan
 */
public final class Expression implements LatexOutput {

    final private Vector<Term> termList;
    final private Vector<TermOperator> operatorList;

    public Expression() {
        termList = new Vector<Term>();
        operatorList = new Vector<TermOperator>();
    }

    /**
     * Construct an expression from an existing term (and operator).  
     */
    public Expression(Term term, TermOperator op) {
        assert ((term != null) && (op != null));
        termList = new Vector<Term>();
        operatorList = new Vector<TermOperator>();
        if (op == TermOperator.ADD) {
            op = TermOperator.NONE;
        }
        termList.add(term);
        operatorList.add(op);
    }

    /**
     * Construct an expression by copying existing term and operator lists.  
     */
    public Expression(Vector<Term> inputTermList, Vector<TermOperator> inputOperatorList) {
        assert ((inputTermList != null) && (inputOperatorList != null));
        assert (inputTermList.size() == inputOperatorList.size());
        termList = (Vector<Term>) inputTermList.clone();
        operatorList = (Vector<TermOperator>) inputOperatorList.clone();
    }

    /**
     * Construct an expression by copying existing term and operator lists, then append on a new term (and operator).  
     */
    public Expression(Vector<Term> inputTermList, Vector<TermOperator> inputOperatorList, Term appendTerm, TermOperator appendOp) {
        assert ((inputTermList != null) && (inputOperatorList != null) && (appendTerm != null) && (appendOp != null));
        assert (inputTermList.size() == inputOperatorList.size());
        termList = (Vector<Term>) inputTermList.clone();
        operatorList = (Vector<TermOperator>) inputOperatorList.clone();
        if (termList.size() == 0) {
            if (appendOp == TermOperator.ADD) {
                appendOp = TermOperator.NONE;
            }
        } else {
            if (appendOp == TermOperator.NONE) {
                appendOp = TermOperator.ADD;
            }
        }
        termList.add(appendTerm);
        operatorList.add(appendOp);
    }

    /**
     * Construct an expression by copying existing term and operator lists, then either setting or inserting a new term (and operator).  
     */
    public Expression(Vector<Term> inputTermList, Vector<TermOperator> inputOperatorList, int index, Term newTerm, TermOperator newOp, boolean isInsertion) {
        assert ((inputTermList != null) && (inputOperatorList != null) && (newTerm != null) && (newOp != null));
        assert (inputTermList.size() == inputOperatorList.size());
        assert ((index >= 0) && (index < inputTermList.size()));
        termList = (Vector<Term>) inputTermList.clone();
        operatorList = (Vector<TermOperator>) inputOperatorList.clone();

        if (termList.size() == 0) {
            if (newOp == TermOperator.ADD) {
                newOp = TermOperator.NONE;
            }
        }
        if (index == 0) {
            if (newOp == TermOperator.ADD) {
                newOp = TermOperator.NONE;
            }
        } else {
            if (newOp == TermOperator.NONE) {
                newOp = TermOperator.ADD;
            }
        }

        if (isInsertion) {
            termList.insertElementAt(newTerm, index);
            operatorList.insertElementAt(newOp, index);
        } else {
            termList.set(index, newTerm);
            operatorList.set(index, newOp);
        }
    }

    /**
     * Append a term to the expression.  
     * @param term      The term to append.  
     * @param operator  The term operator to append.  
     * @return          The resulting expression with the appended term and operator.  
     */
    public Expression AppendTerm(Term term, TermOperator operator) {
        if (termList.size() == 0) {
            if (operator == TermOperator.ADD) {
                operator = TermOperator.NONE;
            }
        } else {
            if (operator == TermOperator.NONE) {
                operator = TermOperator.ADD;
            }
        }
        return new Expression(termList, operatorList, term, operator);
    }

    /**
     * Remove a term from the expression
     * @param index     The index of the term to remove.  
     * @return          The resulting expression with the term removed.  
     * @throws franklinmath.expression.ExpressionException
     */
    public Expression RemoveTerm(int index) throws ExpressionException {
        if ((index < 0) || (index >= termList.size())) {
            throw new ExpressionException("Removal index out of range");
        }
        //do a slightly expensive (two clones per list thanks to Expression constructor) remove since I don't want to write a term removal constructor
        Vector<Term> termListCopy = (Vector<Term>) termList.clone();
        Vector<TermOperator> opListCopy = (Vector<TermOperator>) operatorList.clone();
        termListCopy.remove(index);
        opListCopy.remove(index);
        return new Expression(termListCopy, opListCopy);
    }

    /**
     * Get the number of terms in the expression.  
     * @return      The number of terms.  
     */
    public int NumTerms() {
        return termList.size();
    }

    /**
     * Find a term in the expression.  
     * @param term      The term to find.  
     * @return          The (first) index of the located term.  
     */
    public int FindTerm(Term term) {
        return termList.indexOf(term);
    }

    public Term GetTerm(int index) throws ExpressionException {
        if ((index < 0) || (index >= termList.size())) {
            throw new ExpressionException("Retrieval index out of range");
        }
        return termList.get(index);
    }

    public TermOperator GetOperator(int index) throws ExpressionException {
        if ((index < 0) || (index >= operatorList.size())) {
            throw new ExpressionException("Operator retrieval index out of range");
        }
        return operatorList.get(index);
    }

    public Vector<Term> GetTerms() {
        return (Vector<Term>) termList.clone();
    }

    public Vector<TermOperator> GetOperators() {
        return (Vector<TermOperator>) operatorList.clone();
    }

    public FMNumber GetSingleNumber() throws ExpressionException {
        SingleExpression single = GetSingle();
        if (single == null) return null;
        
        Factor singleValue = single.SingleValue();
        if (!singleValue.IsNumber()) return null;
        
        FMNumber number = singleValue.GetNumber();
        if (single.IsSingleNegative()) {
            number = number.Negate(new MathContext(FMProperties.GetDisplayPrecision(), FMProperties.GetRoundingMode()));
        }
        return number;
    }

    public String GetSingleString() throws ExpressionException {
        SingleExpression single = GetSingle();
        if (single == null) return null;
        
        Factor singleValue = single.SingleValue();
        if (!singleValue.IsString()) return null;
        
        String result = singleValue.GetString();
        if (single.IsSingleNegative()) {
            throw new ExpressionException("Negative character string");
        }
        return result;
    }

    public Expression Add(Expression ex) throws ExpressionException {
        if (ex == null) {
            throw new ExpressionException("Invalid expression for addition");
        }

        Vector<Term> termListCopy = (Vector<Term>) termList.clone();
        Vector<TermOperator> opListCopy = (Vector<TermOperator>) operatorList.clone();

        int numInputTerms = ex.NumTerms();
        for (int i = 0; i < numInputTerms; i++) {
            Term term = ex.GetTerm(i);
            TermOperator op = ex.GetOperator(i);
            if (i == 0) {
                if (op == TermOperator.ADD) {
                    op = TermOperator.NONE;
                }
            } else {
                if (op == TermOperator.NONE) {
                    op = TermOperator.ADD;
                }
            }
            termListCopy.add(term);
            opListCopy.add(op);
        }

        return new Expression(termListCopy, opListCopy);
    }

    public Expression Add(Term term) throws ExpressionException {
        if (term == null) {
            throw new ExpressionException("Invalid term for addition");
        }

        return new Expression(termList, operatorList, term, TermOperator.ADD);
    }

    public Expression Subtract(Expression ex) throws ExpressionException {
        if (ex == null) {
            throw new ExpressionException("Invalid expression for subtraction");
        }

        Vector<Term> termListCopy = (Vector<Term>) termList.clone();
        Vector<TermOperator> opListCopy = (Vector<TermOperator>) operatorList.clone();

        int numInputTerms = ex.NumTerms();
        for (int i = 0; i < numInputTerms; i++) {
            Term term = ex.GetTerm(i);
            TermOperator op = ex.GetOperator(i);
            if (op == TermOperator.SUBTRACT) {
                op = TermOperator.ADD;
            } else {
                op = TermOperator.SUBTRACT;
            }

            termListCopy.add(term);
            opListCopy.add(op);
        }

        return new Expression(termListCopy, opListCopy);
    }

    public Expression Subtract(Term term) throws ExpressionException {
        if (term == null) {
            throw new ExpressionException("Invalid term for subtraction");
        }

        return new Expression(termList, operatorList, term, TermOperator.SUBTRACT);
    }

    /**
     * If this expression represents a single factor, return it.
     * @return  The single factor.
     * @throws franklinmath.expression.ExpressionException
     */
    public SingleExpression GetSingle() {
        if (termList.size() != 1) {
            return null;
        }
        Term term = termList.get(0);
        Factor singleFactor = term.GetSingleFactor();
        if (singleFactor == null) {
            return null;
        }
        SingleExpression single = new SingleExpression(singleFactor, (operatorList.get(0) == TermOperator.SUBTRACT) ? true : false);
        return single;
    }

    /**
     * Replace each occurance of a symbol with an expression.
     * @param symbol    The symbol to replace.
     * @param expr      The expression to replace each matching symbol.
     * @throws franklinmath.expression.ExpressionException
     */
    //todo: make this immutable
    public Expression Replace(String symbol, Expression expr) throws ExpressionException {

        Vector<Term> termListCopy = GetTerms();
        ListIterator<Term> termIterator = termListCopy.listIterator();
        Expression newExpr = null;
        while (termIterator.hasNext()) {
            Term term = termIterator.next();
            Vector<Power> powerListCopy = term.GetPowers();
            ListIterator<Power> powerIterator = powerListCopy.listIterator();

            while (powerIterator.hasNext()) {
                Power power = powerIterator.next();
                Vector<Factor> factorListCopy = power.GetFactors();
                ListIterator<Factor> factorIterator = factorListCopy.listIterator();

                while (factorIterator.hasNext()) {
                    Factor factor = factorIterator.next();
                    if (factor.IsSymbol()) {
                        if (factor.GetSymbol().equals(symbol)) {
                            factorIterator.set(new Factor(expr));
                        }
                    } else if (factor.IsNestedExpr()) {
                        Expression nested = factor.GetNestedExpr();
                        factorIterator.set(new Factor(nested.Replace(symbol, expr)));
                    } else if (factor.IsExprList()) {
                        Vector<Expression> list = factor.GetExprList();
                        for (int l = 0; l < list.size(); l++) {
                            Expression result = list.get(l).Replace(symbol, expr);
                            list.set(l, result);
                        }
                        factorIterator.set(new Factor(list));
                    } else if (factor.IsSymbolicFunction()) {
                        SymbolicFunction symFunc = factor.GetSymbolicFunction();
                        Vector<Equation> equList = symFunc.GetParamList();
                        for (int l = 0; l < equList.size(); l++) {
                            Equation equ = equList.get(l);
                            Expression lhs = equ.GetLHS();
                            Expression rhs = equ.GetRHS();
                            if (lhs != null) {
                                lhs = lhs.Replace(symbol, expr);
                            }
                            if (rhs != null) {
                                rhs = rhs.Replace(symbol, expr);
                            }
                            equ = new Equation(lhs, rhs);
                            equList.set(l, equ);
                        }
                        factorIterator.set(new Factor(new SymbolicFunction(symFunc.GetName(), equList, symFunc.IsMathFunction())));
                    }
                }
                powerIterator.set(new Power(factorListCopy));
            }
            termIterator.set(new Term(powerListCopy, term.GetOperators()));
        }
        newExpr = new Expression(termListCopy, operatorList);

        return newExpr;
    }

    public Expression ReplaceTerm(int index, Term newTerm, TermOperator newOperator) throws ExpressionException {
        if ((index < 0) || (index >= termList.size())) {
            throw new ExpressionException("Replacement index out of range");
        }

        return new Expression(termList, operatorList, index, newTerm, newOperator, false);
    }

    public boolean ContainsSymbol(String symbol) throws ExpressionException {
        for (int i = 0; i < termList.size(); i++) {
            Term term = termList.get(i);
            for (int j = 0; j < term.NumPowers(); j++) {
                Power power = term.GetPower(j);
                for (int k = 0; k < power.NumFactors(); k++) {
                    Factor factor = power.GetFactor(k);
                    if (factor.IsSymbol()) {
                        if (factor.GetSymbol().equals(symbol)) {
                            return true;
                        }
                    } else if (factor.IsSymbolicFunction()) {
                        SymbolicFunction sf = factor.GetSymbolicFunction();
                        ListIterator argsIterator = sf.GetParamList().listIterator();
                        while (argsIterator.hasNext()) {
                            Equation equ = (Equation) argsIterator.next();
                            if (equ.GetLHS() != null) {
                                if (equ.GetLHS().ContainsSymbol(symbol)) {
                                    return true;
                                }
                            }
                            if (equ.GetRHS() != null) {
                                if (equ.GetRHS().ContainsSymbol(symbol)) {
                                    return true;
                                }
                            }
                        }
                    } else if (factor.IsExprList()) {
                        ListIterator exprIterator = factor.GetExprList().listIterator();
                        while (exprIterator.hasNext()) {
                            Expression expr = (Expression) exprIterator.next();
                            if (expr.ContainsSymbol(symbol)) {
                                return true;
                            }
                        }
                    } else if (factor.IsNestedExpr()) {
                        if (factor.GetNestedExpr().ContainsSymbol(symbol)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //todo:  implement this
    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        int termListSize = termList.size();
        for (int i = 0; i < termListSize; i++) {
            TermOperator termOp = operatorList.get(i);
            Term term = termList.get(i);

            if (i != 0) {
                strBuilder.append(" ");
            }
            if (termOp.compareTo(TermOperator.ADD) == 0) {
                if (i != 0) {
                    strBuilder.append("+");
                }
            } else if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                strBuilder.append("-");
            }
            //append a space if necessary
            if (i != 0) {
                strBuilder.append(" ");
            }

            strBuilder.append(term.toString());
        }
        return strBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        //check for equal reference values
        if (this == obj) {
            return true;
        }

        try {
            Expression compareExpression = (Expression) obj;
            Vector<Term> compareTermList = compareExpression.GetTerms();
            Vector<TermOperator> compareOperatorList = compareExpression.GetOperators();

            assert termList.size() == operatorList.size();
            assert compareTermList.size() == compareOperatorList.size();

            int numTerms = termList.size();
            int numCompareTerms = compareExpression.NumTerms();

            //check for a match (regardless of term order)
            Hashtable<Term, Integer> termAddTable = new Hashtable<Term, Integer>();
            Hashtable<Term, Integer> termSubtractTable = new Hashtable<Term, Integer>();
            for (int i = 0; i < numTerms; i++) {
                Term term = termList.get(i);
                TermOperator termOp = operatorList.get(i);
                if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                    Integer currentValue = termSubtractTable.get(term);
                    if (currentValue == null) {
                        currentValue = 0;
                    }
                    currentValue++;
                    termSubtractTable.put(term, currentValue);
                } else {
                    Integer currentValue = termAddTable.get(term);
                    if (currentValue == null) {
                        currentValue = 0;
                    }
                    currentValue++;
                    termAddTable.put(term, currentValue);
                }
            }
            for (int i = 0; i < numCompareTerms; i++) {
                Term term = compareExpression.GetTerm(i);
                TermOperator termOp = compareExpression.GetOperator(i);
                if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                    Integer currentValue = termSubtractTable.get(term);
                    if (currentValue == null) {
                        return false;
                    }
                    currentValue--;
                    termSubtractTable.put(term, currentValue);
                } else {
                    Integer currentValue = termAddTable.get(term);
                    if (currentValue == null) {
                        return false;
                    }
                    currentValue--;
                    termAddTable.put(term, currentValue);
                }

            }
            Enumeration<Term> addKeyEnumeration = termAddTable.keys();
            while (addKeyEnumeration.hasMoreElements()) {
                if (termAddTable.get(addKeyEnumeration.nextElement()) != 0) {
                    return false;
                }
            }
            Enumeration<Term> subtractKeyEnumeration = termSubtractTable.keys();
            while (subtractKeyEnumeration.hasMoreElements()) {
                if (termSubtractTable.get(subtractKeyEnumeration.nextElement()) != 0) {
                    return false;
                }
            }
            return true;

        } catch (ExpressionException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int primeNumber = 7;
        int hash = 1;

        assert termList.size() == operatorList.size();

        //make sure that the term order does not matter in hash code generation
        int numTerms = termList.size();
        for (int i = 0; i < numTerms; i++) {
            hash += termList.get(i).hashCode() * primeNumber;
            if (operatorList.get(i).compareTo(TermOperator.SUBTRACT) == 0) {
                hash++;
            }
        }

        return hash;
    }
}
