package com.bbpos.bbdevice.util.log;

import android.util.Log;

import com.bbpos.bbdevice.BuildConfig;

import java.util.Date;

public class ConsoleLogger implements ILogger
{
    private final static boolean DEBUG = BuildConfig.DEBUG;
    public void log(int level, Date date, String area, String message)
    {
        if (!DEBUG){
            return;
        }
        String text = DateTimeUtility.getDateTimeString(date) + " " + message;
        switch (level)
        {
        case LogLevel.DEBUG:
            Log.d(area, text);
            break;
        case LogLevel.INFO:
            Log.i(area, text);
            break;
        case LogLevel.WARNING:
            Log.w(area, text);
            break;
        case LogLevel.ERROR:
            Log.e(area, text);
            break;
        default:
            Log.d(area, text);
            break;
        }
    }
}