package com.example.app_2.activities;

import java.io.File;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.spinner.model.UserSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.AsyncTask;
import com.example.app_2.utils.Utils;
import com.example.app_2.utils.ZipUnzipFiles;


public class MainActivity extends Activity {
	private final static String LOG_TAG = "MainActivity";
	private final static int ZIP_DIALOG= 62;
	private final static int UNZIP_DIALOG= 60;
	private final static int REQUEST_CHOOSER=3;
	private static final int FILE_SELECT_REQUEST = 45;
	private ShareActionProvider mShareActionProvider;
	private SharedPreferences prefs = null;
	public static ProgressDialog zip_dialog;
	
	private TextView loggin_info_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	
		//LinearLayout ll = (LinearLayout) findViewById(R.id.main_activity);
		//Utils.setWallpaper(ll, App_2.maxHeight, App_2.getMaxWidth(), null, ScalingLogic.CROP);
		
		 File app_root = Storage.getAppRootDir();
	  	 if(!app_root.exists()){
	  		   app_root.mkdirs();
	  	 }

		ActionBar actionBar = getActionBar();
		actionBar.setSubtitle("G�owne menu");
		prefs = getSharedPreferences("com.example.app_2", MODE_PRIVATE);
		//Log.i("PREFS", "pref.getInt" + prefs.getInt("pref_img_size", 100));
		

	}
	
	private void setLogginInfo(){
		loggin_info_tv = (TextView) findViewById(R.id.logged_user_name);
		SharedPreferences user_sharedPref = getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);			// pobranie informacji o zalogowanym u�ytkowniku
		Long logged_user_id = user_sharedPref.getLong("logged_user_id", 0);
		Long logged_user_root = user_sharedPref.getLong("logged_user_root", Database.getMainRootFk());

		String loginfo= "TRYB EDYCJI";
		if(logged_user_root == Database.getMainRootFk()) //je�li kto� zalogowany tryb edycji wy��czony
			loggin_info_tv.setText(loginfo);
		else{
			
			Uri uri = Uri.parse(UserContract.CONTENT_URI+"/"+logged_user_id);
			String[] projection = { UserContract.Columns.IMG_FILENAME,	UserContract.Columns.USERNAME};
			Cursor c = getContentResolver().query(uri, projection, null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast()) {
				loginfo = c.getString(1);
				String image_file = c.getString(0);
			}
			c.close();
			
			loggin_info_tv.setText(loginfo);
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setLogginInfo();
		
	       if (prefs.getBoolean("firstrun", true)) {
	    	  
	    		   
	            // Do first run stuff here then set 'firstrun' as false
	            // using the following line to edit/commit prefs
	            prefs.edit().putBoolean("firstrun", false).commit();
	        }
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// Inflate menu resource file.
		getMenuInflater().inflate(R.menu.main, menu);
		// Locate MenuItem with ShareActionProvider
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Activity a = this;
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			 new AlertDialog.Builder(this)
		        .setTitle("Spowoduje usuni�cie obrazk�w z bazy danych")
		        .setMessage("Kontynuowa�?")
		        .setNegativeButton(android.R.string.no, null)
		        .setPositiveButton(android.R.string.yes, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Database.open();
						Database.recreateDB();
						File directory = Storage.getAppRootDir();
						try {
							Storage.delete(directory);
						} catch (IOException e) {
						
						}
					}
		        }).create().show();
			 break;
			 
		case R.id.import_export_files:
			new AlertDialog.Builder(this)
	            .setIcon(R.drawable.ic_action_import_export)
	            .setTitle("Wybierz")
	            .setMessage("Importuj lub eksportuj pliki u�ytkownika")
	            .setPositiveButton("Importuj", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	runFilePicker();
	                	
	                	//importAllUserFiles( Storage.getAppRootDir()+File.separator+"zapis.zip");
	                }
	            })
	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	return;
	                }
	            })
	            .setNeutralButton("Eksportuj", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                    //Intent getContentIntent = FileUtils.createGetContentIntent();

	                    //Intent intent = Intent.createChooser(getContentIntent, "Select a file");
	                    //startActivityForResult(intent, REQUEST_CHOOSER);

	                	ExportUserFilesTask euft = new ExportUserFilesTask(a, false);
	                	euft.execute(Storage.getAppRootDir()+File.separator+"zapis.zip", Storage.getAppRootDir().getAbsolutePath());
	                	
	                	//exportAllUserFiles();
	                }
	            })
	            .create().show();
			 break;
		}
		return true;
	}
	
	
	private void runFilePicker(){
		Intent  intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");

		try{
			startActivityForResult(Intent.createChooser(intent, "Wybierz obrazek"), FILE_SELECT_REQUEST);	
			
		}catch(android.content.ActivityNotFoundException ex){
			Toast.makeText(this, "Zainstaluj menad�er plik�w", Toast.LENGTH_SHORT).show();
		}
	}

	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.grid_activity:
			intent = new Intent(this, ImageGridActivity.class);
			// intent = new Intent(this, ProgressActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.right_slide_in,
					R.anim.right_slide_out);
			break;
			
		case R.id.edit_activity:
			intent = new Intent(this, ImageEditActivity.class);
			startActivity(intent);
			break;
		
		case R.id.users_activity:
			intent = new Intent(this, UsersActivity.class);
			startActivity(intent);
			break;
			
		case R.id.settings_activity:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
			
		case R.id.user_login_activity:
			intent = new Intent(this, UserLoginActivity.class);
			startActivity(intent);
			break;
		default:
			intent = null;
			break;
		}

	}
	
	@Override
    public Dialog onCreateDialog(int dialogId) {
	        switch (dialogId) {
	        case ZIP_DIALOG:
	        	zip_dialog = new ProgressDialog(this);
	        	zip_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	zip_dialog.setCancelable(false);
	            zip_dialog.setTitle("Zapis plik�w u�ytkownika");
	            zip_dialog.setMessage("Tworzenie archiwum plik�w. Prosz� czeka�....");
	            return zip_dialog;
	            
	        case UNZIP_DIALOG:
	        	zip_dialog = new ProgressDialog(this);
	        	zip_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        	zip_dialog.setCancelable(false);
	            zip_dialog.setTitle("Import plik�w u�ytkownika");
	            zip_dialog.setMessage("Kopiowanie plik�w ze wskazanego �r�d�a. Prosz� czeka�....");
	            return zip_dialog;
	        default:
	            break;
	        }
	        return null;
	    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    switch (requestCode) {
		case FILE_SELECT_REQUEST:
			if(resultCode == RESULT_OK){
				Uri uri = data.getData();
		        final String path = Utils.getPath(this, uri);
		        if(!Utils.isZipFile(path)){
		        	Toast.makeText(App_2.getAppContext(), "B��d importowania!\n Plik " + Utils.getFilenameFromPath(path) + " jest niepoprawny" , Toast.LENGTH_LONG).show();
		        	return;
		        }
		        
		        
				new AlertDialog.Builder(this)
	            .setIcon(R.drawable.ic_action_import_export)
	            .setTitle("Zainportowa� plik"+ Utils.getFilenameFromPath(path))
	            .setMessage("Isniej�ce symbole zostan� utracone!")
	            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	    		        importUserFiles(path);
	                }
	            })
	            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	return;
	                }
	            }).create().show();
			}
			break;

		default:
			break;
		}
	 }
	
	private static class ExportUserFilesTask extends AsyncTask<String, Integer, Void>{
		private Activity executing_activity;
		private boolean includeRootFolder = false;
		
		private ExportUserFilesTask(){}
		
		public ExportUserFilesTask(Activity a, boolean includeRootFolder){
			this.executing_activity = a;
			this.includeRootFolder = includeRootFolder;
		}
					
		@Override
		protected void onPreExecute() {
			this.executing_activity.showDialog(ZIP_DIALOG,null);
		}
			
		@Override
		protected Void doInBackground(String... params) {
			String zipFilePath = params[0];
			String pathToFolder = params[1];
			
			File exp = new File(Storage.getAppRootDir()+File.separator+"exportedDB");
			if(exp.exists()){
				exp.delete();
			}
			
			Database.backupDb("exportedDB", Storage.getAppRootDir()+File.separator);
			
			File folderToAdd = new File(pathToFolder);
			if(!folderToAdd.exists() || !folderToAdd.isDirectory())
				return null;
			try{
				File f = new File(zipFilePath);
				if(f.exists())
					try {	Storage.delete(f);
					} catch (IOException e) {	e.printStackTrace();}
				
				
				ZipFile zipFile = new ZipFile(zipFilePath);
				zipFile.setRunInThread(true);
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
				parameters.setIncludeRootFolder(includeRootFolder);
				zipFile.addFolder(pathToFolder, parameters);
				ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
				
				int published = -1;
				while(progressMonitor.getState() == ProgressMonitor.STATE_BUSY){
					int progress = progressMonitor.getPercentDone();

					if(published!= progress){
						published = progress;
						publishProgress(published);
					}
					
				}
			}catch(ZipException e){
				e.printStackTrace();
			}
			
		return null;
		}
		
		protected void onProgressUpdate(Integer... progress) {
				MainActivity.zip_dialog.setProgress(progress[0]);
		}

			
			
		@Override
		protected void onPostExecute(Void result){
			this.executing_activity.removeDialog(ZIP_DIALOG);
			Toast.makeText(executing_activity.getApplicationContext(), "Pliki u�ytkownika zosta�y zapisane poprawnie", Toast.LENGTH_LONG).show();
			executing_activity = null;
		}

	}
	
	private void importUserFiles(String path){
    	File f = new File(path);
    	if(!f.exists()){
    		Toast.makeText(App_2.getAppContext(), "plik: "+ f.getAbsolutePath()+" nie istnieje" , Toast.LENGTH_LONG).show();
    	}else{
    		ImportUserFilesTask iuft = new ImportUserFilesTask(this);
    		iuft.execute(path);
    	}
		
	}
	
	private static class ImportUserFilesTask extends AsyncTask<String, Integer, Void>{
		private Activity executing_activity;
		
		private ImportUserFilesTask(){}
		
		public ImportUserFilesTask(Activity a){
			this.executing_activity = a;
		}
					
		@Override
		protected void onPreExecute() {
			this.executing_activity.showDialog(UNZIP_DIALOG,null);
		}
			
		@Override
		protected Void doInBackground(String... params) {
			String pathToZippedFile = params[0];

			
			File appRootDir = Storage.getAppRootDir();
			ZipUnzipFiles.decompress(pathToZippedFile, appRootDir.getAbsolutePath());
			File exportedDb = new File(appRootDir.getAbsolutePath()+File.separator+"exportedDB.db");
			if(exportedDb.exists())
				Database.importDb(exportedDb.getAbsolutePath());
			
			return null;
		}
		

		@Override
		protected void onPostExecute(Void result){
			this.executing_activity.removeDialog(UNZIP_DIALOG);
			Toast.makeText(executing_activity.getApplicationContext(), "Pliki zosta�y poprawnie zaimporotwane", Toast.LENGTH_LONG).show();
			executing_activity = null;
		}

	}
		
	

	
}
