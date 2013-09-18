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

import com.example.app_2.models.CategoryObject;
import com.example.app_2.models.ImageObject;



public class Database {
	private static SQLiteDatabase db;		// Variable to hold the database instance
	public static myDbHelper dbHelper; 	// Database open/upgrade helper
	private static Context context = null;
	
	SimpleDateFormat dateFormat;
	Date date;
	
	private static final String DATABASE_NAME="myDatabase.db";
	private static Database instance = null;
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
	private static final Map<String, Integer> imgMap;
	static{
		Map<String, Integer> mImg = new HashMap<String, Integer>();
		mImg.put(KEY_ID, 0);
		mImg.put(COL_PATH,1);
		mImg.put(COL_AUDIO_PATH,2);
		mImg.put(COL_DESC, 3);
		mImg.put(COL_CAT, 4);
		imgMap = Collections.unmodifiableMap(mImg);
	}
	
	
	private static final String TABLE_CAT = "category";
	private static final String COL_NAME = "name";
	
    private static final String TABLE_DIC = "dictionary";
    private static final String COL_WORD = "word";
    private static final String COL_DEFINITION = "definition";
    
    private static final String TABLE_HTTP = "httpimages";
    private static final String COL_URL  = "url";
    
	
	private static final int DATABASE_VERSION = 1;
	
	
	private static final String TABLE_CAT_CREATE = "CREATE TABLE "+
	TABLE_CAT+" ("+
			KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
			COL_NAME+ " TEXT NOT NULL "+
	");";	
	
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
			COL_CAT+ " INTEGER DEFAULT 0, "+
			COL_PARENT+ " INTEGER DEFAULT 0, "+
		    "FOREIGN KEY("+COL_PARENT+") REFERENCES "+TABLE_IMAGE+"("+KEY_ID+") "+
		    "FOREIGN KEY("+COL_CAT+") REFERENCES "+TABLE_CAT+"("+KEY_ID+") "+
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
	
    private static final String INSERT_MAIN_CATEGORY = "INSERT INTO " +
    TABLE_CAT+"("+KEY_ID+","+COL_NAME+") VALUES(0,\'GLOWNA\');";    //INSERT INTO category(name) VALUES ('jejkujejku');
    
