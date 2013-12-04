package com.example.app_2.contentprovider;

import java.util.ArrayList;

import com.example.app_2.storage.Database.myDbHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ImagesOfParentContentProvider extends ContentProvider{
	
	private myDbHelper mOpenHelper;
	private static final String DBNAME = "myDatabase.db";
	
	  // Used for the UriMacher
	  private static final int PARENT = 33;
	  private static final int PARENT_ID = 44; 
	  
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		//sURIMatcher.addURI(ImagesOfParentContract.AUTHORITY, ImagesOfParentContract.BASE_PATH, PARENT);
		sURIMatcher.addURI(ImagesOfParentContract.AUTHORITY, ImagesOfParentContract.BASE_PATH + "/#", PARENT_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new myDbHelper(getContext(), DBNAME, null, 1);
		return (mOpenHelper == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		//checkColumns(projection);
		queryBuilder.setTables(
				"("+ImageContract.TABLE_IMAGE+" i " +" INNER JOIN "+UserContract.TABLE_USER+" u "+
				"ON  i."+ImageContract.Columns.AUTHOR_FK+" = u."+UserContract.Columns._ID+
				
				")" +
				" INNER JOIN "+
				ParentContract.TABLE_PARENT+" p" +
				" ON ( i."+ImageContract.Columns._ID+" = p."+ParentContract.Columns.IMAGE_FK+" )"
				);
		
		switch(sURIMatcher.match(uri)){
		case PARENT:
			break;
		case PARENT_ID:
			queryBuilder.appendWhere("p."+ParentContract.Columns.PARENT_FK + "=" + uri.getLastPathSegment());
			break;
		default:
			queryBuilder.appendWhere("p."+ParentContract.Columns.PARENT_FK + "=" + uri.getLastPathSegment());
		  //throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		String orderBy = TextUtils.isEmpty(sortOrder) ? "i."+ImageContract.Columns.DEFAULT_SORT_ORDER : sortOrder;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
	    throw new IllegalArgumentException("Unknown URI: " + uri);
	}
	

}
