package com.example.app_2.storage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;


import com.example.app_2.App_2;


public class Storage {
	private static final String LOG_TAG = "Storage";
	
	public static final String IMG_DIR = "images";				// pobrane obrazki
	public static final String IMG_THUBS = "thumbs";    		// miniatury obrazków
	public static final String IMG_THUMBS_MAX = "thumbs_max";	// miniatury obrazków które siê mieszcz¹ na ca³ym ekranie tablety/smartfona
	public static final String TEMP = "temp";
	public static final int IO_BUFFER_SIZE = 8 * 1024;
	
	/**
	 * AppRootDirecotory is /Android/data/[packageName]/files/
	 * @return File to application root directory
	 */
	public static File getAppRootDir() {
		Context c = App_2.getAppContext();
		if (ExternalStorage.isExternalStorageReadable() || ExternalStorage.hasExternalCacheDir()) {
			return c.getExternalFilesDir(null);
		} else {
			final String cacheDir = "/Android/data/" + c.getPackageName()+ "/files/";
			return new File(Environment.getExternalStorageDirectory().getPath()	+ cacheDir);
		}
	}
	
	public static File createImageFile() {

		String JPEG_FILE_PREFIX = "img";
		String JPEG_FILE_SUFIX = ".jpg";
		// create a image file name
		String timeStamp = new SimpleDateFormat("yyyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		try {
			File image = File.createTempFile(imageFileName, JPEG_FILE_SUFIX,getImagesDir());
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static File createTempImageFile(){
		String JPEG_FILE_PREFIX = "tmp";
		String JPEG_FILE_SUFIX = ".jpg";
		// create a image file name
		String timeStamp = new SimpleDateFormat("yyyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		try {
			File image = File.createTempFile(imageFileName, JPEG_FILE_SUFIX,getTempDir());
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public static void galleryAddPic(Activity a, String path) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(path);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		a.sendBroadcast(mediaScanIntent);
	}

	
	
	public static File isfileExist(String filename, File path) {
		File file = new File(path.getAbsoluteFile() + "/" + filename);
		if (file.exists())
			return file;
		else
			return null;
	}

	public static File isfileExist(String path) {
		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (file.exists())
			return file;
		else
			return null;
	}

	public static File getImagesDir(){
		return getsDir(IMG_DIR);
		}
	

	
	public static File getThumbsDir(){
		return getsDir(IMG_THUBS);
		}
	
	public static File getThumbsMaxDir(){
		return getsDir(IMG_THUMBS_MAX);
	}
	
	public static File getTempDir(){
		return getsDir(TEMP);
	}
	public static File getsDir(String dir) {
		File file = new File(getAppRootDir().getAbsolutePath() + File.separator	+ dir);
		if(file.exists()){
			return file;
		}
		if (!file.mkdir()) {
			Log.e(LOG_TAG, "Directory:"+file.getAbsolutePath()+" not created");
		}
		return file;
	}
	
	public static List<File> getFilesListFromDir(File dir){
		if(!dir.exists()){
			Log.w(LOG_TAG,dir.getAbsolutePath()+" not exists");
			return null;
		}
		if(!dir.isDirectory()){
			Log.w(LOG_TAG,dir.getAbsolutePath()+" is not a directory");
			return null;
		}
		
		File[] ftab = dir.listFiles();
		List<File> fileList = new LinkedList<File>();
		for(File f: ftab){
			if(f.isFile())
				fileList.add(f);
		}
		return fileList;
	}
	
	public static List<String> getFilesNamesFromDir(File dir){
		List<File> fl = getFilesListFromDir(dir);
		List<String> fp = new LinkedList<String>();
		for(File f:fl){
			fp.add(f.getName());
		}
		return fp;		
	}
	
	public static List<String> getChangedFilesFromDir(File dir){
		List<File> fl = getFilesListFromDir(dir);
		List<String> cfl = new LinkedList<String>();
		Long img_dir_last_read = Long.valueOf(Storage.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead", "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE));
		for(File f:fl){
			if(f.lastModified()>img_dir_last_read)
				cfl.add(f.getName());
		}
		return cfl;
	}
	
	/**
	 * 	 Check if media is mounted or storage is built-in, if so, try and use
	 *	 external cache dir
	 *	 otherwise use internal cache dir
	 * @param subDir
	 * @return
	 */
	public static File getDiskCacheDir(String subDir) {
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable() ? ExternalStorage.getExternalCacheDir()
				.getPath() : App_2.getAppContext().getCacheDir().getPath();
		return new File(cachePath + File.separator + subDir);
	}
	
	/* SHARED PREFERENCES ------------------------------------------------------------------------------------------*/
	public static void saveToSharedPreferences(String prefName, String value,
			String key, Context context, int mode) {
		SharedPreferences sharedPref = context.getSharedPreferences(prefName,
				mode);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void saveToPreferences(String value, String key,
			Activity activity, int mode) {
		SharedPreferences sharedPref = activity.getPreferences(mode);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String readFromSharedPreferences(String defValue,
			String prefName, String key, Context context, int mode) {
		SharedPreferences sharedPref = context.getSharedPreferences(prefName,
				mode);
		String value = sharedPref.getString((key), defValue);
		return value;
	}

	public static String readFromPreferences(String defValue, String key,
			Activity activity, int mode) {
		SharedPreferences sharedPref = activity.getPreferences(mode);
		String value = sharedPref.getString((key), defValue);
		return value;
	}

}
