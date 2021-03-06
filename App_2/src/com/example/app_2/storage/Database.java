package com.example.app_2.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.app_2.App_2;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.models.ImageObject;
import com.example.app_2.utils.Utils;



public class Database {
	private static SQLiteDatabase db;		// Variable to hold the database instance
	public static myDbHelper dbHelper; 	// Database open/upgrade helper
	private static Context context = null;
	
	private static SimpleDateFormat dateFormat;
	private static Date date;
	
	private static final String DATABASE_NAME="myDatabase.db";
	public static String DB_FILEPATH = "/data/data/com.example.app_2/databases/"+DATABASE_NAME;
	
	private static Database db_instance = null;
	private static final String LOG_TAG = "Database";			
	private static final int DATABASE_VERSION = 1;
		
	private static final String TABLE_IMAGES_CREATE = "CREATE TABLE "+
			ImageContract.TABLE_IMAGE+" ("+
			ImageContract.Columns._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			ImageContract.Columns.FILENAME+ " TEXT, "+
			ImageContract.Columns.DESC+ " TEXT, "+
			ImageContract.Columns.TTS_M+" TEXT, "+
			ImageContract.Columns.TTS_F+" TEXT, "+
			ImageContract.Columns.MODIFIED+ " DATETIME, "+
			ImageContract.Columns.TIME_USED+ " INTEGER DEFAULT 0 ,"+
			ImageContract.Columns.LAST_USED+ " DATETIME, "+
			ImageContract.Columns.IS_CATEGORY+ " INTEGER DEFAULT 0, "+
			ImageContract.Columns.IS_ADD_TO_EXPR+ " INTEGER DEFAULT 1, "+
			ImageContract.Columns.IS_ADD_TO_CAT_LIST+" INTEGER DEFAULT 0, "+
			ImageContract.Columns.AUTHOR_FK+ " INTEGER "+
	");";
	
	private static final String TABLE_PARENT_CREATE = "CREATE TABLE "+
			ParentContract.TABLE_PARENT+" ("+
			ParentContract.Columns._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			ParentContract.Columns.IMAGE_FK+" INTEGER DEFAULT 0, "+
			ParentContract.Columns.PARENT_FK+" INTEGER DEFAULT 0, "+
		    "FOREIGN KEY("+ParentContract.Columns.PARENT_FK+") REFERENCES "+ImageContract.TABLE_IMAGE+"("+ImageContract.Columns._ID+"), "+
			"FOREIGN KEY("+ParentContract.Columns.IMAGE_FK+") REFERENCES "+ ImageContract.TABLE_IMAGE+"("+ImageContract.Columns._ID+") " +
	");";
	
	private static final String TABLE_USER_CREATE = "CREATE TABLE "+
			UserContract.TABLE_USER+" ("+
			UserContract.Columns._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			UserContract.Columns.USERNAME+" TEXT, "+
			UserContract.Columns.IMG_FILENAME+" TEXT, "+
			UserContract.Columns.ISMALE+" INTEGER DEFAULT 1, "+
			UserContract.Columns.FONT_SIZE+" INTEGER DEFAULT 15, "+
			UserContract.Columns.IMG_SIZE +" INTEGER DEFAULT 150, "+
			UserContract.Columns.CAT_BACKGROUND+" TEXT, "+
			UserContract.Columns.CONTEXT_CAT_BACKGROUND+" TEXT, "+
			UserContract.Columns.ROOT_FK+" INTEGER, "+
		    "FOREIGN KEY("+UserContract.Columns.ROOT_FK+") REFERENCES "+ImageContract.TABLE_IMAGE+"("+ImageContract.Columns._ID+") "+
	");";
	
	private static final String TABLE_METADATA_CREATE = "CREATE TABLE metadata (dict_fk INTEGER DEFAULT 1, root_fk INTEGER DEFAULT 2)";
		
	private static final String CREATE_UNIQUE_INDEX_ON_PARENT = "CREATE UNIQUE INDEX "+
			"parent_idx ON "+ParentContract.TABLE_PARENT+"("+ParentContract.Columns._ID+","+ParentContract.Columns.IMAGE_FK+","+ParentContract.Columns.PARENT_FK+");";
		    
