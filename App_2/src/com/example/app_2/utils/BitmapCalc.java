package com.example.app_2.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapCalc {
	private final static String LOG_TAG = "BitmapCalc";
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
	// Raw height and width of image
		
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if(height >reqHeight || width > reqWidth){
			
			// Calculate ratios of height and width to request height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			
			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			if((inSampleSize==1)&&(height > 1000 || width>1000)){
				inSampleSize=2;			
			}
		}
		Log.w(LOG_TAG, "outHeight: "+options.outHeight + " outWidth:"+options.outWidth +" rH"+ reqHeight +" rW"+ reqWidth +" inSample:" +inSampleSize);
		return inSampleSize;
	}
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}

	public static Bitmap decodeSampleBitmapFromResources(Resources res, int resId, int reqWidth, int reqHeight){
		
		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId,options);
		
		// Calculate in SampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap decodeSampleBitmapFromFile(String filePath,int reqWidth, int reqHeight){
		
		// First decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		
		// Calculate in SampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(filePath, options);
	}
}
