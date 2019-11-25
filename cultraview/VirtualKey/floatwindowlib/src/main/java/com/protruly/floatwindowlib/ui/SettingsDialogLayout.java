package com.protruly.floatwindowlib.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.pppoe.PppoeManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.protruly.floatwindowlib.activity.SettingNewActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.cultraview.tv.CtvPictureManager;
//import com.mstar.android.pppoe.PPPOE_STA;
//import com.mstar.android.pppoe.PppoeManager;;
import com.mstar.android.tv.TvFactoryManager;
//import com.mstar.android.wifi.MWifiManager;
import com.protruly.floatwindowlib.MyApplication;
import com.protruly.floatwindowlib.R;
import com.protruly.floatwindowlib.adapter.NotificationAdapter;
import com.protruly.floatwindowlib.been.AppInfo;
import com.protruly.floatwindowlib.been.NotificationInfo;
import com.protruly.floatwindowlib.callback.SimpleItemTouchCallBack;
import com.protruly.floatwindowlib.constant.CommConsts;
import com.protruly.floatwindowlib.control.FloatWindowManager;
import com.protruly.floatwindowlib.entity.SpacesItemDecoration;
import com.protruly.floatwindowlib.helper.LightDB;
import com.protruly.floatwindowlib.service.FloatWindowService;
import com.protruly.floatwindowlib.utils.ApkInfoUtils;
import com.protruly.floatwindowlib.utils.MyUtils;
import com.yinghe.whiteboardlib.bean.TimeInfo;
import com.yinghe.whiteboardlib.utils.AppUtils;
import com.yinghe.whiteboardlib.utils.CommConst;
import com.yinghe.whiteboardlib.utils.SPUtil;
import com.yinghe.whiteboardlib.utils.ScreenUtils;
import com.yinghe.whiteboardlib.utils.TimeUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Desc:设置弹框
 *
 * @author wang
 * @time 2017/4/13.
 */
public class SettingsDialogLayout extends FrameLayout {
    private static final String TAG = SettingsDialogLayout.class.getSimpleName();
    private Context mContext;

    // 宽和高
    public static int viewWidth;
    public static int viewHeight;

    private GridView gv_apps;

    private boolean isRightShow = false; // 是否在右边弹出信号源

    TextView tvWeek;
    TextView tvDay;
    TextView tvTime;

    ImageView wireImage; // 有线
    ImageView wifiImage;// 无线
    ImageView hostpotImage; // 热点

    ImageView eyecareImage;

    ImageView OPSImage; // OPS
    ImageView HomeImage;// 主页
    ImageView ShutdownImage; // 关机

    SeekBar sound;
    SeekBar light;
    AudioManager audioManager;
    CtvPictureManager mTvPictureManager = null;
    public static Handler mHandler;

    WifiManager mWifiManager;
    EthernetManager mEthernetManager;
    PppoeManager mPppoeManager;

    private SelectAppDialog dialogView;
    private RelativeLayout pupAdd;
    ImageView deleteImage;
    Dialog selectDialog;

    private ImageView addMenu;
    private ApkInfoUtils apkInfoUtils;
    private TextView tvAdd;
    private LinearLayout energySaving;
    private List<Map<String, Object>> app_list;
    private SimpleAdapter sim_adapter;
    // 图片封装为一个数组
    private int[] icon = { R.drawable.apps_wire_normal, R.drawable.apps_wireless_normal, R.drawable.apps_hotspot_normal,
            R.drawable.apps_settings_normal, R.drawable.apps_screenshot_normal, R.drawable.apps_timer_normal,
            R.drawable.apps_record_normal, R.drawable.apps_magnifier_normal, R.mipmap.light_sense_default,
            R.drawable.apps_eye_care_normal, R.mipmap.energy_saving_default, R.drawable.apps_add_normal };
    private int[] iconName = null;

    protected boolean isOpenWifi = false, isDialog = false;
    boolean isOpenHotspot = false;

