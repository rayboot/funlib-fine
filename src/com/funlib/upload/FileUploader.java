package com.funlib.upload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import com.funlib.log.FLog;
import com.funlib.network.NetWork;

import android.R.integer;
import android.content.Context;
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
	
	private Context mContext;

	public FileUploader(Context context, UploadListener lis) {
		
		mContext = context;
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
				
				mUploadlistener.onUploadStatusChanged(msg.arg1, msg.arg2 , msg.obj);
			}
		}
	};

	@Override
	public void run() {

		try {

			String responseString = doPost(mFileInputStream, this.mServerUrl);
			if(responseString == null){
				sendMessage(UploadStatus.FAIL , 0 , null);
			}else{
				sendMessage(UploadStatus.SUCCESS , 100 , responseString);
			}
			
		} catch (Exception e) {
			
			sendMessage(UploadStatus.FAIL , 0 , null);
		}
		
	}

	private void sendMessage(int status , int percent , Object obj){
		
		Message msg = Message.obtain();
		msg = Message.obtain();
		msg.arg1 = status;
		msg.arg2 = percent;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	
	private String doPost(InputStream fStream, String serverUrl)
			throws IOException {

		sendMessage(UploadStatus.STARTUPLOADING , 0 , null);
		
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
		if(NetWork.isDefaultWap(mContext)){
        	Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress(NetWork.getDefaultWapProxy(), NetWork.getDefaultWapPort()));
        	mHttpConn = (HttpURLConnection) url.openConnection(proxy);
        }else{
        	mHttpConn = (HttpURLConnection) url.openConnection();
        }
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
		int totalLength = dis.available();
		int tmpTotalLength = 0;
		while ((length = dis.read(buffer)) != -1) {
			/* 将数据写入DataOutputStream中 */
			ds.write(buffer, 0, length);
			
			tmpTotalLength += length;
			sendMessage(UploadStatus.UPLOADING , tmpTotalLength/totalLength*100 , null);
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
