package com.ctv.settings.device;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.ctv.settings.base.BaseActivity;
import com.ctv.settings.device.viewHolder.DeviceViewHolder;
import com.ctv.settings.security.R;

/**
 * 安全模块
 * @author wanghang
 * @date 2019/09/17
 */
public class DeviceActivity extends BaseActivity {
    private final static String TAG = "SecurityLib";
    private DeviceViewHolder deviceViewHolder;

    private ImageView imAppPermissions; // app权限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Log.d(TAG, "DeviceActivity onCreate");
        // 初始化viewHolder
        deviceViewHolder = new DeviceViewHolder(this);

//        new UserHandle(-2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        deviceViewHolder.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
