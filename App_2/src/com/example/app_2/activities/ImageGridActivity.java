/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.app_2.activities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.actionbar.adapter.TitleNavigationAdapter;
import com.example.app_2.actionbar.model.IdPositionModel;
import com.example.app_2.actionbar.model.SpinnerNavItem;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.fragments.ExpressionListFragment;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity implements TextToSpeech.OnInitListener, OnNavigationListener{
    private static final String LOG_TAG = "ImageGridActivity";

    public static final String GRID_FRAGMENT_TAG = "FragmentGrid";
    public static final String LIST_FRAGMENT_TAG = "FragmentList";
    private static final String EXPRESSION_FRAGMENT_TAG = "FragmentExpression";
    public static final int PLEASE_WAIT_DIALOG = 1;
    public static ProgressDialog dialog;
    private static boolean doubleBackToExitPressedOnce = false;
	public static IdPositionModel actual_category_fk = new IdPositionModel();
    
    public static List<IdPositionModel> fragmentsHistory = new LinkedList<IdPositionModel>();
    private List<String> mCategoryTitles = new LinkedList<String>();
    private DrawerLayout mDrawerLayout;
    private int layout_width = 0 , layout_height = 0;
    
    public static boolean mEditMode =false;
    
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<SpinnerNavItem> navSpinner;
    private TitleNavigationAdapter title_nav_adapter;
    
    private int mCatBColor, mCtxBColor;
    
    private  ListView mDrawerList;
    private SimpleCursorAdapter categoriesAdapter;
  

   // private Map<String, Long> mCategoryMap  = new HashMap<String, Long>();
	public TextToSpeech tts;
    //CharSequence mTitle;
    //private CharSequence mDrawerTitle;
    //public ImageLoader imageLoader;
    public ExpressionListFragment elf;
    Long logged_user_root;
    Long logged_user_id;
    public ImageGridFragment igf;

    
    protected boolean mDualPane = false;
    
	private static final int TTS_REQUEST_CODE = 1;
    private static final int SETTINGS_REQUEST_CODE = 37;
    
    
    
    @SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        //LinearLayout ll = (LinearLayout) findViewById(R.layout.activity_grid);
        //Utils.setWallpaper(ll, App_2.maxHeight, App_2.getMaxWidth(), null, ScalingLogic.CROP);
		SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);			// pobranie informacji o zalogowanym u¿ytkowniku
		logged_user_root = sharedPref.getLong("logged_user_root", Database.getMainRootFk());
		logged_user_id = sharedPref.getLong("logged_user_id", 0);
		
		SharedPreferences sharedPref2 = PreferenceManager.getDefaultSharedPreferences(this);
		mCatBColor =sharedPref2.getInt("category_view_background", 0xff33b5e5);
        mCtxBColor =sharedPref2.getInt("context_category_view_background",0xffe446ff);
        
		if(logged_user_root == Database.getMainRootFk()) //jeœli ktoœ zalogowany tryb edycji wy³¹czony
			mEditMode = true;
		else
			mEditMode = false;
		
        setActionBar();
		getTTS();
		
		
		igf = new ImageGridFragment();
		//Bundle args = new Bundle();		
		
		//if(actual_category_fk.getCategoryId()!=null)
			//args.putLong("CATEGORY_ID", actual_category_fk.getCategoryId());
		//else if(logged_user_root != null){	
		//	actual_category_fk.setCategoryId(logged_user_root);
			//args.putLong("CATEGORY_ID", logged_user_root);	
		//}
    
    	if(logged_user_root != null)
    		actual_category_fk.setCategoryId(logged_user_root);
    	
		//igf.setArguments(args);



        
		
		ListView categoriesListView = (ListView) findViewById(R.id.categories_list);		
		if(categoriesListView != null)
			mDualPane = categoriesListView.getVisibility() == View.VISIBLE;
		
		setDrawerOrLeftList();// ustawienie drawera lub listy kategorii z lewej strony
        
        
        
        // za³adowanie do content_frame ImageGridFragment
        if (getSupportFragmentManager().findFragmentByTag(GRID_FRAGMENT_TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, igf, GRID_FRAGMENT_TAG);
            ft.commit();
        }
        
        if (getSupportFragmentManager().findFragmentByTag(EXPRESSION_FRAGMENT_TAG) == null) {
        	elf= new ExpressionListFragment();
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	ft.replace(R.id.horizontal_listview, elf);
            ft.commit();
        }
        

    }
        
    private void getTTS(){
    	// pobranie syntezatora mowy
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_REQUEST_CODE);
    }
    
    private void setActionBar(){
    	  mActionBar = getActionBar();
          mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
          if(mEditMode){
      		mActionBar.setBackgroundDrawable(new ColorDrawable(0xff0084b3)); 
      		mActionBar.setDisplayShowTitleEnabled(false);
      		mActionBar.setDisplayShowTitleEnabled(true);
          }else{
      		mActionBar.setBackgroundDrawable(new ColorDrawable(0xff4d055e)); 
      		mActionBar.setDisplayShowTitleEnabled(false);
      		mActionBar.setDisplayShowTitleEnabled(true);
      		Uri uri = Uri.parse(UserContract.CONTENT_URI+"/"+logged_user_id);
      		Cursor c = getContentResolver().query(uri, new String[]{UserContract.Columns.IMG_FILENAME}, null ,null, null);
      		c.moveToFirst();
      		if(!c.isAfterLast()){
      			String path = Storage.getPathToScaledBitmap(c.getString(0), 50);
      			Bitmap user_icon = ScalingUtilities.decodeFile(path, 50, 50, ScalingLogic.FIT);
      			mActionBar.setIcon(new BitmapDrawable(getResources(),user_icon));
      		}
      		c.close();
          }
          	
       
  		navSpinner = new ArrayList<SpinnerNavItem>();
  		navSpinner.add(new SpinnerNavItem("Alfabetycznie", R.drawable.sort_ascend));
  		navSpinner.add(new SpinnerNavItem("Ostatnio zmodyfikowane", R.drawable.clock));
  		navSpinner.add(new SpinnerNavItem("Najczêœciej u¿ywane", R.drawable.favourites));
  		
  		title_nav_adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
  		mActionBar.setListNavigationCallbacks(title_nav_adapter, this);
    }
    
    public void onButtonClick(View v){
    	switch (v.getId()) {
		case R.id.clear_ex_button:
			elf.removeAllImages();
			break;
		case R.id.play_button:
			elf.speakOutExpression();
			break;

		default:
			break;
		}
    }
    /*
    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
        case PLEASE_WAIT_DIALOG:
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setTitle("Obliczanie, proszê czekaæ....");
            dialog.setMessage("Proszê czekaæ....");
            return dialog;
 
        default:
            break;
        }
        return null;
    }
    */
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(!mDualPane)
        	mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(!mDualPane)
        	mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if(!mDualPane)
	        if (mDrawerToggle.onOptionsItemSelected(item)) {
	          return true;
	        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
	        case R.id.action_add_image:// TODO dodawanie nowych obrazków 
	        	Intent bind_intent = new Intent(this, BindImagesToCategoryActivity.class);
	        	if(actual_category_fk.getCategoryId()== Database.getMainRootFk()){
	        		Toast.makeText(getApplicationContext(), "Tutaj nie mo¿na dodawaæ obrazków, wybierz najpierw u¿ytkownika", Toast.LENGTH_LONG).show();
	        		return true;
	        	}
	        	bind_intent.putExtra("executing_category_id", actual_category_fk.getCategoryId());
	        	startActivity(bind_intent);
	        	return true;
	        	
	        case R.id.action_add_new_image:
	        	Intent new_imageIntent = new Intent(this, AddImageActivity.class);
	        	Bundle bundle = new Bundle();
	        	bundle.putLong("cat_fk",actual_category_fk.getCategoryId());
	        	new_imageIntent.putExtras(bundle);
	        	startActivity(new_imageIntent);
	        	return true;
	        	
            case R.id.action_settings:
    			Intent intent = new Intent(this, SettingsActivity.class);
    			startActivity(intent);
    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    			finish();
                return true;
            	
            case R.id.action_logout:
            	igf.mEditMode = true;
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    			SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);
    			SharedPreferences.Editor editor = sharedPref.edit();
    			
    			//zapisanie usawieñ u¿ytkownika do bazy
    			Uri uri = Uri.parse(UserContract.CONTENT_URI + "/" + sharedPref.getLong("logged_user_id", 0));
    			ContentValues cv = new ContentValues();
    			cv.put(UserContract.Columns.FONT_SIZE, sp.getInt("pref_img_desc_font_size", 15));
    			cv.put(UserContract.Columns.IMG_SIZE, sp.getInt("pref_img_size", 100));
    			cv.put(UserContract.Columns.CAT_BACKGROUND, String.valueOf(sp.getInt("category_view_background", 0xff33b5e5)));
    			cv.put(UserContract.Columns.CONTEXT_CAT_BACKGROUND, String.valueOf(sp.getInt("context_category_view_background", 0xffe446ff)));
    			getContentResolver().update(uri, cv, null,null);    			
    			editor.putLong("logged_user_root", Database.getMainRootFk());
    			editor.putLong("logged_user_id", 0 );
    			editor.commit();
    			
    			fragmentsHistory.clear();
    			Intent i = getIntent();
    			finish();
    			startActivity(i);
    		
            	replaceCategory(Database.getMainRootFk(), 0, false);
            	
            	return true;
            	
            case R.id.action_login_user:
            	Intent login_intent = new Intent(this, UserLoginActivity.class);
            	startActivity(login_intent);
            	finish();
            	fragmentsHistory.clear();
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
    	if(mEditMode)
	        inflater.inflate(R.menu.grid_activity_actions, menu);    		
    	else
    		inflater.inflate(R.menu.grid_activity_login_user, menu);
 
        return true;
    }
    
    public void addImageToAdapter(ImageObject image_object){
    	if(elf!=null){
    		elf.addImageToExAdapter(image_object);
    	}
    	else{
    		elf = new ExpressionListFragment();
    		elf.addImageToExAdapter(image_object);
    	}
    }

    
	private ViewTreeObserver.OnGlobalLayoutListener vto = new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			layout_width = mDrawerLayout.getWidth();
			layout_height = mDrawerLayout.getHeight();
		}
	};
	
	

	
    private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			if(actual_category_fk.getCategoryId() != id){
				//selectItem(position);
				replaceCategory(id, 0, true);
				 mDrawerList.setItemChecked(position, true);
				 if(!mDualPane)
				    	mDrawerLayout.closeDrawers();
				igf.finishActionMode();
				IdPositionModel firstCategory = fragmentsHistory.get(0);
				fragmentsHistory.clear();
				if(firstCategory != null && id!= firstCategory.getCategoryId()){
					fragmentsHistory.add(firstCategory);
				}
			}
			else{
				Toast.makeText(getApplicationContext(), "Jesteœ ju¿ w tej kategorii", Toast.LENGTH_SHORT ).show();
			    if(!mDualPane)
			    	mDrawerLayout.closeDrawers();
			}
		}
		/*
		private void selectItem(int position){
			Long cat_id = mCategoryMap.get(mCategoryTitles.get(position));
			//replaceGridFragment(new IdPositionModel(cat_id), false, true);
			replaceCategory(cat_id, 0, true);
			
			 // Highlight the selected item, update the title, and close the drawer
		    mDrawerList.setItemChecked(position, true);
		    if(!mDualPane)
		    	mDrawerLayout.closeDrawers();
		}
		*/

    }
    
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			Log.i("TTS", "tts shutdown");
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		int result;
		if (status == TextToSpeech.SUCCESS) {			
			Locale pol_loc = new Locale("pl", "pl_PL");
			if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(pol_loc)){
				result = tts.setLanguage(pol_loc);
				if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED){
					Log.e("TTS", "LANG_NOT_SUPPORTED");
				}
			}else{
				result=tts.setLanguage(Locale.ENGLISH);
			}			
		} else {
			Log.e("TTS", "Initialization Failed");
		}
	}
	
	public void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("actual_category_fk", actual_category_fk.getCategoryId());
	};
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		actual_category_fk.setCategoryId(savedInstanceState.getLong("actual_category_fk"));
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_REQUEST_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				tts = new TextToSpeech(this, this); // success, crate the TTS instance
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setDrawerOrLeftList(){
		if(!mDualPane){
		      mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		      mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(vto);
		      mDrawerToggle = new ActionBarDrawerToggle(
			                this,                  /* host Activity */
			                mDrawerLayout,         /* DrawerLayout object */
			                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
			                R.string.drawer_open,  /* "open drawer" description */
			                R.string.drawer_close  /* "close drawer" description */
		                ) {
				    		String mActionBarTitle = null;
				    		ActionBar mActionBar = getActionBar();
		
				            /** Called when a drawer has settled in a completely closed state. */
				            public void onDrawerClosed(View view) {
				            	mActionBar.setTitle(mActionBarTitle);
				            }
		
				            /** Called when a drawer has settled in a completely open state. */
				            public void onDrawerOpened(View drawerView) {
				            	mActionBarTitle = (String) mActionBar.getTitle();
				            	mActionBar.setTitle( "Wybierz kategoriê");
				            }
		        };
		        mDrawerLayout.setDrawerListener(mDrawerToggle);
		        mDrawerList  = (ListView) findViewById(R.id.left_drawer);
		}else{
			mDrawerList = (ListView) findViewById(R.id.categories_list);
		}
			
		
		String[] projection = {"i."+ImageContract.Columns._ID, "i."+ImageContract.Columns.FILENAME, "i."+ImageContract.Columns.CATEGORY, "i."+ImageContract.Columns.IS_CONTEXTUAL_CATEGORY};
		String selection;
		String[] selectionArgs = {""};

  
        getActionBar().setDisplayHomeAsUpEnabled(true);


        
        
        if(logged_user_id!=0){
        	selection = "i."+ImageContract.Columns.CATEGORY + " IS NOT NULL AND (i."+ImageContract.Columns.CATEGORY +" <> ?) AND "+"i."+ImageContract.Columns.AUTHOR_FK+" = ? ";
        	selectionArgs = new String[2];
        	selectionArgs[0]="";
        	selectionArgs[1]= String.valueOf(logged_user_id);
        }
        else
        	selection = "i."+ImageContract.Columns.CATEGORY + " IS NOT NULL AND (i."+ImageContract.Columns.CATEGORY +" <> ?)";
        
        
        
        Cursor categoryCursor = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, selectionArgs, ImageContract.Columns._ID+", " +ImageContract.Columns.AUTHOR_FK);
        startManagingCursor(categoryCursor);
        /*
        categoryCursor.moveToFirst();
        while(!categoryCursor.isAfterLast()){
        	String category = categoryCursor.getString(2);
        	Long id = categoryCursor.getLong(0);
        	mCategoryMap.put(category, id);
        	mCategoryTitles.add(category);
        	categoryCursor.moveToNext();
        }
        */
        
       
		String[] from = new String[] {
				   ImageContract.Columns._ID, 
				   ImageContract.Columns.FILENAME,
				   ImageContract.Columns.CATEGORY,
				   ImageContract.Columns.IS_CONTEXTUAL_CATEGORY};
		int[] to = new int[] { 0, R.id.drawer_category_icon, R.id.category };
		
        categoriesAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.drawer_row, categoryCursor, from,to, 0);
    	categoriesAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			       if(view.getId() == R.id.drawer_category_icon/*category_image*/){
						String path = Storage.getPathToScaledBitmap(cursor.getString(1),50);
						ImageLoader.loadBitmap(path, (ImageView) view);

						if(cursor.getInt(3) == 1)
							view.setBackgroundColor(mCatBColor);
						else
							//view.setBackgroundColor(Color.argb(120, 0, 255, 0));
							view.setBackgroundColor(mCtxBColor);

						
			           return true; //true because the data was bound to the view
			       }
			       return false;
			   }
			});

        
        // Set the categoriesAdapter for the list view
    	mDrawerList.setAdapter(categoriesAdapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	@Override
	public void onBackPressed() {
		if(fragmentsHistory.size()>0){
			//IdPositionModel previousCategory = fragmentsHistory.get(fragmentsHistory.size()-1);
			
			//replaceGridFragment(previousCategory, true, false);
			gotoPreviousCategory();
		}
		else{ // opuszczam aplikacje
			if (doubleBackToExitPressedOnce) {
	            super.onBackPressed();
	            finish();
	            return;
	        }
	        this.doubleBackToExitPressedOnce = true;
	        Toast.makeText(this, "Naciœnij jeszcze raz aby wyjœæ", Toast.LENGTH_SHORT).show();
	        new Handler().postDelayed(new Runnable() {

	            @Override
	            public void run() {
	             doubleBackToExitPressedOnce=false;   

	            }
	        }, 2000);
		}
	}
	

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if(actual_category_fk== null){
			return false;
		}
		Bundle args = new Bundle();		
		if(igf.getActivity() != null){
			switch (itemPosition) {
	
			case 0: // alfabetycznie 
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.DESC + " COLLATE LOCALIZED ASC";			
				args.putLong("CATEGORY_ID", actual_category_fk.getCategoryId());
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
			case 1: // ostatnio zmodyfikowane
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.MODIFIED + " DESC";
				args.putLong("CATEGORY_ID", actual_category_fk.getCategoryId());
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
			case 2: // najczêœciej u¿ywane
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.TIME_USED + " DESC";
				args.putLong("CATEGORY_ID", actual_category_fk.getCategoryId());
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
	
			default:
				break;
			}
		}
		
		return false;
	}
	/*
	public void replaceGridFragment(IdPositionModel replaceToCategory, boolean gotoPreviousFragment, boolean addPreviousToHistory){
		igf = new ImageGridFragment();
		Bundle args = new Bundle();	
		Long repCatId = replaceToCategory.getCategoryId();
		int repCatPos = replaceToCategory.getPosition();
		
		args.putLong("CATEGORY_ID", repCatId);
		args.putInt("RET_POSITION", repCatPos);
		
		igf.setArguments(args);
		IdPositionModel prevCat = actual_category_fk;
		actual_category_fk = new IdPositionModel(repCatId, repCatPos);
		
		

	     
	    if(gotoPreviousFragment){
			igf.setArguments(args);
	    	fragmentsHistory.remove(fragmentsHistory.size()-1);
			setActionBarTitleFromCategoryId(id_pos.getCategoryId());
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);//R.anim.slide_in_right, R.anim.slide_out_left);
		    ft.replace(R.id.content_frame, igf, ImageGridActivity.GRID_FRAGMENT_TAG);
		    ft.commit();	

	    }
	    else{
			igf.setArguments(args);
	    	if(addPreviousToHistory)
	    		fragmentsHistory.add(prevCategoryFk);
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		    ft.replace(R.id.content_frame, igf, ImageGridActivity.GRID_FRAGMENT_TAG);
		    ft.commit();	
	    }
        
	}
	*/
	
	public void replaceCategory(Long categoryId, int position, boolean addPreviousToHistory){
		actual_category_fk.setNextCatPosition(position);
		
		igf = new ImageGridFragment();
		/*
		Bundle args = new Bundle();	
		args.putLong("CATEGORY_ID", categoryId);
		igf.setArguments(args);
		*/
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
	    ft.replace(R.id.content_frame, igf, ImageGridActivity.GRID_FRAGMENT_TAG);
	    ft.commit();
	    
	    if(addPreviousToHistory){
	    	fragmentsHistory.add(actual_category_fk);
	    }
	    
	    actual_category_fk = new IdPositionModel(categoryId);	
		setActionBarTitleFromCategoryId(actual_category_fk.getCategoryId());
	    
	}
	
	public void gotoPreviousCategory(){
		IdPositionModel previousCategory = fragmentsHistory.get(fragmentsHistory.size()-1);
		//Long prevCatId = previousCategory.getCategoryId();
		//int prevCatPos = actual_category_fk.getNextCatPosition();
	
		igf = new ImageGridFragment();
		/*
		Bundle args = new Bundle();			
		args.putLong("CATEGORY_ID", prevCatId);
		args.putInt("RET_POSITION", prevCatPos);
		igf.setArguments(args);
		*/
		
		
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    ft.replace(R.id.content_frame, igf, ImageGridActivity.GRID_FRAGMENT_TAG);
	    ft.commit();
		actual_category_fk = previousCategory;
	    fragmentsHistory.remove(fragmentsHistory.size()-1);
	    setActionBarTitleFromCategoryId(actual_category_fk.getCategoryId());
		
		
		
	}
	
	public void notifyCategoryAdapter(){
		categoriesAdapter.notifyDataSetChanged();
	}
	
	private void setActionBarTitleFromCategoryId(Long category_id){
		Uri uri = Uri.parse(ImageContract.CONTENT_URI+"/"+category_id);
		Cursor c = getApplicationContext().getContentResolver().query(uri, new String[]{ImageContract.Columns.DESC},null,null,null);
		if(c != null){
			c.moveToFirst();
			if(!c.isAfterLast()){
				getActionBar().setTitle(c.getString(0));
			}
			c.close();
		}

		
	}

}
