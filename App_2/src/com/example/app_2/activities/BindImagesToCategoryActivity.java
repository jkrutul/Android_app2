package com.example.app_2.activities;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.fragments.ImagesMultiselectFragment;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.utils.DFS;
import com.example.app_2.utils.Utils;

public class BindImagesToCategoryActivity extends FragmentActivity{
	static final String LOG_TAG = "BindImagesToCategory";
	ImagesMultiselectFragment imf;
	Long executing_category_id;
	Long logged_user_id;
	private static SimpleDateFormat dateFormat;
	private static Date date;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		date = new Date();
		
		
		SharedPreferences sharedPref = getSharedPreferences("USER",Context.MODE_PRIVATE);
		logged_user_id = sharedPref.getLong("logged_user_id", 0);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		executing_category_id = ImageGridActivity.actual_category_fk;	
		setContentView(R.layout.activity_bind_images_to_category);
		
        imf = new ImagesMultiselectFragment();
        
    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.replace(R.id.list_fragment_container, imf);
        ft.commit();
	}
	
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.bi_confirm_button:
			ArrayList<Long> checked_list = imf.getCheckedItemIds();
			if(checked_list.size()<=0){
				finish();
				break;
			}

		    boolean copy_images = true;
			Long category_author = null, selected_items_author = null;
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"+ImageGridActivity.actual_category_fk); 				// znaleŸenie autora kategorii do której dodawane bêd¹ obrazki
			Cursor c = getContentResolver().query(uri, new String[]{ImageContract.Columns.AUTHOR_FK}, null,null, null);
			c.moveToFirst(); // TODO dodaæ else
			if(!c.isAfterLast())
				category_author = c.getLong(0);
			c.close();
			
			uri = Uri.parse(ImageContract.CONTENT_URI + "/"+checked_list.get(0));
			c = getContentResolver().query(uri, new String[]{ImageContract.Columns.AUTHOR_FK}, null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast())
				selected_items_author = c.getLong(0);
			c.close();
			
			copy_images = (category_author == selected_items_author) ? false : true;
			ContentValues[] cvArray  = new ContentValues[checked_list.size()];
			if(copy_images){
				String[] projection = {
							ImageContract.Columns._ID,
							ImageContract.Columns.FILENAME,
							ImageContract.Columns.DESC,
							ImageContract.Columns.CATEGORY};
				
				for(Long checked_list_item : checked_list){

					uri = Uri.parse(ImageContract.CONTENT_URI+"/"+checked_list_item);
					c= getContentResolver().query(uri, projection, null, null, null);
					c.moveToFirst();
					if(!c.isAfterLast()){
						ContentValues img_cv = new ContentValues();
						img_cv.put(ImageContract.Columns.FILENAME, c.getString(1));
						img_cv.put(ImageContract.Columns.DESC, c.getString(2));
						img_cv.put(ImageContract.Columns.CATEGORY, c.getString(3));
						img_cv.put(ImageContract.Columns.AUTHOR_FK, category_author );
						img_cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
						if(c.getString(3) != null && !c.getString(3).isEmpty()){
							DFS.getElements(checked_list_item, this); // TODO dfs je¿eli kopiujemy ca³e drzewo
						}
						
						c.close();
						
						Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
						// dodanie wi¹zania na kategoriê
						ContentValues parent_cv = new ContentValues();
						parent_cv.put(ParentContract.Columns.IMAGE_FK, inserted_image_uri.getLastPathSegment());
						parent_cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
						getContentResolver().insert(ParentContract.CONTENT_URI, parent_cv);
						
						//dodanie wi¹zania na s³ownik
						parent_cv = new ContentValues();
						parent_cv.put(ParentContract.Columns.IMAGE_FK, inserted_image_uri.getLastPathSegment());
						parent_cv.put(ParentContract.Columns.PARENT_FK, Database.getMainDictFk());
						getContentResolver().insert(ParentContract.CONTENT_URI, parent_cv);
						
					}	
				}
				
			}else{	
				int i =0;
				for(Long image_fk :checked_list){
					ContentValues cv = new ContentValues();
					cv.put(ParentContract.Columns.IMAGE_FK, image_fk);
					cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
					cvArray[i++] = cv;
				}
				getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
			}
			
			Bundle args = new Bundle();		
			args.putLong("CATEGORY_ID", ImageGridActivity.actual_category_fk);
			ImageGridActivity.igf.getLoaderManager().restartLoader(1, args, ImageGridActivity.igf);	
			finish();
			break;
			
		case R.id.bi_cancel_button:
			finish();
		default:
			break;
		}
	}

}
