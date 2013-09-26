package com.example.app_2.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;

public class Images {
	public static List<ImageObject> images = new LinkedList<ImageObject>();  // list of ImageObject selected by category id
	public static long img_dir_last_read = Long.valueOf(Storage.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead", "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE));
	public static long imgLastModified;
	private static final String LOG_TAG = "Images";

	
	public static void readImagesFromDB(){ 				//TODO zmieni� - u�yteczne tylko do debugowania
		Log.i(LOG_TAG, "images size: " +images.size());
		if(images.size()<=0){
			Database db = Database.open();	
			images = db.getAllImages();
			imgLastModified = Storage.getImagesDir().lastModified();
			if(imgLastModified> img_dir_last_read){
				Log.w(LOG_TAG, "images in directory has changed:"+String.valueOf(img_dir_last_read)+"<"+String.valueOf(imgLastModified));
			}
			if(images.size()<=1 || Storage.getImagesDir().lastModified()> img_dir_last_read ){
				Log.i(LOG_TAG, "images size2: " +images.size());
				populateImagePaths();								 //wstawia wszystko do g��wnej kategorii
				//images = db.getAllImages();
				generateThumbs();
				img_dir_last_read = Storage.getImagesDir().lastModified();
				Storage.saveToSharedPreferences("imgDirLastRead", Long.toString(img_dir_last_read), "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE);
				}
			}
		}
	
	public static void populateImagePaths(){
		images.clear();
		Database db = Database.open();	
		ImageObject rootCategory = db.getRootCategory();
		Long rootCatId = rootCategory.getId();
		List<String> fileNames = new LinkedList<String>();
		fileNames = Storage.getFilesNamesFromDir(Storage.getImagesDir());		// TODO info jak folder images jest pusty
		
		for(String filename: fileNames){   										// sprawd� czy pliki s� obrazkami
			if(!(isImgFile(Storage.getImagesDir() + File.separator + filename))){
				fileNames.remove(filename);
			}
		}
		
		ImageObject img_obj= null;
		for(String filename: fileNames){
			img_obj= db.isImageAlreadyExist(filename);		// je�eli tak to zwaracam do img_obj
			if(img_obj == null){							// brak obiektu w bazie danych
				img_obj = new ImageObject(filename);		// TODO zamieni� na klucz generowany z MD5
				img_obj.setParent_fk(rootCatId);
				images.add(db.insertImage(img_obj));
			}else{
				images.add(img_obj);
			}
		}

		//imagesPaths = db.getAllImagePathByCategory(category);
	}
	
	public static void populateImagePaths(int category_fk){
		images.clear();
		Database db = Database.open();	
		images = db.getAllImagesByCategory(category_fk);
	}
	
	public static void generateThumbs(){
		Database db = Database.open();
		String path_toIMG, path_toTHUMB, path_toFullScreenTHUMB;
		Bitmap bitmap =null;
		List<ImageObject> all_images =  db.getAllImages();
		//int thumbWidth=ImageLoader.mWidth;
		//int thumbHeight = ImageLoader.mHeight;
		int thumbWidth, thumbHeight;
		thumbWidth =App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		thumbHeight= App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		
		//full screen thumbs
		WindowManager wm = (WindowManager) App_2.getAppContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int maxWidth = display.getWidth();
		int maxHeight = display.getHeight();		

		Log.i(LOG_TAG, "thumbs will be w:"+thumbWidth+" h:"+thumbHeight);
		Log.i(LOG_TAG, "max thumbs will be w:"+maxWidth+" h:"+maxHeight);
		
		for(ImageObject img_o : all_images){
			path_toIMG = Storage.getImagesDir() + File.separator + img_o.getImageName();
			path_toTHUMB = Storage.getThumbsDir() + File.separator + img_o.getImageName();
			path_toFullScreenTHUMB = Storage.getThumbsMaxDir() + File.separator + img_o.getImageName();
			
        	bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, maxWidth,maxHeight);
        	Log.w(LOG_TAG, bitmap.getHeight() + " " +bitmap.getWidth());
        	try {
        	       FileOutputStream out = new FileOutputStream(path_toFullScreenTHUMB);
        	       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        	} catch (Exception e) {
        	       e.printStackTrace();
        	}
        	
        	bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toFullScreenTHUMB, thumbWidth,thumbHeight);
        	try {
        	       FileOutputStream out = new FileOutputStream(path_toTHUMB);
        	       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        	} catch (Exception e) {
        	       e.printStackTrace();
        	}
        	
        	

		}
		

		

		

		
	}
	
	
	public static String getImagePath(int number){
		if(number<images.size()){
			return Storage.getImagesDir() + File.separator + images.get(number).getImageName();
		}else{
			return null;
		}

	}
	
	public static String getImageThubms(int number){
		if(number<images.size()){
			return Storage.getThumbsDir() + File.separator + images.get(number).getImageName();
		}
		return null;
	}
	
	public static String getImageFullScreenThumbs(int number){
		if(number<images.size()){
			return Storage.getThumbsMaxDir() + File.separator + images.get(number).getImageName();
		}
		return null;
	}
	
	private static boolean isImgFile(String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (options.outWidth != -1 && options.outHeight != -1) {
		    return true;
		}
		else {
		    return false;
		}
	}
	
	
	

}
