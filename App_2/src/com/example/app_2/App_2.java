package com.example.app_2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.storage.Database;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;


public class App_2 extends Application{
	private static Context context;
	//public static Bitmap mPlaceHolderBitmap;
	//public static Bitmap mDarkPlaceHolderBitmap;
	public static int maxWidth;
	public static int maxHeight; 
	private static Long main_dict_id;

	
	public void onCreate(){
		super.onCreate();
		
		App_2.context = getApplicationContext();
		ImageLoader il = new ImageLoader(context);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		maxWidth = display.getWidth();
		maxHeight = display.getHeight();
		//mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.empty_photo, 100, 100);
		//mDarkPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.dark_empty, 100, 100);
		

		Database db = Database.getInstance(context);
		Database.open();
		
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

	public static Long getMain_dict_id() {
		return main_dict_id;
	}

	public static void setMain_dict_id(Long main_dict_id) {
		App_2.main_dict_id = main_dict_id;
	}
	


}
