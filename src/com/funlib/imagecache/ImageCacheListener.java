package com.funlib.imagecache;

import android.graphics.Bitmap;

/**
 * 获取图片的监听器
 * @author taojianli
 * 
 */
public interface ImageCacheListener {

	/**
	 * 图片获取结果
	 * @param error 错误码，见ImageCacheError
	 * @param target 需要图片的对象
	 * @param bitmap 获取到的图像
	 * @param imageCache
	 */
	public void getImageFinished(int error, Object target, Bitmap bitmap , ImageCache imageCache);

}
