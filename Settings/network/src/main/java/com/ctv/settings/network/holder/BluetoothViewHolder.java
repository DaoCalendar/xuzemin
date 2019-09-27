
package com.ctv.settings.network.holder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;
import com.ctv.settings.base.BaseViewHolder;
import com.ctv.settings.network.R;
import com.ctv.settings.network.adapter.BluetoothSimperAdapter;
import com.ctv.settings.network.utils.ClsUtils;
import com.ctv.settings.network.utils.NetUtils;
import com.ctv.settings.network.view.BluetoothListView;
import com.ctv.settings.utils.L;
import com.ctv.settings.utils.T;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 蓝牙模块
 * @author xuzemin
 * @date 2019/09/19
 */

public class BluetoothViewHolder extends BaseViewHolder implements OnFocusChangeListener, View.OnClickListener {

	private static final String TAG = "BluetoothViewHolder";

	private ProgressDialog mDialog = null;

	private Activity activity;

	private BluetoothSimperAdapter bluetoothSimperAdapter = null;

	private BluetoothSimperAdapter bluetoothSimperAdapter_paired = null;

	private FrameLayout Bluetooth_switch_fl = null;//,tooth_discover_time_fl= null;

	private ImageView Bluetooth_switch_iv = null;

	private Context ctvContext;

	private BluetoothListView bluetooth_lv = null;

	private BluetoothListView bluetooth_pair_lv = null;

	private RelativeLayout bluetooth_pb = null;

	private BluetoothAdapter mBluetoothAdapter = null;

	private List<BluetoothDevice> blueToothDeviceList = null;

	private List<BluetoothDevice> blueToothDeviceList_paired = null;

	private BluetoothDevice connectDevice = null;

	private boolean isOpenBluetooth = false;

	protected Resources resources;

	private final static int THREAD_WAIT = 500;

	private final static int BLUETOOTH_STATE = 0;

	private final static int BLUETOOTH_CONNECT = 1;

	private int currentState = 0;

	private boolean isConnecting = false;

	private LinearLayout ll_on_all = null;

	private LinearLayout ll_bluetooth_lv = null;

	private LinearLayout ll_pair_lv = null;

	private static BluetoothA2dp mA2dpProfile = null;

	private static BluetoothInputDevice bluetoothInputDevice = null;

	private static final int REFRESHH_GET_TIME = 2;

	private static final int REFRESHH_SHOW_TIME = 4;

	private static int time = 10;

	private static int showtime = 0;

	private List<BluetoothDevice> hasDevices = null;

