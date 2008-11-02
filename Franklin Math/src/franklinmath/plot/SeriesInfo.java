package franklinmath.plot;

import java.awt.Color;

import franklinmath.expression.*;
import franklinmath.util.*;

/**
 * Container for information about a data series.  
 * @author Allen Jordan
 */
public class SeriesInfo {

    protected Expression expr;
    protected String varName,  xLabel,  yLabel,  title;
    protected Range xRange;
    protected Range yRange;
    protected Color color;
    protected int thickness;
    protected SeriesStyle style;

    public SeriesInfo() {
        SetDefaults();
    }
    
    public SeriesInfo(Expression expr, String varName, Range xRange) {
        SetDefaults();
        this.expr = expr;
        this.varName = varName;
        this.xLabel = varName;
        this.xRange = xRange;
        yRange = Range.BAD_RANGE;
    }
    
    public SeriesInfo(Expression expr, String varName, Range xRange, Range yRange) {
        SetDefaults();
        this.expr = expr;
        this.varName = varName;
        this.xLabel = varName;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public SeriesInfo(Expression expr, String varName, double lowX, double highX) {
        SetDefaults();
        this.expr = expr;
        this.varName = varName;
        this.xLabel = varName;
        xRange = new Range(lowX, highX);
        yRange = Range.BAD_RANGE;
    }
    
    public SeriesInfo(Expression expr, String varName, double lowX, double highX, double lowY, double highY) {
        SetDefaults();
        this.expr = expr;
        this.varName = varName;
        this.xLabel = varName;
        xRange = new Range(lowX, highX);
        yRange = new Range(lowY, highY);
    }

    /*public void SetExpression(Expression expr) {
    this.expr = expr;
    }*/
    /*public void SetVariableName(String varName) {
    this.varName = varName;
    }*/
    public void SetXLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public void SetYLabel(String yLabel) {
        this.yLabel = yLabel;
    }

    public void SetTitle(String title) {
        this.title = title;
    }

    public void SetXRange(Range xRange) {
        this.xRange = xRange;
    }
    
    public void SetYRange(Range yRange) {
        this.yRange = yRange;
    }

    public void SetColor(Color color) {
        this.color = color;
    }

    public void SetThickness(int thickness) {
        this.thickness = thickness;
    }
    
    public void SetSeriesStyle(SeriesStyle style) {
        this.style = style;
    }

    public Expression GetExpression() {
        return expr;
    }

    public String GetVariableName() {
        return varName;
    }

    public String GetXLabel() {
        return xLabel;
    }

    public String GetYLabel() {
        return yLabel;
    }

    public String GetTitle() {
        return title;
    }

    public Range GetXRange() {
        return xRange;
    }
    
    public Range GetYRange() {
        return yRange;
    }

    public Color GetColor() {
        return color;
    }

    public int GetThickness() {
        return thickness;
    }
    
    public SeriesStyle GetSeriesStyle() {
        return style;
    }

    protected void SetDefaults() {
        xLabel = "x";
        yLabel = "y";
        title = "";
        color = Color.BLUE;
        thickness = 1;
        style = SeriesStyle.SOLID_LINE;
    }
}
