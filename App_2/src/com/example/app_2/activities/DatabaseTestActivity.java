package com.example.app_2.activities;

import java.util.List;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ArrayAdapter;


import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.models.ImageObject;



public class DatabaseTestActivity extends ListActivity implements	LoaderCallbacks<Cursor>{
	Cursor c;
	SimpleCursorAdapter sca;
	
	 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_test_db);
	    c= getContentResolver().query(ImageContract.CONTENT_URI, 	null, null, null, null);
	    
	    String[] from = {ImageContract.Columns.PATH};
	    int [] to = {android.R.id.list};
	    sca = new SimpleCursorAdapter(getApplicationContext(),android.R.layout.simple_list_item_1, c, from, to, 0 );
	    //ArrayAdapter<ImageObject> adapter = new ArrayAdapter<ImageObject>(this, android.R.layout.simple_list_item_1, values);
	    setListAdapter(sca);
	  }
/*
	  // Will be called via the onClick attribute
	  // of the buttons in main.xml
	  public void onClick(View view) {
	    @SuppressWarnings("unchecked")
	    ArrayAdapter<ImageObject> adapter = (ArrayAdapter<ImageObject>) getListAdapter();
	    ImageObject mio = null;
	    switch (view.getId()) {
	    
	    case R.id.add:
	      String[] mios = new String[] { "Cool", "Very nice", "Hate it" };
	      int nextInt = new Random().nextInt(3);
	      ImageObject tmp_mio = new ImageObject(mios[nextInt]);
	      // Save the new comment to the database
	      adapter.add(mda.insertImage(tmp_mio));
	      break;
	      
	    case R.id.delete:
	      if (getListAdapter().getCount() > 0) {
	        mio = (ImageObject) getListAdapter().getItem(0);
	        mda.deleteImage(mio);
	        adapter.remove(mio);
	      }
	      break;
	     
	    case R.id.recreateDB:
	    	mda.recreateDB();
	    	adapter.notifyDataSetChanged();
	    	break;
	    	
	    case R.id.import_db:
	    	mda.importImageFromCsv("image_table.csv");
	    	break;
	    
	    case R.id.export_db:
	    	mda.exportImageToCsv("image_table.csv");
	    	break;
	    }
	    adapter.notifyDataSetChanged();
	  }
*/
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
