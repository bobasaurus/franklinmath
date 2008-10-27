package franklinmath.plot;

import java.awt.Color;

import franklinmath.expression.*;

/**
 * Container for information about a data series.  
 * @author Allen Jordan
 */
public class SeriesInfo {

    protected Expression expr;
    protected String varName,  xLabel,  yLabel,  title;
    protected double lowX,  highX;
    protected Color color;
    protected int thickness;
    protected SeriesStyle style;

    public SeriesInfo() {
        SetDefaults();
    }

    public SeriesInfo(Expression expr, String varName, double lowX, double highX) {
        SetDefaults();
        this.expr = expr;
        this.varName = varName;
        this.lowX = lowX;
        this.highX = highX;
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

    public void SetLowX(double lowX) {
        this.lowX = lowX;
    }

    public void SetHighX(double highX) {
        this.highX = highX;
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

    public double GetLowX() {
        return lowX;
    }

    public double GetHighX() {
        return highX;
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
        expr = null;
        varName = "x";
        xLabel = "x";
        yLabel = "y";
        title = "";
        lowX = 0;
        highX = 1;
        color = Color.BLACK;
        thickness = 2;
        style = SeriesStyle.SOLID_LINE;
    }
}
