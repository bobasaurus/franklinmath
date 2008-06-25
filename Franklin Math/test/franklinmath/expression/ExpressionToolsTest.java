package franklinmath.expression;

import franklinmath.executor.*;
import franklinmath.parser.*;
import franklinmath.util.*;

import java.util.*;
import java.math.*;
import java.io.*;
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
    protected BigDecimal threshold;
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
        threshold = new BigDecimal("10E-14");
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
        assertEquals(BigDecimal.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 0, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 1, 0);
        resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(-1), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", 1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(2), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d", -19, 15);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(-4), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d + %d", 1, 2, 3);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(6), resultExpr.GetSingleNumber());

        strInput = String.format("%d + %d + %d", 3, 2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(6), resultExpr.GetSingleNumber());

        strInput = String.format("%f + %f", 1.1, 1.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().subtract(new BigDecimal(2.2, context), context).abs().compareTo(threshold) < 0);

        strInput = String.format("%f + %f", 1.2, 2.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().subtract(new BigDecimal(3.3, context), context).abs().compareTo(threshold) < 0);

        //now perform input fuzzing

        //adding two integers
        for (int i = 0; i < 1000; i++) {
            BigDecimal num1 = new BigDecimal(random.nextInt());
            BigDecimal num2 = (new BigDecimal(random.nextInt())).abs();
            strInput = String.format("%d + %d", num1.intValue(), num2.intValue());
            resultExpr = ProcessString(strInput);
            assertEquals(num1.add(num2, context), resultExpr.GetSingleNumber());
        }

        //adding multiple integers
        for (int i = 0; i < 100; i++) {
            int count = random.nextInt(100) + 2;
            BigDecimal result = BigDecimal.ZERO;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                BigDecimal num = (new BigDecimal(random.nextInt())).abs();
                if ((j == 0) && (random.nextBoolean())) {
                    num = num.negate();
                }
                result = result.add(num, context);
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
            BigDecimal num1 = new BigDecimal(random.nextDouble());
            BigDecimal num2 = new BigDecimal(random.nextDouble());
            num1 = num1.multiply(new BigDecimal(random.nextInt(100)), context);
            num2 = num2.multiply(new BigDecimal(random.nextInt(100)), context).abs();
            strInput = String.format("%.100f + %.100f", num1, num2);
            resultExpr = ProcessString(strInput);
            assertTrue(resultExpr.GetSingleNumber().subtract(num1.add(num2, context)).abs().compareTo(threshold) < 0);
        }

        //adding multiple doubles
        for (int i = 0; i < 100; i++) {
            int count = random.nextInt(100) + 2;
            BigDecimal result = BigDecimal.ZERO;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                BigDecimal num = new BigDecimal(random.nextDouble());
                num = num.multiply(new BigDecimal(random.nextInt(100)), context).abs();
                if ((j == 0) && (random.nextBoolean())) {
                    num = num.negate();
                }
                result = result.add(num, context);
                if (j != 0) {
                    builder.append("+");
                }
                builder.append(String.format("%.100f", num));
            }
            resultExpr = ProcessString(builder.toString());
            assertTrue(resultExpr.GetSingleNumber().subtract(result).abs().compareTo(threshold) < 0);
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
        assertEquals(BigDecimal.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 0, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(-1), resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 1, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ZERO, resultExpr.GetSingleNumber());

        strInput = String.format("%d - %d", 2, 1);
        resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ONE, resultExpr.GetSingleNumber());

        strInput = String.format("%f - %f", 1.1, 1.1);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().subtract(BigDecimal.ZERO, context).abs().compareTo(threshold) < 0);

        strInput = String.format("%f - %f", 1.1, 2.2);
        resultExpr = ProcessString(strInput);
        assertTrue(resultExpr.GetSingleNumber().subtract(new BigDecimal(-1.1, context), context).abs().compareTo(threshold) < 0);

        //now perform input fuzzing

        //subtracting two integers
        for (int i = 0; i < 1000; i++) {
            BigDecimal num1 = new BigDecimal(random.nextInt());
            BigDecimal num2 = (new BigDecimal(random.nextInt())).abs();
            strInput = String.format("%d - %d", num1.intValue(), num2.intValue());
            resultExpr = ProcessString(strInput);
            assertEquals(num1.subtract(num2, context), resultExpr.GetSingleNumber());
        }

        //subtracting multiple integers
        for (int i = 0; i < 100; i++) {
            BigDecimal result = BigDecimal.ZERO;
            int count = random.nextInt(100) + 2;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                BigDecimal num = (new BigDecimal(random.nextInt())).abs();
                if (j == 0) {
                    result = num;
                } else {
                    result = result.subtract(num, context);
                    builder.append("-");
                }
                builder.append(num.intValue());
            }
            resultExpr = ProcessString(builder.toString());
            assertEquals(result, resultExpr.GetSingleNumber());
        }

        //subtracting two doubles
        for (int i = 0; i < 1000; i++) {
            BigDecimal num1 = new BigDecimal(random.nextDouble());
            BigDecimal num2 = new BigDecimal(random.nextDouble());
            num1 = num1.multiply(new BigDecimal(random.nextInt(100)), context);
            num2 = num2.multiply(new BigDecimal(random.nextInt(100)), context).abs();
            strInput = String.format("%.100f - %.100f", num1, num2);
            resultExpr = ProcessString(strInput);
            assertTrue(resultExpr.GetSingleNumber().subtract(num1.subtract(num2, context)).abs().compareTo(threshold) < 0);
        }

        //subtracting multiple doubles
        for (int i = 0; i < 100; i++) {
            BigDecimal result = BigDecimal.ZERO;
            int count = random.nextInt(100) + 2;
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < count; j++) {
                BigDecimal num = (new BigDecimal(random.nextDouble())).multiply(new BigDecimal(random.nextInt(100))).abs();
                if (j == 0) {
                    result = num;
                } else {
                    result = result.subtract(num, context);
                    builder.append("-");
                }
                builder.append(String.format("%.100f", num));
            }
            resultExpr = ProcessString(builder.toString());
            assertTrue(resultExpr.GetSingleNumber().subtract(result).abs().compareTo(threshold) < 0);
        }


    }

    @Test(timeout = 30000)
    public void testNumberMultiplyAndDivide() throws ExpressionException, ExecutionException, ParseException {

        //start with specific test cases

        String strInput = String.format("%d * %d", 0, 0);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(BigDecimal.ZERO, resultExpr.GetSingleNumber());

        //now perform input fuzzing

        for (int i = 0; i < 1000; i++) {
            BigDecimal num1 = new BigDecimal(random.nextInt());
            BigDecimal num2 = new BigDecimal(random.nextInt()).abs().add(BigDecimal.ONE);
            String mulStr = String.format("%d * %d", num1.intValue(), num2.intValue());
            String divStr = String.format("%d / %d", num1.intValue(), num2.intValue());

            resultExpr = ProcessString(mulStr);
            assertEquals(resultExpr.GetSingleNumber(), num1.multiply(num2, context));

            resultExpr = ProcessString(divStr);
            assertEquals(resultExpr.GetSingleNumber(), num1.divide(num2, context));
        }

        for (int i = 0; i < 1000; i++) {
            BigDecimal num1 = (new BigDecimal(random.nextDouble())).multiply(new BigDecimal(random.nextInt(100)));
            BigDecimal num2 = (new BigDecimal(random.nextDouble())).multiply(new BigDecimal(random.nextInt(100))).abs().add(BigDecimal.ONE);
            String mulStr = String.format("%.100f * %.100f", num1, num2);
            String divStr = String.format("%.100f / %.100f", num1, num2);

            resultExpr = ProcessString(mulStr);
            assertTrue(resultExpr.GetSingleNumber().subtract(num1.multiply(num2, context)).abs().compareTo(threshold) < 0);

            resultExpr = ProcessString(divStr);
            assertTrue(resultExpr.GetSingleNumber().subtract(num1.divide(num2, context)).abs().compareTo(threshold) < 0);
        }
    }

    @Test
    public void testNumberMixed() throws ExpressionException, ExecutionException, ParseException {
        //test -1+2+3+1-2-4
        String strInput = String.format("%d + %d + %d + %d - %d - %d", -1, 2, 3, 1, 2, 4);
        Expression resultExpr = ProcessString(strInput);
        assertEquals(new BigDecimal(-1), resultExpr.GetSingleNumber());
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
        Term expectedTerm = new Term(new Power(new Factor(BigDecimal.ZERO)));
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
        expectedTerm = new Term(new Power(new Factor(new BigDecimal(2))));
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
        expectedTerm = new Term(new Power(new Factor(new BigDecimal(8))));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);

        strInput = "3x - 11x";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor(new BigDecimal(8))));
        expectedTerm = expectedTerm.AppendPower(new Power(new Factor("x", true)), PowerOperator.MULTIPLY);
        assertEquals(new Expression(expectedTerm, TermOperator.SUBTRACT), resultExpr);
    }

    @Test
    public void testSymbolicMixed() throws Exception {
        String strInput = "-1*(-x)";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);
    }

    @Test
    public void testNestingRemoval() throws Exception {
        String strInput = "(-1)";
        Expression resultExpr = ProcessString(strInput);
        Term expectedTerm = new Term(new Power(new Factor(new BigDecimal(-1))));
        assertEquals(new Expression(expectedTerm, TermOperator.NONE), resultExpr);
        
        strInput = "(-x)";
        resultExpr = ProcessString(strInput);
        expectedTerm = new Term(new Power(new Factor("x", true)));
        assertEquals(new Expression(expectedTerm, TermOperator.SUBTRACT), resultExpr);
    }

    @Test
    public void testFlattenTermNumbers() throws ExpressionException, ExecutionException {
        Term term = new Term();
        term = term.AppendPower(new Power(new Factor(12)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(2)), PowerOperator.MULTIPLY);
        Term resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new BigDecimal(24), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(-52)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(46)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new BigDecimal(-2392), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(0)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(7)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new BigDecimal(0), resultTerm.GetSingleFactor().GetNumber());

        term = new Term();
        term = term.AppendPower(new Power(new Factor(1)), PowerOperator.NONE);
        term = term.AppendPower(new Power(new Factor(42)), PowerOperator.MULTIPLY);
        resultTerm = ExpressionTools.FlattenTerm(term, null, null, null, null, 0);
        assertEquals(new BigDecimal(42), resultTerm.GetSingleFactor().GetNumber());

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
                assertEquals((new BigDecimal(int1)).divide(new BigDecimal(int2), context), resultTerm.GetSingleFactor().GetNumber());
            } else {
                assertEquals((new BigDecimal(int1)).multiply(new BigDecimal(int2), context), resultTerm.GetSingleFactor().GetNumber());
            }

            term = new Term();
            double double1 = random.nextDouble() * random.nextInt();
            double double2 = random.nextDouble() * random.nextInt();
            operator = (random.nextBoolean()) ? PowerOperator.MULTIPLY : PowerOperator.DIVIDE;
            term = term.AppendPower(new Power(new Factor(double1)), PowerOperator.NONE);
            term = term.AppendPower(new Power(new Factor(double2)), operator);
            resultTerm = ExpressionTools.FlattenTerm(term, context, null, null, null, 0);
            if (operator.compareTo(PowerOperator.DIVIDE) == 0) {
                BigDecimal expected = (new BigDecimal(double1)).divide(new BigDecimal(double2), context);
                assertTrue(resultTerm.GetSingleFactor().GetNumber().subtract(expected).abs().compareTo(threshold) < 0);
            } else {
                BigDecimal expected = (new BigDecimal(double1)).multiply(new BigDecimal(double2), context);
                assertTrue(resultTerm.GetSingleFactor().GetNumber().subtract(expected).abs().compareTo(threshold) < 0);
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
        power = power.AppendFactor(new Factor(new BigDecimal(2)));
        power = power.AppendFactor(new Factor(new BigDecimal(8)));
        Power resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(256));

        power = new Power();
        power = power.AppendFactor(new Factor(new BigDecimal(16)));
        power = power.AppendFactor(new Factor(new BigDecimal(0.5)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(4));

        power = new Power();
        power = power.AppendFactor(new Factor(new BigDecimal(-1)));
        power = power.AppendFactor(new Factor(new BigDecimal(2)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(1));

        power = new Power();
        power = power.AppendFactor(new Factor(new BigDecimal(-12)));
        power = power.AppendFactor(new Factor(new BigDecimal(-1)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(1.0 / -12));

        power = new Power();
        power = power.AppendFactor(new Factor(new BigDecimal(0)));
        power = power.AppendFactor(new Factor(new BigDecimal(0)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(1));

        power = new Power();
        power = power.AppendFactor(new Factor(new BigDecimal(12)));
        power = power.AppendFactor(new Factor(new BigDecimal(0)));
        resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
        assertEquals(resultPower.GetSingleFactor().GetNumber(), new BigDecimal(1));

        int count = 1000;
        for (int i = 0; i < count; i++) {
            int num1 = random.nextInt(100);
            int num2 = random.nextInt(100);

            power = new Power();
            power = power.AppendFactor(new Factor(new BigDecimal(num1)));
            power = power.AppendFactor(new Factor(new BigDecimal(num2)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            double expectedResult = StrictMath.pow((double) num1, (double) num2);

            assertTrue(resultPower.GetSingleFactor().GetNumber().subtract(new BigDecimal(expectedResult)).abs().compareTo(threshold) < 0);

            int num3 = random.nextInt(100) + 1;
            double num4 = random.nextDouble() * random.nextInt(100) * ((random.nextBoolean()) ? 1 : -1);

            power = new Power();
            power = power.AppendFactor(new Factor(new BigDecimal(num3)));
            power = power.AppendFactor(new Factor(new BigDecimal(num4)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num3, (double) num4);

            assertTrue(resultPower.GetSingleFactor().GetNumber().subtract(new BigDecimal(expectedResult)).abs().compareTo(threshold) < 0);

            double num5 = (random.nextDouble() * random.nextInt(100) + 0.01) * ((random.nextBoolean()) ? 1 : -1);
            int num6 = random.nextInt(100) * ((random.nextBoolean()) ? 1 : -1);

            power = new Power();
            power = power.AppendFactor(new Factor(new BigDecimal(num5)));
            power = power.AppendFactor(new Factor(new BigDecimal(num6)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num5, (double) num6);

            assertTrue(resultPower.GetSingleFactor().GetNumber().subtract(new BigDecimal(expectedResult)).abs().compareTo(threshold) < 0);

            int num7 = 0;
            int num8 = random.nextInt(1000) + 1;

            power = new Power();
            power = power.AppendFactor(new Factor(new BigDecimal(num7)));
            power = power.AppendFactor(new Factor(new BigDecimal(num8)));
            resultPower = ExpressionTools.FlattenPower(power, null, null, null, null, 0);
            expectedResult = StrictMath.pow((double) num7, (double) num8);

            assertTrue(resultPower.GetSingleFactor().GetNumber().subtract(new BigDecimal(expectedResult)).abs().compareTo(threshold) < 0);
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
        assertEquals(BuildExpression(new BigDecimal(2)), result);
        Expression result2 = ProcessString("testvar");
        assertEquals(BuildExpression(new BigDecimal(2)), result2);
    }

    protected Expression BuildExpression(BigDecimal value) throws ExpressionException {
        return new Expression(new Term(new Power(new Factor(value))), TermOperator.NONE);
    }

    protected Expression BuildExpression(Factor value) throws ExpressionException {
        return new Expression(new Term(new Power(value)), TermOperator.NONE);
    }

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
