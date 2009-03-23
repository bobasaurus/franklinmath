/*
Copyright 2009 Allen Franklin Jordan (allen.jordan@gmail.com).

This file is part of Franklin Math.

Franklin Math is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Franklin Math is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Franklin Math.  If not, see <http://www.gnu.org/licenses/>.
 */
package franklinmath.plot;

import java.util.*;
import java.awt.*;
import java.awt.image.*;

import franklinmath.util.*;

/**
 * A class for plotting series data.  
 * @author Allen Jordan
 */

//todo: make this an image instead of a panel
public class Plot {

    protected Vector<SeriesData> seriesCollection;
    //graph coordinate variables
    protected int borderSize,  windowWidth,  windowHeight,  internalPlotWidth,  internalPlotHeight,  plotStartX,  plotStartY,  plotEndX,  plotEndY;

    public Plot() {
        seriesCollection = new Vector<SeriesData>();
        InitializeCoordinateData();
    }

    public Plot(SeriesData series) {
        seriesCollection = new Vector<SeriesData>();
        InitializeCoordinateData();
        AddSeries(series);
    }

    protected void InitializeCoordinateData() {
        borderSize = 50;
        windowWidth = FMProperties.GetPlotWidth();
        windowHeight = FMProperties.GetPlotHeight();
        internalPlotWidth = windowWidth - 2 * borderSize;
        internalPlotHeight = windowHeight - 2 * borderSize;
        plotStartX = borderSize;
        plotStartY = borderSize;
        plotEndX = windowWidth - borderSize;
        plotEndY = windowHeight - borderSize;
    }

    public BufferedImage GetPlotImage() {
        BufferedImage image = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, windowWidth, windowHeight);
        paint(graphics);
        return image;
    }

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
            double aspectX = ((double) internalPlotWidth) / (xRange.GetWidth());
            double aspectY = ((double) internalPlotHeight) / (yRange.GetWidth());

            franklinmath.util.Point origin = DataToPlotTransform(new franklinmath.util.Point(0, 0), aspectX, aspectY, xRange, yRange);
            DrawAxis(g2d, origin, xRange, yRange, aspectX, aspectY);

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

    //Draw the horizontal axis and vertical axis
    protected void DrawAxis(Graphics2D g2d, franklinmath.util.Point origin, franklinmath.util.Range xRange, franklinmath.util.Range yRange, double aspectX, double aspectY) {
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

        g2d.drawLine(plotStartX, (int) origin.y, plotEndX, (int) origin.y);
        g2d.drawLine((int) origin.x, plotStartY, (int) origin.x, plotEndY);

        double tickSpacingX = GetTickSpacing(xRange.GetWidth());
        double tickSpacingY = GetTickSpacing(yRange.GetWidth());

        //calculate a clean starting value for the X ticks
        double tickStartX = xRange.low;
        if (tickSpacingX > 1) {
            tickSpacingX = (int) tickSpacingX;
            tickStartX = Math.ceil(tickStartX);
            double tickFraction = tickStartX / tickSpacingX;
            while (tickFraction != ((int) tickFraction)) {
                tickStartX++;
                if (tickStartX > xRange.high) {
                    return;
                }
                tickFraction = tickStartX / tickSpacingX;
            }
        }

        //calculate a clean starting value for the Y ticks
        double tickStartY = yRange.low;
        if (tickSpacingY > 1) {
            tickSpacingY = (int) tickSpacingY;
            tickStartY = Math.ceil(tickStartY);
            double tickFraction = tickStartY / tickSpacingY;
            while (tickFraction != ((int) tickFraction)) {
                tickStartY++;
                if (tickStartY > yRange.high) {
                    return;
                }
                tickFraction = tickStartY / tickSpacingY;
            }
        }

        assert tickSpacingX > 0;
        assert tickSpacingY > 0;
        int tickHalfLength = windowWidth / 100;
        Font font = new Font(g2d.getFont().getFontName(), Font.PLAIN, 10);
        g2d.setFont(font);
        if (tickHalfLength <= 0) {
            tickHalfLength = 1;
        }
        //draw the X ticks and labels
        for (double tickValue = tickStartX; tickValue <= xRange.high; tickValue += tickSpacingX) {
            double plotTickValueX = DataToPlotTransform(new franklinmath.util.Point(tickValue, 0), aspectX, aspectY, xRange, yRange).x;
            g2d.setColor(Color.BLACK);
            g2d.drawLine((int) plotTickValueX, (int) origin.y - tickHalfLength, (int) plotTickValueX, (int) origin.y + tickHalfLength);

            g2d.setColor(Color.GRAY);

            String labelString;
            if (tickSpacingX >= .01) {
                labelString = String.format("%.2f", tickValue);
            } else {
                labelString = String.format("%.2E", tickValue);
            }
            g2d.drawString(labelString, (int) (plotTickValueX - labelString.length() / 2 * 5), (int) origin.y + tickHalfLength + 10);
        }
        //draw the Y ticks and labels
        for (double tickValue = tickStartY; tickValue <= yRange.high; tickValue += tickSpacingY) {
            double plotTickValueY = DataToPlotTransform(new franklinmath.util.Point(0, tickValue), aspectY, aspectY, xRange, yRange).y;
            g2d.drawLine((int) origin.x - tickHalfLength, (int) plotTickValueY, (int) origin.x + tickHalfLength, (int) plotTickValueY);

            g2d.setColor(Color.GRAY);

            String labelString;
            if (tickSpacingY >= .01) {
                labelString = String.format("%.2f", tickValue);
            } else {
                labelString = String.format("%.2E", tickValue);
            }
            g2d.drawString(labelString, (int) origin.x - labelString.length() * 5 - tickHalfLength - 2, (int) plotTickValueY + 5);
        }

        return;
    }

    protected double GetTickSpacing(double rangeWidth) {
        double tickSpacing = rangeWidth / 6;

        if (rangeWidth <= 5) {
            tickSpacing = 1;
        } else if (rangeWidth <= 10) {
            tickSpacing = 2;
        } else if (rangeWidth <= 30) {
            tickSpacing = 5;
        } else if (rangeWidth <= 50) {
            tickSpacing = 10;
        } else if (rangeWidth <= 100) {
            tickSpacing = 20;
        } else if (rangeWidth <= 300) {
            tickSpacing = 50;
        } else if (rangeWidth <= 500) {
            tickSpacing = 100;
        } else if (rangeWidth <= 1000) {
            tickSpacing = 200;
        } else if (rangeWidth <= 3000) {
            tickSpacing = 500;
        } else if (rangeWidth <= 5000) {
            tickSpacing = 1000;
        } else if (rangeWidth <= 10000) {
            tickSpacing = 2000;
        } else if (rangeWidth <= 30000) {
            tickSpacing = 5000;
        } else if (rangeWidth <= 50000) {
            tickSpacing = 10000;
        } else if (rangeWidth <= 100000) {
            tickSpacing = 20000;
        } else if (rangeWidth <= 300000) {
            tickSpacing = 50000;
        } else if (rangeWidth <= 500000) {
            tickSpacing = 100000;
        }

        return tickSpacing;
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
        double plotY = internalPlotHeight - normalizedDataY * aspectY + borderSize;
        return new franklinmath.util.Point(plotX, plotY);
    }

    /**
     * Add a data series to this plot.  
     * @param series    The data series to add.  
     * @return          Returns the index of this series in the plot.  
     */
    public int AddSeries(SeriesData series) {
        seriesCollection.add(series);
        return seriesCollection.size() - 1;
    }
}
