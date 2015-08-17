package com.hangzhou.tonight.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hangzhou.tonight.LoginActivity;
import com.hangzhou.tonight.R;
import com.hangzhou.tonight.entity.NearByGroup;
import com.hangzhou.tonight.entity.NearByPeople;
import com.hangzhou.tonight.service.XXService;

public class BaseApplication extends Application {
	private Bitmap mDefaultAvatar;
	private static final String AVATAR_DIR = "avatar/";
	private static final String PHOTO_ORIGINAL_DIR = "photo/original/";
	private static final String PHOTO_THUMBNAIL_DIR = "photo/thumbnail/";
	private static final String STATUS_PHOTO_DIR = "statusphoto/";
	public Map<String, SoftReference<Bitmap>> mAvatarCache = new HashMap<String, SoftReference<Bitmap>>();
	public Map<String, SoftReference<Bitmap>> mPhotoOriginalCache = new HashMap<String, SoftReference<Bitmap>>();
	public Map<String, SoftReference<Bitmap>> mPhotoThumbnailCache = new HashMap<String, SoftReference<Bitmap>>();
	public Map<String, SoftReference<Bitmap>> mStatusPhotoCache = new HashMap<String, SoftReference<Bitmap>>();

	public List<NearByPeople> mNearByPeoples = new ArrayList<NearByPeople>();
	public List<NearByGroup> mNearByGroups = new ArrayList<NearByGroup>();

	public static List<String> mEmoticons = new ArrayList<String>();
	public static Map<String, Integer> mEmoticonsId = new HashMap<String, Integer>();
	public static List<String> mEmoticons_Zem = new ArrayList<String>();
	public static List<String> mEmoticons_Zemoji = new ArrayList<String>();

	public LocationClient mLocationClient;
	public double mLongitude;
	public double mLatitude;
	public String mCity;

	@Override
	public void onCreate() {
		super.onCreate();
		mDefaultAvatar = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_common_def_header);
	/*	for (int i = 1; i < 64; i++) {
			String emoticonsName = "[zem" + i + "]";
			int emoticonsId = getResources().getIdentifier("zem" + i,
					"drawable", getPackageName());
			mEmoticons.add(emoticonsName);
			mEmoticons_Zem.add(emoticonsName);
			mEmoticonsId.put(emoticonsName, emoticonsId);
		}
		for (int i = 1; i < 59; i++) {
			String emoticonsName = "[zemoji" + i + "]";
			int emoticonsId = getResources().getIdentifier("zemoji_e" + i,
					"drawable", getPackageName());
			mEmoticons.add(emoticonsName);
			mEmoticons_Zemoji.add(emoticonsName);
			mEmoticonsId.put(emoticonsName, emoticonsId);
		}
		
*/
		
		startService(new Intent(getApplicationContext(), XXService.class));
		
		// 获取当前用户位置
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.setAK("60b43d1a9513d904b6aa2948b27b4a20");
		/*LocationClientOption option = new LocationClientOption();

		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
*/
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceivePoi(BDLocation arg0) {

			}

			@Override
			public void onReceiveLocation(BDLocation arg0) {
				mLongitude = arg0.getLongitude();
				mLatitude = arg0.getLatitude();
				Log.i("地理位置", "经度:" + mLongitude + ",纬度:" + mLatitude);
				List<Address> addList = null;			
				Geocoder ge = new Geocoder(getApplicationContext());
				try {
					addList = ge.getFromLocation(mLatitude, mLongitude, 1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (addList != null && addList.size() > 0) {
					for (int i = 0; i < addList.size(); i++) {
						Address ad = addList.get(i);
						mCity = ad.getLocality();
					}
				}
				System.out.println("您当前的城市是:\n" + mCity);

				mLocationClient.stop();
			}
		});
		mLocationClient.start();
		mLocationClient.requestOfflineLocation();
		System.out.println("开始获取");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e("BaseApplication", "onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.e("BaseApplication", "onTerminate");
	}

	public Bitmap getAvatar(String imageName) {
		if (mAvatarCache.containsKey(imageName)) {
			Reference<Bitmap> reference = mAvatarCache.get(imageName);
			if (reference.get() == null || reference.get().isRecycled()) {
				mAvatarCache.remove(imageName);
			} else {
				return reference.get();
			}
		}
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			is = getAssets().open(AVATAR_DIR + imageName);
			bitmap = BitmapFactory.decodeStream(is);
			if (bitmap == null) {
				throw new FileNotFoundException(imageName + "is not find");
			}
			mAvatarCache.put(imageName, new SoftReference<Bitmap>(bitmap));
			return bitmap;
		} catch (Exception e) {
			return mDefaultAvatar;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {

			}
		}
	}

	public Bitmap getPhotoOriginal(String imageName) {
		if (mPhotoOriginalCache.containsKey(imageName)) {
			Reference<Bitmap> reference = mPhotoOriginalCache.get(imageName);
			if (reference.get() == null || reference.get().isRecycled()) {
				mPhotoOriginalCache.remove(imageName);
			} else {
				return reference.get();
			}
		}
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			is = getAssets().open(PHOTO_ORIGINAL_DIR + imageName);
			bitmap = BitmapFactory.decodeStream(is);
			if (bitmap == null) {
				throw new FileNotFoundException(imageName + "is not find");
			}
			mPhotoOriginalCache.put(imageName,
					new SoftReference<Bitmap>(bitmap));
			return bitmap;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {

			}
		}
	}

	public Bitmap getPhotoThumbnail(String imageName) {
		if (mPhotoThumbnailCache.containsKey(imageName)) {
			Reference<Bitmap> reference = mPhotoThumbnailCache.get(imageName);
			if (reference.get() == null || reference.get().isRecycled()) {
				mPhotoThumbnailCache.remove(imageName);
			} else {
				return reference.get();
			}
		}
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			is = getAssets().open(PHOTO_THUMBNAIL_DIR + imageName);
			bitmap = BitmapFactory.decodeStream(is);
			if (bitmap == null) {
				throw new FileNotFoundException(imageName + "is not find");
			}
			mPhotoThumbnailCache.put(imageName, new SoftReference<Bitmap>(
					bitmap));
			return bitmap;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {

			}
		}
	}

	public Bitmap getStatusPhoto(String imageName) {
		if (mStatusPhotoCache.containsKey(imageName)) {
			Reference<Bitmap> reference = mStatusPhotoCache.get(imageName);
			if (reference.get() == null || reference.get().isRecycled()) {
				mStatusPhotoCache.remove(imageName);
			} else {
				return reference.get();
			}
		}
		InputStream is = null;
		Bitmap bitmap = null;
		try {
			is = getAssets().open(STATUS_PHOTO_DIR + imageName);
			bitmap = BitmapFactory.decodeStream(is);
			if (bitmap == null) {
				throw new FileNotFoundException(imageName + "is not find");
			}
			mStatusPhotoCache.put(imageName, new SoftReference<Bitmap>(bitmap));
			return bitmap;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {

			}
		}
	}
}
