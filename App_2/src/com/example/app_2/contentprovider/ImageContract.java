package com.example.app_2.contentprovider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ImageContract {
	private ImageContract(){}
	
	public static final String AUTHORITY = "com.example.app_2.contentprovider";
	
	public static final String BASE_PATH = "image";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/image";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/image";
	public static final String TABLE_IMAGE = "image"; 
		
	public final static class Columns implements BaseColumns{
		private Columns() {}
		public static final String FILENAME = "filename";
		public static final String DESC = "description";
		public static final String TTS_M = "tts_m";
		public static final String TTS_F = "tts_f";
		public static final String TIME_USED = "used";
		public static final String MODIFIED  = "last_modified";
		public static final String LAST_USED = "last_used";
		public static final String IS_CATEGORY = "is_category";
		public static final String IS_ADD_TO_EXPR = "is_add_to_expr";
		public static final String IS_ADD_TO_CAT_LIST = "is_add_to_cat_list";
		public static final String AUTHOR_FK = "author_fk";
		public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
		
	}
}