	public static void recreateDB(){
		open();
		String drop_table = "DROP TABLE IF EXISTS ";
		db.execSQL(drop_table+"metadata");
		db.execSQL(drop_table+ParentContract.TABLE_PARENT);
		db.execSQL(drop_table+ImageContract.TABLE_IMAGE);
		db.execSQL(drop_table+UserContract.TABLE_USER);
		db.execSQL(drop_table+"metadata");
		//db.execSQL("drop index if exists parent_idx");
		dbHelper.onCreate(db);
	}
	
	private Database(Context context ){
		// set the format to sql date time
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		date = new Date();
		
		this.context = context;
		dbHelper = myDbHelper.getInstance(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static Database getInstance(Context context){
		if (db_instance == null){
			db_instance = new Database(context);
			return db_instance;
		}else
			return db_instance;
	}
	
	public static Database open() throws SQLException{
		if(dbHelper==null)
			getInstance(App_2.getAppContext());
		
				try{
					db = dbHelper.getWritableDatabase();
				}catch(SQLException ex){
					Log.w(LOG_TAG, "Database not open");
					db = dbHelper.getReadableDatabase();
				}
			return db_instance;
	}
	
	public void close(){
		dbHelper.close();
	}
	
	
	public static String backupDb(String filename){
		
		String dbDirPath = Storage.getBackupDir().getAbsolutePath()+File.separator; //Storage.getAppRootDir()+File.separator+"backups"+File.separator;
		filename+=".db";
		dbDirPath+=filename;
		try {

			if(dbHelper.exportDatabase(dbDirPath))
				return filename;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String backupDb(String filename, String dbDirPath ){
		File exportDbDir = new File(dbDirPath);
		if( !exportDbDir.exists() || !exportDbDir.isDirectory()){
			exportDbDir.mkdirs();
		}
		
		filename+=".db";
		dbDirPath+=filename;
		try {

			if(dbHelper.exportDatabase(dbDirPath))
				return filename;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean filenameVerification(String _filename){
		int count = -1;
		Cursor c = null;
		try{
			String query = "select count(*) from "+ImageContract.TABLE_IMAGE+" where " + ImageContract.Columns.FILENAME+" =?";
			c = db.rawQuery(query, new String[]{_filename});
			if(c.moveToFirst()){
				count = c.getInt(0);
			}
			return count > 0;
		}finally{
			if(c!=null){
				c.close();
			}
		}
	}
	
	
	public static boolean importDb(String dbPath){
		try {
			return dbHelper.importDatabase(dbPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/* I M A G E */
	/* image - insert */
	/***
	 * Puts ImageObject to database
	 * @param mio
	 * @return inserted ImageObject
	 */
	
	/*
	public ImageObject insertImage1(ImageObject mio){
		ContentValues cv = new ContentValues();
		cv.put(ImageContract.Columns.FILENAME, mio.getImageName() );
		cv.put(ImageContract.Columns.IS_CATEGORY, mio.getCategory());
		cv.put(ImageContract.Columns.DESC, mio.getDescription());
		cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));


		if(db == null)
			open();
		long l = db.insert(ImageContract.TABLE_IMAGE, null, cv);
	    if(l==-1){
	    	return null;
	    }
		Cursor c = db.query(ImageContract.TABLE_IMAGE,  null, ImageContract.Columns._ID+" = ?", new String[] {String.valueOf(l)},null, null,null);
		c.moveToFirst();
		mio = cursorToImage(c);
		c.close();
		return mio;
	}
	*/
	
	public Long insertImage(ImageObject img_object){
		String filename = img_object.getFilename();
		String description = img_object.getDescription();
		if(description == null)
			description = Utils.cutExtention(filename);
		
		ContentValues cv = new ContentValues();
		cv.put(ImageContract.Columns.FILENAME, filename);
		cv.put(ImageContract.Columns.DESC,  description);
		cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
		cv.put(ImageContract.Columns.AUTHOR_FK, img_object.getAuthor_fk());
		return db.insert(ImageContract.TABLE_IMAGE, null, cv);
	}
	
	/* image - remove */
	public boolean deleteImage(ImageObject mio){
		long row_id = mio.getId();
		String where = ImageContract.Columns._ID + "=?";
		String[] whereArgs = {""};
		whereArgs[0]=String.valueOf(row_id);
		return db.delete(ImageContract.TABLE_IMAGE, where, whereArgs)>0;
	}
	
	/* image - update */
	public int updateImage(long _rowIndex, ImageObject mio){
		String where = ImageContract.Columns._ID + "=?";
		String[] whereArgs = {""};
		whereArgs[0] = String.valueOf(_rowIndex);
		ContentValues cv = new ContentValues();
		cv.put(ImageContract.Columns._ID,mio.getId());
		cv.put(ImageContract.Columns.FILENAME, mio.getFilename() );
		
		cv.put(ImageContract.Columns.IS_CATEGORY, mio.isCategory());
		cv.put(ImageContract.Columns.IS_ADD_TO_EXPR, mio.isAddToExpr());
		cv.put(ImageContract.Columns.IS_ADD_TO_CAT_LIST, mio.isAddToCatList());
		cv.put(ImageContract.Columns.DESC, mio.getDescription());
		cv.put(ImageContract.Columns.TTS_M, mio.getTts_m());
		cv.put(ImageContract.Columns.TTS_F, mio.getTts_f());
		cv.put(ImageContract.Columns.TIME_USED, mio.getTimes_used());
		return db.update(ImageContract.TABLE_IMAGE, cv, where, whereArgs);
	}
	
	/*
	public int updateImage(String imageName, ImageObject mio){
		String where = ImageContract.Columns.FILENAME + "=?";
		String[] whereArgs = {""};
		whereArgs[0] = imageName;
		ContentValues cv = new ContentValues();
		//cv.put(ImageContract.Columns._ID,mio.getId());
		cv.put(ImageContract.Columns.FILENAME, mio.getFilename() );
		cv.put(ImageContract.Columns.IS_CATEGORY, mio.getCategory());
		cv.put(ImageContract.Columns.DESC, mio.getDescription());
		Log.i(LOG_TAG, "updating image:"+mio);
		return db.update(ImageContract.TABLE_IMAGE, cv, where, whereArgs);
	}
	*/
	
	public int updateImageCounter(ImageObject io){
		long used_counter = io.getTimes_used();
		if(used_counter < 99999){
			used_counter++;
			io.setTimes_used(used_counter);
			return updateImage(io.getId(), io);
		}
		else
			return 0;
		
	}
	
	/* image - get */
	public ImageObject getImage(long _rowIndex){
		String selection =ImageContract.Columns._ID+"=?";
		String[] selectionArgs = {""};
		selectionArgs[0] = String.valueOf(_rowIndex);
		Cursor c = db.query(ImageContract.TABLE_IMAGE,  null,selection, selectionArgs,null, null,null);
		c.moveToFirst();
		return cursorToImage(c);
	}
	
	public ImageObject isImageAlreadyExist(String imageName){
		String selection =ImageContract.Columns.FILENAME+" = ?";
		String[] selectionArgs = {""};
		selectionArgs[0] = imageName;
		Cursor c = db.query(ImageContract.TABLE_IMAGE,  null, selection, selectionArgs,null, null,null);
		c.moveToFirst();
		if(c.getCount()>0)
			return cursorToImage(c);
		return null;
	}


	public static Long getMainDictFk(){
		Cursor c = db.query("metadata", new String[]{"dict_fk"}, null ,null,null, null, null);
		c.moveToFirst();
		if(!c.isAfterLast()){
			return c.getLong(0);
		}
		else
			return null;
	}
	
	public static Long getMainRootFk(){
		Cursor c = db.query("metadata", new String[]{"root_fk"}, null ,null,null, null, null);
		c.moveToFirst();
		if(!c.isAfterLast()){
			return c.getLong(0);
		}
		else
			return null;
	}

	/* image - cursor */
	private static ImageObject cursorToImage(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns._ID)));
		mio.setFilename(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.FILENAME)));
		mio.setDescription(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.DESC)));
		mio.setModified(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.MODIFIED)));
		mio.setTimes_used(	cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns.TIME_USED)));
		mio.setLast_used(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.LAST_USED)));
		mio.setCategory(	cursor.getInt(	cursor.getColumnIndex(ImageContract.Columns.IS_CATEGORY)));
		return mio;
	}
	
	private static ImageObject cursorToCategory(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns._ID)));
		mio.setFilename(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.FILENAME)));
		mio.setCategory(	cursor.getInt(	cursor.getColumnIndex(ImageContract.Columns.IS_CATEGORY)));
		return mio;
	}
	
	


	
	//----
	public static class myDbHelper extends SQLiteOpenHelper{

		@Override
		public void onOpen(SQLiteDatabase db) {
		    super.onOpen(db);
		    if (!db.isReadOnly()) {
		        // Enable foreign key constraints
		        //db.execSQL("PRAGMA foreign_keys=ON;");
		    }
		}
		private static myDbHelper instance = null;
		
		public myDbHelper(Context context, String name, CursorFactory factory, int version){
			super(context,name,factory,version);
		}
		
		public static myDbHelper getInstance(Context context, String name, CursorFactory factory, int version){
			if(instance==null){
				instance = new myDbHelper(context, name, factory,version);
				return instance;
			}
			else
				return instance;
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			try{
				_db.execSQL(TABLE_IMAGES_CREATE);
				_db.execSQL(TABLE_PARENT_CREATE);
				_db.execSQL(TABLE_USER_CREATE);
				_db.execSQL(TABLE_METADATA_CREATE);
				//_db.execSQL(CREATE_UNIQUE_INDEX_ON_PARENT);
				long main_dict, main_root;
				
				ContentValues cv = new ContentValues();
				cv.put(ImageContract.Columns.IS_CATEGORY, true);
				cv.put(ImageContract.Columns.DESC,  "MAIN_DICT");
				cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
				main_dict=  _db.insert(ImageContract.TABLE_IMAGE, null, cv);
				
				cv.clear();
				cv.put(ImageContract.Columns.IS_CATEGORY, true);
				cv.put(ImageContract.Columns.DESC,  "MAIN_ROOT");
				cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
				main_root =  _db.insert(ImageContract.TABLE_IMAGE, null, cv);
				
				if(main_dict!= -1 && main_root != -1){
					cv = new ContentValues();
					cv.put("dict_fk", main_dict);
					cv.put("root_fk", main_root);
					_db.insert("metadata", null, cv);
				}
				else{
					Log.e(LOG_TAG, "nie tworzono s�ownika:"+main_dict+" lub korzenia: "+main_root);
				}
				
				App_2.setMain_dict_id(main_dict);
				


		
			}catch(SQLException ex){
				Log.w(LOG_TAG, ex);
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String drop_table = "DROP TABLE IF EXISTS ";
			// Log the version upgrade/
			Log.w("TaskDBAdapter", "Upgrading from version "+	oldVersion + " to "+ newVersion + ", witch will destroy all old data");
			db.execSQL(drop_table+"metadata");
			db.execSQL(drop_table+ImageContract.TABLE_IMAGE);
			//db.execSQL("drop index if exists parent_idx");
			db.execSQL(drop_table+ParentContract.TABLE_PARENT);
			db.execSQL(drop_table+UserContract.TABLE_USER);
			
			onCreate(db);
			
		}
	
	
	public boolean exportDatabase(String exDbPath) throws IOException{
		close();
		File savedDb = new File(exDbPath);
		File currentDb = new File(DB_FILEPATH);
		if(!currentDb.exists())
			return false;

		if(savedDb.createNewFile()){
			Storage.copyFile(new FileInputStream(currentDb), new FileOutputStream(savedDb));
			open();
			return true;
		}
		else{
			open();
			return false;
		}

	}
		 
	/**
	 * Copies the database file at the specified location over the current
	 * internal application database.
	 * */
	public boolean importDatabase(String dbPath) throws IOException {
	
	    // Close the SQLiteOpenHelper so it will commit the created empty
	    // database to internal storage.
	    close();
	    File newDb = new File(dbPath);
	    File oldDb = new File(DB_FILEPATH);
	    if (newDb.exists()) {
	        Storage.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
	        // Access the copied database so SQLiteHelper will cache it and mark
	        // it as created.
	        getWritableDatabase().close();
	        open();
	        return true;
	    }
	    
	    return false;}
	}
		
		
}
