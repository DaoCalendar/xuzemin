
package com.ctv.settings.network.holder;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctv.settings.R;
import com.ctv.settings.network.Listener.ConnectivityListener;
import com.ctv.settings.network.activity.NetWorkActivity;
import com.ctv.settings.network.utils.InitDataInfo;
import com.ctv.settings.network.utils.NetUtils;

import static com.ctv.settings.network.utils.NetUtils.HOST_BAND_TYPE;
import static com.ctv.settings.network.utils.NetUtils.SCREEN_HOST_BAND;

/**
 * 投屏热点模块
 *
 * @author xuzemin
 * @date 2019/09/19
 */
public class ScreenHotSponViewHolder implements OnFocusChangeListener, OnClickListener {

    private static final String TAG = "ScreenHotSponViewHolder";

    private static final int[] SECURE_TYPE = {
            R.string.wifi_security_open, R.string.wifi_security_wpa2
    };

    private final Context ctvContext;

    private LinearLayout screen_hotspot_ll;

    private FrameLayout screen_hotspot_open_fl;

    private FrameLayout screen_hotspot_show_pwd_fl;

    private FrameLayout screen_hotspot_pwd_fl;

    private FrameLayout screen_hotspot_secure_fl;

    private EditText screen_hotspot_ssid_edt;

    private EditText screen_hotspot_pwd_edt;

    private ImageView screen_hotspot_open_iv;

    private ImageView screen_hotspot_secure_iv;

    private ImageView screen_hotspot_show_pwd_iv;

    private TextView screen_hotspot_ssid_tv;

    private TextView screen_hotspot_secure_tv;

    private TextView screen_hotspot_secure_sele_tv;

    private TextView screen_hotspot_pwd_tv;

    private TextView screen_hotspot_ssid_value_tv;

    private TextView screen_hotspot_secure_value_tv;

    private TextView screen_hotspot_show_pwd_tv;

    private Button screen_hotspot_save_btn;

    private boolean isShowPwd = false;

    public boolean isOpenHotspot = false;

    private boolean isSecureOpenType = false;

    private int secureType = NetUtils.SECURE_TYPE_WPA;

    private String secureString;

    private String ss_id1, wifi_secure1;

    private final NetWorkActivity activity;

    private final ConnectivityListener mListener;

    private ProgressDialog mDialog = null;
    private ConnectivityManager mCm;
    private OnStartTetheringCallback mStartTetheringCallback;
    //安全性
    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;

