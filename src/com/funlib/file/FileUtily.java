package com.funlib.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.text.TextUtils;

/**
 * 文件操作类
 * 
 * @author taojianli
 * 
 */
public class FileUtily {

	/**
	 * 获取SD卡路径
	 * @return
	 */
	public static String getSDPath() {
		
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			
			return sdDir.getAbsolutePath() + "/";
		}
		
		return null;
	}
	
	/**
	 * 获取应用在SD卡的路径
	 * @param name
	 * @return
	 */
	private static String sAppSDPath = null;
	public static String getAppSDPath(){
		
		return sAppSDPath;
	}
	
	public static boolean initAppSDPath(String name){
		
		String sdPath = getSDPath();
		if(TextUtils.isEmpty(sdPath) == false){
			
			sdPath += name;
			
			File file = new File(sdPath);
			if(file.exists() == false)
				file.mkdir();
			
			sAppSDPath = sdPath + "/";
		}
		
		return true;
	}

	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static boolean saveFileToSdcard(String filePath, byte[] data) {

		try {

			File file = new File(filePath);
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(data);
			outStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

		return false;
	}

}
