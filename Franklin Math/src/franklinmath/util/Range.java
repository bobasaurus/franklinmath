package franklinmath.util;

/**
 * This class represents a range of numbers between two endpoints.  
 * @author Allen Jordan
 */
public class Range {
    public double low, high;
    
    public Range(double low, double high) {
        assert low <= high;
        this.low = low;
        this.high = high;
    }
    
    public double GetWidth() {
        return high-low;
    }
    
    public static Range BAD_RANGE = new Range(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    
    @Override
    public String toString() {
        return low + " <--> " + high;
    }
}
