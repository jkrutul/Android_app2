package com.example.app_2.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
	private static Database db_instance = null;
	private static final String LOG_TAG = "Database";			
	private static final int DATABASE_VERSION = 1;
		
	private static final String TABLE_IMAGES_CREATE = "CREATE TABLE "+
			ImageContract.TABLE_IMAGE+" ("+
			ImageContract.Columns._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			ImageContract.Columns.FILENAME+ " TEXT, "+
			ImageContract.Columns.AUDIO_PATH + " TEXT, "+
			ImageContract.Columns.DESC+ " TEXT, "+
			ImageContract.Columns.MODIFIED+ " DATETIME, "+
			ImageContract.Columns.TIME_USED+ " INTEGER DEFAULT 0 ,"+
			ImageContract.Columns.LAST_USED+ " DATETIME, "+
			ImageContract.Columns.CATEGORY+ " TEXT, "+
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
			UserContract.Columns.ROOT_FK+" INTEGER, "+
		    "FOREIGN KEY("+UserContract.Columns.ROOT_FK+") REFERENCES "+ImageContract.TABLE_IMAGE+"("+ImageContract.Columns._ID+") "+
	");";
	
	private static final String TABLE_METADATA_CREATE = "CREATE TABLE metadata (_id INTEGER PRIMARY KEY AUTOINCREMENT,  dict_fk INTEGER DEFAULT 1, root_fk INTEGER DEFAULT 2)";
		
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
	
	/* I M A G E */
	/* image - insert */
	/***
	 * Puts ImageObject to database
	 * @param mio
	 * @return inserted ImageObject
	 */
	public ImageObject insertImage1(ImageObject mio){
		ContentValues cv = new ContentValues();
		cv.put(ImageContract.Columns.FILENAME, mio.getImageName() );
		cv.put(ImageContract.Columns.CATEGORY, mio.getCategory());
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
	
	public Long insertImage(ImageObject img_object){
		String filename = img_object.getImageName();
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
		cv.put(ImageContract.Columns.FILENAME, mio.getImageName() );
		cv.put(ImageContract.Columns.CATEGORY, mio.getCategory());
		cv.put(ImageContract.Columns.DESC, mio.getDescription());
		cv.put(ImageContract.Columns.TIME_USED, mio.getTimes_used());
		return db.update(ImageContract.TABLE_IMAGE, cv, where, whereArgs);
	}
	
	public int updateImage(String imageName, ImageObject mio){
		String where = ImageContract.Columns.FILENAME + "=?";
		String[] whereArgs = {""};
		whereArgs[0] = imageName;
		ContentValues cv = new ContentValues();
		//cv.put(ImageContract.Columns._ID,mio.getId());
		cv.put(ImageContract.Columns.FILENAME, mio.getImageName() );
		cv.put(ImageContract.Columns.CATEGORY, mio.getCategory());
		cv.put(ImageContract.Columns.DESC, mio.getDescription());
		Log.i(LOG_TAG, "updating image:"+mio);
		return db.update(ImageContract.TABLE_IMAGE, cv, where, whereArgs);
	}
	
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
/*	
	public List<ImageObject> getAllImagesByCategory(long category_id){
		ImageObject mio;
		String selection = ImageContract.Columns.PARENTS+ " = ?";
		String[] selectionArgs = {""};
		selectionArgs[0] = String.valueOf(category_id);
		List<ImageObject> images = new LinkedList<ImageObject>();
		Cursor c = db.query(ImageContract.TABLE_IMAGE, null,selection, selectionArgs, null,null, null,null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			mio = cursorToImage(c);
			images.add(mio);
			c.moveToNext();
		}
		c.close();
		
		return images;
	}


	public List<String> getAllImagePathByCategory(long category_id ){
		List<String> img_paths = new LinkedList<String>();
		List<ImageObject> img_o = getAllImagesByCategory(category_id);
		for(ImageObject i : img_o){
			img_paths.add(i.getImageName());
		}
		return img_paths;
	}
	*/	
	
	/*
	public ImageObject getRootCategory(){
		ImageObject category =null;

		String[] columns = {ImageContract.Columns._ID, ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY};
		String selection =ImageContract.Columns.CATEGORY+"=\'ROOT\'";
		//String[] selectionArgs = {""};
		//selectionArgs[0] = "\'ROOT\'";

		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category = cursorToCategory(c);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}
		return category;
	}
*/	
	public List<ImageObject> getAllImages(){
		ImageObject mio;
		
		String selection =ImageContract.Columns.CATEGORY+" NOT LIKE \'ROOT\'";
		String[] selectionArgs = {""};
		//selectionArgs[0] = "\'ROOT\'";		
		
		List<ImageObject> images = new LinkedList<ImageObject>();
		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, null, selection,null /*selectionArgs*/,null, null,ImageContract.Columns._ID);
			c.moveToFirst();
			//if(!c.isAfterLast())
			//	c.moveToNext();
			if(c.getCount()>0){
				while(!c.isAfterLast()){
					mio = cursorToImage(c);
					images.add(mio);
					c.moveToNext();
				}
			}
			c.close();
		}
		catch(SQLException ex){
			Log.w(LOG_TAG,ex);
		}
		
		return images;
	}
	
	public static Long getMainDictFk(){
		Cursor c = db.query("metadata", new String[]{"dict_fk"}, null ,null,null, null, "_id");
		c.moveToFirst();
		if(!c.isAfterLast()){
			return c.getLong(0);
		}
		else
			return null;
	}
	
	public static Long getMainRootFk(){
		Cursor c = db.query("metadata", new String[]{"root_fk"}, null ,null,null, null, "_id");
		c.moveToFirst();
		if(!c.isAfterLast()){
			return c.getLong(0);
		}
		else
			return null;
	}
	
