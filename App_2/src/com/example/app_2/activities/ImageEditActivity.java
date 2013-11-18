package com.example.app_2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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
	private final static int TAKE_PIC_REQUEST = 86;
	private final static int FILE_SELECT_REQUEST = 25;
	

	
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
		case R.id.take_pic:
			i = new Intent(this, NewImgTemplateActivity.class);
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
	  
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {
			case FILE_SELECT_REQUEST:
				if (resultCode == RESULT_OK) {
					Intent i = new Intent(this, NewImgTemplateActivity.class);
					startActivity(i);
					/*
					Uri uri = data.getData();
					String path = null;
					try {
						if(uri != null){
						path = Utils.getPath(this, uri);
						ImageLoader.loadBitmap(path, mUserImage, true);
						mHintText.setVisibility(View.INVISIBLE);
						//Bitmap bitmap = BitmapCalc.decodeSampleBitmapFromFile(path,	150, 150);
						// drawable = new BitmapDrawable(bitmap);
						//mUserImage.setBackgroundDrawable(drawable);
						}
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					*/
				}
				break;
			case TAKE_PIC_REQUEST:
				if (resultCode == RESULT_OK) {
					Intent i = new Intent(this, NewImgTemplateActivity.class);
					startActivity(i);
					
					//String path_toIMG = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
					//ImageLoader.loadBitmap(path_toIMG, mUserImage, true);
					//mHintText.setVisibility(View.INVISIBLE);
					//Bitmap bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, mUserImage.getWidth(), mUserImage.getHeight());
					//Toast.makeText(this, path_toIMG, Toast.LENGTH_LONG).show();
					//BitmapDrawable drawable = new BitmapDrawable(bitmap);
					//mUserImage.setBackgroundDrawable(drawable);
				}
				break;

			}
			//super.onActivityResult(requestCode, resultCode, data);
		}
}
