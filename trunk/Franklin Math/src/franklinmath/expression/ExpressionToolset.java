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

import java.math.*;
import java.util.*;

import franklinmath.executor.*;
import franklinmath.util.*;

/**
 * This class provides a set of tools to help with expressions and equations.  
 * @author Allen Jordan
 */
public class ExpressionToolset {

    //limit the problem recursion depth
    protected int depthLimit = 256;
    //limit the problem looping breadth
    protected int breadthLimit = 32768;
    protected MathContext context;
    protected LookupTable lookupTable;
    protected FunctionTable userFunctionTable,  systemFunctionTable;
    protected Vector<FMResult> resultList;

    public ExpressionToolset() {
        context = new MathContext(FMProperties.GetPrecision(), FMProperties.GetRoundingMode());
        lookupTable = null;
        userFunctionTable = null;
        systemFunctionTable = null;
        resultList = null;
    }

    public ExpressionToolset(MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable systemFunctionTable, Vector<FMResult> resultList) {
        this.context = context;
        this.lookupTable = lookupTable;
        this.userFunctionTable = userFunctionTable;
        this.systemFunctionTable = systemFunctionTable;
        this.resultList = resultList;
    }

    public MathContext GetMathContext() {
        return context;
    }

    /**
     * Generate a random expression (possibly for use with fuzzing)
     * @return Return the resulting random expression
     */
    public Expression RandomExpression() throws ExpressionException {
        Random rand = new Random();
        Expression result = new Expression();
        int numTerms = rand.nextInt(5) + 1;
        for (int i = 0; i < numTerms; i++) {
            Term term = new Term();
            TermOperator termOp = TermOperator.ADD;
            if (rand.nextBoolean()) {
                termOp = TermOperator.SUBTRACT;
            }

            int numPowers = rand.nextInt(3) + 1;
            for (int j = 0; j < numPowers; j++) {
                Power power = new Power();
                PowerOperator powerOp = PowerOperator.MULTIPLY;
                if (rand.nextBoolean() && (j != 0)) {
                    powerOp = PowerOperator.DIVIDE;
                }

                int numFactors = rand.nextInt(3) + 1;
                for (int k = 0; k < numFactors; k++) {
                    Factor factor = null;

                    int factorType = rand.nextInt(4) + 1;
                    //random integer
                    if (factorType == 1) {
                        factor = new Factor(StrictMath.abs(rand.nextInt()));
                    } //random double
                    else if (factorType == 2) {
                        factor = new Factor(Math.abs(rand.nextDouble() * rand.nextInt(100)));
                    } //random symbol
                    else if (factorType == 3) {
                        //generate a random symbol (without underscores, for now)
                        char A = 'A';
                        char a = 'a';
                        int symbolLength = rand.nextInt(3) + 1;
                        StringBuilder builder = new StringBuilder();
                        for (int symbolCount = 0; symbolCount < symbolLength; symbolCount++) {
                            char big = (char) (rand.nextInt(26) + ((int) A));
                            char small = (char) (rand.nextInt(26) + ((int) a));
                            if (rand.nextBoolean()) {
                                builder.append(big);
                            } else {
                                builder.append(small);
                            }
                        }

                        factor = new Factor(builder.toString(), true);
                    } //random function
                    else if (factorType == 4) {
                        Vector<Equation> args = new Vector<Equation>();
                        String randomSymbol = "" + ((char) (rand.nextInt(26) + ((int) 'a')));
                        args.add(new Equation(new Expression(new Term(new Power(new Factor(randomSymbol, true))), TermOperator.NONE), null));
                        int functionNum = rand.nextInt(4) + 1;
                        if (functionNum == 1) {
                            factor = new Factor(new SymbolicFunction("Sin", args, true));
                        } else if (functionNum == 2) {
                            factor = new Factor(new SymbolicFunction("Cos", args, true));
                        } else if (functionNum == 3) {
                            factor = new Factor(new SymbolicFunction("Tan", args, true));
                        } else if (functionNum == 4) {
                            factor = new Factor(new SymbolicFunction("Pi", new Vector<Equation>(), true));
                        }
                    }

                    if (factor != null) {
                        power = power.AppendFactor(factor);
                    }
                }

                term = term.AppendPower(power, powerOp);
            }

            result = result.AppendTerm(term, termOp);
        }
        return result;
    }

