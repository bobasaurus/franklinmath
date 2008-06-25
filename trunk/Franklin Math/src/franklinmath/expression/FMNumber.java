package franklinmath.expression;

import java.math.*;

import franklinmath.util.*;

/**
 * A class to handle imaginary number arithmatic (which includes real number arithmatic)
 * @author Allen Jordan
 */
public final class FMNumber implements LatexOutput, Comparable {

    private final BigDecimal real;
    private final BigDecimal imag;

    public FMNumber() {
        real = BigDecimal.ZERO;
        imag = BigDecimal.ZERO;
    }

    public FMNumber(long value) {
        real = new BigDecimal(value);
        imag = new BigDecimal(value);
    }

    public FMNumber(double value) {
        real = new BigDecimal(value);
        imag = new BigDecimal(value);
    }

    public FMNumber(String value) {
        real = new BigDecimal(value);
        imag = new BigDecimal(value);
    }
    
    public String toLatexString() {
        
    }
    
    @Override public String toString() {
        
    }
    
    public int compareTo(Object obj) {
        FMNumber number = (FMNumber) obj;
        
    }
}
