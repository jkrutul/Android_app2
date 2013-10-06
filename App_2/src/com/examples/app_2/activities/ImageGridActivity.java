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

package com.examples.app_2.activities;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.Images.ThumbsProcessTask;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity implements TextToSpeech.OnInitListener{
    private static final String TAG = "ImageGridActivity";
    public static final int PLEASE_WAIT_DIALOG = 1;
    public static ProgressDialog dialog;
    
    private List<String> mCategoryTitles = new LinkedList<String>();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private static ListView mDrawerList;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    public static ImageGridActivity mInstance;
    
    private ImageObject rootCategory;
    private List<ImageObject> allCategories;
    private ImageObject parentCategory;
    private List<ImageObject> subCategories;
    private List<ImageObject> categoryLeafs;
    
    private List<ImageObject> categoriesInDrawer;
    
    CharSequence mTitle;
    CharSequence mDrawerTitle;
    
	private static TextToSpeech tts;
	public static final int MY_DATA_CHECK_CODE = 1;
    
    
    
    @SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        // ustawienie syntezatora mowy
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        
		// ustawienie drawera
        mInstance = this;
        App_2.actvity= mInstance;
        mDrawerTitle = "Wybierz kategoriê";

        
        //Database db = Database.getInstance(getApplicationContext());
        //db.open();
					
        
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
        getActionBar().setHomeButtonEnabled(true);
    
        
        mDrawerList  = (ListView) findViewById(R.id.left_drawer);
        
        /*
        rootCategory = db.getRootCategory();
        if(rootCategory!=null){
	        subCategories = db.getSubcategories(rootCategory.getId());
	        
	        for(ImageObject i: subCategories){
	        	mCategoryTitles.add(i.getCategory());
	        }
        }
        */
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mCategoryTitles)); // TODO zmieniæ na adapter z obrazkiem
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
 
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, new ImageGridFragment(), TAG);
            ft.commit();
        }

    }
    
    @Override
    public Dialog onCreateDialog(int dialogId) {
 
        switch (dialogId) {
        case PLEASE_WAIT_DIALOG:
            dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCancelable(false);
            dialog.setTitle("Obliczanie");
            dialog.setMessage("Proszê czekaæ....");
            dialog.setCancelable(true);
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
    

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			//parent.getChildAt(position).get
			selectItem(position);		
			
		}
		
		private void selectItem(int position){
			Fragment fragment = new ImageGridFragment();
			Bundle args = new Bundle();
			Long cat_id = subCategories.get(position).getId();
			args.putLong("CATEGORY_ID", cat_id);
			fragment.setArguments(args);
			
			//Insert the fragment by replacing an existing fragment
			if(getSupportFragmentManager().findFragmentByTag(TAG)!=null){
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            ft.replace(R.id.content_frame, fragment, TAG);
	            ft.commit();				
			}
			
			 // Highlight the selected item, update the title, and close the drawer
		    mDrawerList.setItemChecked(position, true);
		    setTitle(subCategories.get(position).getCategory());
		    mTitle = getTitle();
		    mDrawerLayout.closeDrawer(mDrawerList);
		}
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
	        Locale[] AvalLoc = Locale.getAvailableLocales();

	        for( Locale l: AvalLoc){
	        	if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(l))
	        		Log.i("TTS",l.toString());
	        }
	        //Log.i("TTS","Available locales " + Arrays.toString(AvalLoc));
	        
			Locale pol_loc = new Locale("pl", "pl_PL");
			if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(Locale.ENGLISH)){
				result = tts.setLanguage(pol_loc);
				}
			else{
				result=tts.setLanguage(Locale.ITALIAN);
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
	
	public static void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	public static void refreshDrawer(Long img_id){
	     Database db = Database.getInstance(App_2.getAppContext());
	     db.open();
		List<ImageObject> parentCategory = db.getParentCategory(img_id);
		List<ImageObject> subcategories = db.getSubcategories(img_id);
		List<String> categoryTitles = new LinkedList<String>();
		
        for(ImageObject i : parentCategory){
        	categoryTitles.add(i.getCategory());			
        }	
        for(ImageObject i : subcategories){
        	categoryTitles.add(i.getCategory());			
        }	
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(App_2.getAppContext(), R.layout.drawer_list_item, categoryTitles));
	}
}
