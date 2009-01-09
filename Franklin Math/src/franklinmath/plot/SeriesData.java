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
    protected SeriesInfo seriesInfo;
    protected ExpressionToolset expressionToolset;

    public SeriesData(SeriesInfo info, ExpressionToolset exprToolset) {
        assert info != null;
        assert (info.GetExpression() != null);
        expressionToolset = exprToolset;
        
        seriesInfo = info;
        pointData = new Vector<Point>();

        long numPoints = FMProperties.GetNumPlotPoints();
        GenerateData(numPoints);
    }

    public Vector<Point> GetData() {
        return pointData;
    }

    public SeriesInfo GetSeriesInfo() {
        return seriesInfo;
    }

    /**
     * Generate point data from the series expression.  
     * @param numPoints
     */
    public void GenerateData(long numPoints) {
        Expression expr = seriesInfo.GetExpression();
        Range xRange = seriesInfo.GetXRange();
        double lowX = xRange.low;
        double highX = xRange.high;
        double xIncrement = (highX - lowX) / ((double) numPoints - 1);

        //keep track of lowest and highest y values
        double lowY = Double.POSITIVE_INFINITY;
        double highY = Double.NEGATIVE_INFINITY;

        for (double currentX = lowX; currentX < highX; currentX += xIncrement) {
            try {
                //try to reduce the propagated arithmetic error at the endpoint
                if ((currentX + xIncrement) >= highX) {
                    currentX = highX;
                }
                Expression replacedExpr = expr.Replace(seriesInfo.GetVariableName(), new Expression(new Term(new Power(new Factor(currentX))), TermOperator.NONE));
                Expression resultExpr = expressionToolset.Flatten(replacedExpr);
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
            } catch (ArithmeticException ex) {
                pointData.add(Point.BAD_POINT);
            }
        }
        
        //if no range was specified, use the max and min from the data
        if (seriesInfo.GetYRange() == Range.BAD_RANGE) {
            seriesInfo.SetYRange(new Range(lowY, highY));
        }

    }
}
