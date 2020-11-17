package com.ctv.newlauncher.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ctv.newlauncher.reservice.NetworkStateReceiver;
import com.ctv.newlauncher.service.Theme1TimeService;

public class BroadcastUtil {

    public static void sendBroadcaset(Context context, int year, int month, int day, int hour, int minute, String week){
        Intent time = new Intent();
        time.setAction(Theme1TimeService.ACTION_TYPE_TIME);
//        time.putExtra("year", year);
//        time.putExtra("month", month);
//        time.putExtra("day", day);
//        time.putExtra("hour", hour);
//        time.putExtra("minute", minute);
        time.putExtra("network", NetWorkUtil.checkNetWorkState(context));
        Log.d("TimeUtil", "week:" + week + ",NetworkStateReceiver.isNewWorkAvailable:" + NetworkStateReceiver.isNewWorkAvailable);
        Log.d("TimeUtil", "BroadcastUtil     network: = "+NetWorkUtil.checkNetWorkState(context));
        time.putExtra("week", week);
        LocalBroadcastManager.getInstance(context).sendBroadcast(time);

    }
    public static void sendnetbroad(Context context){
        Intent time = new Intent();
        time.setAction(Theme1TimeService.ACTION_TYPE_TIME);
        time.putExtra("network", NetWorkUtil.checkNetWorkState(context));
        LocalBroadcastManager.getInstance(context).sendBroadcast(time);
    }

}