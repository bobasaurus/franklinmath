package franklinmath.expression;

import java.math.*;

import franklinmath.util.*;

/**
 * A class to handle complex number arithmatic (which includes real number arithmatic)
 * @author Allen Jordan
 */
public final class FMNumber implements Comparable, LatexOutput {

    private final BigDecimal real;
    private final BigDecimal imag;
    private final MathContext defaultContext;
    public static final FMNumber ZERO = new FMNumber(BigDecimal.ZERO);
    public static final FMNumber ONE = new FMNumber(BigDecimal.ONE);

    public FMNumber() {
        real = BigDecimal.ZERO;
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(BigDecimal realValue, BigDecimal imagValue) {
        assert (realValue != null) && (imagValue != null);
        real = realValue;
        imag = imagValue;
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(FMNumber realValue, FMNumber imagValue) throws ExpressionException {
        assert (realValue != null) && (imagValue != null);
        if (realValue.IsImaginary() || imagValue.IsImaginary()) {
            throw new ExpressionException("Invalid (imaginary) parameter(s) for number construction");
        }
        real = realValue.RealValue();
        imag = imagValue.RealValue();
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(long realValue, long imagValue) {
        real = new BigDecimal(realValue);
        imag = new BigDecimal(imagValue);
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(double realValue, double imagValue) {
        real = new BigDecimal(realValue);
        imag = new BigDecimal(imagValue);
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(String realValue, String imagValue) {
        assert (realValue != null) && (imagValue != null);
        real = new BigDecimal(realValue);
        imag = new BigDecimal(imagValue);
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(BigDecimal value) {
        assert value != null;
        real = value;
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }
    //I'll make a copy constructor even though it isn't necessary for an immutable class
    public FMNumber(FMNumber value) throws ExpressionException {
        assert value != null;
        if (value.IsImaginary()) {
            throw new ExpressionException("Invalid (imaginary) parameter for number construction");
        }
        real = value.RealValue();
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(long value) {
        real = new BigDecimal(value);
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(double value) {
        real = new BigDecimal(value);
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }

    public FMNumber(String value) {
        assert (value != null);
        real = new BigDecimal(value);
        imag = BigDecimal.ZERO;
        defaultContext = MathContext.DECIMAL128;
    }

    public boolean IsImaginary() {
        return (imag.compareTo(BigDecimal.ZERO) != 0);
    }

    public boolean IsReal() {
        return (imag.compareTo(BigDecimal.ZERO) == 0);
    }

    public BigDecimal RealValue() {
        return real;
    }

    public BigDecimal ImaginaryValue() {
        return imag;
    }

    public FMNumber Add(FMNumber addValue, MathContext context) {
        BigDecimal realTotal = real.add(addValue.RealValue(), context);
        BigDecimal imagTotal = imag.add(addValue.ImaginaryValue(), context);
        return new FMNumber(realTotal, imagTotal);
    }

    public FMNumber Subtract(FMNumber subtractValue, MathContext context) {
        BigDecimal realTotal = real.subtract(subtractValue.RealValue(), context);
        BigDecimal imagTotal = imag.subtract(subtractValue.ImaginaryValue(), context);
        return new FMNumber(realTotal, imagTotal);
    }

    public FMNumber Multiply(FMNumber multiplyValue, MathContext context) {
        BigDecimal realTotal = real.multiply(multiplyValue.RealValue(), context).subtract(imag.multiply(multiplyValue.ImaginaryValue(), context), context);
        BigDecimal imagTotal = imag.multiply(multiplyValue.RealValue(), context).add(real.multiply(multiplyValue.ImaginaryValue(), context), context);
        return new FMNumber(realTotal, imagTotal);
    }

    public FMNumber Divide(FMNumber divideValue, MathContext context) {
        BigDecimal realNom = real.multiply(divideValue.RealValue(), context).add(imag.multiply(divideValue.ImaginaryValue(), context), context);
        BigDecimal imagNom = imag.multiply(divideValue.RealValue(), context).subtract(real.multiply(divideValue.ImaginaryValue(), context), context);
        BigDecimal denom = divideValue.RealValue().pow(2, context).add(divideValue.ImaginaryValue().pow(2, context), context);
        return new FMNumber(realNom.divide(denom, context), imagNom.divide(denom, context));
    }

    public FMNumber Abs(MathContext context) {
        BigDecimal realSquared = real.pow(2, context);
        BigDecimal imagSquared = imag.pow(2, context);
        return new FMNumber(new BigDecimal(StrictMath.sqrt(realSquared.add(imagSquared, context).doubleValue())));
    }

    public FMNumber Modulus(MathContext context) {
        return Abs(context);
    }

    public FMNumber Magnitude(MathContext context) {
        return Abs(context);
    }

    public FMNumber Negate(MathContext context) {
        return new FMNumber(real.negate(context), imag.negate(context));
    }
    
    public FMNumber Pow(int n, MathContext context) throws ExpressionException {
        //todo: change this to support complex
        if (IsImaginary()) throw new ExpressionException("Invalid (imaginary) base");
        return new FMNumber(real.pow(n, context));
    }

    //This class doesn't use the Number extension so that exceptions may be thrown from these methods.  
    public byte byteValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return byte value");
        }
        return real.byteValue();
    }

    public double doubleValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return double value");
        }
        return real.doubleValue();
    }

    public float floatValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return float value");
        }
        return real.floatValue();
    }

    public int intValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return int value");
        }
        return real.intValue();
    }

