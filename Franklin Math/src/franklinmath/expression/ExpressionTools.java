package franklinmath.expression;

import franklinmath.executor.*;
import java.math.*;
import java.util.*;

/**
 * This class provides a set of tools to help with expressions and equations
 * @author Allen Jordan
 */
public class ExpressionTools {

    protected static int depthLimit = 256;
    protected static int breadthLimit = 32768;

    /**
     * Generate a random expression (possibly for use with fuzzing)
     * @return Return the resulting random expression
     */
    public static Expression RandomExpression() throws ExpressionException {
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

    public static Equation Flatten(Equation mainEqu, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable) throws ExpressionException, ExecutionException {
        return FlattenEquation(mainEqu, context, lookupTable, userFunctionTable, functionTable, 0);
    }

    public static Expression Flatten(Expression mainExpr, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable) throws ExpressionException, ExecutionException {
        return FlattenExpression(mainExpr, context, lookupTable, userFunctionTable, functionTable, 0);
    }

    protected static Equation FlattenEquation(Equation inEqu, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable, int depth) throws ExpressionException, ExecutionException {
        assert inEqu != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        if (context == null) {
            context = MathContext.DECIMAL128;
        }

        if (inEqu.IsExpression()) {
            Expression lhs = inEqu.GetLHS();
            assert lhs != null;
            Expression exprFlat = FlattenExpression(lhs, context, lookupTable, userFunctionTable, functionTable, depth);
            return new Equation(exprFlat, null);
        } else {
            Expression lhs = inEqu.GetLHS();
            Expression rhs = inEqu.GetRHS();
            assert lhs != null;
            assert rhs != null;

            Expression lhsFlat = FlattenExpression(lhs, context, lookupTable, userFunctionTable, functionTable, depth);
            Expression rhsFlat = FlattenExpression(rhs, context, lookupTable, userFunctionTable, functionTable, depth);

            return new Equation(lhsFlat, rhsFlat);
        }
    }

    protected static Expression FlattenExpression(Expression inExpr, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable, int depth) throws ExpressionException, ExecutionException {
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
            Term term = FlattenTerm(termIterator.next(), context, lookupTable, userFunctionTable, functionTable, depth);
            termIterator.set(term);
            termIterator.previous();
            termIterator.next();
            TermOperator termOp = termOpIterator.next();

            try {
                Factor single = term.GetSingleFactor();
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
            } catch (ExpressionException ex) {

            }
        }
        inExpr = new Expression(termListCopy, operatorListCopy);

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
            try {
                //try to pull out a single value
                Factor single = firstPower.GetSingleFactor();
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
                            currentValue = currentValue.subtract(value, context);
                        } else {
                            currentValue = currentValue.add(value, context);
                        }
                        termTable.put(term, currentValue);
                    } else {
                        if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                            numTotal = numTotal.subtract(value, context);
                        } else {
                            numTotal = numTotal.add(value, context);
                        }
                    }
                } else {
                    FMNumber addValue = FMNumber.ONE;
                    if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                        addValue = addValue.negate();
                    }
                    currentValue = currentValue.add(addValue, context);

                    termTable.put(term, currentValue);
                }
            } catch (ExpressionException ex) {
                FMNumber addValue = FMNumber.ONE;
                if (termOp.compareTo(TermOperator.SUBTRACT) == 0) {
                    addValue = addValue.negate();
                }
                currentValue = currentValue.add(addValue, context);

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
                    coeff = coeff.abs();
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

                newTerm = FlattenTerm(newTerm, context, lookupTable, userFunctionTable, functionTable, depth);
                resultExpr = resultExpr.AppendTerm(newTerm, op);
            }
        }
        if (resultExpr.NumTerms() == 0) {
            resultExpr = resultExpr.AppendTerm(new Term(new Power(new Factor(FMNumber.ZERO))), TermOperator.NONE);
        }


        return resultExpr;
    }

    protected static Term FlattenTerm(Term inTerm, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable, int depth) throws ExpressionException, ExecutionException {
        assert inTerm != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        if (context == null) {
            context = MathContext.DECIMAL128;
        }
        assert inTerm.NumPowers() > 0;

        //remove unnecessary nesting within the term
        Vector<Power> powerListCopy = inTerm.GetPowers();
        ListIterator powerIterator = powerListCopy.listIterator();
        Vector<PowerOperator> operatorListCopy = inTerm.GetOperators();
        ListIterator powerOpIterator = operatorListCopy.listIterator();
        while (powerIterator.hasNext()) {
            //retrieve and flatten the next power
            Power power = FlattenPower((Power) powerIterator.next(), context, lookupTable, userFunctionTable, functionTable, depth);
            powerIterator.set(power);
            powerIterator.previous();
            powerIterator.next();
            PowerOperator powerOp = (PowerOperator) powerOpIterator.next();
            try {
                Factor single = power.GetSingleFactor();
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
            } catch (ExpressionException ex) {
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
            try {
                Factor single = power.GetSingleFactor();
                if (single.IsNumber()) {
                    FMNumber resultNum = single.GetNumber().pow(powerCount);
                    numTotal = numTotal.multiply(resultNum, context);
                } else {
                    if (powerCount != 1) {
                        power = power.AppendFactor(new Factor(powerCount));
                    }
                    multiplyList.add(power);
                }
            } catch (ExpressionException ex) {
                if (powerCount != 1) {
                    power = power.AppendFactor(new Factor(powerCount));
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
            try {
                Factor single = power.GetSingleFactor();
                if (single.IsNumber()) {
                    FMNumber resultNum = single.GetNumber().pow(powerCount);
                    numTotal = numTotal.divide(resultNum, context);
                } else {
                    if (powerCount != 1) {
                        power = power.AppendFactor(new Factor(powerCount));
                    }
                    divideList.add(power);
                }
            } catch (ExpressionException ex) {
                if (powerCount != 1) {
                    power = power.AppendFactor(new Factor(powerCount));
                }
                divideList.add(power);
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
            power = FlattenPower(power, context, lookupTable, userFunctionTable, functionTable, depth);
            resultTerm = resultTerm.AppendPower(power, PowerOperator.MULTIPLY);
        }
        //insert the divided powers
        for (int i = 0; i < numDivides; i++) {
            Power power = divideList.get(i);
            power = FlattenPower(power, context, lookupTable, userFunctionTable, functionTable, depth);
            resultTerm = resultTerm.AppendPower(power, PowerOperator.DIVIDE);
        }

        return resultTerm;
    }

    protected static Power FlattenPower(Power inPower, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable, int depth) throws ExpressionException, ExecutionException {
        assert inPower != null;
        depth++;
        if (depth > depthLimit) {
            throw new ExpressionException("Recursion depth limit reached");
        }
        if (context == null) {
            context = MathContext.DECIMAL128;
        }

        assert inPower.NumFactors() > 0;
        Vector<Factor> factorListCopy = inPower.GetFactors();
        ListIterator powerIterator = factorListCopy.listIterator(inPower.NumFactors());
        Factor previousFactor = (Factor) powerIterator.previous();
        previousFactor = FlattenFactor(previousFactor, context, lookupTable, userFunctionTable, functionTable, depth);
        powerIterator.set(previousFactor);
        while (powerIterator.hasPrevious()) {
            Factor factor = (Factor) powerIterator.previous();
            factor = FlattenFactor(factor, context, lookupTable, userFunctionTable, functionTable, depth);
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

    protected static Factor FlattenFactor(Factor inFactor, MathContext context, LookupTable lookupTable, FunctionTable userFunctionTable, FunctionTable functionTable, int depth) throws ExpressionException, ExecutionException {
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
                    expr = FlattenExpression(expr, context, lookupTable, userFunctionTable, functionTable, depth);
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
                Equation equFlat = FlattenEquation(sfArgs.get(i), context, lookupTable, userFunctionTable, functionTable, depth);
                sfArgs.set(i, equFlat);
            }
            sf = new SymbolicFunction(sfName, sfArgs, sf.IsMathFunction());

            try {
                Expression expr = null;
                //call the function
                if (functionTable != null) {
                    if (functionTable.Exists(sf.GetName())) {
                        Command functionCommand = functionTable.Get(sfName);
                        FMResult result = functionCommand.Execute(sfArgs);
                        if (result.IsExpression()) {
                            expr = result.GetExpression();
                        } else {
                        //todo:  handle other FMResult values
                        }
                    }
                }
                if (userFunctionTable != null) {
                    if (userFunctionTable.Exists(sf.GetName())) {
                        FMResult result = userFunctionTable.Get(sfName).Execute(sfArgs);
                        if (!result.IsExpression()) {
                            throw new ExecutionException("Invalid user function result for " + sf.GetName());
                        }
                        expr = result.GetExpression();
                        expr = FlattenExpression(expr, context, lookupTable, userFunctionTable, functionTable, depth);
                    }
                }
                if (expr != null) {
                    return ExpressionToFactor(expr);
                }

            } catch (Exception ex) {
                throw new ExpressionException(ex.toString());
            }
        }//end symbolic function processing
        else if (inFactor.IsNestedExpr()) {
            Expression expr = inFactor.GetNestedExpr();
            expr = FlattenExpression(expr, context, lookupTable, userFunctionTable, functionTable, depth);

            return ExpressionToFactor(expr);
        }//end nested expression processing
        else if (inFactor.IsExprList()) {
            Vector<Expression> exprList = inFactor.GetExprList();
            for (int i = 0; i < exprList.size(); i++) {
                Expression exprFlat = FlattenExpression(exprList.get(i), context, lookupTable, userFunctionTable, functionTable, depth);
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
    protected static Factor ExpressionToFactor(Expression expr) {
        assert expr != null;

        try {
            SingleExpression single = expr.GetSingle();
            Factor factor = single.SingleValue();
            if (single.IsSingleNegative()) {
                if (factor.IsNumber()) {
                    FMNumber num = factor.GetNumber();
                    return new Factor(num.negate());
                } else {
                    throw new Exception();
                }
            }
            return single.SingleValue();

        } catch (Exception ex) {
        }

        return new Factor(expr);
    }
}
