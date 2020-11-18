
package com.ctv.settings.network.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ctv.settings.network.Listener.ConnectivityListener;
import com.ctv.settings.network.R;
import com.ctv.settings.network.helper.WifiConfigHelper;
import com.ctv.settings.network.holder.WirelessViewHolder;
import com.ctv.settings.network.utils.NetUtils;
import com.ctv.settings.network.utils.Tools;
import com.ctv.settings.utils.L;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressLint("NewApi")
public class WiFiConnectDialog extends Dialog implements OnFocusChangeListener, OnClickListener {

    private static final String TAG = "WiFiConnectDialog";

    private final Context ctvContext;

    private EditText wifi_cn_pwd_edt, wifi_cn_ip_address, wifi_cn_netmask, wifi_cn_gateway,
            wifi_cn_dns1, wifi_cn_dns2;

    private TextView wifi_cn_secure_tv, wifi_cn_ssid_tv, wifi_cn_pwd_tv, wifi_cn_show_pwd_tv,
            wifi_connect_ip_tv, wifi_connect_ip_netmask_tv, wifi_connect_gateway_tv,
            wifi_connect_dns1_tv, wifi_connect_dns2_tv;

    private ImageView wifi_cn_show_pwd_iv, wifi_cn_auto_ip_iv;

    private FrameLayout wifi_cn_show_pwd_fl, wifi_cn_pwd_fl, wifi_cn_auto_ip_layout;

    private LinearLayout wifi_cn_ip_config_fl;

    private Button wifi_cn_save_btn, wifi_cn_cancle_btn, wifi_cn_forget_btn;

    private boolean isShowPwd = false;

    private boolean isShowStaticIP = false;

    private final ScanResult mScanResult;

    private int mSecureType = NetUtils.SECURE_OPEN;

    private static WirelessViewHolder wirelessViewHolder;

    private static WifiManager wifiManager;

    private boolean mHasConfiged = false;

    private final ConnectivityListener mListener;

    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;

    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;

    private ProxyInfo mHttpProxy = null;

    private StaticIpConfiguration mStaticIpConfiguration = null;

    private boolean isErrorTips = false;
    private Map<String, String> mArpMap = new HashMap<String, String>();

