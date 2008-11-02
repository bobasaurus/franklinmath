package franklinmath.util;

/**
 * Class representing a single 2-dimensional point.  
 * @author Allen Jordan
 */
public class Point {
    //For ease of access, just expose these as public.  
    public double x,  y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point copyPoint) {
        x = copyPoint.x;
        y = copyPoint.y;
    }
    public static Point BAD_POINT = new Point(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    @Override
    public String toString() {
        return x + "," + y;
    }
}