/*	
	public static Cursor getCursorOfAllImages(){
		String selection =ImageContract.Columns.CATEGORY+" NOT LIKE \'ROOT\'";
		String[] selectionArgs = {""};
		selectionArgs[0] = "\'ROOT\'";		
		
		try{
			if(db == null)
				open();
			return db.query(ImageContract.TABLE_IMAGE, null, selection, null,null, null,null);
			}
		catch(SQLException ex){
			Log.w(LOG_TAG,ex);
		}
		return null;
	}
	
	public List<ImageObject> getAllCategories(){
		ImageObject category;
		List<ImageObject> categories = new LinkedList<ImageObject>();
		String[] columns = {ImageContract.Columns._ID, ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY};
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL ";
		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category = cursorToImage(c);
				//category = c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}
		
		return categories;
	}
	*/
/*	
	public List<ImageObject> getSubcategories(Long img_id){
		ImageObject category = new ImageObject();
		List<ImageObject> categories = new LinkedList<ImageObject>();

		String[] columns = {ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY};
		//String selection = ImageContract.Columns.CATEGORY + " NOTNULL AND "+ImageContract.Columns.PARENTS+"="+img_id.toString();
		String selection = ImageContract.Columns.CATEGORY + " != ? AND " +ImageContract.Columns.PARENTS+"="+img_id.toString();
		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, columns,selection, new String[] {"null"},null, null,null);
			//Cursor c = db.rawQuery("SELECT "+ImageContract.Columns._ID+","+ImageContract.Columns.PATH+","+ImageContract.Columns.CATEGORY+" FROM "+ImageContract.TABLE_IMAGE+" WHERE ("+ ImageContract.Columns.CATEGORY+" IS NOT NULL) AND "+ImageContract.Columns.PARENTS+ " = " + img_id.toString()+";", null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category.setId(			c.getLong(		0));
				category.setImageName(	c.getString(	1));
				category.setCategory(	c.getString(	2));
				//category = c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}
		return categories;
	}
	
	public List<ImageObject> getCategoryLeafs(Long img_id){
		ImageObject category;
		List<ImageObject> categories = new LinkedList<ImageObject>();

		String[] columns = {ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY};
		String selection = ImageContract.Columns.CATEGORY + " IS NULL AND "+ImageContract.Columns.PARENTS+"="+img_id.toString();
		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category = cursorToImage(c);
				//category = c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}
		return categories;
	}
	
	public List<ImageObject> getParentCategory(Long img_id){
		ImageObject category = new ImageObject();
		List<ImageObject> categories = new LinkedList<ImageObject>();
		String[] columns = {ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY};
		String selection = ImageContract.Columns._ID +"=(SELECT "+ImageContract.Columns.PARENTS+" FROM " +ImageContract.TABLE_IMAGE+" WHERE "+ImageContract.Columns._ID+"="+img_id.toString()+");";                    //ImageContract.Columns.CATEGORY + "IS NOT NULL AND "+COL_; //SELECT * FROM IMAGE WHERE ID = (SELECT ImageContract.Columns.PARENTS FROM IMAGE WHERE ID = 
		try{
			Cursor c = db.query(ImageContract.TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){		
				category.setId(			c.getLong(		0));
				category.setImageName(	c.getString(	1));
				category.setCategory(	c.getString(	2));
				//category = c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}

		return categories;
	}
*/	
	/* image - cursor */
	private static ImageObject cursorToImage(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns._ID)));
		mio.setImageName(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.FILENAME)));
		mio.setAudioPath(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.AUDIO_PATH)));
		mio.setDescription(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.DESC)));
		mio.setModified(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.MODIFIED)));
		mio.setTimes_used(	cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns.TIME_USED)));
		mio.setLast_used(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.LAST_USED)));
		mio.setCategory(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.CATEGORY)));
		return mio;
	}
	
	private static ImageObject cursorToCategory(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(ImageContract.Columns._ID)));
		mio.setImageName(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.FILENAME)));
		mio.setCategory(	cursor.getString(	cursor.getColumnIndex(ImageContract.Columns.CATEGORY)));
		return mio;
	}
	
	public void exportImageToCsv(String filename){
		File f  = ExternalStorage.getExternalStorageDir(filename);
	    try{
	    	FileOutputStream out = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(out);
			for(ImageObject img_o : getAllImages()){       
				osw.write(img_o+"\n");
			}
			osw.flush();
			osw.close();
		}catch (Exception e) {
			Log.e(LOG_TAG, "File write failed:" + e.toString());
		}	
	}
	
	public void importImageFromCsv(String filename){
		File file = new File(Environment.getExternalStorageDirectory(),	filename);
		if(file.exists()){
			try{
				FileInputStream in = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = null;
				String tv[] = null;
				recreateDB();
				while((line = reader.readLine())!=null){
					tv = line.split("\\;"); //ID imageName, AUDIOPATH, DESCRIPTION, times_used, modified, last_used, category,parent_fk 
					ImageObject io = new ImageObject(tv[1],tv[2],tv[3],tv[7]);  //String imageName, String audioPath, String description,String category, Long paretn_fk
					insertImage(io);
				}
				in.close();
			}catch(Exception e){
				Log.e(LOG_TAG, "File read failed:" + e.toString());
			}
		}else
			Log.i(LOG_TAG, "file not exists");
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
				cv.put(ImageContract.Columns.CATEGORY, "MAIN_DICT");
				cv.put(ImageContract.Columns.DESC,  "MAIN_DICT");
				cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
				main_dict=  _db.insert(ImageContract.TABLE_IMAGE, null, cv);
				
				cv.clear();
				cv.put(ImageContract.Columns.CATEGORY, "MAIN_ROOT");
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
					Log.e(LOG_TAG, "nie tworzono s³ownika:"+main_dict+" lub korzenia: "+main_root);
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
	}
}
