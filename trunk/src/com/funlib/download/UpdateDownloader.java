package com.funlib.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.funlib.log.FLog;
import com.funlib.network.NetWork;

/**
 * 只用于升级下载组建
 * 
 * 用法：
 * UpdateDownloader ud = new UpdateDownloader();
   ud.download(null, "http://www.baidu.com/yy.apk", this, "/sdcard/test.apk");
 * 
 * @author taojianli
 *
 */
public class UpdateDownloader implements Runnable {

	private static final int BUFFER_SIZE = 10*1024;//缓冲大小
	
	private int mReadTimeout 			= 	5000;		/** 读取超时时间默认值 */
	private int mConnectionTimeout 		= 	5000;		/** 连接超时时间默认值 */
	private int mFailRetryCount			=	3;			/** 失败重试次数 */

	private boolean bCanceled;
	private String mUrl;
	private DownloadListener mDownloadListener;
	private String mFilePath;
	private Handler mHandler;
	private int mDownloadPercent;
	private int mPreDownloadPercent;

	private Context mContext;
	private HttpURLConnection mHttpURLConnection;
	private InputStream mDownloadInputStream = null;
	
	private Object mUpdateTag;

	public UpdateDownloader(Context context) {

		mContext = context;
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				if(mDownloadListener != null)
				    mDownloadListener.onDownloadStatusChanged(mUpdateTag , 0, msg.what, (Integer) msg.obj, mFilePath);
			}
		};
	}
	
	/**
	 * 设置读操作超时时间，默认5s
	 * @param timeout
	 */
	public void setReadTimeout(int timeout){
		
		mReadTimeout = timeout;
	}
	
	/**
	 * 设置链接操作超时时间，默认5s
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout){
		
		mConnectionTimeout = timeout;
	}
	
	/**
	 * 设置超时失败后的重试次数，默认3次
	 * @param cnt
	 */
	public void setFailRetryCount(int cnt){
		
		mFailRetryCount = cnt;
	}

	/**
	 * 
	 * @param tag 下载属性
	 * @param url 下载地址
	 * @param listener 下载状态监听
	 * @param filePath 文件存放路径，带后缀
	 */
	public void download(Object tag, String url, DownloadListener listener, String filePath) {

		bCanceled = false;
		mUpdateTag = tag;
		mUrl = url;
		mDownloadListener = listener;
		mFilePath = filePath;
		bCanceled = false;
		mDownloadPercent = 0;
        mPreDownloadPercent = 0;

		new Thread(this).start();
	}

	/**
	 * 取消下载
	 */
	public void canceled() {
		
		bCanceled = true;
		try{
		    
		    if(mDownloadInputStream != null)
	            mDownloadInputStream.close();
		    
		    if(mHttpURLConnection != null)
		    	mHttpURLConnection.disconnect();
		    
		    Thread.sleep(500);
		}catch(Exception e){
		    
		}
	}
	
	/**
	 * 发送下载状态消息
	 * @param status
	 * @param percent
	 */
	private void sendMessage(int status , int percent){
		
		Message msg = mHandler.obtainMessage();
        msg = mHandler.obtainMessage();
        msg.what = status;
        msg.obj = percent;
        mHandler.sendMessage(msg);
	}

    public void run() {

    	// /线程一开始运行，即可发送STATUS_DOWNLOADING消息
//    	sendMessage(DownloadStatus.STATUS_STARTDOWNLOADING , 0);
        //

        URL url = null;
        boolean bInitConnection = false;
        byte[] buffer = null;
        int readTotalCnt = 0;
        int retryCount = 0;
        int fileSize = 0;
        File file = null;
        RandomAccessFile accessFile = null;
        
        do {
			
        	try {
				
        		if(bInitConnection == false){
        			
        			if(file == null){
        				
        				try {
							
        					file = new File(mFilePath);
        					if(file.exists())
        						file.delete();
            				accessFile = new RandomAccessFile(file, "rwd");
						} catch (Exception e) {
							// TODO: handle exception
							
							sendMessage(DownloadStatus.STATUS_RW_FILE_ERROR, 0);
							return;
						}
        			}
        			
        			canceled();
        			bCanceled = false;
        			url = new URL(mUrl);
        			if(NetWork.isDefaultWap(mContext)){
                    	Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress(NetWork.getDefaultWapProxy(), NetWork.getDefaultWapPort()));
                    	mHttpURLConnection = (HttpURLConnection) url.openConnection(proxy);
                    }else{
                    	mHttpURLConnection = (HttpURLConnection) url.openConnection();
                    }
        			mHttpURLConnection.setConnectTimeout(mConnectionTimeout);
        			mHttpURLConnection.setReadTimeout(mReadTimeout);
        			if(readTotalCnt > 0 && fileSize > 0)
        				mHttpURLConnection.setRequestProperty("Range", "bytes=" + readTotalCnt + "-" + fileSize);
        			accessFile.seek(readTotalCnt);
        			mHttpURLConnection.connect();
        			fileSize = mHttpURLConnection.getContentLength();
        			
        			sendMessage(DownloadStatus.STATUS_STARTDOWNLOADING , fileSize/1024);
        			
                    mDownloadInputStream = mHttpURLConnection.getInputStream();
                    
                    bInitConnection = true;
        		}else{//binitconnection == true
        			
        			buffer = null;
        			buffer = new byte[BUFFER_SIZE];
                    int realReadCnt = mDownloadInputStream.read(buffer);
                    if (realReadCnt == -1) {// 读完

                        sendMessage(DownloadStatus.STATUS_COMPLETE, mDownloadPercent);
                        return;
                    } else {

                        retryCount = 0;// 重置retryCount

                        readTotalCnt += realReadCnt;
                        accessFile.write(buffer, 0, realReadCnt);

//                        mDownloadPercent = (int) ((readTotalCnt*100 / fileSize));
                        mDownloadPercent = readTotalCnt/1024;
                        // FIXME tjianli比较两次下载进度，有明显变化，才会通知界面更新，尽量避免ANR
                        if (mDownloadPercent > mPreDownloadPercent  + 30) {

                            mPreDownloadPercent = mDownloadPercent;
                            sendMessage(DownloadStatus.STATUS_DOWNLOADING, mDownloadPercent);
                        }
                        //

                    }
        		}
        		
        		
			} catch (Exception e) {
				// TODO: handle exception
				
				canceled();
				bCanceled = false;
				
				FLog.i(">>>>retry for :" + retryCount);
				if (retryCount >= mFailRetryCount) {
					
					sendMessage(DownloadStatus.STATUS_NET_ERROR , mDownloadPercent);
					return;
                }

                ++retryCount;
				bInitConnection = false;
			}
        	
        	if (bCanceled == true) {
                
        		canceled();
        		sendMessage(DownloadStatus.STATUS_CANCELED , mDownloadPercent);
        		return;
            }

		} while (true);
    }

}
