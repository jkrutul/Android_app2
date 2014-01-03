package com.example.app_2.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;


import com.example.app_2.App_2;
import com.example.app_2.utils.AsyncTask;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.Utils;


public class Storage {
	private static final String LOG_TAG = "Storage";
	
	//public static final String IMG_DIR = "images";				// pobrane obrazki
	public static final String IMG_THUBS = "thumbs";    		// miniatury obrazków
	//public static final String IMG_THUMBS_MAX = "thumbs_max";	// miniatury obrazków które siê mieszcz¹ na ca³ym ekranie tablety/smartfona
	public static final String TEMP = "temp";
	public static final int IO_BUFFER_SIZE = 8 * 1024;
	public static final int dev_width = App_2.getMaxWidth();
	public static final int dev_height = App_2.getMaxHeight();
	public static final int[] scaleTab = new int[]{1,4,8};
	
	
	

	
	
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
	
	/*
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
	*/
	
	public static File createTempImageFile(){
		String JPEG_FILE_PREFIX = "img";
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

	public static File isfileExist(String name) {
		File file = new File(Environment.getExternalStorageDirectory(), name);
		if (file.exists())
			return file;
		else
			return null;
	}
	
	/*
	public static File getImagesDir(){
		return getsDir(IMG_DIR);
		}
	

	
	public static File getThumbsDir(){
		return getsDir(IMG_THUBS);
		}
	
	public static File getThumbsMaxDir(){
		return getsDir(IMG_THUMBS_MAX);
	}
	*/
	public static File getScaledThumbsDir(String scale, boolean createIfNotExist){
		File file = new File(getAppRootDir().getAbsolutePath() + File.separator+ IMG_THUBS+ File.separator+ scale+ File.separator);
		if(file.exists())
			return file;
		else
			if(createIfNotExist){
				if (!file.mkdirs())
					Log.e(LOG_TAG, "Directory:"+file.getAbsolutePath()+" not created");
				else
					return file;
			}
		return null;
	}
	
	
	public static boolean removeThumbFile(String filename){
		LinkedList<File> filesToRemove = new LinkedList<File>();
		for(int scale :scaleTab){
			File fileToRemove=  new File (getScaledThumbsDir(Integer.toString(scale), false).getAbsoluteFile() + File.separator + filename);
			if(fileToRemove != null && fileToRemove.exists())
				filesToRemove.add(fileToRemove);
			
		}
		
		
		
		
		
		
		return false;
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
	public static void saveToSharedPreferences(String prefName, String value, String key, Context context, int mode) {
		SharedPreferences sharedPref = context.getSharedPreferences(prefName,mode);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void saveToPreferences(String value, String key, Activity activity, int mode) {
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

	public static String readFromPreferences(String defValue, String key,	Activity activity, int mode) {
		SharedPreferences sharedPref = activity.getPreferences(mode);
		String value = sharedPref.getString((key), defValue);
		return value;
	}

	
	public static String scaleAndSaveBitmapFromPath(String path_toIMG,  Bitmap.CompressFormat compressformat, int quality,Database db, boolean filenameUniqueVerification ){
		Bitmap bitmap;
		String last_saved_img_path = null;
		String filename = Utils.getFilenameFromPath(path_toIMG);
		
		for(int scale : scaleTab){
			File app_thumb_dir = Storage.getScaledThumbsDir(String.valueOf(scale),true);
			int new_w, new_h;
			new_w = (int) Math.floor(dev_width / scale);
			new_h = (int) Math.floor(dev_height / scale);
			if(last_saved_img_path==null)
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, new_w, new_h); // mo¿e byæ null
			else
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(last_saved_img_path, new_w, new_h);
			
			try {
				// sprawdzam czy plik o podanej nazwie nie istnieje w bazie 
				if(filenameUniqueVerification){
					while(db.filenameVerification(filename)){
						String ext = ".";
						ext+= Utils.getExtention(filename);
						filename = Utils.cutOnlyExtention(filename);
						filename+=("_"+ext);
					}
				}
				last_saved_img_path = app_thumb_dir+ File.separator+filename;
				FileOutputStream out = new FileOutputStream(last_saved_img_path);
				bitmap.compress(compressformat, quality, out);
				Log.i(LOG_TAG,"filename: " + path_toIMG +" saved w: "+new_w+" h: "+new_h);
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return filename;
		
	}

	public static Bitmap getScaledBitmap(String filename, int width){
		if( width == 0 )
			return null;
		
		
		File img_f;	
		int scale = (int) Math.floor(dev_width/width);
		do{
			String dir_to_scaled = Storage.getScaledThumbsDir(String.valueOf(scale), false).getAbsolutePath();
			if(dir_to_scaled!= null){										//sprawdzam czy istnieje ju¿ skalowany obrazek
				img_f = new File(dir_to_scaled + File.separator + filename);
				if(img_f.exists())
					return BitmapFactory.decodeFile(img_f.getAbsolutePath());
			}else
				scale/=2;
		}while(scale!=1);
		//brak obrazka!!!
		return null;
	}
	
	/**
	 * Returns path to scaled image
	 * @param filename
	 * @param width
	 * @return path
	 */
	
	public static String getPathToScaledBitmap(String filename, int width){
		if( width == 0 )
			return null;
		File img_f = null;
		int scale = (int) Math.floor(dev_width/width);
		Log.i(LOG_TAG, "scale request: " + scale);
		scale  = (scale >= 8) ? 8: scale;
		do{
			File f = Storage.getScaledThumbsDir(String.valueOf(scale), false);
			if(f!= null){											//sprawdzam czy istnieje ju¿ skalowany obrazek
				//Log.i(LOG_TAG, "scale response:" +scale);
				String dir_to_scaled = f.getAbsolutePath();
				img_f = new File(dir_to_scaled + File.separator + filename);
				if(img_f.exists()){
					//Log.i("GETPATH", "width: " +width+" scale: "+scale+" path: "+img_f.getAbsolutePath() + " dev_width: "+dev_width);
					return img_f.getAbsolutePath();
				}
				//Log.w("GETPATH", "NOT EXIST width: " +width+" scale: "+scale+" path: "+filename + " dev_width: "+dev_width);
			}
			//Log.w("GETPATH", "2NOT EXIST width: " +width+" scale: "+scale+" path: "+filename +  " dev_width: "+dev_width);
			if(scale == 1)
				break;
			scale--;// = (int) Math.ceil(scale/2d);
		}while(true);

		return null;
	}
	

    public static void delete(File file)
    	throws IOException{
 
    	if(file.isDirectory()){
 
    		//directory is empty, then delete it
    		if(file.list().length==0){
 
    		   file.delete();
    		   System.out.println("Directory is deleted : " 
                                                 + file.getAbsolutePath());
 
    		}else{
 
    		   //list all the directory contents
        	   String files[] = file.list();
 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp);
 
        	      //recursive delete
        	     delete(fileDelete);
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	     System.out.println("Directory is deleted : " 
                                                  + file.getAbsolutePath());
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		file.delete();
    		System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }



}
