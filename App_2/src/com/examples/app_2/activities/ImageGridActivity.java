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
import java.util.concurrent.LinkedBlockingDeque;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
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
import com.example.app_2.models.CategoryObject;
import com.example.app_2.storage.Database;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
    private static final String TAG = "ImageGridActivity";
    private List<String> mCategoryTitles = new LinkedList<String>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    public static FragmentActivity mInstance;
    private List<CategoryObject> categories;
    
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        mInstance = this;
        App_2.actvity= mInstance;

        
        Database db = Database.getInstance(getApplicationContext());
        db.open();
					
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList  = (ListView) findViewById(R.id.left_drawer);
        
        categories = db.getAllCategories();
        for(CategoryObject i : categories){
        	mCategoryTitles.add(i.getCategoryName());			//TODO ³adowanie kategorii do drawera, co je¿eli pierwszy raz ³aduje 
        }	
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mCategoryTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        getActionBar().setDisplayHomeAsUpEnabled(true);


        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, new ImageGridFragment(), TAG);
            ft.commit();
        }

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grid_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener{

		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			//parent.getChildAt(position).get
			selectItem(position);		
			
		}
		
		private void selectItem(int position){
			Fragment fragment = new ImageGridFragment();
			Bundle args = new Bundle();
			Long cat_id = categories.get(position).getId();
			args.putLong("CATEGORY_ID", cat_id);
			fragment.setArguments(args);
			
			//Insert the fragment by replacing an existing fragment
			if(getSupportFragmentManager().findFragmentByTag(TAG)!=null){
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            ft.replace(R.id.content_frame, fragment, TAG);
	            ft.commit();				
			}
		}
		
    	
    }
}
