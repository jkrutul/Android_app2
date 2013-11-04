package com.example.app_2.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import com.example.app_2.storage.Database.myDbHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ParentContentProvider extends ContentProvider{
	
	private myDbHelper mOpenHelper;
	private static final String DBNAME = "myDatabase.db";
	
	  // Used for the UriMacher
	  private static final int PARENT = 30;
	  private static final int PARENT_ID = 40; 
	  
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		sURIMatcher.addURI(ParentContract.AUTHORITY, ParentContract.BASE_PATH, PARENT);
		sURIMatcher.addURI(ParentContract.AUTHORITY, ParentContract.BASE_PATH + "/#", PARENT_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int rowsDeleted = 0;
		String id = uri.getLastPathSegment();
		String whereClause;
		switch(sURIMatcher.match(uri)){
		case PARENT:
			rowsDeleted = db.delete(ParentContract.TABLE_PARENT, selection, selectionArgs);
			break;
		case PARENT_ID:
			whereClause = ParentContract.Columns._ID + "="+id+ (!TextUtils.isEmpty(selection) ? " AND ("+selection+')' : "");
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
		case PARENT:
			//rowID = db.replace(ParentContract.TABLE_PARENT, null, values);
			//if(rowID == -1)
				rowID = db.insertOrThrow(ParentContract.TABLE_PARENT, null, values);
			break;
		default:
			throw new IllegalArgumentException("Uknown URI:" + uri);
		}
		if(rowID > 0){
			result = ContentUris.withAppendedId(ParentContract.CONTENT_URI, rowID);
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
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		//checkColumns(projection);
		queryBuilder.setTables(ParentContract.TABLE_PARENT);
		
		switch(sURIMatcher.match(uri)){
		case PARENT:
			break;
		case PARENT_ID:
			queryBuilder.appendWhere(ParentContract.Columns._ID + "=" + uri.getLastPathSegment());
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
		case PARENT:
			rowsUpdated = db.update(ParentContract.TABLE_PARENT, values,selection, selectionArgs);
			break;
		case PARENT_ID:
			whereClause = ImageContract.Columns._ID + "="+ id +
					(!TextUtils.isEmpty(selection) ? "AND ("+ selection +')':"");
			
			rowsUpdated = db.update(ParentContract.TABLE_PARENT, values, whereClause, selectionArgs);
			break;
		default:
		    throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		if(rowsUpdated > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
	
	private void checkColumns(String[] projection){
		if(projection !=null){
			String[] available = {ParentContract.Columns._ID, ParentContract.Columns.IMAGE_FK, ParentContract.Columns.PARENT_FK};
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> avaliableColumns = new HashSet<String>(Arrays.asList(available));
			if((avaliableColumns.contains(requestedColumns))==false){
				throw new IllegalArgumentException("Uknown columns in projection");
			}
		}
	}

}
