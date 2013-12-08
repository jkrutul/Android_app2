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
import android.net.Uri;
import android.text.TextUtils;

import com.example.app_2.storage.Database.myDbHelper;

public class UserContentProvider extends ContentProvider{
	
	private myDbHelper mOpenHelper;
	private static final String DBNAME = "myDatabase.db";
	
	  // Used for the UriMacher
	  private static final int USER = 38;
	  private static final int USER_ID = 41; 
	  
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_PATH, USER);
		sURIMatcher.addURI(UserContract.AUTHORITY, UserContract.BASE_PATH + "/#", USER_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsDeleted = 0;
		String id = uri.getLastPathSegment();
		String whereClause;
		switch(sURIMatcher.match(uri)){
		case USER:
			rowsDeleted = db.delete(UserContract.TABLE_USER, selection, selectionArgs);
			break;
		case USER_ID:
			whereClause = UserContract.Columns._ID + "="+id+ (!TextUtils.isEmpty(selection) ? " AND ("+selection+')' : "");
			rowsDeleted = db.delete(UserContract.TABLE_USER, whereClause, selectionArgs);
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
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri result = null;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowID = 0;
		switch(sURIMatcher.match(uri)){
		case USER:
			rowID = db.insertOrThrow(UserContract.TABLE_USER, null, values);
			break;
		default:
			throw new IllegalArgumentException("Uknown URI:" + uri);
		}
		if(rowID > 0){
			result = ContentUris.withAppendedId(UserContract.CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(result, null);
		}
		return result;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new myDbHelper(getContext(), DBNAME, null, 1);
		return (mOpenHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		//checkColumns(projection);
		queryBuilder.setTables(UserContract.TABLE_USER);
		switch(sURIMatcher.match(uri)){
		case USER:
			break;
		case USER_ID:
			queryBuilder.appendWhere(UserContract.Columns._ID + "=" + uri.getLastPathSegment());
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		String orderBy = TextUtils.isEmpty(sortOrder) ? ImageContract.Columns.DEFAULT_SORT_ORDER : sortOrder;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);
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
		case USER:
			rowsUpdated = db.update(UserContract.TABLE_USER, values,selection, selectionArgs);
			break;
		case USER_ID:
			whereClause = ImageContract.Columns._ID + "="+ id +
					(!TextUtils.isEmpty(selection) ? "AND ("+ selection +')':"");
			
			rowsUpdated = db.update(UserContract.TABLE_USER, values, whereClause, selectionArgs);
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		if(rowsUpdated > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	


}
