package franklinmath.plot;

import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 * A class for plotting series data.  
 * @author Allen Jordan
 */

//todo: make this an image instead of a panel
public class Plot extends JPanel {

    protected Vector<SeriesData> seriesCollection;
    //graph coordinate variables
    protected int borderSize,  windowWidth,  windowHeight,  plotWidth,  plotHeight,  plotStartX,  plotStartY,  plotEndX,  plotEndY;

    public Plot() {
        seriesCollection = new Vector<SeriesData>();
        borderSize = 10;
        windowWidth = 0;
        windowHeight = 0;
        InitializeCoordinateData();
        this.repaint();
    }

    public Plot(SeriesData series) {
        seriesCollection = new Vector<SeriesData>();
        borderSize = 10;
        windowWidth = 0;
        windowHeight = 0;
        InitializeCoordinateData();
        AddSeries(series);
    }

    protected void InitializeCoordinateData() {
        if (windowWidth == 0) windowWidth = this.getWidth();
        if (windowHeight == 0) windowHeight = this.getHeight();
        plotWidth = windowWidth - 2 * borderSize;
        plotHeight = windowHeight - 2 * borderSize;
        plotStartX = borderSize;
        plotStartY = borderSize;
        plotEndX = windowWidth - borderSize;
        plotEndY = windowHeight - borderSize;
    }
    
    public void SetPlotDimensions(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        InitializeCoordinateData();

        //render each data series
        for (int i = 0; i < seriesCollection.size(); i++) {
            SeriesData seriesData = seriesCollection.get(i);
            SeriesInfo seriesInfo = seriesData.GetSeriesInfo();

            Vector<franklinmath.util.Point> pointData = seriesData.GetData();
            //determine sizing information
            franklinmath.util.Range xRange = seriesInfo.GetXRange();
            franklinmath.util.Range yRange = seriesInfo.GetYRange();
            double aspectX = ((double) plotWidth) / (xRange.GetWidth());
            double aspectY = ((double) plotHeight) / (yRange.GetWidth());

            franklinmath.util.Point origin = DataToPlotTransform(new franklinmath.util.Point(0, 0), aspectX, aspectY, xRange, yRange);
            DrawAxis(g2d, origin, xRange, yRange);

            //set the graphic options
            int thickness = seriesInfo.GetThickness();
            BasicStroke stroke = new BasicStroke(thickness);
            g2d.setStroke(stroke);
            g2d.setColor(seriesInfo.GetColor());

            //antialias the line
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //render the data series
            franklinmath.util.Point point;
            int index = 0;
            //locate a good first point
            do {
                point = pointData.get(index);
                index++;
                if (index >= pointData.size()) {
                    g2d.drawString("Series contains entirely bad points", 20, 20);
                    return;
                }
            } while (point == franklinmath.util.Point.BAD_POINT);

            //transform to plot coordinates
            point = DataToPlotTransform(point, aspectX, aspectY, xRange, yRange);
            int currentX = (int) Math.round(point.x);
            int currentY = (int) Math.round(point.y);

            if (seriesInfo.GetSeriesStyle() == SeriesStyle.POINTS) {
                g2d.fillRect(currentX, currentY, thickness, thickness);
            }

            for (int j = index; j < pointData.size(); j++) {
                int prevX = currentX;
                int prevY = currentY;

                point = pointData.get(j);
                if (point != franklinmath.util.Point.BAD_POINT) {

                    //transform to plot coordinates
                    point = DataToPlotTransform(point, aspectX, aspectY, xRange, yRange);
                    currentX = (int) Math.round(point.x);
                    currentY = (int) Math.round(point.y);

                    SeriesStyle style = seriesInfo.GetSeriesStyle();
                    if (style == SeriesStyle.POINTS) {
                        g2d.fillRect(currentX, currentY, thickness, thickness);
                    } else if (style == SeriesStyle.SOLID_LINE) {
                        g2d.drawLine(prevX, prevY, currentX, currentY);
                    }
                }
            }
        }
    }

    protected void DrawAxis(Graphics2D g2d, franklinmath.util.Point origin, franklinmath.util.Range xRange, franklinmath.util.Range yRange) {
        g2d.setColor(Color.black);
        if (origin.x < plotStartX) {
            origin.x = plotStartX;
        } else if (origin.x > plotEndX) {
            origin.x = plotEndX;
        }

        if (origin.y < plotStartY) {
            origin.y = plotStartY;
        } else if (origin.y > plotEndY) {
            origin.y = plotEndY;
        }
        
        g2d.drawLine(plotStartX, (int)origin.y, plotEndX, (int)origin.y);
        g2d.drawLine((int)origin.x, plotStartY, (int)origin.x, plotEndY);
        
        
    }

    /**
     * Transform a data coordinate to fit within the plot window.  
     * @param dataPoint     The data point to transform
     * @param aspectX       The horizontal aspect ratio for the transformation
     * @param aspectY       The vertical aspect ratio for the transformation
     * @param xRange        The independent variable's range
     * @param yRange        The dependent variable's range
     * @return
     */
    protected franklinmath.util.Point DataToPlotTransform(franklinmath.util.Point dataPoint, double aspectX, double aspectY, franklinmath.util.Range xRange, franklinmath.util.Range yRange) {
        double normalizedDataX = dataPoint.x - xRange.low;
        double normalizedDataY = dataPoint.y - yRange.low;
        double plotX = normalizedDataX * aspectX + borderSize;
        double plotY = plotHeight - normalizedDataY * aspectY + borderSize;
        return new franklinmath.util.Point(plotX, plotY);
    }

    /**
     * Add a data series to this plot.  
     * @param series    The data series to add.  
     * @return          Returns the index of this series in the plot.  
     */
    public int AddSeries(SeriesData series) {
        seriesCollection.add(series);
        this.repaint();
        return seriesCollection.size() - 1;

    }
}
