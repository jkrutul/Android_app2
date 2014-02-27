package com.example.app_2.provider;

import java.io.File;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.example.app_2.App_2;
import com.example.app_2.activities.AddImagesFromFolderActivity;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.models.ImageObject;
import com.example.app_2.models.UserAndCategoriesListModel;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

public final class Images {
	
	
	
	private Images(){}
	

	public static List<Uri> imageUris = new ArrayList<Uri>();
	public ImageLoader il = new ImageLoader(App_2.getAppContext());
	public static long img_dir_last_read = Long.valueOf(Storage
			.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead",
					"imgDirLastRead", App_2.getAppContext(),
					Context.MODE_PRIVATE));
	
	
	public static long imgLastModified;
	private static final String LOG_TAG = "Images";

	private static Long[] getIdsFromContentProviderResult(ContentProviderResult cpr[]){
		Uri[] uris = null;
		Long[] ids = null;
		if (cpr != null) {
			uris = new Uri[cpr.length];
			for (int index = 0; index < cpr.length; index++)
				uris[index] = cpr[index].uri;
			ids = new Long[uris.length];
			int i = 0;
			for (Uri uri : uris)
				ids[i++] = Long.valueOf(uri.getLastPathSegment());
		}
		return ids;
	}
	
	private static void addToDict(Long[] ids, Long[] parents){
		ContentProviderResult[] opResults = null;
		ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
		for (Long image_fk : ids) { 								// dodanie obrazków do s³ownika o identyfikatorze -1
			batchOps.add(ContentProviderOperation
					.newInsert(ParentContract.CONTENT_URI)
					.withValue(ParentContract.Columns.IMAGE_FK, image_fk)
					.withValue(ParentContract.Columns.PARENT_FK, -1).build());
		}
		try {
			opResults = App_2.getAppContext().getContentResolver().applyBatch(ParentContract.AUTHORITY, batchOps);
			batchOps.clear();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		for(Long p : parents){
			for (Long image_fk : ids) { 								// dodanie do wszystkich kategorii
				batchOps.add(ContentProviderOperation
						.newInsert(ParentContract.CONTENT_URI)
						.withValue(ParentContract.Columns.IMAGE_FK, image_fk)
						.withValue(ParentContract.Columns.PARENT_FK, p).build());
			}
			try {
				opResults = App_2.getAppContext().getContentResolver().applyBatch(ParentContract.AUTHORITY, batchOps);
				batchOps.clear();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}

		
	}
	
	private static LinkedList<UserAndCategoriesListModel> groupCategoriesByUser(LinkedList<ImageObject> io){
		LinkedList<UserAndCategoriesListModel> groupByUser = new LinkedList<UserAndCategoriesListModel>();
		LinkedList<Long> users = new LinkedList<Long>();
		
				
		for(ImageObject img_object : io){			
			long author_fk= img_object.getAuthor_fk();

			if(!users.contains(author_fk)){
				users.add(author_fk);
			}
		}
		
		
		for(Long user : users){
			groupByUser.add(new UserAndCategoriesListModel(user));
		}
		
		UserAndCategoriesListModel user_and_cats = null;
		
		for(ImageObject imageObject : io ){
			long author_fk = imageObject.getAuthor_fk();
			if(user_and_cats != null){
				if( user_and_cats.getUser_fk() == author_fk)
					user_and_cats.getCategories().add(imageObject.getId());			
				
				else
					for(UserAndCategoriesListModel uaclm : groupByUser)
						if(uaclm.getUser_fk() == author_fk){
							user_and_cats = uaclm;
							user_and_cats.getCategories().add(imageObject.getId());			
							break;
						}
			}
			else{
				for(UserAndCategoriesListModel uaclm : groupByUser)
					if(uaclm.getUser_fk() == author_fk){
						user_and_cats = uaclm;
						user_and_cats.getCategories().add(imageObject.getId());			
						break;
					}
			}

		}
		return groupByUser;
	}
	
	private static LinkedList<ImageObject> getAutorsFromImageList(ArrayList<Long> symbol_ids){
		LinkedList<ImageObject> ios = new LinkedList<ImageObject>();
		Cursor c = null;
		String[] projection = {"i."+ImageContract.Columns.AUTHOR_FK};
		
		for(Long symbolId : symbol_ids){
			Uri uri = Uri.parse(ImageContract.CONTENT_URI+"/"+symbolId);

			c = App_2.getAppContext().getContentResolver().query(uri, projection, null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast()){
				long authorId = c.getLong(0);
				ImageObject io = new ImageObject();
				io.setAuthor_fk(authorId);
				io.setId(symbolId);
				ios.add(io);
			}
			
		}
		
		c.close();
		return ios;
	}
	
	
	
	
	public static void addNewEntriesToImageTable(List<String> filenames, ArrayList<Long> categories){
		
		LinkedList<ImageObject> catAndUsers_ids = getAutorsFromImageList(categories);		//pobieram autorów ka¿dej z kategorii
		
		LinkedList<UserAndCategoriesListModel> groupByUser = groupCategoriesByUser(catAndUsers_ids);	//grupujê w listy kategorii dla ka¿dego autora
		

		
		
		
		Database db = Database.getInstance(App_2.getAppContext());
		db.open();
		int cvSize = 0;
		Long main_dict_id = db.getMainDictFk();
		//Long logged_user_id = null;
		/*
		 * 
		if(user_id != null){
			logged_user_id = user_id;
		}else{
			SharedPreferences sharedPref = App_2.getAppContext().getSharedPreferences("USER",Context.MODE_PRIVATE);
			logged_user_id = sharedPref.getLong("logged_user_id", -1);
			if(logged_user_id == -1)
				logged_user_id = null;
		}
		 
		 */
		
		/*
		if(categories!=null){
			categories.add(main_dict_id);
			cvSize = categories.size();
		}
		else{ 
			categories = new ArrayList<Long>();
			categories.add(main_dict_id);
			cvSize  = 1;
		}
		
		*/
		
		for(UserAndCategoriesListModel uc : groupByUser){					//dodanie s³ownika do list kategorii
			LinkedList<Long> user_categories = uc.getCategories();			
			user_categories.add(main_dict_id);
			//cvSize += user_categories.size();
		}
		

		

		
		for(UserAndCategoriesListModel uc : groupByUser){					//dodanie s³ownika do list kategorii
			Long user_fk = uc.getUser_fk();
			LinkedList<Long> user_categories  = uc.getCategories();
			
			for(String filename: filenames){
				Long inserted_id = db.insertImage(new ImageObject(filename, user_fk));
				
				if(inserted_id!= -1){
					ContentValues[] cvArray  = new ContentValues[user_categories.size()];
					int i =0;
					for(Long category_fk : user_categories){
						ContentValues cv = new ContentValues();
						cv.put(ParentContract.Columns.IMAGE_FK, inserted_id);
						cv.put(ParentContract.Columns.PARENT_FK, category_fk);
						cvArray[i++] = cv;
					}
					App_2.getAppContext().getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
				}
			}
		}
		
		/*
		for(ImageObject io : catAndUsers_ids){
			for(String filename : filenames){
				Long inserted_id = db.insertImage(new ImageObject(filename, io.getAuthor_fk()));
				if( inserted_id!=-1 ){
					ContentValues cv = new ContentValues();
					cv.put(ParentContract.Columns.IMAGE_FK, inserted_id);
					cv.put(ParentContract.Columns.PARENT_FK, io.getId());
					App_2.getAppContext().getContentResolver().insert(ParentContract.CONTENT_URI, cv);
				}
			}
		}
		*/
		
		/*
		for(Long userId : users_ids){
			for(String filename: filenames){
				Long inserted_id = db.insertImage(new ImageObject(filename, userId));
				if(inserted_id!= -1){
					ContentValues[] cvArray  = new ContentValues[cvSize];
					int i =0;
					for(Long category_fk :categories){
						//category_fk = categories.get(i);
						ContentValues cv = new ContentValues();
						cv.put(ParentContract.Columns.IMAGE_FK, inserted_id);
						cv.put(ParentContract.Columns.PARENT_FK, category_fk);
						cvArray[i++] = cv;
					}
					App_2.getAppContext().getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
				}
			}
		}
		*/

		
	}
	
	
	public static void addNewEntryToImageTable(String filename, String image_description, Long category_fk, Long user_id){
		Database db = Database.getInstance(App_2.getAppContext());
		db.open();
		Long main_dict_id = db.getMainDictFk();
		LinkedList<Long> categories = new LinkedList<Long>();
		categories.add(main_dict_id);
		if(category_fk != null)
			categories.add(category_fk);


		Long inserted_id = db.insertImage(new ImageObject(filename, image_description, user_id));
		if(inserted_id!= -1){
			ContentValues[] cvArray  = new ContentValues[categories.size()];
			int i =0;
			for(Long c_fk :categories){
				ContentValues cv = new ContentValues();
				cv.put(ParentContract.Columns.IMAGE_FK, inserted_id);
				cv.put(ParentContract.Columns.PARENT_FK, c_fk);
				cvArray[i++] = cv;
			}
			App_2.getAppContext().getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
		}
		
		
	}
	
	public static void addImagesToDatabase(String images_dir, String parent_id) {
		List<String> fileNames = new LinkedList<String>();
		ContentProviderResult[] opResults = null;
		fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(new File(images_dir)));

		ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
		/* DODANIE OBRAZKA DO BAZY I POWI¥ZANIE ZE S£OWNIKIEM */
		for (String filename : fileNames) {
			batchOps.add(ContentProviderOperation
					.newInsert(ImageContract.CONTENT_URI)
					.withValue(ImageContract.Columns.FILENAME, filename)
					.withValue(ImageContract.Columns.DESC,
							Utils.cutExtention(filename)).build());
		}

		try {
			opResults = App_2.getAppContext().getContentResolver().applyBatch(ImageContract.AUTHORITY, batchOps);
			batchOps.clear();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}

		Long parents[] = new Long[1];
		parents[0]=Long.valueOf(parent_id);
		addToDict(getIdsFromContentProviderResult(opResults), parents);
		ContentValues[] cvArray  = new ContentValues[fileNames.size()];
		/* DODANIE OBRAZKA DO WYBRANYCH KATEGORII */
		if (parent_id != null) {// TODO zmieniæ na for i dodawaæ obrazki po tablicy rodziców
			 
			int itr=0;
			for (String filename : fileNames) {
				ContentValues cv = new ContentValues();
				cv.put(ImageContract.Columns.FILENAME, filename);
				cv.put(ImageContract.Columns.DESC,Utils.cutExtention(filename));
				cvArray[itr++] = cv;
				/*
				batchOps.add(ContentProviderOperation
						.newInsert(ImageContract.CONTENT_URI)
						.withValue(ImageContract.Columns.PATH, filename)
						.withValue(ImageContract.Columns.DESC,
								Utils.cutExtention(filename)).build());
				*/
			}
			
			
			//opResults = App_2.getAppContext().getContentResolver().applyBatch(ImageContract.AUTHORITY, batchOps);
			 App_2.getAppContext().getContentResolver().bulkInsert(ImageContract.CONTENT_URI, cvArray);
			//batchOps.clear();
			
				for (Long image_fk : getIdsFromContentProviderResult(opResults)) { // dodanie do wybranego drzewa
					batchOps.add(ContentProviderOperation
							.newInsert(ParentContract.CONTENT_URI)
							.withValue(ParentContract.Columns.IMAGE_FK, image_fk)
							.withValue(ParentContract.Columns.PARENT_FK,
									Long.valueOf(parent_id)).build());
				}
				try {
					opResults = App_2.getAppContext().getContentResolver().applyBatch(ParentContract.AUTHORITY, batchOps);
					batchOps.clear();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
		}
	}
// TODO
	public static List<String> getImagesFileNames(List<String> paths) {
		Iterator<String> li = paths.iterator();
		while (li.hasNext()) { // sprawdŸ czy pliki s¹ obrazkami
			if (Utils.isImgFile(li.next()) == false)
				li.remove();
		}
		return paths;
	}
	/** Returns list of paths to image files
	 * @param directory containing image files
	 * @return list of image files
	 */
	public static List<File> getListOfImageFiles(String dir) {
		List<File> files_from_dir = Storage.getFilesListFromDir(new File(dir));
		List<File> images_from_dir = new LinkedList<File>();
		
		if(files_from_dir != null){	
			for(File f:files_from_dir){
				if(Utils.isImgFile(dir+File.separator+f.getName()))
					images_from_dir.add(f);
			}
			return images_from_dir;
		}else
			return null;
	}

/*
	public static String getImageThumbsPath(String imageName) {
		String path = Storage.getThumbsDir() + File.separator + imageName;
		File f = new File(path);
		if (f.exists())
			return Storage.getThumbsDir() + File.separator + imageName;
		else
			return getImageFullScreenThumbsPath(imageName);
	}

	public static String getImageFullScreenThumbsPath(String imageName) {
		String path = Storage.getThumbsMaxDir() + File.separator + imageName;
		File f = new File(path);
		if (f.exists())
			return path;
		else
			return getImagePath(imageName);
	}

	public static String getImagePath(String imageName) {
		String path = Storage.getImagesDir() + File.separator + imageName;
		File f = new File(path);
		if (f.exists())
			return path;
		else
			return null;

	}

	public static String getImagePath(int number) {
		if (number < images.size())
			return Storage.getImagesDir() + File.separator
					+ images.get(number).getImageName();
		return null;
	}

	public static String getImageThubm(int number) {
		if (number < images.size())
			return Storage.getThumbsDir() + File.separator
					+ images.get(number).getImageName();
		return null;
	}

	public static String getImageFullScreenThumbs(int number) {
		if (number < images.size())
			return Storage.getThumbsMaxDir() + File.separator
					+ images.get(number).getImageName();
		return null;
	}
	
	*/



	/*
	 * Dodawanie obrazków z wybranego folderu, do s³ownika i wybranej kategorii
	 */
	public static class ProcessBitmapsTask extends	AsyncTask<ArrayList<String>, Integer, Void> {
		Activity executing_activity;
		Database db;
		Long user_id;

		public ProcessBitmapsTask(Activity activity, Long user_id) {
			this.executing_activity = activity;
			db = Database.getInstance(activity);
			db.open();
			this.user_id = user_id;
		}

		@Override
		protected void onPreExecute() {
				executing_activity.showDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
		}

		@Override
		protected Void doInBackground(ArrayList<String>... arg) {
			int count;		
			boolean filenameVerification = true;
			ArrayList<String> argsList = arg[0];
			
			//int parents_count = argsList.size();
			//parents_count--;
			String path_to_dir = (String) argsList.get(0);
			ArrayList<Long> parents_fk = new ArrayList<Long>();
			
			for(int i=1; i<argsList.size(); i++){
				try{
					parents_fk.add(Long.parseLong((String) argsList.get(i)));
				}catch(NumberFormatException e){
					e.printStackTrace();
				}
			}

			List<File> img_files = getListOfImageFiles(path_to_dir);

			count = img_files.size();
			int i = 0;
			LinkedList<String> uniqueFilenames = new LinkedList<String>();
			for (File image_file : img_files) {
				publishProgress((int) ((i / (float) count) * 100));
				
				uniqueFilenames.add(Storage.scaleAndSaveBitmapFromPath(image_file.getAbsolutePath(), Bitmap.CompressFormat.PNG,90,db, filenameVerification));
				i++;
				if (isCancelled())
					break;

			}
			addNewEntriesToImageTable(uniqueFilenames, parents_fk);
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			if (executing_activity instanceof ImageGridActivity)
				ImageGridActivity.dialog.setProgress(progress[0]);
			else
				AddImagesFromFolderActivity.dialog.setProgress(progress[0]);
		}

		protected void onPostExecute(Void result) {
				executing_activity.removeDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
		}
	}
	
	
	
	
	public static class ProcessOneBitmapTask extends	AsyncTask<ArrayList<String>, Integer, Void> {
		Database db;
		Long user_id= null;

		public ProcessOneBitmapTask(Context context, Long user_id) {
			db = Database.getInstance(context);
			db.open();
			this.user_id = user_id;

		}

		@Override
		protected Void doInBackground(ArrayList<String>... arg) {
			boolean filenameVerification = true;
			ArrayList<String> argsList = arg[0];
			String path_to_file = (String) argsList.get(0);
			LinkedList<String> uniqueFilenames = new LinkedList<String>();
			File image_file = new File(path_to_file);
			uniqueFilenames.add(Storage.scaleAndSaveBitmapFromPath(image_file.getAbsolutePath(), Bitmap.CompressFormat.PNG,90,db, filenameVerification));
			Log.i(LOG_TAG, "doInBackground");
			addNewEntriesToImageTable(uniqueFilenames, null);
			return null;
		}

	}

	/* dodanie jednego obrazka do bazy */
	public static class AddingImageTask extends AsyncTask<String, Integer, Void> {
		Database db;
		public boolean filenameVerification = false;
		public AddingImageTask(Activity activity) {
			db = Database.getInstance(activity);
			db.open();
		}
		@Override
		protected Void doInBackground(String... params) {
			String path_toIMG= params[0];
						
/*
			// GENERUJ MINIATURKI
			String path_toTHUMB, path_toFullScreenTHUMB;
			Bitmap bitmap = null;

			int thumbWidth, thumbHeight;
			thumbWidth = App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
			thumbHeight = App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

			// full screen thumbs
			int maxWidth = App_2.getMaxWidth();
			int maxHeight = App_2.getMaxHeight();

			//Log.i(LOG_TAG, "thumbs will be w:" + thumbWidth + " h:"		+ thumbHeight);
			//Log.i(LOG_TAG, "max thumbs will be w:" + maxWidth + " h:"		+ maxHeight);
			path_toTHUMB = Storage.getThumbsDir() + File.separator + filename;
			path_toFullScreenTHUMB = Storage.getThumbsMaxDir() + File.separator		+ filename;

			bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG,		maxWidth, maxHeight);
			Log.w(LOG_TAG, bitmap.getHeight() + " " + bitmap.getWidth());
			try {
				FileOutputStream out = new FileOutputStream(path_toFullScreenTHUMB);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toFullScreenTHUMB, thumbWidth, thumbHeight);
			try {
				FileOutputStream out = new FileOutputStream(path_toTHUMB);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
			*/
			ArrayList<String> uniqueFilename = new ArrayList<String>();
			int dev_width=(App_2.getMaxHeight()> App_2.getMaxWidth()) ?	App_2.getMaxWidth() : App_2.getMaxHeight();


			
			
			
			uniqueFilename.add(Storage.scaleAndSaveBitmapFromPath(path_toIMG, Bitmap.CompressFormat.PNG,100,db, filenameVerification));
			addNewEntriesToImageTable(uniqueFilename, null);
			return null;
		}
	}

	/* dodanie wpisów do bazy danych */
	public static class AddToDatabaseTask extends AsyncTask<String, Integer, Void> {
		Activity executing_activity;

		public AddToDatabaseTask(Activity activity) {
			this.executing_activity = activity;
		}

		@Override
		protected void onPreExecute() {
			if (executing_activity instanceof ImageGridActivity)
				executing_activity
						.showDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
			else
				executing_activity
						.showDialog(AddImagesFromFolderActivity.ADD_TO_DB_WAIT_DIALOG);
		}

		@Override
		protected Void doInBackground(String... params) {
			String path, parent_id;
			path = params[0];
			parent_id = params[1];
			List<String> fileNames = new LinkedList<String>();
			ContentProviderResult[] opResults = null;
			fileNames = getImagesFileNames(Storage
					.getFilesNamesFromDir(new File(path)));

			ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();

			for (String filename : fileNames) {
				batchOps.add(ContentProviderOperation
						.newInsert(ImageContract.CONTENT_URI)
						.withValue(ImageContract.Columns.FILENAME, filename)
						.withValue(ImageContract.Columns.DESC,
								Utils.cutExtention(filename)).build());
			}

			try {
				opResults = App_2.getAppContext().getContentResolver()
						.applyBatch(ImageContract.AUTHORITY, batchOps);

				batchOps.clear();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}

			Uri[] imageUris = null;
			if (opResults != null) {
				imageUris = new Uri[opResults.length];
				for (int index = 0; index < opResults.length; index++)
					imageUris[index] = opResults[index].uri;
			}

			Long image_fks[] = new Long[imageUris.length];
			int i = 0;
			for (Uri image : imageUris)
				image_fks[i++] = Long.valueOf(image.getLastPathSegment());
			
			int count = imageUris.length;
			if(parent_id !=null)
				count*=2;
			
			
			int processing_item= 0;
			for (Long image_fk : image_fks) { // dodanie obrazków do s³ownika o identyfikatorze -1
				batchOps.add(ContentProviderOperation
						.newInsert(ParentContract.CONTENT_URI)
						.withValue(ParentContract.Columns.IMAGE_FK, image_fk)
						.withValue(ParentContract.Columns.PARENT_FK, -1)
						.build());
				publishProgress((int) ((processing_item++/ (float) count)*100));
			}
			try {
				opResults = App_2.getAppContext().getContentResolver()
						.applyBatch(ParentContract.AUTHORITY, batchOps);
				batchOps.clear();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}

			if (parent_id != null) {
				Long parentId = Long.valueOf(parent_id);
				for (Long image_fk : image_fks) { // dodanie do wybranego drzewa
					batchOps.add(ContentProviderOperation
							.newInsert(ParentContract.CONTENT_URI)
							.withValue(ParentContract.Columns.IMAGE_FK,
									image_fk)
							.withValue(ParentContract.Columns.PARENT_FK,
									parentId).build());
					publishProgress((int) ((processing_item++/ (float) count)*100));
				}
				try {
					opResults = App_2.getAppContext().getContentResolver()
							.applyBatch(ParentContract.AUTHORITY, batchOps);
					batchOps.clear();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		protected void onProgressUpdate(Integer... progress) {
			if (executing_activity instanceof ImageGridActivity)
				ImageGridActivity.dialog.setProgress(progress[0]);
			else
				AddImagesFromFolderActivity.dialog.setProgress(progress[0]);
		}

		protected void onPostExecute(Void result) {
			if (executing_activity instanceof ImageGridActivity)
				executing_activity
						.removeDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
			else
				executing_activity
						.removeDialog(AddImagesFromFolderActivity.ADD_TO_DB_WAIT_DIALOG);
		}

	}
}