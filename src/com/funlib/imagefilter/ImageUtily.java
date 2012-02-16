package com.funlib.imagefilter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import android.R.integer;
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
import android.util.Log;

public class ImageUtily {

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
	 * 组合两张图片，通过像素叠加 如果src图片尺寸小于mask，会对src按照mask比例缩放 src图片会先进行灰度处理
	 * 
	 * @param src
	 * @param mask
	 *            相框图片
	 * @param srcGray
	 *            是否对原图做灰度处理
	 * @return
	 */
	public static Bitmap combinateBitmapByPixel(Bitmap src, Bitmap mask,
			boolean srcGray) {

		int src_w = src.getWidth();
		int src_h = src.getHeight();
		int mask_w = mask.getWidth();
		int mask_h = mask.getHeight();
		if (mask_w > src_w || mask_h > src_h) {

			src = Bitmap.createScaledBitmap(src, mask_w, mask_h, false);
		}
		Bitmap bmpGrayscale = null;
		if (srcGray) {
			bmpGrayscale = Bitmap.createBitmap(mask_w, mask_h,
					Bitmap.Config.RGB_565);
			Canvas c = new Canvas(bmpGrayscale);
			Paint paint = new Paint();
			ColorMatrix cm = new ColorMatrix();
			cm.setSaturation(0);
			ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
			paint.setColorFilter(f);
			paint.setAntiAlias(true);
			c.drawBitmap(src, 0, 0, paint);
			src = bmpGrayscale;
		}

		src_w = src.getWidth();
		src_h = src.getHeight();

		int[] srcPixels = new int[src_w * src_h];
		int[] maskPixels = new int[src_w * src_h];
		int[] dstPixels = new int[src_w * src_h];
		src.getPixels(srcPixels, 0, src_w, 0, 0, src_w, src_h);
		mask.getPixels(maskPixels, 0, src_w, 0, 0, src_w, src_h);

		int srcPixColor = 0;
		int srcPixR = 0;
		int srcPixG = 0;
		int srcPixB = 0;
		int yOffset = 0;
		int maskPixColor = 0;
		int maskPixR = 0;
		int maskPixG = 0;
		int maskPixB = 0;
		int dstPixColor = 0;
		int dstPixR = 0;
		int dstPixG = 0;
		int dstPixB = 0;
		for (int y = 0; y < src_h; y++) {
			for (int x = 0; x < src_w; x++) {
				yOffset = y * src_w + x;
				srcPixColor = srcPixels[yOffset];
				srcPixR = Color.red(srcPixColor);
				srcPixG = Color.green(srcPixColor);
				srcPixB = Color.blue(srcPixColor);

				maskPixColor = maskPixels[yOffset];
				maskPixR = Color.red(maskPixColor);
				maskPixG = Color.green(maskPixColor);
				maskPixB = Color.blue(maskPixColor);

				double k = (double) 125 / 256.0;
				double reverse_k = 1.0 - k;

				dstPixR = (int) (srcPixR + maskPixR * k);
				dstPixG = (int) (srcPixG + maskPixG * k);
				dstPixB = (int) (srcPixB + maskPixB * k);

				dstPixColor = Color.argb(255, dstPixR > 255 ? 255 : dstPixR,
						dstPixG > 255 ? 255 : dstPixG, dstPixB > 255 ? 255
								: dstPixB);
				dstPixels[yOffset] = dstPixColor;
			}
		}

		return Bitmap.createBitmap(dstPixels, src_w, src_h, Config.ARGB_8888);
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
