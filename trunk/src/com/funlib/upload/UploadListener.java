package com.funlib.upload;

/**
 * 上传监听
 * @author taojianli
 *
 */
public interface UploadListener {
	
	/**
	 * 
	 * @param statusCode
	 * @param percent
	 * @param obj
	 */
	public void onUploadStatusChanged(int statusCode , int percent , Object obj);
}