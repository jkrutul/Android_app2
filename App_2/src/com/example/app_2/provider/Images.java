package com.example.app_2.provider;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;

public class Images {
	public static List<String> imagesPaths = new LinkedList<String>();
	public static List<String> imagesUrls = new LinkedList<String>();
	
	public static void populateImagePaths(int category){
		
		Database db = Database.open();	
		
		List<String> fileNames = new LinkedList<String>();
		fileNames = Storage.getFilesNamesFromDir(Storage.getImagesDir());
		for(String fn: fileNames){
			ImageObject iObject = new ImageObject(fn);
			db.insertImage(iObject);
		}

		imagesPaths = db.getAllImagePathByCategory(category);
	}
	
	

}
