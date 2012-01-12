package com.funlib.imagecache;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.view.View;
import com.funlib.basehttprequest.BaseHttpRequest;

/**
 * 图片缓存
 * 
 * 图片查找步骤：
 * 1，从内存中查找
 * 2，从本地文件中查找
 * 3，从网络获取
 * 
 * @author 陶建立
 *
 */
public class ImageCache implements Runnable{

	private final static String FILE_PREFIX = "image-format";		/** 图片文件名前缀 */
	
	private int mReadTimeout 			= 	5000;		/** 读取超时时间默认值 */
	private int mConnectionTimeout 		= 	5000;		/** 连接超时时间默认值 */
	
	private Context mContext;
	private Handler mHandler;
	private BaseHttpRequest mBaseHttpRequest;
	private ImageCacheListener mImageCacheListener;
	private Object mTarget;
	private String mImageUrl;
	private List<NameValuePair> mRequestParams;

	private static HashMap<String , SoftReference<Bitmap>> sBitampPools = new HashMap<String , SoftReference<Bitmap>>();
	
	/**
	 * 获取图片对应的hashcode
	 * @return
	 */
	private static String hashString(String imgUrl){
		
		return FILE_PREFIX + String.valueOf(imgUrl.hashCode());
	}
	
	/**
	 * 添加图片到内存缓存
	 * @param imgUrl
	 * @param bitmap
	 */
	private static void addBitmap(String imgUrl , Bitmap bitmap){
        
        if(sBitampPools == null){
            
            sBitampPools = new HashMap<String , SoftReference<Bitmap>>();
        }
        
        sBitampPools.put(hashString(imgUrl) , new SoftReference<Bitmap>(bitmap));
    }
	
	/**
	 * 从内存缓存读取图片
	 * @param imgUrl
	 * @return
	 */
	private static Bitmap getBitmap(String imgUrl){
        
        if(sBitampPools == null)
            return null;

        SoftReference<Bitmap> sr = sBitampPools.get(hashString(imgUrl));
        if(sr == null)
            return null;
        
        return sr.get();
    }
	
	/**
	 * 清除所有缓存
	 */
	public static void clearCache(Context context){

		if(sBitampPools != null){
			
			sBitampPools.clear();
		}
		
		String[] files = context.fileList();
		if(files != null && files.length > 0){
			
			int size = files.length;
			for(int i = 0 ; i < size ; ++i){

				final String fileName = files[i];
				if(fileName.startsWith(FILE_PREFIX)){
					context.deleteFile(fileName);
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public ImageCache(){
		
		mHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == ImageCacheError.SUCCESS) {

					if (mTarget != null && msg.obj != null) {
						
						if(mTarget instanceof View){
							
							Bitmap bmp = (Bitmap)msg.obj;
							Drawable drawable = new BitmapDrawable(bmp);
							
							View view = (View)mTarget;
							view.setBackgroundDrawable(drawable);
						}
					}

				}

				if(mImageCacheListener != null){
					mImageCacheListener.getImageFinished(msg.what, mTarget, (Bitmap) msg.obj , ImageCache.this);
				}
			}
		};
	}
	
	/**
	 * 设置读操作超时时间，默认5s
	 * @param timeout
	 */
	public void setReadTimeout(int timeout){
		
		mReadTimeout = timeout;
	}
	
	/**
	 * 设置链接操作超时时间，默认5s
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout){
		
		mConnectionTimeout = timeout;
	}
	
	/**
	 * 设置上下文环境
	 * @param context
	 */
	public void setContext(Context context){
		
		this.mContext = context;
	}

	/**
	 * 获取缓存图片
	 * @param context
	 * @param listener
	 * @param target
	 * @param imgUrl
	 * @param params
	 */
	public void cacheImage(Context context , ImageCacheListener listener, Object target,
			String imgUrl , List<NameValuePair> params){
		
		this.mContext = context;
		this.mImageCacheListener = listener;
		this.mTarget = target;
		this.mImageUrl = imgUrl;
		this.mRequestParams = params;
		
		new Thread(this).start();
	}

	/**
	 * 缓存图片
	 * @param imgUrl
	 * @param bitmap
	 * @param bitmapBytes
	 */
	private void storeCachedBitmap(String imgUrl , Bitmap bitmap , byte[] bitmapBytes){
		
		//缓存到内存
		addBitmap(imgUrl, bitmap);
		
		//缓存到文件
		try {
			
			FileOutputStream fos = mContext.openFileOutput(hashString(imgUrl), Context.MODE_PRIVATE);
			fos.write(bitmapBytes);
			fos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 在内存中查找
	 * @return
	 */
	private Bitmap lookupInMemory(String imgUrl){
		
		return getBitmap(imgUrl);
	}
	
	/**
	 * 在缓存文件中查找
	 * @return
	 */
	private Bitmap lookupInFiles(String imgUrl){
		
		final String hashString = hashString(imgUrl);
		try {
			
			FileInputStream file = mContext.openFileInput(hashString);
			return BitmapFactory.decodeStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * 从网络获取
	 * @param imgUrl
	 * @return
	 */
	private byte[] featchBitmap(String imgUrl){
		
		mBaseHttpRequest = new BaseHttpRequest(mContext);
		mBaseHttpRequest.setConnectionTimeout(mConnectionTimeout);
		mBaseHttpRequest.setReadTimeout(mReadTimeout);
		HttpResponse response = mBaseHttpRequest.request(mImageUrl, mRequestParams);
		if(response == null)
			return null;
		
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
			
			try {
				
				return EntityUtils.toByteArray(response.getEntity());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * 图片查找步骤：
	 * 1，从内存中查找
	 * 2，从本地文件中查找
	 * 3，从网络获取
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub

		Bitmap bmp = null;
		final String imgUrl = mImageUrl;
		
		bmp = lookupInMemory(imgUrl);
		if(bmp == null){
		
			bmp = lookupInFiles(imgUrl);
			if(bmp == null){
				
				byte[] bmpBytes = featchBitmap(imgUrl);
				if(bmpBytes != null){
					
					bmp = BitmapFactory.decodeByteArray(bmpBytes, 0, bmpBytes.length);
					//存储图片
					storeCachedBitmap(imgUrl , bmp , bmpBytes);
				}
			}
		}
		
		Message msg = Message.obtain();
		if(bmp == null)
			msg.what = ImageCacheError.FAIL;
		else
			msg.what = ImageCacheError.SUCCESS;
		msg.obj = bmp;
		mHandler.sendMessage(msg);
	}
	
}
