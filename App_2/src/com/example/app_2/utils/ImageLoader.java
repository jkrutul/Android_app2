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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.storage.DiskLruImageCache;


public class ImageLoader {
	private static String LOG_TAG = "ImageLoader";
	private Context context;
	private Bitmap mPlaceHolderBitmap;
	
	private DiskLruImageCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private static final String DISK_CACHE_SUBDIR = "thumbnails";
	public static int  mWidth=100, mHeight=100;
	
	
	
	// Get max available VM memory, exceeding this amount will throw an
	// OutOfMemory exception. Stored in kilobytes as LruCache takes an
	// int in its constructor.
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);


	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory / 8;

	private LruCache<String, Bitmap> mMemoryCache;
	
	public ImageLoader() {
		context = App_2.getAppContext();
		mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.image_placeholder, 100, 100);
		
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

	public Bitmap getBitmapFromMemCache(String key) {
		if(key!=null){
			return mMemoryCache.get(key);
		}
		else{
			Log.w(LOG_TAG, "key is null");
			return null;
		}

	}
	
/*
	public void loadBitmap(int resId, ImageView imageView) {
	    if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        final AsyncDrawable asyncDrawable =   new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(resId);
	    }
	}
*/
	
	@SuppressLint("NewApi")
	public void loadBitmap(String path, ImageView imageView){
		if(cancelPotentialWork(path, imageView)){
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
		}
	}
	
	
	public static boolean cancelPotentialWork(String path, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final String bitmapPath = bitmapWorkerTask.path;
	        if (bitmapPath != path) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		   if (imageView != null) {
		       final Drawable drawable = imageView.getDrawable();
		       if (drawable instanceof AsyncDrawable) {
		           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
		           return asyncDrawable.getBitmapWorkerTask();
		       }
		    }
		    return null;
		}
	
	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private String path = null;

	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(String... params) {
	        path = params[0];	        
	        final String imageKey = String.valueOf(path);
	        Bitmap bitmap = mMemoryCache.get(imageKey);
	        if(bitmap ==null)
	        // Check disk cache in background thread
	        //Bitmap bitmap = getBitmapFromDiskCache(imageKey);
	        //if (bitmap == null) { // Not found in disk cache
	        	//bitmap = BitmapCalc.decodeSampleBitmapFromFile(path, mWidth, mHeight);
	        	bitmap = BitmapFactory.decodeFile(path);
	        	Log.i(LOG_TAG,"decoded image h:"+bitmap.getHeight()+" w:"+bitmap.getWidth());
	        //}
	        // add final bitmap to caches
	        addBitmapToCache(imageKey,bitmap);
	        return bitmap;
	    }
	    public void addBitmapToCache(String key, Bitmap bitmap) {
	    	if(key !=null && bitmap != null){
		        // Add to memory cache as before
		        if (getBitmapFromMemCache(key) == null) {
		            mMemoryCache.put(key, bitmap);
		        }
	
		        // Also add to disk cache
		        /*
		        synchronized (mDiskCacheLock) {
		            if (mDiskLruCache != null && mDiskLruCache.getBitmap(key) == null) {
		                mDiskLruCache.put(key, bitmap);
		            }
		        }
		        */
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
	                imageView.setImageBitmap(bitmap);
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
