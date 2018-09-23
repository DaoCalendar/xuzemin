package com.android.zbrobot.activity;

import android.app.Application;

import com.android.zbrobot.helper.RobotDBHelper;
import com.android.zbrobot.util.Constant;
import com.ls.lsros.helper.RosSDKInitHelper;

public class ZB_MyApplication extends Application{
    @Override
    public void onCreate() {
        Constant.debugLog("init");
        RosSDKInitHelper.init(this);
        RobotDBHelper robotDBHelper = RobotDBHelper.getInstance(this);
        super.onCreate();

    }
}
