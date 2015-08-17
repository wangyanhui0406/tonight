package com.hangzhou.tonight;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.hangzhou.tonight.base.BaseActivity;
import com.hangzhou.tonight.maintabs.MainActivity;
import com.hangzhou.tonight.service.IConnectionStatusCallback;
import com.hangzhou.tonight.service.XXService;
import com.hangzhou.tonight.util.Base64Utils;
import com.hangzhou.tonight.util.HttpRequest;
import com.hangzhou.tonight.util.JsonResolveUtils;
import com.hangzhou.tonight.util.JsonUtils;
import com.hangzhou.tonight.util.L;
import com.hangzhou.tonight.util.MD5Utils;
import com.hangzhou.tonight.util.PreferenceConstants;
import com.hangzhou.tonight.util.PreferenceUtils;
import com.hangzhou.tonight.util.RC4Utils;
import com.hangzhou.tonight.util.T;
import com.hangzhou.tonight.view.HeaderLayout;
import com.hangzhou.tonight.view.HeaderLayout.HeaderStyle;

public class LoginActivity extends BaseActivity implements OnClickListener,
		IConnectionStatusCallback {

	public static final String LOGIN_ACTION = "com.way.action.LOGIN";
	private static final int LOGIN_OUT_TIME = 0;

	private HeaderLayout mHeaderLayout;
	private Button mLoginBtn;
	private Button mRegisterBtn;
	private EditText mAccountEt;
	private EditText mPasswordEt;

	private XXService mXxService;
	private String mAccount;
	private String mPassword;

	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XXService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(LoginActivity.this);
			// 开始连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(LoginActivity.this,
						PreferenceConstants.ACCOUNT, "");
				String password = PreferenceUtils.getPrefString(
						LoginActivity.this, PreferenceConstants.PASSWORD,
						"");
				//mXxService.Login(usr, password);
				// mTitleNameView.setText(R.string.login_prompt_msg);
				// setStatusImage(false);
				// mTitleProgressBar.setVisibility(View.VISIBLE);
			} else {
				/*
				 * mTitleNameView.setText(XMPPHelper
				 * .splitJidAndServer(PreferenceUtils.getPrefString(
				 * MainActivity.this, PreferenceConstants.ACCOUNT, "")));
				 * setStatusImage(true);
				 * mTitleProgressBar.setVisibility(View.GONE);
				 */
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};
	
	
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		if (connectedState == XXService.CONNECTED) {
			save2Preferences();
			//startActivity(new Intent(this, MainActivity.class));
			//finish();
		} else if (connectedState == XXService.DISCONNECTED)
			T.showLong(LoginActivity.this, getString(R.string.request_failed)
					+ reason);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// startService(new Intent(LoginActivity.this, XXService.class));
		bindXMPPService();
		initViews();
		init();
		initEvents();
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
		Intent mServiceIntent = new Intent(this, XXService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	@Override
	protected void initViews() {
		mHeaderLayout = (HeaderLayout) findViewById(R.id.login_header);
		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
		mHeaderLayout.setDefaultTitle("登录", null);

		mAccountEt = (EditText) findViewById(R.id.login_et_account);
		mPasswordEt = (EditText) findViewById(R.id.login_et_pwd);
		mLoginBtn = (Button) findViewById(R.id.login_btn_login);
		mRegisterBtn = (Button) findViewById(R.id.login_btn_register);

	}

	@Override
	protected void init() {
		String account = PreferenceUtils.getPrefString(this,
				PreferenceConstants.ACCOUNT, "");
		String password = PreferenceUtils.getPrefString(this,
				PreferenceConstants.PASSWORD, "");
		if (!TextUtils.isEmpty(account))
			mAccountEt.setText(account);
		if (!TextUtils.isEmpty(password))
			mPasswordEt.setText(password);

	}

	@Override
	protected void initEvents() {
		mLoginBtn.setOnClickListener(LoginActivity.this);
		mRegisterBtn.setOnClickListener(LoginActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn_login:
			/*
			 * Intent intent = new Intent(LoginActivity.this,
			 * MainActivity.class); startActivity(intent);
			 */
			login();
			break;
		case R.id.login_btn_register:
			Intent intent2 = new Intent(LoginActivity.this,
					RegisterActivity.class);
			startActivity(intent2);

			/*
			 * Intent intent2=new Intent(Intent.ACTION_SEND);
			 * intent2.setType("image/*");
			 * intent2.putExtra(Intent.EXTRA_SUBJECT, "分享");
			 * intent2.putExtra(Intent.EXTRA_TEXT, "终于可以了!!!");
			 * intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * startActivity(Intent.createChooser(intent2, getTitle()));
			 */
			// shareMsg("分享", "试试分享功能", "分享吧", "mnt/sdcard/1.jpg");
			break;

		default:
			break;
		}
	}
	
	
	

	private void login() {
		/*
		 * if ((!validateAccount()) || (!validatePwd())) { return; }
		 */

		new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showLoadingDialog("正在登录,请稍后...");
			}

			@Override
			protected String doInBackground(Void... params) {

				Map<String, String> param =setParams();
				return HttpRequest.submitPostData(PreferenceConstants.TONIGHT_SERVER,
						param, "UTF-8");
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				dismissLoadingDialog();
				/*
				 * if (result) { Intent intent = new Intent(LoginActivity.this,
				 * MainActivity.class); startActivity(intent); finish(); } else
				 * { showCustomToast("账号或密码错误,请检查是否输入正确"); }
				 */
				System.out.println("用户登录结果：      " +result);
				showCustomToast(result);
				boolean success=dealResult(result);
				if(success){
					JsonResolveUtils.resolveuserLogin(result);
					Intent intent = new Intent(LoginActivity.this, MainActivity.class); 
					startActivity(intent);
					finish();
				}else{
					showCustomToast("账号或密码错误,请检查是否输入正确");
				}
			}
		}.execute();
	}
	
	
	private Map<String, String> setParams(){
		Map<String, String> map = new HashMap<String, String>();
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put("id", 1000003);
		parms.put("password", "9d2b201382a3a8cf1342c1be422594d5");
		ArrayList<Object> arry = new ArrayList<Object>();
		arry.add(0, "userLogin");
		arry.add(1, 0);
		arry.add(2, parms);
		String data0 = RC4Utils.RC4("mdwi5uh2p41nd4ae23qy4",
				JsonUtils.list2json(arry));

		System.out.println("RC4加密后：   " + data0);
		
		String encoded1 = "";
		try {
			encoded1 = new String(Base64Utils.encode(
					data0.getBytes("ISO-8859-1"), 0, data0.length()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("base64编码后：     " + encoded1);
		String decode = "";
		try {
			if(!encoded1.equals("")){
				decode = new String(
						Base64.decode(encoded1, Base64.DEFAULT),
						"ISO-8859-1");
			}		
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("base64解码后：" + decode);

		/*try {
			String data8 = new String(Base64.decode(
					encoded1.getBytes(), Base64.DEFAULT), "ISO-8859-1");
			System.out.println("base64解码后：" + data8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		// String data7=HloveyRC4(decode,"mdwi5uh2p41nd4ae23qy4");
		String data7 = RC4Utils.RC4("mdwi5uh2p41nd4ae23qy4", decode);
		System.out.println("RC4解密后：    " +data7);
		
		map.put("d", encoded1);
		
		return map;
		
	}
	
	
	private boolean dealResult(String result){
		
		boolean success=JsonResolveUtils.resolveuserResult(result);
		
		System.out.println("结果    " +success);
		
		return success;
	}

	private boolean matchEmail(String text) {
		if (Pattern.compile("\\w[\\w.-]*@[\\w.]+\\.\\w+").matcher(text)
				.matches()) {
			return true;
		}
		return false;
	}

	private boolean matchPhone(String text) {
		if (Pattern.compile("(\\d{11})|(\\+\\d{3,})").matcher(text).matches()) {
			return true;
		}
		return false;
	}

	private boolean matchMoMo(String text) {
		if (Pattern.compile("\\d{7,9}").matcher(text).matches()) {
			return true;
		}
		return false;
	}

	private boolean isNull(EditText editText) {
		String text = editText.getText().toString().trim();
		if (text != null && text.length() > 0) {
			return false;
		}
		return true;
	}

	private boolean validateAccount() {
		mAccount = null;
		if (isNull(mAccountEt)) {
			showCustomToast("请输入您的手机号码");
			mAccountEt.requestFocus();
			return false;
		}
		String account = mAccountEt.getText().toString().trim();
		if (matchPhone(account)) {
			if (account.length() < 3) {
				showCustomToast("账号格式不正确");
				mAccountEt.requestFocus();
				return false;
			}
			if (Pattern.compile("(\\d{3,})|(\\+\\d{3,})").matcher(account)
					.matches()) {
				mAccount = account;
				return true;
			}
		}
		/*
		 * if (matchEmail(account)) { mAccount = account; return true; }
		 */
		/*
		 * if (matchMoMo(account)) { mAccount = account; return true; }
		 */
		showCustomToast("账号格式不正确");
		mAccountEt.requestFocus();
		return false;
	}

	private boolean validatePwd() {
		mPassword = null;
		String pwd = mPasswordEt.getText().toString().trim();
		if (pwd.length() < 4) {
			showCustomToast("密码不能小于4位");
			mPasswordEt.requestFocus();
			return false;
		}
		/*
		 * if (pwd.length() > 16) { showCustomToast("密码不能大于16位");
		 * mPasswordEt.requestFocus(); return false; }
		 */
		mPassword = MD5Utils.Md5(pwd);
		return true;
	}

	public void shareMsg(String activityTitle, String msgTitle, String msgText,
			String imgPath) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/jpg");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}
		intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(intent, activityTitle));
	}

	

	private void save2Preferences() {
		PreferenceUtils.setPrefString(this, PreferenceConstants.ACCOUNT,
				mAccount);
		PreferenceUtils.setPrefString(this, PreferenceConstants.PASSWORD,
				mPassword);

	}

}
