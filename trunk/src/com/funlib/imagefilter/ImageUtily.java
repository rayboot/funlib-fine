package com.funlib.imagefilter;

import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class ImageUtily {
	
	/**
	 * 模仿ps 柔光效果
	 * @param src
	 * @param mask
	 * @param maskAlpha
	 * @return
	 */
	public static Bitmap ps_softLight(Bitmap src , Bitmap mask , float maskAlpha){
		
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
		int pixColorSrc = 0;
		int pixColorMask = 0;
		int RSrc = 0;
		int GSrc = 0;
		int BSrc = 0;
		int RMask = 0;
		int GMask = 0;
		int BMask = 0;
		int index = 0;
		int[] pixelsSrc = new int[width * height];
		int[] pixelsMask = new int[width * height];
		src.getPixels(pixelsSrc, 0, width, 0, 0, width, height);
		mask.getPixels(pixelsMask, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				
				index = width*i + k; 
				pixColorSrc = pixelsSrc[index];
				pixColorMask = pixelsMask[index];
				RSrc = Color.red(pixColorSrc);
				GSrc = Color.green(pixColorSrc);
				BSrc = Color.blue(pixColorSrc);
				RMask = Color.red(pixColorMask);
				GMask = Color.green(pixColorMask);
				BMask = Color.blue(pixColorMask);
				
				RSrc = ps_softLightFun(RSrc, RMask , maskAlpha);
				GSrc = ps_softLightFun(GSrc, GMask , maskAlpha);
				BSrc = ps_softLightFun(BSrc, BMask , maskAlpha);
				pixelsSrc[index] = Color.argb(255, RSrc, GSrc, BSrc);
			}
		}

		bitmap.setPixels(pixelsSrc, 0, width, 0, 0, width, height);
		return bitmap;
	}
	private static int ps_softLightFun(int src , int mask , float maskAlpha){
		
		int value = (int) (((src < 128)?(2*((mask>>1)+64))*((float)src/255):(255-(2*(255-((mask>>1)+64))*(float)(255-src)/255))));
		return (int) ((1-maskAlpha)*src + maskAlpha*value);
	}
	
	/**
	 * 模仿ps 排除效果
	 * @param src
	 * @param mask
	 * @param maskAlpha
	 * @return
	 */
	public static Bitmap ps_exclusion(Bitmap src , Bitmap mask , float maskAlpha){
		
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
		int pixColorSrc = 0;
		int pixColorMask = 0;
		int RSrc = 0;
		int GSrc = 0;
		int BSrc = 0;
		int RMask = 0;
		int GMask = 0;
		int BMask = 0;
		int index = 0;
		int[] pixelsSrc = new int[width * height];
		int[] pixelsMask = new int[width * height];
		src.getPixels(pixelsSrc, 0, width, 0, 0, width, height);
		mask.getPixels(pixelsMask, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				
				index = width*i + k; 
				pixColorSrc = pixelsSrc[index];
				pixColorMask = pixelsMask[index];
				RSrc = Color.red(pixColorSrc);
				GSrc = Color.green(pixColorSrc);
				BSrc = Color.blue(pixColorSrc);
				RMask = Color.red(pixColorMask);
				GMask = Color.green(pixColorMask);
				BMask = Color.blue(pixColorMask);
				
				RSrc = ps_exclusionFun(RSrc, RMask , maskAlpha);
				GSrc = ps_exclusionFun(GSrc, GMask , maskAlpha);
				BSrc = ps_exclusionFun(BSrc, BMask , maskAlpha);
				pixelsSrc[index] = Color.argb(255, RSrc, GSrc, BSrc);
			}
		}

		bitmap.setPixels(pixelsSrc, 0, width, 0, 0, width, height);
		return bitmap;
	}
	private static int ps_exclusionFun(int src , int mask , float maskAlpha){
		
		int value = (int) ((src + mask - 2 * src * mask / 255));
		return (int) ((1-maskAlpha)*src + maskAlpha*value);
	}
	
	/**
	 * 修改图片亮度
	 * @param bmp
	 * @param degress 亮度[-255, 255]
	 * @return
	 */
	public static Bitmap changeBrightness(Bitmap bmp , int brightness){
		
		Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),  Config.ARGB_8888);  
        ColorMatrix cMatrix = new ColorMatrix();  
        cMatrix.set(new float[] { 1, 0, 0, 0, brightness, 0, 1,  
                0, 0, brightness,// 改变亮度  
                0, 0, 1, 0, brightness, 0, 0, 0, 1, 0 });  

        Paint paint = new Paint();  
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));  
        Canvas canvas = new Canvas(bitmap);  
        canvas.drawBitmap(bmp, 0, 0, paint);  
        
        return bitmap;
	}
	
	/**
	 * 修改对比度
	 * @param bmp
	 * @param contrast [0-1]
	 * @return
	 */
	public static Bitmap changeContrast(Bitmap bmp, float contrast){
		
		Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),  Config.ARGB_8888);
        ColorMatrix cMatrix = new ColorMatrix();  
        cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0,  
        		contrast, 0, 0, 0,// 改变对比度  
                0, 0, contrast, 0, 0, 0, 0, 0, 1, 0 });  

        Paint paint = new Paint();  
        paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));  
        Canvas canvas = new Canvas(bitmap);  
        canvas.drawBitmap(bmp, 0, 0, paint);
        return bitmap;
	}

	/**
	 * 生成圆角图片
	 * 
	 * @param bitmap
	 * @param corner
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float corner) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight()));
			final float roundPx = corner;
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.WHITE);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());

			canvas.drawBitmap(bitmap, src, rect, paint);
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	/**
	 * byte数组转换成Bitmap
	 * 
	 * @param bmp
	 * @return
	 */
	public static byte[] bitmap2Byte(Bitmap bmp) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 数组转换成Bitmap
	 * 
	 * @param buffer
	 * @return
	 */
	public static Bitmap byte2Bitmap(byte[] buffer) {
		return BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
	}

	/**
	 * Bitmap转换成Drawable
	 * 
	 * @param bmp
	 * @return
	 */
	public static Drawable bitmap2Drawable(Bitmap bmp) {
		return new BitmapDrawable(bmp);
	}

	/**
	 * BitmapDrawable转换成Bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawable2Bitmap(BitmapDrawable drawable) {
		return drawable.getBitmap();
	}

	/**
	 * 图片旋转
	 * 
	 * @param bmp
	 *            要旋转的图片
	 * @param degree
	 *            图片旋转的角度，负值为逆时针旋转，正值为顺时针旋转
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bmp, float degree) {

		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
				matrix, true);
	}

	/**
	 * 图片缩放
	 * 
	 * @param bm
	 * @param scale
	 *            值小于1则为缩小，否则为放大
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap bm, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
				matrix, true);
	}

	/**
	 * 图片等比缩放
	 * 
	 * @param bm
	 * @param w
	 *            缩小或放大成的宽
	 * @param h
	 *            缩小或放大成的高
	 * @return
	 */
	public static Bitmap resizeBitmap(Bitmap bm, int w, int h) {
		Bitmap BitmapOrg = bm;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		float scaleWidth = ((float) w) / width;
		float scaleHeight = ((float) h) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap
				.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
	}

	/**
	 * 图片反转
	 * 
	 * @param bm
	 * @param flag
	 *            0为水平反转，1为垂直反转
	 * @return
	 */
	public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
		float[] floats = null;
		switch (flag) {
		case 0: // 水平反转
			floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
			break;
		case 1: // 垂直反转
			floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
			break;
		}

		if (floats != null) {
			Matrix matrix = new Matrix();
			matrix.setValues(floats);
			return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
		}

		return null;
	}


	/**
	 * 给图片添加相框，mask要是png格式，中间部分支持透明 如果src图片尺寸小于mask，会对src按照mask比例缩放
	 * 
	 * @param src
	 * @param mask
	 *            相框图片
	 * @return
	 */
	public static Bitmap addFrame(Bitmap src, Bitmap mask) {

		int src_w = src.getWidth();
		int src_h = src.getHeight();
		int mask_w = mask.getWidth();
		int mask_h = mask.getHeight();

		if (src_w < mask_w || src_h < mask_h)
			src = Bitmap.createScaledBitmap(src, mask_h, mask_h, false);

		Drawable[] array = new Drawable[2];
		array[0] = new BitmapDrawable(src);
		array[1] = new BitmapDrawable(mask);

		LayerDrawable layer = new LayerDrawable(array);
		Bitmap bitmap = Bitmap
				.createBitmap(
						mask_w,
						mask_w,
						layer.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		layer.setBounds(0, 0, mask_w, mask_w);
		layer.draw(canvas);
		return bitmap;
	}
	
	/**
	 * 给图片添加指定宽高的黑色背景框
	 * @param bitmap
	 * @param bgW
	 * @param bgH
	 * @return
	 */
	public static Bitmap addBitmapBlackBG(Bitmap bitmap , int bgW , int bgH){
		
		try {
			
			int x = 0;
			int y = 0;
			int bmpW = bitmap.getWidth();
			int bmpH = bitmap.getHeight();
			x = (bgW - bmpW)/2;
			y = (bgH - bmpH)/2;
			
			Bitmap output = Bitmap.createBitmap(bgW , bgH , Config.RGB_565);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			canvas.drawBitmap(bitmap , x , y , paint);
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}

	/**
	 * 解析出图片的宽高
	 * 
	 * @param data
	 * @return
	 */
	public static PointF getBitmapBounds(byte[] data) {

		PointF pf = new PointF();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length , opt);
		pf.x = opt.outWidth;
		pf.y = opt.outHeight;

		return pf;
	}

	public static byte[] getResizedImageData(byte[] data, int srcWidth,
			int srcHeight, int widthLimit, int heightLimit) {
		int outWidth = srcWidth;
		int outHeight = srcHeight;
		int s = 1;
		while ((outWidth / s > widthLimit) || (outHeight / s > heightLimit)) {
			s *= 2;
		}
		// 先设置选项
		BitmapFactory.Options options = new BitmapFactory.Options();
		// returning a smaller image to save memory.
		options.inSampleSize = s;
		Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		if (b == null) {
			return null;
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		b.compress(CompressFormat.JPEG, 80, os);
		return os.toByteArray();
	}
}
