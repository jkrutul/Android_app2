package com.example.app_2.activities;

import java.io.File;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;

public class AddUserActivity extends Activity {
	EditText mUserName;
	TextView mHintText;
	RadioButton mMaleRB, mFemaleRB;
	private Button mSubmit_button;
	private ImageView mUserImage;
	private final int FILE_SELECT_CODE = 12;
	private static final int CROPPED_PHOTO = 48;
	private static final int TAKE_PIC_REQUEST = 24;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adduser);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mUserName = (EditText) findViewById(R.id.user_name);
		mMaleRB = (RadioButton) findViewById(R.id.radioMale);
		mUserImage = (ImageView) findViewById(R.id.user_image);
		mSubmit_button = (Button) findViewById(R.id.adduser_button);
		mHintText = (TextView) findViewById(R.id.user_img_hint);

		mUserName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == 0) {
					mSubmit_button.setEnabled(false);
				} else
					mSubmit_button.setEnabled(true);
			}
		});
	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.adduser_button:
			String username = mUserName.getText().toString();
			String path_toIMG = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
			String filename = Utils.getFilenameFromPath(path_toIMG);
			if(path_toIMG != null){
				AddingImageTask ait= new AddingImageTask();
				ait.execute(path_toIMG);
			}	
			int ismale = mMaleRB.isChecked() == true ? 1 : 0;
			// String[] projection = { ImageContract.Columns._ID,
			// ImageContract.Columns.CATEGORY };

			ContentValues img_val = new ContentValues();
			img_val.put(ImageContract.Columns.PATH, filename);
			img_val.put(ImageContract.Columns.CATEGORY, username + " - G³ówna");
			// img_val.put(ImageContract.Columns.PATH, )
			Uri img_uri = getContentResolver().insert(
					ImageContract.CONTENT_URI, img_val);

			Long user_root_fk = Long.valueOf(img_uri.getLastPathSegment());
			ContentValues user_val = new ContentValues();
			user_val.put(UserContract.Columns.USERNAME, username);
			user_val.put(UserContract.Columns.ISMALE, ismale);
			user_val.put(UserContract.Columns.IMG_FILENAME, filename);
			user_val.put(UserContract.Columns.ROOT_FK, user_root_fk);
			Uri user_uri = getContentResolver().insert(UserContract.CONTENT_URI, user_val);

			img_val.put(ImageContract.Columns.AUTHOR_FK,user_uri.getLastPathSegment()); // ustawienie autora korzenia
			getContentResolver().update(img_uri, img_val, null, null);

			finish();
			break;

		case R.id.cancel_adduser_button:

			finish();
			break;

		case R.id.user_image:
			final Activity a = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);	// Add the buttons
			builder.setPositiveButton("zrób zdjêcie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							cameraIntent();
						}
					});
			builder.setNegativeButton("wybierz obrazek",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							selectImageIntent();
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
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
			}
			break;
		case TAKE_PIC_REQUEST:
			if (resultCode == RESULT_OK) {
				String path_toIMG = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
				ImageLoader.loadBitmap(path_toIMG, mUserImage, true);
				mHintText.setVisibility(View.INVISIBLE);
				//Bitmap bitmap = BitmapCalc.decodeSampleBitmapFromFile(path_toIMG, mUserImage.getWidth(), mUserImage.getHeight());
				Toast.makeText(this, path_toIMG, Toast.LENGTH_LONG).show();
				//BitmapDrawable drawable = new BitmapDrawable(bitmap);
				//mUserImage.setBackgroundDrawable(drawable);
			}
			break;

		}
		//super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void selectImageIntent(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		File f = Storage.createTempImageFile(); // tworzy tymczasowy plik
		String mCurrentPhotoPath = f.getAbsolutePath();
		Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", this, Activity.MODE_PRIVATE);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("aspectX",1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
		try {
			startActivityForResult(Intent.createChooser(intent, "Wybierz obrazek"),	FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getApplicationContext(),"Proszê zainstalowaæ menad¿er plików", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void cameraIntent(){
		Intent camera = new Intent(	MediaStore.ACTION_IMAGE_CAPTURE);
		if (Utils.verifyResolves(camera)) {
			File f = Storage.createTempImageFile(); // tworzy tymczasowy plik
			String mCurrentPhotoPath = f.getAbsolutePath();
			Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", this, Activity.MODE_PRIVATE);
			camera.putExtra("crop", "true");
			camera.putExtra("outputX", 150);
			camera.putExtra("outputY", 150);
			camera.putExtra("aspectX",1);
			camera.putExtra("aspectY", 1);
			camera.putExtra("scale", true);
			camera.putExtra("return-data", true);
			camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			camera.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
			try{
			startActivityForResult(camera, TAKE_PIC_REQUEST);
			}catch (android.content.ActivityNotFoundException ex){
				Toast.makeText(getApplicationContext(),"Brak aparatu???", Toast.LENGTH_SHORT).show();
			}
		}		
	}

}
