package com.funlib.businessrequest;

/**
 * 业务http请求监听
 * 
 * @author taojianli
 * 
 */
public interface BusinessRequestListener {

	/**
	 * 
	 * @param errorCode
	 *            错误码
	 * @param listenerID
	 *            如果调用端，同时发送多个也去请求，可以在调用端维护唯一id，并传递，在业务处理结束后，带回
	 * @param responseString
	 */
	public void businessRequestDidFinish(int errorCode, int listenerID,
			String responseString);
}
