package com.example.app_2.activities;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.intents.ImageIntents;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

// za³adowawnie pustego ImageDetailsFragment
public class NewImgTemplateActivity extends Activity {
	private TextView mId;
	private EditText mCategory;
	private TextView mTitleText;
	private EditText mDescText;
	private EditText mParent;
	private Button mButton;
	public ImageView mImage;
	private static Bitmap bitmap;
	private CheckBox mCreateCategoryCheckBox;
	private String pathToNewImage;
	private String filename;

	private String newFileName;

	private Map<String, Long> categories_map;
	private Spinner mSpinner;
	List<String> list = new ArrayList<String>();

	private Uri imageUri;
	private static Long row_id;
	public static final int TAKE_PIC_REQUEST = 2;
	public static final int FILE_SELECT_REQUEST = 3;

	public static final String TAG = "NewImgTemplateActivity";
	ImageDetailsFragment details;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		//pathToNewImage = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_WORLD_READABLE);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.image_details);
		mId = (TextView) findViewById(R.id.img_id);
		mImage = (ImageView) findViewById(R.id.img);
		mButton = (Button) findViewById(R.id.submit_button);
		mTitleText = (TextView) findViewById(R.id.edit_name);
		mCategory = (EditText) findViewById(R.id.edit_category);
		mDescText = (EditText) findViewById(R.id.edit_description);
		mCreateCategoryCheckBox = (CheckBox) findViewById(R.id.create_category);
		
		mCreateCategoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked)
							mCategory.setVisibility(View.VISIBLE);
						else
							mCategory.setVisibility(View.INVISIBLE);
					}
				});


	}

	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if(requestCode == TAKE_PIC_REQUEST || requestCode == FILE_SELECT_REQUEST){
				Uri uri = data.getData();
				if(uri != null)
					pathToNewImage = Utils.getPath(this, uri);
				else
					pathToNewImage = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
				
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
	
	@Override
	public void onBackPressed() {
		final Activity a = this;
		final String photo_path = Storage.readFromPreferences(null, "photoPath", this, Activity.MODE_PRIVATE);
		if(mButton.getVisibility()==View.VISIBLE){
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	new AddingImageTask(a).execute(photo_path); // skalowanie obrazka, dodanie do 2 folderów
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
		case R.id.img:
			final Activity a = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);	// Add the buttons
			builder.setPositiveButton("zrób zdjêcie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.cameraIntent(a, TAKE_PIC_REQUEST, App_2.maxWidth, App_2.maxWidth);
						}
					});
			builder.setNegativeButton("wybierz obrazek",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a, FILE_SELECT_REQUEST, App_2.maxWidth, App_2.maxWidth);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
			break;
		case R.id.submit_button:
			String path_toIMG = Storage.readFromPreferences(null, "photoPath", this, Activity.MODE_PRIVATE);
			new AddingImageTask(this).execute(path_toIMG); // skalowanie obrazka, dodanie do 2 folderów
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
		String description = null;

		if (mCreateCategoryCheckBox.isChecked()) {
			category = mCategory.getText().toString();
			if (category.equals("")) {
				category = null;
			}
		}
		
		description = mDescText.getText().toString();

		ContentValues values = new ContentValues();
		values.put(ImageContract.Columns.CATEGORY, category);
		values.put(ImageContract.Columns.DESC, description);
		values.put(ImageContract.Columns.FILENAME, this.newFileName);
		
		if (imageUri.getLastPathSegment().equals("null")){
			imageUri = this.getContentResolver().insert(ImageContract.CONTENT_URI, values);
		} else {
			this.getContentResolver().update(imageUri, values, null,null);
		}
	}

	private void fillData(Uri uri) {
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.FILENAME, ImageContract.Columns.DESC,
				ImageContract.Columns.CATEGORY};
		Cursor cursor = getContentResolver().query(uri,	projection, null, null, null);
		cursor.moveToFirst();
		if ( cursor.isAfterLast()) {
			Log.i("imageDetail", String.valueOf(cursor.getCount()));


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
					.getColumnIndexOrThrow(ImageContract.Columns.FILENAME));
			mTitleText.setText(imgName);
			mDescText.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(ImageContract.Columns.DESC)));
			cursor.close();

		}
	}
}
