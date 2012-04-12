package com.funlib.imagecache;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.funlib.basehttprequest.BaseHttpRequest;
import com.funlib.file.FileUtily;

/**
 * 图片缓存
 * 
 * 图片查找步骤： 1，从内存中查找 2，从本地文件中查找 3，从网络获取
 * 
 * @author 陶建立
 * 
 */
public class ImageCache {

	private int mReadTimeout = 10000;
	/** 读取超时时间默认值 */
	private int mConnectionTimeout = 10000;
	/** 连接超时时间默认值 */

	private Context mContext;
	private static HashMap<String, SoftReference<Bitmap>> sBitampPools = new HashMap<String, SoftReference<Bitmap>>();

	private static String sImageCachePath = "";
	static{
		
		String appPath = FileUtily.getAppSDPath();
		if(appPath != null){
			
			sImageCachePath = appPath + File.separator + "imgcache" + File.separator;
			FileUtily.mkDir(sImageCachePath);
		}
	}
	
	/**
	 * 获取图片对应的hashcode
	 * 
	 * @return
	 */
	private static String hashString(String imgUrl) {

		if (TextUtils.isEmpty(imgUrl) == true) {

			imgUrl = String.valueOf(System.currentTimeMillis());
		}

		return imgUrl.hashCode()+".pic";
	}

	/**
	 * 添加图片到内存缓存
	 * 
	 * @param imgUrl
	 * @param bitmap
	 */
	private static void addBitmap(String imgUrl, Bitmap bitmap) {

		if (sBitampPools == null) {

			sBitampPools = new HashMap<String, SoftReference<Bitmap>>();
		}

		sBitampPools.put(hashString(imgUrl), new SoftReference<Bitmap>(bitmap));
	}

	/**
	 * 从内存缓存读取图片
	 * 
	 * @param imgUrl
	 * @return
	 */
	private static Bitmap getBitmap(String imgUrl) {

		if (sBitampPools == null)
			return null;

		SoftReference<Bitmap> sr = sBitampPools.get(hashString(imgUrl));
		if (sr == null)
			return null;

		return sr.get();
	}

	/**
	 * 清除所有缓存
	 */
	public static void clearCache(Context context) {

		if (sBitampPools != null) {

			sBitampPools.clear();
		}

	}

	/**
	 * 设置读操作超时时间，默认3s
	 * 
	 * @param timeout
	 */
	public void setReadTimeout(int timeout) {

		mReadTimeout = timeout;
	}

	/**
	 * 设置链接操作超时时间，默认3s
	 * 
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout) {

		mConnectionTimeout = timeout;
	}

	/**
	 * 设置上下文环境
	 * 
	 * @param context
	 */
	public void setContext(Context context) {

		this.mContext = context;
	}

	/**
	 * 缓存图片
	 * 
	 * @param imgUrl
	 * @param bitmap
	 * @param bitmapBytes
	 */
	private void storeCachedBitmap(String imgUrl, Bitmap bitmap,
			byte[] bitmapBytes) {

		// 缓存到内存
		addBitmap(imgUrl, bitmap);

		FileUtily.saveBytes(sImageCachePath + hashString(imgUrl),
				bitmapBytes);

	}

	/**
	 * 在内存中查找
	 * 
	 * @return
	 */
	private Bitmap lookupInMemory(String imgUrl) {

		return getBitmap(imgUrl);
	}

	/**
	 * 在缓存文件中查找
	 * 
	 * @return
	 */
	private Bitmap lookupInFiles(String imgUrl) {

		try {

			String filePath = sImageCachePath + hashString(imgUrl);
			byte[] bytes = FileUtily.getBytes(filePath);
			return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (OutOfMemoryError e) {

		} catch (Exception e) {
			// TODO: handle exception
		}

		return null;
	}

	/**
	 * 从网络获取
	 * 
	 * @param imgUrl
	 * @return
	 */
	private byte[] featchBitmap(String imgUrl) {

		try {
			BaseHttpRequest baseHttpRequest = new BaseHttpRequest(mContext);
			baseHttpRequest.setConnectionTimeout(mConnectionTimeout);
			baseHttpRequest.setReadTimeout(mReadTimeout);
			HttpResponse response = baseHttpRequest.request(imgUrl, null);
			if (response == null)
				return null;

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				return EntityUtils.toByteArray(response.getEntity());
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取缓存图片
	 * 
	 * @param context
	 * @param listener
	 * @param target
	 * @param imgUrl
	 * @param params
	 */
	public Bitmap cacheImage(Context context,
			final ImageCacheListener listener, final Object target,
			final String imgUrl, final List<NameValuePair> params) {

		this.mContext = context;

		Bitmap resultBmp = null;
		// find memory
		resultBmp = lookupInMemory(imgUrl);
		if (resultBmp != null)
			return resultBmp;

		// find files
		resultBmp = lookupInFiles(imgUrl);
		if (resultBmp != null)
			return resultBmp;
		try {
			
			new ImageCacheTask().execute(listener , target , imgUrl , params);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}
	
	private class ImageCacheTask extends AsyncTask<Object,Integer,Bitmap>{

		private ImageCacheListener listener;
		private Object target;
		private String imgUrl;
		private List<NameValuePair> requestParams;
		
		@Override
		protected Bitmap doInBackground(Object... params) {
			// TODO Auto-generated method stub
			
			listener = (ImageCacheListener) params[0];
			target = params[1];
			imgUrl = (String)params[2];
			requestParams = (List<NameValuePair>) params[3];
			
			Bitmap resultBmp = null;
			byte[] bmpBytes = featchBitmap(imgUrl);
			if (bmpBytes != null) {

				try {

					resultBmp = BitmapFactory.decodeByteArray(bmpBytes, 0,bmpBytes.length);
					// 存储图片
					storeCachedBitmap(imgUrl, resultBmp, bmpBytes);
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
				}
			}
			bmpBytes = null;
			return resultBmp;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// TODO Auto-generated method stub
			super.onPostExecute(bitmap);
			
			
			if (target != null && bitmap != null) {

				if(target instanceof View){
					
					View view = (View) target;
					if(imgUrl.equals(view.getTag())){
						
						if (target instanceof ImageView) {

							ImageView iv = (ImageView) target;
							iv.setImageBitmap(bitmap);
						} else if (target instanceof ImageButton) {

							ImageButton iv = (ImageButton) target;
							iv.setImageBitmap(bitmap);
						} else if (target instanceof View) {

							Drawable drawable = new BitmapDrawable(bitmap);
							//
							view.setBackgroundDrawable(drawable);
						}
					}
				}
				
			}
			
			int errorCode = ImageCacheError.SUCCESS;
			if(bitmap == null){
				errorCode = ImageCacheError.FAIL;
			}
			if (listener != null) {
				listener.getImageFinished(errorCode, target , bitmap , ImageCache.this);
			}
		}
		
	}
}
