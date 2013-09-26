package com.example.app_2.storage;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.app_2.models.ImageObject;



public class Database {
	private static SQLiteDatabase db;		// Variable to hold the database instance
	public static myDbHelper dbHelper; 	// Database open/upgrade helper
	private static Context context = null;
	
	private static SimpleDateFormat dateFormat;
	private static Date date;
	
	private static final String DATABASE_NAME="myDatabase.db";
	private static Database db_instance = null;
	private static final String LOG_TAG = "Database";
	
	
	// The name and the column index of each column in your database
	// The index (key) column name for use in where clauses.
	public static final String KEY_ID="_id";
	
	public static final String TABLE_IMAGE = "image"; 					// KEY_ID, COL_PATH, COL_AUDIO_PATH, COL_DESC, COL_CAT
	public static final String COL_PATH = "path";
	public static final String COL_AUDIO_PATH = "audio";
	public static final String COL_DESC = "description";
	public static final String COL_TIME_USED = "used";
	public static final String COL_MODIFIED  = "last_modified";
	public static final String COL_LAST_USED = "last_used";
	public static final String COL_IS_CAT = "is_category";
	public static final String COL_CAT = "category";
	public static final String COL_PARENT = "parent_fk";
		
    private static final String TABLE_DIC = "dictionary";
    private static final String COL_WORD = "word";
    private static final String COL_DEFINITION = "definition";
    
    private static final String TABLE_HTTP = "httpimages";
    private static final String COL_URL  = "url";
    
	
	private static final int DATABASE_VERSION = 1;
		
	private static final String TABLE_IMAGES_CREATE = "CREATE TABLE "+
	TABLE_IMAGE+" ("+
			KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
			COL_PATH+ " TEXT, "+
			COL_AUDIO_PATH + " TEXT, "+
			COL_DESC+ " TEXT, "+
			COL_MODIFIED+ " DATETIME, "+
			COL_TIME_USED+ " INTEGER DEFAULT 0 ,"+
			COL_LAST_USED+ " DATETIME, "+
			COL_IS_CAT+ " INTEGER DEFAULT 0, "+
			COL_CAT+ " TEXT, "+
			COL_PARENT+ " INTEGER DEFAULT 0, "+
		    "FOREIGN KEY("+COL_PARENT+") REFERENCES "+TABLE_IMAGE+"("+KEY_ID+") "+
	");";
	
	
    private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "+
    		TABLE_DIC+ " ("+
    		KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COL_WORD + " TEXT, " +
            COL_DEFINITION + " TEXT" +
    ");";
 
    private static final String HTTP_IMG_TABLE_CREATE = "CREATE TABLE "+
    		TABLE_HTTP+" ("+
    		KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            COL_URL + " TEXT" +
    ");";
	
    //private static final String INSERT_MAIN_CATEGORY = "INSERT INTO " +
    //TABLE_CAT+"("+KEY_ID+","+COL_NAME+") VALUES(0,\'GLÓWNA\');";    
    
	public void recreateDB(){
		open();
		String drop_table = "DROP TABLE IF EXISTS ";
		db.execSQL(drop_table+TABLE_IMAGE);
		//db.execSQL(drop_table+TABLE_HTTP);

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
			getInstance(context);
		
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
	
	
	/* I M A G E */
	/* image - insert */
	/***
	 * Puts ImageObject to database
	 * @param mio
	 * @return inserted ImageObject
	 */
	public ImageObject insertImage(ImageObject mio){
		ContentValues cv = new ContentValues();
		cv.put(COL_PATH, mio.getImageName() );
		cv.put(COL_CAT, mio.getCategory());
		cv.put(COL_DESC, mio.getDescription());
		cv.put(COL_PARENT, mio.getParent_fk());
		cv.put(COL_MODIFIED, dateFormat.format(date));


		if(db == null)
			open();
		long l = db.insert(TABLE_IMAGE, null, cv);
	    if(l==-1){
	    	return null;
	    }
		Cursor c = db.query(TABLE_IMAGE,  null, KEY_ID+" = "+l, null,null, null,null);
		c.moveToFirst();
		mio = cursorToImage(c);
		c.close();
		return mio;
	}
	
	/* image - remove */
	public boolean deleteImage(ImageObject mio){
		long row_id = mio.getId();
		return db.delete(TABLE_IMAGE, KEY_ID+" = "+row_id, null)>0;
	}
	
	/* image - update */
	public int updateImage(long _rowIndex, ImageObject mio){
		String where = KEY_ID + "=" + _rowIndex;
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID,mio.getId());
		cv.put(COL_PATH, mio.getImageName() );
		cv.put(COL_CAT, mio.getCategory());
		cv.put(COL_DESC, mio.getDescription());
		return db.update(TABLE_IMAGE, cv, where, null);
	}
	
