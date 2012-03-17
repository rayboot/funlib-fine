package com.funlib.businessrequest;

import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.funlib.basehttprequest.BaseHttpRequest;
import com.funlib.utily.Utily;

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
public class BusinessRequest implements Runnable{

	private int mReadTimeout 			= 	7000;		/** 读取超时时间默认值 */
	private int mConnectionTimeout 		= 	7000;		/** 连接超时时间默认值 */
	private int mFailRetryCount			=	2;			/** 失败重试次数 */
	
	private Context mContext;
	private Handler mHandler;
	private BaseHttpRequest mBaseHttpRequest;
	private BusinessRequestListener mBusinessRequestListener;
	private String mRequestUrl;
	private List<NameValuePair> mRequestParams;
	private int mListenerID;
	
	private int mNowRetryCount;
	private boolean bCanceled;
	
	/**
	 * 
	 */
	public BusinessRequest(Context context){
		
		mContext = context;
		
		mHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				String result = (String)msg.obj;
				if(TextUtils.isEmpty(result)){
					result = "";
				}
                if(mBusinessRequestListener != null)
                	mBusinessRequestListener.businessRequestDidFinish(msg.what , mListenerID , result);
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
	 * 设置超时失败后的重试次数，默认3次
	 * @param cnt
	 */
	public void setFailRetryCount(int cnt){
		
		mFailRetryCount = cnt;
	}
	
	/**
	 * 
	 * @param listener
	 * @param listenerId
	 *            如果调用端，同时发送多个也去请求，可以在调用端维护唯一id，并传递，在业务处理结束后，带回
	 * @param requestUrl
	 */
	public void request(BusinessRequestListener listener, int listenerId,
			String requestUrl , List<NameValuePair> params) {
		
		this.mBusinessRequestListener = listener;
		this.mRequestParams = params;
		this.mRequestUrl = requestUrl;
		this.mListenerID = listenerId;
		
		new Thread(this).start();
	}
	
	/**
	 * 取消请求
	 */
	public void cancel(){
		
		bCanceled = true;
		if(mBaseHttpRequest != null)
			mBaseHttpRequest.cancel();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		mBaseHttpRequest = new BaseHttpRequest(mContext);
		mBaseHttpRequest.setConnectionTimeout(mConnectionTimeout);
		mBaseHttpRequest.setReadTimeout(mReadTimeout);
		
		bCanceled = false;
		mNowRetryCount = 0;
		HttpResponse response = null;
		do {
			
			response = mBaseHttpRequest.request(mRequestUrl, mRequestParams);
			if(response != null){
				break;
			}else{
				
				++mNowRetryCount;
			}
			
			if(bCanceled == true)
				break;
			
		} while (mNowRetryCount < mFailRetryCount);
		
		Message msg = Message.obtain();
		if(bCanceled == true){
			
			msg.what = BusinessRequestError.CANCELED;
			msg.obj = null;
		}else{
			
			if(response == null || 
					response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				
				msg.what = BusinessRequestError.FAIL;
				msg.obj = null;
			}else{
				
				msg.what = BusinessRequestError.SUCCESS;
				try {
					msg.obj = EntityUtils.toString(response.getEntity(),"UTF-8");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					msg.what = BusinessRequestError.FAIL;
					msg.obj = null;
				}
			}
		}
		
		mHandler.sendMessage(msg);
		
	}
	
}
