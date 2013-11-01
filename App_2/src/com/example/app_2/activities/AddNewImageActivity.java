package com.example.app_2.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

// za³adowawnie pustego ImageDetailsFragment
public class AddNewImageActivity extends Activity implements OnClickListener {
	private TextView mId;
	private EditText mCategory;
	private TextView mTitleText;
	private EditText mDescText;
	private EditText mParent;
	private Button mButton;
	public ImageView mImage;
	private static Bitmap bitmap;
	private CheckBox mParentCheckBox;
	private CheckBox mCreateCategoryCheckBox;

	private String newFileName;

	private Map<String, Long> categories_map;
	private Spinner mSpinner;
	List<String> list = new ArrayList<String>();

	private Uri imageUri;
	private static Long row_id;
	public static final int TAKE_PIC_REQUEST = 2;

	public static final String TAG = "AddNewImageActivity";
	ImageDetailsFragment details;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id");
		//imageUri = Uri.parse(ImageContract.CONTENT_URI + "/" + row_id);

		
		setContentView(R.layout.image_details);
		// During initial setup, plug in the details fragment.

		mId = (TextView) findViewById(R.id.img_id);
		mImage = (ImageView) findViewById(R.id.img);
		mImage.setOnClickListener(this);
		mButton = (Button) findViewById(R.id.submit_button);
		mTitleText = (TextView) findViewById(R.id.edit_name);
		mCategory = (EditText) findViewById(R.id.edit_category);
		mDescText = (EditText) findViewById(R.id.edit_description);
		mParent = (EditText) findViewById(R.id.edit_parent);
		//mSpinner = (Spinner) findViewById(R.id.parent_spinner);
		

		
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
		//mParentCheckBox = (CheckBox) findViewById(R.id.add_to_category);
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

