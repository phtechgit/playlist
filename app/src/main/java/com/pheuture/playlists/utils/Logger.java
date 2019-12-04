package com.pheuture.playlists.utils;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * creator: Shashank
 * date: 27-Dec-18.
 */
public class Logger {

    private Logger() {
        throw new IllegalStateException("utility class should not be instantiated ");
    }

    public static void e(String tag, String message) {
        if (message!=null) {
            Log.e(tag, message);
            Calendar calendar = Calendar.getInstance();
            appendLog(CalenderUtils.getFullTimeWithSecAndMillis(calendar) + " " + CalenderUtils.getFullDate(calendar)+ " " + ": " + tag + ": " + message + "\n");
        }
    }
    public static void d(String tag, String message) {
        if (message!=null) {
            Log.d(tag, message);
        }
    }
    public static void i(String tag, String message) {
        if (message!=null) {
            Log.i(tag, message);
        }
    }
    public static void w(String tag, String message) {
        if (message!=null) {
            Log.w(tag, message);
        }
    }

    private static void appendLog(String text) {
        Calendar calendar = Calendar.getInstance();
        File logFile = new File(Environment.getExternalStorageDirectory(), "WhyFi" + "_log_"  + CalenderUtils.getFullDate(calendar).replaceAll("/","") + ".file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
