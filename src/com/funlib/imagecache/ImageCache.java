package com.funlib.imagecache;

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
import android.os.Handler;
import android.os.Message;
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

		FileUtily.saveBytes(
				FileUtily.getAppSDPath() + hashString(imgUrl),
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

			String filePath = FileUtily.getAppSDPath() + "/"
					+ hashString(imgUrl);
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

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == ImageCacheError.SUCCESS) {

					if (target != null && msg.obj != null) {

						if (target instanceof ImageView) {

							Bitmap bmp = (Bitmap) msg.obj;
							ImageView iv = (ImageView) target;
							iv.setImageBitmap(bmp);
						} else if (target instanceof ImageButton) {

							Bitmap bmp = (Bitmap) msg.obj;
							ImageButton iv = (ImageButton) target;
							iv.setImageBitmap(bmp);
						} else if (target instanceof View) {

							Bitmap bmp = (Bitmap) msg.obj;
							Drawable drawable = new BitmapDrawable(bmp);
							//
							View view = (View) target;
							view.setBackgroundDrawable(drawable);
						}
					}

				}

				if (listener != null) {
					listener.getImageFinished(msg.what, target,
							(Bitmap) msg.obj, ImageCache.this);
				}
			}
		};
		new Thread() {

			public void run() {

				byte[] bmpBytes = featchBitmap(imgUrl);
				Bitmap bmp = null;
				if (bmpBytes != null) {

					try {

						bmp = BitmapFactory.decodeByteArray(bmpBytes, 0,
								bmpBytes.length);

						// 存储图片
						storeCachedBitmap(imgUrl, bmp, bmpBytes);
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
					}
				}
				
				Message msg = Message.obtain();
				msg.what = ImageCacheError.SUCCESS;
				if(bmp == null){
					msg.what = ImageCacheError.FAIL;
				}
				msg.obj = bmp;
				handler.sendMessage(msg);
			}
		}.start();

		return null;
	}
}
