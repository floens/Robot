package com.itopia.rowcontroller;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

public class AndroidUtils {
    private static Resources getRes() {
        return ControllerApplication.getInstance().getResources();
    }

    public static int dp(float dp) {
        return (int) (dp * getRes().getDisplayMetrics().density);
    }

    public static int sp(float sp) {
        return (int) (sp * getRes().getDisplayMetrics().scaledDensity);
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(ControllerApplication.getInstance());
    }

    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void runOnUiThread(Runnable runnable, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delay);
    }

}
