package com.example.app_2.contentprovider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class UserContract {

	public static final String AUTHORITY = "com.example.app_2.contentprovider.user";
	
	public static final String BASE_PATH = "user";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/user";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/user";
	public static final String TABLE_USER = "user"; 
		
	public final static class Columns implements BaseColumns{
		private Columns() {}
		public static final String USERNAME = "username";
		public static final String ISMALE = "ismale";
		public static final String ROOT_FK = "rootfk";
		public static final String IMG_FILENAME = "user_image";
		public static final String FONT_SIZE = "font_size";
		public static final String IMG_SIZE = "image_size";
		public static final String DEFAULT_SORT_ORDER = _ID + " ASC";

	}
}
