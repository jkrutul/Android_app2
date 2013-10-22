package com.example.app_2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;

import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.utils.BitmapCalc;


public class App_2 extends Application{
	private static Context context;
	//public static ImageGridActivity actvity;
	public static Bitmap mPlaceHolderBitmap;
	public static Bitmap mDarkPlaceHolderBitmap;

	
	public void onCreate(){
		super.onCreate();
		App_2.context = getApplicationContext();
		mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.empty_photo, 100, 100);
		mDarkPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.dark_empty, 100, 100);
	}
	
	public static Context getAppContext(){
		return App_2.context;
	}
	

}
