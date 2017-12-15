package com.bbpos.bbdevice.util;

import android.text.TextUtils;

import java.util.Locale;

public class CommonUtils {
    public static boolean checkExpireDate(String plainExpireDate) {
        return !TextUtils.isEmpty(plainExpireDate) && plainExpireDate.length() >= 4;
    }
    public static String bytesToHexString(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder(2 * src.length);
            for (int i = 0; i < src.length; i++) {
                int v = src[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        } catch (Exception outOfMemoryError) {
            LogT.w( "bytes length:" + src.length, outOfMemoryError);
        }
        return null;
    }
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase(Locale.US);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
