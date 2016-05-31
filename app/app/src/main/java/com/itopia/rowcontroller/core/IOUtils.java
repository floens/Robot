package com.itopia.rowcontroller.core;

import android.content.Context;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static String assetAsString(Context context, String assetName) {
        String res = null;
        try {
            res = IOUtils.readString(context.getResources().getAssets().open(assetName));
        } catch (IOException ignored) {
        }
        return res;
    }

    public static String readString(InputStream is) {
        Reader sr = new InputStreamReader(is);
        Writer sw = new StringWriter();

        try {
            copy(sr, sw);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(sr);
            IOUtils.closeQuietly(sw);
        }

        return sw.toString();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        int read;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
    }

    public static boolean copy(InputStream is, OutputStream os, long maxBytes) throws IOException {
        long total = 0;
        int read;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
            total += read;
            if (total >= maxBytes) {
                return false;
            }
        }
        return true;
    }

    public static void copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
    }
}
