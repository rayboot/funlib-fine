package com.funlib.config;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 保存配置字段
 * 
 * 用法：
 * Config.init(this)，使用前必须初始化
 * 
 * 
 * @author taojianli
 * 
 */
public class Config {

	private static final String CONFIG_NAME = "con_config";
	private static Context context;

	/**
	 * 初始化Context
	 * @param con
	 */
	public static void init(Context con){
		
		context = con;
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveInt(String key, int value) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param key
	 * @return 默认-1
	 */
	public static int getInt(String key) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		return sp.getInt(key, -1);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveBoolean(String key, boolean value) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param key
	 * @return默认false
	 */
	public static boolean getBoolean(String key) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		return sp.getBoolean(key, false);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveString(String key, String value) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param key
	 * @return默认null
	 */
	public static String getString(String key) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		return sp.getString(key, null);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveFloat(String key, float value) {
		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putFloat(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param key
	 * @return 默认值-1
	 */
	public static float getFloat(String key) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		return sp.getFloat(key, -1);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void saveLong(String key, long value) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		SharedPreferences.Editor editor = sp.edit();

		editor.putLong(key, value);
		editor.commit();
	}

	/**
	 * 
	 * @param key
	 * @return 默认-1
	 */
	public static long getLong(String key) {

		SharedPreferences sp = context
				.getSharedPreferences(CONFIG_NAME, 0);
		return sp.getLong(key, -1);
	}
}