    private Listener mListener = new Listener() {
        @Override
        public void onConnectivityChange(Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                upApStateChange(intent);
            }
        }
    };
    private TextView tv_sound;
    private TextView tv_light;
    private LinearLayout lightSenseLL;
    private ImageView lightSenseImage;
    private LinearLayout  magnifierLL;
    private RecyclerView mRecyclerView;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationInfo> notificationList;
    private TextView mAllIgnore;


    /**
     * 更新AP状态
     * @param intent
     */
    private void upApStateChange(Intent intent){
        int state = intent.getIntExtra("wifi_state",
                14);

        SPUtil.saveData(getContext(),CommConsts.WIFI_STATE, state);
        switch (state) {
            default:
            case 12:
                Log.d(TAG, "WIFI_AP_STATE_ENABLING");
                break;
            case 13:
                Log.d(TAG, "WIFI_AP_STATE_ENABLED");
                // 开启热点
                if (!isOpenHotspot) {
                    isOpenHotspot = true;
                    setOpenHotspot(false);
                    Log.d(TAG, "CHANGE WIFI_AP_STATE_ENABLED:" + isOpenHotspot);
                }

                SPUtil.saveData(getContext(),CommConsts.IS_HOTSPOT_ON, isOpenHotspot);
                break;
            case 10:
                Log.d(TAG, "WIFI_AP_STATE_DISABLING");
                break;
            case 11:
                Log.d(TAG, "WIFI_AP_STATE_DISABLED");
                if (isOpenHotspot) {
                    isOpenHotspot = false;
                    Log.i(TAG, "CHANGE WIFI_AP_STATE_DISABLED:" + isOpenHotspot);
                    SPUtil.saveData(getContext(),CommConsts.IS_HOTSPOT_ON, isOpenHotspot);
                }
                break;
        }
    }

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Timer timer;

    public SettingsDialogLayout(Context context) {
        this(context, null);
    }

    public SettingsDialogLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        init();
    }

    private void init(){
        LayoutInflater.from(mContext).inflate(R.layout.dialog_setting, this);
        initView();

        isOpenHotspot = isWifiApEnabled();
        reflashUI();
        initReceiver();

        Log.i(TAG, "----last-isOpenHotspot:" + isOpenHotspot);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_OUTSIDE){
            this.setVisibility(View.GONE);
            if (isRightShow) {
                FloatWindowManager.getMenuWindow().changeIndexBg(true);
            } else {
                FloatWindowManager.getMenuWindowLeft().changeIndexBg(true);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化UI
     */
    private void initView() {
        mHandler = new UIHandler(this);
        apkInfoUtils = new ApkInfoUtils();
        tvWeek = (TextView)findViewById(R.id.tv_week);
        tvDay = (TextView)findViewById(R.id.tv_day);
        tvTime = (TextView)findViewById(R.id.tv_time);

        // 有线网络
        mEthernetManager = (EthernetManager) getContext().getSystemService("ethernet");

        // P2P网络
//        mPppoeManager = PppoeManager.getInstance(getContext());

        //OPS HOME shuatdow
        OPSImage=(ImageView) findViewById(R.id.btn_ops);
        HomeImage=(ImageView) findViewById(R.id.btn_android);
        ShutdownImage=(ImageView) findViewById(R.id.btn_shutdown);

        //通知
        mRecyclerView = (RecyclerView)findViewById(R.id.recycleview_notification);
        mAllIgnore = (TextView)findViewById(R.id.all_ignore);

        notificationList = FloatWindowManager.getNotificationList();
        if(notificationList !=null){
            Log.i("gyx","init recycleview");
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.addItemDecoration(new SpacesItemDecoration(8));
            notificationAdapter = new NotificationAdapter(mContext, notificationList);
            SimpleItemTouchCallBack simpleItemTouchCallBack = new SimpleItemTouchCallBack(notificationAdapter);
            ItemTouchHelper helper = new ItemTouchHelper(simpleItemTouchCallBack);
            helper.attachToRecyclerView(mRecyclerView);
            mRecyclerView.setAdapter(notificationAdapter);
        }

        gv_apps = findViewById(R.id.gv_apps);
        app_list = new ArrayList<>();
        //获取数据
        getData();
        //新建适配器
        String [] from ={"image","text"};
        int [] to = {R.id.image,R.id.text};
        sim_adapter = new SimpleAdapter(mContext, app_list, R.layout.app_item, from, to);
        //配置适配器
        gv_apps.setAdapter(sim_adapter);
        gv_apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int textid = iconName[i];
                switch (textid){
                    case R.string.apps_wire: { // 网络
//                    gotoNetUI(CommConsts.WIRE_CONNECT);
                        clickWire();
                        break;
                    }
                    case R.string.apps_wireless: { // WiFi
                        clickWifi();
                        break;
                    }
                    case R.string.apps_hotspot: { // 热点
                        isOpenHotspot = !isOpenHotspot;
                        setOpenHotspot(true);
                        break;
                    }

                    case R.string.apps_settings: { // 设置
                        String action = "com.cultraview.settings.CTVSETTINGS";
                        AppUtils.gotoOtherApp(getContext(), action);
                        break;
                    }

                    case R.string.apps_screenshot: { // 截屏
                        Log.i(TAG, "screenshot start");
                        AppUtils.showScreenshot(getContext().getApplicationContext());
                        break;
                    }
                    case R.string.apps_timer: { // 计时器
                        String mPackageName = "com.dazzle.timer";
                        String mActivityName = "com.dazzle.timer.TimerActivity";
                        AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                        break;
                    }
                    case R.string.apps_record: { // 录像
                        String mPackageName = "com.dazzlewisdom.screenrec";
                        String mActivityName = "com.dazzlewisdom.screenrec.ScreenRecActivity";
                        AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                        break;
                    }
                    case R.string.apps_eyecare: { // 护眼
                        setEyecareMode();
                        break;
                    }
                    case R.string.apps_usred:{//自定义
                        setUserAPPShow();
                        break;
                    }
                    case R.id.btn_delete: { // 删除添加的快捷应用
                        autoDelayHide();

                        SPUtil.saveData(getContext(), CommConst.USERED_PACKAGE_NAME, "");
                        deleteImage.setVisibility(View.GONE);

                        updateUseredIcon();
                        break; // 不隐藏UI，还可以再操作
                    }
                    case R.string.light_sense: {
                        // 允许背光进度条滑动
                        mHandler.post(() ->{
                            if (SettingNewActivity.mHandler != null){
                                Message msg = SettingNewActivity.mHandler.obtainMessage(SettingNewActivity.MSG_UPDATE_LIGHT,
                                        true);
                                SettingNewActivity.mHandler.sendMessage(msg); // 更新亮度进度条
                            }
                        });
                        // 切换光感
                        changeLightSense();
                        break;
                    }
                    case R.string.energy_saving:{//自定义
                        CtvPictureManager.getInstance().disableBacklight();
                        Settings.System.putInt(mContext.getContentResolver(), "isSeperateHear", 1);
                        break;
                    }
                    case R.string.magnifier:{ //放大镜
                        String mPackageName = "com.example.newmagnifier";
                        String mActivityName = "com.example.newmagnifier.MainActivity";
                        AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                        break;
                    }
                }
            }
        });


        // WiFi网络
        wifiImage = (ImageView) findViewById(R.id.wifi_image);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        // WiFi网络
        hostpotImage = (ImageView) findViewById(R.id.hotspot_image);

        lightSenseLL = (LinearLayout) findViewById(R.id.pup_light_sense);
        lightSenseImage = (ImageView) findViewById(R.id.iv_light_sense);


        //放大镜
        magnifierLL = (LinearLayout) findViewById(R.id.pup_magnifier);
        View wireLL = findViewById(R.id.pup_net);
        View wifiLL = findViewById(R.id.pup_wifi);
        View hotspotLL = findViewById(R.id.pup_hotspot);
        View settingsLL = findViewById(R.id.pup_settings);

        View screenshotLL = findViewById(R.id.pup_screenshot);

        View timerLL = findViewById(R.id.pup_timer);
        View recordLL = findViewById(R.id.pup_record);

        //节能
        energySaving = (LinearLayout) findViewById(R.id.pup_energy_saving);
        // 护眼
        View eyecareLL = findViewById(R.id.pup_eyecare);
        eyecareImage = (ImageView) findViewById(R.id.eyecare_iv);
        int eyeCare = Settings.System.getInt(mContext.getContentResolver(), CommConsts.IS_EYECARE, 0);
        int resID = (eyeCare == 0) ? R.mipmap.apps_eye_care_default: R.mipmap.apps_eye_care_focus;
        eyecareImage.setImageResource(resID);

        pupAdd = (RelativeLayout)findViewById(R.id.pup_add);
        pupAdd.setOnLongClickListener(mOnLongClickListener);

        tvAdd = (TextView)findViewById(R.id.tv_add);
        addMenu = (ImageView)findViewById(R.id.add_menu);
        deleteImage = (ImageView)findViewById(R.id.btn_delete);

        // 声音和亮度
        sound = (SeekBar) findViewById(R.id.pup_seekbar2);
        light = (SeekBar) findViewById(R.id.pup_seekbar1);

        tv_sound = (TextView) findViewById(R.id.tv_sound);
        tv_light = (TextView) findViewById(R.id.tv_light);

        audioManager = (AudioManager) mContext.getSystemService(Service.AUDIO_SERVICE);
        int maxSound = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获取系统音量最大值
        sound.setMax(maxSound);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
        sound.setProgress(currentVolume);//音量控制Bar的当前值设置为系统音量当前值
        tv_sound.setText(""+currentVolume);
        try {
            mTvPictureManager = CtvPictureManager.getInstance();
            light.setProgress(mTvPictureManager.getBacklight());
            tv_light.setText(""+mTvPictureManager.getBacklight());
        } catch (Exception e){
            e.printStackTrace();
        }
        light.setMax(100);


        // 设置监听
        wireLL.setOnClickListener(mOnClickListener);
        wifiLL.setOnClickListener(mOnClickListener);
        hotspotLL.setOnClickListener(mOnClickListener);
        settingsLL.setOnClickListener(mOnClickListener);

        OPSImage.setOnClickListener(mOnClickListener);
        HomeImage.setOnClickListener(mOnClickListener);
        ShutdownImage.setOnClickListener(mOnClickListener);

        screenshotLL.setOnClickListener(mOnClickListener);
        timerLL.setOnClickListener(mOnClickListener);
        recordLL.setOnClickListener(mOnClickListener);
        eyecareLL.setOnClickListener(mOnClickListener);

        sound.setOnSeekBarChangeListener(new SeekBarListen());
        light.setOnSeekBarChangeListener(new LightListen());

        pupAdd.setOnClickListener(mOnClickListener);
        mAllIgnore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatWindowManager.clearALL(mContext);
            }
        });

        deleteImage.setOnClickListener(mOnClickListener);
        magnifierLL.setOnClickListener(mOnClickListener);
        lightSenseLL.setOnClickListener(mOnClickListener);
        energySaving.setOnClickListener(mOnClickListener);
	    updateTime();
        // 开启定时器，每隔2秒刷新一次
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 2000);
        }
        initLightSenseUI();
        initSelectDialog();
        updateUseredIcon();
    }
    /**
     * 初始化光感UI
     */
    private void initLightSenseUI(){
        boolean isClick = false;
        float alpha = 0.4F;
        boolean isLightSenseEnable = MyUtils.isSupportLightSense();
        int resID = R.mipmap.light_sense_default;
        if (isLightSenseEnable){
            isClick = true;
            alpha = 1F;

            int lightSense = Settings.System.getInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, 0);
            resID = (lightSense == 0) ? R.mipmap.light_sense_default: R.mipmap.light_sense_focus;
        }
        lightSenseImage.setImageResource(resID);
        lightSenseLL.setClickable(isClick);
        lightSenseLL.setAlpha(alpha);
    }


    /**
     * 长按事件
     */
    private View.OnLongClickListener mOnLongClickListener = (view)->{
        autoDelayHide();

        if (deleteImage != null){
            deleteImage.setVisibility(View.VISIBLE);
        }

        return true;
    };

    public List<Map<String, Object>> getData(){
        //cion和iconName的长度是相同的，这里任选其一都可以
        iconName = new int[]{R.string.apps_wire, R.string.apps_wireless, R.string.apps_hotspot,
                R.string.apps_settings, R.string.apps_screenshot, R.string.apps_timer,
                R.string.apps_record, R.string.magnifier, R.string.light_sense,
                R.string.apps_eyecare, R.string.energy_saving, R.string.apps_usred};
        for(int i=0;i<icon.length;i++){
            Map<String, Object> map = new HashMap<>();
            map.put("image", icon[i]);
            map.put("text", mContext.getString(iconName[i]));
            app_list.add(map);
        }

        return app_list;
    }

    /**
     * 更新图标
     */
    private void updateUseredIcon(){
        String packageName = (String)SPUtil.getData(getContext(), CommConst.USERED_PACKAGE_NAME, "");
        if (!TextUtils.isEmpty(packageName)){
            Log.d(TAG, "updateUseredIcon start");
            AppInfo appInfo = apkInfoUtils.scanInstallApp(getContext(), packageName);
            if (appInfo != null){
                Log.d(TAG, "updateUseredIcon change icon");
                addMenu.setImageDrawable(appInfo.getAppIcon());
                tvAdd.setText(appInfo.getAppName());
            } else {
                addMenu.setImageResource(R.drawable.apps_add_normal);
                tvAdd.setText(getContext().getResources().getString(R.string.apps_usred));
            }
        } else {
            addMenu.setImageResource(R.drawable.apps_add_normal);
            tvAdd.setText(getContext().getResources().getString(R.string.apps_usred));
        }
    }

    private void initSelectDialog() {
        dialogView = new SelectAppDialog(this.getContext().getApplicationContext());
        selectDialog = new Dialog(this.getContext().getApplicationContext(), R.style.dialog);
        selectDialog.setContentView(dialogView);
        selectDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        selectDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        selectDialog.setCanceledOnTouchOutside(true);

        // 设置对话框的大小
        Window dialogWindow = selectDialog.getWindow();

        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = ScreenUtils.dip2px(this.getContext(), 180); // 宽度
        lp.height = ScreenUtils.dip2px(this.getContext(), 300); // 高度

        dialogWindow.setAttributes(lp);
        dialogView.setCallBack(mCallback);
    }
    SelectAppDialog.Callback mCallback = (appInfo) -> {
        if (selectDialog.isShowing()){
            selectDialog.dismiss();
        }

        if (appInfo != null) {
            SPUtil.saveData(getContext(), CommConst.USERED_PACKAGE_NAME, appInfo.getPackName());
        }
    };


    private void initReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getContext().registerReceiver(mWifiReceiver, filter);

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction("com.ctv.UPDATE_NOTIFICATION");
        mFilter.addAction("android.net.conn.INET_CONDITION_ACTION");
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        // wifi state change
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        // pppoe state change
//        mFilter.addAction(PppoeManager.PPPOE_STATE_ACTION);
//        mFilter.addAction(MWifiManager.WIFI_DEVICE_ADDED_ACTION);
//        mFilter.addAction(MWifiManager.WIFI_DEVICE_REMOVED_ACTION);
        // wifi ap
        mFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");

        mContext.registerReceiver(mReceiver, mFilter);

        mContext.registerReceiver(mWifiListReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.ctv.UPDATE_NOTIFICATION")){
                notificationList=FloatWindowManager.getNotificationList();
                notificationAdapter.notifyDataSetChanged();
            }else{
                mListener.onConnectivityChange(intent);
            }

        }
    };

    private final BroadcastReceiver mWifiListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (mWifiListener != null) {
//                mWifiListener.onWifiListChanged();
//            }
        }
    };

    private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!isOpenWifi || isDialog) {
                return;
            }
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d(TAG, "----------------Got supplicant state: " + state.name());
            }
        }
    };

    public void reflashUI(){
        // 有线
        boolean isWireOpen = mEthernetManager.isEnabled();
        wireImage = (ImageView) findViewById(R.id.wire_image);
        if (isWireOpen){
            wireImage.setImageResource(R.mipmap.apps_wire_focus);
        } else {
            wireImage.setImageResource(R.mipmap.apps_wire_default);
        }
        SPUtil.saveData(getContext(),CommConsts.IS_WIRE_ON, isWireOpen);

        // WiFi
        boolean isWifiOpen = mWifiManager.isWifiEnabled();
        if (isWifiOpen){
            wifiImage.setImageResource(R.mipmap.apps_wireless_focus);
        } else {
            wifiImage.setImageResource(R.mipmap.apps_wireless_default);
        }
        SPUtil.saveData(getContext(),CommConsts.IS_WIFI_ON ,isWifiOpen);
        Log.i(TAG, "isWifiEnabled:" + isWifiOpen);

        // Wifi热点
        setOpenHotspot(false);
    }

    /**
     * 设置Wifi热点
     * @param isClick
     */
    void setOpenHotspot(boolean isClick) {
        int apState = (Integer) SPUtil.getData(getContext(),CommConsts.WIFI_STATE, (Integer)11);
        Log.i(TAG, "setOpenHotspot-isOpenHotspot:" + isOpenHotspot);
        if (isOpenHotspot) {
            if (!hasReady(isClick)) {
                Log.d(TAG, "wifiap have not ready.");
                isOpenHotspot = false;
                return;
            }
            if (apState == 12
                    || apState == 13) {
                isOpenHotspot = true;
                if (isOpenHotspot){
                    hostpotImage.setImageResource(R.mipmap.apps_hotspot_focus);
                } else {
                    hostpotImage.setImageResource(R.mipmap.apps_hotspot_default);
                }

                SPUtil.saveData(getContext(),CommConsts.IS_HOTSPOT_ON, isOpenHotspot);
                return;
            }
        } else {
            if (apState == 10
                    || apState == 11) {
                isOpenHotspot = false;
            }
        }
        if (isClick) {
            if (setWifiApEnabled(isOpenHotspot)) {
                Log.i(TAG, "setOpenHotspot-setWifiApEnabled:" + isOpenHotspot);
            }
        }

        if (isOpenHotspot){
            hostpotImage.setImageResource(R.mipmap.apps_hotspot_focus);
        } else {
            hostpotImage.setImageResource(R.mipmap.apps_hotspot_default);
        }

        SPUtil.saveData(getContext(),CommConsts.IS_HOTSPOT_ON, isOpenHotspot);
    }

    private boolean hasReady(boolean isClick){
//        if (!MWifiManager.getInstance().isWifiDeviceExist()) {
//            if (isClick){
//                Toast.makeText(getContext(), R.string.please_insert_dongle, Toast.LENGTH_LONG).show();
//            }
//            Log.d(TAG, "setAndCheckWifiData --  wifi_dongle");
//            return false;
//        }
//        if (!MWifiManager.getInstance().isWifiDeviceSupportSoftap()) {
//            if (isClick){
//                Toast.makeText(getContext(), R.string.device_do_not_support, Toast.LENGTH_LONG).show();
//            }
//            Log.d(TAG, "hasReady -- support wifi hotspot?");
//            return false;
//        }

        return true;
    }

    /**
     * 更新时间
     */
    private void updateTime(){
        TimeInfo timeInfo = TimeUtils.getTimeInfo(getContext());
        tvWeek.setText("" + timeInfo.getWeek());
        tvDay.setText("" + timeInfo.getDay());
        tvTime.setText("" + timeInfo.getTime());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setRightShow(boolean rightShow) {
        isRightShow = rightShow;
    }

    /**
     * 设置护眼模式
     */
    private void setEyecareMode(){
        // 修改UI
        int eyeCare = Settings.System.getInt(mContext.getContentResolver(), CommConsts.IS_EYECARE, 0);
        final int eyeCareTmp = (eyeCare == 0) ? 1 : 0;

        int resID = (eyeCareTmp == 0) ? R.mipmap.apps_eye_care_default: R.mipmap.apps_eye_care_focus;
        eyecareImage.setImageResource(resID);

        // 改变状态
        mHandler.postDelayed(()->{
            Settings.System.putInt(getContext().getContentResolver(), CommConsts.IS_EYECARE, eyeCareTmp);
        }, 500);
        Settings.System.putInt(mContext.getContentResolver(), CommConsts.IS_EYECARE, eyeCareTmp);
        Log.d(TAG, "setEyecareMode eyeCare->" + eyeCareTmp);

        if (eyeCareTmp == 1){ // 打开护眼模式
            mHandler.removeMessages(KEY_RESET_BACK_LIGHT);

            // 保存背光值
            int curBacklight = AppUtils.getBacklight();
            if (curBacklight > 50){
                Settings.System.putInt(getContext().getContentResolver(), "lastBlackLight", curBacklight);
                LogUtils.d("护眼模式 降低light setBacklight 50, curBacklight:" + curBacklight);
            }
            int lightSense = Settings.System.getInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, 0);
            if (lightSense == 1){
                // 切换光感
                Settings.System.putInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, 0);
                if (!MyUtils.isSupportLightSense()){
                    return;
                }

                lightSense = 0;
                Settings.System.putInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, lightSense);

                int ID =  R.mipmap.light_sense_default;
                lightSenseImage.setImageResource(ID);
                Log.d(TAG, "isLightSense->" + lightSense);
            }
        } else { // 关闭护眼时，恢复背光值
            mHandler.sendEmptyMessage(KEY_RESET_BACK_LIGHT);
        }
    }

    // wifi热点开关

    /**
     * 关闭WiFi AP
     * @param enabled
     * @return
     */
    public boolean setWifiApEnabled(boolean enabled) {
        try {
            //通过反射调用设置热点
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

            //返回热点打开状态
            return (Boolean) method.invoke(mWifiManager, null, enabled);
        } catch (Exception e) {
            return false;
        }
    }

    // wifi热点开关
    public boolean isWifiApEnabled() {
        try {
            //通过反射调用设置热点
            Method method = mWifiManager.getClass().getMethod(
                    "isWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

            //返回热点打开状态
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            return false;
        }
    }

    // wifi热点开关
    public int getWifiApState() {
        try {
            //通过反射调用设置热点
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApState", WifiConfiguration.class, Integer.TYPE);

            //返回热点打开状态
            return (Integer) method.invoke(mWifiManager);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 自动延迟收缩
     */
    public void autoDelayHide(){
        mHandler.removeCallbacks(shrinkRunnable);
        mHandler.postDelayed(shrinkRunnable, 10000);
    }

    /**
     * 清除
     */
    public void destroy(){
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        mContext.unregisterReceiver(mReceiver);
        mContext.unregisterReceiver(mWifiListReceiver);
    }

    /**
     * 更新声音UI
     */
    public void updateVoiceUI(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获取当前音量
        sound.setProgress(currentVolume);//音量控制Bar的当前值设置为系统音量当前值
        tv_sound.setText(""+currentVolume);
    }

    /**
     * 点击有线
     */
    private void clickWire(){
        boolean isWireOn = (Boolean) SPUtil.getData(getContext(),CommConsts.IS_WIRE_ON, false);
        isWireOn = !isWireOn;
        mEthernetManager.setEnabled(isWireOn);
        isWireOn = mEthernetManager.isEnabled();
        SPUtil.saveData(getContext(),CommConsts.IS_WIRE_ON, isWireOn);
        int resID = R.mipmap.apps_wire_default;
        if (isWireOn){ // 开启有线网络
            resID = R.mipmap.apps_wire_focus;
        }

        wireImage.setImageResource(resID);

//        // 判断WIFI是否打开
//        if (isWireOn){
//            if (MWifiManager.getInstance().isWifiDeviceExist()) {
//                boolean isWifiOn = mWifiManager.isWifiEnabled();
//                if (isWifiOn) {
//                    mWifiManager.setWifiEnabled(false);
//                }
//            }
//        }
    }

    /**
     * 点击WIFI
     */
    private void clickWifi(){
        boolean isWifiOn = (Boolean) SPUtil.getData(getContext(),CommConsts.IS_WIFI_ON, false);
//        if (!MWifiManager.getInstance().isWifiDeviceExist()) {
//            Toast.makeText(getContext(), R.string.please_insert_dongle, Toast.LENGTH_LONG).show();
//            Log.d(TAG, "setAndCheckWifiData --  wifi_dongle");
//            SPUtil.saveData(getContext(),CommConsts.IS_WIFI_ON, false);
//            return;
//        }

        isWifiOn = !isWifiOn;
        if (isWifiOn){ // 开启WiFi时，关闭WiFi和PPPoE,开启有线


//            if (PPPOE_STA.CONNECTING == mPppoeManager.PppoeGetStatus()) { // 2.Close PPPoE
//                mPppoeManager.PppoeHangUp();
//            }
//
            if (mEthernetManager.isEnabled()) { // 3.Close Ethernet
                mEthernetManager.setEnabled(false);
            }
        }

        mWifiManager.setWifiEnabled(isWifiOn);
        if (isWifiOn){
            mWifiManager.getScanResults();
        }
        Log.i(TAG, "setWifi-setWifiEnabled:" + isWifiOn);
//                isWifiOn = mWifiManager.isWifiEnabled();
        SPUtil.saveData(getContext(),CommConsts.IS_WIFI_ON, isWifiOn);

        int resID = R.mipmap.apps_wireless_default;
        if (isWifiOn){ // 开启Wifi网络
            resID = R.mipmap.apps_wireless_focus;
        }

        wifiImage.setImageResource(resID);
    }

    /**
     * item事件监听
     */
    private View.OnClickListener mOnClickListener = (view) -> {
        SettingsDialogLayout.this.setVisibility(View.GONE);

        int id = view.getId();
        switch (id){
            case R.id.pup_net: { // 网络
//                    gotoNetUI(CommConsts.WIRE_CONNECT);
                clickWire();
                break;
            }
            case R.id.pup_wifi: { // WiFi
                clickWifi();
                break;
            }
            case R.id.pup_hotspot: { // 热点
                isOpenHotspot = !isOpenHotspot;
                setOpenHotspot(true);
                break;
            }

            case R.id.pup_settings: { // 设置
                    String action = "com.cultraview.settings.CTVSETTINGS";
                AppUtils.gotoOtherApp(getContext(), action);
                break;
            }

            case R.id.pup_screenshot: { // 截屏
                Log.i(TAG, "screenshot start");
                AppUtils.showScreenshot(getContext().getApplicationContext());
                break;
            }
            case R.id.pup_timer: { // 计时器
                String mPackageName = "com.dazzle.timer";
                String mActivityName = "com.dazzle.timer.TimerActivity";
                AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                break;
            }
            case R.id.pup_record: { // 录像
                String mPackageName = "com.dazzlewisdom.screenrec";
                String mActivityName = "com.dazzlewisdom.screenrec.ScreenRecActivity";
                AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                break;
            }
            case R.id.pup_eyecare: { // 护眼
                setEyecareMode();
                break;
            }
            case R.id.pup_add:{//自定义
                setUserAPPShow();
                break;
            }
            case R.id.btn_delete: { // 删除添加的快捷应用
                autoDelayHide();

                SPUtil.saveData(getContext(), CommConst.USERED_PACKAGE_NAME, "");
                deleteImage.setVisibility(View.GONE);

                updateUseredIcon();
                break; // 不隐藏UI，还可以再操作
            }
            case R.id.pup_light_sense: {
                // 允许背光进度条滑动
                mHandler.post(() ->{
                    if (SettingNewActivity.mHandler != null){
                        Message msg = SettingNewActivity.mHandler.obtainMessage(SettingNewActivity.MSG_UPDATE_LIGHT,
                                true);
                        SettingNewActivity.mHandler.sendMessage(msg); // 更新亮度进度条
                    }
                });
                // 切换光感
                changeLightSense();
                break;
            }
            case R.id.pup_energy_saving:{//自定义
                CtvPictureManager.getInstance().disableBacklight();
                Settings.System.putInt(mContext.getContentResolver(), "isSeperateHear", 1);
                break;
            }
            case R.id.pup_magnifier:{ //放大镜
                String mPackageName = "com.example.newmagnifier";
                String mActivityName = "com.example.newmagnifier.MainActivity";
                AppUtils.gotoOtherApp(getContext(), mPackageName, mActivityName);
                break;
            }
            case R.id.btn_ops: {//OPS
                AppUtils.changeSignal(mContext, 26);
                break;
            }
            case R.id.btn_android: { //主页
                AppUtils.keyEventBySystem(KeyEvent.KEYCODE_HOME);
                break;
            }
            case R.id.btn_shutdown: { //关机
                AppUtils.keyEventBySystem(KeyEvent.KEYCODE_POWER);
                break;
            }

        }
        // 在PC界面时，收起菜单
        // 收缩菜单
        ControlMenuLayout controlMenu = isRightShow ? FloatWindowManager.getMenuWindow() : FloatWindowManager.getMenuWindowLeft();
        if ((controlMenu != null)) {
            controlMenu.shrinkMenu();
        }

        // 退出设置界面
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    };

    /**
     * 改变光感
     */
    private void changeLightSense(){
        // 切换光感
        int lightSense = Settings.System.getInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, 0);
        boolean isOpen = (lightSense == 0);
        setLightSenseMode(isOpen);

        // 自动光感开启时,关闭护眼模式
        lightSense = Settings.System.getInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, 0);
        if (lightSense == 1){ // 自动光感时,关闭护眼模式
            int eyeCare = Settings.System.getInt(getContext().getContentResolver(), CommConsts.IS_EYECARE, 0);
            if (eyeCare == 1){ // 当在护眼时， 关闭护眼
                Settings.System.putInt(getContext().getContentResolver(), CommConsts.IS_EYECARE, 0);
                setEyecareMode();

                // 恢复背光值
                resetBlackLight();
            }
        }
    }

    /**
     * 设置自动感光
     */
    private void setLightSenseMode(boolean isOpen){
        if (!MyUtils.isSupportLightSense()){
            return;
        }

        int lightSense = isOpen ? 1 : 0;
        Settings.System.putInt(getContext().getContentResolver(), CommConsts.IS_LIGHTSENSE, lightSense);

        int resID = isOpen ? R.mipmap.light_sense_focus : R.mipmap.light_sense_default;
        lightSenseImage.setImageResource(resID);
        Log.d(TAG, "isLightSense->" + lightSense);

        // 更新光感定时任务
        updateLightSense();
    }

    /**
     * 更新光感定时任务
     */
    private void updateLightSense(){
        LogUtils.d("更新光感定时任务.....");
        Intent tempIntent = new Intent(getContext(), FloatWindowService.class);
        tempIntent.setAction("com.ctv.FloatWindowService.LIGHT_SENSE_ACTION");
        getContext().startService(tempIntent);
    }


    /**
     * 设置自定义应用
     */
    private void setUserAPPShow(){
        String packageName = (String)SPUtil.getData(getContext(), CommConst.USERED_PACKAGE_NAME, "");
        // 若是没有选择APP，则弹出选择对话框;反之，则启动APP
        if (TextUtils.isEmpty(packageName)){
            selectDialog.show();
        } else {
            // 启动APP
            apkInfoUtils.startApp(getContext(), packageName);
        }
    }
    Runnable shrinkRunnable = ()-> {
        // 退出设置界面
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    };

    /**
     * 声音监听类
     */
    class SeekBarListen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            autoDelayHide();

            int progress = seekBar.getProgress();
//            LogUtils.d("声音设置值：" + progress);

            MyApplication.IsTouchSeting = true;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            tv_sound.setText(""+progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MyApplication.IsTouchSeting = false;
        }
    }


    /**
     * 亮度监听类
     */
    class LightListen implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            autoDelayHide();

            int progress = seekBar.getProgress();
