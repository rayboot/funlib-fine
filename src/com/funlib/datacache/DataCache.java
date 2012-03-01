package com.funlib.datacache;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.http.Header;
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
import com.funlib.file.FileUtily;
import com.funlib.utily.Utily;

public class DataCache implements Runnable{

	private int mReadTimeout 			= 	3000;		/** 读取超时时间默认值 */
	private int mConnectionTimeout 		= 	3000;		/** 连接超时时间默认值 */
	private int mFailRetryCount			=	3;			/** 失败重试次数 */
	
	private Context mContext;
	private Handler mHandler;
	private BaseHttpRequest mBaseHttpRequest;
	private DataCacheListener mDataCacheListener;
	private String mRequestUrl;
	private List<NameValuePair> mRequestParams;
	private int mListenerID;
	
	private int mNowRetryCount;
	private boolean bCanceled;
	
	private boolean bForceFromNet;	/** 强制从网络获取新数据 */
	
	/**
	 * 
	 */
	public DataCache(){
		
		bForceFromNet = false;
		mHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				String result = (String)msg.obj;
                if(mDataCacheListener != null){
                	mDataCacheListener.getDataFinished(msg.what, mListenerID, result, DataCache.this);
                }
			}
		};
	}
	
	/**
	 * 获取数据对应的hashcode
	 * @return
	 */
	private static String hashString(String dataUrl){
		
		return String.valueOf(dataUrl.hashCode()) + ".txt";
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
	public void request(Context context, DataCacheListener listener, int listenerId,
			String requestUrl , List<NameValuePair> params) {
		
		this.mContext = context;
		this.mDataCacheListener = listener;
		this.mRequestParams = params;
		this.mRequestUrl = requestUrl;
		this.mListenerID = listenerId;
		if(this.mRequestParams == null)
			this.mRequestParams = new Vector<NameValuePair>();
		mRequestParams.add(new BasicNameValuePair("IMSIID", Utily.getDeviceIMSI()));
		
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
	
	/**
	 * 强制从网络获取数据
	 * @param force
	 */
	public void setForceFromNet(boolean force){
		
		this.bForceFromNet = force;
	}
	
	/**
	 * 在本地缓存文件中查找
	 * @param dataUrl
	 * @return
	 */
	private DataCacheModel lookupInFiles(String dataUrl){
		
		return (DataCacheModel) FileUtily.getObject(FileUtily.getAppSDPath() + "/" + hashString(dataUrl));
	}
	
	/**
	 * 存储缓存到本地
	 * @param model
	 * @param dataUrl
	 */
	private void storeDataCahe(DataCacheModel model , String dataUrl){
		
		FileUtily.saveObject(FileUtily.getAppSDPath() + "/" + hashString(mRequestUrl), model);
	}
	
	/**
	 * 清除所有缓存
	 */
	public static void clearCache(Context context){

	}
	
	/**
	 * 检查是否需要更新
	 * @param dcm
	 * @return
	 */
	private boolean checkNeedUpdate(DataCacheModel dcm){
		
		BaseHttpRequest brBaseHttpRequest = new BaseHttpRequest(mContext);
		HttpResponse response = brBaseHttpRequest.request(mRequestUrl, mRequestParams);
		if(response != null){
			
			String newHeader = parserCacheHeader(response);
			if(TextUtils.isEmpty(newHeader) == false){
				
				if(dcm.cacheHeader.equals(newHeader) == false)
					return true;
				else
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 从http头里解析处最长有效时间
	 * @param response
	 * @return
	 */
	private long parserMaxAvailableTime(HttpResponse response){
		
		String str = null;
		Header[] tmpCacheControl = response.getHeaders("Cache-Control");
		for(int i = 0 ; i < tmpCacheControl.length ; ++i){
		    
		    Header header = tmpCacheControl[i];
		    String tmp = header.getValue();
		    if(tmp.contains("max-age")){
		        str = tmp;
		        break;
		    }
		}
		
		long time = 0;
		if(str != null){
			
			time = Long.parseLong(str);
		}
			
		return time;
	}
	
	/**
	 * 从http头里解析处上次修改时间
	 * @param response
	 * @return
	 */
	private long parserLastModifiedTime(HttpResponse response){
	
		Header lastModifiedTime = response
				.getFirstHeader("Last-Modified");
		if (lastModifiedTime != null){
		    return Date.parse(lastModifiedTime.getValue());
		}
		
		return 0;
	}

	/**
	 * 解析处cache-header字段
	 * @param response
	 * @return
	 */
	private String parserCacheHeader(HttpResponse response){
		
		String header = "";
		Header cacheHeader = response.getFirstHeader("cache-header");
		if(cacheHeader != null)
			header = cacheHeader.getValue();
		
		return header;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		bForceFromNet = true;//fixme 强制从net
		
		DataCacheModel ret = null;
		if(bForceFromNet == false){
			
			ret = lookupInFiles(mRequestUrl);
			if(ret != null){
				
				if(checkNeedUpdate(ret) == false){
					
					Message msg = Message.obtain();
					msg.what = DataCacheError.SUCCESS;
					msg.obj = ret.content;
					mHandler.sendMessage(msg);
					
					return;
				}
			}
		}
		
		mBaseHttpRequest = new BaseHttpRequest(mContext);
		mBaseHttpRequest.setConnectionTimeout(mConnectionTimeout);
		mBaseHttpRequest.setReadTimeout(mReadTimeout);
		
		bCanceled = false;
		mNowRetryCount = 0;
		HttpResponse response = null;
		do {
			
//			if(ret != null)
//				mBaseHttpRequest.setHeaderParam("Last-Modified", new Date(ret.lastModifiedTime).toGMTString());
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
			
			msg.what = DataCacheError.CANCELED;
			msg.obj = null;
		}else{
			
			if(response == null || 
					response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
				
				msg.what = DataCacheError.FAIL;
				msg.obj = null;
			}else{
				
				msg.what = DataCacheError.SUCCESS;
				
				int responseCode = response.getStatusLine().getStatusCode();
				if(responseCode == 304){
					
					msg.obj = ret.content;
				}else{
					
					try {
						msg.obj = EntityUtils.toString(response.getEntity(),"UTF-8");
						
						ret = new DataCacheModel();
						ret.content = (String)msg.obj;
//						ret.lastModifiedTime = parserLastModifiedTime(response);
//						ret.maxAvaiableTime = parserMaxAvailableTime(response);
//						ret.saveTime = new Date().getTime();
						ret.cacheHeader = parserCacheHeader(response);
						
						storeDataCahe(ret, mRequestUrl);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						msg.what = DataCacheError.FAIL;
						msg.obj = null;
					}
				}
				
			}
		}
		
		mHandler.sendMessage(msg);
		
	}
	
}
