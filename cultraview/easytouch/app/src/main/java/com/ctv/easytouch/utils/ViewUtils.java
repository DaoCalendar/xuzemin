package com.ctv.easytouch.utils;

/**
 * Desc:View工具类
 *
 * @author wang
 * @time 2017/7/6.
 */
public class ViewUtils {
    private static long lastClickTime;

    /**
     * 防止短时间内重复点击
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 400) {
            return true;
        }

        lastClickTime = time;
        return false;
    }

    /**
     * 防止短时间内重复点击
     * @param interval
     * @return
     */
    public static boolean isFastDoubleClick(int interval) {
        int tmpInterval = 400;
        if (interval > 400){
            tmpInterval = interval;
        }

        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < tmpInterval) {
            return true;
        }

        lastClickTime = time;
        return false;
    }
}
