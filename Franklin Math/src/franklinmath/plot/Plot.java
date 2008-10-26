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

    public Plot() {
        seriesCollection = new Vector<SeriesData>();
        this.repaint();
    }

    public Plot(SeriesData series) {
        seriesCollection = new Vector<SeriesData>();
        AddSeries(series);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        //render each data series
        for (int i = 0; i < seriesCollection.size(); i++) {
            SeriesData seriesData = seriesCollection.get(i);
            SeriesInfo seriesInfo = seriesData.GetSeriesInfo();

            //set the graphic options
            int thickness = seriesInfo.GetThickness();
            BasicStroke stroke = new BasicStroke(thickness);
            g2d.setColor(seriesInfo.GetColor());

            Vector<franklinmath.util.Point> pointData = seriesData.GetData();
            //determine sizing information
            int windowWidth = this.getWidth();
            int windowHeight = this.getHeight();
            double aspectX = ((double) windowWidth) / (seriesInfo.GetHighX() - seriesInfo.GetLowX());
            double aspectY = ((double) windowHeight) / (seriesData.GetHighY() - seriesData.GetLowY());

            //render the data series
            int currentX = (int) (pointData.get(0).x * aspectX);
            int currentY = (int) (pointData.get(0).y * aspectY);
            if (seriesInfo.GetSeriesStyle() == SeriesStyle.POINTS) {
                g2d.fillRect(currentX, currentY, thickness, thickness);
            }
            for (int j = 1; j < pointData.size(); j++) {
                int prevX = currentX;
                int prevY = currentY;
                currentX = (int) (pointData.get(j).x * aspectX);
                currentY = (int) (pointData.get(j).y * aspectY);

                SeriesStyle style = seriesInfo.GetSeriesStyle();
                if (style == SeriesStyle.POINTS) {
                    g2d.fillRect(currentX, currentY, thickness, thickness);
                } else if (style == SeriesStyle.SOLID_LINE) {
                    g2d.drawLine(prevX, prevY, currentX, currentY);
                }
            }
        }
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
