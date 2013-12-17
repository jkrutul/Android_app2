package com.example.app_2.activities;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.fragments.ImagesMultiselectFragment;
import com.example.app_2.models.EdgeModel;
import com.example.app_2.storage.Database;
import com.example.app_2.utils.DFS;

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
        Bundle args = new Bundle();
        args.putLong("category_id", executing_category_id);
        imf.setArguments(args);
        
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
			Cursor c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null,null, null);
			c.moveToFirst(); // TODO dodaæ else
			if(!c.isAfterLast())
				category_author = c.getLong(0);
			c.close();
			
			uri = Uri.parse(ImageContract.CONTENT_URI + "/"+checked_list.get(0));									// znaleŸenie autora dodawanych obrazków
			c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast())
				selected_items_author = c.getLong(0);
			c.close();
			
			copy_images = (category_author == selected_items_author) ? false : true;
			ContentValues[] cvArray  = new ContentValues[checked_list.size()];
			if(copy_images){			// obrazki nie nale¿¹ do tego samego u¿ytkownika - kopiujê obrazki
				String[] projection = {
							"i."+ImageContract.Columns._ID,
							"i."+ImageContract.Columns.FILENAME,
							"i."+ImageContract.Columns.DESC,
							"i."+ImageContract.Columns.CATEGORY};
				
				
				ArrayList<Long>checked_category_list = new ArrayList<Long>();			// wydzielam kategorie do listy
				for(Long checked_list_item : checked_list){
					uri = Uri.parse(ImageContract.CONTENT_URI+"/"+checked_list_item);
					c= getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns._ID, "i."+ImageContract.Columns.CATEGORY}, null, null, null);
					c.moveToFirst();
					if(!c.isAfterLast()){
						String category = c.getString(1);
						if(category != null && !category.isEmpty())
							checked_category_list.add(c.getLong(0));
					}
				}
					
				checked_list.removeAll(checked_category_list);
				
				HashMap<Long,Long> copied = new HashMap<Long,Long>(); // key - id elemenutu kopiowanego, value - id nowego elementu
				
				// najpierw kopiuje kategorie
				for(Long checked_category_id : checked_category_list){
					DFS.getElements(checked_category_id, this);      		// ustawia liste DFS.edges
					for(EdgeModel edge : DFS.edges){
						Long parent = edge.getParent();
						Long child = edge.getChild();
						
						if(!copied.keySet().contains(parent)){					// parent
							uri = Uri.parse(ImageContract.CONTENT_URI+"/"+parent);
							c = getContentResolver().query(uri, projection, null, null, null);
							c.moveToFirst();
							if(!c.isAfterLast()){
								ContentValues img_cv = createImgContentValue( c.getString(1), c.getString(2), c.getString(3), category_author);
								Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
								copied.put(parent, Long.parseLong(inserted_image_uri.getLastPathSegment()));
							}
							c.close();

						}
						
						if(!copied.keySet().contains(child)){						// child
							uri = Uri.parse(ImageContract.CONTENT_URI+"/"+child);
							c = getContentResolver().query(uri, projection, null, null, null);
							c.moveToFirst();
							if(!c.isAfterLast()){
								ContentValues img_cv = createImgContentValue( c.getString(1), c.getString(2), c.getString(3), category_author );
								Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
								copied.put(child, Long.parseLong(inserted_image_uri.getLastPathSegment()));
							}
							c.close();

						}
						getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(copied.get(child), copied.get(parent)));
					}
					
					// dodajê wi¹zanie nowo powsta³ego podgrafu do kategorii
					getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(copied.get(checked_category_id), executing_category_id));
					
				}
	
				// teraz kopiuje pozosta³e liœcie	
				for(Long checked_list_item : checked_list){
					if(!copied.keySet().contains(checked_list_item)){
						uri = Uri.parse(ImageContract.CONTENT_URI+"/"+checked_list_item);
						c= getContentResolver().query(uri, projection, null, null, null);
						c.moveToFirst();
						if(!c.isAfterLast()){
							ContentValues img_cv = createImgContentValue( c.getString(1), c.getString(2), c.getString(3), category_author );
							if(c.getString(3) != null && !c.getString(3).isEmpty()){
								Log.w(LOG_TAG, "obrazek jest kategori¹, a dodawany jako liœæ");							
							}
							c.close();
								
							Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
							copied.put(checked_list_item, Long.parseLong(inserted_image_uri.getLastPathSegment()));
							
							// dodanie wi¹zania na kategoriê do której kopiujê element
							ContentValues parent_cv = new ContentValues();
							parent_cv.put(ParentContract.Columns.IMAGE_FK, inserted_image_uri.getLastPathSegment());
							parent_cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
							getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(Long.parseLong(inserted_image_uri.getLastPathSegment()), executing_category_id));
						}	
					}

				}
			
				for(Long newCopiedId : copied.values())
					getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(newCopiedId, Database.getMainDictFk()));
				

			
			}else{ // obrazki nale¿¹ do tego samego u¿ytkownika co kategoria - dodajê tylko wiazania
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
	
	private ContentValues createParentContentValue(Long image, Long parent){
		ContentValues bind_cv = new ContentValues();
		bind_cv.put(ParentContract.Columns.IMAGE_FK, image);
		bind_cv.put(ParentContract.Columns.PARENT_FK, parent);
		return bind_cv;
	}
	
	private ContentValues createImgContentValue(String filename, String description, String category, Long author_fk){
		ContentValues img_cv = new ContentValues();
		img_cv.put(ImageContract.Columns.FILENAME, filename);
		img_cv.put(ImageContract.Columns.DESC, description);
		img_cv.put(ImageContract.Columns.CATEGORY,	category);
		img_cv.put(ImageContract.Columns.AUTHOR_FK, author_fk);
		img_cv.put(ImageContract.Columns.MODIFIED, dateFormat.format(date));
		return img_cv;
	}

}
