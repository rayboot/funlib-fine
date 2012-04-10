package com.funlib.imagefilter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class ImageUtily {

	/**
	 * 修改图片亮度
	 * 
	 * @param bmp
	 * @param degress
	 *            亮度[-255, 255]
	 * @return
	 */
	public static Bitmap changeBrightness(Bitmap bmp, int brightness) {

		Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.RGB_565);
		ColorMatrix cMatrix = new ColorMatrix();
		cMatrix.set(new float[] { 1, 0, 0, 0, brightness, 0, 1, 0, 0,
				brightness,// 改变亮度
				0, 0, 1, 0, brightness, 0, 0, 0, 1, 0 });

		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(cMatrix));
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bmp, 0, 0, paint);

		return bitmap;
	}

	/**
	 * 修改对比度
	 * 
	 * @param bmp
	 * @param contrast
	 *            [0-1]
	 * @return
	 */
	public static Bitmap changeContrast(Bitmap bmp, float contrast) {

		Bitmap bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.RGB_565);
		ColorMatrix cMatrix = new ColorMatrix();
		cMatrix.set(new float[] { contrast, 0, 0, 0, 0, 0, contrast, 0, 0, 0,// 改变对比度
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
					bitmap.getHeight(), Config.RGB_565);
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

		Bitmap tmpMask = mask;
		if (src_w != mask_w || src_h != mask_h) {

			tmpMask = ImageUtily.resizeBitmap(mask, src_w, src_h);
		}

		Drawable[] array = new Drawable[2];
		array[0] = new BitmapDrawable(src);
		array[1] = new BitmapDrawable(tmpMask);

		LayerDrawable layer = new LayerDrawable(array);
		Bitmap bitmap = Bitmap
				.createBitmap(
						src_w,
						src_h,
						layer.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.RGB_565
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		layer.setBounds(0, 0, src_w, src_h);
		layer.draw(canvas);
		if(tmpMask.isRecycled() == false){
			tmpMask.recycle();
		}
		return bitmap;
	}

	/**
	 * 给图片添加指定宽高的黑色背景框
	 * 
	 * @param bitmap
	 * @param bgW
	 * @param bgH
	 * @return
	 */
	public static Bitmap addBitmapWhiteBG(Bitmap bitmap, int bgW, int bgH) {

		try {

			int x = 0;
			int y = 0;
			int bmpW = bitmap.getWidth();
			int bmpH = bitmap.getHeight();
			if (bgW > bgH)
				bgH = bgW;
			if (bgW < bgH)
				bgW = bgH;
			x = (bgW - bmpW) / 2;
			y = (bgH - bmpH) / 2;

			Bitmap output = Bitmap.createBitmap(bgW, bgH, Config.RGB_565);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			paint.setAntiAlias(true);
			canvas.drawARGB(255, 255, 255, 255);
			canvas.drawBitmap(bitmap, x, y, paint);
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
		BitmapFactory.decodeByteArray(data, 0, data.length, opt);
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
		b.compress(CompressFormat.JPEG, 100, os);
		if (b != null && b.isRecycled() == false && b.isMutable()) {

			b.recycle();
			b = null;
		}
		return os.toByteArray();
	}

	public static Bitmap decodeFileBitmap(String filePath, int fixSize) {

		Bitmap bmp = null;

		int scale = 1;
		if (fixSize != -1) {

			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, o);
			// The new size we want to scale to
			// Find the correct scale value. It should be the power of 2.
			while (o.outWidth / scale / 2 >= fixSize
					&& o.outHeight / scale / 2 >= fixSize)
				scale *= 2;
		}
		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		bmp = BitmapFactory.decodeFile(filePath, o2);

		return bmp;
	}

	/**
	 * 创建等比缩放图片
	 * 
	 * @param src
	 * @param targetW
	 * @param targetH
	 * @return
	 */
	public static Bitmap createProportionCompressBitmap(Context context , Bitmap src, int targetW,
			int targetH , int quality) {

		if (src == null)
			return src;

		final String TMP_FILE_NAME = "tmpcompress.jpg";
		int srcWidth = src.getWidth();
		int srcHeight = src.getHeight();
		double rate1 = ((double) srcWidth) / (double) targetW + 0.1;
		double rate2 = ((double) srcHeight) / (double) targetH + 0.1;
		// 根据缩放比率大的进行缩放控制
		double rate = rate1 > rate2 ? rate1 : rate2;
		int newWidth = (int) (((double) srcWidth) / rate);
		int newHeight = (int) (((double) srcHeight) / rate);

		Bitmap tmpBitmap = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
		try {
			
		 	FileOutputStream fos = context.openFileOutput(TMP_FILE_NAME, Context.MODE_PRIVATE);
		 	tmpBitmap.compress(CompressFormat.JPEG, quality, fos);
		 	fos.close();

		 	if(tmpBitmap != null && !tmpBitmap.isRecycled()){
		 		tmpBitmap.recycle();
		 	}
		 	FileInputStream fis = context.openFileInput(TMP_FILE_NAME);
		 	tmpBitmap = BitmapFactory.decodeStream(fis);
		 	fis.close();
		 	context.deleteFile(TMP_FILE_NAME);
		 	return tmpBitmap;
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}
	
	public static Bitmap scaleBitmapToSize(Context context , Bitmap bitmap , int targetW , int targetH , int quality){
		
		final String TMP_FILE_NAME = "tmpcompress.jpg";
		try {
			
			int bitmapWidth = bitmap.getWidth();
			int bitmapHeight = bitmap.getHeight();
			float scaleWidth = (float) targetW / bitmapWidth;
			float scaleHeight = (float) targetH / bitmapHeight;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
			FileOutputStream fos = context.openFileOutput(TMP_FILE_NAME, Context.MODE_PRIVATE);
			tmpBitmap.compress(CompressFormat.JPEG, quality, fos);
		 	fos.close();

		 	if(tmpBitmap != null && !tmpBitmap.isRecycled()){
		 		tmpBitmap.recycle();
		 	}
		 	FileInputStream fis = context.openFileInput(TMP_FILE_NAME);
		 	tmpBitmap = BitmapFactory.decodeStream(fis);
		 	fis.close();
		 	context.deleteFile(TMP_FILE_NAME);
		 	return tmpBitmap;
		 	
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}
}