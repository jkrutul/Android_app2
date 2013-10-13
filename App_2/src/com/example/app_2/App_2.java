package com.example.app_2;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;

import com.example.app_2.utils.BitmapCalc;
import com.examples.app_2.activities.ImageGridActivity;


public class App_2 extends Application{
	private static Context context;
	//public static ImageGridActivity actvity;
	public static Bitmap mPlaceHolderBitmap;

	
	public void onCreate(){
		super.onCreate();
		App_2.context = getApplicationContext();
		mPlaceHolderBitmap = BitmapCalc.decodeSampleBitmapFromResources(App_2.getAppContext().getResources(), R.drawable.empty_photo, 100, 100);
	}
	
	public static Context getAppContext(){
		return App_2.context;
	}
	

}
