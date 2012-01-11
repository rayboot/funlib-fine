package com.funlib.upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Handler;
import android.os.Message;

/**
 * 只用于上传组建
 * 
 * @author taojianli
 *
 */
public class FileUploader implements Runnable {
	
	private InputStream mFileInputStream;
	private UploadListener mUploadlistener;
	private String mServerUrl;
	private HttpURLConnection mHttpConn;

	public FileUploader(UploadListener lis) {
		
		mUploadlistener = lis;
	}
	
	/**
	 * 
	 * @param serverUrl 目标服务器地址
	 * @param is 待上传的文件流
	 */
	public void upLoadFile(String serverUrl, InputStream is) {

		this.mServerUrl = serverUrl;
		this.mFileInputStream = is;
		new Thread(this).start();
	}

	/**
	 * 取消上传
	 */
	public void cancel(){
		
		if(mHttpConn != null){
			mHttpConn.disconnect();
			mHttpConn = null;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if(mUploadlistener != null){
				
				mUploadlistener.onUploadFinish(msg.arg1, msg.obj);
			}
		}
	};

	@Override
	public void run() {

		Message msg = Message.obtain();
		try {

			String responseString = doPost(mFileInputStream, this.mServerUrl);
			msg.obj = responseString;
			if(msg.obj == null)
				msg.arg1 = UploadResult.FAIL;
			else
				msg.arg1 = UploadResult.SUCCESS;
			
		} catch (Exception e) {
			
			msg.arg1 = UploadResult.FAIL;
			msg.obj = null;
		}
		
		mHandler.sendMessage(msg);
	}

	private String doPost(InputStream fStream, String serverUrl)
			throws IOException {

		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		URL url = null;

		try {
			url = new URL(serverUrl);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* 允许Input、Output，不使用Cache */
		mHttpConn = (HttpURLConnection) url.openConnection();
		mHttpConn.setDoInput(true);
		mHttpConn.setDoOutput(true);
		mHttpConn.setUseCaches(false);
		/* 设定传送的method=POST */
		mHttpConn.setRequestMethod("POST");
		/* setRequestProperty */
		mHttpConn.setRequestProperty("Connection", "Keep-Alive");
		mHttpConn.setRequestProperty("Charset", "UTF-8");
		mHttpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
				+ boundary);
		/* 设定DataOutputStream */
		DataOutputStream ds = new DataOutputStream(mHttpConn.getOutputStream());
		ds.writeBytes(twoHyphens + boundary + end);
		ds.writeBytes("Content-Disposition: form-data; "
				+ "name=\"file\";filename=\"imgfile.png\"" + end);
		ds.writeBytes(end);
		/* 设定每次写入1024bytes */
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int length = -1;
		/* 从文件读取数据到缓冲区 */
		
		DataInputStream dis = new DataInputStream(fStream);
		while ((length = dis.read(buffer)) != -1) {
			/* 将数据写入DataOutputStream中 */
			ds.write(buffer, 0, length);
		}
		ds.writeBytes(end);
		ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

		/* close streams */
		fStream.close();
		ds.flush();
		/* 取得Response内容 */
		InputStream is = mHttpConn.getInputStream();
		int ch;
		StringBuffer b = new StringBuffer();
		while ((ch = is.read()) != -1) {
			b.append((char) ch);
		}

		int responseCode = mHttpConn.getResponseCode();
		String responseString = b.toString();
		if(responseCode != 200)
			responseString = null;
		
		/* 关闭DataOutputStream */
		ds.close();
		mHttpConn.disconnect();
		mHttpConn = null;
		
		return responseString;
	}
	
}
