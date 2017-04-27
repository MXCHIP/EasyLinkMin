package com.fog.el;

import io.fogcloud.easylink.api.EasyLink;
import io.fogcloud.easylink.helper.EasyLinkCallBack;
import io.fogcloud.easylink.helper.EasyLinkParams;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private String TAG = "---main---";
	private final int _EL_S = 1;
	private final int _EL_F = 2;
	private boolean _ISSTART = false;

	// View
	private EditText ssid;
	private EditText pswid;
	private TextView startel;
	private TextView logsid;

	private EasyLink elink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		elink = new EasyLink(MainActivity.this);

		initView();
		initOnClick();

		listenwifichange();
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		ssid = (EditText) findViewById(R.id.ssid);
		pswid = (EditText) findViewById(R.id.pswid);
		logsid = (TextView) findViewById(R.id.logsid);
		startel = (TextView) findViewById(R.id.startel);

		ssid.setText(elink.getSSID());
	}

	/**
	 * 初始化点击事件
	 */
	private void initOnClick() {
		startel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setTag();
			}
		});
	}

	/**
	 * 开启配网
	 */
	private void startEl() {
		EasyLinkParams easylinkPara = new EasyLinkParams();
		easylinkPara.ssid = ssid.getText().toString().trim();
		easylinkPara.password = pswid.getText().toString().trim();
		easylinkPara.runSecond = 60000;
		easylinkPara.sleeptime = 10;

		elink.startEasyLink(easylinkPara, new EasyLinkCallBack() {
			@Override
			public void onSuccess(int code, String message) {
				Log.d(TAG, code + message);
				send2handler(_EL_S, message);
			}

			@Override
			public void onFailure(int code, String message) {
				Log.d(TAG, code + message);
				send2handler(_EL_F, message);
			}
		});
	}
	
	/**
	 * 停止配网
	 */
	private void stopEl() {
		
		elink.stopEasyLink(new EasyLinkCallBack() {
			@Override
			public void onSuccess(int code, String message) {
				Log.d(TAG, code + message);
				send2handler(_EL_S, message);
			}
			
			@Override
			public void onFailure(int code, String message) {
				Log.d(TAG, code + message);
				send2handler(_EL_F, message);
			}
		});
	}

	/**
	 * 监听配网时候调用接口的log，并显示在activity上
	 */
	Handler elhandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case _EL_S:
				logsid.append("\n" + msg.obj.toString());
				break;
			case _EL_F:
				logsid.append("\n" + msg.obj.toString());
				break;
			}
		};
	};

	/**
	 * 发送消息给handler
	 * 
	 * @param tag
	 * @param message
	 */
	private void send2handler(int tag, String message) {
		Message msg = new Message();
		msg.what = tag;
		msg.obj = message;
		elhandler.sendMessage(msg);
	}

	/**
	 * 设置TAG标记按钮
	 */
	private void setTag() {
		if (!_ISSTART) {
			startel.setBackgroundResource(R.color.red);
			startel.setText(R.string.stop_el);
			startEl();
		} else {
			startel.setBackgroundResource(R.color.blue_btn);
			startel.setText(R.string.start_el);
			stopEl();
		}
		_ISSTART = !_ISSTART;
	}

	/**
	 * 注册广播，监听WiFI变化
	 */
	private void listenwifichange() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	/**
	 * 广播分析
	 */
	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
					ssid.setText(elink.getSSID());
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}
}
