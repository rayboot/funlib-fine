package com.funlib.upload;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.funlib.httputily.HttpUtily;
import com.funlib.log.FLog;
import com.funlib.network.NetWork;
import com.funlib.upload.CustomMultiPartEntity.ProgressListener;

/**
 * 只用于上传组建
 * 
 * @author taojianli
 * 
 */
public class FileUploader {

	private UploadListener mUploadlistener;
	private Context mContext;
	private HttpMultipartPost mPostTask;

	public FileUploader(Context context, UploadListener lis) {

		mContext = context;
		mUploadlistener = lis;
	}

	/**
	 * 
	 * @param serverUrl
	 *            目标服务器地址
	 * @param is
	 *            待上传的文件流
	 */
	public void upLoadFile(String serverUrl, InputStream is, String fileName) {

		mPostTask = new HttpMultipartPost();
		mPostTask.execute(serverUrl, is, fileName);
	}

	/**
	 * 取消上传
	 */
	public void cancel() {

	}

	class HttpMultipartPost extends AsyncTask<Object, Integer, String> {
		long totalSize;

		@Override
		protected void onPreExecute() {
			
			if(mUploadlistener != null)
				mUploadlistener.onUploadStatusChanged(UploadStatus.STARTUPLOADING, 0, null);
		}

		@Override
		protected String doInBackground(Object... arg0) {

			String serverUrl = (String) arg0[0];
			InputStream iStream = (InputStream) arg0[1];
			String fileName = (String) arg0[2];

			HttpClient httpClient = new DefaultHttpClient();
			HttpContext httpContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(serverUrl);
			String serverResponse = "";
			try {
				CustomMultiPartEntity multipartContent = new CustomMultiPartEntity(
						new ProgressListener() {
							@Override
							public void transferred(long num) {
								
								int percent = (int)((float)num / (float)totalSize * 100);
								publishProgress(percent);
							}
						});

				InputStreamBody isb = new InputStreamBody(iStream, fileName); 
				multipartContent.addPart(fileName, isb);
				totalSize = iStream.available();

				httpPost.setEntity(multipartContent);
				HttpResponse response = httpClient.execute(httpPost,
						httpContext);
				serverResponse = EntityUtils.toString(response.getEntity());

			} catch (Exception e) {
			}

			return serverResponse;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if(mUploadlistener != null)
				mUploadlistener.onUploadStatusChanged(UploadStatus.UPLOADING, (int)progress[0], null);
		}

		@Override
		protected void onPostExecute(String response) {
			
			if(mUploadlistener != null){
				if(response == null)
					response = "";
				mUploadlistener.onUploadStatusChanged(UploadStatus.FINISH, 100, response);
			}
		}
	}
}
