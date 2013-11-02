package com.example.app_2.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
//import android.widget.ArrayAdapter;

public class ImageDetailsFragment extends Fragment{
	private TextView mId;
	private EditText mCategory;
	private TextView mTitleText;
	private EditText mDescText;
	private EditText mParent;
	private TextView mParents;
	private Button mButton;
	public  ImageView mImage;
	private static Bitmap bitmap;
	//private CheckBox mParentCheckBox;
	private CheckBox mCreateCategoryCheckBox;
	private String newFileName;

	private Map<String, Long> categories_map;
	private Spinner mSpinner;
	List<String> list = new ArrayList<String>();

	private Uri imageUri;
	private static Long row_id;
	public static final int TAKE_PIC_REQUEST = 2;
	private static Activity executing_activity;


	/**
	 * Create a new instance of DetailsFragment, initialized to show the text at
	 * 'index'.
	 */

	

	public static ImageDetailsFragment newInstance(Long id) {
		ImageDetailsFragment f = new ImageDetailsFragment();
		Bundle args = new Bundle();
		args.putLong("row_id", id);
		row_id = id;
		f.setArguments(args);
		return f;

	}

	public Long getShownId() {
		return getArguments().getLong("row_id", -1);
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		executing_activity=getActivity();
		// if(row_id == null)
		row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id");
		if (row_id == null) {
			if (this.getArguments() != null)
				row_id = (Long) getArguments().get("row_id");
		}

		imageUri = Uri.parse(ImageContract.CONTENT_URI + "/" + row_id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		executing_activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.

		}
		final View view = inflater.inflate(R.layout.image_details, container,false);
		mId = (TextView) view.findViewById(R.id.img_id);
		mImage = (ImageView) view.findViewById(R.id.img);
		//mImage.setOnClickListener(this);
		mButton = (Button) view.findViewById(R.id.submit_button);
		mTitleText = (TextView) view.findViewById(R.id.edit_name);
		mCategory = (EditText) view.findViewById(R.id.edit_category);
		mDescText = (EditText) view.findViewById(R.id.edit_description);
		mParent = (EditText) view.findViewById(R.id.edit_parent);
		mParents = (TextView) view.findViewById(R.id.parents);

		/*
		mSpinner = (Spinner) view.findViewById(R.id.parent_spinner);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				String key = mSpinner.getSelectedItem().toString();
				if (categories_map.containsKey(key))
					mParent.setText(String.valueOf(categories_map.get(key)));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				mParent.setText(-1);
			}

		});
		
		//mParentCheckBox = (CheckBox) view.findViewById(R.id.add_to_category);
		mParentCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked)
							mSpinner.setVisibility(View.VISIBLE);
						else {
							mSpinner.setVisibility(View.GONE);
							mParent.setText(String.valueOf(-1));
						}

					}
				});
		 */
		mCreateCategoryCheckBox = (CheckBox) view
				.findViewById(R.id.create_category);
		mCreateCategoryCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked){
							String image_description = mDescText.getText().toString();
							mCategory.setText(image_description);
							mCategory.setVisibility(View.VISIBLE);
							}
						else
							mCategory.setVisibility(View.INVISIBLE);
					}
				});

		
		
		categories_map = new HashMap<String, Long>();
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.CATEGORY };
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		Cursor c = executing_activity.getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			categories_map.put(c.getString(1), c.getLong(0));
			c.moveToNext();
		}
		c.close();
		
		

		
		if (row_id != null && row_id != 0){
			fillData(row_id);
			//mImage.setClickable(false);
			}
		else{
			//mImage.setClickable(true);
			if(bitmap!=null)
				mImage.setImageBitmap(bitmap);
		}
		return view;

	}

	private void setImageParents(Long id){
		Uri imageParentsUri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+id);
		String[] projection = { "i."+ImageContract.Columns._ID, "i."+ImageContract.Columns.CATEGORY };
		Cursor c = executing_activity.getContentResolver().query(imageParentsUri, projection, null, null, null);
		c.moveToFirst();
		mParents.setText("");
		while (!c.isAfterLast()) {
			mParents.append((c.getString(1)+" "+ c.getLong(0))+"\n");
			c.moveToNext();
		}
		c.close();
	}
	
	private void addItemsOnSpinner() {
		categories_map = new HashMap<String, Long>();
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.CATEGORY };
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		Cursor c = executing_activity.getContentResolver().query(
				ImageContract.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			categories_map.put(c.getString(1), c.getLong(0));
			c.moveToNext();
		}
		c.close();

		list.addAll(categories_map.keySet());
		ArrayAdapter<String> spinnerDataAdapter = new ArrayAdapter<String>(
				executing_activity, android.R.layout.simple_spinner_dropdown_item) {
			@Override
			public View getView(int position, View converView, ViewGroup parent) {
				View v = super.getView(position, converView, parent);
				if (position == getCount()) {
					((TextView) v.findViewById(android.R.id.text1))
							.setText("Select category");
					((TextView) v.findViewById(android.R.id.text1))
							.setHint(getItem(getCount())); // "Hint to be displayed"
				}
				return v;
			}
		};

		spinnerDataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(String l:list)
			spinnerDataAdapter.add(l);
		mSpinner.setAdapter(spinnerDataAdapter);
	}
	
	public static void addParents(long[] ids){
		if(row_id != null){
			Long alreadyHaveParensIds[];
			List<Long> alreadyBindParentsIds = new ArrayList<Long>();
			Uri imageParentsUri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+row_id);
			String[] projection = { "i."+ImageContract.Columns._ID};
			Cursor c = executing_activity.getContentResolver().query(imageParentsUri, projection, null, null, null);
			c.moveToFirst();

			while(!c.isAfterLast()){
				alreadyBindParentsIds.add(c.getLong(0));
				c.moveToNext();
			}
			
			ContentValues cv = new ContentValues();
			for(Long id : ids){
				if(!alreadyBindParentsIds.contains(id)){
					cv.put(ParentContract.Columns.IMAGE_FK, row_id);
					cv.put(ParentContract.Columns.PARENT_FK, id);
					executing_activity.getContentResolver().insert(ParentContract.CONTENT_URI, cv);
				}

			}
		}
		
	}
	
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.submit_button:
			saveState();
			break;
		case R.id.cancel_button:
			break;
		default:
			break;
		}


		if(executing_activity instanceof ImageDetailsActivity)
			executing_activity.finish();
	}
	
	private void fillData(Long id) {
		Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.PATH, ImageContract.Columns.DESC,
				ImageContract.Columns.CATEGORY,/* ImageContract.Columns.PARENTS */ };
		Cursor cursor = executing_activity.getContentResolver().query(uri,	projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();

			String img_id = cursor.getString(cursor
					.getColumnIndex(ImageContract.Columns._ID));
			/*Long parent_fk = cursor.getLong(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.PARENTS));
					*/
			String category = cursor.getString(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.CATEGORY));

			mId.setText(img_id);
