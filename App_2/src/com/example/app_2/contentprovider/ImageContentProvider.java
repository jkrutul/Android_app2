package com.example.app_2.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;

import com.example.app_2.storage.Database.myDbHelper;

public class ImageContentProvider extends ContentProvider{
	
	//private Database dbAdapter;
	
	private myDbHelper mOpenHelper;
	private static final String DBNAME = "myDatabase.db";
	private SQLiteDatabase db;
	
	  // Used for the UriMacher
	  private static final int IMAGE = 10;
	  private static final int IMAGE_ID = 20;
	  
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sURIMatcher.addURI(ImageContract.AUTHORITY, ImageContract.BASE_PATH, IMAGE);
		sURIMatcher.addURI(ImageContract.AUTHORITY, ImageContract.BASE_PATH + "/#", IMAGE_ID);
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsDeleted = 0;
		String id = uri.getLastPathSegment();
		String whereClause;
		switch(sURIMatcher.match(uri)){
		case IMAGE:
			rowsDeleted = db.delete(ImageContract.TABLE_IMAGE, selection, selectionArgs);
			break;
		case IMAGE_ID:
			whereClause = ImageContract.Columns._ID + "="+id+(!TextUtils.isEmpty(selection) ? " AND ("+selection+')' : "");
			rowsDeleted = db.delete(ImageContract.TABLE_IMAGE, whereClause, selectionArgs);
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		if(rowsDeleted>0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		//String ret =getContext().getContentResolver().getType(System.CONTENT_URI);
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri result = null;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowID = 0;
		switch(sURIMatcher.match(uri)){
		case IMAGE:
			rowID = db.insert(ImageContract.TABLE_IMAGE, null, values);
			//db.insert(ParentContract.TABLE_PARENT, null, values);
			break;
		default:
			throw new IllegalArgumentException("Uknown URI:" + uri);
		}
		
		if(rowID > 0){
			result = ContentUris.withAppendedId(ImageContract.CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(result, null);
		}
		return result;
	}

	@Override
	public boolean onCreate() {
		//dbAdapter = Database.getInstance(getContext());
		//Database.open();
		mOpenHelper = new myDbHelper(getContext(), DBNAME, null, 1);
		return (mOpenHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		//checkColumns(projection);
		queryBuilder.setTables(
				"("+ImageContract.TABLE_IMAGE+" i " +" INNER JOIN "+UserContract.TABLE_USER+" u "+
						"ON  i."+ImageContract.Columns.AUTHOR_FK+" = u."+UserContract.Columns._ID+")"
				);
		
		switch(sURIMatcher.match(uri)){
		case IMAGE:
			break;
		case IMAGE_ID:
			queryBuilder.appendWhere("i."+ImageContract.Columns._ID + "=" + uri.getLastPathSegment());
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		String orderBy = TextUtils.isEmpty(sortOrder) ? ImageContract.Columns.DEFAULT_SORT_ORDER : sortOrder;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, "i."+orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsUpdated = 0;
		String id = uri.getLastPathSegment();
		String whereClause;
		switch(sURIMatcher.match(uri)){
		case IMAGE:
			rowsUpdated = db.update(ImageContract.TABLE_IMAGE, values,selection, selectionArgs);
			break;
		case IMAGE_ID:
			whereClause = ImageContract.Columns._ID + "="+ id +	(!TextUtils.isEmpty(selection) ? "AND ("+ selection +')':"");
			
			rowsUpdated = db.update(ImageContract.TABLE_IMAGE, values, whereClause, selectionArgs);
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		if(rowsUpdated > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		switch(match){
		case IMAGE:
			int numInserted = 0;
			db.beginTransaction();
			try{
				SQLiteStatement insert = db.compileStatement("insert into "+ImageContract.TABLE_IMAGE
						+ " ("+ImageContract.Columns.FILENAME+","+ImageContract.Columns.DESC+")"
						+ " values "+"(?,?)");
				for(ContentValues value : values){
					insert.bindString(1, value.getAsString(ImageContract.Columns.FILENAME));
					insert.bindString(2, value.getAsString(ImageContract.Columns.DESC));
					insert.execute();
				}
				db.setTransactionSuccessful();
				numInserted= values.length;
			}finally{
				db.endTransaction();
			}
			return numInserted;
			default:
				throw new UnsupportedOperationException("unsupported uri:" +uri);
		}
	}
	
	private void checkColumns(String[] projection){
		if(projection !=null){
			String[] available = {ImageContract.Columns._ID, ImageContract.Columns.FILENAME, ImageContract.Columns.AUDIO_PATH, ImageContract.Columns.DESC, ImageContract.Columns.CATEGORY};
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> avaliableColumns = new HashSet<String>(Arrays.asList(available));
			if((avaliableColumns.contains(requestedColumns))==false){
				throw new IllegalArgumentException("Uknown columns in projection");
			}
		}
	}

}
