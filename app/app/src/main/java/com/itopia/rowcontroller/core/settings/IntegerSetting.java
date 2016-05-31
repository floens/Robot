package com.itopia.rowcontroller.core.settings;

import android.content.SharedPreferences;

public class IntegerSetting extends Setting<Integer> {
    private boolean hasCached = false;
    private Integer cached;

    public IntegerSetting(SharedPreferences sharedPreferences, String key, Integer def) {
        super(sharedPreferences, key, def);
    }

    public IntegerSetting(SharedPreferences sharedPreferences, String key, Integer def, SettingCallback<Integer> callback) {
        super(sharedPreferences, key, def, callback);
    }

    @Override
    public Integer get() {
        if (hasCached) {
            return cached;
        } else {
            cached = sharedPreferences.getInt(key, def);
            hasCached = true;
            return cached;
        }
    }

    @Override
    public void set(Integer value) {
        if (!value.equals(get())) {
            sharedPreferences.edit().putInt(key, value).apply();
            cached = value;
            onValueChanged();
        }
    }
}
