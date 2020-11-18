
package com.ctv.settings.network.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * @Copyright (C), 2015-12-8, CultraView
 * @author Write Macro.Song(songhong@cultraview.com)
 * @since 1.0.0 PageDemo. 调用用列 code
 */
@SuppressWarnings("unused")
public class PageDemo {

    private Context ctvContext;

    public static final int SETTING_PAGE_NETWORK = 1;

    public static final String SETTINGS_ACTION = "android.settings.CTVSETTINGS";

    public static final String CTVSETTINGS_ACTION = "com.cultraview.settings.CTVSETTINGS";

    public static final String CTVNETWORK_SETTINGS_ACTION = "com.cultraview.settings.net.NETWORK_SETTINGS";

    private void startSettingPage() {
        // command
        // am start -a android.settings.CTVSETTINGS

        // 新增的action
        ctvContext.startActivity(new Intent(CTVNETWORK_SETTINGS_ACTION));// (默认是网络状态页面)
    }

    private void startSettingPage0() {
        Intent intent = new Intent();
        // 兼容android系统原生的action
        ctvContext.startActivity(new Intent(NetUtils.WIFI_SETTINGS));// (网络
        ctvContext.startActivity(new Intent(NetUtils.PICK_WIFI_NETWORK));// (网络
        ctvContext.startActivity(new Intent(NetUtils.WIRELESS_SETTINGS));// (网络
        ctvContext.startActivity(new Intent(NetUtils.WIFI_IP_SETTINGS));// (网络
        ctvContext.startActivity(new Intent(NetUtils.ETHERNET_SETTINGS));// (网络
                                                                          // 有线连接页面)
        // 如果想跳到指定的页面需传参数
        // eg:网络状态为: NetUtils.NET_STATE
        // eg:有线状态为: NetUtils.WIRE_CONNECT
        // eg:无线状态为: NetUtils.WIRELESS_CONNECT
        // eg:pppoe状态为: NetUtils.PPPOE_CONNECT
        // eg:wifi ap状态为: NetUtils.WIFI_HOTSPOT
        intent.putExtra("PageNumber", NetUtils.WIRE_CONNECT);// (网络 有线连接页面)

        // 此外还可以直接启动Activity的形式(不推荐这样)
        ComponentName componentName = new ComponentName("com.cultraview.settings.net",
                "com.cultraview.settings.net.NetworkSettingsActivity");
        Intent intent2 = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("PageNumber", NetUtils.WIRE_CONNECT);// (网络 有线连接页面)
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        ctvContext.startActivity(intent2);
    }

}
