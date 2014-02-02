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
		navSpinner.add(new SpinnerNavItem("Najczêœciej u¿ywane", R.drawable.favourites));
		
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

            bindImagesToCategory(checked_list, true);  
            finish();
            break;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
		case R.id.bi_cancel_button:
			finish();
		default:
			break;
		}
	}
	
	
    private void bindImagesToCategory(ArrayList<Long> checked_list, boolean preventDuplicates){
        boolean copy_images = true;
        Long category_author = null, selected_items_author = null;
   
        Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"+ImageGridActivity.actual_category_fk);                 // znaleŸenie autora kategorii do której dodawane bêd¹ obrazki
        Cursor c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null,null, null);
        c.moveToFirst(); // TODO dodaæ else
        if(!c.isAfterLast())
            category_author = c.getLong(0);
        c.close();
        
        uri = Uri.parse(ImageContract.CONTENT_URI + "/"+checked_list.get(0));                                    // znaleŸenie autora dodawanych obrazków
        c = getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null, null, null);
        c.moveToFirst();
        if(!c.isAfterLast())
            selected_items_author = c.getLong(0);
        c.close();
        
        copy_images = (category_author == selected_items_author) ? false : true;
        ContentValues[] cvArray  = new ContentValues[checked_list.size()];
        
        if(copy_images){            // obrazki nie nale¿¹ do tego samego u¿ytkownika - kopiujê obrazki
            String[] projection = {
                        "i."+ImageContract.Columns._ID,
                        "i."+ImageContract.Columns.FILENAME,
                        "i."+ImageContract.Columns.DESC,
                        "i."+ImageContract.Columns.CATEGORY};
            
            
            ArrayList<Long>checked_category_list = new ArrayList<Long>();            // wydzielam kategorie do osobnej listy
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
            
            HashMap<Long,Long> copied = new HashMap<Long,Long>(); // key - id elementu kopiowanego, value - id nowego elementu
            
            // kopiuje kategorie
            for(Long checked_category_id : checked_category_list){            	
                DFS.getElements(checked_category_id);              // ustawia liste DFS.edges
                
                if(DFS.edges.size()== 0){						   // dodawana kategoria jest pusta
                    if(!copied.keySet().contains(checked_category_id)){      
                        Long parentInUser = checkIfImageAlreadyExistInUserSet(checked_category_id,category_author);// sprawdzam czy zaznaczone podobne obrazki nie s¹ ju¿ zdefiniowane w zbiorze u¿ytkownika, porównujê nazwê pliku i jego opis
                        if(checked_category_id != parentInUser)
                            copied.put(checked_category_id, parentInUser);
                        else{
	                        uri = Uri.parse(ImageContract.CONTENT_URI+"/"+checked_category_id);
	                        c = getContentResolver().query(uri, projection, null, null, null);
	                        c.moveToFirst();
	                        if(!c.isAfterLast()){
	                            ContentValues img_cv = createImgContentValue( c.getString(1), c.getString(2), c.getString(3), category_author);
	                            Uri inserted_image_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_cv);
	                            copied.put(checked_category_id, Long.parseLong(inserted_image_uri.getLastPathSegment()));
	                        }
	                        c.close();
                        }
                    }
                }else{                	// dodawana kategoria jest grafem
					for(EdgeModel edge : DFS.edges){
						Long parent = edge.getParent();
		                Long child = edge.getChild();

	                    if(!copied.keySet().contains(parent)){                    // parent
	                    	Long parentInUser = checkIfImageAlreadyExistInUserSet(parent,category_author);
	                    	if(parent != parentInUser)
	                    		copied.put(parent, parentInUser);
	                    	else{
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
	                    }
	                    
	                    if(!copied.keySet().contains(child)){                        // child
	                        Long childInUser =  checkIfImageAlreadyExistInUserSet(child,category_author);
	                        if(child != childInUser)
	                            copied.put(child, childInUser);
	                        else{
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
	                    }
	                    getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(copied.get(child), copied.get(parent)));
	                }
				}
                // dodajê wi¹zanie nowo powsta³ego podgrafu do kategorii
                getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(copied.get(checked_category_id), executing_category_id));
                
            }

            // kopiuje pozosta³e liœcie    
            for(Long checked_list_item : checked_list){
                if(!copied.keySet().contains(checked_list_item)){
                	Long childInUser =  checkIfImageAlreadyExistInUserSet(checked_list_item,category_author);
                    if(checked_list_item != childInUser)
                    	copied.put(checked_list_item, childInUser);
                    else{
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
            }
        
            for(Long newCopiedId : copied.values())			// dodajê wi¹zanie na s³ownik
                getContentResolver().insert(ParentContract.CONTENT_URI, createParentContentValue(newCopiedId, Database.getMainDictFk()));
            
        }else{ // obrazki nale¿¹ do tego samego u¿ytkownika co kategoria - dodajê jedynie wiazania
            int i =0;
            for(Long image_fk :checked_list){
                ContentValues cv = new ContentValues();
                cv.put(ParentContract.Columns.IMAGE_FK, image_fk);
                cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
                cvArray[i++] = cv;
            }
            getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
        }
    }
   
    /**
     * Funkcja sprawdza czy u¿ytkownik posiada obrazek o parametrach co obrazek o podanym id img_id (opis, nazwa pliku)
     *
     * @param img_id id obrazka z którego pobieram informacjê do porównania
     * @param user_id id u¿ytkownika którego w którego zbiorze bêdê poszukiwa³ obrazka
     * @return Long je¿eli znaleŸiono obrazek w zbiorze u¿ytkownika to zwracam jego id, jeœli nie zwracam id podane jako parametr
     */
    private Long checkIfImageAlreadyExistInUserSet(Long img_id, Long user_id){
        Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + img_id);
        String[] projection = {"i."+ImageContract.Columns.FILENAME, "i."+ImageContract.Columns.DESC};
        String description = null, filename = null;
        Cursor c = getContentResolver().query(uri, projection , null, null, null);        // pobieram wartoœci kopiowanego obrazka
        c.moveToFirst();
        if(!c.isAfterLast()){
            description = c.getString(1);
            filename = c.getString(0);
        }
        c.close();
        
        if(filename != null && description != null){
	        String selection = "i."+ImageContract.Columns.FILENAME+" = ? AND " + "i."+ImageContract.Columns.DESC + " = ? AND " +"i."+ImageContract.Columns.AUTHOR_FK+" = ? ";    // sprawdzam czy u¿ytkownik posiada w swoim zbiorze obrazek o podanych wartoœciach
	        String selectionArgs[] = {filename,description, Long.toString(user_id)};
	        c= getContentResolver().query(ImageContract.CONTENT_URI, new String[]{"i."+ImageContract.Columns._ID}, selection, selectionArgs, null);
	        c.moveToFirst();
	        if(!c.isAfterLast()){
	            img_id = c.getLong(0);
	            Log.i(LOG_TAG, "filename: " +filename+" description: "+ description+ " already exists in user: "+user_id);
	        }
	        c.close();
        }
        return img_id;
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
			case 2: // najczêœciej u¿ywane
				imf.sortOrder = "i."+ImageContract.Columns.TIME_USED + " DESC";
				imf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) imf);
				break;
	
			default:
				break;
			}
		
		return false;
	}

}
