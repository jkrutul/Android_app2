package com.example.app_2.fragments;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.intents.ImageIntents;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
//import android.widget.ArrayAdapter;
import com.example.app_2.utils.Utils;

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
	private CheckBox mCreateCategoryCheckBox;
	private String pathToNewImage;
	private String filename;

	private Map<String, Long> categories_map;
	List<String> list = new ArrayList<String>();

	private Uri imageUri;
	private static Long row_id;
	public static final int TAKE_PIC_REQUEST = 2;
	public static final int FILE_SELECT_REQUEST = 3;
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
		if (container == null) {}
		
		final View view = inflater.inflate(R.layout.image_details, container,false);
		mId = (TextView) view.findViewById(R.id.img_id);
		mImage = (ImageView) view.findViewById(R.id.img);
		mButton = (Button) view.findViewById(R.id.submit_button);
		mTitleText = (TextView) view.findViewById(R.id.edit_name);
		mCategory = (EditText) view.findViewById(R.id.edit_category);
		mDescText = (EditText) view.findViewById(R.id.edit_description);
		mParent = (EditText) view.findViewById(R.id.edit_parent);
		mParents = (TextView) view.findViewById(R.id.parents);

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
	
	public void setParentsView(long ids[]){
		mParents.setText("");
		String parents = new String();
		for(Long l : ids)
			if(categories_map.containsKey(l))
				parents +=categories_map.get(l)+"\n";
		mParents.setText(parents);	
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
	
	public static void addParents(long[] ids){
		if(row_id != null){
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
	
	public static void deleteParents(long[] ids){
		if(row_id != null){
			List<Long> alreadyBindParentsIds = new ArrayList<Long>();
			Uri imageParentsUri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+row_id);
			String[] projection = { "i."+ImageContract.Columns._ID};
			Cursor c = executing_activity.getContentResolver().query(imageParentsUri, projection, null, null, null);
			c.moveToFirst();

			while(!c.isAfterLast()){
				alreadyBindParentsIds.add(c.getLong(0));
				c.moveToNext();
			}
			
			for(Long id : ids){
				if(alreadyBindParentsIds.contains(id)){
					String[] selectionArgs ={ String.valueOf(row_id), String.valueOf(id) };
					executing_activity.getContentResolver().delete(ParentContract.CONTENT_URI, ParentContract.Columns.IMAGE_FK +" =? AND "+ ParentContract.Columns.PARENT_FK+ "=? ",selectionArgs );
				}

			}
		}
	}
	
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.submit_button:
			saveState();
			break;
		case R.id.cancel_button: // TODO if dual pane
			break;
			
		case R.id.img:
			final Activity a = getActivity();
			final Fragment f = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(a);	// Add the buttons
			builder.setPositiveButton("zrób zdjêcie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							ImageIntents.cameraIntent(a, f, TAKE_PIC_REQUEST);
						}
					});
			builder.setNegativeButton("wybierz obrazek",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a, f , FILE_SELECT_REQUEST);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
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
				ImageContract.Columns.CATEGORY};
		Cursor cursor = executing_activity.getContentResolver().query(uri,	projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();

			String img_id = cursor.getString(cursor
					.getColumnIndex(ImageContract.Columns._ID));
			String category = cursor.getString(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.CATEGORY));

			mId.setText(img_id);

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

			cursor.close();
			
			ImageLoader.loadBitmap(Storage.getPathToScaledBitmap(imgName, 300), mImage, false);
			
			setImageParents(id);
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if(requestCode == TAKE_PIC_REQUEST || requestCode == FILE_SELECT_REQUEST){
				Uri uri = data.getData();
				if(uri != null)
					pathToNewImage = Utils.getPath(getActivity(), uri);
				else
					pathToNewImage = Storage.readFromPreferences(null,"photoPath", getActivity(), Activity.MODE_PRIVATE);
				
				filename = Utils.getFilenameFromPath(pathToNewImage);
				String title = Utils.cutExtention(filename);
				mTitleText.setText(title);
				mDescText.setText(title);
				mCategory.setText(title);
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(pathToNewImage,	mImage.getWidth(), mImage.getHeight());
				mImage.setImageBitmap(bitmap);
			}
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

		if (mCreateCategoryCheckBox.isChecked()) {
			category = mCategory.getText().toString();
			if (category.equals("")) {
				category = null;
			}
		}

		String description = mDescText.getText().toString();

		ContentValues values = new ContentValues();
		values.put(ImageContract.Columns.CATEGORY, category);
		values.put(ImageContract.Columns.DESC, description);

		if (row_id == null && this.filename!=null) {
			values.put(ImageContract.Columns.PATH, this.filename);
			imageUri = executing_activity.getContentResolver().insert(ImageContract.CONTENT_URI, values);
		} else {
			executing_activity.getContentResolver().update(imageUri, values, null,	null);
		}
	}
}
