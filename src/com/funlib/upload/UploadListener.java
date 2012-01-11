package com.funlib.upload;

/**
 * 上传监听
 * @author taojianli
 *
 */
public interface UploadListener {
	
	/**
	 * 
	 * @param resultCode 见UploadResult
	 * @param obj 服务器返回值
	 */
	public void onUploadFinish(int resultCode, Object obj);
}