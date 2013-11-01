package com.example.app_2.contentprovider;

import android.content.ContentResolver;
import android.net.Uri;

public class ParentsOfImageContract {
	
	private ParentsOfImageContract(){}
	public static final String AUTHORITY = "com.example.app_2.contentprovider.parentsofimage";
	
	public static final String BASE_PATH = "parents_of_image";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/parents_of_image";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+"/parents_of_image";

}