		mCreateCategoryCheckBox = (CheckBox) findViewById(R.id.create_category);
		mCreateCategoryCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked)
							mCategory.setVisibility(View.VISIBLE);
						else
							mCategory.setVisibility(View.INVISIBLE);
					}
				});

		addItemsOnSpinner();
		mImage.setClickable(true);
		
		if(bitmap !=null){
			mImage.setImageBitmap(bitmap);
			showFields();
		}
		
		row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id"); //je¿eli ju¿ zapisano w bazie row_id != null, przywracam to co jest zapisane 
		imageUri = Uri.parse(ImageContract.CONTENT_URI + "/" + row_id);
		if(row_id!=null && row_id!=0){
			fillData(imageUri);
			showFields();
		}else
		{
			hideFields();
		}
		

	}

	private void showFields(){
		mId.setVisibility(View.VISIBLE);
		mTitleText.setVisibility(View.VISIBLE);
		//mCategory.setVisibility(View.VISIBLE);
		mDescText.setVisibility(View.VISIBLE);
		//mParent.setVisibility(View.VISIBLE);
		mSpinner.setVisibility(View.VISIBLE);
		mButton.setVisibility(View.VISIBLE);
		mParentCheckBox.setVisibility(View.VISIBLE);
		mCreateCategoryCheckBox.setVisibility(View.VISIBLE);
	}
	
	private void hideFields(){
		mId.setVisibility(View.GONE);
		mTitleText.setVisibility(View.GONE);
		mCategory.setVisibility(View.GONE);
		mDescText.setVisibility(View.GONE);
		mParent.setVisibility(View.GONE);
		mSpinner.setVisibility(View.GONE);
		mButton.setVisibility(View.GONE);
		mParentCheckBox.setVisibility(View.GONE);
		mCreateCategoryCheckBox.setVisibility(View.GONE);
	}
	
	
	private boolean verifyResolves(Intent intent) {
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				intent, PackageManager.PERMISSION_GRANTED);
		return activities.size() > 0;
	}

	private void addItemsOnSpinner() {
		categories_map = new HashMap<String, Long>();
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.CATEGORY };
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		Cursor c = this.getContentResolver().query(ImageContract.CONTENT_URI,
				projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			categories_map.put(c.getString(1), c.getLong(0));
			c.moveToNext();
		}
		c.close();

		list.addAll(categories_map.keySet());
		ArrayAdapter<String> spinnerDataAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item) {
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
		for (String l : list)
			spinnerDataAdapter.add(l);
		mSpinner.setAdapter(spinnerDataAdapter);
	}

	@Override
	public void onClick(View v) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (verifyResolves(takePictureIntent)) {
			File f = Storage.createTempImageFile(); // tworzy tymczasowy plik
			String mCurrentPhotoPath = f.getAbsolutePath();
			Storage.saveToPreferences(mCurrentPhotoPath, "photoPath", this,
					Activity.MODE_PRIVATE);
			//Storage.galleryAddPic(this, mCurrentPhotoPath);
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(takePictureIntent, TAKE_PIC_REQUEST);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case TAKE_PIC_REQUEST:
				String path_toIMG = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG,	mImage.getWidth(), mImage.getHeight());
				mImage.setImageBitmap(bitmap);
				this.newFileName = new File(path_toIMG).getName();
				mTitleText.setText(this.newFileName);
				showFields();
				saveState();
				fillData(imageUri);
			default:
				break;

			}
		}
	}
	
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		final String photo_path = Storage.readFromPreferences(null, "photoPath", this, Activity.MODE_PRIVATE);
		if(mButton.getVisibility()==View.VISIBLE){
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	new AddingImageTask().execute(photo_path); // skalowanie obrazka, dodanie do 2 folderów
						saveState();
						bitmap=null;
						finish();
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
						if(imageUri!=null && !imageUri.getLastPathSegment().equals("null")){  // je¿eli ju¿ jest w bazie i nie zapisujemy
							getContentResolver().delete(imageUri, null, null);
							finish();
						}
			            break;
			        }
			    }
			};
			AlertDialog.Builder ad = new AlertDialog.Builder(this);
	        ad.setMessage("Zapisaæ zmiany?");
	        ad.setNegativeButton(android.R.string.no, dialogClickListener);
	        ad.setPositiveButton(android.R.string.yes,dialogClickListener); 
	        ad.create().show();
		}else
		{
			this.finish();
			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);  
		}


	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.submit_button:
			String path_toIMG = Storage.readFromPreferences(null, "photoPath", this, Activity.MODE_PRIVATE);
			new AddingImageTask().execute(path_toIMG); // skalowanie obrazka, dodanie do 2 folderów
			saveState();
			bitmap=null;
			this.finish();
			break;

		default:
			break;
		}
		
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

		if (mParentCheckBox.isChecked()) {
			if (mSpinner.getSelectedItemPosition() != Spinner.INVALID_POSITION) {
				String key = mSpinner.getSelectedItem().toString();
				if (categories_map.containsKey(key))
					parent_fk = String.valueOf(categories_map.get(key));
			}
		}

		String description = mDescText.getText().toString();

		ContentValues values = new ContentValues();
		values.put(ImageContract.Columns.CATEGORY, category);
		values.put(ImageContract.Columns.DESC, description);
		values.put(ImageContract.Columns.PATH, this.newFileName);
		
		if (imageUri.getLastPathSegment().equals("null")){
			imageUri = this.getContentResolver().insert(ImageContract.CONTENT_URI, values);
		} else {
			this.getContentResolver().update(imageUri, values, null,null);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (!imageUri.getLastPathSegment().equals("null")) {
			outState.putLong("row_id",	Long.valueOf(imageUri.getLastPathSegment()));
		}
		if(bitmap != null)
			saveState();
		
		// outState.putParcelable(ImageContract.CONTENT_ITEM_TYPE, imageUri);
	}
	private void fillData(Uri uri) {
		//Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.PATH, ImageContract.Columns.DESC,
				ImageContract.Columns.CATEGORY};
		Cursor cursor = getContentResolver().query(uri,	projection, null, null, null);
		cursor.moveToFirst();
		if ( cursor.isAfterLast()) {
			Log.i("imageDetail", String.valueOf(cursor.getCount()));


			String img_id = cursor.getString(cursor
					.getColumnIndex(ImageContract.Columns._ID));
			//Long parent_fk = cursor.getLong(cursor
			//		.getColumnIndexOrThrow(ImageContract.Columns.PARENTS));
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
			mParent.setText(String.valueOf(parent_fk));
			*/
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
			//ImageLoader.loadBitmap(Storage.getThumbsMaxDir() + File.separator	+ imgName, mImage);
		}
	}
}