//            LogUtils.d("亮度设置值：" + progress);

            light.setProgress(progress);
            try {
                Settings.System.putInt(mContext.getContentResolver(),"backlight",progress);
                mTvPictureManager.setBacklight(progress);
                tv_light.setText(""+progress);
            } catch (Exception e){
                e.printStackTrace();
                LogUtils.e("设置亮度异常：" + e.getMessage());
            }

            LightDB db = new LightDB(getContext());
            db.updatePicModeSetting(progress);
            MyApplication.IsTouchSeting = true;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MyApplication.IsTouchSeting = false;
        }
    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            // 当前没有浮框，直接创建浮框
            SettingsDialogLayout settingsDialog = FloatWindowManager.getSettingsDialog();
            if (settingsDialog!= null &&  settingsDialog.getVisibility() == View.VISIBLE){
                mHandler.post(() -> {
                    updateTime();
                    SystemClock.sleep(500);
                });
            }
        }
    }

    /**
     * 恢复背光值
     */
    private void resetBlackLight(){
        mHandler.removeMessages(KEY_RESET_BACK_LIGHT);

        // 恢复背光值
        int lastBlackLight = Settings.System.getInt(mContext.getContentResolver(),
                "lastBlackLight", 50);
        if (lastBlackLight > 50){
            AppUtils.setBacklight(lastBlackLight);
            // 更新背光
            updateBlackLightSeekbar();
            LogUtils.d("护眼模式 恢复light setBacklight lastBlackLight:" + lastBlackLight);
        }
    }

    private void updateBlackLightSeekbar(){
        light.setProgress(AppUtils.getBacklight());
    }

    public static final int KEY_CHANGE_LIGHT_SENSE = 1; // 切换光感
    public static final int KEY_CHANGE_EYE_CARE = 2; // 切换护眼
    public static final int KEY_RESET_BACK_LIGHT = 4; // 恢复背光
    public static final int MSG_UPDATE_LIGHT = 5; // 更新背光进度条值

    /**
     * UI异步处理
     */
    public static final class UIHandler extends Handler {
        WeakReference<SettingsDialogLayout> weakReference;

        public UIHandler(SettingsDialogLayout dialogLayout) {
            super();
            this.weakReference = new WeakReference<>(dialogLayout);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SettingsDialogLayout dialogLayout = weakReference.get();
            if (dialogLayout == null) {
                return;
            }

            switch (msg.what){
                case KEY_RESET_BACK_LIGHT:{ // 恢复背光值
                    dialogLayout.resetBlackLight();
                    break;
                }
                case MSG_UPDATE_LIGHT:{ // 更新背光进度条值
                    dialogLayout.updateBlackLightSeekbar();
                    break;
                }
                default:
                    break;
            }
        }
    }

    public interface Listener {
        void onConnectivityChange(Intent intent);

//        void onPPPoeChanged(String status);
//
//        void onEthernetAvailabilityChanged(boolean isAvailable);
    }

}
