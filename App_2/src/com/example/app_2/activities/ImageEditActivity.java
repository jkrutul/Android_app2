package com.example.app_2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageListFragment;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;

public class ImageEditActivity extends FragmentActivity{
	private static ImageListFragment ilf;
	//private Spinner mSpinner;
	
	ArrayList<ImageSpinnerItem> items;
	private final static int TAKE_PIC_REQUEST = 86;
	private final static int FILE_SELECT_REQUEST = 25;
	
	private final static String IMAGE_LIST_FRAGMENT= "image_list_fragment";
	

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_image_edit);
        ilf = new ImageListFragment();
        
        
        if (getSupportFragmentManager().findFragmentByTag(IMAGE_LIST_FRAGMENT) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fcontainer, ilf, IMAGE_LIST_FRAGMENT);
            ft.commit();
        }
       
    }
    
    
    @Override
    protected void onResume() {
    	super.onResume();
		getSupportLoaderManager().initLoader(0, null, (LoaderCallbacks<Cursor>)ilf);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	ilf = null;
    };
         
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
				}
				break;
			case TAKE_PIC_REQUEST:
				if (resultCode == RESULT_OK) {
					Intent i = new Intent(this, NewImgTemplateActivity.class);
					startActivity(i);
				}
				break;

			}
		}
		
}
