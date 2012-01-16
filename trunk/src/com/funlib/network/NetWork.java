package com.funlib.network;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 网络设置
 * 
 * 如果是wap连接，必须要在请求前设置网络代理
 * 用法：
 * if(NetWork.isDefaultWap(CTMeetingApplication.getInstance())){
        	HttpHost proxy = new HttpHost(NetWork.getDefaultWapProxy(), NetWork.getDefaultWapPort());  
        	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  
        }
 * 
 * @author taojianli
 * 
 */
public class NetWork {
	
	public static final String CTWAP_PROXY_ENDS = "200";	//电信代理端口10.0.0.200
	public static final String CMWAP_PROXY_ENDS = "172";	//移动代理端口10.0.0.172
	public static final String CUWAP_PROXY_ENDS = "172";	//联通代理端口10.0.0.172
	
	private static final int GSM_DATA_TYPE = 0;
	private static final int CDMA_DATA_TYPE = 1;
	private static final int TD_SCDMA_DATA_TYPE = 2;
	
	public static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	public static Uri GSM_PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn2");
	public static Uri CDMA_PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	
	/**
	 * wifi
	 * @param a
	 * @return
	 */
	public static boolean isDefaultWifi(Context context) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo == null)
			return false;
		if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}

		return false;
	}
	
	/**
	 * wap
	 * @param context
	 * @return
	 */
	public static boolean isDefaultWap(Context context){
		
		if(isDefaultWifi(context))
			return false;
		
		ApnNode apnNode = getDefaultApnNode(context);
		if(apnNode != null){

			String proxy = apnNode.proxy;
			if(proxy != null && proxy.length() > 0){
				
				if(proxy.endsWith(CMWAP_PROXY_ENDS) ||
						proxy.endsWith(CTWAP_PROXY_ENDS) ||
						proxy.endsWith(CUWAP_PROXY_ENDS))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * net
	 * @param context
	 * @return
	 */
	public static boolean isDefaultNet(Context context){
		
		boolean ret = isDefaultWap(context);
		if(ret == false && (false == isDefaultWifi(context)))
			return true;
		
		return false;
	}
	
	
	/**
	 * 获取默认网络制式
	 * @param context
	 * @return
	 */
	public static int getDefaultDataNetwork(Context context){
		
//	    String str = Settings.System.getString(context.getContentResolver(), "default_data_network");
//	    if ("none".equals(str))
//	      str = Settings.System.getString(context.getContentResolver(), "saved_data_network");
//	    
//	    if(str == null){
//	    	
//	    }else if (!GSM_DATA_TYPE.equals(str))
//	      str = CDMA_DATA_TYPE;
//	    
//	    return str;
		
		int defaultNet = GSM_DATA_TYPE;
		
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		int type = tm.getPhoneType();
		if(type == TelephonyManager.PHONE_TYPE_CDMA)
			defaultNet = CDMA_DATA_TYPE;
		else
			defaultNet = GSM_DATA_TYPE;
		
		return defaultNet;
	  }
	
	/**
	 * 获取缺省的网络类型
	 * @param context
	 * @return
	 */
	public static ApnNode sDefaultApnNode = new NetWork(). new ApnNode(); 
	public static ApnNode getDefaultApnNode(Context context){

		Uri tmpUri = GSM_PREFERRED_APN_URI;
		int dataType = getDefaultDataNetwork(context);
		if(dataType == GSM_DATA_TYPE){
			tmpUri = PREFERRED_APN_URI;
		}else if(dataType == CDMA_DATA_TYPE){
			tmpUri = CDMA_PREFERRED_APN_URI;
		}else{
			tmpUri = GSM_PREFERRED_APN_URI;
		}
		
		sDefaultApnNode = new NetWork(). new ApnNode();
		ContentResolver cResolver = context.getContentResolver();  
        Cursor cr = cResolver.query(tmpUri, null, null, null, null);
        if(cr != null && cr.moveToFirst()){
        	
        	sDefaultApnNode.apnType = cr.getString(cr.getColumnIndex("user"));
        	sDefaultApnNode.proxy =  cr.getString(cr.getColumnIndex("proxy"));
        	sDefaultApnNode.port = cr.getInt(cr.getColumnIndex("port"));
        }
        
		return sDefaultApnNode;
	}
	
	/**
	 * 获取wap代理地址
	 * @return
	 */
	public static String getDefaultWapProxy(){
		
		return sDefaultApnNode.proxy;
	}
	
	/**
	 * 获取wap代理端口
	 * @return
	 */
	public static int getDefaultWapPort(){
		
		return sDefaultApnNode.port;
	}
	
	public class ApnNode{
		
		public String apnType;
		public String proxy;
		public int port;
	}
}
