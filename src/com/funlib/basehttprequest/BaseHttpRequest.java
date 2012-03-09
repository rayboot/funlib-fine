package com.funlib.basehttprequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import com.funlib.network.NetWork;

import android.content.Context;


/**
 * 采用同步的方式提供基本的http请求方式，不建议直接使用
 * @author taojianli
 *
 */
public class BaseHttpRequest {

	private int mReadTimeout 			= 	2000;		/** 读取超时时间默认值 */
	private int mConnectionTimeout 		= 	2000;		/** 连接超时时间默认值 */

	private Context mContext;
	private HttpPost mHttpPost = new HttpPost();
	
	public BaseHttpRequest(Context context){
		
		mContext = context;
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
	 * 设置HttpPost头参数
	 * @param name
	 * @param value
	 */
	public void setHeaderParam(String name , String value){
		
		if(mHttpPost != null){
			
			mHttpPost.setHeader(name, value);
		}
	}
	
	/**
	 * 以同步方式发起httppost请求
	 * @param url
	 * @param params
	 * @return
	 */
	public HttpResponse request(String url , List<NameValuePair> params){
		
		try{
			
			mHttpPost.setURI(URI.create(url));
			if(params != null && params.size() > 0){
				
				HttpEntity httpEntity = new UrlEncodedFormEntity(params, "UTF-8");
				mHttpPost.setEntity(httpEntity);
			}
			HttpClient httpClient = new DefaultHttpClient();
			if(NetWork.isDefaultWap(mContext)){
	        	
	        	HttpHost proxy = new HttpHost(NetWork.getDefaultWapProxy(), NetWork.getDefaultWapPort());
	        	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  
	        }
			httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, mReadTimeout);
			httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, mConnectionTimeout);
			HttpResponse httpResponse = httpClient.execute(mHttpPost);
			
			return httpResponse;
			
		}catch(UnsupportedEncodingException exception){
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		
		return null;
	}
	
	/**
	 * 强制终止请求
	 */
	public void cancel(){
		
		if(mHttpPost != null){
			
			mHttpPost.abort();
		}
	}
}
