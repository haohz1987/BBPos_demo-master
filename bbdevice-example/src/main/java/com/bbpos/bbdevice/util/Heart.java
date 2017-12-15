package com.bbpos.bbdevice.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Heart
{
    public static int displayWidthPixels;
    public static int displayHeightPixels;
    public static float density;
    public static int densityDpi;
    public static void initialize(Context context)
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        density = displayMetrics.density;
         densityDpi = displayMetrics.densityDpi;
        displayWidthPixels = displayMetrics.widthPixels;
        displayHeightPixels = displayMetrics.heightPixels;
    }
}
