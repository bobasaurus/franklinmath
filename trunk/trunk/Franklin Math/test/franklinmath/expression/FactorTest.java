package franklinmath.expression;

import java.math.BigDecimal;
import java.util.Vector;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit tests for the Factor class.  
 * @author Allen Jordan
 */
public class FactorTest {

    public FactorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEquals() throws ExpressionException {
        //quickly test the obvious case with equal references
        Factor factor1 = new Factor(1);
        Factor factor2 = factor1;
        assertEquals(factor1, factor2);

        //now test other individual cases

        factor1 = new Factor(0);
        factor2 = new Factor(0);
        assertEquals(factor1, factor2);

        factor1 = new Factor(1);
        factor2 = new Factor(1);
        assertEquals(factor1, factor2);

        factor1 = new Factor(-1);
        factor2 = new Factor(-1);
        assertEquals(factor1, factor2);

        factor1 = new Factor(1);
        factor2 = new Factor(-1);
        assertFalse(factor1.equals(factor2));

        factor1 = new Factor(1.1);
        factor2 = new Factor(1.1);
        assertEquals(factor1, factor2);

        factor1 = new Factor(1.1);
        factor2 = new Factor(1.2);
        assertFalse(factor1.equals(factor2));

        factor1 = new Factor("x", true);
        factor2 = new Factor("x", true);
        assertEquals(factor1, factor2);

        factor1 = new Factor("x", true);
        factor2 = new Factor("y", true);
        assertFalse(factor1.equals(factor2));

        Random random = new Random();

        int count = 100;
        for (int i = 0; i < count; i++) {
            //integer test
            int intValue = random.nextInt();
            factor1 = new Factor(intValue);
            factor2 = new Factor(intValue);
            assertEquals(factor1, factor2);

            //double test
            double doubleValue = random.nextDouble();
            factor1 = new Factor(doubleValue);
            factor2 = new Factor(doubleValue);
            assertEquals(factor1, factor2);

            //symbol test
            String symbol = "symbol_" + random.nextInt();
            factor1 = new Factor(symbol, true);
            factor2 = new Factor(symbol, true);
            assertEquals(factor1, factor2);

            //string test
            String string = "string_" + random.nextInt();
            factor1 = new Factor(string, false);
            factor2 = new Factor(string, false);
            assertEquals(factor1, factor2);

            //nested expression test
            Expression expr = ExpressionTools.RandomExpression();
            factor1 = new Factor(expr);
            factor2 = new Factor(expr);
            assertEquals(factor1, factor2);

            //expression list test
            Vector<Expression> exprList = new Vector<Expression>();
            int listSize = random.nextInt(10) + 1;
            for (int j = 0; j < listSize; j++) {
                exprList.add(ExpressionTools.RandomExpression());
            }
            factor1 = new Factor(exprList);
            factor2 = new Factor(exprList);
            assertEquals(factor1, factor2);

        }
    }

    @Test
    public void testHashCode() {
        //quickly test the obvious case with equal references
        Factor factor1 = new Factor(1);
        Factor factor2 = factor1;
        assertEquals(factor1.hashCode(), factor2.hashCode());

        //now test other individual cases

        factor1 = new Factor(0);
        factor2 = new Factor(0);
        assertEquals(factor1.hashCode(), factor2.hashCode());

        factor1 = new Factor(1);
        factor2 = new Factor(1);
        assertEquals(factor1.hashCode(), factor2.hashCode());

        factor1 = new Factor(-1);
        factor2 = new Factor(-1);
        assertEquals(factor1.hashCode(), factor2.hashCode());

        factor1 = new Factor(1.1);
        factor2 = new Factor(1.1);
        assertEquals(factor1.hashCode(), factor2.hashCode());

        factor1 = new Factor("x", true);
        factor2 = new Factor("x", true);
        assertEquals(factor1.hashCode(), factor2.hashCode());

    }
}
