package franklinmath.expression;

import franklinmath.executor.*;
import franklinmath.parser.*;
import franklinmath.util.*;

import java.util.*;
import java.math.*;
//import java.io.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/**
 * JUnit tests for the ExpressionTools class.  
 * @author Allen Jordan
 */
public class ExpressionToolsTest {

    protected TreeExecutor executor;
    protected MathContext context;
    protected FMNumber threshold;
    protected Random random;

    public ExpressionToolsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (FMProperties.IsLoaded()) {
            System.out.println("FMProperties is already loaded");
        } else {
            System.out.println("Loading FMProperties");
            FMProperties.LoadProperties();
            System.out.println("  Internal precision: " + FMProperties.GetPrecision());
            System.out.println("  Display precision: " + FMProperties.GetDisplayPrecision());
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws ExecutionException {
        executor = new TreeExecutor();
        context = new MathContext(FMProperties.GetPrecision(), FMProperties.GetRoundingMode());
        threshold = new FMNumber("10E-15");
        random = new Random();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test numeric addition (using normal string processing).  
     * @throws franklinmath.expression.ExpressionException
     * @throws franklinmath.executor.ExecutionException
     * @throws franklinmath.parser.ParseException
     */
    @Test
    public void testNumberAdd() throws ExpressionException, ExecutionException, ParseException {

        //start with specific test cases

        String strInput = String.format("%d + %d", 0, 0);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 0, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 1, 0);
        resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(-1), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(2), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -19, 15);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(-4), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d + %d", 1, 2, 3);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(6), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d + %d", 3, 2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(6), resultExpr.GetSingleNumber());

        strInput = String.format("%f + %f", 1.1, 1.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().Subtract(new FMNumber(2.2), context).Abs(context).compareTo(threshold) < 0);

        strInput = String.format("%f + %f", 1.2, 2.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().Subtract(new FMNumber(3.3), context).Abs(context).compareTo(threshold) < 0);

        //now perform input fuzzing