	private BluetoothProfile.ServiceListener listener_Profile = new BluetoothProfile.ServiceListener() {
		@Override
		public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
			switch (i){
				//外设输入设备
				case NetUtils.DEVICE_INPUT_DEVICE:
					bluetoothInputDevice = (BluetoothInputDevice) bluetoothProfile;
					if(bluetoothInputDevice.connect(connectDevice)){
						L.e(TAG,"bluetoothInputDevice connect true");
					}else{
						L.e(TAG,"bluetoothInputDevice connect false");
					}
					break;
				//音频耳机设备
				case BluetoothProfile.A2DP:
					mA2dpProfile = (BluetoothA2dp) bluetoothProfile;
					try {
						ClsUtils.connect_A2dp(mA2dpProfile.getClass(),mA2dpProfile,connectDevice);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
			isConnecting = false;
			connectDevice = null;
			checkConnectDevice();
			connectDevice = null;
			bluetoothSimperAdapter.setConnectDeviceNull();
			bluetoothSimperAdapter_paired.setConnectDeviceNull();
			bluetoothSimperAdapter.notifyDataSetChanged();
			bluetoothSimperAdapter_paired.notifyDataSetChanged();

		}

		@Override
		public void onServiceDisconnected(int i) {
		}
	};

	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

		public void onReceive(Context context,Intent intent){

			String action = intent.getAction();
			Bundle b = intent.getExtras();
			BluetoothDevice device;
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				boolean ishave;
				int typeid = device.getBluetoothClass().getMajorDeviceClass();
				switch (typeid){
					case 1024:
					case 1280:
					case 7936:
						ishave = false;
						break;
					default:
						ishave = true;
						break;
				}
				if(!ishave&&blueToothDeviceList_paired !=null && blueToothDeviceList_paired.size() > 0){
					for(int i = 0,length = blueToothDeviceList_paired.size();i<length;i++){
						BluetoothDevice pairedDevice = blueToothDeviceList_paired.get(i);
						if(device.getAddress().equals(pairedDevice.getAddress())){
							ishave = true;
							break;
						}
					}
				}
				if(!ishave){
					if(blueToothDeviceList !=null && blueToothDeviceList.size() > 0){
						for(int i = 0,length = blueToothDeviceList.size();i<length;i++){
							BluetoothDevice hadDevice = blueToothDeviceList.get(i);
							if(device.getAddress().equals(hadDevice.getAddress())){
								ishave = true;
								break;
							}
						}
					}else{
						blueToothDeviceList = new ArrayList<>();
					}
				}
				if(!ishave){
					if(device.getName()!=null && !device.getName().equals("")) {
						blueToothDeviceList.add(device);
						bluetoothSimperAdapter.setDeviceList(blueToothDeviceList);
						bluetoothSimperAdapter.notifyDataSetChanged();
						setListViewHeightBasedOnChildren();
					}
				}

			}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
					case BluetoothDevice.BOND_BONDING://正在配对
						L.d(TAG, "正在配对......");
						bluetoothSimperAdapter.setConnectDevice(connectDevice);
						break;
					case BluetoothDevice.BOND_BONDED://配对结束
						L.d(TAG, "完成配对");
						if (mDialog != null) {
							mDialog.cancel();
						}
						isConnecting = true;
						mBluetoothAdapter.cancelDiscovery();
						switch (device.getBluetoothClass().getMajorDeviceClass()){
							case 1024:
								L.e(TAG,"this is a Audio");
								device.createBond();
								mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
										BluetoothProfile.A2DP);
								break;
							case 1280:
								L.e(TAG,"this is a Input");
								device.createBond();
								mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
										NetUtils.DEVICE_INPUT_DEVICE);
								break;
						}
						checkConnectDevice();
						bluetoothSimperAdapter.setConnectDeviceNull();
						break;
					case BluetoothDevice.BOND_NONE://取消配对/未配对
						L.d(TAG, "取消配对");
						if (mDialog != null) {
							mDialog.cancel();
						}
						bluetoothSimperAdapter.setConnectDeviceNull();
					default:
						break;
				}
			}else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
				BluetoothDevice connect_Device =
						intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				abortBroadcast();
				L.d(TAG, "pair"+connect_Device.setPairingConfirmation(true));
				//				connect_Device.cancelPairingUserInput();
				int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
				L.d(TAG, "pair mType"+mType);
				boolean ret;
				int pairingKey;
				switch (mType) {
					case 0:
					case 1:
					case 3:
					case 6:
					case 7:
						pairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY,
								BluetoothDevice.ERROR);
						L.e(TAG,"pairingKey"+pairingKey);
						try {
							if(pairingKey != 0){
								ClsUtils.setPin(connect_Device.getClass(), connect_Device, String.format(Locale.US, "%06d", pairingKey));
							}else {
								ClsUtils.setPin(connect_Device.getClass(), connect_Device, "0000");
							}
							ret = true;
						} catch (Exception e) {
							e.printStackTrace();
							ret = false;
						}
						break;
					case 2:
					case 4:
					case 5:
						pairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY,
								BluetoothDevice.ERROR);
						String mPairingKey = String.format(Locale.US, "%06d", pairingKey);
						ret = false;
						L.d(TAG, "pair ret"+ret);
						T.showShort(ctvContext,"mPairingKey"+mPairingKey);
						ShowInputDeviceDialog(mPairingKey);
						break;

					default:
						try {
							ret =ClsUtils.setPin(connect_Device.getClass(), connect_Device, "0000");
						} catch (Exception e) {
							e.printStackTrace();
							ret = false;
						}
						break;
				}

				if(ret){
					connect_Device.createBond();
					connectDevice = connect_Device;
					mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
							BluetoothProfile.A2DP);
					mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
							NetUtils.DEVICE_INPUT_DEVICE);
				}
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private final Handler bluetoothHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BLUETOOTH_STATE:
					if (mBluetoothAdapter == null) {
						mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					}else{
						currentState = mBluetoothAdapter.getState();
						switch (currentState){
							case 10:  //蓝牙未开启
								if (mDialog != null) {
									mDialog.cancel();
								}
								isConnecting = false;
								if (mBluetoothAdapter.isDiscovering()) {
									mBluetoothAdapter.cancelDiscovery();
								}
								blueToothDeviceList.clear();
								blueToothDeviceList_paired.clear();
								connectDevice = null;
								mA2dpProfile = null;
								bluetoothInputDevice = null;
								break;
							case 11:  //蓝牙正在开启
								if (mDialog != null) {
									mDialog.cancel();
								}
								isConnecting = false;
								break;
							case 12:   //蓝牙已开启
								L.e(TAG, "bluetooth is isConnecting"+isConnecting);
								if(isOpenBluetooth){
									if(!isConnecting) {
										if (!mBluetoothAdapter.isDiscovering()) {
											if (mBluetoothAdapter.startDiscovery()) {
												L.e(TAG, "bluetooth is startDiscovery");
											} else {
												L.e(TAG, "bluetooth is not startDiscovery");
											}
										}
									}
								}
								break;
						}
						if(isOpenBluetooth){
							if(time < REFRESHH_GET_TIME){
								time++;
							}else{
								time = 0;
								checkConnectDevice();
								getPairedDevices();
							}
						}
					}
					refreshUI(ll_on_all);
					refreshUI(bluetooth_pb);
					refreshUI(Bluetooth_switch_iv);
					refreshUI(ll_bluetooth_lv);
					refreshUI(ll_pair_lv);
					bluetoothHandler.sendEmptyMessageDelayed(BLUETOOTH_STATE,THREAD_WAIT);
					break;
				case BLUETOOTH_CONNECT:
					if(showtime < REFRESHH_SHOW_TIME){
						showtime++;
						bluetoothHandler.sendEmptyMessageDelayed(BLUETOOTH_CONNECT,THREAD_WAIT);
					}else{
						showtime = 0;
						if (mDialog != null) {
							mDialog.dismiss();
							mDialog.cancel();
							mDialog = null;
						}
					}
				case 2:
					break;
				case 3:
					break;
				default:
					break;
			}
		}
	};


	public BluetoothViewHolder(Activity activity) {
		super(activity);
		// 注册事件
		initUI(activity);
		initData(activity);
		initListener();
	}


	/*
	检测已连接蓝牙设备
	 */
	private void checkConnectDevice(){
		hasDevices = new ArrayList<>();
		List<BluetoothDevice> hasDevices_bak;
		boolean isconnect = false;
		if(mA2dpProfile !=null) {
			hasDevices_bak = mA2dpProfile.getConnectedDevices();
			for (BluetoothDevice bluedevice : hasDevices_bak) {
				L.e(TAG, "mA2dpProfile connectedDevices" + bluedevice.getName());
				hasDevices.add(bluedevice);
				if(connectDevice!=null) {
					if (bluedevice.getAddress().equals(connectDevice.getAddress())) {
						isconnect = true;
					}
				}
			}
		}


		if(bluetoothInputDevice !=null) {
			hasDevices_bak = bluetoothInputDevice.getConnectedDevices();
			for (BluetoothDevice bluedevice : hasDevices_bak) {
				L.e(TAG, "bluetoothInputDevice connectedDevices" + bluedevice.getName());
				hasDevices.add(bluedevice);
				if(!isconnect && connectDevice!=null) {
					if (bluedevice.getAddress().equals(connectDevice.getAddress())) {
						isconnect = true;
					}
				}
			}
		}

		bluetoothSimperAdapter_paired.setConnectDevice(hasDevices);
		bluetoothSimperAdapter_paired.notifyDataSetChanged();

		int length = blueToothDeviceList.size();
		for(int i = 0;i<length;i++){
			BluetoothDevice device = blueToothDeviceList.get(i);
			if(hasDevices.contains(device)){
				blueToothDeviceList.remove(device);
				i--;
				length--;
			}
		}
		connectDevice = null;
	}

	/*
	获取已匹配设备
	 */
	private void getPairedDevices(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices != null && pairedDevices.size() > 0) {
			blueToothDeviceList_paired = new ArrayList<BluetoothDevice>();
			if(hasDevices != null && hasDevices.size() > 0){
				blueToothDeviceList_paired.addAll(hasDevices);
			}

			for (BluetoothDevice device : pairedDevices) {
				boolean isHaveConnected = false;

				if(hasDevices.contains(device)){
					isHaveConnected = true;
					L.e(TAG,"hasDevices contain");
				}

				if (!isHaveConnected){
					L.e(TAG,"isHaveConnected false");
					switch (device.getBluetoothClass().getMajorDeviceClass()) {
						case 1024:
							blueToothDeviceList_paired.add(device);
							mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
									BluetoothProfile.A2DP);
							break;
						case 1280:
							blueToothDeviceList_paired.add(device);
							mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
									NetUtils.DEVICE_INPUT_DEVICE);
							break;
						case 7936:
							blueToothDeviceList_paired.add(device);
							mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
									NetUtils.DEVICE_INPUT_DEVICE);
							mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
									BluetoothProfile.A2DP);
							break;
						default:
							break;
					}
				}
			}
			if(blueToothDeviceList_paired != null && blueToothDeviceList_paired.size()>0){
				ll_pair_lv.setVisibility(View.VISIBLE);
				bluetooth_pb.setVisibility(View.GONE);
			}else{
				ll_pair_lv.setVisibility(View.GONE);
			}
			bluetoothSimperAdapter_paired.setDeviceList(blueToothDeviceList_paired);
			bluetoothSimperAdapter_paired.notifyDataSetChanged();
			setListViewHeightBasedOnChildren();
		}else{
			ll_pair_lv.setVisibility(View.GONE);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				Bluetooth_switch_iv.requestFocus();
				bluetooth_lv.setSelection(0);
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				if (Bluetooth_switch_iv.hasFocus()) {
					return true;
				}
				if (!bluetooth_pair_lv.hasFocus()) {
					if (bluetooth_lv.hasFocus()) {
						if (ll_pair_lv.getVisibility() == View.VISIBLE) {
							bluetooth_pair_lv.requestFocus();
						} else {
							Bluetooth_switch_iv.requestFocus();
						}
						return true;
					}
					Bluetooth_switch_iv.requestFocus();
					return true;
				} else {
					Bluetooth_switch_iv.requestFocus();
					return true;
				}
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (Bluetooth_switch_iv.hasFocus()) {
					if(isOpenBluetooth){
						if(ll_pair_lv.getVisibility() == View.VISIBLE) {
							bluetooth_pair_lv.requestFocus();
							bluetooth_pair_lv.setSelection(0);
							return true;
						}
						if(ll_bluetooth_lv.getVisibility() == View.VISIBLE) {
							bluetooth_lv.requestFocus();
							bluetooth_lv.setSelection(0);
							return true;
						}
					}
					return true;
				}
				if (ll_pair_lv.hasFocus()) {
					if(bluetooth_lv.getVisibility() == View.VISIBLE) {
						bluetooth_lv.requestFocus();
						bluetooth_lv.setSelection(0);
						return true;
					}
				}
				return true;
			default:
				break;
		}
		return false;
	}

	@Override
	public void onFocusChange(View view, boolean has_focus) {
		L.i(TAG, "--onFocusChange" + view.getId());
		if (view.getId() == R.id.bluetooth_switch_iv) {
			if (has_focus) {
				Bluetooth_switch_fl.setSelected(true);
			} else {
				Bluetooth_switch_fl.setSelected(false);
			}
		}
	}

	public void onExit() {
		if (bluetoothSimperAdapter != null) {
			bluetoothSimperAdapter.onExit();
		}
		ctvContext.unregisterReceiver(mReceiver);
		bluetoothHandler.removeMessages(BLUETOOTH_STATE);
		bluetoothSimperAdapter = null;
		Bluetooth_switch_fl = null;
		Bluetooth_switch_iv = null;
		bluetooth_lv = null;
		bluetooth_pb = null;
		isOpenBluetooth = false;
		bluetoothHandler.removeCallbacksAndMessages(null);
	}

	public void onResume(){
		if(ctvContext != null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			ctvContext.registerReceiver(mReceiver, filter);
			bluetoothHandler.sendEmptyMessageDelayed(BLUETOOTH_STATE, 2000);
		}
	}

	/*
	设置双层ListView滑动长度
	 */
	private void setListViewHeightBasedOnChildren() {
		int totalHeight = 0;
		int count = 0;
		if (bluetoothSimperAdapter != null) {
			totalHeight += NetUtils.dip2px(ctvContext,60) * bluetoothSimperAdapter.getCount();
			ViewGroup.LayoutParams params = bluetooth_lv.getLayoutParams();
			params.height = totalHeight+ NetUtils.dip2px(ctvContext,60) + (bluetooth_lv.getDividerHeight() * (count - 1));
			bluetooth_lv.setLayoutParams(params);
		}
		totalHeight = 0;
		if(bluetoothSimperAdapter_paired != null){
			totalHeight += NetUtils.dip2px(ctvContext,60) * bluetoothSimperAdapter_paired.getCount() ;//listItem.getMeasuredHeight();
			ViewGroup.LayoutParams params = bluetooth_pair_lv.getLayoutParams();
			params.height = totalHeight + (bluetooth_pair_lv.getDividerHeight() * (count - 1));
			bluetooth_pair_lv.setLayoutParams(params);
		}
	}

	private void unPaired(final BluetoothDevice bluetoothDevice){
		if(bluetoothDevice == null){
			return;
		}
		DialogInterface.OnClickListener ok = (dialog, which) -> {
			if(blueToothDeviceList_paired.contains(bluetoothDevice)){
				try {
					//解除匹配
					ClsUtils.removeBond(bluetoothDevice.getClass(),bluetoothDevice);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		AlertDialog dialog = new AlertDialog.Builder(activity)
				.setCancelable(true)
				.setTitle(R.string.wifi_display_disconnect_title)
				.setMessage(
						Html.fromHtml(resources.getString(
								R.string.wifi_display_disconnect_text,
								bluetoothDevice.getName())))
				.setPositiveButton(android.R.string.ok, ok)
				.setNegativeButton(android.R.string.cancel, null).create();
		dialog.show();
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

		bluetoothHandler.sendEmptyMessage(BLUETOOTH_CONNECT);
	}

	private void ShowInputDeviceDialog(String password) {
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
		mDialog.setMessage(ctvContext.getString(R.string.bluetooth_enter_passkey_other_device)+password);
		mDialog.show();
	}

	/**
	 * 初始化UI
	 *
	 * @param activity
	 */
//	@Override
	public void initUI(Activity activity) {
		this.ctvContext = activity.getApplicationContext();
		this.activity = activity;
		resources = ctvContext.getResources();

		ll_on_all = activity.findViewById(R.id.ll_on_all);
		ll_bluetooth_lv = activity.findViewById(R.id.ll_bluetooth_lv);
		ll_pair_lv = activity.findViewById(R.id.ll_pair_lv);
		Bluetooth_switch_fl = activity.findViewById(R.id.bluetooth_switch_fl);
		Bluetooth_switch_iv = activity.findViewById(R.id.bluetooth_switch_iv);
		bluetooth_pb = activity.findViewById(R.id.bluetooth_pb);
		bluetooth_lv = activity.findViewById(R.id.bluetooth_lv);
		bluetooth_pair_lv = activity.findViewById(R.id.bluetooth_pair_lv);

		bluetooth_pb.setVisibility(View.GONE);
		ll_pair_lv.setVisibility(View.GONE);
		ll_bluetooth_lv.setVisibility(View.GONE);
	}

	/**
	 * 初始化数据
	 *
	 **/
//	@Override
	public void initData(Activity activity) {
		if(blueToothDeviceList ==null){
			blueToothDeviceList = new ArrayList<>();
		}
		if(blueToothDeviceList_paired ==null){
			blueToothDeviceList_paired = new ArrayList<>();
		}
		bluetoothSimperAdapter = new BluetoothSimperAdapter(activity, blueToothDeviceList);
		bluetooth_lv.setAdapter(bluetoothSimperAdapter);
		bluetoothSimperAdapter_paired = new BluetoothSimperAdapter(activity, blueToothDeviceList_paired);
		bluetooth_pair_lv.setAdapter(bluetoothSimperAdapter_paired);
		setListViewHeightBasedOnChildren();
	}

	/**
	 * 初始化监听
	 */
//	@Override
	@SuppressLint("MissingPermission")
	public void initListener() {
		Bluetooth_switch_iv.setOnFocusChangeListener(this);
		Bluetooth_switch_iv.setOnClickListener(this);
		bluetooth_lv.setVerticalScrollBarEnabled(true);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter != null) {
			mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
					NetUtils.DEVICE_INPUT_DEVICE);
			mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
					BluetoothProfile.A2DP);
			checkConnectDevice();
			L.d("isEnabled"+mBluetoothAdapter.isEnabled());
			if (mBluetoothAdapter.isEnabled()) {
				isOpenBluetooth = true;
				isConnecting = false;
				getPairedDevices();
			}
			L.d("isOpenBluetooth"+isOpenBluetooth);
			if(isOpenBluetooth){
				Bluetooth_switch_iv.setBackgroundResource(R.mipmap.on);
				ll_on_all.setVisibility(View.VISIBLE);
			}else{
				Bluetooth_switch_iv.setBackgroundResource(R.mipmap.off);
				ll_on_all.setVisibility(View.GONE);
			}
		}

		bluetooth_pair_lv.setOnItemClickListener((parent, view, position, id) -> {
			ShowSaveStatusDialog();
			connectDevice = blueToothDeviceList_paired.get(position);
			if(hasDevices.contains(connectDevice)){
				unPaired(connectDevice);
			}else{
				isConnecting = true;
				mBluetoothAdapter.cancelDiscovery();
				bluetoothSimperAdapter_paired.setConnectDevice(connectDevice);
				bluetoothSimperAdapter_paired.notifyDataSetChanged();
				switch (connectDevice.getBluetoothClass().getMajorDeviceClass()){
					case 1024:
						L.e(TAG,"this is a Audio");
						mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
								BluetoothProfile.A2DP);
						break;
					case 1280:
						L.e(TAG,"this is a Input");
						mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
								NetUtils.DEVICE_INPUT_DEVICE);
						break;
				}
			}
		});
		bluetooth_lv.setOnItemClickListener((parent, view, position, id) -> {
			L.i(TAG, "-----setOnItemClickListener--position:" + position);
			ShowSaveStatusDialog();
			isConnecting = true;
			mBluetoothAdapter.cancelDiscovery();
			connectDevice = blueToothDeviceList.get(position);
			bluetoothSimperAdapter.setConnectDevice(connectDevice);
			bluetoothSimperAdapter.notifyDataSetChanged();
			switch (connectDevice.getBluetoothClass().getMajorDeviceClass()){
				case 1024:
					L.e(TAG,"this is a Audio");
					connectDevice.createBond();
					mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
							BluetoothProfile.A2DP);
					break;
				case 1280:
					L.e(TAG,"this is a Input");
					connectDevice.createBond();
					mBluetoothAdapter.getProfileProxy(ctvContext, listener_Profile,
							NetUtils.DEVICE_INPUT_DEVICE);
					break;
			}
		});
	}

	/**
	 * 刷新指定view
	 *
	 */
