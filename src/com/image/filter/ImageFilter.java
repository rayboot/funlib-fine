package com.image.filter;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

public class ImageFilter {

	/**
	 * 黑白效果
	 * 
	 * @param bmpOriginal
	 * @return
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {

		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);

		return bmpGrayscale;
	}

	/**
	 * 怀旧效果
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap toOldRemeber(Bitmap bmp) {

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		int pixColor = 0;
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				pixColor = pixels[width * i + k];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
				newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
				newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
				int newColor = Color.argb(255, newR > 255 ? 255 : newR,
						newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
				pixels[width * i + k] = newColor;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 柔化效果(高斯模糊)
	 * 
	 * @param bmp
	 * @param delta
	 *            模糊度，值越小图片会越亮，越大则越暗
	 * @return
	 */
	public static Bitmap toBlurImageAmeliorate(Bitmap bmp, int delta) {

		// 高斯矩阵
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);

						newR = newR + (int) (pixR * gauss[idx]);
						newG = newG + (int) (pixG * gauss[idx]);
						newB = newB + (int) (pixB * gauss[idx]);
						idx++;
					}
				}

				newR /= delta;
				newG /= delta;
				newB /= delta;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[i * width + k] = Color.argb(255, newR, newG, newB);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 浮雕效果
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap toEmboss(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				pixColor = pixels[pos + 1];
				newR = Color.red(pixColor) - pixR + 127;
				newG = Color.green(pixColor) - pixG + 127;
				newB = Color.blue(pixColor) - pixB + 127;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 底片效果
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap toFilm(Bitmap bmp) {
		// RGBA的最大值
		final int MAX_VALUE = 255;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				newR = MAX_VALUE - pixR;
				newG = MAX_VALUE - pixG;
				newB = MAX_VALUE - pixB;

				newR = Math.min(MAX_VALUE, Math.max(0, newR));
				newG = Math.min(MAX_VALUE, Math.max(0, newG));
				newB = Math.min(MAX_VALUE, Math.max(0, newB));

				pixels[pos] = Color.argb(MAX_VALUE, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 光照效果
	 * 
	 * @param bmp
	 * @param strength
	 *            光照强度 100~150
	 * @return
	 */
	public static Bitmap toSunshine(Bitmap bmp, final float strength) {

		final int width = bmp.getWidth();
		final int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(centerX, centerY);

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int pos = 0;
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				pos = i * width + k;
				pixColor = pixels[pos];

				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				newR = pixR;
				newG = pixG;
				newB = pixB;

				// 计算当前点到光照中心的距离，平面座标系中求两点之间的距离
				int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(
						centerX - k, 2));
				if (distance < radius * radius) {
					// 按照距离大小计算增加的光照值
					int result = (int) (strength * (1.0 - Math.sqrt(distance)
							/ radius));
					newR = pixR + result;
					newG = pixG + result;
					newB = pixB + result;
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[pos] = Color.argb(255, newR, newG, newB);
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 素描效果
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap toSketch(Bitmap bmp) {

		int w = bmp.getWidth();
		int h = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

		int[] pixels = new int[w * h];
		bmp.getPixels(pixels, 0, w, 0, 0, w, h);

		// Convert to simple grayscale
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int idx = (y * w) + x;
				int p = pixels[idx];
				int r = p & 0x00FF0000 >> 16;
				int g = p & 0x0000FF >> 8;
				int b = p & 0x000000FF;
				pixels[idx] = (int) ((r + g + b) / 3.0);
			}
		}

		int convolutionSize = 3;
		int[][] convolution = { { 0, -1, 0 }, { -1, 4, -1 }, { 0, -1, 0 } };

		int[] newPixels = new int[w * h];
		// Apply the convolution to the whole image, note that we start at
		// 1 instead 0 zero to avoid out-of-bounds access
		for (int y = 1; y + 1 < h; y++) {
			for (int x = 1; x + 1 < w; x++) {
				int idx = (y * w) + x;

				// Apply the convolution
				for (int cy = 0; cy < convolutionSize; cy++) {
					for (int cx = 0; cx < convolutionSize; cx++) {
						int cIdx = (((y - 1) + cy) * w) + ((x - 1) + cx);
						newPixels[idx] += convolution[cy][cx] * pixels[cIdx];
					}
				}

				// pixel value rounding
				if (newPixels[idx] < 0) {
					newPixels[idx] = -newPixels[idx];
				} else {
					newPixels[idx] = 0;
				}
				if (newPixels[idx] > 0) {
					newPixels[idx] = 120 - newPixels[idx];
				} else {
					newPixels[idx] = 255;
				}

			}
		}

		// Convert to "proper" grayscale
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int idx = (y * w) + x;
				int p = newPixels[idx];
				newPixels[idx] = 0xFF000000 | (p << 16) | (p << 8) | p;
			}
		}

		bitmap.setPixels(newPixels, 0, w, 0, 0, w, h);
		return bitmap;
	}

	/**
	 * 扫描线效果
	 * 
	 * @param input
	 * @return
	 */
	public static Bitmap toScanLine(Bitmap input) {

		int input_w = input.getWidth();
		int input_h = input.getHeight();
		int[] pixels = new int[input_w * input_h];
		input.getPixels(pixels, 0, input_w, 0, 0, input_w, input_h);

		int pixColor = 0;
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int yOffset = 0;
		for (int y = 0; y < input_h; y += 2) {
			for (int x = 0; x < input_w; x++) {
				yOffset = y * input_w + x;
				pixColor = pixels[yOffset];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				int rr = pixR * 2;
				newR = rr > 255 ? 255 : rr;
				int gg = pixG * 2;
				newG = gg > 255 ? 255 : gg;
				int bb = pixB * 2;
				newB = bb > 255 ? 255 : bb;

				int newColor = Color.argb(255, newR > 255 ? 255 : newR,
						newG > 255 ? 255 : newG, newB > 255 ? 255 : newB);
				pixels[yOffset] = newColor;

			}
		}

		Bitmap dst = Bitmap.createBitmap(input_w, input_h,
				Bitmap.Config.RGB_565);
		dst.setPixels(pixels, 0, input_w, 0, 0, input_w, input_h);

		return dst;
	}

	/**
	 * 漫画效果
	 * 
	 * @param input
	 * @return
	 */
	public static Bitmap toCartoon(Bitmap input) {

		int input_w = input.getWidth();
		int input_h = input.getHeight();
		int[] pixels = new int[input_w * input_h];
		input.getPixels(pixels, 0, input_w, 0, 0, input_w, input_h);

		int pixColor = 0;
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int yOffset = 0;
		for (int y = 0; y < input_h; y++) {
			for (int x = 0; x < input_w; x++) {
				yOffset = y * input_w + x;
				pixColor = pixels[yOffset];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);

				int ava = (int) ((pixR + pixG + pixB) / 3.0);
				int newAva = ava > 128 ? 255 : 0;
				int newColor = Color.argb(255, newAva, newAva, newAva);
				pixels[yOffset] = newColor;

			}
		}

		Bitmap dst = Bitmap.createBitmap(input_w, input_h,
				Bitmap.Config.RGB_565);
		dst.setPixels(pixels, 0, input_w, 0, 0, input_w, input_h);

		return dst;
	}

	/**
	 * 鱼眼效果
	 * @param input
	 * @param k 建议值0.00002f
	 * @return
	 */
	public static Bitmap toFishEye(Bitmap input, float k) {

		float xscale;
		float yscale;
		float xshift;
		float yshift;
		float res;
		int offset;
		float x;
		float y;
		int[] s = new int[4];
		int[] s1= new int[4];
		int[] s2= new int[4];
		int[] s3= new int[4];
		int[] s4= new int[4];
		
		float centerX = input.getWidth() / 2; // center of distortion
		float centerY = input.getHeight() / 2;

		int width = input.getWidth(); // image bounds
		int height = input.getHeight();

		Bitmap dst = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565); // output
		int[] dstPixels = new int[width * height];
		int[] srcPixels = new int[width * height];
		input.getPixels(srcPixels, 0, width, 0, 0, width, height);
		
		xshift = calc_shift(0, centerX - 1, centerX, k);
		float newcenterX = width - centerX;
		float xshift_2 = calc_shift(0, newcenterX - 1, newcenterX, k);

		yshift = calc_shift(0, centerY - 1, centerY, k);
		float newcenterY = height - centerY;
		float yshift_2 = calc_shift(0, newcenterY - 1, newcenterY, k);

		xscale = (width - xshift - xshift_2) / width;
		yscale = (height - yshift - yshift_2) / height;
		for (int j = 0; j < dst.getHeight(); j++) {
			for (int i = 0; i < dst.getWidth(); i++) {
				
				offset = i*width + j;
				x = (i * xscale + xshift);
				y = (j * yscale + yshift);
				x = x + ((x - centerX) * k * ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)));
				
				x = (i * xscale + xshift);
				y = (j * yscale + yshift);
				y = y + ((y - centerY) * k * ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)));
				
				if (x < 0 || y < 0 || x > (height - 1)
						|| y > (width - 1)) {
					s[0] = 0;
					s[1] = 0;
					s[2] = 0;
					s[3] = 0;
				}else{
					
					float idx0_fl = (float) Math.floor(x);
					float idx0_cl = (float) Math.ceil(x);
					float idx1_fl = (float) Math.floor(y);
					float idx1_cl = (float) Math.ceil(y);
					
					int tmpOffset = (int) (idx0_fl*width + idx1_fl);
					s1[0] = (srcPixels[tmpOffset] >>> 24) & 0xFF;
					s1[1] = (srcPixels[tmpOffset] >>> 16) & 0xFF;
					s1[2] = (srcPixels[tmpOffset] >>> 8) & 0xFF;
					s1[3] = (srcPixels[tmpOffset] >>> 0) & 0xFF;
					
					tmpOffset = (int) (idx0_fl*width + idx1_cl);
					s2[0] = (srcPixels[tmpOffset] >>> 24) & 0xFF;
					s2[1] = (srcPixels[tmpOffset] >>> 16) & 0xFF;
					s2[2] = (srcPixels[tmpOffset] >>> 8) & 0xFF;
					s2[3] = (srcPixels[tmpOffset] >>> 0) & 0xFF;
					
					tmpOffset = (int) (idx0_cl*width + idx1_cl);
					s3[0] = (srcPixels[tmpOffset] >>> 24) & 0xFF;
					s3[1] = (srcPixels[tmpOffset] >>> 16) & 0xFF;
					s3[2] = (srcPixels[tmpOffset] >>> 8) & 0xFF;
					s3[3] = (srcPixels[tmpOffset] >>> 0) & 0xFF;
					
					tmpOffset = (int) (idx0_cl*width + idx1_cl);
					s4[0] = (srcPixels[tmpOffset] >>> 24) & 0xFF;
					s4[1] = (srcPixels[tmpOffset] >>> 16) & 0xFF;
					s4[2] = (srcPixels[tmpOffset] >>> 8) & 0xFF;
					s4[3] = (srcPixels[tmpOffset] >>> 0) & 0xFF;
					
					x = x - idx0_fl;
					y = y - idx1_fl;

					s[0] = (int) (s1[0] * (1 - x) * (1 - y) + s2[0] * (1 - x) * y + s3[0]
							* x * y + s4[0] * x * (1 - y));
					s[1] = (int) (s1[1] * (1 - x) * (1 - y) + s2[1] * (1 - x) * y + s3[1]
							* x * y + s4[1] * x * (1 - y));
					s[2] = (int) (s1[2] * (1 - x) * (1 - y) + s2[2] * (1 - x) * y + s3[2]
							* x * y + s4[2] * x * (1 - y));
					s[3] = (int) (s1[3] * (1 - x) * (1 - y) + s2[3] * (1 - x) * y + s3[3]
							* x * y + s4[3] * x * (1 - y));
				}
				
				
				int color = ((s[1] & 0x0ff) << 16) | ((s[2] & 0x0ff) << 8)
						| (s[3] & 0x0ff);

				dstPixels[offset] = color;
			}
		}
		
		dst.setPixels(dstPixels, 0, width, 0, 0, width, height);
		return dst;

	}
	public static float thresh = 1;
	public static float calc_shift(float x1, float x2, float cx, float k) {
		float x3 = (float) (x1 + (x2 - x1) * 0.5);
		float res1 = x1 + ((x1 - cx) * k * ((x1 - cx) * (x1 - cx)));
		float res3 = x3 + ((x3 - cx) * k * ((x3 - cx) * (x3 - cx)));

		if (res1 > -thresh && res1 < thresh)
			return x1;
		if (res3 < 0) {
			return calc_shift(x3, x2, cx, k);
		} else {
			return calc_shift(x1, x3, cx, k);
		}
	}

	
}
