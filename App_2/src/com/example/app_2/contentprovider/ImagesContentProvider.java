package com.example.app_2.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.app_2.App_2;
import com.example.app_2.storage.Database;

public class ImagesContentProvider extends ContentProvider{
	
	private Database dbAdapter;
	  // Used for the UriMacher
	  private static final int IMAGES = 10;
	  private static final int IMAGES_ID = 20;
	  
	private static final String AUTHORITY = "com.example.app_2.contentprovider";
	
	public static final String BASE_PATH = "images";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/images";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/images";
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, IMAGES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", IMAGES_ID);
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase s_db = dbAdapter.dbHelper.getWritableDatabase();
		int rowsDeleted = 0;
		switch(uriType){
		case IMAGES:
			rowsDeleted = s_db.delete(dbAdapter.TABLE_IMAGE, selection, selectionArgs);
			break;
		case IMAGES_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)){
				rowsDeleted = s_db.delete(dbAdapter.TABLE_IMAGE, dbAdapter.KEY_ID + "=" + id, null);
			}
			else{
				rowsDeleted = s_db.delete(dbAdapter.TABLE_IMAGE, dbAdapter.KEY_ID + "=" + id + " and " + selection, selectionArgs);
				
			}
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase s_db = dbAdapter.dbHelper.getWritableDatabase();
		long id = 0;
		switch(uriType){
		case IMAGES:
			id = s_db.insert(dbAdapter.TABLE_IMAGE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Uknown URI:" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH+ "/" + id);
	}

	@Override
	public boolean onCreate() {
		dbAdapter = Database.getInstance(getContext());
		//Database.open();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		checkColumns(projection);
		queryBuilder.setTables(dbAdapter.TABLE_IMAGE);
		
		int uriType = sURIMatcher.match(uri);
		switch(uriType){
		case IMAGES:
			break;
		case IMAGES_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(dbAdapter.KEY_ID + "=" + uri.getLastPathSegment());
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase s_db = dbAdapter.dbHelper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(s_db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase s_db = dbAdapter.dbHelper.getWritableDatabase();
		int rowsUpdated = 0;
		switch(uriType){
		case IMAGES:
			rowsUpdated = s_db.update(dbAdapter.TABLE_IMAGE, values,selection, selectionArgs);
			break;
		case IMAGES_ID:
			String id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection)){
				rowsUpdated = s_db.update(dbAdapter.TABLE_IMAGE, values, dbAdapter.KEY_ID + "=" + id, null);
			}
			else{
				rowsUpdated = s_db.update(dbAdapter.TABLE_IMAGE, values, dbAdapter.KEY_ID + "=" + id + " and " + selection, selectionArgs);
			}
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}
	
	private void checkColumns(String[] projection){
		String[] available = {dbAdapter.KEY_ID, dbAdapter.TABLE_IMAGE, dbAdapter.COL_PATH, dbAdapter.COL_AUDIO_PATH, dbAdapter.COL_DESC, dbAdapter.COL_CAT};
		if(projection !=null){
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> avaliableColumns = new HashSet<String>(Arrays.asList(available));
			if(!avaliableColumns.contains(requestedColumns)){
				//throw new IllegalArgumentException("Uknown columns in projection");
			}
		}
	}

}