//	@Override
	public void refreshUI(View view) {
		int id = view.getId();
		if(isOpenBluetooth){
			if(id == R.id.ll_on_all) {
				view.setVisibility(View.VISIBLE);
			}
			if(id == R.id.bluetooth_switch_iv) {
				view.setBackgroundResource(R.mipmap.on);
			}
			if(blueToothDeviceList!=null && blueToothDeviceList.size()> 0){
				if(id == R.id.ll_bluetooth_lv) {
					view.setVisibility(View.VISIBLE);
				}
				if(id == R.id.bluetooth_pb) {
					view.setVisibility(View.GONE);
				}
			}else{
				if(id == R.id.ll_bluetooth_lv) {
					view.setVisibility(View.GONE);
				}
			}
			if(blueToothDeviceList_paired!=null && blueToothDeviceList_paired.size()> 0){
				if(id == R.id.ll_pair_lv) {
					view.setVisibility(View.VISIBLE);
				}
				if(id == R.id.bluetooth_pb) {
					view.setVisibility(View.GONE);
				}
			}else{
				if(id == R.id.ll_pair_lv) {
					view.setVisibility(View.GONE);
				}
			}
		}else{
			if(id == R.id.ll_on_all) {
				view.setVisibility(View.GONE);
			}

			if(id == R.id.bluetooth_switch_iv) {
				view.setBackgroundResource(R.mipmap.off);
			}
		}
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bluetooth_switch_iv) {
			if (mBluetoothAdapter.isEnabled() && isOpenBluetooth) {
				mBluetoothAdapter.disable();
				isOpenBluetooth = false;
				ll_on_all.setVisibility(View.GONE);
			} else if (!mBluetoothAdapter.isEnabled() && !isOpenBluetooth) {
				mBluetoothAdapter.enable();
				isOpenBluetooth = true;
				ll_on_all.setVisibility(View.VISIBLE);
				bluetooth_pb.setVisibility(View.VISIBLE);
				ll_bluetooth_lv.setVisibility(View.GONE);
				ll_pair_lv.setVisibility(View.GONE);
			}
		}
	}
}
