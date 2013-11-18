package com.example.app_2.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ImageScalingService extends Service{
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}



}
