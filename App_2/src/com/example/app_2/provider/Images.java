package com.example.app_2.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.AddImagesFromFolderActivity;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

public class Images { // TODO nie mo¿e byæ static
	public static List<ImageObject> images = new LinkedList<ImageObject>();  // list of ImageObject selected by category id
	public static List<Uri> imageUris = new ArrayList<Uri>();
	public ImageLoader il = new ImageLoader(App_2.getAppContext());
	public static long img_dir_last_read = Long.valueOf(Storage.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead", "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE));
	public static long imgLastModified;
	private static final String LOG_TAG = "Images";

	
	public static void readImagesFromDB(){ 				//TODO zmieniæ - u¿yteczne tylko do debugowania
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
				populateImagePaths();								 //wstawia wszystko do g³ównej kategorii
				//images = db.getAllImages();
				generateThumbs();
				img_dir_last_read = Storage.getImagesDir().lastModified();
				Storage.saveToSharedPreferences("imgDirLastRead", Long.toString(img_dir_last_read), "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE);
				}
			}
		}
	
	
	public static List<String> getImagesFileNames(List<String> fileNames){
		Iterator<String> li =  fileNames.iterator();
		while(li.hasNext()){   										// sprawdŸ czy pliki s¹ obrazkami
			if(!(isImgFile(Storage.getImagesDir() + File.separator + li.next()))){
				li.remove();
			}
		}
		return fileNames;
	}
	
	public static void addImagesToDatabase(String path){
		List<String> fileNames = new LinkedList<String>();
		fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(new File(path)));
		
		ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
		
		for(String filename: fileNames){
				batchOps.add(ContentProviderOperation.newInsert(ImageContract.CONTENT_URI)
						.withValue(ImageContract.Columns.PATH, filename)
						.withValue(ImageContract.Columns.PARENT, -1)
						.build());
		}
		
		try {
			App_2.getAppContext().getContentResolver().applyBatch(ImageContract.AUTHORITY, batchOps);
		} catch (RemoteException e) {
										// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
										// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static Uri[] populateImagePaths(){
		//images.clear();
		imageUris.clear();
		Database.recreateDB();
		List<String> fileNames = new LinkedList<String>();
		//fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(Storage.getThumbsMaxDir()));
		fileNames = Storage.getFilesNamesFromDir(Storage.getThumbsMaxDir());
		
		ImageObject img_obj= null;
		ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
		
		for(String filename: fileNames){
			//File f = new File(Storage.getImagesDir() + File.separator + filename);
			
			//img_obj= db.isImageAlreadyExist(filename);		// je¿eli tak to zwaracam do img_obj
			if(img_obj == null){
				batchOps.add(ContentProviderOperation.newInsert(ImageContract.CONTENT_URI)
						.withValue(ImageContract.Columns.PATH, filename)
						.withValue(ImageContract.Columns.PARENT, -1)
						.build());
			//	img_obj = new ImageObject(filename);		// TODO zamieniæ na klucz generowany z MD5
			//	img_obj.setParent_fk(rootCatId);
			//	images.add(db.insertImage(img_obj));
			}else{
				//batchOps.
				//imageUris.add(img_obj);
			}
		}
		
		// Invoke the batch insertion
		ContentProviderResult[] opResults = null;
		try {
			opResults = App_2.getAppContext().getContentResolver().applyBatch(ImageContract.AUTHORITY, batchOps);
		} catch (RemoteException e) {
										// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OperationApplicationException e) {
										// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Extract the Uris of the new records
		if(opResults!=null){
			Uri[] newUris = new Uri[opResults.length];
			
			for(int index=0; index < opResults.length; index++){
				newUris[index] = opResults[index].uri;
				imageUris.add(opResults[index].uri);
			}
			return newUris;
		}
		return null;

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
	
	
	public static String getImageThumbsPath(String imageName){
		String path =  Storage.getThumbsDir() + File.separator + imageName;
		File f = new File(path);
		if(f.exists())
			return Storage.getThumbsDir() + File.separator + imageName;
		else 
			return getImageFullScreenThumbsPath(imageName);
	}
	public static String getImageFullScreenThumbsPath(String imageName){
		String path =  Storage.getThumbsMaxDir() + File.separator + imageName;
		File f = new File(path);
		if(f.exists())
			return path;
		else
			return getImagePath(imageName);
	}
	
	public static String getImagePath(String imageName){
		String path = Storage.getImagesDir() + File.separator + imageName;
		File f = new File(path);
		if(f.exists())
			return path;
		else
			return null;
		
	}
	
	public static String getImagePath(int number){
		if(number<images.size())
			return Storage.getImagesDir() + File.separator + images.get(number).getImageName();
		return null;
	}
		
	public static String getImageThubms(int number){
		if(number<images.size())
			return Storage.getThumbsDir() + File.separator + images.get(number).getImageName();
		return null;
	}
	
	public static String getImageFullScreenThumbs(int number){
		if(number<images.size())
			return Storage.getThumbsMaxDir() + File.separator + images.get(number).getImageName();
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
	
	public static class ThumbsProcessTask extends AsyncTask<Void, Integer, Void>{
		Activity executing_activity;
		
		public ThumbsProcessTask(Activity activity){
			this.executing_activity = activity;
		}
		
	@Override
		    protected void onPreExecute() {
			executing_activity.showDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
		    }
		
		@Override
		protected Void doInBackground(Void... arg0) {
			int count;

			
			Log.i(LOG_TAG, "images size: " +images.size());
			if(images.size()<=0){
				Database db = Database.open();	
				images = db.getAllImages();
				imgLastModified = Storage.getImagesDir().lastModified();
				if(imgLastModified> img_dir_last_read){																// katalog zosta³ zmodyfikowany
					Log.w(LOG_TAG, "images in directory has changed:"+String.valueOf(img_dir_last_read)+"<"+String.valueOf(imgLastModified));
				}
				if(images.size()<=1 || Storage.getImagesDir().lastModified()> img_dir_last_read ){
					Log.i(LOG_TAG, "images size2: " +images.size());
					populateImagePaths();								 											//wstawia wszystko do g³ównej kategorii

													// GENERUJ MINIATURKI
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
					count = all_images.size();
					
					int i= 0;
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
			        	i++;
			        	publishProgress((int) ((i/ (float) count)*100));
			        	//Escape early if cancel() is called
			        	if(isCancelled()) break;
			        	

					}
					
					img_dir_last_read = Storage.getImagesDir().lastModified();
					Storage.saveToSharedPreferences("imgDirLastRead", Long.toString(img_dir_last_read), "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE);
					}
				}


			
			return null;
		}
		
	     protected void onProgressUpdate(Integer... progress) {
	    	 ImageGridActivity.dialog.setProgress(progress[0]);		
	     }

	     protected void onPostExecute(Void result) {
	         executing_activity.removeDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
	     }
	}
	
	
	public static class ProcessBitmapsTask extends AsyncTask<String, Integer, Void>{
		Activity executing_activity;
		
		public ProcessBitmapsTask(Activity activity){
			this.executing_activity = activity;
		}
		
	@Override
		    protected void onPreExecute() {
			if(executing_activity instanceof ImageGridActivity)
				executing_activity.showDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
			else
				executing_activity.showDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
		    }
		
		@Override
		protected Void doInBackground(String... arg0) {
			int count;
			String path = arg0[0];
			// - przejrzeæ katalog
			// - sprawdziæ czy wygenerowane miniaturki dla wpisów
			//Cursor cursor = executing_activity.getContentResolver().query(ImageContract.CONTENT_URI, null, null, null,null);
			//cursor.moveToFirst();
			
			imgLastModified = Storage.getImagesDir().lastModified();
			//if(imgLastModified> img_dir_last_read){																// katalog zosta³ zmodyfikowany
			//	if(true){
				Log.w(LOG_TAG, "images in directory has changed:"+String.valueOf(img_dir_last_read)+"<"+String.valueOf(imgLastModified));
				//executing_activity.getContentResolver().delete(ImageContract.CONTENT_URI, null, null);
				
				//List<String> fileNames = getImagesFileNames(Storage.getChangedFilesFromDir(Storage.getImagesDir()));
				List<String> fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(new File(path)));
				//List<String> fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(Storage.getImagesDir()));
								
				//ContentResolver contentRes= App_2.getAppContext().getContentResolver();
				//ContentValues cv = new ContentValues();
				//cv.put(ImageContract.Columns.PARENT, -1);
				
				// GENERUJ MINIATURKI
				String path_toIMG, path_toTHUMB, path_toFullScreenTHUMB;
				Bitmap bitmap =null;
				//Cursor c = executing_activity.getContentResolver().query(ImageContract.CONTENT_URI, null, null, null,null);
				//c.moveToFirst();
				//List<ImageObject> all_images =  db.getAllImages();
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
				//count = all_images.size();
				count = fileNames.size();
				int i= 0;
				
				
				//File f;
				for(String filename : fileNames){
					publishProgress((int) ((i/ (float) count)*100));
					path_toIMG = path + File.separator + filename;
					//f = new File(path_toIMG);
					path_toTHUMB = Storage.getThumbsDir() + File.separator + filename;
					path_toFullScreenTHUMB = Storage.getThumbsMaxDir() + File.separator + filename;
					
					bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, maxWidth,maxHeight); // mo¿e byæ null
					//Log.w(LOG_TAG, bitmap.getHeight() + " " +bitmap.getWidth());
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
					
					//f.delete();
					
					i++;

					//cv.put(ImageContract.Columns.PATH, filename);
					//contentRes.insert(ImageContract.CONTENT_URI, cv);

					//Escape early if cancel() is called
					if(isCancelled()) break;
					
				}
				
				img_dir_last_read = Storage.getImagesDir().lastModified();
				Storage.saveToSharedPreferences("imgDirLastRead", Long.toString(img_dir_last_read), "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE);
				addImagesToDatabase(path);
				//populateImagePaths();
			
			//cursor.close();
			return null;
		}
		
	     protected void onProgressUpdate(Integer... progress) {
				if(executing_activity instanceof ImageGridActivity)
					ImageGridActivity.dialog.setProgress(progress[0]);		
				else
					AddImagesFromFolderActivity.dialog.setProgress(progress[0]);		
			    
	    	 
	     }

	     protected void onPostExecute(Void result) {
	    	 if(executing_activity instanceof ImageGridActivity)
	    		 executing_activity.removeDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
	    	 else
	    		 executing_activity.removeDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
	     }
	}
	
	public static class AddingImageTask extends AsyncTask<String, Integer, Void>{
		
		@Override
		protected Void doInBackground(String... params) {
			String path_toIMG = params[0];
			String filename = Utils.getFilenameFromPath(path_toIMG);
				
			// GENERUJ MINIATURKI
			String path_toTHUMB, path_toFullScreenTHUMB;
			Bitmap bitmap =null;

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
			path_toTHUMB = Storage.getThumbsDir() + File.separator + filename;
			path_toFullScreenTHUMB = Storage.getThumbsMaxDir() + File.separator + filename;
				
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
			
		    File f = new File(path_toIMG);
		    f.delete();

			return null;		
		}
	}
}