	/* image - get */
	public ImageObject getImage(long _rowIndex){
		Cursor c = db.query(TABLE_IMAGE,  null, KEY_ID+" = "+_rowIndex, null,null, null,null);
		c.moveToFirst();
		return cursorToImage(c);
	}
	
	public ImageObject isImageAlreadyExist(String imageName){
		Cursor c = db.query(TABLE_IMAGE,  null, COL_PATH+" = "+"\""+imageName+"\"", null,null, null,null);
		c.moveToFirst();
		if(c.getCount()>0)
			return cursorToImage(c);
		return null;
	}
	
	public List<ImageObject> getAllImagesByCategory(long category_id){
		ImageObject mio;
		List<ImageObject> images = new LinkedList<ImageObject>();
		Cursor c = db.query(TABLE_IMAGE, null,COL_PARENT+ " = "+category_id, null,null, null,null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			mio = cursorToImage(c);
			images.add(mio);
			c.moveToNext();
		}
		c.close();
		
		return images;
	}

	/***
	 * Returns paths to images filtered by category_id
	 * @param category_id
	 * @return
	 */
	public List<String> getAllImagePathByCategory(long category_id ){
		List<String> img_paths = new LinkedList<String>();
		List<ImageObject> img_o = getAllImagesByCategory(category_id);
		for(ImageObject i : img_o){
			img_paths.add(i.getImageName());
		}
		return img_paths;
	}
	
