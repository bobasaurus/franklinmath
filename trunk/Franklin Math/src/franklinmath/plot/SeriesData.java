package franklinmath.plot;

import java.util.*;

import franklinmath.executor.*;
import franklinmath.expression.*;
import franklinmath.util.*;

/**
 * Represents point data generated from series specification.  
 * @author Allen Jordan
 */
public class SeriesData {

    protected Vector<Point> pointData;
    protected double lowY,  highY;
    protected SeriesInfo seriesInfo;

    public SeriesData(SeriesInfo info) {
        assert info != null;
        assert (info.GetExpression() != null);
        seriesInfo = info;
        pointData = new Vector<Point>();
        lowY = 0;
        highY = 1;

        long numPoints = 100;
        GenerateData(numPoints);
    }

    public Vector<Point> GetData() {
        return pointData;
    }

    public SeriesInfo GetSeriesInfo() {
        return seriesInfo;
    }

    public double GetLowY() {
        return lowY;
    }

    public double GetHighY() {
        return highY;
    }

    /**
     * Generate point data from the series expression.  
     * @param numPoints
     */
    public void GenerateData(long numPoints) {
        Expression expr = seriesInfo.GetExpression();
        double lowX = seriesInfo.GetLowX();
        double highX = seriesInfo.GetHighX();
        assert highX >= lowX;
        double xIncrement = (highX - lowX) / ((double) numPoints);

        //keep track of lowest and highest y values
        lowY = Double.MAX_VALUE;
        highY = Double.MIN_VALUE;

        for (double currentX = lowX; currentX < highX; currentX += xIncrement) {
            try {
                Expression replacedExpr = expr.Replace(seriesInfo.GetVariableName(), new Expression(new Term(new Power(new Factor(currentX))), TermOperator.NONE));
                Expression resultExpr = ExpressionTools.Flatten(replacedExpr, null, null, null, null, null);
                FMNumber yValue = resultExpr.GetSingleNumber();
                if (yValue == null) {
                    pointData.add(Point.BAD_POINT);
                } else if (yValue.IsImaginary()) {
                    pointData.add(Point.BAD_POINT);
                } else {
                    double currentY = yValue.RealValue().doubleValue();
                    if (currentY < lowY) {
                        lowY = currentY;
                    }
                    if (currentY > highY) {
                        highY = currentY;
                    }
                    pointData.add(new Point(currentX, currentY));
                }
            } catch (ExpressionException ex) {
                pointData.add(Point.BAD_POINT);
            } catch (ExecutionException ex) {
                pointData.add(Point.BAD_POINT);
            }
        }

    }
}