        //adding two integers
        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = new FMNumber(random.nextInt());
            FMNumber num2 = (new FMNumber(random.nextInt())).Abs(context);
            strInput = String.format("%d + %d", num1.intValue(), num2.intValue());
            resultExpr = ProcessString(strInput);
            assertEquals(num1.Add(num2, context), resultExpr.GetSingleNumber());
        }

        //adding multiple integers
        for (int i = 0; i < 100; i++) {
            int count = random.nextInt(100) + 2;
            FMNumber result = FMNumber.ZERO;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                FMNumber num = (new FMNumber(random.nextInt())).Abs(context);
                if ((j == 0) && (random.nextBoolean())) {
                    num = num.Negate(context);
                }
                result = result.Add(num, context);
                if (j != 0) {
                    builder.append("+");
                }
                builder.append(num.intValue());
            }
            resultExpr = ProcessString(builder.toString());
            assertEquals(result, resultExpr.GetSingleNumber());
        }

        //adding two doubles
        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = new FMNumber(random.nextDouble());
            FMNumber num2 = new FMNumber(random.nextDouble());
            num1 = num1.Multiply(new FMNumber(random.nextInt(100)), context);
            num2 = num2.Multiply(new FMNumber(random.nextInt(100)), context).Abs(context);
            strInput = String.format("%.100f + %.100f", num1.BigDecimalValue(), num2.BigDecimalValue());
            resultExpr = ProcessString(strInput);
            assertTrue(resultExpr.GetSingleNumber().Subtract(num1.Add(num2, context), context).Abs(context).compareTo(threshold) < 0);
        }

        //adding multiple doubles
        for (int i = 0; i < 100; i++) {
            int count = random.nextInt(100) + 2;
            FMNumber result = FMNumber.ZERO;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                FMNumber num = new FMNumber(random.nextDouble());
                num = num.Multiply(new FMNumber(random.nextInt(100)), context).Abs(context);
                if ((j == 0) && (random.nextBoolean())) {
                    num = num.Negate(context);
                }
                result = result.Add(num, context);
                if (j != 0) {
                    builder.append("+");
                }
                builder.append(String.format("%.100f", num.BigDecimalValue()));
            }
            resultExpr = ProcessString(builder.toString());
            assertTrue(resultExpr.GetSingleNumber().Subtract(result, context).Abs(context).compareTo(threshold) < 0);
        }

    }

    /**
     * Test subtracting numbers using fuzzing
     * @throws franklinmath.expression.ExpressionException
     * @throws franklinmath.executor.ExecutionException
     */
    @Test(timeout = 30000)
    public void testNumberSubtract() throws ExpressionException, ExecutionException, ParseException {

        //start with specific test cases

        String strInput = String.format("%d - %d", 0, 0);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 0, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(-1), resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%f - %f", 1.1, 1.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().Subtract(FMNumber.ZERO, context).Abs(context).compareTo(threshold) < 0);

        strInput = String.format("%f - %f", 1.1, 2.2);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().Subtract(new FMNumber(-1.1), context).Abs(context).compareTo(threshold) < 0);

        //now perform input fuzzing

        //subtracting two integers
        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = new FMNumber(random.nextInt());
            FMNumber num2 = (new FMNumber(random.nextInt())).Abs(context);
            strInput = String.format("%d - %d", num1.intValue(), num2.intValue());
            resultExpr = ProcessString(strInput);
            assertEquals(num1.Subtract(num2, context), resultExpr.GetSingleNumber());
        }

        //subtracting multiple integers
        for (int i = 0; i < 100; i++) {
            FMNumber result = FMNumber.ZERO;
            int count = random.nextInt(100) + 2;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                FMNumber num = (new FMNumber(random.nextInt())).Abs(context);
                if (j == 0) {
                    result = num;
                } else {
                    result = result.Subtract(num, context);
                    builder.append("-");
                }
                builder.append(num.intValue());
            }
            resultExpr = ProcessString(builder.toString());
            assertEquals(result, resultExpr.GetSingleNumber());
        }

        //subtracting two doubles
        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = new FMNumber(random.nextDouble());
            FMNumber num2 = new FMNumber(random.nextDouble());
            num1 = num1.Multiply(new FMNumber(random.nextInt(100)), context);
            num2 = num2.Multiply(new FMNumber(random.nextInt(100)), context).Abs(context);
            strInput = String.format("%.100f - %.100f", num1.BigDecimalValue(), num2.BigDecimalValue());
            resultExpr = ProcessString(strInput);
            assertTrue(resultExpr.GetSingleNumber().Subtract(num1.Subtract(num2, context), context).Abs(context).compareTo(threshold) < 0);
        }

        //subtracting multiple doubles
        for (int i = 0; i < 100; i++) {
            FMNumber result = FMNumber.ZERO;
            int count = random.nextInt(100) + 2;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                FMNumber num = (new FMNumber(random.nextDouble())).Multiply(new FMNumber(random.nextInt(100)), context).Abs(context);
                if (j == 0) {
                    result = num;
                } else {
                    result = result.Subtract(num, context);
                    builder.append("-");
                }
                builder.append(String.format("%.100f", num.BigDecimalValue()));
            }
            resultExpr = ProcessString(builder.toString());
            assertTrue(resultExpr.GetSingleNumber().Subtract(result, context).Abs(context).compareTo(threshold) < 0);
        }


    }

    @Test(timeout = 30000)
    public void testNumberMultiplyAndDivide() throws ExpressionException, ExecutionException, ParseException {

        //start with specific test cases

        String strInput = String.format("%d * %d", 0, 0);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(FMNumber.ZERO, resultExpr.GetSingleNumber());

        //now perform input fuzzing

        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = new FMNumber(random.nextInt());
            FMNumber num2 = new FMNumber(random.nextInt()).Abs(context).Add(FMNumber.ONE, context);
            String mulStr = String.format("%d * %d", num1.intValue(), num2.intValue());
            String divStr = String.format("%d / %d", num1.intValue(), num2.intValue());

            resultExpr = ProcessString(mulStr);
            assertEquals(resultExpr.GetSingleNumber(), num1.Multiply(num2, context));

            resultExpr = ProcessString(divStr);
            assertEquals(resultExpr.GetSingleNumber(), num1.Divide(num2, context));
        }

        for (int i = 0; i < 1000; i++) {
            FMNumber num1 = (new FMNumber(random.nextDouble())).Multiply(new FMNumber(random.nextInt(100)), context);
            FMNumber num2 = (new FMNumber(random.nextDouble())).Multiply(new FMNumber(random.nextInt(100)), context).Abs(context).Add(FMNumber.ONE, context);
            String mulStr = String.format("%.100f * %.100f", num1.BigDecimalValue(), num2.BigDecimalValue());
            String divStr = String.format("%.100f / %.100f", num1.BigDecimalValue(), num2.BigDecimalValue());

            resultExpr = ProcessString(mulStr);
            assertTrue(resultExpr.GetSingleNumber().Subtract(num1.Multiply(num2, context), context).Abs(context).compareTo(threshold) < 0);

            resultExpr = ProcessString(divStr);
            assertTrue(resultExpr.GetSingleNumber().Subtract(num1.Divide(num2, context), context).Abs(context).compareTo(threshold) < 0);
        }
    }

    @Test
    public void testNumberMixed() throws ExpressionException, ExecutionException, ParseException {
        //test -1+2+3+1-2-4
        String strInput = String.format("%d + %d + %d + %d - %d - %d", -1, 2, 3, 1, 2, 4);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(new FMNumber(-1), resultExpr.GetSingleNumber());
    }

    @Test
    public void testSymbolicAdd() throws Exception {
        String strInput = "x + x";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor(2)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "x + x + x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(3)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "x + x + y";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(2)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        Expression expectedExpr = new Expression(expectedTerm, TermOperator.NONE);
        expectedExpr = expectedExpr.AppendTerm(new Term(new Power(new Factor("y", true))), TermOperator.ADD);
        assertEquals(expectedExpr, resultExpr);

        strInput = "3x+2x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(5)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "3x + 2y + 2x + 3y";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(5)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        expectedExpr = new Expression(expectedTerm, TermOperator.NONE);
        Term expectedTerm2 = new Term(new Power(new Factor(5)));
        expectedTerm2 = expectedTerm2.AppendPower(new Power(new Factor("y", true)), PowerOperator.MULTIPLY);
        expectedExpr = expectedExpr.AppendTerm(expectedTerm2, TermOperator.ADD);
        assertEquals(expectedExpr, resultExpr);

        strInput = "3x*y+2y*x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(5)));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("y", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);
    }

    @Test
    public void testSymbolicSubtract() throws Exception {
        String strInput = "x - x";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor(FMNumber.ZERO)));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "x - x - x";
        resultExpr = ProcessString(strInput);
        Expression expectedExpr = new Expression(new Term(new Power(new Factor("x", true))), TermOperator.SUBTRACT);
        assertEquals(expectedExpr, resultExpr);

        strInput = "x - x - y";
        resultExpr = ProcessString(strInput);
        expectedExpr = new Expression(new Term(new Power(new Factor("y", true))), TermOperator.SUBTRACT);
        assertEquals(expectedExpr, resultExpr);

        strInput = "x - y - y";
        resultExpr = ProcessString(strInput);
        expectedExpr = new Expression(new Term(new Power(new Factor("x", true))), TermOperator.NONE);
        expectedTerm = new Term(new Power(new Factor(new FMNumber(2))));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("y", true)), PowerOperator.MULTIPLY);
        expectedExpr = expectedExpr.AppendTerm(expectedTerm, TermOperator.SUBTRACT);
        assertEquals(expectedExpr, resultExpr);

        strInput = "3x - 2x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "2x - 3x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.SUBTRACT), resultExpr);

        strInput = "11x - 3x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(new FMNumber(8))));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "3x - 11x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(new FMNumber(8))));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.SUBTRACT), resultExpr);
    }

    @Test
    public void testSymbolicPowerArithmatic() throws Exception {
        Expression resultExpr = ProcessString("x*x^2");
        Power expectedPower = new Power(new Factor("x", true));
        expectedPower = expectedPower.AppendFactor(new Factor(new FMNumber(3)));
        assertEquals(new Expression(new Term(expectedPower), TermOperator.NONE), resultExpr);
    }
    
    @Test public void testSymbolicCancel() throws Exception {
        Expression resultExpr = ProcessString("x/x");
        Term expectedTerm = new Term(new Power(new Factor(new FMNumber(1))));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);
    }

    @Test
    public void testSymbolicMixed() throws Exception {
        String strInput = "-1*(-x)";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        resultExpr = ProcessString("-(-1+x^2-5+3)");
        expectedTerm = new Term(new Power(new Factor(new FMNumber(3))));
        Power expectedPower = new Power(new Factor("x", true));
        expectedPower = expectedPower.AppendFactor(new Factor(new FMNumber(2)));
        Expression expectedExpr = new Expression(expectedTerm, TermOperator.NONE);
        expectedExpr = expectedExpr.AppendTerm(new Term(expectedPower), TermOperator.SUBTRACT);
        assertEquals(expectedExpr, resultExpr);
    }

    @Test
    public void testNestingRemoval() throws Exception {
        String strInput = "(-1)";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor(new FMNumber(-1))));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "(1)";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(FMNumber.ONE)));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "(-x)";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.SUBTRACT), resultExpr);

        strInput = "(-1+x)";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(new FMNumber(-1))));
        Term expectedTerm2 = new Term(new Power(new Factor("x", true)));
        Expression expectedExpr = new Expression(expectedTerm, TermOperator.NONE);
        expectedExpr = expectedExpr.AppendTerm(expectedTerm2, TermOperator.ADD);
        assertEquals(expectedExpr, resultExpr);
    }

    @Test
    public void testFlattenTermNumbers() throws ExpressionException, ExecutionException {
        Term term = new Term();
        term = term.AppendPower(new Power(new Factor(12)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(2)), PowerOperator.MULTIPLY);
        Term resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new FMNumber(24), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(-52)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(46)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new FMNumber(-2392), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(0)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(7)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new FMNumber(0), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(1)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(42)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new FMNumber(42), resultTerm.GetSingleFactor().GetNumber());

        int count = 1000;
        for (int i = 0; i < count; i++) {
            term = new Term();
            int int1 = random.nextInt();
            int int2 = random.nextInt();
            PowerOperator operator = (random.nextBoolean()) ? PowerOperator.MULTIPLY : PowerOperator.DIVIDE;
            term = term.AppendPower(new Power(new Factor(int1)), PowerOperator.NONE);
            term = term.AppendPower(new Power(new Factor(int2)), operator);
            resultTerm = ExpressionTools.FlattenTerm(term, context, null, null, null, 0);
            if (operator.compareTo(PowerOperator.DIVIDE) == 0) {
                assertEquals((new FMNumber(int1)).Divide(new FMNumber(int2), context), resultTerm.GetSingleFactor().GetNumber());
            } else {
                assertEquals((new FMNumber(int1)).Multiply(new FMNumber(int2), context), resultTerm.GetSingleFactor().GetNumber());
            }

            term = new Term();
            double double1 = random.nextDouble() * random.nextInt();
            double double2 = random.nextDouble() * random.nextInt();
            operator = (random.nextBoolean()) ? PowerOperator.MULTIPLY : PowerOperator.DIVIDE;
            term = term.AppendPower(new Power(new Factor(double1)), PowerOperator.NONE);
            term = term.AppendPower(new Power(new Factor(double2)), operator);
            resultTerm = ExpressionTools.FlattenTerm(term, context, null, null, null, 0);
            if (operator.compareTo(PowerOperator.DIVIDE) == 0) {
                FMNumber expected = (new FMNumber(double1)).Divide(new FMNumber(double2), context);
                assertTrue(resultTerm.GetSingleFactor().GetNumber().Subtract(expected, context).Abs(context).compareTo(threshold) < 0);
            } else {
                FMNumber expected = (new FMNumber(double1)).Multiply(new FMNumber(double2), context);
                assertTrue(resultTerm.GetSingleFactor().GetNumber().Subtract(expected, context).Abs(context).compareTo(threshold) < 0);
            }
        }
    }

    @Test
    public void testFlattenTermSymbols() throws ExpressionException, ExecutionException {
        Term term = new Term();
        term = term.AppendPower(new Power(new Factor("a", true)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor("a", true)), PowerOperator.MULTIPLY);
        Term resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals("a^2", resultTerm.toString().trim());

        term = new Term();
        term = term.AppendPower(new Power(new Factor("a", true)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor("b", true)), PowerOperator.MULTIPLY);
        term = term.AppendPower(new Power(new Factor("a", true)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertTrue("a^2*b".equals(resultTerm.toString().trim()) || "b*a^2".equals(resultTerm.toString().trim()));
    }

    @Test
    public void testFlattenPower() throws ExpressionException, ExecutionException {
        Power power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(2)));
        power = power.AppendFactor(new Factor(new FMNumber(8)));
        Power resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(256));

        power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(16)));
        power = power.AppendFactor(new Factor(new FMNumber(0.5)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(4));

        power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(-1)));
        power = power.AppendFactor(new Factor(new FMNumber(2)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(1));

        power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(-12)));
        power = power.AppendFactor(new Factor(new FMNumber(-1)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(1.0 / -12));

        power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(0)));
        power = power.AppendFactor(new Factor(new FMNumber(0)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(1));

        power = new Power();
        power = power.AppendFactor(new Factor(new FMNumber(12)));
        power = power.AppendFactor(new Factor(new FMNumber(0)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new FMNumber(1));

        int count = 1000;
        for (int i = 0; i < count; i++) {
            int num1 = random.nextInt(100);
            int num2 = random.nextInt(100);

            power = new Power();
            power = power.AppendFactor(new Factor(new FMNumber(num1)));
            power = power.AppendFactor(new Factor(new FMNumber(num2)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            double expectedResult = StrictMath.pow((double) num1, (double) num2);

            assertTrue(resultPower.GetSingleFactor().GetNumber().Subtract(new FMNumber(expectedResult), context).Abs(context).compareTo(threshold) < 0);

            int num3 = random.nextInt(100) + 1;
            double num4 = random.nextDouble() * random.nextInt(100) * ((random.nextBoolean()) ? 1 : -1);

            power = new Power();
            power = power.AppendFactor(new Factor(new FMNumber(num3)));
            power = power.AppendFactor(new Factor(new FMNumber(num4)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num3, (double) num4);

            assertTrue(resultPower.GetSingleFactor().GetNumber().Subtract(new FMNumber(expectedResult), context).Abs(context).compareTo(threshold) < 0);

            double num5 = (random.nextDouble() * random.nextInt(100) + 0.01) * ((random.nextBoolean()) ? 1 : -1);
            int num6 = random.nextInt(100) * ((random.nextBoolean()) ? 1 : -1);

            power = new Power();
            power = power.AppendFactor(new Factor(new FMNumber(num5)));
            power = power.AppendFactor(new Factor(new FMNumber(num6)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num5, (double) num6);

            assertTrue(resultPower.GetSingleFactor().GetNumber().Subtract(new FMNumber(expectedResult), context).Abs(context).compareTo(threshold) < 0);

            int num7 = 0;
            int num8 = random.nextInt(1000) + 1;

            power = new Power();
            power = power.AppendFactor(new Factor(new FMNumber(num7)));
            power = power.AppendFactor(new Factor(new FMNumber(num8)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num7, (double) num8);

            assertTrue(resultPower.GetSingleFactor().GetNumber().Subtract(new FMNumber(expectedResult), context).Abs(context).compareTo(threshold) < 0);
        }

    }

    /**
     * Test for a special case that was causing problems (involving variable expression values being changed after they are returned).  
     * @throws java.lang.Exception
     */
    @Test
    public void testVariableValueRetainment() throws Exception {
        ProcessString("testvar = 2");
        Expression result = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(2)), result);
        Expression result2 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(2)), result2);
        Expression result3 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(2)), result3);

        ProcessString("testvar = 1.1");
        result = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber("1.1")), result);
        result2 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber("1.1")), result2);
        result3 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber("1.1")), result3);

        ProcessString("testvar = 0");
        result = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(0)), result);
        result2 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(0)), result2);
        result3 = ProcessString("testvar");
        assertEquals(BuildExpression(new FMNumber(0)), result3);
    }

    @Test
    public void testDirectNumberOutput() throws Exception {
        Expression expr = ProcessString("1.1");
        assertEquals(expr.GetSingleNumber(), new FMNumber("1.1"));

        expr = ProcessString("-2.3");
        assertEquals(expr.GetSingleNumber(), new FMNumber("-2.3"));

        expr = ProcessString("0");
        assertEquals(expr.GetSingleNumber(), new FMNumber("0"));

        expr = ProcessString("1");
        assertEquals(expr.GetSingleNumber(), new FMNumber("1"));

        expr = ProcessString("-1");
        assertEquals(expr.GetSingleNumber(), new FMNumber("-1"));
    }
    
    @Test
    public void testFunctionCalls() throws Exception {
        Expression resultExpr = ProcessString("Sin[2]");
        FMNumber resultNumber = resultExpr.GetSingleNumber();
        FMNumber expectedNumber = new FMNumber("0.90929742682568169539601986591174");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[1.1]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("0.8912073600614353399518025778717");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[-3]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("-0.14112000805986722210074480280811");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Pi[]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("3.14159265358979323846");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Cos[2]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("-0.41614683654714238699756822950076");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        int count = 1000;
        for (int i=0; i<count; i++) {
            FMNumber number = new FMNumber(random.nextDouble()*random.nextInt(100));
            resultExpr = ProcessString(String.format("Sin[%.100f]", number.doubleValue()));
            resultNumber = resultExpr.GetSingleNumber();
            expectedNumber = new FMNumber(StrictMath.sin(number.doubleValue()));
            assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        }
    }
    
    @Test public void testFunctionArithmatic() throws Exception {
        Expression resultExpr = ProcessString("Sin[2] + Cos[2]");
        FMNumber resultNumber = resultExpr.GetSingleNumber();
        FMNumber expectedNumber = new FMNumber("0.49315059027853930839845163641098");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[-1] + Cos[-1]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("-0.301168678939757");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[1.1]^2 + Cos[1.1]^2");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = FMNumber.ONE;
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[2]/Cos[2]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("-2.18503986326152");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        assertTrue(resultNumber.Subtract(ProcessString("Tan[2]").GetSingleNumber(), context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("-Sin[Pi[]/4] + Cos[Pi[]/6]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = new FMNumber("0.158918622597891");
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
        
        resultExpr = ProcessString("Sin[Pi[]] - Cos[Pi[]]");
        resultNumber = resultExpr.GetSingleNumber();
        expectedNumber = FMNumber.ONE;
        assertTrue(resultNumber.Subtract(expectedNumber, context).compareTo(threshold) < 0);
    }
    
    @Test public void testImaginaryArithmatic() throws Exception {
        Expression resultExpr = ProcessString("(2+3i)+(4+5i)");
        FMNumber resultNumber = new FMNumber(6, 8);
        assertEquals(BuildExpression(resultNumber), resultExpr);
    }

    /**
     * Create an expression from a number.  
     * @param value     The number to use when building the expression.  
     * @return          The newly built expression.  
     * @throws franklinmath.expression.ExpressionException
     */
    protected Expression BuildExpression(FMNumber value) throws ExpressionException {
        return new Expression(new Term(new Power(new Factor(value))), TermOperator.NONE);
    }

    /**
     * Create an expression from a Factor.  
     * @param value     The factor to use when building the expression.  
     * @return          The newly built expression.  
     * @throws franklinmath.expression.ExpressionException
     */
    protected Expression BuildExpression(Factor value) throws ExpressionException {
        return new Expression(new Term(new Power(value)), TermOperator.NONE);
    }

    /**
     * Process a string input to the Franklin Math parser/executor/flattener system.  
     * @param str       The input string to process.  
     * @return          The resulting expression.  
     * @throws franklinmath.parser.ParseException
     * @throws franklinmath.executor.ExecutionException
     * @throws franklinmath.expression.ExpressionException
     */
    protected Expression ProcessString(String str) throws ParseException, ExecutionException, ExpressionException {
        java.io.StringReader strReader = new java.io.StringReader(str);
        java.io.Reader reader = new java.io.BufferedReader(strReader);
        FMParser parser = new FMParser(reader);
        Vector<FMResult> resultList = executor.Execute(parser.Program());
        if (resultList.size() != 1) {
            throw new ExecutionException("Too many results");
        }
        return resultList.get(0).GetExpression();
    }
}
