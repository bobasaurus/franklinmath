package franklinmath.executor;

import java.util.*;
import java.math.*;
import java.lang.reflect.*;
import java.io.*;

import franklinmath.parser.*;
import franklinmath.expression.*;
import franklinmath.util.*;

import javax.xml.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

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

    public TreeExecutor() throws ExecutionException {
        //use reflection to build the function list
        try {
            //Read in the XML function list.  The java DOM API is fairly complex and difficult.  Might eventually switch back to the JDOM library.  
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("functions.xml"));
            Element root = document.getDocumentElement();
            NodeList functionNodeList = root.getElementsByTagName("function");
            for (int i = 0; i < functionNodeList.getLength(); i++) {
                org.w3c.dom.Node node = functionNodeList.item(i);
                if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    throw new ExecutionException("XML function list parsing error; invalid node type");
                }
                if (!node.getNodeName().equals("function")) {
                    throw new ExecutionException("XML function list parsing error; invalid element name");
                }

                String functionName = "";
                boolean isMathFunction = false;

                NodeList functionDataList = node.getChildNodes();
                for (int j = 0; j < functionDataList.getLength(); j++) {
                    org.w3c.dom.Node dataNode = functionDataList.item(j);
                    String nodeName = dataNode.getNodeName();

                    if (nodeName.equals("name")) {
                        functionName = dataNode.getTextContent();
                    } else if (nodeName.equals("is_math_function")) {
                        isMathFunction = (dataNode.getTextContent().equals("true")) ? true : false;
                    }
                }

                String classname = "franklinmath.math." + functionName + "Command";
                Class function = Class.forName(classname);
                Class[] argsClassArray = new Class[]{String.class, boolean.class};
                Constructor functionConstructor = function.getConstructor(argsClassArray);

                Object[] argsArray = new Object[]{functionName, isMathFunction};
                Command functionCommand = (Command) functionConstructor.newInstance(argsArray);

                //add the command into the function table
                functionTable.Set(functionName, functionCommand);
            }
        } catch (Exception ex) {
            throw new ExecutionException("Error when loading function table: " + ex.toString());
        }
    }

    protected void CheckValidTree(SimpleNode node, String expected) throws ExecutionException {
        if (!node.toString().equals(expected)) {
            throw new ExecutionException("Invalid execution tree (expecting " + expected + ", found " + node.toString() + ")");
        }
    }

    protected void CheckReserved(String name) throws ExecutionException {
        if (functionTable.Exists(name)) {
            throw new ExecutionException(name + " is a reserved word");
        }
    }

    public synchronized Vector<FMResult> Execute(SimpleNode node) {
        //setup the math context using the system properties
        context = new MathContext(FMProperties.GetPrecision(), FMProperties.GetRoundingMode());

        results.clear();
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
            lhsExpr = ExpressionTools.Flatten(lhsExpr, context, lookupTable, userFunctionTable, functionTable);
            results.add(new FMResult(lhsExpr));
        } //else, the statement is an assignment
        else {
            //get the RHS node
            SimpleNode rhsNode = (SimpleNode) node.jjtGetChild(1);
            CheckValidTree(rhsNode, "Expr");

            //get a single factor representing the LHS to be assigned
            Factor lhsFactor = null;
            try {
                SingleExpression single = lhsExpr.GetSingle();
                if (single.IsSingleNegative()) {
                    throw new ExecutionException("LHS assignment value can not be negative");
                }
                lhsFactor = single.SingleValue();
            } catch (ExpressionException ex) {
                throw new ExecutionException("Invalid assignment LHS");
            }

            //perform the assignment based on the LHS factor type
            if (lhsFactor.IsSymbol()) {
                String symbol = lhsFactor.GetSymbol();
                CheckReserved(symbol);

                Expression rhsExpr = ExecuteExpr(rhsNode);
                rhsExpr = ExpressionTools.Flatten(rhsExpr, context, lookupTable, userFunctionTable, functionTable);

                lookupTable.Set(symbol, rhsExpr);
                results.add(new FMResult(rhsExpr));
            } else if (lhsFactor.IsSymbolicFunction()) {
                SymbolicFunction sf = lhsFactor.GetSymbolicFunction();
                CheckReserved(sf.GetName());
                Vector<Equation> params = sf.GetParamList();

                Expression rhsExpr = ExecuteExpr(rhsNode);
                rhsExpr = ExpressionTools.Flatten(rhsExpr, context, lookupTable, userFunctionTable, functionTable);

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
                CheckReserved(id);
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
            return new Factor(tokenList.get(0).toString(), false);
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