	public ImageObject getRootCategory(){
		ImageObject category =null;

		String[] columns = {KEY_ID, COL_PATH, COL_CAT};
		String selection = COL_CAT+"=\"ROOT\"";
		try{
			Cursor c = db.query(TABLE_IMAGE, columns,selection, null,null, null,null);
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
	
	public List<ImageObject> getAllImages(){
		ImageObject mio;
		List<ImageObject> images = new LinkedList<ImageObject>();
		//String selection = COL_CAT+" NOT LIKE \"ROOT\" ";
		//String sql = "select * from "+TABLE_IMAGE+" where " +COL_CAT+ " not like \"ROOT\";";
		try{
			Cursor c = db.query(TABLE_IMAGE, null,null, null,null, null,null);
			//c = db.rawQuery(sql, null);
			c.moveToFirst();
			if(!c.isAfterLast())
				c.moveToNext();
			while(!c.isAfterLast()){
				mio = cursorToImage(c);
				images.add(mio);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){
			Log.w(LOG_TAG,ex);
		}
		
		return images;
	}
	
	
	public static Cursor getCursorOfAllImages(){
		Cursor c=null;
		String selection = COL_CAT+"!= \"ROOT\"";
		
		try{
			if(db == null)
				open();
			c = db.query(TABLE_IMAGE, null,selection, null,null, null,null);
			}
		catch(SQLException ex){
			Log.w(LOG_TAG,ex);
		}
		return c;
	}
	
	public List<ImageObject> getAllCategories(){
		ImageObject category;
		List<ImageObject> categories = new LinkedList<ImageObject>();
		String[] columns = {KEY_ID, COL_PATH, COL_CAT};
		String selection = COL_CAT + " IS NOT NULL ";
		try{
			Cursor c = db.query(TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category = cursorToImage(c);
				//category = c.getString(c.getColumnIndex(COL_CAT));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}
		
		return categories;
	}
	
	public List<ImageObject> getSubcategories(Long img_id){
		ImageObject category;
		List<ImageObject> categories = new LinkedList<ImageObject>();

		String[] columns = {KEY_ID, COL_PATH, COL_CAT};
		String selection = COL_CAT + " IS NOT NULL AND "+COL_PARENT+"="+img_id.toString();
		try{
			Cursor c = db.query(TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				//category = cursorToCategory(c);
				category = cursorToImage(c);
				//category = c.getString(c.getColumnIndex(COL_CAT));
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

		String[] columns = {KEY_ID, COL_PATH, COL_CAT};
		String selection = COL_CAT + " IS NULL AND "+COL_PARENT+"="+img_id.toString();
		try{
			Cursor c = db.query(TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				category = cursorToImage(c);
				//category = c.getString(c.getColumnIndex(COL_CAT));
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
		String[] columns = {KEY_ID, COL_PATH, COL_CAT};
		String selection = KEY_ID +"=(SELECT "+COL_PARENT+" FROM " +TABLE_IMAGE+" WHERE "+KEY_ID+"="+img_id.toString()+");";                    //COL_CAT + "IS NOT NULL AND "+COL_; //SELECT * FROM IMAGE WHERE ID = (SELECT COL_PARENT FROM IMAGE WHERE ID = 
		try{
			Cursor c = db.query(TABLE_IMAGE, columns,selection, null,null, null,null);
			c.moveToFirst();
			while(!c.isAfterLast()){		
				category.setId(			c.getLong(		0));
				category.setImageName(	c.getString(	1));
				category.setCategory(	c.getString(	2));
				//category = c.getString(c.getColumnIndex(COL_CAT));
				categories.add(category);
				c.moveToNext();
			}
			c.close();
		}
		catch(SQLException ex){Log.w(LOG_TAG,ex);}

		return categories;
	}
	
	/* image - cursor */
	private static ImageObject cursorToImage(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(KEY_ID)));
		mio.setImageName(	cursor.getString(	cursor.getColumnIndex(COL_PATH)));
		mio.setAudioPath(	cursor.getString(	cursor.getColumnIndex(COL_AUDIO_PATH)));
		mio.setDescription(	cursor.getString(	cursor.getColumnIndex(COL_DESC)));
		mio.setModified(	cursor.getString(	cursor.getColumnIndex(COL_MODIFIED)));
		mio.setTimes_used(	cursor.getLong(		cursor.getColumnIndex(COL_TIME_USED)));
		mio.setLast_used(	cursor.getString(	cursor.getColumnIndex(COL_LAST_USED)));
		mio.setParent_fk(	cursor.getLong(		cursor.getColumnIndex(COL_PARENT)));
		mio.setCategory(	cursor.getString(	cursor.getColumnIndex(COL_CAT)));
		return mio;
	}
	
	private static ImageObject cursorToCategory(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(KEY_ID)));
		mio.setImageName(	cursor.getString(	cursor.getColumnIndex(COL_PATH)));
		mio.setCategory(	cursor.getString(	cursor.getColumnIndex(COL_CAT)));
		return mio;
	}
	
	
	
	
	public void insertHttpTable(String[] http_img){
		ContentValues cv = new ContentValues();
		for(int i=0; i<http_img.length; i++){
			cv.put(COL_URL, http_img[i]);
			if(db.insert(TABLE_HTTP, null, cv)==-1){
				Log.w(LOG_TAG, "Value: "+http_img[i] + " not save in DB");
			}
		}

	}
	
	
/*
	public long insertWord(MyWordObject mwo){
		ContentValues cv = new ContentValues();
		cv.put(COL_WORD,mwo.getKey_word());
		cv.put(COL_DEFINITION, mwo.getKey_definition());
		return db.insert(TABLE_DIC, null,cv);
		
	}
	*/
	
	//----
	public static class myDbHelper extends SQLiteOpenHelper{
		
		private static myDbHelper instance = null;
		
		private myDbHelper(Context context, String name, CursorFactory factory, int version){
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
				ImageObject img_o = new ImageObject();
				img_o.setCategory("ROOT");
				
				db_instance.insertImage(img_o);
				
				//_db.execSQL(HTTP_IMG_TABLE_CREATE);				
			}catch(SQLException ex){
				Log.w(LOG_TAG, ex);
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String drop_table = "DROP TABLE IF EXISTS ";
			// Log the version upgrade/
			Log.w("TaskDBAdapter", "Upgrading from version "+	oldVersion + " to "+ newVersion + ", witch will destroy all old data");
			db.execSQL(drop_table+TABLE_IMAGE);
			//db.execSQL(drop_table+TABLE_DESC);
			//db.execSQL(drop_table+TABLE_HTTP);
			onCreate(db);
			
		}
	}
}