    /**
     * Flatten a given equation by performing some arithmetic reduction, both symbolic and numerical.  
     * @param mainEqu               The equation to flatten.  
     * @param context               The math context, with rounding mode and precision, for performing numerical operations.  
     * @param lookupTable           The lookup table for both user-defined and system variables.  
     * @param userFunctionTable     The function table containing user functions.  
     * @param functionTable         The function table containing system functions.  
     * @return                      Returns the flattened equation.  
     * @throws franklinmath.expression.ExpressionException
     * @throws franklinmath.executor.ExecutionException
     */
    public Equation Flatten(Equation mainEqu) throws ExpressionException, ExecutionException {
        return FlattenEquation(mainEqu, 0);
    }

    /**
     * Flatten a given expression by performing some arithmetic reduction, both symbolic and numerical.  
     * @param mainExpr              The expression to flatten.  
     * @param context               The math context, with rounding mode and precision, for performing numerical operations.  
     * @param lookupTable           The lookup table for both user-defined and system variables.  
     * @param userFunctionTable     The function table containing user functions.  
     * @param functionTable         The function table containing system functions.  
     * @return                      Returns the flattened expression.  
     * @throws franklinmath.expression.ExpressionException
     * @throws franklinmath.executor.ExecutionException
     */
    public Expression Flatten(Expression mainExpr) throws ExpressionException, ExecutionException {
        return FlattenExpression(mainExpr, 0);
    }

    protected Equation FlattenEquation(Equation inEqu, int depth) throws ExpressionException, ExecutionException {
        assert inEqu != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }

