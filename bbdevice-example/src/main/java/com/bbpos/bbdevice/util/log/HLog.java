package com.bbpos.bbdevice.util.log;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @author dlfeng
 */
public class HLog {
    public static final String FDL_TAG = "FDL";
    public static final String YL_TAG = "yl";
    public static final String LFP_TAG = "lfp";
    public static final String SSX_TAG = "ssx";
    public static final String TAG_RESULT = "result";
    private static ILogger _logger = new ConsoleLogger();

    public static void SetLogger(ILogger logger) {
        _logger = logger;
    }

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final int JSON_INDENT = 1;

    public static void d(String area, String message) {
        _logger.log(LogLevel.DEBUG, new Date(System.currentTimeMillis()), area, getClassInfo(message));
    }

    public static void d(String area, Object message) {
        i(area, getClassInfo(message.toString()));
    }

    public static void d(String area, String message, Exception exception) {
        _logger.log(LogLevel.DEBUG, new Date(System.currentTimeMillis()), area, getClassInfo(message + getExceptionStack(exception)));
    }


    public static void i(String area, String message) {
        _logger.log(LogLevel.INFO, new Date(System.currentTimeMillis()), area, getClassInfo(message));
    }

    public static void i(String area, Object message) {
        i(area, getClassInfo(message.toString()));
    }

    public static void i(String area, String message, Exception exception) {
        _logger.log(LogLevel.INFO, new Date(System.currentTimeMillis()), area, getClassInfo(message + getExceptionStack(exception)));
    }

    public static void w(String area, String message) {
        _logger.log(LogLevel.WARNING, new Date(System.currentTimeMillis()), area, getClassInfo(message));
    }

    public static void w(String area, String message, Exception exception) {
        _logger.log(LogLevel.WARNING, new Date(System.currentTimeMillis()), area, getClassInfo(message) + getExceptionStack(exception));
    }

    public static void e(String area, String message) {
        _logger.log(LogLevel.ERROR, new Date(System.currentTimeMillis()), area, getClassInfo(message));
    }

    public static void e(String area, String message, Exception exception) {
        _logger.log(LogLevel.ERROR, new Date(System.currentTimeMillis()), area, getClassInfo(message + getExceptionStack(exception)));
    }

    public static void e(String area, String message, Throwable exception) {
        _logger.log(LogLevel.ERROR, new Date(System.currentTimeMillis()), area, getClassInfo(message + getExceptionStack(exception)));
    }

    private static String getExceptionStack(Exception exception) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(os);
        exception.printStackTrace(writer);
        writer.flush();
        String stack = os.toString();
        return stack;
    }

    private static String getExceptionStack(Throwable exception) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(os);
        exception.printStackTrace(writer);
        writer.flush();
        String stack = os.toString();
        return stack;
    }

    /***
     * @return获取相关类、方法信息
     */
    private static String getClassInfo(String msg) {
        int index = 4;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[index].getFileName();
        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();
        methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(className).append(":").append(lineNumber).append(")→").append(msg);
        return stringBuilder.toString();
    }

    /***
     * @param tagStr
     * @param json
     */
    public static void json(String tagStr, String json) {
        String msg;
        msg = json;
        if (TextUtils.isEmpty(msg)) {
            i(tagStr, "Empty or Null json content");
            return;
        }
        String message = null;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            }
        } catch (JSONException e) {
            e(tagStr, e.getCause().getMessage() + "\n" + msg);
            return;
        }
        message = LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        StringBuilder jsonContent = new StringBuilder();
        for (String line : lines) {
            jsonContent.append(line).append(LINE_SEPARATOR);
        }
        _logger.log(LogLevel.INFO, new Date(System.currentTimeMillis()), tagStr, getClassInfo(jsonContent.toString()));
    }
}
