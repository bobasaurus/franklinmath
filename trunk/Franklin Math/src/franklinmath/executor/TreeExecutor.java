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

package franklinmath.executor;

import java.util.*;
import java.math.*;
import java.lang.reflect.*;
//import java.io.*;

import franklinmath.parser.*;
import franklinmath.expression.*;
import franklinmath.util.*;

/**
 * This class executes a parsed syntax tree.  
 * @author Allen Jordan
 */
public class TreeExecutor {

    protected LookupTable lookupTable = new LookupTable();
    protected FunctionTable functionTable = new FunctionTable();
    protected FunctionTable userFunctionTable = new FunctionTable();
    protected Vector<FMResult> results = new Vector<FMResult>();
    protected MathContext context;
    protected ExpressionToolset expressionToolset;

    public TreeExecutor(FunctionInformation functionInformation) throws ExecutionException {
        try {
            Vector<FunctionInformation.FunctionInfo> functionInfoList = functionInformation.GetFunctionList();
            for (int i = 0; i < functionInfoList.size(); i++) {
                FunctionInformation.FunctionInfo info = functionInfoList.get(i);
                String classname = "franklinmath.math." + info.name + "Command";
                Class function = Class.forName(classname);
                Constructor functionConstructor = function.getConstructor();

                Command functionCommand = (Command) functionConstructor.newInstance();
                functionCommand.SetName(info.name);
                functionCommand.SetIsMathFunction(info.isMathFunction);

                //add the command into the function table
                functionTable.Set(info.name, functionCommand);
            }
        } catch (Exception ex) {
            throw new ExecutionException(ex.toString());
        }
    }

    protected void CheckValidTree(SimpleNode node, String expected) throws ExecutionException {
        if (!node.toString().equals(expected)) {
            throw new ExecutionException("Invalid execution tree (expecting " + expected + ", found " + node.toString() + ")");
        }
    }

    protected boolean IsReserved(String name) {
        boolean isReserved = false;
        if (functionTable.Exists(name)) {
            isReserved = true;
        }
        return isReserved;
    }

    public synchronized Vector<FMResult> Execute(SimpleNode node) {
        //setup the math context using the system properties
        context = new MathContext(FMProperties.GetPrecision(), FMProperties.GetRoundingMode());

        results.clear();

        //create the toolset that flattens expressions
        expressionToolset = new ExpressionToolset(context, lookupTable, userFunctionTable, functionTable, results);

        try {
            CheckValidTree(node, "Program");
            int numChildren = node.jjtGetNumChildren();
            for (int i = 0; i < numChildren; i++) {
                SimpleNode stmtListNode = (SimpleNode) node.jjtGetChild(i);
                CheckValidTree(stmtListNode, "StmtList");
                ExecuteStmtList(stmtListNode);
            }
        } catch (ExecutionException ex) {
            FMResult error = new FMResult(ex.toString());
            results.add(error);
        } catch (ExpressionException ex) {
            FMResult error = new FMResult(ex.toString());
            results.add(error);
        }

        return results;
    }

