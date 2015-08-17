package com.hangzhou.tonight.maintabs;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;

import com.hangzhou.tonight.ChatActivity;
import com.hangzhou.tonight.LoginActivity;
import com.hangzhou.tonight.R;
import com.hangzhou.tonight.service.IConnectionStatusCallback;
import com.hangzhou.tonight.service.XXService;
import com.hangzhou.tonight.util.L;
import com.hangzhou.tonight.util.PreferenceConstants;
import com.hangzhou.tonight.util.PreferenceUtils;
import com.hangzhou.tonight.util.T;
import com.hangzhou.tonight.util.XMPPHelper;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements IConnectionStatusCallback{

	private TabHost mTabHost;
	private XXService mXxService;
	
	
	
	
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(MainActivity.this);
			// 开始连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(MainActivity.this,
						PreferenceConstants.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(
						MainActivity.this, PreferenceConstants.PASSWORD, "");
				mXxService.Login(usr, password);
				// mTitleNameView.setText(R.string.login_prompt_msg);
				// setStatusImage(false);
				// mTitleProgressBar.setVisibility(View.VISIBLE);
			} else {
				/*mTitleNameView.setText(XMPPHelper
						.splitJidAndServer(PreferenceUtils.getPrefString(
								MainActivity.this, PreferenceConstants.ACCOUNT,
								"")));
				setStatusImage(true);
				mTitleProgressBar.setVisibility(View.GONE);*/
				XMPPHelper.splitJidAndServer(PreferenceUtils.getPrefString(
						MainActivity.this, PreferenceConstants.ACCOUNT,
						""));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(MainActivity.this, XXService.class));
		setContentView(R.layout.activity_main);
		initViews();
		initTabs();
	}
	
	private void initViews() {
		mTabHost = getTabHost();
	}

	private void initTabs() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

		View nearbyView = inflater.inflate(
				R.layout.common_bottombar_tab_business, null);
		TabHost.TabSpec nearbyTabSpec = mTabHost.newTabSpec(
				BusinessActivity.class.getName()).setIndicator(nearbyView);
		nearbyTabSpec.setContent(new Intent(MainActivity.this,
				BusinessActivity.class));
		mTabHost.addTab(nearbyTabSpec);
		
		View sessionListView = inflater.inflate(
				R.layout.common_bottombar_tab_chat, null);
		TabHost.TabSpec sessionListTabSpec = mTabHost.newTabSpec(
				SessionListActivity.class.getName()).setIndicator(
				sessionListView);
		sessionListTabSpec.setContent(new Intent(MainActivity.this,
				SessionListActivity.class));
		mTabHost.addTab(sessionListTabSpec);		

		View nearbyFeedsView = inflater.inflate(
				R.layout.common_bottombar_tab_promotion, null);
		TabHost.TabSpec nearbyFeedsTabSpec = mTabHost.newTabSpec(
				PromotionActivity.class.getName()).setIndicator(
				nearbyFeedsView);
		nearbyFeedsTabSpec.setContent(new Intent(MainActivity.this,
				PromotionActivity.class));
		mTabHost.addTab(nearbyFeedsTabSpec);
		

		View contactView = inflater.inflate(
				R.layout.common_bottombar_tab_discover, null);
		TabHost.TabSpec contactTabSpec = mTabHost.newTabSpec(
				DiscoverActivity.class.getName()).setIndicator(contactView);
		contactTabSpec.setContent(new Intent(MainActivity.this,
				DiscoverActivity.class));
		mTabHost.addTab(contactTabSpec);

		View userSettingView = inflater.inflate(
				R.layout.common_bottombar_tab_profile, null);
		TabHost.TabSpec userSettingTabSpec = mTabHost.newTabSpec(
				ChatActivity.class.getName()).setIndicator(
				userSettingView);
		userSettingTabSpec.setContent(new Intent(MainActivity.this,
				UserSettingActivity.class));
		mTabHost.addTab(userSettingTabSpec);
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
	}
	
	
	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - firstTime < 3000) {
			finish();
			//T.showShort(this, R.string.press_again_backrun);
		} else {
			firstTime = System.currentTimeMillis();
			T.showShort(this, R.string.press_again_backrun);
		}
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			L.i(LoginActivity.class, "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			L.e(LoginActivity.class, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		L.i(LoginActivity.class, "[SERVICE] Unbind");
		bindService(new Intent(MainActivity.this, XXService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		switch (connectedState) {
		case XXService.CONNECTED:
			/*mTitleNameView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils
					.getPrefString(MainActivity.this,
							PreferenceConstants.ACCOUNT, "")));
			mTitleProgressBar.setVisibility(View.GONE);
			// mTitleStatusView.setVisibility(View.GONE);
			setStatusImage(true);*/
			T.showLong(this, "连接成功");
			break;
		case XXService.CONNECTING:
			/*mTitleNameView.setText(R.string.login_prompt_msg);
			mTitleProgressBar.setVisibility(View.VISIBLE);
			mTitleStatusView.setVisibility(View.GONE);*/
			
			break;
		case XXService.DISCONNECTED:
			/*mTitleNameView.setText(R.string.login_prompt_no);
			mTitleProgressBar.setVisibility(View.GONE);
			mTitleStatusView.setVisibility(View.GONE);
			T.showLong(this, reason);*/
			break;

		default:
			break;
		}
	}

	
	
}
