package com.example.app_2.utils;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
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
	
	private static DiskLruImageCache mDiskLruCache;
	private final static Object mDiskCacheLock = new Object();
	private static boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
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
		
		/* INITIALIZE DISK CACHE */
	  //  File cacheDir = Storage.getDiskCacheDir(DISK_CACHE_SUBDIR);
	  //  new InitDiskCacheTask().execute(cacheDir);
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
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
	public static void loadBitmap(String path, ImageView imageView, boolean darkPlaceholder){
		if(path == null){
			return;
		}
		Bitmap value = null;
		if(mMemoryCache!= null){
			value = mMemoryCache.get(Utils.getFilenameFromPath(path));
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
	    
	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
    		options.inPurgeable = true;
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
	            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, mWidth, mHeight, ScalingLogic.FIT);

	            // Part 2: Scale image
	            if(unscaledBitmap != null){
	            	bitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, mWidth, mHeight, ScalingLogic.FIT);
		            unscaledBitmap.recycle();
	            }
	            else
	            	Log.e(LOG_TAG, "bitmap missing, path:" + path);

	        	if(bitmap == null)
	        		return null;
	        	
	        	Log.i(LOG_TAG,bitmap.toString()+" decoded image h:"+bitmap.getHeight()+" w:"+bitmap.getWidth());
		        addBitmapToCache(imageKey,bitmap);
	        return bitmap;
	    }
	    
	    public void addBitmapToCache(String key, Bitmap bitmap) {
	    	if(key !=null && bitmap != null){
		        if (getBitmapFromMemCache(key) == null) {
		            mMemoryCache.put(key, bitmap);
		        }
	        }
	    }

	    public Bitmap getBitmapFromDiskCache(String key) {
	        synchronized (mDiskCacheLock) {
	            // Wait while disk cache is started from background thread
	            while (mDiskCacheStarting) {
	                try {
	                    mDiskCacheLock.wait();
	                } catch (InterruptedException e) {}
	            }
	            if (mDiskLruCache != null) {
	                return mDiskLruCache.getBitmap(key);
	            }
	        }
	        return null;
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

	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	            mDiskLruCache = new DiskLruImageCache(cacheDir, DISK_CACHE_SIZE, CompressFormat.JPEG, 100);
	            mDiskCacheStarting = false; // Finished initialization
	            mDiskCacheLock.notifyAll(); // Wake any waiting threads
	        }
	        return null;
	    }
	}
	

	public static void setImageSize(int height) {
		mWidth= height;
		mHeight = height;		
	}


}