    public boolean mRestartWifiApAfterConfigChange;
    @SuppressLint("HandlerLeak")
    private final Handler wifiHotHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "wifiHotHandler setOpenHotspot");
            isOpenHotspot = true;
            try {
                if (NetUtils.setWifiApEnabled(mListener.getWifiManager(), null, isOpenHotspot)
                ) {
                    Log.i(TAG, "setOpenHotspot-setWifiApEnabled:" + isOpenHotspot);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            screen_hotspot_open_iv.setBackgroundResource(R.mipmap.on);
            screen_hotspot_ll.setVisibility(View.VISIBLE);
            screen_hotspot_save_btn.setVisibility(View.VISIBLE);
            ShowSaveStatusDialog();
        }
    };
    private WifiManager mWifiManager;

    public ScreenHotSponViewHolder(Context ctvContext, ConnectivityListener conListener) throws Exception {
        super();
        this.ctvContext = ctvContext.getApplicationContext();
        this.mListener = conListener;
        activity = (NetWorkActivity) ctvContext;

        initManager();
        initView();
        initData();
        Log.i(TAG, "----last-isOpenHotspot:" + isOpenHotspot);
    }

    /*

     */
    private void initManager() {
        mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mCm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean gHzBandSupported = mWifiManager.isDualBandSupported();
//        if (gHzBandSupported) {
        Log.d("qkmin--->", "是否支持5G" + gHzBandSupported);
//        }
    }

    private void initView() {
        screen_hotspot_ll = (LinearLayout) activity.findViewById(R.id.screen_hotspot_ll);
        screen_hotspot_open_fl = (FrameLayout) activity.findViewById(R.id.screen_hotspot_open_fl);
        screen_hotspot_secure_fl = (FrameLayout) activity.findViewById(R.id.screen_hotspot_secure_fl);
        screen_hotspot_show_pwd_fl = (FrameLayout) activity.findViewById(R.id.screen_hotspot_show_pwd_fl);
        screen_hotspot_pwd_fl = (FrameLayout) activity.findViewById(R.id.screen_hotspot_pwd_fl);

        screen_hotspot_ssid_edt = (EditText) activity.findViewById(R.id.screen_hotspot_ssid_edt);
        screen_hotspot_pwd_edt = (EditText) activity.findViewById(R.id.screen_hotspot_pwd_edt);

        screen_hotspot_open_iv = (ImageView) activity.findViewById(R.id.screen_hotspot_open_iv);
        screen_hotspot_secure_iv = (ImageView) activity.findViewById(R.id.screen_hotspot_secure_iv);
        screen_hotspot_show_pwd_iv = (ImageView) activity.findViewById(R.id.screen_hotspot_show_pwd_iv);

        screen_hotspot_ssid_tv = (TextView) activity.findViewById(R.id.screen_hotspot_ssid_tv);
        screen_hotspot_secure_tv = (TextView) activity.findViewById(R.id.screen_hotspot_secure_tv);
        screen_hotspot_secure_sele_tv = (TextView) activity.findViewById(R.id.screen_hotspot_secure_sele_tv);
        screen_hotspot_pwd_tv = (TextView) activity.findViewById(R.id.screen_hotspot_pwd_tv);
        screen_hotspot_show_pwd_tv = (TextView) activity.findViewById(R.id.screen_hotspot_show_pwd_tv);
        screen_hotspot_ssid_value_tv = (TextView) activity.findViewById(R.id.screen_hotspot_ssid_value_tv);
        screen_hotspot_secure_value_tv = (TextView) activity.findViewById(R.id.screen_hotspot_secure_value_tv);

        screen_hotspot_save_btn = (Button) activity.findViewById(R.id.screen_hotspot_save_btn);

        screen_hotspot_open_fl.setOnFocusChangeListener(this);
        screen_hotspot_ssid_edt.setOnFocusChangeListener(this);
        screen_hotspot_secure_iv.setOnFocusChangeListener(this);
        screen_hotspot_pwd_edt.setOnFocusChangeListener(this);
        screen_hotspot_show_pwd_fl.setOnFocusChangeListener(this);
        screen_hotspot_save_btn.setOnFocusChangeListener(this);
        screen_hotspot_open_fl.setOnClickListener(this);
        screen_hotspot_secure_iv.setOnClickListener(this);
        screen_hotspot_show_pwd_fl.setOnClickListener(this);
        screen_hotspot_save_btn.setOnClickListener(this);
        activity.findViewById(R.id.back_screen).setOnClickListener(this);
        ss_id1 = ctvContext.getString(R.string.ss_id1) + "  ";
        wifi_secure1 = ctvContext.getString(R.string.wifi_secure1) + "  ";
    }

    /**
     * initData(The function of the method)
     *
     * @Title: initData
     * @Description: TODO
     */
    private void initData() throws Exception {
        isOpenHotspot = NetUtils.isWifiApEnabled(mListener.getWifiManager());// mListener.getWifiManager().isWifiApEnabled();
        String wifiapband = SystemProperties.get(HOST_BAND_TYPE);
        Log.d(TAG, "wifiap isOpenHotspot = " + isOpenHotspot + " ----- wifiapband : " + wifiapband);
        if (isOpenHotspot && ("".equals(wifiapband) || wifiapband == null || wifiapband.equals("Apband2G"))) {
            isOpenHotspot = false;
        }
        setOpenHotspot(false);
        Log.d(TAG, "wifiap isOpenHotspot 111111111111111 = " + isOpenHotspot);
        if (isOpenHotspot) {
            screen_hotspot_open_iv.setBackgroundResource(R.mipmap.on);
            screen_hotspot_ll.setVisibility(View.VISIBLE);
            screen_hotspot_save_btn.setVisibility(View.VISIBLE);
        } else {
            screen_hotspot_open_iv.setBackgroundResource(R.mipmap.off);
            screen_hotspot_ll.setVisibility(View.INVISIBLE);
            screen_hotspot_save_btn.setVisibility(View.INVISIBLE);
        }
        setShowPwd();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (screen_hotspot_secure_iv.hasFocus()) {
                    switchSecure(false);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (screen_hotspot_secure_iv.hasFocus()) {
                    switchSecure(true);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (isSecureOpenType && screen_hotspot_secure_iv.hasFocus()) {
                    screen_hotspot_save_btn.requestFocus();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (isSecureOpenType && screen_hotspot_save_btn.hasFocus()) {
                    screen_hotspot_secure_iv.requestFocus();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.screen_hotspot_show_pwd_fl) {
            isShowPwd = !isShowPwd;
            setShowPwd();
        } else if (i == R.id.screen_hotspot_open_fl) {
            isOpenHotspot = !isOpenHotspot;
            try {
                setOpenHotspot(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (i == R.id.screen_hotspot_secure_iv) {
            switchSecure(true);
        } else if (i == R.id.screen_hotspot_save_btn) {
            secureString = screen_hotspot_secure_sele_tv.getText().toString();
            if (secureString.equals(ctvContext.getString(R.string.wifi_security_wpa2))) {
                secureType = NetUtils.SECURE_TYPE_WPA2;
            } else if (secureString.equals(ctvContext.getString(R.string.wifi_security_open))) {
                secureType = NetUtils.SECURE_TYPE_OPEN;
            } else {
                secureType = NetUtils.SECURE_TYPE_WPA;
            }
            try {
                saveWifiApConfig();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (i == R.id.back_screen) {
            activity.finish();
        }
    }

    private void saveWifiApConfig() throws Exception {

        WifiConfiguration mWifiConfig = getConfig(secureType);
        if (mWifiConfig != null) {
            /**
             * if soft AP is stopped, bring up
             * else restart with new config
             * TODO: update config on a running access point when framework support is added
             * qkmin 先关闭，收到广播ConnectivityManager.ACTION_TETHER_STATE_CHANGED  调用 setSoftapEnabled后启动
             */
            if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
                // MStar Android Patch Begin
                Log.d("TetheringSettings",
                        "Wifi AP config changed while enabled, stop and restart");
                mRestartWifiApAfterConfigChange = true;
                setSoftapEnabled(activity, false);
            }
            mWifiManager.setWifiApConfiguration(mWifiConfig);
            // MStar Android Patch End
//            int index = WifiApDialog.getSecurityTypeIndex(mWifiConfig);
//            mCreateNetwork.setSummary(String.format(getActivity().getString(CONFIG_SUBTEXT),
//                    mWifiConfig.SSID,
//                    mSecurityType[index]));
        } else {
            return;
        }
        ShowSaveStatusDialog();


//        String passwd = screen_hotspot_pwd_edt.getText().toString().trim();
//        if (secureType != NetUtils.SECURE_TYPE_OPEN
//                && (TextUtils.isEmpty(passwd) || passwd.length() < 8)) {
//            screen_hotspot_pwd_edt.setText("");
//            screen_hotspot_pwd_edt.requestFocus();
//            showToast(R.string.wifiap_pwd_notice);
//            return;
//        }
//        String ssid = screen_hotspot_ssid_edt.getText().toString().trim();
//        if (TextUtils.isEmpty(ssid)) {
//            screen_hotspot_ssid_edt.setText("");
//            screen_hotspot_ssid_edt.setHint(R.string.please_input_ssid);
//            showToast(R.string.please_input_ssid);
//            return;
//        }
//        WifiConfiguration config = new WifiConfiguration();
//        config.SSID = ssid;
//        switch (secureType) {
//            case NetUtils.SECURE_TYPE_WPA:
//                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
//                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
//                config.preSharedKey = passwd;
//                break;
//            case NetUtils.SECURE_TYPE_WPA2:
//                config.allowedKeyManagement.set(NetUtils.WPA2_PSK);
//                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
//                config.preSharedKey = passwd;
//                break;
//            case NetUtils.SECURE_TYPE_OPEN:
//                config.allowedKeyManagement.set(KeyMgmt.NONE);
//                break;
//            default:
//                return;
//
//        }
//        WifiManager wifiManager = mListener.getWifiManager();
//        L.d(TAG, "state=" + NetUtils.getWifiApState(wifiManager));
//        if (NetUtils.WIFI_AP_STATE_ENABLED == NetUtils.getWifiApState(wifiManager)) {
//            // restart wifiap
//            NetUtils.setWifiApEnabled(wifiManager, null, false);
//            NetUtils.setWifiApEnabled(wifiManager, config, true);
//        } else {
//            NetUtils.setWifiApConfiguration(wifiManager, config);
//        }
//        // configure successful
//        ShowSaveStatusDialog();
    }

    private void switchSecure(boolean isIncrease) {
        if (isIncrease) {
            secureType = (secureType + 1) % 3;
        } else {
            secureType = (secureType + 3 - 1) % 3;
        }
        Log.d(TAG, "mSecureType, " + secureType);
        switch (secureType) {
            case NetUtils.SECURE_TYPE_WPA:
                screen_hotspot_secure_sele_tv.setText(SECURE_TYPE[0]);
                break;
            case NetUtils.SECURE_TYPE_WPA2:
                screen_hotspot_secure_sele_tv.setText(SECURE_TYPE[1]);
                break;
            case NetUtils.SECURE_TYPE_OPEN:
                screen_hotspot_secure_sele_tv.setText(SECURE_TYPE[2]);
                break;
            default:
                break;
        }
        // hide passwd layout
        if (secureType == NetUtils.SECURE_TYPE_OPEN) {
            isSecureOpenType = true;
            screen_hotspot_pwd_fl.setVisibility(View.INVISIBLE);
            screen_hotspot_show_pwd_fl.setVisibility(View.INVISIBLE);
        } else {
            isSecureOpenType = false;
            screen_hotspot_pwd_fl.setVisibility(View.VISIBLE);
            screen_hotspot_show_pwd_fl.setVisibility(View.VISIBLE);
        }
    }

    public void setOpenHotspot(boolean isClik) throws Exception {
        Log.i(TAG, "setOpenHotspot  22222222-isOpenHotspot:" + isOpenHotspot);
        try {
            if (isClik) {
                if (isOpenHotspot) {
                    //先关闭在打开
                    setSoftapEnabled(activity, false);
                    setSoftapEnabled(activity, true);
                } else {
                    //关闭热点
                    setSoftapEnabled(activity, false);
                }
                if (isOpenHotspot) {
                    ShowSaveStatusDialog();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isOpenHotspot) {
            screen_hotspot_open_iv.setBackgroundResource(R.mipmap.on);
            screen_hotspot_ll.setVisibility(View.VISIBLE);
            screen_hotspot_save_btn.setVisibility(View.VISIBLE);
        } else {
            screen_hotspot_open_iv.setBackgroundResource(R.mipmap.off);
            screen_hotspot_ll.setVisibility(View.INVISIBLE);
            screen_hotspot_save_btn.setVisibility(View.INVISIBLE);
        }


    }

    @SuppressLint("SetTextI18n")
    public void refreshWifiApUi(boolean isInit, InitDataInfo data) {
        mStartTetheringCallback = new OnStartTetheringCallback();
        WifiConfiguration config = null;
        if (isInit) {
            config = data.getConfig();
        } else {
            WifiManager wifiManager = mListener.getWifiManager();
            config = NetUtils.getWifiApConfiguration(wifiManager);
        }
        if (config == null) {
            return;
        } else {
            // show Wi-Fi Ap info
            screen_hotspot_ssid_value_tv.setText(ss_id1 + config.SSID);
            screen_hotspot_ssid_edt.setText(config.SSID);
            if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
                screen_hotspot_secure_value_tv.setText(wifi_secure1
                        + ctvContext.getString(R.string.wifi_security_wpa));
                screen_hotspot_secure_sele_tv.setText(R.string.wifi_security_wpa);
                screen_hotspot_pwd_edt.setText(config.preSharedKey);
                secureType = NetUtils.SECURE_TYPE_WPA;

            } else if (config.allowedKeyManagement.get(NetUtils.WPA2_PSK)) {
                screen_hotspot_secure_value_tv.setText(wifi_secure1
                        + ctvContext.getString(R.string.wifi_security_wpa2));
                screen_hotspot_secure_sele_tv.setText(R.string.wifi_security_wpa2);
                screen_hotspot_pwd_edt.setText(config.preSharedKey);
                secureType = NetUtils.SECURE_TYPE_WPA2;
            } else {
                screen_hotspot_secure_value_tv.setText(wifi_secure1
                        + ctvContext.getString(R.string.wifi_security_open));
                screen_hotspot_secure_sele_tv.setText(R.string.wifi_security_open);
                secureType = NetUtils.SECURE_TYPE_OPEN;
                screen_hotspot_pwd_edt.setText("");
            }
            if (secureType == NetUtils.SECURE_TYPE_OPEN) {
                screen_hotspot_pwd_fl.setVisibility(View.INVISIBLE);
                screen_hotspot_show_pwd_fl.setVisibility(View.INVISIBLE);
                isSecureOpenType = true;
            } else {
                isSecureOpenType = false;
                screen_hotspot_pwd_fl.setVisibility(View.VISIBLE);
                screen_hotspot_show_pwd_fl.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setShowPwd() {
        if (isShowPwd) {
            screen_hotspot_show_pwd_iv.setBackgroundResource(R.mipmap.on);
            screen_hotspot_pwd_edt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            screen_hotspot_show_pwd_iv.setBackgroundResource(R.mipmap.off);
            screen_hotspot_pwd_edt.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    @Override
    public void onFocusChange(View view, boolean has_focus) {
        int i = view.getId();
        if (i == R.id.screen_hotspot_open_fl) {
            if (has_focus) {
                screen_hotspot_open_fl.setSelected(true);
            } else {
                screen_hotspot_open_fl.setSelected(false);
            }
        } else if (i == R.id.screen_hotspot_ssid_edt) {
            if (has_focus) {
                screen_hotspot_ssid_edt.setSelected(true);
                screen_hotspot_ssid_tv.setSelected(true);
                screen_hotspot_ssid_edt.selectAll();
            } else {
                screen_hotspot_ssid_edt.setSelected(false);
                screen_hotspot_ssid_tv.setSelected(false);
            }
        } else if (i == R.id.screen_hotspot_secure_iv) {
            if (has_focus) {
                screen_hotspot_secure_fl.setSelected(true);
                screen_hotspot_secure_tv.setSelected(true);
                screen_hotspot_secure_sele_tv.setSelected(true);
            } else {
                screen_hotspot_secure_tv.setSelected(false);
                screen_hotspot_secure_fl.setSelected(false);
                screen_hotspot_secure_sele_tv.setSelected(false);
            }
        } else if (i == R.id.screen_hotspot_pwd_edt) {
            if (has_focus) {
                screen_hotspot_pwd_tv.setSelected(true);
                screen_hotspot_pwd_edt.setSelected(true);
                screen_hotspot_pwd_edt.selectAll();
            } else {
                screen_hotspot_pwd_tv.setSelected(false);
                screen_hotspot_pwd_edt.setSelected(false);
            }
        } else if (i == R.id.screen_hotspot_show_pwd_fl) {
            if (has_focus) {
                screen_hotspot_show_pwd_tv.setSelected(true);
                screen_hotspot_show_pwd_fl.setSelected(true);
            } else {
                screen_hotspot_show_pwd_tv.setSelected(false);
                screen_hotspot_show_pwd_fl.setSelected(false);
            }
        } else if (i == R.id.screen_hotspot_save_btn) {
            if (!has_focus) {
                screen_hotspot_show_pwd_tv.setSelected(false);
            }
        }
    }

    private void showToast(int id) {
        Toast.makeText(ctvContext, id, Toast.LENGTH_SHORT).show();
    }

    /**
     * wifi ap state change receiver
     */
    public void upApStateChange(Intent intent) throws Exception {
        int state = intent.getIntExtra(NetUtils.EXTRA_WIFI_AP_STATE,
                NetUtils.WIFI_AP_STATE_FAILED);
        switch (state) {
            default:
            case NetUtils.WIFI_AP_STATE_ENABLING:
                Log.d(TAG, "WIFI_AP_STATE_ENABLING");
                break;
            case NetUtils.WIFI_AP_STATE_ENABLED:
                Log.d(TAG, "WIFI_AP_STATE_ENABLED   ---------- 1111  isOpenHotspot ： " + isOpenHotspot);
                String wifiapband = SystemProperties.get(HOST_BAND_TYPE);
                if (!isOpenHotspot && ("".equals(wifiapband) || wifiapband == null || wifiapband.equals("Apband2G"))) {
                    setOpenHotspot(false);
                    refreshWifiApUi(false, null);
                    break;
                }
                if (mDialog != null) {
                    mDialog.cancel();
                }
                showToast(R.string.wifiap_config_success);
                if (!isOpenHotspot) {
                    isOpenHotspot = true;
                    setOpenHotspot(false);
                    refreshWifiApUi(false, null);
                }
                break;
            case NetUtils.WIFI_AP_STATE_DISABLING:
                Log.d(TAG, "WIFI_AP_STATE_DISABLING");
                break;
            case NetUtils.WIFI_AP_STATE_DISABLED:
                Log.d(TAG, "WIFI_AP_STATE_DISABLED");
                if (isOpenHotspot) {
                    isOpenHotspot = false;
                }
                break;
        }
    }

    private void ShowSaveStatusDialog() {
        if (mDialog != null) {
            mDialog.cancel();
        }
        mDialog = new ProgressDialog(activity);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setTitle(ctvContext.getString(R.string.tip));
        mDialog.setOnKeyListener((dialog, keyCode, event) -> {
            // TODO Auto-generated method stub
            return false;
        });
        mDialog.setMessage(ctvContext.getString(R.string.wifi_ap_submitting));
        mDialog.show();
    }

    public void onExit() {
        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
        wifiHotHandler.removeCallbacksAndMessages(null);
        mStartTetheringCallback = null;

    }

    /**
     * ap 打开和关闭
     *
     * @param mContext
     * @param enable
     */
    public void setSoftapEnabled(Context mContext, boolean enable) {
        final ContentResolver cr = mContext.getContentResolver();
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (enable) {
            // TODO: 2019-12-05 qkmin
            SystemProperties.set(HOST_BAND_TYPE, SCREEN_HOST_BAND);
            mCm.startTethering(ConnectivityManager.TETHERING_WIFI, true, mStartTetheringCallback, wifiHotHandler);
        } else {
            mCm.stopTethering(ConnectivityManager.TETHERING_WIFI);
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    private static final class OnStartTetheringCallback extends
            ConnectivityManager.OnStartTetheringCallback {

        OnStartTetheringCallback() {
        }

        @Override
        public void onTetheringStarted() {
        }

        @Override
        public void onTetheringFailed() {
        }

    }

    public WifiConfiguration getConfig(int mSecurityTypeIndex) {

        WifiConfiguration config = new WifiConfiguration();

        /**
         * TODO: SSID in WifiConfiguration for soft ap
         * is being stored as a raw string without quotes.
         * This is not the case on the client side. We need to
         * make things consistent and clean it up
         */

        String ssid = screen_hotspot_ssid_edt.getText().toString().trim();
        if (TextUtils.isEmpty(ssid)) {
            screen_hotspot_ssid_edt.setText("");
            screen_hotspot_ssid_edt.setHint(R.string.please_input_ssid);
            showToast(R.string.please_input_ssid);
            return null;
        }
        config.SSID = ssid;

        switch (mSecurityTypeIndex) {
            case OPEN_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case WPA2_INDEX:
                config.allowedKeyManagement.set(KeyMgmt.WPA2_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                String passwd = screen_hotspot_pwd_edt.getText().toString().trim();
                if (secureType != NetUtils.SECURE_TYPE_OPEN
                        && (TextUtils.isEmpty(passwd) || passwd.length() < 8)) {
                    screen_hotspot_pwd_edt.setText("");
                    screen_hotspot_pwd_edt.requestFocus();
                    showToast(R.string.wifiap_pwd_notice);
                    return null;
                } else {
                    String password = passwd;
                    config.preSharedKey = password;
                }

                return config;
        }
        return null;
    }


}
