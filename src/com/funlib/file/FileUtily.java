package com.funlib.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.funlib.log.FLog;

import android.content.Context;
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
	public static boolean saveBytes(String filePath, byte[] data) {

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
	
	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static boolean saveBytes(File file, byte[] data) {

		try {

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
	
	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static byte[] getBytes(String filePath) {

		try {

			File file = new File(filePath);
			FileInputStream inStream = new FileInputStream(file);
			byte bytes[] = new byte[inStream.available()];
			inStream.read(bytes);
			inStream.close();
			
			return bytes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

	}
	
	
	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static boolean saveObject(String filePath, Object object) {

		try {

			File file = new File(filePath);
			FileOutputStream outStream = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(outStream);
			oos.writeObject(object);
			oos.flush();
			oos.close();
			outStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

		return false;
	}
	
	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static boolean saveObject(File file, Object object) {

		try {

			FileOutputStream outStream = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(outStream);
			oos.writeObject(object);
			oos.flush();
			oos.close();
			outStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return false;
		}

		return false;
	}
	
	/**
	 * 如果sdcard没有mounted，返回false
	 * 
	 * @param os
	 * @return
	 */
	public static Object getObject(String filePath) {

		try {

			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object obj = ois.readObject();
			ois.close();
			fis.close();
			
			return obj;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		}

	}

	/**
	 * 创建临时文件
	 * @param fielPath
	 * @return
	 */
	public static File getTempFile(Context context){
		
		File file = null;
		try {
			
			file = File.createTempFile("tmp", null , context.getCacheDir());
			file.deleteOnExit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		return file;
	}
	
	/**
	 * 创建文件夹
	 * @param dirPath
	 * @return
	 */
	public static boolean mkDir(String dirPath){
		
		File file = new File(dirPath);
		if(file.exists() == false){
			
			return file.mkdirs();
		}
		
		return true;
	}
}
