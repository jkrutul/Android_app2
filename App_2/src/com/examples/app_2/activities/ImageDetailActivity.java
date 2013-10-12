package com.examples.app_2.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContentProvider;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImageDetailActivity extends Activity {
	  private TextView mId;
	  private EditText mCategory;
	  private TextView mTitleText;
	  private EditText mDescText;
	  private EditText mParent;
	  private ImageView mImage;
	  private ImageLoader imgLoader;
	  
	  private Map<String,Long> categories_map;
	  private Spinner mSpinner;
	  List<String> list = new ArrayList<String>();

	  private Uri todoUri;

	  @Override
	  protected void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    setContentView(R.layout.image_edit);
	    mId = (TextView) findViewById(R.id.img_id);
	    mImage = (ImageView) findViewById(R.id.img);
	    mTitleText = (TextView) findViewById(R.id.edit_name);
	    mCategory = (EditText) findViewById(R.id.edit_category);
	    mDescText = (EditText) findViewById(R.id.edit_description);
	    mParent = (EditText) findViewById(R.id.edit_parent);
	    mSpinner = (Spinner) findViewById(R.id.parent_spinner);
	    Bundle extras = getIntent().getExtras();
	    imgLoader = new ImageLoader();
	    categories_map = new HashMap<String,Long>();
	    addItemsOnSpinner();
	    // Check from the saved Instance
	    todoUri = (bundle == null) ? null : (Uri) bundle.getParcelable(ImageContract.CONTENT_ITEM_TYPE);

	    // Or passed from the other activity
	    if (extras != null) {
	      todoUri = extras.getParcelable(ImageContract.CONTENT_ITEM_TYPE);

	      fillData(todoUri);
	    }

	  }
	  
	  private void addItemsOnSpinner(){
		  String[] projection = {ImageContract.Columns._ID, ImageContract.Columns.CATEGORY};
		  String selection = ImageContract.Columns.CATEGORY +" IS NOT NULL";
		  Cursor c = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null,null);
		  c.moveToFirst();
		  while(!c.isAfterLast()){
				categories_map.put(c.getString(1), c.getLong(0));
				c.moveToNext();
		  }
		  c.close();

		  list.addAll(categories_map.keySet());
		  
		  ArrayAdapter<String> spinnerDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item){
			  @Override
			  public View getView(int position, View converView, ViewGroup parent){
				  View v = super.getView(position, converView, parent);
				  if(position == getCount()){
					  ((TextView)v.findViewById(android.R.id.text1)).setText("");
			          ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
				  }
				  return v;
			  }
			  
			  @Override
			  public int getCount(){
				  return super.getCount()-1;
			  }
		  };
		  
		  spinnerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		  spinnerDataAdapter.addAll(list);
		  spinnerDataAdapter.add("Wybierz kategoriê");
		  mSpinner.setAdapter(spinnerDataAdapter);
		  
		  
	  }
	  
	  
	  private void fillData(Uri uri) {
	    String[] projection = {ImageContract.Columns._ID,ImageContract.Columns.PATH, ImageContract.Columns.DESC, ImageContract.Columns.CATEGORY, ImageContract.Columns.PARENT};
	    Cursor cursor = getContentResolver().query(uri, projection, null, null,null);
	    if (cursor != null) {
	    	Log.i("imageDetail",String.valueOf(cursor.getCount()));
	      cursor.moveToFirst();

	      	  String img_id = cursor.getString(cursor.getColumnIndex(ImageContract.Columns._ID));
	      	  Long parent_fk = cursor.getLong(cursor.getColumnIndexOrThrow(ImageContract.Columns.PARENT));
	      	  String category = cursor.getString(cursor.getColumnIndexOrThrow(ImageContract.Columns.CATEGORY));
	      	  mId.setText(img_id);
		     
		      mCategory.setText(category);
		      mParent.setText(String.valueOf(parent_fk));
		      if(categories_map.containsValue(Long.valueOf(mParent.getText().toString()))){
		    	  String categoryName =   Utils.getKeyByValue(categories_map, parent_fk);
		    	  mSpinner.setSelection(list.indexOf(categoryName));
			      
		      }else
		    	  mSpinner.setSelection(list.indexOf(""));
		      
		      String imgName = cursor.getString(cursor.getColumnIndexOrThrow(ImageContract.Columns.PATH));
		      mTitleText.setText(imgName);
		      mDescText.setText(cursor.getString(cursor.getColumnIndexOrThrow(ImageContract.Columns.DESC)));
		      
		      // Always close the cursor
		      cursor.close();
			  imgLoader.loadBitmap(Storage.getThumbsMaxDir()+File.separator+imgName, mImage);			 
	    }
	  }


	  
	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    saveState();
	    outState.putParcelable(ImageContract.CONTENT_ITEM_TYPE, todoUri);
	  }

	  public void onClick(View view){
			switch(view.getId()){
				case R.id.submit_button:
			        if (TextUtils.isEmpty(mTitleText.getText().toString())) {
				          makeToast();
				        } else {
				          setResult(RESULT_OK);
				          finish();
				        }			
			}
	  }
	  
	  @Override
	  protected void onPause() {
	    super.onPause();
	    saveState();
	  }

	  private void saveState() {
	    String category = mCategory.getText().toString();
	    String summary = mTitleText.getText().toString();
	    String description = mDescText.getText().toString();
	    String parent_fk = String.valueOf(-1);
	    
	    if(mSpinner.getSelectedItemPosition()!=-1){
	    	String key = mSpinner.getSelectedItem().toString();
		    if(categories_map.containsKey(key))
		    	parent_fk = String.valueOf(categories_map.get(key));
	    }
	    

	    // Only save if either summary or description
	    // is available

	    if (description.length() == 0 && summary.length() == 0) {
	      return;
	    }

	    ContentValues values = new ContentValues();
	    values.put(ImageContract.Columns.CATEGORY, category);
	    values.put(ImageContract.Columns.PATH, summary);
	    values.put(ImageContract.Columns.DESC, description);
	    values.put(ImageContract.Columns.PARENT, parent_fk);

	    if (todoUri == null) {
	      // New todo
	      todoUri = getContentResolver().insert(ImageContract.CONTENT_URI, values);
	    } else {
	      // Update todo
	      getContentResolver().update(todoUri, values, null, null);
	    }
	  }

	  private void makeToast() {
	    Toast.makeText(ImageDetailActivity.this, "Please maintain a summary",
	        Toast.LENGTH_LONG).show();
	  }
	  
	  


}
