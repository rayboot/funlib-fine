package com.funlib.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class SDCardSearch {

	private Context mContext;
	private Vector<SDCardSearchModel> mSearchResults;
	private FileSearchListener mFileSearchListener;
	
	private Thread mSearchThread;
	
	public SDCardSearch(Context context , FileSearchListener l , Vector<SDCardSearchModel> result){
		
		mContext = context;
		mSearchResults = result;
		mFileSearchListener = l;
	}
	
	private Handler mSearchHandler = new Handler(){
		
		public void handleMessage(Message msg){
			
			if(mFileSearchListener != null)
				mFileSearchListener.onFileSearchStatusChanged(msg.arg1, mSearchResults);
		}
	};
	private void sendMessage(int status){
		Message msg = Message.obtain();
		msg.what = 0;
		msg.arg1 = status;
		mSearchHandler.sendMessage(msg);
	}
	public void startSearch(final String rootPath , final String suffix , final long sizeLimit){
		
		mSearchThread = new Thread(){
			
			public void run(){
				
				sendMessage(SDCardSearchStatus.STATUS_SEARCH_BEGIN.ordinal());
				
				//first from cache
				//delete not exist files from cache
				//update background
				searchFiles(rootPath , suffix ,sizeLimit);
				
				//save search result 
				sendMessage(SDCardSearchStatus.STATUS_SEARCH_END.ordinal());
			}
		};
		mSearchThread.start();
	}
	
	private void searchFiles(String rootPath, String suffix ,long sizeLimit){
		
		File rootFile = new File(rootPath);
		File[] files = rootFile.listFiles();
		if(files == null)
			return;
		for (File file : files) {
			
			if(file.isHidden())
				continue;
			
			if(file.isDirectory()){
				
				searchFiles(file.getAbsolutePath() , suffix , sizeLimit);
			}else{
				
				long size = file.length();
				if(size < sizeLimit) continue;
				{
					String name = file.getName();
					int index = name.lastIndexOf(".");
					if(index == -1)
						continue;
					String tmpSuffix = name.substring(index+1);
					if(suffix.toLowerCase().contains(tmpSuffix.toLowerCase()) == false)
						continue;
				}
				{
					SDCardSearchModel ssm = new SDCardSearchModel();
					ssm.fileFullPath = file.getAbsolutePath();
					ssm.fileName = file.getName();
					ssm.fileSize = size;
					
					mSearchResults.add(ssm);
					sendMessage(SDCardSearchStatus.STATUS_SEARCH_ING.ordinal());
				}
			}
		}
	}
	
	public void stop(){
		
		if(mSearchThread != null){
			
			mSearchThread.stop();
			mSearchThread = null;
		}
	}
	
	
	public interface FileSearchListener{
		
		public void onFileSearchStatusChanged(int status , List<SDCardSearchModel> result);
	}
	
	public enum SDCardSearchStatus{
		
		STATUS_SEARCH_BEGIN,
		STATUS_SEARCH_ING,
		STATUS_SEARCH_END
	}
	
	public class SDCardSearchModel implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String fileName;
		public long fileSize;
		public String fileFullPath; 
		
		public boolean selected;//for multi select
	}
	
	
}
