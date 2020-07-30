
package com.ctv.settings.network.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.NetworkUtils;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.util.Log;

import com.cultraview.tv.CtvTvManager;
import com.cultraview.tv.common.exception.CtvCommonException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright (C), 2015-12-8, CultraView
 * @author Write Macro.Song(songhong@cultraview.com)
 * @since 1.0.0 Tools.
 */
@SuppressLint({
        "WorldReadableFiles", "WorldWriteableFiles", "NewApi"
})
public class Tools {

    private static final String TAG = "Tools";

    private static final String DEFAULT_MAC_ADDRESS = "00:30:1B:BA:02:DB";

    /**
     * isNetInterfaceAvailable(The function of the method)
     *
     * @Description: Tools.isNetInterfaceAvailable(ifName);
     * @param ifName
     * @return
     */
    public static boolean isNetInterfaceAvailable(String ifName) {
        String netInterfaceStatusFile = "/sys/class/net/" + ifName + "/carrier";
        char st = readStatus(netInterfaceStatusFile);
        if (st == '1') {
            return true;
        }
        return false;
    }

    private synchronized static char readStatus(String filePath) {
        int tempChar = 0;
        File file = new File(filePath);
        if (file.exists()) {
            Reader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(file));
                tempChar = reader.read();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (char) tempChar;
    }

    public static boolean matchIP(String ip) {
        String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    /**
     * getSecurity(The function of the method)
     *
     * @Description: Tools.getSecurity(result);
     * @param result
     * @return
     */
    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return NetUtils.SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return NetUtils.SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return NetUtils.SECURITY_EAP;
        }
        return NetUtils.SECURITY_NONE;
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return NetUtils.SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return NetUtils.SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? NetUtils.SECURITY_WEP : NetUtils.SECURITY_NONE;
    }

    /**
     * getBooleanPref(The function of the method)
     *
     * @Title: Tools.getBooleanPref(ctvContext,name,defValue);
     * @param ctvContext
     * @param name
     * @param defValue
     * @return
     */
    @SuppressWarnings("deprecation")
    public static boolean getBooleanPref(Context ctvContext, String name, boolean defValue) {
        String pkg = ctvContext.getPackageName();
        SharedPreferences sharedPreferences = ctvContext.getSharedPreferences(pkg,
                Context.MODE_PRIVATE + Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(name, defValue);
    }

    /**
     * setBooleanPref(The function of the method)
     *
     * @Description: Tools.setBooleanPref(ctvContext,name,defValue);
     * @param ctvContext
     * @param name
     * @param value
     */
    @SuppressWarnings("deprecation")
    public static void setBooleanPref(Context ctvContext, String name, boolean value) {
        String pkg = ctvContext.getPackageName();
        SharedPreferences prefs = ctvContext.getSharedPreferences(pkg, Context.MODE_PRIVATE
                + Context.MODE_PRIVATE);
        Editor ed = prefs.edit();
        ed.putBoolean(name, value);
        ed.commit();
    }

    public static boolean is638CIBN() {
        if (SystemProperties.get("client.config").equals("CIBN")
                && SystemProperties.get("ro.build.product").equals("cv6a638_base")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * clearMemory(The function of the method)
     *
     * @Description: Tools.clearMemory(ctvContext);
     * @param ctvContext
     */
    public static void clearMemory(Context ctvContext) {
        ActivityManager am = (ActivityManager) ctvContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infoList = am.getRunningAppProcesses();
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                RunningAppProcessInfo appProcessInfo = infoList.get(i);
                if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {
                        if (isKillPro(pkgList[j])) {
                            Log.d(TAG, " killed package name : " + pkgList[j]);
                            am.killBackgroundProcesses(pkgList[j]);
                        }
                    }
                }

            }
        }
        // Log show availMem
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        Log.d(TAG, "availMem >" + mi.availMem / (1024 * 1024));
    }

    private static boolean isKillPro(String pName) {
        if (pName.equals("com.android.providers.userdictionary")) {
            return false;
        } else if (pName.equals("com.android.providers.contacts")) {
            return false;
        } else if (pName.equals("com.android.externalstorage")) {
            return false;
        } else if (pName.equals("com.google.android.gms")) {
            return false;
        } else if (pName.equals("com.google.android.gsf")) {
            return false;
        } else if (pName.equals("com.android.mcast")) {
            return false;
        }
        return true;
    }