    public long longValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return long value");
        }
        return real.longValue();
    }

    public short shortValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return short value");
        }
        return real.shortValue();
    }

    public BigDecimal BigDecimalValue() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return BigDecimal value");
        }
        return real;
    }

    public BigInteger toBigIntegerExact() throws ExpressionException {
        if (IsImaginary()) {
            throw new ExpressionException("Imaginary number, cannot return exact BigInteger value");
        }
        return real.toBigIntegerExact();
    }

    public String toLatexString() {
        return "";
    }

    @Override
    public String toString() {

        StringBuilder realStrBuilder = new StringBuilder();
        StringBuilder imagStrBuilder = new StringBuilder();

        try {
            BigInteger realInteger = real.toBigIntegerExact();
            realStrBuilder.append(realInteger.toString());
        } catch (ArithmeticException arithExc) {
            try {
                java.text.DecimalFormat f = (java.text.DecimalFormat) java.text.DecimalFormat.getNumberInstance();
                f.setRoundingMode(FMProperties.GetRoundingMode());
                int displayPrecision = FMProperties.GetDisplayPrecision();
                f.setMaximumFractionDigits(displayPrecision);
                f.setMinimumFractionDigits(displayPrecision);
                String numberStr = f.format(real);

                String fractional = numberStr.substring(numberStr.length() - displayPrecision);
                if (Double.parseDouble(fractional) == 0) {
                    realStrBuilder.append(real.toEngineeringString());
                } else {
                    realStrBuilder.append(numberStr);
                }
            } catch (Exception e) {
                realStrBuilder.append(real.toPlainString());
            }
        }

        if (IsImaginary()) {
            try {
                BigInteger imagInteger = real.toBigIntegerExact();
                imagStrBuilder.append(imagInteger.toString());
            } catch (ArithmeticException arithExc) {
                try {
                    java.text.DecimalFormat f = (java.text.DecimalFormat) java.text.DecimalFormat.getNumberInstance();
                    f.setRoundingMode(FMProperties.GetRoundingMode());
                    int displayPrecision = FMProperties.GetDisplayPrecision();
                    f.setMaximumFractionDigits(displayPrecision);
                    f.setMinimumFractionDigits(displayPrecision);
                    String numberStr = f.format(real);

                    String fractional = numberStr.substring(numberStr.length() - displayPrecision);
                    if (Double.parseDouble(fractional) == 0) {
                        imagStrBuilder.append(real.toEngineeringString());
                    } else {
                        imagStrBuilder.append(numberStr);
                    }
                } catch (Exception e) {
                    imagStrBuilder.append(real.toPlainString());
                }
            }
        }

        String resultStr;
        if (IsReal()) {
            resultStr = realStrBuilder.toString();
        } else {
            resultStr = "(" + realStrBuilder.toString() + " ";
            if (imag.compareTo(BigDecimal.ZERO) > 0) {
                resultStr += "+ ";
            }
            resultStr += ")";
        }

        return resultStr;
    }

    public int compareTo(Object obj) {
        FMNumber compareNumber = (FMNumber) obj;
        //kind of a crude comparison
        return Abs(defaultContext).RealValue().compareTo(compareNumber.Abs(defaultContext).RealValue());
    }
}
