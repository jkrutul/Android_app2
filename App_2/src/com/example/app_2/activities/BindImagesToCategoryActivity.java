package com.example.app_2.activities;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.actionbar.adapter.TitleNavigationAdapter;
import com.example.app_2.actionbar.model.SpinnerNavItem;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.fragments.ImagesMultiselectFragment;
import com.example.app_2.models.EdgeModel;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.DFS;
import com.example.app_2.utils.ImageLoader;

public class BindImagesToCategoryActivity extends FragmentActivity implements OnNavigationListener{
	static final String LOG_TAG = "BindImagesToCategory";
	ImagesMultiselectFragment imf;
	Long executing_category_id;
	//Long logged_user_id;
	private static SimpleDateFormat dateFormat;
	private static Date date;
	
    private ArrayList<SpinnerNavItem> navSpinner;
    private TitleNavigationAdapter title_nav_adapter;
    
    private ImageView parent_category_imageView;
    private TextView category_name_textView;

    
    
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		date = new Date();
		
		//SharedPreferences sharedPref = getSharedPreferences("USER",Context.MODE_PRIVATE);
		//logged_user_id = sharedPref.getLong("logged_user_id", 0);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		executing_category_id = ImageGridActivity.actual_category_fk;	
		setContentView(R.layout.activity_bind_images_to_category);
		parent_category_imageView = (ImageView) findViewById(R.id.parent_category_image);
		category_name_textView = (TextView) findViewById(R.id.txt_cat_info);

		
		Uri uri = Uri.parse(ImageContract.CONTENT_URI+"/"+executing_category_id);
		
		Cursor c = getContentResolver().query(uri, new String[]{ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY}, null, null, null);
		c.moveToFirst();
		if(!c.isAfterLast()){
			String path = Storage.getPathToScaledBitmap(c.getString(0), 150);
			ImageLoader.loadBitmap(path, parent_category_imageView);
			category_name_textView.append("\""+c.getString(1)+"\"");
		}
		c.close();
		
		
        imf = new ImagesMultiselectFragment();
        Bundle args = new Bundle();
        args.putLong("category_id", executing_category_id);
        imf.setArguments(args);
        
    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.replace(R.id.list_fragment_container, imf);
        ft.commit();
        
        
        
    	navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Alfabetycznie", R.drawable.sort_ascend));
		navSpinner.add(new SpinnerNavItem("Ostatnio zmodyfikowane", R.drawable.clock));
		navSpinner.add(new SpinnerNavItem("Najcz�ciej u�ywane", R.drawable.favourites));
		
		title_nav_adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
		getActionBar().setListNavigationCallbacks(title_nav_adapter, this);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
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
			
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"+ImageGridActivity.actual_category_fk); 				// znale�enie autora kategorii do kt�rej dodawane b�d� obrazki
			Cursor c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null,null, null);
			c.moveToFirst(); // TODO doda� else
			if(!c.isAfterLast())
				category_author = c.getLong(0);
			c.close();
			
			uri = Uri.parse(ImageContract.CONTENT_URI + "/"+checked_list.get(0));									// znale�enie autora dodawanych obrazk�w
			c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast())
				selected_items_author = c.getLong(0);
			c.close();
			
			copy_images = (category_author == selected_items_author) ? false : true;
			ContentValues[] cvArray  = new ContentValues[checked_list.size()];
			if(copy_images){			// obrazki nie nale�� do tego samego u�ytkownika - kopiuj� obrazki
				String[] projection = {
							"i."+ImageContract.Columns._ID,
							"i."+ImageContract.Columns.FILENAME,
							"i."+ImageContract.Columns.DESC,
							"i."+ImageContract.Columns.CATEGORY};
				
				
				ArrayList<Long>checked_category_list = new ArrayList<Long>();			// wydzielam kategorie do osobnej listy
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
					DFS.getElements(checked_category_id);      		// ustawia liste DFS.edges
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
					
					// dodaj� wi�zanie nowo powsta�ego podgrafu do kategorii
					getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(copied.get(checked_category_id), executing_category_id));
					
				}
	
				// teraz kopiuje pozosta�e li�cie	
				for(Long checked_list_item : checked_list){
					if(!copied.keySet().contains(checked_list_item)){
						uri = Uri.parse(ImageContract.CONTENT_URI+"/"+checked_list_item);
						c= getContentResolver().query(uri, projection, null, null, null);
						c.moveToFirst();
						if(!c.isAfterLast()){
							ContentValues img_cv = createImgContentValue( c.getString(1), c.getString(2), c.getString(3), category_author );
							if(c.getString(3) != null && !c.getString(3).isEmpty()){
								Log.w(LOG_TAG, "obrazek jest kategori�, a dodawany jako li��");							
							}
							c.close();
								
							Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
							copied.put(checked_list_item, Long.parseLong(inserted_image_uri.getLastPathSegment()));
							
							// dodanie wi�zania na kategori� do kt�rej kopiuj� element
							ContentValues parent_cv = new ContentValues();
							parent_cv.put(ParentContract.Columns.IMAGE_FK, inserted_image_uri.getLastPathSegment());
							parent_cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
							getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(Long.parseLong(inserted_image_uri.getLastPathSegment()), executing_category_id));
						}	
					}

				}
			
				for(Long newCopiedId : copied.values())
					getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(newCopiedId, Database.getMainDictFk()));
				

			
			}else{ // obrazki nale�� do tego samego u�ytkownika co kategoria - dodaj� jedynie wiazania
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


	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Bundle args = new Bundle();		

			switch (itemPosition) {
	
			case 0: // alfabetycznie 
				imf.sortOrder = "i."+ImageContract.Columns.DESC + " COLLATE LOCALIZED ASC";			
				imf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) imf);
				break;
			case 1: // ostatnio zmodyfikowane
				imf.sortOrder = "i."+ImageContract.Columns.MODIFIED + " DESC";
				imf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) imf);
				break;
			case 2: // najcz�ciej u�ywane
				imf.sortOrder = "i."+ImageContract.Columns.TIME_USED + " DESC";
				imf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) imf);
				break;
	
			default:
				break;
			}
		
		return false;
	}

}
