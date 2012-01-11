package com.funlib.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

/**
 * 网络设置
 * 
 * @author taojianli
 * 
 */
public class NetWork {

	private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	private static final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");

	private static ArrayList<APNModel> apnArrayList = new ArrayList<APNModel>();

	// 设置当前接入点
//	private static String DEFAULT_APN = "ctnet";
//	private static String DEFAULT_APN1 = "#777";

	public static boolean setDefaultAPN(Activity a) {

//		enumAPNList(a);
		APNModel apnModel = getDefaultAPN_Imp(a);
		return setDefaultAPN_Imp(a , apnModel._id);

//		boolean ret = true;
//		if (apnModel.apn.equals(DEFAULT_APN) == false) {
//
//			int i = 0;
//			for (; i < apnArrayList.size(); ++i) {
//
//				APNModel tmpAPN = apnArrayList.get(i);
//				if (tmpAPN.apn.equals(DEFAULT_APN) ||
//						tmpAPN.apn.equals(DEFAULT_APN1)) {
//					break;
//				}
//			}
//			if (i >= apnArrayList.size()) {
//
//				// 没有找到ctnet，新建ctnet apn
//				int id = addCTNetAPN(a);
//				ret = setDefaultAPN_Imp(a , String.valueOf(id));
//			} else {
//				
//				ret = setDefaultAPN_Imp(a, apnArrayList.get(i)._id);
//				try {
//					Thread.sleep(6000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//
//		return ret;
	}

	// 枚举所有接入点
	public static void enumAPNList(Activity a) {

		// Cursor cursor_need = a.getContentResolver().query(APN_TABLE_URI,null,
		// "apn = \'ctwap\' and current = 1", null, null);
		Cursor cursor_need = a.getContentResolver().query(APN_TABLE_URI, null,
				"current = 1", null, null);
		if (cursor_need != null) {
			while (cursor_need.moveToNext()) {

				APNModel apnModel = new NetWork().new APNModel();

				apnModel.proxy = cursor_need.getString(cursor_need
						.getColumnIndex("proxy"));
				apnModel.apn = cursor_need.getString(cursor_need
						.getColumnIndex("apn"));
				apnModel.port = cursor_need.getString(cursor_need
						.getColumnIndex("port"));
				apnModel.current = cursor_need.getString(cursor_need
						.getColumnIndex("current"));
				apnModel._id = cursor_need.getString(cursor_need
						.getColumnIndex("_id"));
				apnArrayList.add(apnModel);
			}
		}

	}

	// 获取当前apn属性
	public static APNModel getDefaultAPN_Imp(Activity a) {

		APNModel apnModel = new NetWork().new APNModel();
		Cursor cursor_current = a.getContentResolver().query(PREFERRED_APN_URI,
				null, null, null, null);
		if (cursor_current.moveToFirst()) {
			apnModel.proxy = cursor_current.getString(cursor_current
					.getColumnIndex("proxy"));
			apnModel.apn = cursor_current.getString(cursor_current
					.getColumnIndex("apn"));
			apnModel.port = cursor_current.getString(cursor_current
					.getColumnIndex("port"));
			apnModel.current = cursor_current.getString(cursor_current
					.getColumnIndex("current"));
			apnModel._id = cursor_current.getString(cursor_current
					.getColumnIndex("_id"));

			return apnModel;
		}

		return null;
	}

	// 设置缺省的apn
	public static boolean setDefaultAPN_Imp(Activity a, String id) {

		boolean res = false;
		ContentResolver resolver = a.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("apn_id", id);
		try {
			resolver.update(PREFERRED_APN_URI, values, null, null);
			Cursor c = resolver.query(PREFERRED_APN_URI, new String[] { "name",
					"apn" }, "_id=" + id, null, null);
			if (c != null) {
				res = true;
				c.close();
			}
		} catch (SQLException e) {

			res = false;
		}
		return res;
	}

	//新建ctnet apn
	private static int addCTNetAPN(Activity a) {
		ContentResolver cr = a.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put("name", "ctnet");
		cv.put("apn", "ctnet");
		cv.put("proxy", "10.0.0.200");
		cv.put("port", "80");
		cv.put("current", 1);

		Cursor c = null;
		try {
			Uri newRow = cr.insert(APN_TABLE_URI, cv);
			if (newRow != null) {
				c = cr.query(newRow, null, null, null, null);
				c.moveToFirst();
				String id = c.getString(c.getColumnIndex("_id"));
				return Integer.parseInt(id);// 返回新创建的cmwap接入点的id
			}
		} catch (SQLException e) {
		}

		if (c != null)
			c.close();
		return 0;
	}

	public class APNModel {

		public String _id;
		public String proxy;
		public String port;
		public String current;
		public String apn;
	}

	/**
	 * is wifi connected
	 * @param a
	 * @return
	 */
	public static boolean isWifiConnected(Activity a) {

		if (a == null)
			return false;

		Context context = a.getApplicationContext();// 获取应用上下文
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
		if (activeNetInfo == null)
			return false;
		if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}

		return false;
	}
	
	/**
	 * is 3g connected
	 * @param a
	 * @return
	 */
	public static boolean is3GConnected(Activity a) {

		if (a == null)
			return false;

		Context context = a.getApplicationContext();// 获取应用上下文
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
		if (activeNetInfo == null)
			return false;
		if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}

		return false;
	}
	
	
	/**
	 * teset network from www.baidu.com in 5 minutes
	 * @return
	 */
	public static boolean testNetIn5(){
		
		try {
			URL url = new URL("http://www.baidu.com");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			int responseCode = conn.getResponseCode();
			conn.disconnect();
			
			if(responseCode == 200)
				return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