        if (inEqu.IsExpression()) {
            Expression lhs = inEqu.GetLHS();
            assert lhs != null;
            Expression exprFlat = FlattenExpression(lhs, depth);
            return new Equation(exprFlat, null);
        } else {
            Expression lhs = inEqu.GetLHS();
            Expression rhs = inEqu.GetRHS();
            assert lhs != null;
            assert rhs != null;

            Expression lhsFlat = FlattenExpression(lhs, depth);
            Expression rhsFlat = FlattenExpression(rhs, depth);

            return new Equation(lhsFlat, rhsFlat);
        }
    }

    protected Expression FlattenExpression(Expression inExpr, int depth) throws ExpressionException, ExecutionException {
        assert inExpr != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        if (context == null) {
            context = MathContext.DECIMAL128;
        }

        //remove unnecessary nesting
        Vector<Term> termListCopy = inExpr.GetTerms();
        ListIterator<Term> termIterator = termListCopy.listIterator();
        Vector<TermOperator> operatorListCopy = inExpr.GetOperators();
        ListIterator<TermOperator> termOpIterator = operatorListCopy.listIterator();
        while (termIterator.hasNext()) {
            //retrieve and flatten the next term
            Term term = termIterator.next();
            term = FlattenTerm(term, depth);
            termIterator.set(term);
            termIterator.previous();
            termIterator.next();
            TermOperator termOp = termOpIterator.next();

            Factor single = term.GetSingleFactor();
            if (single != null) {
                //if we have redundant nesting, remove it
                if (single.IsNestedExpr()) {
                    termIterator.remove();
                    termOpIterator.remove();
                    Expression nestedExpr = single.GetNestedExpr();
                    int numNestedTerms = nestedExpr.NumTerms();
                    for (int i = 0; i < numNestedTerms; i++) {
                        Term nestedTerm = nestedExpr.GetTerm(i);
                        TermOperator newOp = nestedExpr.GetOperator(i);
                        //if the outer operator was subtraction, flip the nested term's operator before using
                        if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                            if (newOp.compareTo(TermOperator.SUBTRACT) == 0) {
                                newOp = TermOperator.ADD;
                            } else {
                                newOp = TermOperator.SUBTRACT;
                            }
                        }
                        if (termIterator.hasPrevious()) {
                            termIterator.previous();
                            termOpIterator.previous();
                            termIterator.next();
                            termOpIterator.next();
                        }
                        termIterator.add(nestedTerm);
                        termOpIterator.add(newOp);
                    }
                }
            }

        }
        inExpr = new Expression(termListCopy, operatorListCopy);

        //combine equal terms using a hash table
        Hashtable<Term, FMNumber> termTable = new Hashtable<Term, FMNumber>();
        FMNumber numTotal = FMNumber.ZERO;
        int numTerms = inExpr.NumTerms();
        for (int i = 0; i < numTerms; i++) {
            Term term = inExpr.GetTerm(i);
            TermOperator termOp = inExpr.GetOperator(i);

            FMNumber currentValue = termTable.get(term);
            if (currentValue == null) {
                currentValue = FMNumber.ZERO;
            }

            //now the constant, if any, should be the first element of the term, so collect it.  
            assert term.NumPowers() > 0;
            Power firstPower = term.GetPower(0);
            //try to pull out a single value
            Factor single = firstPower.GetSingleFactor();
            if (single != null) {
                //check to see if we've found a number constant
                if (single.IsNumber()) {
                    FMNumber value = single.GetNumber();
                    if (term.NumPowers() > 1) {
                        if (term.GetOperator(1).compareTo(PowerOperator.DIVIDE) == 0) {
                            term = new Term(term.GetPowers(), term.GetOperators(), 0, new Power(new Factor(FMNumber.ONE)), PowerOperator.MULTIPLY, false);
                        } else {
                            term = term.RemovePower(0);
                        }
                    } else {
                        term = term.RemovePower(0);
                    }

                    //get a new current value since the term changed
                    currentValue = termTable.get(term);
                    if (currentValue == null) {
                        currentValue = FMNumber.ZERO;
                    }
                    //if the term is not just a single number
                    if (term.NumPowers() > 0) {
                        if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                            currentValue = currentValue.Subtract(value, context);
                        } else {
                            currentValue = currentValue.Add(value, context);
                        }
                        termTable.put(term, currentValue);
                    } else {
                        if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                            numTotal = numTotal.Subtract(value, context);
                        } else {
                            numTotal = numTotal.Add(value, context);
                        }
                    }
                } else {
                    FMNumber addValue = FMNumber.ONE;
                    if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                        addValue = addValue.Negate(context);
                    }
                    currentValue = currentValue.Add(addValue, context);

                    termTable.put(term, currentValue);
                }
            } else {
                FMNumber addValue = FMNumber.ONE;
                if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                    addValue = addValue.Negate(context);
                }
                currentValue = currentValue.Add(addValue, context);

                termTable.put(term, currentValue);
            }
        }

        //transform the term table back into an expression
        Expression resultExpr = new Expression();
        //first insert the constant, if any
        if (numTotal.compareTo(FMNumber.ZERO) != 0) {
            resultExpr = resultExpr.AppendTerm(new Term(new Power(new Factor(numTotal))), TermOperator.NONE);
        }
        //now insert the combined terms
        Enumeration<Term> terms = termTable.keys();
        while (terms.hasMoreElements()) {
            Term term = terms.nextElement();
            FMNumber coeff = termTable.get(term);
            TermOperator op = TermOperator.ADD;

            if (coeff.compareTo(FMNumber.ZERO) != 0) {
                if (coeff.compareTo(FMNumber.ZERO) < 0) {
                    coeff = coeff.Abs(context);
                    op = TermOperator.SUBTRACT;
                }
                Term newTerm = new Term();
                if (coeff.compareTo(FMNumber.ONE) != 0) {
                    newTerm = newTerm.AppendPower(new Power(new Factor(coeff)), PowerOperator.NONE);
                }
                int numExistingTerms = term.NumPowers();
                for (int i = 0; i < numExistingTerms; i++) {
                    newTerm = newTerm.AppendPower(term.GetPower(i), term.GetOperator(i));
                }

                newTerm = FlattenTerm(newTerm, depth);
                resultExpr = resultExpr.AppendTerm(newTerm, op);
            }
        }
        if (resultExpr.NumTerms() == 0) {
            resultExpr = resultExpr.AppendTerm(new Term(new Power(new Factor(FMNumber.ZERO))), TermOperator.NONE);
        }


        return resultExpr;
    }

    protected Term FlattenTerm(Term inTerm, int depth) throws ExpressionException, ExecutionException {
        assert inTerm != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        assert inTerm.NumPowers() > 0;

        //remove unnecessary nesting within the term
        Vector<Power> powerListCopy = inTerm.GetPowers();
        ListIterator powerIterator = powerListCopy.listIterator();
        Vector<PowerOperator> operatorListCopy = inTerm.GetOperators();
        ListIterator powerOpIterator = operatorListCopy.listIterator();
        while (powerIterator.hasNext()) {
            //retrieve and flatten the next power
            Power power = FlattenPower((Power) powerIterator.next(), depth);
            powerIterator.set(power);
            powerIterator.previous();
            powerIterator.next();
            PowerOperator powerOp = (PowerOperator) powerOpIterator.next();

            Factor single = power.GetSingleFactor();
            if (single != null) {
                if (single.IsNestedExpr()) {
                    Expression nestedExpr = single.GetNestedExpr();
                    if (nestedExpr.NumTerms() == 1) {
                        //nesting is unnecessary, so remove it
                        Term nestedTerm = nestedExpr.GetTerm(0);
                        TermOperator nestedTermOp = nestedExpr.GetOperator(0);
                        if (nestedTermOp == TermOperator.SUBTRACT) {
                            powerIterator.set(new Power(new Factor(new FMNumber(-1))));
                            powerOpIterator.set(PowerOperator.MULTIPLY);
                        } else {
                            powerIterator.remove();
                            powerOpIterator.remove();
                        }
                        for (int i = 0; i < nestedTerm.NumPowers(); i++) {
                            Power nestedPower = nestedTerm.GetPower(i);
                            PowerOperator newOp = nestedTerm.GetOperator(i);
                            if (newOp.compareTo(PowerOperator.NONE) == 0) {
                                newOp = PowerOperator.MULTIPLY;
                            }
                            if (powerOp.compareTo(PowerOperator.DIVIDE) == 0) {
                                if (newOp.compareTo(PowerOperator.DIVIDE) == 0) {
                                    newOp = PowerOperator.MULTIPLY;
                                } else {
                                    newOp = PowerOperator.DIVIDE;
                                }
                            }

                            if (powerIterator.hasPrevious()) {
                                powerIterator.previous();
                                powerOpIterator.previous();
                                powerIterator.next();
                                powerOpIterator.next();
                            }
                            powerIterator.add(nestedPower);
                            powerOpIterator.add(newOp);
                        }
                    }
                }
            }
        }
        inTerm = new Term(powerListCopy, operatorListCopy);

        //break the term into organized tables
        Hashtable<Power, Integer> powerMultiplyTable = new Hashtable<Power, Integer>();
        Hashtable<Power, Integer> powerDivideTable = new Hashtable<Power, Integer>();
        int numPowers = inTerm.NumPowers();
        for (int i = 0; i < numPowers; i++) {
            Power power = inTerm.GetPower(i);
            PowerOperator powerOp = inTerm.GetOperator(i);

            //determine which table to use
            Hashtable<Power, Integer> powerTable = (powerOp.compareTo(PowerOperator.DIVIDE) == 0) ? powerDivideTable : powerMultiplyTable;

            Integer currentValueObject = powerTable.get(power);
            int currentValue = (currentValueObject == null) ? 0 : currentValueObject;
            //increment the number of values for this power
            currentValue++;

            //put the new value into the power table
            powerTable.put(power, currentValue);
        }

        //split out the constants from the multiplied powers
        FMNumber numTotal = FMNumber.ONE;
        Vector<Power> multiplyList = new Vector<Power>();
        Enumeration<Power> multiplyEnumeration = powerMultiplyTable.keys();
        while (multiplyEnumeration.hasMoreElements()) {
            Power power = multiplyEnumeration.nextElement();
            int powerCount = powerMultiplyTable.get(power);
            Factor single = power.GetSingleFactor();
            if (single != null) {
                if (single.IsNumber()) {
                    FMNumber singleNumber = single.GetNumber();
                    if (!singleNumber.IsImaginary()) {
                        FMNumber resultNum = singleNumber.Pow(powerCount, context);
                        numTotal = numTotal.Multiply(resultNum, context);
                    } else {
                        if (powerCount != 1) {
                            power = power.AppendFactor(new Factor(powerCount));
                        }
                        multiplyList.add(power);
                    }
                } else {
                    if (powerCount != 1) {
                        power = power.AppendFactor(new Factor(powerCount));
                    }
                    multiplyList.add(power);
                }
            } else {
                //deal with cases that have more than one factor in the power
                if (powerCount > 1) {
                    Factor rightmostFactor = power.GetFactor(power.NumFactors() - 1);
                    //if we can add powers, do it
                    if (rightmostFactor.IsNumber()) {
                        //add together exponents by multiplying the value by the number of equal factors
                        FMNumber rightmostPowerNum = rightmostFactor.GetNumber();
                        rightmostPowerNum = rightmostPowerNum.Multiply(new FMNumber(powerCount), context);
                        //rebuild the power
                        Power newPower = new Power(power.GetFactor(0));
                        for (int i = 1; i < (power.NumFactors() - 1); i++) {
                            newPower = newPower.AppendFactor(power.GetFactor(1));
                        }
                        newPower = newPower.AppendFactor(new Factor(rightmostPowerNum));
                        power = newPower;
                    } else {
                        //wrap nesting parens around the power
                        power = new Power(new Factor(new Expression(new Term(power), TermOperator.NONE)));
                        //then exponentiate
                        power = power.AppendFactor(new Factor(powerCount));
                    }
                }
                multiplyList.add(power);
            }
        }

        //split out the constants from the divided powers
        Vector<Power> divideList = new Vector<Power>();
        Enumeration<Power> divideEnumeration = powerDivideTable.keys();
        while (divideEnumeration.hasMoreElements()) {
            Power power = divideEnumeration.nextElement();
            int powerCount = powerDivideTable.get(power);

            Factor single = power.GetSingleFactor();
            if (single != null) {
                if (single.IsNumber()) {
                    FMNumber singleNum = single.GetNumber();
                    if (!singleNum.IsImaginary()) {
                        FMNumber resultNum = singleNum.Pow(powerCount, context);
                        numTotal = numTotal.Divide(resultNum, context);
                    } else {
                        if (powerCount != 1) {
                            power = power.AppendFactor(new Factor(powerCount));
                        }
                        //first check for any cancellation, then append the power to the divide list
                        Power newDividePower = CheckCancel(multiplyList, power);
                        if (newDividePower != null) {
                            divideList.add(newDividePower);
                        }
                    }
                } else {
                    if (powerCount != 1) {
                        power = power.AppendFactor(new Factor(powerCount));
                    }
                    //first check for any cancellation, then append the power to the divide list
                    Power newDividePower = CheckCancel(multiplyList, power);
                    if (newDividePower != null) {
                        divideList.add(newDividePower);
                    }
                }
            } else {
                if (powerCount != 1) {
                    power = power.AppendFactor(new Factor(powerCount));
                }
                //first check for any cancellation, then append the power to the divide list
                Power newDividePower = CheckCancel(multiplyList, power);
                if (newDividePower != null) {
                    divideList.add(newDividePower);
                }
            }
        }

        //assemble a new Term
        Term resultTerm = new Term();
        int numMultiplies = multiplyList.size();
        int numDivides = divideList.size();
        //first put in the constant
        if (numTotal.compareTo(FMNumber.ZERO) == 0) {
            resultTerm = resultTerm.AppendPower(new Power(new Factor(numTotal)), PowerOperator.NONE);
            //return a zero value right away
            return resultTerm;
        }
        if (numTotal.compareTo(FMNumber.ONE) != 0) {
            resultTerm = resultTerm.AppendPower(new Power(new Factor(numTotal)), PowerOperator.NONE);
        } else if (numMultiplies == 0) {
            resultTerm = resultTerm.AppendPower(new Power(new Factor(numTotal)), PowerOperator.NONE);
        }
        //insert the multiplied powers
        for (int i = 0; i < numMultiplies; i++) {
            Power power = multiplyList.get(i);
            power = FlattenPower(power, depth);
            resultTerm = resultTerm.AppendPower(power, PowerOperator.MULTIPLY);
        }
        //insert the divided powers
        for (int i = 0; i < numDivides; i++) {
            Power power = divideList.get(i);
            power = FlattenPower(power, depth);
            resultTerm = resultTerm.AppendPower(power, PowerOperator.DIVIDE);
        }

        return resultTerm;
    }

    protected Power FlattenPower(Power inPower, int depth) throws ExpressionException, ExecutionException {
        assert inPower != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }

        assert inPower.NumFactors() > 0;
        Vector<Factor> factorListCopy = inPower.GetFactors();
        ListIterator powerIterator = factorListCopy.listIterator(inPower.NumFactors());
        Factor previousFactor = (Factor) powerIterator.previous();
        previousFactor = FlattenFactor(previousFactor, depth);
        if (previousFactor == null) {
            powerIterator.remove();
            return inPower;
        }
        powerIterator.set(previousFactor);

        while (powerIterator.hasPrevious()) {
            Factor factor = (Factor) powerIterator.previous();
            factor = FlattenFactor(factor, depth);
            if (factor == null) {
                powerIterator.remove();
                return inPower;
            }

            //check for zero powers
            if (previousFactor.IsNumber()) {
                FMNumber num = previousFactor.GetNumber();
                if (num.equals(FMNumber.ZERO)) {
                    return new Power(new Factor(FMNumber.ONE));
                }
            }

            if (previousFactor.IsNumber() && factor.IsNumber()) {
                FMNumber base = factor.GetNumber();
                FMNumber exp = previousFactor.GetNumber();
                FMNumber result = new FMNumber(StrictMath.pow(base.doubleValue(), exp.doubleValue()));
                factor = new Factor(result);

                powerIterator.remove();
                powerIterator.next();
                powerIterator.set(factor);
                powerIterator.previous();
            } else {
                powerIterator.set(factor);
            }

            previousFactor = factor;
        }

        return new Power(factorListCopy);
    }

    protected Factor FlattenFactor(Factor inFactor, int depth) throws ExpressionException, ExecutionException {
        assert inFactor != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        if (context == null) {
            context = MathContext.DECIMAL128;
        }

        if (inFactor.IsSymbol()) {
            if (lookupTable != null) {
                String symbol = inFactor.GetSymbol();
                if (lookupTable.Exists(symbol)) {
                    Expression expr = lookupTable.Get(symbol);
                    expr = FlattenExpression(expr, depth);
                    return ExpressionToFactor(expr);
                }
            }
        }//end symbol processing
        else if (inFactor.IsSymbolicFunction()) {
            SymbolicFunction sf = inFactor.GetSymbolicFunction();
            String sfName = sf.GetName();
            Vector<Equation> sfArgs = sf.GetParamList();
            //flatten each function argument
            for (int i = 0; i < sfArgs.size(); i++) {
                Equation equFlat = FlattenEquation(sfArgs.get(i), depth);
                sfArgs.set(i, equFlat);
            }
            sf = new SymbolicFunction(sfName, sfArgs, sf.IsMathFunction());

            try {
                Expression expr = null;

                String functionName = sf.GetName();

                if ((userFunctionTable != null) && (systemFunctionTable != null)) {

                    //call the function
                    if (systemFunctionTable.Exists(functionName)) {
                        Command functionCommand = systemFunctionTable.Get(sfName);
                        FMResult result = functionCommand.Execute(sfArgs, this);
                        if (result.IsExpression()) {
                            expr = result.GetExpression();
                        } else if (result.IsEquation()) {
                            Equation equ = result.GetEquation();
                            if (equ.IsExpression()) {
                                expr = equ.GetLHS();
                            }
                        } else if (result.IsString()) {
                            if (resultList != null) {
                                resultList.add(result);
                            }
                        } else if (result.IsImage()) {
                            if (resultList != null) {
                                resultList.add(result);
                            }
                        } else {
                            throw new ExecutionException("Unrecognized function result type");
                        }
                    } else if (userFunctionTable.Exists(functionName)) {
                        FMResult result = userFunctionTable.Get(sfName).Execute(sfArgs, this);
                        if (!result.IsExpression()) {
                            throw new ExecutionException("Invalid user function result for " + sf.GetName());
                        }
                        expr = result.GetExpression();
                        expr = FlattenExpression(expr, depth);
                    } else {
                        return inFactor;
                    }
                }


                if (expr != null) {
                    return ExpressionToFactor(expr);
                } else {
                    inFactor = new Factor();
                }

            } catch (Exception ex) {
                throw new ExpressionException(ex.toString());
            }
        }//end symbolic function processing
        else if (inFactor.IsNestedExpr()) {
            Expression expr = inFactor.GetNestedExpr();
            expr = FlattenExpression(expr, depth);

            return ExpressionToFactor(expr);
        }//end nested expression processing
        else if (inFactor.IsExprList()) {
            Vector<Expression> exprList = inFactor.GetExprList();
            for (int i = 0; i < exprList.size(); i++) {
                Expression exprFlat = FlattenExpression(exprList.get(i), depth);
                exprList.set(i, exprFlat);
            }
            return new Factor(exprList);
        }//end expression list processing

        return inFactor;
    }

    /**
     * Convert an expression into a factor (without flattening).  Single nesting is removed if possible, and negatives transfered to the actual numbers.  
     * @param expr  The expression (hopefully pre-flattened) needing to be converted into a factor.  
     * @return      The resulting factor created from the given expression.  
     */
    protected Factor ExpressionToFactor(Expression expr) throws ExpressionException {
        assert expr != null;

        SingleExpression single = expr.GetSingle();
        if (single != null) {
            Factor factor = single.SingleValue();
            if (single.IsSingleNegative()) {
                if (factor.IsNumber()) {
                    FMNumber num = factor.GetNumber();
                    return new Factor(num.Negate(context));
                } else {
                    return new Factor(expr);
                }
            }
            return single.SingleValue();

        }

        return new Factor(expr);
    }

    /**
     * Check to see if cancellation is possible between a list of nominator powers and a dividing power.  
     * @param mulList       List of powers multiplied in the nominator.  Will be modified if cancellation is possible.  
     * @param divValue      The dividing value in the denominator.  
     * @return              Null if total cancellation removed the power, otherwise returns the proper divide power to use.  
     */
    protected Power CheckCancel(Vector<Power> mulList, Power divValue) throws ExpressionException {
        boolean done = false;
        ListIterator<Power> mulIterator = mulList.listIterator();
        while (mulIterator.hasNext() && (!done)) {
            Power mulPower = mulIterator.next();

            assert mulPower.NumFactors() > 0;
            assert divValue.NumFactors() > 0;

            //check for total cancellation
            if (mulPower.equals(divValue)) {
                mulIterator.remove();
                divValue = null;
                done = true;
            } //check for partial cancellation
            else if (mulPower.GetFactor(0).equals(divValue.GetFactor(0))) {
                //check cancellation when both powers have two factors
                if ((mulPower.NumFactors() == 2) && (divValue.NumFactors() == 2)) {
                    Factor secondMulFactor = mulPower.GetFactor(1);
                    Factor secondDivFactor = divValue.GetFactor(1);
                    if (secondMulFactor.IsNumber() && secondDivFactor.IsNumber()) {
                        FMNumber mulNum = secondMulFactor.GetNumber();
                        FMNumber divNum = secondDivFactor.GetNumber();
                        if (mulNum.compareTo(divNum) < 0) {
                            mulIterator.remove();
                            divValue = new Power(divValue.GetFactor(0));
                            divValue = divValue.AppendFactor(new Factor(divNum.Subtract(mulNum, context)));
                            done = true;
                        } else {
                            divValue = null;
                            Power newMulPower = new Power(mulPower.GetFactor(0));
                            newMulPower = newMulPower.AppendFactor(new Factor(mulNum.Subtract(divNum, context)));
                            mulIterator.set(newMulPower);
                            done = true;
                        }
                    }
                } //check cancellation when mul power has 2 factors and div power has 1 factor
                else if ((mulPower.NumFactors() == 2) && (divValue.NumFactors() == 1)) {
                    Factor secondMulFactor = mulPower.GetFactor(1);
                    if (secondMulFactor.IsNumber()) {
                        FMNumber mulNum = secondMulFactor.GetNumber();
                        divValue = null;
                        Power newMulPower = new Power(mulPower.GetFactor(0));
                        newMulPower = newMulPower.AppendFactor(new Factor(mulNum.Subtract(FMNumber.ONE, context)));
                        mulIterator.set(newMulPower);
                        done = true;
                    }
                } //check cancellation when mul power has 1 factor and div power has 2 factors
                else if ((mulPower.NumFactors() == 1) && (divValue.NumFactors() == 2)) {
                    Factor secondDivFactor = divValue.GetFactor(1);
                    if (secondDivFactor.IsNumber()) {
                        FMNumber divNum = secondDivFactor.GetNumber();
                        mulIterator.remove();
                        divValue = new Power(divValue.GetFactor(0));
                        divValue = divValue.AppendFactor(new Factor(divNum.Subtract(FMNumber.ONE, context)));
                        done = true;
                    }
                }

            }
        }

        return divValue;
    }
}
