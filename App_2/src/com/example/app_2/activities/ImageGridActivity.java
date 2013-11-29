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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.actionbar.adapter.TitleNavigationAdapter;
import com.example.app_2.actionbar.model.SpinnerNavItem;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ExpressionListFragment;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity implements TextToSpeech.OnInitListener, OnNavigationListener{
    private static final String TAG = "ImageGridActivity";

    public static final String GRID_FRAGMENT_TAG = "FragmentGrid";
    private static final String EXPRESSION_FRAGMENT_TAG = "FragmentExpression";
    public static final int PLEASE_WAIT_DIALOG = 1;
    public static ProgressDialog dialog;
    private static boolean doubleBackToExitPressedOnce = false;
	public static Long actual_category_fk;
    
    public static List<Long> fragmentsHistory = new LinkedList<Long>();
    private List<String> mCategoryTitles = new LinkedList<String>();
    private DrawerLayout mDrawerLayout;
    
    private ActionBar actionBar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<SpinnerNavItem> navSpinner;
    private TitleNavigationAdapter title_nav_adapter;
    
    private  ListView mDrawerList;

    private Map<String, Long> mCategoryMap;
	public TextToSpeech tts;
    CharSequence mTitle;
    CharSequence mDrawerTitle;
    public ImageLoader imageLoader;
    public ExpressionListFragment elf;
    
    public ImageGridFragment igf;
    
	private static final int TTS_REQUEST_CODE = 1;
    private static final int SETTINGS_REQUEST_CODE = 37;
    
    
    
    @SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       

        actionBar = getActionBar();
		//actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Alfabetycznie", R.drawable.ic_launcher));
		navSpinner.add(new SpinnerNavItem("Ostatnio zmodyfikowane", R.drawable.ic_launcher));
		navSpinner.add(new SpinnerNavItem("Najcz�ciej u�ywane", R.drawable.ic_launcher));
		
		title_nav_adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
		actionBar.setListNavigationCallbacks(title_nav_adapter, this);
        igf = new ImageGridFragment();
        
        setContentView(R.layout.activity_grid);
		mCategoryMap = new HashMap<String, Long>();
		
        // pobranie syntezatora mowy
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TTS_REQUEST_CODE);
        
		// ustawienie drawera
        //expandedImageView = (ImageView) findViewById(R.id.expanded_image);
		imageLoader = new ImageLoader(getApplicationContext());	
        setDrawer();
        
        // za�adowanie do content_frame ImageGridFragment
        if (getSupportFragmentManager().findFragmentByTag(GRID_FRAGMENT_TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, igf, GRID_FRAGMENT_TAG);
            ft.commit();
        }
        

        	elf= new ExpressionListFragment();
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	ft.replace(R.id.horizontal_listview, elf);
            ft.commit();
        

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
    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
        case PLEASE_WAIT_DIALOG:
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     
            dialog.setCancelable(false);
            dialog.setTitle("Obliczanie, prosz� czeka�....");
            dialog.setMessage("Prosz� czeka�....");
            return dialog;
 
        default:
            break;
        }
        return null;
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
	        case R.id.action_add_image:
	        	Intent bind_intent = new Intent(this, BindImagesToCategory.class);
	        	bind_intent.putExtra("executing_category_id", fragmentsHistory.get(fragmentsHistory.size() - 1));
	        	startActivity(bind_intent);
	        	return true;
	        	
            case R.id.action_settings:
    			Intent intent = new Intent(this, SettingsActivity.class);
    			startActivity(intent);
    			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    			finish();
                return true;
                
                
            case R.id.action_search:
            	
            	return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grid_activity_actions, menu);
        
        
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 
        return super.onCreateOptionsMenu(menu);
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			if(actual_category_fk != id)
				selectItem(position);
			else{
				Toast.makeText(getApplicationContext(), "Jeste� ju� w tej kategorii", Toast.LENGTH_SHORT ).show();
			    mDrawerLayout.closeDrawers();
			}
		}
		
		private void selectItem(int position){
			Long cat_id = mCategoryMap.get(mCategoryTitles.get(position));
			replaceGridFragment(cat_id, false);
			
			 // Highlight the selected item, update the title, and close the drawer
		    mDrawerList.setItemChecked(position, true);
		    mDrawerLayout.closeDrawers();
		}
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
			}else{
				result=tts.setLanguage(Locale.ENGLISH);
			}
				
			if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "LANG_NOT_SUPPORTED");
			} else {
			}
		} else {
			Log.e("TTS", "Initialization Failed");
		}
	}
	
	public void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
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
	private void setDrawer(){
	
		mDrawerTitle = "Wybierz kategori�";
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
	                this,                  /* host Activity */
	                mDrawerLayout,         /* DrawerLayout object */
	                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
	                R.string.drawer_open,  /* "open drawer" description */
	                R.string.drawer_close  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerList  = (ListView) findViewById(R.id.left_drawer);
        String[] projection= {ImageContract.Columns._ID, ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
        Cursor cursor = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, selectionArgs, null);
        
        MatrixCursor extras = new MatrixCursor(new String[] {ImageContract.Columns._ID, ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY});
        extras.addRow(new String[]{"-1","1.jpg", "HOME"});
        Cursor[] cursors = {extras, cursor};
        Cursor c = new MergeCursor(cursors);
        c.moveToFirst();
        while(!c.isAfterLast()){
        	String category = c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY));
        	Long id = c.getLong(c.getColumnIndex(ImageContract.Columns._ID));
        	mCategoryMap.put(category, id);
        	mCategoryTitles.add(category);
        	c.moveToNext();
        }
       
		String[] from = new String[] {
				   ImageContract.Columns._ID, 
				   ImageContract.Columns.FILENAME,
				   ImageContract.Columns.CATEGORY};
		int[] to = new int[] { 0, R.id.drawer_category_icon, R.id.category };
		
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.drawer_row, c, from,to, 0);
    	adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			   /** Binds the Cursor column defined by the specified index to the specified view */
			   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			       if(view.getId() == R.id.drawer_category_icon/*category_image*/){
						 String path = Storage.getPathToScaledBitmap(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.FILENAME)),50);
						 ImageLoader.loadBitmap(path, (ImageView) view, true);
			           return true; //true because the data was bound to the view
			       }
			       return false;
			   }
			});

        
        // Set the adapter for the list view
    	mDrawerList.setAdapter(adapter);

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	
	@Override
	public void onBackPressed() {
		if(fragmentsHistory.size()>0){
			Long previousFragmentId = fragmentsHistory.get(fragmentsHistory.size()-1);
			replaceGridFragment(previousFragmentId, true);
		}
		else{ // opuszczam aplikacje
			if (doubleBackToExitPressedOnce) {
	            super.onBackPressed();
	            return;
	        }
	        this.doubleBackToExitPressedOnce = true;
	        Toast.makeText(this, "Naci�nij jeszcze raz aby wyj��", Toast.LENGTH_SHORT).show();
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
		Bundle args = new Bundle();		
		if(igf.getActivity() != null){
			switch (itemPosition) {
	
			case 0: // alfabetycznie 
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.DESC + " ASC";			
				args.putLong("CATEGORY_ID", actual_category_fk);
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
			case 1: // ostatnio zmodyfikowane
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.MODIFIED + " DESC";
				args.putLong("CATEGORY_ID", actual_category_fk);
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
			case 2: // najcz�ciej u�ywane
				ImageGridFragment.sortOrder = "i."+ImageContract.Columns.TIME_USED + " DESC";
				args.putLong("CATEGORY_ID", actual_category_fk);
				igf.getLoaderManager().restartLoader(1, args, (LoaderCallbacks<Cursor>) igf);
				break;
	
			default:
				break;
			}
		}
		
		return false;
	}
	
	public void replaceGridFragment(Long category_id, boolean gotoPreviousFragment){
		igf = new ImageGridFragment();
		Bundle args = new Bundle();			
		args.putLong("CATEGORY_ID", category_id);
		igf.setArguments(args);
		

		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
	    ft.replace(R.id.content_frame, igf, ImageGridActivity.GRID_FRAGMENT_TAG);
	    ft.commit();	
	     
	    if(gotoPreviousFragment){
	    	fragmentsHistory.remove(fragmentsHistory.size()-1);
	    }
	    else{
	    	fragmentsHistory.add(actual_category_fk);
	    }
        
	}

}
