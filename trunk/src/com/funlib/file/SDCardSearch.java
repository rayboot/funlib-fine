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

	private static final String CACHE_FILE_NAME="search.cache";
	
	private Context mContext;
	private Vector<SDCardSearchModel> mSearchResults;
	private HashMap<Integer, SDCardSearchModel> mSearchResultsMap;
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
				mSearchResultsMap = new HashMap<Integer, SDCardSearchModel>();
				
				//first from cache
				try {
					
					FileInputStream fis = mContext.openFileInput(CACHE_FILE_NAME);
					ObjectInputStream ois = new ObjectInputStream(fis);
					mSearchResults = (Vector<SDCardSearchModel>) ois.readObject();
					ois.close();
					sendMessage(SDCardSearchStatus.STATUS_SEARCH_ING.ordinal());
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				//delete not exist files from cache
				if(mSearchResults.size() > 0){
					
					Vector<SDCardSearchModel> tmpResult = new Vector<SDCardSearchModel>();
					for(int i = 0 ; i < mSearchResults.size() ;++i){
						
						SDCardSearchModel ssm = tmpResult.get(i);
						File tmpFile = new File(ssm.fileFullPath);
						if(tmpFile.exists() == true){
							mSearchResultsMap.put(ssm.fileFullPath.hashCode(), ssm);
							tmpResult.add(ssm);
						}
					}
				}
				
				//update background
				searchFiles(rootPath , suffix ,sizeLimit);
				
				//save search result 
				try {
					
					FileOutputStream fos = mContext.openFileOutput(CACHE_FILE_NAME, Context.MODE_PRIVATE);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(mSearchResults);
					oos.flush();
					oos.close();
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				sendMessage(SDCardSearchStatus.STATUS_SEARCH_END.ordinal());
			}
		};
	}
	
	private void searchFiles(String rootPath, String suffix ,long sizeLimit){
		
		File rootFile = new File(rootPath);
		File[] files = rootFile.listFiles();
		for (File file : files) {
			
			if(file.isDirectory()){
				
				searchFiles(rootPath , suffix , sizeLimit);
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
					
					String fullPath = file.getAbsolutePath();
					if(mSearchResultsMap.containsKey(fullPath.hashCode()) == false){
						
						SDCardSearchModel ssm = new SDCardSearchModel();
						ssm.fileFullPath = fullPath;
						ssm.fileName = file.getName();
						ssm.fileSize = size;
						
						mSearchResults.add(ssm);
						mSearchResultsMap.put(fullPath.hashCode(), ssm);
						sendMessage(SDCardSearchStatus.STATUS_SEARCH_ING.ordinal());
					}
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
	}
	
	
}
