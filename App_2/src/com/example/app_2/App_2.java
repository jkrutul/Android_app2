package com.example.app_2;

import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.utils.BitmapCalc;


public class App_2 extends Application{
	private static Context context;
	//public static ImageGridActivity actvity;
	public static Bitmap mPlaceHolderBitmap;
	public static Bitmap mDarkPlaceHolderBitmap;
	public static Drawable wallpaperDrawable;
	public static Bitmap wallpaperBitmap;
	public static int maxWidth;
	public static int maxHeight; 

	
	public void onCreate(){
		super.onCreate();
		App_2.context = getApplicationContext();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		maxWidth = display.getWidth();
		maxHeight = display.getHeight();
		mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.empty_photo, 100, 100);
		mDarkPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.dark_empty, 100, 100);
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
		wallpaperDrawable = wallpaperManager.getDrawable();
	}
	
	public static Context getAppContext(){
		return App_2.context;
	}
	
	public static int getMaxWidth(){
		return maxWidth;
	}
	
	public static int getMaxHeight(){
		return maxHeight;
	}
	

}
