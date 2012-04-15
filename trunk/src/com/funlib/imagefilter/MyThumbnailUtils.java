package com.funlib.imagefilter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import com.funlib.log.FLog;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;

public class MyThumbnailUtils {
	private static final int UNCONSTRAINED = -1;

	/* Options used internally. */
	private static final int OPTIONS_NONE = 0x0;
	private static final int OPTIONS_SCALE_UP = 0x1;
	public static final int OPTIONS_RECYCLE_INPUT = 0x2;

	public static Bitmap scaleBitmapFromByte(byte[] datas,
			int width, int height) {
		
		if(width == 0 && height == 0){
			
			return BitmapFactory.decodeByteArray(datas, 0, datas.length);
		}
		
		int targetSize = Math.min(width, height);
		int maxPixels = width * height;
		Bitmap bitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(datas, 0, datas.length, options);
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			return null;
		}
		options.inSampleSize = computeSampleSize(options, targetSize, maxPixels);
		options.inJustDecodeBounds = false;

		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length, options);

		bitmap = extractThumbnail(bitmap, width, height, OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	public static Bitmap scaleBitmapFromFile(String filePath,
			int width, int height) {
		
		if(width == 0 && height == 0){
			
			return BitmapFactory.decodeFile(filePath);
		}
		
		int targetSize = Math.min(width, height);
		int maxPixels = width * height;
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			return null;
		}
		options.inSampleSize = computeSampleSize(options, targetSize,
				maxPixels);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		bitmap = BitmapFactory.decodeFile(filePath, options);

		bitmap = extractThumbnail(bitmap, width, height, OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	public static Bitmap scaleBitmapFromBitmap(Bitmap source, int width, int height) {
		return extractThumbnail(source, width, height, OPTIONS_NONE);
	}

	/**
	 * Creates a centered bitmap of the desired size.
	 * 
	 * @param source
	 *            original bitmap source
	 * @param width
	 *            targeted width
	 * @param height
	 *            targeted height
	 * @param options
	 *            options used during thumbnail extraction
	 */
	private static Bitmap extractThumbnail(Bitmap source, int width, int height,
			int options) {
		if (source == null) {
			return null;
		}

		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = width / (float) source.getWidth();
		} else {
			scale = height / (float) source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap thumbnail = transform(matrix, source, width, height,
				OPTIONS_SCALE_UP | options);
		return thumbnail;
	}

	/*
	 * Compute the sample size as a function of minSideLength and
	 * maxNumOfPixels. minSideLength is used to specify that minimal width or
	 * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
	 * pixels that is tolerable in terms of memory usage.
	 * 
	 * The function returns a sample size based on the constraints. Both size
	 * and minSideLength can be passed in as IImage.UNCONSTRAINED, which
	 * indicates no care of the corresponding constraint. The functions prefers
	 * returning a sample size that generates a smaller bitmap, unless
	 * minSideLength = IImage.UNCONSTRAINED.
	 * 
	 * Also, the function rounds up the sample size to a power of 2 or multiple
	 * of 8 because BitmapFactory only honors sample size this way. For example,
	 * BitmapFactory downsamples an image by 2 even though the request is 3. So
	 * we round up the sample size to avoid OOM.
	 */
	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * Transform source Bitmap to targeted width and height.
	 */
	private static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, int options) {
		boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
		boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		double rate1 = ((double) srcWidth) / (double) targetWidth + 0.1;
		double rate2 = ((double) srcHeight) / (double) targetHeight + 0.1;
		// 根据缩放比率大的进行缩放控制
		double rate = rate1 > rate2 ? rate1 : rate2;
		int newWidth = (int) (((double) srcWidth) / rate);
		int newHeight = (int) (((double) srcHeight) / rate);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, false);
		if(targetHeight != newHeight || targetWidth != newWidth){
			
			Rect srcRect = new Rect(0, 0, newWidth, newHeight);
			int dstLeft = (targetWidth-newWidth)/2;
			int dstTop = (targetHeight-newHeight)/2;
			int dstRight = dstLeft + newWidth;
			int dstBottom = dstTop + newHeight;
			Rect dstRect = new Rect(dstLeft , dstTop , dstRight , dstBottom);
			Bitmap tmpBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(tmpBitmap);
			canvas.drawBitmap(scaledBitmap, srcRect, dstRect, new Paint());
			canvas.save();
			scaledBitmap.recycle();
			scaledBitmap = tmpBitmap;
		}
		if(recycle){
			source.recycle();
		}
		return scaledBitmap;
		
//		int deltaX = source.getWidth() - targetWidth;
//		int deltaY = source.getHeight() - targetHeight;
//		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
//			/*
//			 * In this case the bitmap is smaller, at least in one dimension,
//			 * than the target. Transform it by placing as much of the image as
//			 * possible into the target and leaving the top/bottom or left/right
//			 * (or both) black.
//			 */
//			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
//					Bitmap.Config.ARGB_8888);
//			Canvas c = new Canvas(b2);
//
//			int deltaXHalf = Math.max(0, deltaX / 2);
//			int deltaYHalf = Math.max(0, deltaY / 2);
//			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
//					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
//					+ Math.min(targetHeight, source.getHeight()));
//			int dstX = (targetWidth - src.width()) / 2;
//			int dstY = (targetHeight - src.height()) / 2;
//			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
//					- dstY);
//			c.drawBitmap(source, src, dst, null);
//			if (recycle) {
//				source.recycle();
//			}
//			return b2;
//		}
//		float bitmapWidthF = source.getWidth();
//		float bitmapHeightF = source.getHeight();
//
//		float bitmapAspect = bitmapWidthF / bitmapHeightF;
//		float viewAspect = (float) targetWidth / targetHeight;
//
//		if (bitmapAspect > viewAspect) {
//			float scale = targetHeight / bitmapHeightF;
//			if (scale < .9F || scale > 1F) {
//				scaler.setScale(scale, scale);
//			} else {
//				scaler = null;
//			}
//		} else {
//			float scale = targetWidth / bitmapWidthF;
//			if (scale < .9F || scale > 1F) {
//				scaler.setScale(scale, scale);
//			} else {
//				scaler = null;
//			}
//		}
//
//		Bitmap b1;
//		if (scaler != null) {
//			// this is used for minithumb and crop, so we want to filter here.
//			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
//					source.getHeight(), scaler, true);
//		} else {
//			b1 = source;
//		}
//
//		if (recycle && b1 != source) {
//			source.recycle();
//		}
//
//		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
//		int dy1 = Math.max(0, b1.getHeight() - targetHeight);
//
//		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
//				targetHeight);
//
//		if (b2 != b1) {
//			if (recycle || b1 != source) {
//				b1.recycle();
//			}
//		}
//
//		return b2;
	}

}
