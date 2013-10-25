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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ExpressionListFragment;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity implements TextToSpeech.OnInitListener{
    private static final String TAG = "ImageGridActivity";
    private static final String GRID_FRAGMENT_TAG = "FragmentGrid";
    private static final String EXPRESSION_FRAGMENT_TAG = "FragmentExpression";
    public static final int PLEASE_WAIT_DIALOG = 1;
    public static ProgressDialog dialog;
    
    private List<String> mCategoryTitles = new LinkedList<String>();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private  ListView mDrawerList;

    private Map<String, Long> mCategoryMap;
	public TextToSpeech tts;
    CharSequence mTitle;
    CharSequence mDrawerTitle;
    public ImageLoader imageLoader;
    public ExpressionListFragment elf;
    
	public static final int MY_DATA_CHECK_CODE = 1;
    
    
    
    @SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#330000ff")));
        //getActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));
        setContentView(R.layout.activity_grid);
		mCategoryMap = new HashMap<String, Long>();
		
        // pobranie syntezatora mowy
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        
		// ustawienie drawera
        //expandedImageView = (ImageView) findViewById(R.id.expanded_image);
		imageLoader = new ImageLoader(getApplicationContext());	
        setDrawer();
        
        // za³adowanie do content_frame ImageGridFragment
        if (getSupportFragmentManager().findFragmentByTag(GRID_FRAGMENT_TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, new ImageGridFragment(), GRID_FRAGMENT_TAG);
            ft.commit();
        }
        

        	elf= new ExpressionListFragment();
        	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	 ft.replace(R.id.horizontal_listview, elf);
             ft.commit();
        

    }
    
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
            case R.id.action_search:
                //openSearch();
                return true;
            case R.id.action_settings:
                //openSettings();
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
        return super.onCreateOptionsMenu(menu);
    }
    
    public void addImageToAdapter(String s){
    	if(elf!=null){
    		elf.addImageToAdapter(s);
    	}
    	else{
    		elf = new ExpressionListFragment();
    		elf.addImageToAdapter(s);
    	}
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position);		
			
		}
		
		private void selectItem(int position){
            //expandedImageView.setVisibility(View.GONE);
			
			Fragment fragment = new ImageGridFragment();
			Bundle args = new Bundle();
			Long cat_id = mCategoryMap.get(mCategoryTitles.get(position));
			Log.i("info", "parent " + Utils.getKeyByValue(mCategoryMap, cat_id));
			
			
			args.putLong("CATEGORY_ID", cat_id);
			fragment.setArguments(args);
			
			//Insert the fragment by replacing an existing fragment
			if(getSupportFragmentManager().findFragmentByTag(GRID_FRAGMENT_TAG)!=null){
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            ft.replace(R.id.content_frame, fragment, GRID_FRAGMENT_TAG);
	            ft.addToBackStack(null);
	            ft.commit();				
			}
			
			 // Highlight the selected item, update the title, and close the drawer
		    mDrawerList.setItemChecked(position, true);
		    //setTitle(subCategories.get(position).getCategory());
		    //mTitle = getTitle();
		    //mDrawerLayout.closeDrawer(mDrawerList);
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
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		int result;
		if (status == TextToSpeech.SUCCESS) {			
			Locale pol_loc = new Locale("pl", "pl_PL");
			if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(Locale.ENGLISH)){
				result = tts.setLanguage(pol_loc);
			}else{
				result=tts.setLanguage(Locale.ENGLISH);
			}
				
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "LANG_NOT_SUPPORTED");
			} else {
				//btnSpeak.setEnabled(true);
				//speakOut("init successful");
			}
		} else {
			Log.e("TTS", "Initialization Failed");
		}
	}
	
	public void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, crate the TTS instance
				tts = new TextToSpeech(this, this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}
	


	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setDrawer(){
	
		mDrawerTitle = "Wybierz kategoriê";
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
        String[] projection= {ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
        Cursor cursor = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, selectionArgs, null);
        
        MatrixCursor extras = new MatrixCursor(new String[] {ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY});
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
        //c.close();
        
        /*
        rootCategory = db.getRootCategory();
        if(rootCategory!=null){
	        subCategories = db.getSubcategories(rootCategory.getId());
	        
	        for(ImageObject i: subCategories){
	        	mCategoryTitles.add(i.getCategory());
	        }
        }
        */
        
		String[] from = new String[] {
				   ImageContract.Columns._ID, 
				   ImageContract.Columns.PATH,
				   ImageContract.Columns.CATEGORY};
				// Fields on the UI to which we map
		int[] to = new int[] { 0, R.id.category_image, R.id.category_name };
		
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.drawer_row, c, from,to, 0);
    	adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			   /** Binds the Cursor column defined by the specified index to the specified view */
			   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			       if(view.getId() == R.id.category_image){
						 String path = Images.getImageThumbsPath(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH)));
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
	
	/*
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		/*
	    new AlertDialog.Builder(this)
	        .setTitle("Really Exit?")
	        .setMessage("Are you sure you want to exit?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					overridePendingTransition  (R.anim.right_slide_in, R.anim.right_slide_out);
					ImageGridActivity.super.onBackPressed();
					
					
				}
	        }).create().show();
	     
		this.finish();
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);

	}
*/
}
