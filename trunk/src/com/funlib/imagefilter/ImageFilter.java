package com.funlib.imagefilter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

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
				Bitmap.Config.ARGB_8888);

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

	public enum EffectType {
		ChannelBlend_Normal, 
		ChannelBlend_Lighten, 
		ChannelBlend_Darken, 
		ChannelBlend_Multiply,
		ChannelBlend_Average,
		ChannelBlend_Add,
		ChannelBlend_Subtract,
		ChannelBlend_Difference,
		ChannelBlend_Negation,
		ChannelBlend_Screen,
		ChannelBlend_Exclusion,
		ChannelBlend_Overlay,
		ChannelBlend_SoftLight,
		ChannelBlend_HardLight,
		ChannelBlend_ColorDodge,
		ChannelBlend_ColorBurn,
		ChannelBlend_LinearDodge,
		ChannelBlend_LinearBurn,
		ChannelBlend_LinearLight,
		ChannelBlend_VividLight,
		ChannelBlend_PinLight,
		ChannelBlend_HardMix,
		ChannelBlend_Reflect,
		ChannelBlend_Glow,
		ChannelBlend_Phoenix,
		ChannelBlend_Alpha,
		ChannelBlend_Devide,
	}
	
	public static int getEffectFun(int src, int mask, EffectType effectFun, float maskAlpha){
		switch (effectFun) {
		case ChannelBlend_Normal:
			return src;
		case ChannelBlend_Lighten:
			return ps_lightenFun(src, mask);
		case ChannelBlend_Darken:
			return ps_darkenFun(src, mask);
		case ChannelBlend_Multiply:
			return ps_multiplyFun(src, mask);
		case ChannelBlend_Average:
			return ps_averageFun(src, mask);
		case ChannelBlend_Add:
			return ps_addFun(src, mask);
		case ChannelBlend_Subtract:
			return ps_subtractFun(src, mask);
		case ChannelBlend_Difference:
			return ps_differenceFun(src, mask);
		case ChannelBlend_Negation:
			return ps_negationFun(src, mask);
		case ChannelBlend_Screen:
			return ps_screenFun(src, mask);
		case ChannelBlend_Exclusion:
			return ps_ExclusionFun(src, mask);
		case ChannelBlend_Overlay:
			return ps_overlayFun(src, mask);
		case ChannelBlend_SoftLight:
			return ps_softlightFun(src, mask);
		case ChannelBlend_HardLight:
			return ps_hardlightFun(src, mask);
		case ChannelBlend_ColorDodge:
			return ps_ColorDodgeFun(src, mask);
		case ChannelBlend_ColorBurn:
			return ps_ColorBurnFun(src, mask);
		case ChannelBlend_LinearDodge:
			return ps_LinearDodgeFun(src, mask);
		case ChannelBlend_LinearBurn:
			return ps_LinearBurnFun(src, mask);
		case ChannelBlend_LinearLight:
			return ps_LinearLightFun(src, mask);
		case ChannelBlend_VividLight:
			return ps_VividLightFun(src, mask);
		case ChannelBlend_PinLight:
			return ps_PinLightFun(src, mask);
		case ChannelBlend_HardMix:
			return ps_HardMixFun(src, mask);
		case ChannelBlend_Reflect:
			return ps_ReflectFun(src, mask);
		case ChannelBlend_Glow:
			return ps_GlowFun(src, mask);
		case ChannelBlend_Phoenix:
			return ps_PhoenixFun(src, mask);
		case ChannelBlend_Alpha:
			return ps_AlphaFun(src, mask, maskAlpha);
		case ChannelBlend_Devide:
			return ps_DevideFun(src, mask);
		default:
			break;
		}
		return 0;
	}
	
	public static Bitmap ps_getEffectBitmap(EffectType iType, Bitmap src, Bitmap mask, float maskAlpha) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
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

				index = width * i + k;
				pixColorSrc = pixelsSrc[index];
				pixColorMask = pixelsMask[index];
				RSrc = Color.red(pixColorSrc);
				GSrc = Color.green(pixColorSrc);
				BSrc = Color.blue(pixColorSrc);
				RMask = Color.red(pixColorMask);
				GMask = Color.green(pixColorMask);
				BMask = Color.blue(pixColorMask);

				RSrc = getEffectFun(RSrc, RMask, iType, maskAlpha);
				if (maskAlpha > Float.MIN_VALUE) {
					RSrc = getEffectFun(RSrc, RMask, EffectType.ChannelBlend_Alpha, maskAlpha);
				}
				GSrc = getEffectFun(GSrc, GMask, iType, maskAlpha);
				if (maskAlpha > Float.MIN_VALUE) {
					GSrc = getEffectFun(GSrc, GMask, EffectType.ChannelBlend_Alpha, maskAlpha);
				}
				BSrc = getEffectFun(BSrc, BMask, iType, maskAlpha);
				if (maskAlpha > Float.MIN_VALUE) {
					BSrc = getEffectFun(BSrc, BMask, EffectType.ChannelBlend_Alpha, maskAlpha);
				}
				pixelsSrc[index] = Color.argb(255, RSrc, GSrc, BSrc);
			}
		}

		bitmap.setPixels(pixelsSrc, 0, width, 0, 0, width, height);
		return bitmap;
	}

	private static int ps_lightenFun(int src, int mask) {
		return mask > src ? mask : src;
	}

	private static int ps_darkenFun(int src, int mask) {
		return mask > src ? src : mask;
	}

	private static int ps_multiplyFun(int src, int mask) {
		return mask * src / 255;
	}

	private static int ps_averageFun(int src, int mask) {
		return (src + mask) >> 1;
	}

	private static int ps_addFun(int src, int mask) {
		return (src + mask > 255) ? 255 : src + mask;
	}

	private static int ps_subtractFun(int src, int mask) {
		return (src + mask < 255) ? 0 : (src + mask - 255);
	}

	private static int ps_differenceFun(int src, int mask) {
		return (src > mask) ? (src - mask) : (mask - src);
	}

	private static int ps_negationFun(int src, int mask) {
		return (src + mask > 255) ? (src + mask) : (255 + 255 - src - mask);
	}

	private static int ps_screenFun(int src, int mask) {
		return 255 - (((255 - src) * (255 - mask)) >> 8);
	}

	private static int ps_ExclusionFun(int src, int mask) {
		return ((int)(src + mask - 2 * src * mask / 255));
	}

	private static int ps_overlayFun(int src, int mask) {
		return ((int) ((mask < 128) ? (2 * src * mask / 255) : (255 - 2
				* (255 - src) * (255 - mask) / 255)));
	}

	private static int ps_softlightFun(int src, int mask) {
		return (int) (((src < 128) ? (2 * ((mask >> 1) + 64))
				* ((float) src / 255) : (255 - (2 * (255 - ((mask >> 1) + 64))
				* (float) (255 - src) / 255))));
	}

	private static int ps_hardlightFun(int src, int mask) {
		return (ps_overlayFun(mask, src));
	}

	private static int ps_ColorDodgeFun(int src, int mask) {
		return ((int) ((mask == 255) ? mask : Math.min(255,
				((src << 8) / (255 - mask)))));
	}

	private static int ps_ColorBurnFun(int src, int mask) {
		return ((int) ((mask == 0) ? mask : Math.max(0,
				(255 - ((255 - src) << 8) / mask))));
	}

	private static int ps_LinearDodgeFun(int src, int mask) {
		return ps_addFun(src, mask);
	}

	private static int ps_LinearBurnFun(int src, int mask) {
		return ps_subtractFun(src, mask);
	}

	private static int ps_LinearLightFun(int src, int mask) {
		return ((int) ((mask < 128) ? ps_LinearBurnFun(src, (2 * mask))
				: ps_LinearDodgeFun(src, (2 * (mask - 128)))));
	}

	private static int ps_VividLightFun(int src, int mask) {
		return ((int) ((mask < 128) ? ps_ColorBurnFun(src, (2 * mask))
				: ps_ColorDodgeFun(src, (2 * (mask - 128)))));
	}

	private static int ps_PinLightFun(int src, int mask) {
		return ((int) ((mask < 128) ? ps_darkenFun(src, (2 * mask))
				: ps_lightenFun(src, (2 * (mask - 128)))));
	}

	private static int ps_HardMixFun(int src, int mask) {
		return ((int) ((ps_VividLightFun(src, mask) < 128) ? 0 : 255));
	}

	private static int ps_ReflectFun(int src, int mask) {
		return ((int) ((mask == 255) ? mask : Math.min(255,
				(src * src / (255 - mask)))));
	}

	private static int ps_GlowFun(int src, int mask) {
		return ps_ReflectFun(mask, src);
	}

	private static int ps_PhoenixFun(int src, int mask) {
		return ((int) (Math.min(src, mask) - Math.max(src, mask) + 255));
	}

	private static int ps_AlphaFun(int src, int mask, float maskAlpha) {
		return ((int) (maskAlpha * src + (1 - maskAlpha) * mask));
	}

	private static int ps_DevideFun(int src, int mask) {
		return Math.min(255, (int)(src/((float)(mask)/255)));
	}
}
