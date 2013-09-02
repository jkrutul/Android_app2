package com.examples.app_2.activities;

import java.util.List;
import java.util.Random;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.app_2.R;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;



public class DatabaseTestActivity extends ListActivity {
	private Database mda;
	
	 @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_test_db);

	    mda = Database.getInstance();
	    mda.open();

	    List<ImageObject> values = mda.getAllImages();

	    // Use the SimpleCursorAdapter to show the
	    // elements in a ListView
	    ArrayAdapter<ImageObject> adapter = new ArrayAdapter<ImageObject>(this, android.R.layout.simple_list_item_1, values);
	    setListAdapter(adapter);
	  }

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
	    	break;
	    }
	    adapter.notifyDataSetChanged();
	  }

	  @Override
	  protected void onResume() {
	    mda.open();
	    super.onResume();
	  }

	  @Override
	  protected void onPause() {
	    mda.close();
	    super.onPause();
	  }
}
