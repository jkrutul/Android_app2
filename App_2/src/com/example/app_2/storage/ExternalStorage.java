package com.example.app_2.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.app_2.App_2;

public class ExternalStorage extends Storage{
	private static final String LOG_TAG = "ExternalStorage";

	public static boolean isExternalStorageWirtable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)	|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	public static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}
	
	public static File getExternalStorageDir(String filename) {
		File file = new File(Environment.getExternalStorageDirectory(),	filename);
		boolean success = false;

		if (file.exists()) {
			Log.i(LOG_TAG, "Directory: " + file.getAbsolutePath() + " already exists");
		} else {
			success = file.getParentFile().mkdirs();
			if (success) {
				Log.i(LOG_TAG, "Directory: " + file.getAbsolutePath() + " created successfully");
			} else {
				Log.i(LOG_TAG, "Directory: " + file.getAbsolutePath() + " not created");
			}
		}
		return file;
	}

	public static File getExternalPublicStorageDir(String filename, String state) {
		File sdpath = Environment.getExternalStoragePublicDirectory(state);
		File file = new File(sdpath + File.separator + filename);
		if (!file.getParentFile().mkdirs()) {
			Log.e(LOG_TAG, "Directory not created");
		}
		return file;
	}

	public static File getExternalCacheDir() {
		Context context = App_2.getAppContext();
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}
		Log.w(LOG_TAG, "ON FROYO");
		final String cacheDir = "/Android/data/" + context.getPackageName()	+ "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath()	+ cacheDir);
	}


}
