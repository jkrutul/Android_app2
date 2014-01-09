package com.example.app_2.utils;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.storage.DiskLruImageCache;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;


public class ImageLoader {
	private static String LOG_TAG = "ImageLoader";
	public static int  mWidth=100;
	public static int mHeight=100;
	int maxWidth;
	int maxHeight;

	
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);


	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;

	private static LruCache<String, Bitmap> mMemoryCache;
	
	public ImageLoader(Context context) {
		//context = App_2.getAppContext();
		//full screen thumbs
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		maxWidth = display.getWidth();
		maxHeight = display.getHeight();		

		/* INITIALIZE MEMORY CACHE */
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
		@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
				
			}
		};
		

	}

	public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if(key!=null && bitmap != null){
			if (getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}else
			Log.w(LOG_TAG, "key or bitmap is null");

	}

	public static Bitmap getBitmapFromMemCache(String key) {
		if(key!=null){
			return mMemoryCache.get(key);
		}
		else{
			Log.w(LOG_TAG, "key is null");
			return null;
		}

	}
	
	@SuppressLint("NewApi")
	public static void loadBitmap(String path, ImageView imageView){
		if(path == null){
			return;
		}
		Bitmap value = null;
		if(mMemoryCache!= null){
			value = getBitmapFromMemCache(Utils.getFilenameFromPath(path));
			//value = mMemoryCache.get(Utils.getFilenameFromPath(path));
		}
		if(value != null){
			imageView.setImageBitmap(value);
		}else if(cancelPotentialWork(path, imageView)){
			BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			AsyncDrawable asyncDrawable = new AsyncDrawable(App_2.getAppContext().getResources(), null, task);
			imageView.setImageDrawable(asyncDrawable);
			task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, path);

		}			
	}

	
	public static boolean cancelPotentialWork(String path, ImageView imageView) {
	     BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
	   
	    if (bitmapWorkerTask != null) {
	        final String bitmapPath = bitmapWorkerTask.path;
	        if (bitmapPath != path) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	            
	        } else {
	            // The same work is already in progress
	        	Log.i("cancelPotentialWork", "path: "+path);
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static  BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		   if (imageView != null) {
		       final Drawable drawable = imageView.getDrawable();
		       if (drawable instanceof AsyncDrawable) {
		           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
		           return asyncDrawable.getBitmapWorkerTask();
		       }
		    }
		    return null;
		}
	
	public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private String path = null;
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    ScalingLogic sl = ScalingLogic.FIT;
	    
	    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(App_2.getAppContext());
	
	    
	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
    		options.inPurgeable = true;
    	    if(sharedPref.getBoolean("pref_img_crop", false))
    	    	sl =  ScalingLogic.CROP;
    	    else
    	    	sl = ScalingLogic.FIT;
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(String... params) {
	        path = params[0];
	        if(path== null)
	        	return null;
	        final String imageKey = Utils.getFilenameFromPath(path);
	        Bitmap bitmap = null;

	            // Part 1: Decode image
	            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, mWidth, mHeight, sl);

	            // Part 2: Scale image
	            if(unscaledBitmap != null){
	            	bitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, mWidth, mHeight, sl);
		            unscaledBitmap.recycle();
	            }
	            else
	            	Log.e(LOG_TAG, "bitmap missing, path:" + path);

	        	if(bitmap == null)
	        		return null;
	        	
	        	//Log.i(LOG_TAG, "filename: "+ path +" decoded image h:"+bitmap.getHeight()+" w:"+bitmap.getWidth());
	        	
		        addBitmapToMemoryCache(imageKey,bitmap);
	        return bitmap;
	    }
	    	    
	    private void setImageDrawable(ImageView imageView, Bitmap bitmap) {
	    	BitmapDrawable bd  =new BitmapDrawable(App_2.getAppContext().getResources(), bitmap);
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent),  bd});
            // Set background to loading bitmap

           // imageView.setBackgroundDrawable(new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(200);
    }


	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	            	setImageDrawable(imageView, bitmap);
	                //imageView.setImageBitmap(bitmap);
	            }
	        }
	    }
	}
	
	
	 static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap,BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}

	

	public static void setImageSize(int height) {
		mWidth= height;
		mHeight = height;		
	}


}