	public void recreateDB(){
		open();
		String drop_table = "DROP TABLE IF EXISTS ";
		db.execSQL(drop_table+TABLE_IMAGE);
		//db.execSQL(drop_table+TABLE_DESC);
		db.execSQL(drop_table+TABLE_CAT);
		db.execSQL(drop_table+TABLE_HTTP);
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
		if (instance == null){
			instance = new Database(context);
			return instance;
		}else
			return instance;
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
			return instance;
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
		cv.put(COL_CAT, mio.getCategory_fk());
		cv.put(COL_DESC, mio.getDescription());
		cv.put(COL_PARENT, mio.getParent_fk());
		cv.put(COL_MODIFIED, dateFormat.format(date));
		cv.put(COL_IS_CAT, mio.getIs_category());

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
		cv.put(COL_CAT, mio.getCategory_fk());
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
		Cursor c = db.query(TABLE_IMAGE, null,COL_CAT+ " = "+category_id, null,null, null,null);
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
	
	public List<ImageObject> getAllImages(){
		ImageObject mio;
		List<ImageObject> images = new LinkedList<ImageObject>();
		try{
		Cursor c = db.query(TABLE_IMAGE, null,null, null,null, null,null);
		c.moveToFirst();
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
	
	public List<CategoryObject> getAllCategories(){
		CategoryObject co;
		List<CategoryObject> categories = new LinkedList<CategoryObject>();
		String[] columns = new String[2];
		columns[0]=KEY_ID;
		columns[1]=COL_NAME;
		Cursor c = db.query(true, TABLE_CAT, columns,null, null,null, null,COL_NAME, null);
		c.moveToFirst();
		while(!c.isAfterLast()){
			co=cursorToCategory(c);
			categories.add(co);
			c.moveToNext();
		}
		c.close();
		return categories;
	}
	
	/* image - cursor */
	private ImageObject cursorToImage(Cursor cursor){
		ImageObject mio = new ImageObject();
		mio.setId(			cursor.getLong(		cursor.getColumnIndex(KEY_ID)));
		mio.setImageName(	cursor.getString(	cursor.getColumnIndex(COL_PATH)));
		mio.setAudioPath(	cursor.getString(	cursor.getColumnIndex(COL_AUDIO_PATH)));
		mio.setDescription(	cursor.getString(	cursor.getColumnIndex(COL_DESC)));
		mio.setModified(	cursor.getString(	cursor.getColumnIndex(COL_MODIFIED)));
		mio.setTimes_used(	cursor.getLong(		cursor.getColumnIndex(COL_TIME_USED)));
		mio.setLast_used(	cursor.getString(	cursor.getColumnIndex(COL_LAST_USED)));
		mio.setIs_category(	cursor.getLong(		cursor.getColumnIndex(COL_IS_CAT)));
		mio.setCategory_fk(	cursor.getLong(		cursor.getColumnIndex(COL_CAT)));
		mio.setParent_fk(	cursor.getLong(		cursor.getColumnIndex(COL_PARENT)));
		return mio;
	}
	
	/* C A T E G O R Y */
	/* category -insert */
	public CategoryObject insertCategory(CategoryObject co){
		ContentValues cv = new ContentValues();
		cv.put(COL_NAME, co.getCategoryName());
		if(db == null)
			open();
		long l = db.insert(TABLE_CAT, null, cv);
	    if(l==-1){
	    	return null;
	    }
		Cursor c = db.query(TABLE_CAT,  null, KEY_ID+" = "+l, null,null, null,null);
		c.moveToFirst();
		co = cursorToCategory(c);
		c.close();
		return co;
	}
		
	/* category - remove */
	public boolean deleteCategory(CategoryObject co){
		long row_id = co.getId();
		return db.delete(TABLE_CAT, KEY_ID+" = "+row_id, null)>0;
	}
	
	/* category - update */
	public int updateCategory(long _rowIndex, CategoryObject co){
		String where = KEY_ID + "=" + _rowIndex;
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID,co.getId());
		cv.put(COL_NAME, co.getCategoryName() );
		return db.update(TABLE_CAT, cv, where, null);
	}
	
	/* category - get */
	public ImageObject getCategory(long _rowIndex){
		Cursor c = db.query(TABLE_CAT,  null, KEY_ID+" = "+_rowIndex, null,null, null,null);
		c.moveToFirst();
		return cursorToImage(c);
	}

	/* category - cursor*/
	private CategoryObject cursorToCategory(Cursor c){
		CategoryObject co = new CategoryObject();
		co.setId(c.getLong(	c.getColumnIndex(KEY_ID)));
		co.setCategoryName(c.getColumnName(c.getColumnIndex(COL_NAME)));
		return co;		
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

			_db.execSQL(TABLE_CAT_CREATE);
			_db.execSQL(INSERT_MAIN_CATEGORY);
			
			_db.execSQL(TABLE_IMAGES_CREATE);
			//_db.execSQL(TABLE1_CREATE);
			//_db.execSQL(DICTIONARY_TABLE_CREATE);
			_db.execSQL(HTTP_IMG_TABLE_CREATE);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String drop_table = "DROP TABLE IF EXISTS ";
			// Log the version upgrade/
			Log.w("TaskDBAdapter", "Upgrading from version "+	oldVersion + " to "+ newVersion + ", witch will destroy all old data");
			db.execSQL(drop_table+TABLE_IMAGE);
			//db.execSQL(drop_table+TABLE_DESC);
			db.execSQL(drop_table+TABLE_CAT);
			db.execSQL(drop_table+TABLE_HTTP);
			onCreate(db);
			
		}
	}
}