/*
			if (parent_fk != Long.valueOf(-1)) {
				if (categories_map.containsValue(parent_fk)) { // TODO
					mParentCheckBox.setChecked(true);
					String categoryName = Utils.getKeyByValue(categories_map,
							parent_fk);
					mSpinner.setSelection(list.indexOf(categoryName));
				}
			}
			*/
//			mParent.setText(String.valueOf(parent_fk));

			if (category != null)
				if (!category.equals("")) {
					mCreateCategoryCheckBox.setChecked(true);
					mCategory.setText(category);
				}

			String imgName = cursor.getString(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.PATH));
			mTitleText.setText(imgName);
			mDescText.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.DESC)));

			// Always close the cursor
			cursor.close();
			ImageLoader.loadBitmap(Storage.getThumbsMaxDir() + File.separator
					+ imgName, mImage, false);
			
			setImageParents(id);
		}
		
	}

	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (!imageUri.getLastPathSegment().equals("null")) {
			outState.putLong("row_id",
					Long.valueOf(imageUri.getLastPathSegment()));
			saveState();
		}

		// outState.putParcelable(ImageContract.CONTENT_ITEM_TYPE, imageUri);
	}

	@Override
	public void onPause() {
		super.onPause();
		//saveState();
	}

	private void saveState() {
		String category = null;
		String parent_fk = String.valueOf(-1);

		if (mCreateCategoryCheckBox.isChecked()) {
			category = mCategory.getText().toString();
			if (category.equals("")) {
				category = null;
			}
		}
		/*
		if (mParentCheckBox.isChecked()) {
			if (mSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
				String key = mSpinner.getSelectedItem().toString();
				if (categories_map.containsKey(key))
					parent_fk = String.valueOf(categories_map.get(key));
			}
		}
	*/
		String description = mDescText.getText().toString();

		// TODO save if data changed or save button pressed

		//if (description.length() == 0) {
		//	return;
		//}

		ContentValues values = new ContentValues();
		values.put(ImageContract.Columns.CATEGORY, category);
		values.put(ImageContract.Columns.DESC, description);
		//values.put(ImageContract.Columns.PARENTS, parent_fk);

		if (row_id == null && this.newFileName!=null) {
			values.put(ImageContract.Columns.PATH, this.newFileName);
			imageUri = executing_activity.getContentResolver().insert(
					ImageContract.CONTENT_URI, values);
		} else {
			executing_activity.getContentResolver().update(imageUri, values, null,
					null);
		}
	}
}
