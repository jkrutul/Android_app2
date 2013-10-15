package com.example.app_2.fragments;

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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.examples.app_2.activities.ImageDetailActivity;

public class ImageDetailsFragment extends Fragment{
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
	  private static Long row_id;
	  private Activity exec_atv;
	  
	  /**
	     * Create a new instance of DetailsFragment, initialized to
	     * show the text at 'index'.
	     */
	    public static ImageDetailsFragment newInstance(int index) {
	    	ImageDetailsFragment f = new ImageDetailsFragment();

	        // Supply index input as an argument.
	        Bundle args = new Bundle();
	        args.putInt("index", index);
	        f.setArguments(args);

	        return f;
	    }
	    
	    public static ImageDetailsFragment newInstance(Long id){
	    	ImageDetailsFragment f = new ImageDetailsFragment();
	    	Bundle args = new Bundle();
	    	args.putLong("row_id", id);
	    	row_id = id;
	    	f.setArguments(args);
	    	return f;
	    	
	    }
	    public int getShownIndex() {
	        return getArguments().getInt("index", 0);
	    }

	public void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    /*
	    View view = getView();
	   // exec_atv.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	    
	    //setContentView(R.layout.image_edit);
	    mId = (TextView) view.findViewById(R.id.img_id);
	    mImage = (ImageView) view.findViewById(R.id.img);
	    mTitleText = (TextView) view.findViewById(R.id.edit_name);
	    mCategory = (EditText) view.findViewById(R.id.edit_category);
	    mDescText = (EditText) view.findViewById(R.id.edit_description);
	    mParent = (EditText) view.findViewById(R.id.edit_parent);
	    mSpinner = (Spinner) view.findViewById(R.id.parent_spinner);
	    //Bundle extras = exec_atv.getIntent().getExtras();

	    categories_map = new HashMap<String,Long>();
	    addItemsOnSpinner();
	    */
	    // Check from the saved Instance
	    
	    //todoUri = (bundle == null) ? null : (Uri) bundle.getParcelable(ImageContract.CONTENT_ITEM_TYPE);
	    //row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id");
	    //fillData(row_id);
	    // Or passed from the other activity
	    /*
	    if (extras != null) {
	      todoUri = extras.getParcelable(ImageContract.CONTENT_ITEM_TYPE);

	      fillData(todoUri);
	    }
	    */

	  }
	  
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		  super.onCreateView(inflater, container, savedInstanceState);
		  if(container == null){
	            // We have different layouts, and in one of them this
	            // fragment's containing frame doesn't exist.  The fragment
	            // may still be created from its saved state, but there is
	            // no reason to try to create its view hierarchy because it
	            // won't be displayed.  Note this is not needed -- we could
	            // just run the code below, where we would create and return
	            // the view hierarchy; it would just never be used.

		  }
	    	final View view = inflater.inflate(R.layout.image_edit, container,false);
	    	 mId = (TextView) view.findViewById(R.id.img_id);
	 	    mImage = (ImageView) view.findViewById(R.id.img);
	 	    mTitleText = (TextView) view.findViewById(R.id.edit_name);
	 	    mCategory = (EditText) view.findViewById(R.id.edit_category);
	 	    mDescText = (EditText) view.findViewById(R.id.edit_description);
	 	    mParent = (EditText) view.findViewById(R.id.edit_parent);
	 	    mSpinner = (Spinner) view.findViewById(R.id.parent_spinner);
	 	    categories_map = new HashMap<String,Long>();
		    addItemsOnSpinner();
		    fillData(row_id);
			return view;
		  
	  }
	  
	  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void addItemsOnSpinner(){
		  String[] projection = {ImageContract.Columns._ID, ImageContract.Columns.CATEGORY};
		  String selection = ImageContract.Columns.CATEGORY +" IS NOT NULL";
		  Cursor c = getActivity().getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null,null);
		  c.moveToFirst();
		  while(!c.isAfterLast()){
				categories_map.put(c.getString(1), c.getLong(0));
				c.moveToNext();
		  }
		  c.close();

		  list.addAll(categories_map.keySet());
		  
		  ArrayAdapter<String> spinnerDataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item){
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
	  private void fillData(Long id) {
		  	Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		    String[] projection = {ImageContract.Columns._ID,ImageContract.Columns.PATH, ImageContract.Columns.DESC, ImageContract.Columns.CATEGORY, ImageContract.Columns.PARENT};
		    Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null,null);
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
				  ImageLoader.loadBitmap(Storage.getThumbsMaxDir()+File.separator+imgName, mImage);			 
		    }
		  }
	  
	  private void fillData(Uri uri) {
		    String[] projection = {ImageContract.Columns._ID,ImageContract.Columns.PATH, ImageContract.Columns.DESC, ImageContract.Columns.CATEGORY, ImageContract.Columns.PARENT};
		    Cursor cursor = exec_atv.getContentResolver().query(uri, projection, null, null,null);
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
				  ImageLoader.loadBitmap(Storage.getThumbsMaxDir()+File.separator+imgName, mImage);			 
		    }
		  }
	  
	  
	  public void onSaveInstanceState(Bundle outState) {
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
					          exec_atv.setResult(exec_atv.RESULT_OK);
					          //finish();
					        }			
				}
		  }
		  
		  @Override
		public void onPause() {
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
		      todoUri = getActivity().getContentResolver().insert(ImageContract.CONTENT_URI, values);
		    } else {
		      // Update todo
		    	getActivity().getContentResolver().update(todoUri, values, null, null);
		    }
		  }

		  private void makeToast() {
		    Toast.makeText(getActivity(), "Please maintain a summary",
		        Toast.LENGTH_LONG).show();
		  }
		  
}