    protected void ExecuteStmtList(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            SimpleNode stmtNode = (SimpleNode) node.jjtGetChild(i);
            CheckValidTree(stmtNode, "Stmt");
            ExecuteStmt(stmtNode);
        }
    }

    protected void ExecuteStmt(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Stmt)");
        }

        SimpleNode lhsNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(lhsNode, "Expr");
        Expression lhsExpr = ExecuteExpr(lhsNode);


        //if the statement is just an expression, finish up here
        if (numChildren == 1) {
            lhsExpr = expressionToolset.Flatten(lhsExpr);
            if (lhsExpr.NumTerms() > 0) {
                results.add(new FMResult(lhsExpr));
            }
        } //else, the statement is an assignment
        else {
            //get the RHS node
            SimpleNode rhsNode = (SimpleNode) node.jjtGetChild(1);
            CheckValidTree(rhsNode, "Expr");

            //get a single factor representing the LHS to be assigned
            Factor lhsFactor = null;

            SingleExpression single = lhsExpr.GetSingle();
            if (single == null) {
                throw new ExecutionException("Invalid assignment LHS");
            }
            if (single.IsSingleNegative()) {
                throw new ExecutionException("LHS assignment value can not be negative");
            }
            lhsFactor = single.SingleValue();


            //perform the assignment based on the LHS factor type
            if (lhsFactor.IsSymbol()) {
                String symbol = lhsFactor.GetSymbol();
                if (IsReserved(symbol)) {
                    throw new ExecutionException("The symbol \"" + symbol + "\" is reserved");
                }
                Expression rhsExpr = ExecuteExpr(rhsNode);
                rhsExpr = expressionToolset.Flatten(rhsExpr);

                lookupTable.Set(symbol, rhsExpr);
                results.add(new FMResult(rhsExpr));
            } else if (lhsFactor.IsSymbolicFunction()) {
                SymbolicFunction sf = lhsFactor.GetSymbolicFunction();
                if (IsReserved(sf.GetName())) {
                    throw new ExecutionException("The symbol \"" + sf.GetName() + "\" is reserved");
                }
                Vector<Equation> params = sf.GetParamList();

                Expression rhsExpr = ExecuteExpr(rhsNode);
                rhsExpr = expressionToolset.Flatten(rhsExpr);

                userFunctionTable.Set(sf.GetName(), new UserFunction(rhsExpr, params));
                results.add(new FMResult(rhsExpr));
            } else {
                throw new ExecutionException("Invalid assignment LHS");
            }

        }
    }

    //check for an initial minus sign indicating a negative before moving on to the main expression parsing
    protected Expression ExecuteExpr(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if (numChildren != 1) {
            throw new ExecutionException("Wrong number of children (Expr)");
        }

        SimpleNode exprMainNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(exprMainNode, "ExprMain");
        Expression expr = ExecuteExprMain(exprMainNode);

        //check for a starting negative sign on the expression
        Vector<Token> tokenList = node.getTokenList();
        if (tokenList.size() > 0) {
            Token operator = tokenList.get(0);
            if (operator.toString().equals("-")) {
                Vector<TermOperator> operators = expr.GetOperators();
                operators.set(0, TermOperator.SUBTRACT);
                expr = new Expression(expr.GetTerms(), operators);
            }
        }

        return expr;

    }

    //execute the main expression-building code
    protected Expression ExecuteExprMain(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Expr)");
        }

        SimpleNode termNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(termNode, "Term");
        Term term = ExecuteTerm(termNode);
        Expression expr = new Expression(term, TermOperator.NONE);

        //more terms to append to the expression
        if (numChildren == 2) {
            Vector<Token> tokenList = node.getTokenList();
            if (tokenList.size() == 1) {
                Token operator = tokenList.get(0);

                SimpleNode exprNode = (SimpleNode) node.jjtGetChild(1);
                CheckValidTree(exprNode, "ExprMain");
                Expression appendExpr = ExecuteExprMain(exprNode);

                if (appendExpr.NumTerms() > 0) {
                    Term firstAppendTerm = appendExpr.GetTerm(0);

                    if (operator.toString().equals("-")) {
                        expr = expr.Subtract(firstAppendTerm);
                    } else {
                        expr = expr.Add(firstAppendTerm);
                    }

                    for (int i = 1; i < appendExpr.NumTerms(); i++) {
                        Term nextAppendTerm = appendExpr.GetTerm(i);
                        TermOperator nextAppendOperator = appendExpr.GetOperator(i);
                        expr = expr.AppendTerm(nextAppendTerm, nextAppendOperator);
                    }
                }
            }
        }

        return expr;
    }

    protected Term ExecuteTerm(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Term)");
        }

        SimpleNode powerNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(powerNode, "Power");
        Power power = ExecutePower(powerNode);
        Term term = new Term(power);

        //more powers to append to the term
        if (numChildren == 2) {
            Vector<Token> tokenList = node.getTokenList();
            PowerOperator operator = PowerOperator.MULTIPLY;
            if (tokenList.size() == 1) {
                Token opToken = tokenList.get(0);
                if (opToken.toString().equals("/")) {
                    operator = PowerOperator.DIVIDE;
                }
            }

            SimpleNode termNode = (SimpleNode) node.jjtGetChild(1);
            CheckValidTree(termNode, "Term");
            Term appendTerm = ExecuteTerm(termNode);

            if (appendTerm.NumPowers() > 0) {
                Power firstPower = appendTerm.GetPower(0);
                term = term.AppendPower(firstPower, operator);

                for (int i = 1; i < appendTerm.NumPowers(); i++) {
                    term = term.AppendPower(appendTerm.GetPower(i), appendTerm.GetOperator(i));
                }
            }
        }

        return term;
    }

    protected Power ExecutePower(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Power)");
        }

        SimpleNode factorNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(factorNode, "Factor");
        Factor factor = ExecuteFactor(factorNode);
        Power power = new Power(factor);

        if (numChildren == 2) {
            SimpleNode powerNode = (SimpleNode) node.jjtGetChild(1);
            CheckValidTree(powerNode, "Power");
            Power appendPower = ExecutePower(powerNode);

            for (int i = 0; i < appendPower.NumFactors(); i++) {
                power = power.AppendFactor(appendPower.GetFactor(i));
            }
        }

        return power;
    }

    protected Factor ExecuteFactor(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Factor)");
        }

        SimpleNode factorChildNode = (SimpleNode) node.jjtGetChild(0);
        String factorChildType = factorChildNode.toString();

        //nested expression
        if (factorChildType.equals("Expr")) {
            Expression expr = ExecuteExpr(factorChildNode);
            return new Factor(expr);
        } //symbol or function call
        else if (factorChildType.equals("Identifier")) {
            Vector<Token> tokenList = factorChildNode.getTokenList();
            if (tokenList.size() != 1) {
                throw new ExecutionException("Invalid identifier");
            }
            String id = tokenList.get(0).toString();

            //symbol
            if (numChildren == 1) {
                if (IsReserved(id)) {
                    throw new ExecutionException("The symbol \"" + id + "\" is reserved");
                }
                return new Factor(id, true);
            } //function call
            else {
                SimpleNode equnListNode = (SimpleNode) node.jjtGetChild(1);
                CheckValidTree(equnListNode, "EqunList");
                Vector<Equation> args = ExecuteEqunList(equnListNode);
                return new Factor(new SymbolicFunction(id, args, true));
            }
        } //number
        else if (factorChildType.equals("Number")) {
            Vector<Token> tokenList = factorChildNode.getTokenList();
            if (tokenList.size() != 1) {
                throw new ExecutionException("Invalid number");
            }
            try {
                String numberStr = tokenList.get(0).toString();
                if (numberStr.equals("i")) {
                    //complex number
                    return new Factor(new FMNumber(0, 1));
                } else {
                    FMNumber number = new FMNumber(numberStr);
                    return new Factor(number);
                }
            } catch (NumberFormatException ex) {
                throw new ExecutionException("Invalid number: " + ex.toString());
            }
        } //expression list
        else if (factorChildType.equals("List")) {
            Vector<Expression> list = ExecuteList(factorChildNode);
            return new Factor(list);
        } //string literal
        else if (factorChildType.equals("StringLiteral")) {
            Vector<Token> tokenList = factorChildNode.getTokenList();
            if (tokenList.size() != 1) {
                throw new ExecutionException("Invalid string");
            }
            String str = tokenList.get(0).toString();
            //remove quotation marks
            str = str.substring(1, str.length() - 1);
            return new Factor(str, false);
        } //escape sequence
        else if (factorChildType.equals("EscSeq")) {
            //todo: implement escape sequences
            throw new ExecutionException("Sorry, escape sequences are not yet supported");
        } else {
            throw new ExecutionException("Invalid factor type");
        }

    //return null;
    }

    protected Vector<Equation> ExecuteEqunList(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if (numChildren < 0) {
            throw new ExecutionException("Wrong number of children (EqunList)");
        }

        Vector<Equation> result = new Vector<Equation>();

        for (int i = 0; i < numChildren; i++) {
            SimpleNode equnChildNode = (SimpleNode) node.jjtGetChild(i);
            CheckValidTree(equnChildNode, "Equn");
            Equation equ = ExecuteEqun(equnChildNode);
            if (equ == null) {
                throw new ExecutionException("Null equation in function equation list");
            }
            result.add(equ);
        }

        return result;
    }

    protected Equation ExecuteEqun(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();
        if ((numChildren != 1) && (numChildren != 2)) {
            throw new ExecutionException("Wrong number of children (Equn)");
        }

        Expression lhsExpr = null;
        Expression rhsExpr = null;

        SimpleNode lhsExprNode = (SimpleNode) node.jjtGetChild(0);
        CheckValidTree(lhsExprNode, "Expr");
        lhsExpr = ExecuteExpr(lhsExprNode);

        if (numChildren == 2) {
            SimpleNode rhsExprNode = (SimpleNode) node.jjtGetChild(1);
            CheckValidTree(rhsExprNode, "Expr");
            rhsExpr = ExecuteExpr(rhsExprNode);
        }

        return new Equation(lhsExpr, rhsExpr);
    }

    protected Vector<Expression> ExecuteList(SimpleNode node) throws ExecutionException, ExpressionException {
        int numChildren = node.jjtGetNumChildren();

        Vector<Expression> result = new Vector<Expression>();

        for (int i = 0; i < numChildren; i++) {
            SimpleNode exprNode = (SimpleNode) node.jjtGetChild(i);
            CheckValidTree(exprNode, "Expr");
            Expression expr = ExecuteExpr(exprNode);
            result.add(expr);
        }

        return result;
    }
}
