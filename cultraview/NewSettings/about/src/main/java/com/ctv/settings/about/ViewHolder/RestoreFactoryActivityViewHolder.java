package com.ctv.settings.about.ViewHolder;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.ctv.settings.about.R;
import com.ctv.settings.about.activity.AboutDeviceActivity;
import com.ctv.settings.about.activity.RestoreFactoryActivity;
import com.ctv.settings.utils.SystemPropertiesUtils;
import com.cultraview.tv.CtvCommonManager;
import com.cultraview.tv.CtvTvManager;
import com.cultraview.tv.common.exception.CtvCommonException;

import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.Constants;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestoreFactoryActivityViewHolder implements View.OnClickListener {
    private static final String TAG = "RestoreFactoryActivityViewHolder";
    private View closeview;
    private AlertDialog.Builder close_builder;
    private Context context;
    private RestoreFactoryActivity activity;

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    Button restore_ok;
    Button restore_cancel;
    private boolean isReset;
    private boolean msetting_copy_cultraview_projrct;

    public RestoreFactoryActivityViewHolder(Context context) {
        this.context = context;
        activity = (RestoreFactoryActivity) context;
        initdata();
        initView();
    }

    private void initView() {
        close_builder = new AlertDialog.Builder(context);
        closeview = LayoutInflater.from(context).inflate(R.layout.close_layout, null);
        close_builder.setView(closeview);
    }

    private void initdata() {
        restore_ok = (Button) activity.findViewById(R.id.restore_ok);
        restore_cancel = (Button) activity.findViewById(R.id.restore_cancel);
        restore_ok.setOnClickListener(this);
        restore_cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.restore_ok) {
            if (ActivityManager.isUserAMonkey()) {
                return;
            }
            Toast.makeText(context, R.string.restore_factory_system_reboot,
                    Toast.LENGTH_LONG).show();
//            if ("OPEN".equals(SystemPropertiesUtils.get(context, "hotel.config"))) {
//                // 酒店模式数据,此处不做恢复
//                resetHotelDB();
//            }
//            if ("Y".equals(SystemPropertiesUtils.get(context, "persist.sys.password"))) {
//                // 开机计数数据,此处不做恢复
//                resetPassword();
//            }

            resetPassword();
            resetSystem();
        } else if (id == R.id.restore_cancel) {
            activity.finish();
        }

    }

    private void resetSystem() {
        isReset = true;
        int[] GetOPSDEVICESTATUS = CtvCommonManager.getInstance().setTvosCommonCommand("GetOPSDEVICESTATUS");
        int[] GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand("GetOPSPOWERSTATUS");
        Log.d("chen_powerdown", "GetOPSDEVICESTATUS:" + GetOPSDEVICESTATUS[0]);
        Log.d("chen_powerdown", "GetOPSPOWERSTATUS:" + GetOPSPOWERSTATUS[0]);
        if (GetOPSDEVICESTATUS[0] == 0 && GetOPSPOWERSTATUS[0] == 0) {//0,表示有OPS设备接入；1，表示没有OPS设备接入。
            Dialog close_dialog = close_builder.create();
            close_dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            close_dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            close_dialog.setCanceledOnTouchOutside(false);
            close_dialog.show();
            WindowManager.LayoutParams lp = close_dialog.getWindow().getAttributes();
            lp.width = 640;
            close_dialog.getWindow().setAttributes(lp);
            singleThreadExecutor.execute(closeSystemRunnable);
        } else {
            resetStart();
        }

    }

    Runnable closeSystemRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("chen_powerdown", "start");
                Log.d("chen_powerdown", "close ops");
                int[] GetOPSDEVICESTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSDEVICESTATUS");
                int[] GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSPOWERSTATUS");
                Log.d("chen_powerdown", "GetOPSDEVICESTATUS:init:" + GetOPSDEVICESTATUS[0]);
                Log.d("chen_powerdown", "GetOPSPOWERSTATUS:init:" + GetOPSPOWERSTATUS[0]);
                CtvCommonManager.getInstance().setTvosCommonCommand("SetOPSPOWER");
                GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSPOWERSTATUS");
                Log.d("chen_powerdown", "GetOPSPOWERSTATUS:first:" + GetOPSPOWERSTATUS[0]);
                Thread.sleep(200);
                CtvCommonManager.getInstance().setTvosCommonCommand("SetOPSPOWERON");
                GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSPOWERSTATUS");
                Log.d("chen_powerdown", "GetOPSPOWERSTATUS:setover" + GetOPSPOWERSTATUS[0]);
                Log.d("chen_powerdown", "ops:state");
                Thread.sleep(2000);
                GetOPSDEVICESTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSDEVICESTATUS");
                GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                        "GetOPSPOWERSTATUS");
                Log.d("chen_powerdown", "GetOPSPOWERSTATUS:" + GetOPSPOWERSTATUS[0]);
                int count = 0;
                while (GetOPSPOWERSTATUS[0] == 0 && GetOPSDEVICESTATUS[0] == 0) {
                    Log.d("chen_powerdown", "checkops state start ");
                    Log.d("chen_powerdown", "checkops time count : " + count);
                    Thread.sleep(1000);
                    count++;
                    if (count == 115) {
                        Log.d("chen_powerdown", "ops force now :count: " + count);
                        CtvCommonManager.getInstance().setTvosCommonCommand("SetOPSPOWER");
                        Thread.sleep(5 * 1000);
                        CtvCommonManager.getInstance().setTvosCommonCommand("SetOPSPOWERON");
                    }
                    if (count == 145) {
                        break;
                    }
                    Log.d("chen_powerdown", "change ops state start");
                    GetOPSDEVICESTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                            "GetOPSDEVICESTATUS");
                    GetOPSPOWERSTATUS = CtvCommonManager.getInstance().setTvosCommonCommand(
                            "GetOPSPOWERSTATUS");
                    Log.d("chen_powerdown", "change ops state resutl:");
                    Log.d("chen_powerdown", "GetOPSDEVICESTATUS:" + GetOPSDEVICESTATUS[0]);
                    Log.d("chen_powerdown", "GetOPSPOWERSTATUS:" + GetOPSPOWERSTATUS[0]);
                }
                ;
                Log.d("chen_powerdown", "close ops sucess");
                resetStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void resetStart() {

        // TODO: 2019-10-24 8386

        Log.d("qkmin", "resetStart");
        try {
            TvManager tvManager = TvManager.getInstance();
            msetting_copy_cultraview_projrct = tvManager.setTvosInterfaceCommand("MSETTING_COPY_CULTRAVIEW_PROJRCT");
            tvManager.setTvosCommonCommand("Closeaudio");
            Log.d(TAG, "-----setTvosCommonCommand copy cultraview_pro" + msetting_copy_cultraview_projrct);
        } catch (TvCommonException e) {
            e.printStackTrace();
            Log.e("qkmin", "resetStart TvCommonException" + e);
        }
        Settings.System.putInt(context.getContentResolver(), "menuTimeMode", 0);
//        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
//        intent.putExtra("from", "restorefactory");
//        context.sendBroadcast(intent);

        //qkmkin 8.0和6.0不同

        if (msetting_copy_cultraview_projrct) {
            intoRecovery();

        }


    }


    private void intoRecovery() {
        Intent resetIntent;
        if (android.os.Build.VERSION.RELEASE.equals("8.0.0")) {
            resetIntent = new Intent("android.intent.action.FACTORY_RESET");
            resetIntent.setPackage("android");
            resetIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            resetIntent.putExtra(Intent.EXTRA_REASON, "ResetConfirmFragment");
            if (activity.getIntent().getBooleanExtra("shutdown", false)) {
                resetIntent.putExtra("shutdown", true);
            }
        } else {
            resetIntent = new Intent("android.intent.action.MASTER_CLEAR");
            resetIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            resetIntent.putExtra("from", "restorefactory");
        }
        context.sendBroadcast(resetIntent);
    }


    public boolean resetHotelDB() {
        int result = -1;
        File srcFile = new File("/tvdatabase/Database/", "hotel_mode.db");
        File destFile = new File("/tvconfig/Database/", "hotel_mode.db");
        try {
            result = CtvTvManager.getInstance().copyCmDb(srcFile.getPath(), destFile.getPath());
        } catch (CtvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if (result == -1) {
            Log.d(TAG, "save hotel_mode.db to /tvconfig/Database/ fail!!");
            return false;
        }
        Log.d(TAG, "save ok");
        return true;
    }

    public boolean resetPassword() {
        int result = -1;
        File srcFile = new File("/tvdatabase/Database/", "cultraview_projectinfo.db");
        File destFile = new File("/tvconfig/Database/", "cultraview_projectinfo.db");
        try {
            result = CtvTvManager.getInstance().copyCmDb(srcFile.getPath(), destFile.getPath());
        } catch (CtvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (result == -1) {
            Log.d(TAG, "save cultraview_password.db to /tvconfig/Database/ fail!!");
            return false;
        }
        Log.d(TAG, "save ok");
        return true;
    }

}




