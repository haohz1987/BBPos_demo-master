package com.bbpos.bbdevice.util.log;

import android.text.TextUtils;

import com.bbpos.bbdevice.util.LogT;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.channels.FileChannel;

public class StorageUtility {
    public static final Object[] DATA_LOCK = new Object[0];

    public static String combinePath(String path1, String path2) {
        if (TextUtils.isEmpty(path1)) {
            return path2;
        }
        if (TextUtils.isEmpty(path2)) {
            return path1;
        }
        boolean path1EndSlash = (path1.charAt(path1.length() - 1) == '/');
        boolean path2StartSlash = (path2.charAt(0) == '/');
        if ((path1EndSlash && !path2StartSlash) || (!path1EndSlash && path2StartSlash)) {
            return path1 + path2;
        }
        if (path1EndSlash && path2StartSlash) {
            return path1 + path2.substring(1);
        }
        return path1 + "/" + path2;
    }

    public static void copyFile(final File src, final File dst) throws IOException {
        FileInputStream srcStream = new FileInputStream(src);
        FileOutputStream dstStream = new FileOutputStream(dst);
        FileChannel inChannel = srcStream.getChannel();
        FileChannel outChannel = dstStream.getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            srcStream.close();
            dstStream.close();
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static void copyFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            byte[] buf = new byte[20480];
            int c = inputStream.read(buf);
            while (c != -1) {
                fileOutputStream.write(buf, 0, c);
                c = inputStream.read(buf);
            }
        } finally {
            fileOutputStream.close();
        }
    }


    public static void cutFile(final File src, final File dst) throws IOException {
        copyFile(src, dst);
        if (src.exists()) {
            src.delete();
        }
    }

    public static void deleteFile(final String src) throws IOException {
        File file = new File(src);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteFileOrDirectory(final String src) throws IOException {
        File file = new File(src);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                    return;
                }
                for (int i = 0; i < childFiles.length; i++) {
                    childFiles[i].delete();
                }
                file.delete();
            }
        }
    }

    public static boolean writeStringAsFile(final String fileContents, final File file) {
        boolean result = false;
        try {
            synchronized (DATA_LOCK) {
                if (file != null) {
                    file.createNewFile();
                    Writer out = new BufferedWriter(new FileWriter(file), 1024);
                    out.write(fileContents);
                    out.close();
                    result = true;
                }
            }
        } catch (IOException e) {
            LogT.w("Error writing string data to file " + e.getMessage());
        }
        return result;
    }

    public static boolean appendStringToFile(final String appendContents, final File file) {
        boolean result = false;
        try {
            synchronized (DATA_LOCK) {
                if (file != null && file.canWrite()) {
                    // ok if returns false, overwrite
                    file.createNewFile();
                    Writer out = new BufferedWriter(new FileWriter(file, true), 1024);
                    out.write(appendContents);
                    out.close();
                    result = true;
                }
            }
        } catch (IOException e) {
            LogT.w("Error appending string data to file "+ e);
        }
        return result;
    }

    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
            total.append('\n');
        }
        return total.toString();
    }

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        final int BUFFER_SIZE = 1024;
        byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            int count = is.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

    public static void saveFile(String fileName, byte[] data) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data, 0, data.length);
        fos.flush();
        fos.close();
    }

    public static void saveFile(String fileName, InputStream stream) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        copyStream(stream, fos);
        fos.flush();
        fos.close();
    }

    public static byte[] inputStreamTOByte(InputStream in) {
        int BUFFER_SIZE = 1024;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        try {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
                outStream.write(data, 0, count);
            }
        } catch (IOException ex) {
            throw new ApplicationIOException("StorageUtility.inputStreamTOByte", "Failed to read stream.", ex);
        }
        data = null;
        return outStream.toByteArray();
    }

    public static byte[] getFileBytes(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (Exception e) {
            throw new ApplicationException("StorageUtility.getFileBytes", "getFileBytes failed", e);
        }
        return bytes;
    }

    public static void closeStreamNoThrow(Closeable stream) {
        try {
            stream.close();
        } catch (Exception ex) {
            LogT.w("Failed to close stream"+ex);
        }
    }

    public static void resetStreamNoThrow(InputStream stream) {
        try {
            stream.reset();
        } catch (Exception ex) {
            LogT.w("Failed to reset stream"+ex);
        }
    }
}
