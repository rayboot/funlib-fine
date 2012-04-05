package com.funlib.basehttprequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.text.TextUtils;

import com.funlib.network.NetWork;

/**
 * 采用同步的方式提供基本的http请求方式，不建议直接使用
 * 
 * @author taojianli
 * 
 */
public class BaseHttpRequest {

	private int mReadTimeout;
	/** 读取超时时间默认值 */
	private int mConnectionTimeout;
	/** 连接超时时间默认值 */

	private Context mContext;
	private HttpPost mHttpPost = new HttpPost();
	// set timeout parameters for HttpClient
	HttpParams httpParameters;

	public static String JSESSION_ID="";
	
	public BaseHttpRequest(Context context) {

		mContext = context;
	}

	/**
	 * 设置读操作超时时间，默认5s
	 * 
	 * @param timeout
	 */
	public void setReadTimeout(int timeout) {

		mReadTimeout = timeout;
	}

	/**
	 * 设置链接操作超时时间，默认5s
	 * 
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout) {

		mConnectionTimeout = timeout;
	}

	/**
	 * 设置HttpPost头参数
	 * 
	 * @param name
	 * @param value
	 */
	public void setHeaderParam(String name, String value) {

		if (mHttpPost != null) {

			mHttpPost.addHeader(name, value);
		}
	}
	
	/**
	 * 解析处cache-header字段
	 * @param response
	 * @return
	 */
	private String parserJSESSIONID(HttpResponse response){
		
		String header = "";
		if(response != null){
			
			Header cacheHeader = response.getFirstHeader("sessionid");
			if(cacheHeader != null)
				header = cacheHeader.getValue();
		}
		
		return header;
	}

	/**
	 * 以同步方式发起httppost请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public HttpResponse request(String url, List<NameValuePair> params) {

		if(url != null){
			
			if(TextUtils.isEmpty(JSESSION_ID) == false){
				
				String[] s = url.split("\\?");
				if (s != null && s.length >= 2) {

					url = s[0] + ";jsessionid=" + JSESSION_ID + "?";
					url += s[1];
				}else{
					
					url += ";jsessionid=" + JSESSION_ID + "?";
				}
			}
		}
		
		try {
			
			httpParameters = new BasicHttpParams();
			mHttpPost.setURI(URI.create(url));
			if (params != null && params.size() > 0) {

				HttpEntity httpEntity = new UrlEncodedFormEntity(params,
						"UTF-8");
				mHttpPost.setEntity(httpEntity);
			}
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			if (NetWork.isDefaultWap(mContext)) {

				HttpHost proxy = new HttpHost(NetWork.getDefaultWapProxy(),
						NetWork.getDefaultWapPort());
				httpParameters.setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
				
			}
			httpParameters.setIntParameter(
					HttpConnectionParams.SO_TIMEOUT, mReadTimeout);
			httpParameters
					.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
							mConnectionTimeout);
			HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);// setting
			// setSocketBufferSize
			httpClient.setParams(httpParameters);

			HttpResponse httpResponse = httpClient.execute(mHttpPost);
			String tmpSessionId = parserJSESSIONID(httpResponse);
			if(!TextUtils.isEmpty(tmpSessionId)){
				JSESSION_ID = tmpSessionId;
			}
			return httpResponse;

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		}

		return null;
	}

	/**
	 * 强制终止请求
	 */
	public void cancel() {

		if (mHttpPost != null) {

			mHttpPost.abort();
		}
	}
}
