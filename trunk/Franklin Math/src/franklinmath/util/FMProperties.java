package franklinmath.util;

import java.util.Properties;
import java.io.*;
import java.math.*;

import franklinmath.expression.*;

/**
 * Class to handle the loading and saving of properties.  Making these methods static is kind of tacky, but it saves lots of reference passing.  
 * @author Allen Jordan
 */
public class FMProperties {

    protected static String filename = "fmproperties.xml";
    protected static Properties properties = new Properties();
    protected static boolean isLoaded = false;

    //Prevent instantiation
    protected FMProperties() {
    }

    public static synchronized void LoadProperties() throws IOException {
        File file = new File(filename);
        //create the properties file if it doesn't exist
        if (!file.exists()) {
            file.createNewFile();
            LoadDefaults();
            SaveProperties();
        }
        FileInputStream stream = new FileInputStream(file);
        properties.loadFromXML(stream);
        stream.close();
        isLoaded = true;
    }

    /**
     * Check to see if the properties file has been loaded.  
     * @return  Returns true if the properties file is loaded.  
     */
    public static synchronized boolean IsLoaded() {
        return isLoaded;
    }

    /**
     * Load the default property settings.  
     */
    public static synchronized void LoadDefaults() {
        SetPrecision(34);
        SetRoundingMode(java.math.RoundingMode.HALF_EVEN);
        SetDisplayPrecision(15);
        SetNumPlotPoints(70);
        SetPlotWidth(400);
        SetPlotHeight(300);
    }

    public static synchronized void SetPrecision(int value) {
        SetInt("number.precision", value);
    }

    public static synchronized int GetPrecision() {
        return GetInt("number.precision");
    }
    
    public static synchronized void SetNumPlotPoints(long value) {
        SetLong("plot.numPoints", value);
    }
    
    public static synchronized long GetNumPlotPoints() {
        return GetLong("plot.numPoints");
    }
    
    public static synchronized void SetPlotWidth(int width) {
        if (width < 100) width = 100;
        SetInt("plot.width", width);
    }
    
    public static synchronized int GetPlotWidth() {
        return GetInt("plot.width");
    }
    
    public static synchronized void SetPlotHeight(int height) {
        if (height < 100) height = 100;
        SetInt("plot.height", height);
    }
    
    public static synchronized int GetPlotHeight() {
        return GetInt("plot.height");
    }
    
    public static synchronized void SetRoundingModeIndex(int mode) {
        SetInt("number.rounding", mode);
    }

    public static synchronized int GetRoundingModeIndex() {
        return GetInt("number.rounding");
    }

    public static synchronized void SetRoundingMode(RoundingMode mode) {
        int index = 0;
        switch (mode) {
            case CEILING:
                index = 0;
                break;
            case FLOOR:
                index = 1;
                break;
            case UP:
                index = 2;
                break;
            case DOWN:
                index = 3;
                break;
            case HALF_EVEN:
                index = 4;
                break;
            case HALF_UP:
                index = 5;
                break;
            case HALF_DOWN:
                index = 6;
                break;
            case UNNECESSARY:
                index = 7;
                break;
        }
        SetRoundingModeIndex(index);
    }

    public static synchronized RoundingMode GetRoundingMode() {
        int index = GetRoundingModeIndex();
        RoundingMode mode = RoundingMode.CEILING;
        switch (index) {
            case 0:
                mode = RoundingMode.CEILING;
                break;
            case 1:
                mode = RoundingMode.FLOOR;
                break;
            case 2:
                mode = RoundingMode.UP;
                break;
            case 3:
                mode = RoundingMode.DOWN;
                break;
            case 4:
                mode = RoundingMode.HALF_EVEN;
                break;
            case 5:
                mode = RoundingMode.HALF_UP;
                break;
            case 6:
                mode = RoundingMode.HALF_DOWN;
                break;
            case 7:
                mode = RoundingMode.UNNECESSARY;
                break;
        }

        return mode;
    }

    public static synchronized void SetDisplayPrecision(int value) {
        SetInt("display.decimal.precision", value);
    }

    public static synchronized int GetDisplayPrecision() {
        return GetInt("display.decimal.precision");
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////
    public static synchronized void SaveProperties() throws IOException {
        FileOutputStream stream = new FileOutputStream(filename);
        properties.storeToXML(stream, "Options/Properties for Franklin Math");
        stream.close();
    }

    protected static void SetString(String name, String str) {
        properties.setProperty(name, str);
    }

    protected static String GetString(String name) {
        return properties.getProperty(name);
    }

    protected static void SetInt(String name, int value) {
        properties.setProperty(name, String.valueOf(value));
    }

    protected static int GetInt(String name) throws NumberFormatException {
        return Integer.parseInt(properties.getProperty(name));
    }
    
    protected static void SetLong(String name, long value) {
        properties.setProperty(name, String.valueOf(value));
    }

    protected static long GetLong(String name) throws NumberFormatException {
        return Long.parseLong(properties.getProperty(name));
    }

    protected static void SetDouble(String name, double value) {
        properties.setProperty(name, String.valueOf(value));
    }

    protected static double GetDouble(String name) throws NumberFormatException {
        return Double.parseDouble(properties.getProperty(name));
    }

    protected static void SetFMNumber(String name, FMNumber value) {
        properties.setProperty(name, value.toString());
    }

    protected static FMNumber GetFMNumber(String name) throws NumberFormatException {
        return new FMNumber(properties.getProperty(name));
    }
}
