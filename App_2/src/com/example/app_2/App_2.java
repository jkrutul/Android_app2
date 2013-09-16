package com.example.app_2;

import android.app.Activity;
import android.app.Application;
import android.content.Context;


public class App_2 extends Application{
	private static Context context;
	public static Activity actvity;
	
	public void onCreate(){
		super.onCreate();
		App_2.context = getApplicationContext();
	}
	
	public static Context getAppContext(){
		return App_2.context;
	}

}
