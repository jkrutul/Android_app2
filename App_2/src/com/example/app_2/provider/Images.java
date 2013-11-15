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
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.AddImagesFromFolderActivity;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

public class Images {
	public static List<ImageObject> images = new LinkedList<ImageObject>(); // list
																			// of
																			// ImageObject
																			// selected
																			// by
																			// category
																			// id
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
	
	public static void addImagesToDatabase(String path, String parent_id) {
		List<String> fileNames = new LinkedList<String>();
		ContentProviderResult[] opResults = null;
		fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(new File(path)));

		ArrayList<ContentProviderOperation> batchOps = new ArrayList<ContentProviderOperation>();
		/* DODANIE OBRAZKA DO BAZY I POWI¥ZANIE ZE S£OWNIKIEM */
		for (String filename : fileNames) {
			batchOps.add(ContentProviderOperation
					.newInsert(ImageContract.CONTENT_URI)
					.withValue(ImageContract.Columns.PATH, filename)
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
/*
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
*/
		Long parents[] = new Long[1];
		parents[0]=Long.valueOf(parent_id);
		addToDict(getIdsFromContentProviderResult(opResults), parents);
		/*
		for (Long image_fk : getIdsFromContentProviderResult(opResults)) { // dodanie obrazków do s³ownika o identyfikatorze -1
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
		
		if(parent_id != null){
			for (Long image_fk : getIdsFromContentProviderResult(opResults)) {
				batchOps.add(ContentProviderOperation
						.newInsert(ParentContract.CONTENT_URI)
						.withValue(ParentContract.Columns.IMAGE_FK, image_fk)
						.withValue(ParentContract.Columns.PARENT_FK, parent_id).build());
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
		*/

		/* DODANIE OBRAZKA DO WYBRANYCH KATEGORII */
		if (parent_id != null) {// TODO zmieniæ na for i dodawaæ obrazki po tablicy rodziców
			for (String filename : fileNames) {
				batchOps.add(ContentProviderOperation
						.newInsert(ImageContract.CONTENT_URI)
						.withValue(ImageContract.Columns.PATH, filename)
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
			
				for (Long image_fk :getIdsFromContentProviderResult(opResults)) { // dodanie do wybranego drzewa
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

	public static List<String> getImagesFileNames(List<String> fileNames) {
		Iterator<String> li = fileNames.iterator();
		while (li.hasNext()) { // sprawdŸ czy pliki s¹ obrazkami
			if (!(isImgFile(Storage.getImagesDir() + File.separator + li.next()))) {
				li.remove();
			}
		}
		return fileNames;
	}

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

	public static String getImageThubms(int number) {
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

	private static boolean isImgFile(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (options.outWidth != -1 && options.outHeight != -1) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Dodawanie obrazków z wybranego folderu, do s³ownika i wybranej kategorii
	 */
	public static class ProcessBitmapsTask extends
			AsyncTask<String, Integer, Void> {
		Activity executing_activity;

		public ProcessBitmapsTask(Activity activity) {
			this.executing_activity = activity;
		}

		@Override
		protected void onPreExecute() {
			if (executing_activity instanceof ImageGridActivity)
				executing_activity
						.showDialog(ImageGridActivity.PLEASE_WAIT_DIALOG);
			else
				executing_activity
						.showDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
		}

		@Override
		protected Void doInBackground(String... arg0) {
			int count;
			String path = arg0[0];
			String parent_id = arg0[1];

			imgLastModified = Storage.getImagesDir().lastModified();

			List<String> fileNames = getImagesFileNames(Storage.getFilesNamesFromDir(new File(path)));

			// GENERUJ MINIATURKI
			String path_toIMG, path_toTHUMB, path_toFullScreenTHUMB, app_thumb_dir, app_fc_thumb_dir;
			Bitmap bitmap = null;

			int thumbWidth, thumbHeight, maxWidth, maxHeight;
			thumbWidth = App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
			thumbHeight = App_2.getAppContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

			maxWidth = App_2.getMaxWidth();
			maxHeight = App_2.getMaxHeight();

			Log.i(LOG_TAG, "thumbs will be w:" + thumbWidth + " h:"
					+ thumbHeight);
			Log.i(LOG_TAG, "max thumbs will be w:" + maxWidth + " h:"
					+ maxHeight);

			count = fileNames.size();
			int i = 0;

			app_thumb_dir = Storage.getThumbsDir() + File.separator;
			app_fc_thumb_dir = Storage.getThumbsMaxDir() + File.separator;
			
			for (String filename : fileNames) {
				publishProgress((int) ((i / (float) count) * 100));
				path_toIMG = path + File.separator + filename;
				path_toTHUMB = app_thumb_dir+ filename;
				path_toFullScreenTHUMB = app_fc_thumb_dir + filename;

				bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, maxWidth, maxHeight); // mo¿e byæ null

				try {
					FileOutputStream out = new FileOutputStream(
							path_toFullScreenTHUMB);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				} catch (Exception e) {
					e.printStackTrace();
				}

				bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toFullScreenTHUMB, thumbWidth, thumbHeight);
				// bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth,
				// thumbHeight, true);
				try {
					FileOutputStream out = new FileOutputStream(path_toTHUMB);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				} catch (Exception e) {
					e.printStackTrace();
				}

				i++;
				if (isCancelled())
					break;

			}

			img_dir_last_read = Storage.getImagesDir().lastModified();
			Storage.saveToSharedPreferences("imgDirLastRead", Long.toString(img_dir_last_read), "imgDirLastRead",App_2.getAppContext(), Context.MODE_PRIVATE);
			addImagesToDatabase(path, parent_id);

			// cursor.close();
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
						.removeDialog(AddImagesFromFolderActivity.PLEASE_WAIT_DIALOG);
		}
	}

	/* dodanie jednego obrazka do bazy */
	public static class AddingImageTask extends
			AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String path_toIMG = params[0];
			String filename = Utils.getFilenameFromPath(path_toIMG);

			// GENERUJ MINIATURKI
			String path_toTHUMB, path_toFullScreenTHUMB;
			Bitmap bitmap = null;

			int thumbWidth, thumbHeight;
			thumbWidth = App_2.getAppContext().getResources()
					.getDimensionPixelSize(R.dimen.image_thumbnail_size);
			thumbHeight = App_2.getAppContext().getResources()
					.getDimensionPixelSize(R.dimen.image_thumbnail_size);

			// full screen thumbs
			int maxWidth = App_2.getMaxWidth();
			int maxHeight = App_2.getMaxHeight();

			Log.i(LOG_TAG, "thumbs will be w:" + thumbWidth + " h:"
					+ thumbHeight);
			Log.i(LOG_TAG, "max thumbs will be w:" + maxWidth + " h:"
					+ maxHeight);
			path_toTHUMB = Storage.getThumbsDir() + File.separator + filename;
			path_toFullScreenTHUMB = Storage.getThumbsMaxDir() + File.separator
					+ filename;

			bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG,
					maxWidth, maxHeight);
			Log.w(LOG_TAG, bitmap.getHeight() + " " + bitmap.getWidth());
			try {
				FileOutputStream out = new FileOutputStream(
						path_toFullScreenTHUMB);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			bitmap = BitmapCalc.decodeSampleBitmapFromFile(
					path_toFullScreenTHUMB, thumbWidth, thumbHeight);
			try {
				FileOutputStream out = new FileOutputStream(path_toTHUMB);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			File f = new File(path_toIMG);
			f.delete();

			return null;
		}
	}

	/* dodanie wpisów do bazy danych */
	public static class AddToDatabaseTask extends
			AsyncTask<String, Integer, Void> {
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
						.withValue(ImageContract.Columns.PATH, filename)
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