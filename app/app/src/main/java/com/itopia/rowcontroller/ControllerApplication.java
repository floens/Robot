package com.itopia.rowcontroller;

import android.app.Application;

public class ControllerApplication extends Application {
    private static ControllerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static ControllerApplication getInstance() {
        return instance;
    }
}
