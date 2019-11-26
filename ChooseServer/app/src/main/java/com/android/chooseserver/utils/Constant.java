package com.android.chooseserver.utils;

import android.util.Log;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/2
 * 描述: 常量类
 */
public class Constant {
    public static int Area_X = 1;
    public static int SKEWING_Y = 1;
    public static int SCALE = 20;
    public final static boolean isRestaurant = true;
    public static int Bitmap_WIDTH = 0;
    public static int Bitmap_HEIGHT = 0;
    public static final int MSG_REFRESH_CONNECT = 0;
    //wifi静态ip参数
    static final String dns1 = "192.168.1.1";
    static final String dns2 = "192.168.0.1";
    static String gateway = "";
    static int prefix = 24;
    static String IP = "";
    public static int IsView = 0;
    public static final int CommandActivity = 1;
    public static final int RobotActivity = 2;
    public static final int TurnBack = 4;
    public static final int TYPE = 3;
    static final String IPLast = "178";

    public static String CHANNEL_ONE_ID = "com.primedu.cn";
    public static String CHANNEL_ONE_NAME = "Channel One";
    static String isConnectSocket = "";
//    //测试用WIFI USR-C215
    public static String wifiname = "TP-LINK_51289A";
    public static String password = "sjx12345678";
    //Socket服务器端口
    public static int ServerPort = 20123;
    public static int ClientPort = 20123;
    //是否打印日志
    private static final boolean isDebug = true;
    //日志标题
    private static final String TAG = "SJX_Robot---->";
    public static int linearWidth;
    public static String DIR_NAME = "Robot_Map";
    public static int SCALE_NUMBER = 100;
    public static String ADMIN = "admin";
    public static String SINGLE = "111111";
    public static String MANY = "888888";

    //唯一对象
    private static Constant constant;

    public static Constant getConstant() {
        if (constant != null) {
            return constant;
        } else {
            constant = new Constant();
            return constant;
        }
    }

    public static void debugLog(String string) {
        if (isDebug) {
            Log.e(TAG, string);
        }
    }
}