//    public static String getEthernetIpAddress(Context mContext) {
//        @SuppressLint("WrongConstant") ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
//                .getSystemService(NetUtils.ETHERNET_SERVICE);
////        LinkProperties linkProperties = mConnectivityManager
////                .getLinkProperties(Network.fromNetworkHandle(ConnectivityManager.TYPE_ETHERNET));
//
//        LinkProperties linkProperties =
//                mConnectivityManager.getLinkProperties(Network.fromNetworkHandle(ConnectivityManager.TYPE_ETHERNET));
//        // MStar Android Patch Begin
//        if (linkProperties != null) {
//            try {
//                List<LinkAddress> linkAddressList = NetUtils.getAllLinkAddresses(linkProperties);
//                for (LinkAddress linkAddress : linkAddressList) {
//                    InetAddress address = linkAddress.getAddress();
//                    if (address instanceof Inet4Address) {
//                        return address.getHostAddress();
//                    }
//                }
//            } catch (Exception e) {
//                L.d(e.toString());
//            }
//        }
//        // MStar Android Patch End
//
//        // IPv6 address will not be shown like WifiInfo internally does.
//        return "";
//    }

    /**
     * convert mask addr to prefixlength
     *
     * @param mask:mask addr
     * @return:prefixlength
     */
    public static int calcPrefixLengthByMask(String mask) {
        int prefixLength = -1;
        Inet4Address iNetmask = (Inet4Address) NetworkUtils.numericToInetAddress(mask);
        if (checkValidMask(iNetmask)) {
            try {
                int netmask = NetworkUtils.inetAddressToInt(iNetmask);
                prefixLength = NetworkUtils.netmaskIntToPrefixLength(netmask);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        return prefixLength;
    }

    /**
     * check if mask addr is valid
     *
     * @param inetAddr: mask
     * @return:true: mask address is valid false: mask address is not valid
     */
    public static boolean checkValidMask(InetAddress inetAddr) {
        byte[] addr = inetAddr.getAddress();
        if (addr.length != 4) {
            Log.e(TAG, "not valid ip address");
            return false;
        }

        int mask = (addr[0] << 24) | ((addr[1] & 0xff) << 16) | ((addr[2] & 0xff) << 8)
                | ((addr[3] & 0xff) & 0xff);

        boolean bitset = false;
        boolean valid = true;
        for (int i = 0; i < 32; ++i) {
            boolean curBitSet = 1 == (mask & 1) ? true : false;
            if (bitset) {
                if (!curBitSet) {
                    valid = false;
                    Log.e(TAG, "check valid mask : invalid bit at : " + i);
                    break;
                }
            } else if (curBitSet) {
                bitset = true;
                Log.i(TAG, "check valid mask : bit set start at : " + i);
            }
            mask = mask >> 1;
        }
        Log.i(TAG, "valid==" + valid);
        return valid;
    }

    public static boolean isIPConflict(String setIP, String currentIP) {
        String result = null;
        boolean isConflict = false;
        Process p = null;
        Log.d(TAG, "===ip conflict");
        if (currentIP != null && !currentIP.equals("")) {
            if (currentIP.equals(setIP)) {
                Log.d(TAG, "======set ip as a same as auto ip");
                return false;
            }
        }
        try {
            /* "-c 1" is send time,1 is send once,-w is wait time */
            p = Runtime.getRuntime().exec("ping -c 4 -i 0.2 -W 2 " + setIP);
            int status = p.waitFor();
            if (status == 0) {
                result = "ip is used";
                isConflict = true;
            } else {
                result = "ip is not used";
                isConflict = false;
            }
        } catch (IOException e) {
            result = "IOException";
            isConflict = false;

        } catch (InterruptedException e) {
            result = "InterruptedException";
            isConflict = false;

        } finally {
            Log.i(TAG, "==================result = " + result);
            p.destroy();
        }
        return isConflict;
    }

    /**
     * getWirelessMacAddress(The function of the method)
     *
     * @Title: getWirelessMacAddress
     * @Description: TODO
     * @param ctvContext
     * @return
     */
    public static String getWirelessMacAddress(Context ctvContext) {
        WifiManager wm = (WifiManager) ctvContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wm.getConnectionInfo();
        if (info == null) {
            return "";
        }
        return info.getMacAddress();
    }

    /**
     * getWireMacAddress(The function of the method)
     *
     * @Title: getWireMacAddress
     * @Description: TODO
     * @return
     */
    public static String getWireMacAddress(Context ctvContext) {

        String macAddress = "";
        String filePath = "/sys/class/net/eth0/address";
        StringBuffer fileData = new StringBuffer(1000);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            macAddress = fileData.toString().toUpperCase(Locale.ENGLISH).substring(0, 17);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, "macAddress error!");
        }
        if (macAddress.equals(DEFAULT_MAC_ADDRESS)) {
            Log.d(TAG, "DEFAULT_MAC_ADDRESS: 00:30:1B:BA:02:DB");
            return "00.00.00.00.00.00";
        }
        String envMac = "";
        try {
            envMac = CtvTvManager.getInstance().getEnvironment("macaddr");
        } catch (CtvCommonException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(TAG, "envMac1 = " + envMac);
        if (envMac == null || envMac.equals("")) {
            return "00.00.00.00.00.00";
        }
        envMac = envMac.toUpperCase(Locale.ENGLISH);
        if (macAddress.equals(envMac)) {
            return macAddress;
        } else {
            return "00.00.00.00.00.00";
        }
    }

    public static Map<String, String> createArpMap() throws IOException, InterruptedException {
        Map<String, String> checkMapARP = new HashMap<String, String>();
        BufferedReader localBufferdReader = new BufferedReader(new FileReader(new File(
                "/proc/net/arp")));
        String line = "";
        while ((line = localBufferdReader.readLine()) == null) {
            localBufferdReader.close();
            Thread.sleep(1000);
            localBufferdReader = new BufferedReader(new FileReader(new File("/proc/net/arp")));
        }
        do {
            String[] ipmac = line.split("[ ]+");
            if (!ipmac[0].matches("IP")) {
                String ip = ipmac[0];
                String mac = ipmac[3];
                Log.d(TAG, "------------------------------ip" + ip);
                Log.d(TAG, "------------------------------mac" + mac);
                if (!checkMapARP.containsKey(ip)) {
                    checkMapARP.put(ip, mac);
                }
            }
        } while ((line = localBufferdReader.readLine()) != null);
        localBufferdReader.close();
        return checkMapARP;
    }
    public static void keyInjection(final int keyCode) {

        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }.start();
    }
}
