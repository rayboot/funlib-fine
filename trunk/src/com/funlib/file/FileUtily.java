package com.funlib.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.os.Environment;

/**
 * 文件操作类
 * @author taojianli
 *
 */
public class FileUtily {


	/**
	 * 如果sdcard没有mounted，返回false
	 * @param os
	 * @return
	 */
	public static boolean saveFileToSdcard(String fileName , byte[] data){
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			
			File sdCardDir = Environment.getExternalStorageDirectory();
			File saveFile = new File(sdCardDir, fileName);
			
			try {
				
				FileOutputStream outStream = new FileOutputStream(saveFile);
				outStream.write(data);  
	            outStream.close(); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				return false;
			}
			 
		}
		
		return false;
	}
}
