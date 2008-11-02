package franklinmath.plot;

import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 * A class for plotting series data.  
 * @author Allen Jordan
 */
public class Plot extends JPanel {

    protected Vector<SeriesData> seriesCollection;
    //graph coordinate variables
    protected int borderSize,  windowWidth,  windowHeight,  plotWidth,  plotHeight,  plotStartX,  plotStartY,  plotEndX,  plotEndY;

    public Plot() {
        seriesCollection = new Vector<SeriesData>();
        borderSize = 10;
        InitializeCoordinateData();
        this.repaint();
    }

    public Plot(SeriesData series) {
        seriesCollection = new Vector<SeriesData>();
        borderSize = 10;
        InitializeCoordinateData();
        AddSeries(series);
    }

    private void InitializeCoordinateData() {
        windowWidth = this.getWidth();
        windowHeight = this.getHeight();
        plotWidth = windowWidth - 2 * borderSize;
        plotHeight = windowHeight - 2 * borderSize;
        plotStartX = borderSize;
        plotStartY = borderSize;
        plotEndX = windowWidth - borderSize;
        plotEndY = windowHeight - borderSize;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        InitializeCoordinateData();

        //render each data series
        for (int i = 0; i < seriesCollection.size(); i++) {
            SeriesData seriesData = seriesCollection.get(i);
            SeriesInfo seriesInfo = seriesData.GetSeriesInfo();

            //set the graphic options
            int thickness = seriesInfo.GetThickness();
            BasicStroke stroke = new BasicStroke(thickness);
            g2d.setStroke(stroke);
            g2d.setColor(seriesInfo.GetColor());

            Vector<franklinmath.util.Point> pointData = seriesData.GetData();
            //determine sizing information
            franklinmath.util.Range xRange = seriesInfo.GetXRange();
            franklinmath.util.Range yRange = seriesInfo.GetYRange();
            double aspectX = ((double) plotWidth) / (xRange.GetWidth());
            double aspectY = ((double) plotHeight) / (yRange.GetWidth());


            //DrawAxis(g2d, borderSize, windowWidth, windowHeight, seriesInfo.GetLowX(), seriesInfo.GetHighX(), seriesData.GetLowY(), seriesData.GetHighY());

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

    private void DrawAxis(Graphics2D g2d, int borderSize, int windowWidth, int windowHeight, double lowX, double highX, double lowY, double highY) {
        int startWidth = borderSize;
        int endWidth = windowWidth - borderSize;
        int startHeight = borderSize;
        int endHeight = windowHeight - borderSize;

        Color backupColor = g2d.getColor();
        Stroke strokeBackup = g2d.getStroke();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));

        //draw straight axis lines
        g2d.drawLine(startWidth, startHeight, startWidth, endHeight);
        g2d.drawLine(startWidth, endHeight, endWidth, endHeight);


        g2d.setColor(backupColor);
        g2d.setStroke(strokeBackup);
    }

    private franklinmath.util.Point DataToPlotTransform(franklinmath.util.Point dataPoint, double aspectX, double aspectY, franklinmath.util.Range xRange, franklinmath.util.Range yRange) {
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
