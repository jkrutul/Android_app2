package com.example.app_2.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.App_2;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;

public class Images {
	public static List<ImageObject> images = new LinkedList<ImageObject>();  // list of ImageObject selected by category id
	
	//public static List<String> imagesPaths = new LinkedList<String>(); 
	//public static List<String> imagesThumbs = new LinkedList<String>();
	//public static List<String> imagesUrls = new LinkedList<String>();
	//public static List<String> imageNames = new LinkedList<String>();   // list of all files in ImagesDir
	
	private static final String LOG_TAG = "Images";
	
	public static void readImagesFromDB(){ 				//TODO zmieniæ - u¿yteczne tylko do debugowania
		Log.i(LOG_TAG, "images size: " +images.size());
		if(images.size()<=0){
			Database db = Database.open();	
			images = db.getAllImages();
			if(images.size()<=0){
				Log.i(LOG_TAG, "images size2: " +images.size());
				populateImagePaths();
				//images = db.getAllImages();
				generateThumbs();
				}
			}
		}
	
	public static void populateImagePaths(){
		Random rnd = new Random();
		images.clear();
		Database db = Database.open();	
		List<String> fileNames = new LinkedList<String>();
		fileNames = Storage.getFilesNamesFromDir(Storage.getImagesDir());		// TODO info jak folder images jest pusty
		
		ImageObject img_obj= null;
		for(String filename: fileNames){
			img_obj= db.isImageAlreadyExist(filename);		// je¿eli tak to zwaracam do img_obj
			if(img_obj == null){							// brak obiektu w bazie danych
				img_obj = new ImageObject(filename);		// TODO zamieniæ na klucz generowany z MD5
				img_obj.setCategory_fk(Long.valueOf(rnd.nextInt(8)));  	//  wstawienie do g³ównej kategorii
				images.add(db.insertImage(img_obj));
			}else{
				images.add(img_obj);
				//Log.i(LOG_TAG, "image:"+img_obj+"already exist in db");
			}
		}

		//imagesPaths = db.getAllImagePathByCategory(category);
	}
	
	public static void generateThumbs(){
		Database db = Database.open();
		String path_toIMG, path_toTHUMB;
		Bitmap bitmap =null;
		List<ImageObject> all_images =  db.getAllImages();
		int thumbWidth=ImageLoader.mWidth;
		int thumbHeight = ImageLoader.mHeight;
		int maxWidth ;
		int maxHeight;
		
		Log.i(LOG_TAG, "thumbs will be w:"+thumbWidth+" h:"+thumbHeight);
		
		for(ImageObject img_o : all_images){
			path_toIMG = Storage.getImagesDir() + File.separator + img_o.getImageName();
			path_toTHUMB = Storage.getThumbsDir() + File.separator + img_o.getImageName();
        	bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, thumbWidth,thumbHeight);
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
	
	
	
	
	

}
