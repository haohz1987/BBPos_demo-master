package com.bbpos.bbdevice;

import android.app.Application;
import android.util.Log;

import com.bbpos.bbdevice.util.Heart;
import com.bbpos.bbdevice.util.LogT;
import com.bbpos.bbdevice.util.log.HLog;
import com.bbpos.bbdevice.util.log.PlainLogger;
import com.bbpos.bbdevice.util.log.StorageUtility;

/**
 * Created by haohz on 2017/12/15.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogT.init(true, Log.VERBOSE);//不输出到文件
        String logFilePath = StorageUtility.combinePath(getApplicationInfo().dataDir, "LocalLog.txt");
        HLog.SetLogger(PlainLogger.instance(logFilePath));//输出到文件
        HLog.e("filePath=",logFilePath);

        Heart.initialize(this);
        HLog.d("decivesinfo", "density:" + Heart.density + "|displayWidthPixels:" + Heart.displayWidthPixels
                + "|displayHeightPixels:" + Heart.displayHeightPixels + "|dpi:" + Heart.densityDpi);


    }
}
