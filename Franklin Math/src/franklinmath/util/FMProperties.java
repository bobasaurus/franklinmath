package franklinmath.util;

import java.util.Properties;
import java.io.*;
import java.math.*;

/**
 * Class to handle the loading and saving of properties.  Making these methods static is kind of tacky, but it saves lots of reference passing.  
 * @author Allen Jordan
 */
public class FMProperties {
    protected static String filename = "FMProperties.properties";
    protected static Properties properties = new Properties();
    protected static boolean isLoaded = false;

    public static synchronized void LoadProperties() throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            FileInputStream stream = new FileInputStream(file);
            properties.load(stream);
            stream.close();
            isLoaded = true;
        } else {
            throw new IOException("Can't locate the properties file: " + filename);
        }
    }
    
    public static synchronized boolean IsLoaded() {
        return isLoaded;
    }

    public static synchronized void LoadDefaults() {
        SetPrecision(16);
    }

    public static synchronized void SetPrecision(int value) {
        SetInt("number.precision", value);
    }

    public static synchronized int GetPrecision() {
        return GetInt("number.precision");
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
        properties.store(stream, "Options/Properties for Franklin Math");
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
