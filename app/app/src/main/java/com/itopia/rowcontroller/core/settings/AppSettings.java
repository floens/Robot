package com.itopia.rowcontroller.core.settings;

import android.content.SharedPreferences;
import android.os.Environment;
import com.itopia.rowcontroller.AndroidUtils;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class AppSettings {
    public static final StringSetting tcpAddressHost;
    public static final IntegerSetting tcpAddressPort;

    static {
        SharedPreferences p = AndroidUtils.getPreferences();

        tcpAddressHost = new StringSetting(p, "preference_tcp_address_host", "");
        tcpAddressPort = new IntegerSetting(p, "preference_tcp_address_port", 0);
    }
}
