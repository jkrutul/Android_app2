package com.example.app_2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.adapters.MySpinnerAdapter;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageListFragment;
import com.example.app_2.provider.SpinnerItem;

public class ImageEditActivity extends FragmentActivity{
	private Spinner mSpinner;
	ArrayList<SpinnerItem> items;
	private ImageListFragment ilf;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_image_edit);
        
        mSpinner = (Spinner) findViewById(R.id.category_select_spinner);
        addItemsOnSpinner();
        ilf = new ImageListFragment();
    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.add(R.id.fcontainer, ilf);
        ft.commit();
        //final  com.example.app_2.fragments.ImageListFragment ilf = ( com.example.app_2.fragments.ImageListFragment) findViewById(R.id.titles);
        
    }
    
	private void addItemsOnSpinner() {

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			Bundle bundle = new Bundle();
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SpinnerItem data = items.get(position);
				if(data.isHint()){
					TextView tv = (TextView)selectedItemView;
					tv.setTextColor(Color.rgb(148, 150, 148));
				}
				bundle.putLong("cat_id", data.getItemId());
				getSupportLoaderManager().restartLoader(0, bundle, ilf);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				bundle.putLong("cat_id", -1);
				getSupportLoaderManager().restartLoader(0, bundle, ilf);
			}

		});
		
		items =  new ArrayList<SpinnerItem>();
		items.add(new SpinnerItem(null,"Wybierz kategoriê", Long.valueOf(-1), true));
		String[] projection = { ImageContract.Columns._ID, ImageContract.Columns.CATEGORY, ImageContract.Columns.PATH };
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		Cursor c = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			items.add(new SpinnerItem(c.getString(2), c.getString(1), c.getLong(0),false));
			c.moveToNext();
		}
		c.close();
		
		MySpinnerAdapter mySpinnerAdapter = new MySpinnerAdapter(this, android.R.layout.simple_spinner_item, items);
		mySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mySpinnerAdapter);
		mSpinner.setSelection(items.size()-1);
	}

    
	// Create the menu based on the XML defintion
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.insert:
			i = new Intent(this, AddNewImageActivity.class);
			startActivity(i);
			return true;
		case R.id.add_folder:
			i = new Intent(this, AddImagesFromFolderActivity.class);
			startActivity(i);
			return true;
		case R.id.adduser:
			i = new Intent(this, AddUserActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	  public void onButtonClick(View view){
			switch(view.getId()){
				case R.id.submit_button:
					ImageDetailsFragment idf = (ImageDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details);
					boolean mDualPane = idf != null && idf.getView().getVisibility() == View.VISIBLE;
					if(mDualPane)
						idf.onButtonClick(view);
				    Toast.makeText(this, "Zmiany zosta³y zapisane", Toast.LENGTH_SHORT).show();				        			
			}
	  }

}