    private Thread mARPthread = null;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:// Save
                       // wirelessViewHolder.wirelessAdapter.updateTheTop(mScanResult.SSID);
                    wirelessViewHolder.wirelessAdapter.setmSsid(mScanResult.SSID);
                    wirelessViewHolder.wirelessAdapter.setConnectState(2);
                    break;
                case 2:// Forget
                    wirelessViewHolder.wirelessAdapter.setConnectState(0);
                case 3://start arp map;
                    mARPthread = new ArpThread();
                    mARPthread.start();
                    break;
            }
        }
    };

    public WiFiConnectDialog(Context context,
            WirelessViewHolder wirelessViewHolder, ScanResult scanResult,
            ConnectivityListener conListener) {
        super(context);
        this.ctvContext = context.getApplicationContext();
        this.mListener = conListener;
        this.mScanResult = scanResult;
        this.wirelessViewHolder = wirelessViewHolder;
        wifiManager = (WifiManager) ctvContext.getSystemService(Context.WIFI_SERVICE);
        setWindowStyle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wifi_connect);
        findViews();
        initData();
    }

    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        super.dismiss();
        if (mARPthread != null && mARPthread.isAlive()) {
            mARPthread.interrupt();
            mARPthread = null;
        }
		isShowPwd = false;
        isShowStaticIP = false;
        mSecureType = 0;
        mHasConfiged = false;
        mIpAssignment = IpAssignment.UNASSIGNED;
        mProxySettings = ProxySettings.UNASSIGNED;
        mHttpProxy = null;
        mStaticIpConfiguration = null;
        isErrorTips = false;
        mHandler.removeCallbacksAndMessages(null);
        textWatcher = null;
    }

    private void setWindowStyle() {
        Window w = getWindow();
        Resources res = ctvContext.getResources();
        Drawable drab = res.getDrawable(R.drawable.button_save_shape);
        w.setBackgroundDrawable(drab);
        w.setDimAmount(0.0f);
//        WindowManager.LayoutParams lp = w.getAttributes();
////        final float scale = res.getDisplayMetrics().density;
////        // In the mid-point to calculate the offset x and y
////        lp.x = (int) (82.7 * scale + 0.5f);
////        lp.y = (int) (-36 * scale + 0.5f);
////        lp.width = (int) (473.3 * scale + 0.5f);
////        lp.height = (int) (309.3 * scale + 0.5f);
////        w.setAttributes(lp);
//        lp.width = 680;
//        lp.height =480;
//        // Range is from 1.0 for completely opaque to 0.0 for no dim.
//        w.setDimAmount(0.0f);
//        w.setAttributes(lp);
    }

    /**
     * init compontent.
     */
    private void findViews() {
        // secure layout.
        wifi_cn_secure_tv = (TextView) findViewById(R.id.wifi_cn_secure_tv);
        wifi_cn_ssid_tv = (TextView) findViewById(R.id.wifi_cn_ssid_tv);
        // edit password layout
        wifi_cn_pwd_fl = (FrameLayout) findViewById(R.id.wifi_cn_pwd_fl);
        wifi_cn_pwd_tv = (TextView) findViewById(R.id.wifi_cn_pwd_tv);
        wifi_cn_pwd_edt = (EditText) findViewById(R.id.wifi_cn_pwd_edt);
        // show password layout
        wifi_cn_show_pwd_fl = (FrameLayout) findViewById(R.id.wifi_cn_show_pwd_fl);
        wifi_cn_show_pwd_tv = (TextView) findViewById(R.id.wifi_cn_show_pwd_tv);
        wifi_cn_show_pwd_iv = (ImageView) findViewById(R.id.wifi_cn_show_pwd_iv);

        // auto ip layout
        wifi_cn_auto_ip_layout = (FrameLayout) findViewById(R.id.wifi_edit_auto_ip_layout);
        wifi_cn_auto_ip_iv = (ImageView) findViewById(R.id.wifi_edit_auto_ip);

        // ip /gateway / netmask etc config layout
        wifi_cn_ip_config_fl = (LinearLayout) findViewById(R.id.wifi_ip_config_layout);
        wifi_connect_ip_tv = (TextView) findViewById(R.id.wifi_connect_ip_tv);
        wifi_connect_ip_netmask_tv = (TextView) findViewById(R.id.wifi_connect_ip_netmask_tv);
        wifi_connect_gateway_tv = (TextView) findViewById(R.id.wifi_connect_gateway_tv);
        wifi_connect_dns1_tv = (TextView) findViewById(R.id.wifi_connect_dns1_tv);
        wifi_connect_dns2_tv = (TextView) findViewById(R.id.wifi_connect_dns2_tv);
        wifi_cn_ip_address = (EditText) findViewById(R.id.wifi_connect_ip_ed);
        wifi_cn_netmask = (EditText) findViewById(R.id.wifi_connect_netmask_ed);
        wifi_cn_gateway = (EditText) findViewById(R.id.wifi_connect_gateway_ed);
        wifi_cn_dns1 = (EditText) findViewById(R.id.wifi_connect_dns1_ed);
        wifi_cn_dns2 = (EditText) findViewById(R.id.wifi_connect_dns2_ed);

        // bottom button
        wifi_cn_save_btn = (Button) findViewById(R.id.wifi_cn_save_btn);
        wifi_cn_cancle_btn = (Button) findViewById(R.id.wifi_cn_cancle_btn);
        wifi_cn_forget_btn = (Button) findViewById(R.id.wifi_cn_forget_btn);

        wifi_cn_cancle_btn.setOnFocusChangeListener(this);
        wifi_cn_save_btn.setOnFocusChangeListener(this);
        wifi_cn_save_btn.setOnClickListener(this);
        wifi_cn_save_btn.setVisibility(View.GONE);
        wifi_cn_forget_btn.setOnFocusChangeListener(this);
        wifi_cn_forget_btn.setOnClickListener(this);
        wifi_cn_cancle_btn.setOnClickListener(this);
        wifi_cn_show_pwd_fl.setOnFocusChangeListener(this);
        wifi_cn_show_pwd_fl.setOnClickListener(this);
        wifi_cn_auto_ip_layout.setOnFocusChangeListener(this);
        wifi_cn_auto_ip_layout.setOnClickListener(this);
        wifi_cn_pwd_edt.setOnFocusChangeListener(this);
        wifi_cn_pwd_edt.addTextChangedListener(textWatcher);
        wifi_cn_pwd_edt.selectAll();
        wifi_cn_ip_address.setOnFocusChangeListener(this);
        wifi_cn_netmask.setOnFocusChangeListener(this);
        wifi_cn_gateway.setOnFocusChangeListener(this);
        wifi_cn_dns1.setOnFocusChangeListener(this);
        wifi_cn_dns2.setOnFocusChangeListener(this);

        setShowPwd();
        setShowStaticIP();
    }

    private void setShowPwd() {
        if (isShowPwd) {
            wifi_cn_show_pwd_iv.setBackgroundResource(R.mipmap.on);
            wifi_cn_pwd_edt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            wifi_cn_show_pwd_iv.setBackgroundResource(R.mipmap.off);
            wifi_cn_pwd_edt.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    private void setShowStaticIP() {
        if (isShowStaticIP) {
            wifi_cn_auto_ip_iv.setBackgroundResource(R.mipmap.off);
            wifi_cn_ip_config_fl.setVisibility(View.VISIBLE);
            //mHandler.sendEmptyMessage(3);//check ip conflict,scan arp.
        } else {
            wifi_cn_auto_ip_iv.setBackgroundResource(R.mipmap.on);
            wifi_cn_ip_config_fl.setVisibility(View.GONE);
        }
    }

    private void initData() {
        mSecureType = Tools.getSecurity(mScanResult);
        String selectSsid = mScanResult.SSID;
        wifi_cn_ssid_tv.setText(selectSsid);
        Log.i(TAG, "-------mSecureType:" + mSecureType);
        if (mSecureType == NetUtils.SECURITY_NONE) {
            wifi_cn_secure_tv.setText(R.string.wifi_security_open);
            wifi_cn_save_btn.setVisibility(View.VISIBLE);
        } else if (mSecureType == NetUtils.SECURITY_PSK) {
            wifi_cn_secure_tv.setText(R.string.wifi_security_wp);
        } else if (mSecureType == NetUtils.SECURITY_WEP) {
            wifi_cn_secure_tv.setText(R.string.wifi_security_wep);
        } else {
            wifi_cn_secure_tv.setText(R.string.wifi_security_eap);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        mHasConfiged = false;
        if (selectSsid.equals(wifiInfo.getSSID().replace("\"", ""))
                && !selectSsid.equals(wirelessViewHolder.forgetingSsid)) {
            mHasConfiged = true;
            wifi_cn_forget_btn.setVisibility(View.VISIBLE);
            wifi_cn_save_btn.setVisibility(View.GONE);
            wifi_cn_pwd_fl.setVisibility(View.GONE);
            wifi_cn_show_pwd_fl.setVisibility(View.GONE);
        } else {
            wifi_cn_forget_btn.setVisibility(View.GONE);
            wifi_cn_save_btn.setVisibility(View.VISIBLE);
            if (mSecureType == NetUtils.SECURE_OPEN) {
                wifi_cn_pwd_fl.setVisibility(View.GONE);
                wifi_cn_show_pwd_fl.setVisibility(View.GONE);
            } else {
                wifi_cn_pwd_fl.setVisibility(View.VISIBLE);
                wifi_cn_show_pwd_fl.setVisibility(View.VISIBLE);
                wifi_cn_pwd_edt.requestFocus();
                if (SystemProperties.get("ro.board.platform").equals("macan")
                        && SystemProperties.get("persist.sys.brand", "CTV").equalsIgnoreCase(
                                "VIANO")) {
                    wifi_cn_pwd_edt.setFocusable(true);
                    wifi_cn_pwd_edt.setFocusableInTouchMode(true);
                    InputMethodManager imm = (InputMethodManager) wifi_cn_pwd_edt.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                }
            }
        }
        Log.i(TAG, "-------mHasConfiged:" + mHasConfiged);
    }

    public void showWifiDevInfo() {
        String ipString = "";
        String netMask = "";
        String gateWay = "";
        String dns1 = "";
        String dns2 = "";
        LinkProperties linkProperties = null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo && null != wifiInfo.getSSID()
                && wifiInfo.getNetworkId() != NetUtils.INVALID_NETWORK_ID) {
            linkProperties = mListener.getWifiLinkProperties();
        }
        int ip = wifiInfo.getIpAddress();
        ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        Log.i(TAG, "-wifi_ip- :" + ipString);
        String[] wifi_ips = new String[4];
        String[] wifi_gateways = new String[4];
        String[] wifi_masks = new String[4];
        wifi_ips = ipString.split("\\.");
        if (linkProperties != null) {
            for (RouteInfo route : linkProperties.getRoutes()) {
                if (route.isDefaultRoute()) {
                    wifi_gateways = route.getGateway().getHostAddress().trim().split("\\.");
                    gateWay = route.getGateway().getHostAddress().trim();
                }
            }
        }
        if (wifi_ips != null) {
            for (int i = 0; i < wifi_ips.length; i++) {
                wifi_masks[i] = wifi_ips[i].equals(wifi_gateways[i]) ? "255" : "0";
            }
        }
        StringBuffer address = new StringBuffer();
        address.append(wifi_masks[0]);
        address.append(".");
        address.append(wifi_masks[1]);
        address.append(".");
        address.append(wifi_masks[2]);
        address.append(".");
        address.append(wifi_masks[3]);
        netMask = address.toString();
        if (linkProperties != null) {
        Iterator<InetAddress> dnsIterator = linkProperties.getDnsServers().iterator();
        if (dnsIterator.hasNext()) {
            dns1 = dnsIterator.next().getHostAddress();
            if (!dns1.contains(".")) {
                dns1 = "8.8.8.8";
            }
        }
        if (dnsIterator.hasNext()) {
            dns2 = dnsIterator.next().getHostAddress();
            if (!dns2.contains(".")) {
                dns2 = "8.8.4.4";
                }
            }
        }
        wifi_cn_ip_address.setText(ipString);
        wifi_cn_netmask.setText(netMask);
        wifi_cn_gateway.setText(gateWay);
        wifi_cn_dns1.setText(dns1);
        wifi_cn_dns2.setText(dns2);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            wirelessViewHolder.isDialog = false;
            dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.wifi_cn_show_pwd_fl) {
            isShowPwd = !isShowPwd;
            setShowPwd();
        } else if (i == R.id.wifi_edit_auto_ip_layout) {
            isShowStaticIP = !isShowStaticIP;
            setShowStaticIP();
            setStaticIpButtonChanged(isShowStaticIP);
            if (mHasConfiged) {
                showWifiDevInfo();
            }
        } else if (i == R.id.wifi_cn_save_btn) {
            isErrorTips = false;
            wirelessViewHolder.isDialog = false;
            if (checkIpFields()) {
                isErrorTips = false;
                saveDate();
            }
            if (!isErrorTips) {
                dismiss();
            }
        } else if (i == R.id.wifi_cn_cancle_btn) {
            wirelessViewHolder.isDialog = false;
            dismiss();
        } else if (i == R.id.wifi_cn_forget_btn) {
            Log.i(TAG, "------------- forget ");
            wirelessViewHolder.isDialog = false;
            WifiConfiguration config = null;
            try {
                config = WifiConfigHelper.getConfigurationForNetwork(ctvContext,
                        mScanResult);
                WifiConfigHelper.forgetConfiguration(ctvContext, config);
                if(WirelessViewHolder.wirelessAdapter !=null){
                    WirelessViewHolder.wirelessAdapter.setmSsid("");
                    WirelessViewHolder.wirelessAdapter.setConnectState(0);
                }
//                wirelessAdapter.setScanResults(scanResults);
            } catch (Exception e) {
                L.d(e.toString());
            }
            dismiss();
        }
    }

    private void setStaticIpButtonChanged(boolean isStaticIP) {
        if (isStaticIP) {
            wifi_cn_save_btn.setVisibility(View.VISIBLE);
            wifi_cn_forget_btn.setVisibility(View.GONE);
            if (mSecureType == NetUtils.SECURE_OPEN || mHasConfiged) {
                wifi_cn_pwd_fl.setVisibility(View.GONE);
                wifi_cn_show_pwd_fl.setVisibility(View.GONE);
            } else {
                wifi_cn_pwd_fl.setVisibility(View.VISIBLE);
                wifi_cn_show_pwd_fl.setVisibility(View.VISIBLE);
            }
        } else {
            if (mHasConfiged) {
                wifi_cn_save_btn.setVisibility(View.GONE);
                wifi_cn_forget_btn.setVisibility(View.VISIBLE);
                wifi_cn_forget_btn.setClickable(true);
                wifi_cn_forget_btn.setFocusable(true);
            }else{
                if (wifi_cn_pwd_edt.getText().length() >= 8 || mSecureType == NetUtils.SECURITY_NONE) {
                    wifi_cn_save_btn.setVisibility(View.VISIBLE);
                } else {
                    wifi_cn_save_btn.setVisibility(View.GONE);
                }
            }
        }
    }

    private void saveDate() {
        Log.i(TAG, "--saveDate-----mHasConfiged:" + mHasConfiged);
        WifiConfiguration config = null;
        setProxyFields();
        setIpFields();
        config = getNewConfig();
        Log.i(TAG, "------new wifi config:" + config);
        if (config == null) {
            showToast(R.string.not_support_security_hint);
            return;
        } else {
            WifiConfigHelper.saveConfiguration(ctvContext, config);
            if (mHasConfiged) {
                wifiManager.disconnect();
            }
            Log.i(TAG, "newConnect connect to ssid");
//            wifiManager.connect(config, mSaveListener);
            try {
                NetUtils.connect(wifiManager,config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        wirelessViewHolder.initAssociatState(config);
    }

    private WifiConfiguration getNewConfig() {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"".concat(mScanResult.SSID.replace("\"", "\\\"")).concat("\"");
        Log.i(TAG, "initUi mSecureType: " + mSecureType);
        if (mHasConfiged) {
            config.networkId = wifiManager.getConnectionInfo().getNetworkId();
        }
        String passwd = wifi_cn_pwd_edt.getText().toString().trim();
        switch (mSecureType) {
            case NetUtils.SECURE_OPEN:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;
            case NetUtils.SECURE_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (passwd.length() != 0) {
                    int length = passwd.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && passwd.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = passwd;
                    } else {
                        config.wepKeys[0] = '"' + passwd + '"';
                    }
                }
                break;
            case NetUtils.SECURE_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (passwd.length() != 0) {
                    if (passwd.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = passwd;
                    } else {
                        config.preSharedKey = '"' + passwd + '"';
                    }
                }
                break;
            case NetUtils.SECURE_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                break;
            default:
                return null;
        }
        try {
            NetUtils.setIpConfiguration(config,new IpConfiguration(mIpAssignment, mProxySettings,
                    mStaticIpConfiguration, mHttpProxy));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        config.setIpConfiguration(new IpConfiguration(mIpAssignment, mProxySettings,
//                mStaticIpConfiguration, mHttpProxy));

        return config;
    }

    @Override
    public void onFocusChange(View view, boolean has_focus) {
        int i = view.getId();
        if (i == R.id.wifi_cn_pwd_edt) {
            if (has_focus) {
                wifi_cn_pwd_tv.setSelected(true);
                wifi_cn_pwd_edt.setSelected(true);
                wifi_cn_pwd_edt.selectAll();
            } else {
                wifi_cn_pwd_edt.setSelected(false);
                wifi_cn_pwd_tv.setSelected(false);
            }
        } else if (i == R.id.wifi_cn_show_pwd_fl) {
            if (has_focus) {
                wifi_cn_show_pwd_fl.setSelected(true);
                wifi_cn_show_pwd_tv.setSelected(true);
            } else {
                wifi_cn_show_pwd_tv.setSelected(false);
                wifi_cn_show_pwd_fl.setSelected(false);
            }
        } else if (i == R.id.wifi_edit_auto_ip_layout) {
            if (has_focus) {
                wifi_cn_auto_ip_layout.setSelected(true);
                wifi_cn_auto_ip_iv.setSelected(true);
            } else {
                wifi_cn_auto_ip_layout.setSelected(false);
                wifi_cn_auto_ip_iv.setSelected(false);
            }
        } else if (i == R.id.wifi_connect_ip_ed) {
            if (has_focus) {
                wifi_connect_ip_tv.setSelected(true);
                wifi_cn_ip_address.setSelected(true);
            } else {
                wifi_connect_ip_tv.setSelected(false);
                wifi_cn_ip_address.setSelected(false);
            }
        } else if (i == R.id.wifi_connect_netmask_ed) {
            if (has_focus) {
                wifi_connect_ip_netmask_tv.setSelected(true);
                wifi_cn_netmask.setSelected(true);
            } else {
                wifi_connect_ip_netmask_tv.setSelected(false);
                wifi_cn_netmask.setSelected(false);
            }
        } else if (i == R.id.wifi_connect_gateway_ed) {
            if (has_focus) {
                wifi_connect_gateway_tv.setSelected(true);
                wifi_cn_gateway.setSelected(true);
            } else {
                wifi_connect_gateway_tv.setSelected(false);
                wifi_cn_gateway.setSelected(false);
            }
        } else if (i == R.id.wifi_connect_dns1_ed) {
            if (has_focus) {
                wifi_connect_dns1_tv.setSelected(true);
                wifi_cn_dns1.setSelected(true);
            } else {
                wifi_connect_dns1_tv.setSelected(false);
                wifi_cn_dns1.setSelected(false);
            }
        } else if (i == R.id.wifi_connect_dns2_ed) {
            if (has_focus) {
                wifi_connect_dns2_tv.setSelected(true);
                wifi_cn_dns2.setSelected(true);
            } else {
                wifi_connect_dns2_tv.setSelected(false);
                wifi_cn_dns2.setSelected(false);
            }
        } else if (i == R.id.wifi_cn_save_btn) {
        } else if (i == R.id.wifi_cn_cancle_btn) {
            if (has_focus) {
                wifi_cn_cancle_btn.setSelected(true);
            } else {
                wifi_cn_cancle_btn.setSelected(false);
            }
        } else if (i == R.id.wifi_cn_forget_btn) {
            if (has_focus) {
                wifi_cn_forget_btn.setSelected(true);
            } else {
                wifi_cn_forget_btn.setSelected(false);
            }
        }
    }

//    private final WifiManager.ActionListener mForgetListener = new ActionListener() {
//
//        @Override
//        public void onSuccess() {
//            Log.d(TAG, "forget success");
//            mHandler.sendEmptyMessage(2);
//        }
//
//        @Override
//        public void onFailure(int reason) {
//            showToast(R.string.wifi_failed_forget_message);
//        }
//    };
//
//    private final WifiManager.ActionListener mSaveListener = new WifiManager.ActionListener() {
//
//        @Override
//        public void onSuccess() {
//            mHandler.sendEmptyMessage(1);
//        }
//
//        @Override
//        public void onFailure(int reason) {
//            showToast(R.string.wifi_failed_save_message);
//        }
//    };

    private void showToast(int id) {
        if (id <= 0) {
            return;
        }
        Toast.makeText(ctvContext, id, Toast.LENGTH_SHORT).show();
    }

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (wifi_cn_pwd_edt.getText().length() >= 8) {
                wifi_cn_save_btn.setVisibility(View.VISIBLE);
            } else {
                wifi_cn_save_btn.setVisibility(View.GONE);
            }
        }
    };

    private boolean checkIpFields() {
        String ip = wifi_cn_ip_address.getText().toString();
        String netMask = wifi_cn_netmask.getText().toString();
        String gateWay = wifi_cn_gateway.getText().toString();
        String dns1 = wifi_cn_dns1.getText().toString();
        String dns2 = wifi_cn_dns2.getText().toString();
        mStaticIpConfiguration = new StaticIpConfiguration();
        if (!isShowStaticIP) {
            return true;
        }
        if (ip == null || netMask == null || gateWay == null) {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;
        } else if (ip.trim().equals("") || netMask.trim().equals("") || gateWay.trim().equals("")) {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;
        }
        if (TextUtils.isEmpty(ip)) {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;
        }
        String currentIP = mListener.getWifiIpAddress();
         if (Tools.isIPConflict(ip, currentIP)) {
         /* static ip conflict */
         isErrorTips = true;
         showToast(R.string.ethernet_tip_ip_same);
         return false;
         }
//        if (mArpMap != null) {
//            Log.d(TAG,"---------ARP map:"+mArpMap.toString());
//            if(mArpMap.containsKey(ip)){
//            isErrorTips = true;
//            showToast(R.string.ethernet_tip_ip_same);
//            return false;
//            }
//        }
        if (Tools.matchIP(ip)) {
            Inet4Address inetAddr = null;
            inetAddr = (Inet4Address) NetworkUtils.numericToInetAddress(ip);
            int networkPrefixLength = 0;
            if (netMask != null && Tools.matchIP(netMask)) {
                networkPrefixLength = Tools.calcPrefixLengthByMask(netMask);
            } else {
                isErrorTips = true;
                showToast(R.string.check_ip_failure);
                return false;
            }
            try {
                if (networkPrefixLength >= 0 && networkPrefixLength <= 32) {
                    mStaticIpConfiguration.ipAddress = NetUtils.getLinkAddress(inetAddr,networkPrefixLength);
//                            new LinkAddress(inetAddr,
//                            networkPrefixLength);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                isErrorTips = true;
                showToast(R.string.check_ip_failure);
                return false;
            }
        } else {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;
        }
        Log.i(TAG, "gateway==" + gateWay);
        if (!TextUtils.isEmpty(gateWay)) {
            if (Tools.matchIP(gateWay)) {
                mStaticIpConfiguration.gateway = (Inet4Address) NetworkUtils
                        .numericToInetAddress(gateWay);
            } else {
                isErrorTips = true;
                showToast(R.string.check_ip_failure);
                return false;
            }
        } else {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;

        }
        if (!TextUtils.isEmpty(dns1) && Tools.matchIP(dns1)) {
            mStaticIpConfiguration.dnsServers.add((Inet4Address) NetworkUtils
                    .numericToInetAddress(dns1));
        }
        if (!TextUtils.isEmpty(dns2) && Tools.matchIP(dns2)) {
            mStaticIpConfiguration.dnsServers.add((Inet4Address) NetworkUtils
                    .numericToInetAddress(dns2));
        }
        if (TextUtils.isEmpty(dns2) && TextUtils.isEmpty(dns1)) {
            isErrorTips = true;
            showToast(R.string.check_ip_failure);
            return false;
        }
        return true;
    }

    // kwj
    private void setIpFields() {
        // is auto ip
        if (!isShowStaticIP) {
            mIpAssignment = IpAssignment.DHCP;
        } else {
            mIpAssignment = IpAssignment.STATIC;
        }
    }

    private void setProxyFields() {
        if (mListener.hasProxy()) {
            mProxySettings = ProxySettings.STATIC;
        } else {
            mProxySettings = ProxySettings.NONE;
            mHttpProxy = null;
        }
    }

    public class ArpThread extends Thread {
        public void run() {
            while (!isErrorTips) {
                try {
                    if (mArpMap != null && !mArpMap.isEmpty()) {
                        mArpMap.clear();
                    }
                    mArpMap = Tools.createArpMap();
                    isErrorTips = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }
}
