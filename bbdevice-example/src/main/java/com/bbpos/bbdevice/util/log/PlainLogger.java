package com.bbpos.bbdevice.util.log;

import android.util.Log;

import com.bbpos.bbdevice.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class PlainLogger implements ILogger {
    // 是否是debug状态
    private final static boolean DEBUG = BuildConfig.DEBUG;
    private final static String TAG = "bbpos";
    private final Object _sync = new Object();
    private File file;
    private static PlainLogger logger;

    public static PlainLogger instance(String filePath) {
        HLog.i("FDL", "filePath =" + filePath);
        if (logger == null) {
            synchronized (PlainLogger.class) {
                if (logger == null) {
                    logger = new PlainLogger(filePath);
                }
            }
        }
        return logger;
    }

    public String getFileAbsolutePath() {
        return file.getAbsolutePath();
    }

    private PlainLogger(String filePath) {
        String absolutePath = filePath;
        file = new File(absolutePath);
        if (!file.exists()) {
            StorageUtility.writeStringAsFile("Log file installed at " + DateTimeUtility.getDateTimeString(new Date()), file);
        }
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    private void writeFile(String text) {
        if (file.length() > 10 * 1024 * 1024) {
            String filePath = file.getAbsolutePath();
            file.delete();
            file = new File(filePath);
            StorageUtility.writeStringAsFile("Log file re-created. " + DateTimeUtility.getDateTimeString(new Date()), file);
        }

        Writer out = null;
        try {
            synchronized (_sync) {
                if (file.canWrite()) {
                    out = new BufferedWriter(new FileWriter(file, true), 1024);
                    out.write(text);
                    out.close();
                    out = null;
                }
            }
        } catch (IOException e) {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    Log.e("PlainLogger", "Failed to close file. Fallback to system log here, because file log fails.");
                    ex.printStackTrace();
                }
            }
            // Fallback to system log here, because file log fails.
            Log.e("PlainLogger", "Failed to write log to file. Fallback to system log here, because file log fails.");
            e.printStackTrace();
        }
    }

    @Override
    public void log(int level, Date date, String area, String message) {
        String text = DateTimeUtility.getDateTimeString(date) + "  " + area + "\n" + message + "\n\n";
        String txt = area + ":" + message;
        switch (level) {
            case LogLevel.DEBUG:
                if (DEBUG) {
                    Log.d(TAG, txt);
                    writeFile("DEBUG  " + text);
                }
                break;
            case LogLevel.INFO:
                if (DEBUG) {
                    Log.i(TAG, txt);
                    writeFile("INFO  " + text);
                }
                break;
            case LogLevel.WARNING:
                if (DEBUG) {
                    Log.w(TAG, txt);
                    writeFile("WARNING  " + text);
                }
                break;
            case LogLevel.ERROR:
                if (DEBUG) {
                    Log.e(TAG, txt);
                    writeFile("ERROR  " + text);
                }
                break;
            default:
                if (DEBUG) {
                    Log.d(TAG, txt);
                    writeFile("DEBUG  " + text);
                }
                break;
        }
    }
}
