package com.example.app_2.contentprovider;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ParentContract {
	
	private ParentContract(){}
	public static final String AUTHORITY = "com.example.app_2.contentprovider.parent";
	
	public static final String BASE_PATH = "parent";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/parent";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/parent";

	public static final String TABLE_PARENT = "parent"; 
		
	public final static class Columns implements BaseColumns{
		private Columns() {}
		public static final String IMAGE_FK = "image_fk";
		public static final String PARENT_FK = "parent_fk";
		public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
	}
}
