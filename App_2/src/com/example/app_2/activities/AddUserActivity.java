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
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.example.app_2.intents.ImageIntents;

public class AddUserActivity extends Activity {
	EditText mUserName;
	TextView mHintText;
	RadioButton mMaleRB, mFemaleRB;
	private Button mSubmit_button;
	private ImageView mUserImage;
	private final int FILE_SELECT_REQUEST = 12;
	private final int TAKE_PIC_REQUEST = 24;
	private String pathToNewImage;
	private Bitmap bitmap;

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
				if (s.length() == 0)
					mSubmit_button.setEnabled(false);
				else
					mSubmit_button.setEnabled(true);
			}
		});
	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.adduser_button:
			String username = mUserName.getText().toString();
			String filename = null;
			if (pathToNewImage != null) {
				filename = Utils.getFilenameFromPath(pathToNewImage);
				AddingImageTask ait = new AddingImageTask();
				ait.execute(pathToNewImage);
			}
			int ismale = mMaleRB.isChecked() == true ? 1 : 0;

			ContentValues img_val = new ContentValues();
			img_val.put(ImageContract.Columns.PATH, filename);
			img_val.put(ImageContract.Columns.CATEGORY, username + " - G��wna");
			Uri img_uri = getContentResolver().insert(
					ImageContract.CONTENT_URI, img_val);
			Long user_root_fk = Long.valueOf(img_uri.getLastPathSegment());
			ContentValues user_val = new ContentValues();
			user_val.put(UserContract.Columns.USERNAME, username);
			user_val.put(UserContract.Columns.ISMALE, ismale);
			user_val.put(UserContract.Columns.IMG_FILENAME, filename);
			user_val.put(UserContract.Columns.ROOT_FK, user_root_fk);
			Uri user_uri = getContentResolver().insert(
					UserContract.CONTENT_URI, user_val);

			img_val.put(ImageContract.Columns.AUTHOR_FK,
					user_uri.getLastPathSegment()); // ustawienie autora
													// korzenia
			getContentResolver().update(img_uri, img_val, null, null);
			Storage.saveToPreferences(null, "photoPath", this,
					Activity.MODE_PRIVATE); // clear preferences
			finish();
			break;

		case R.id.cancel_adduser_button:
			Storage.saveToPreferences(null, "photoPath", this,
					Activity.MODE_PRIVATE); // clear preferences
			finish();
			break;

		case R.id.user_image:
			final Activity a = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this); // Add
																			// the
																			// buttons
			builder.setPositiveButton("zr�b zdj�cie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.cameraIntent(a, TAKE_PIC_REQUEST);
						}
					});
			builder.setNegativeButton("wybierz obrazek",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a,
									FILE_SELECT_REQUEST);
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if(requestCode == TAKE_PIC_REQUEST || requestCode == FILE_SELECT_REQUEST){
				Uri uri = data.getData();
				if(uri != null)
					pathToNewImage = Utils.getPath(this, uri);
				else
					pathToNewImage = Storage.readFromPreferences(null,"photoPath", this, Activity.MODE_PRIVATE);
				
				mHintText.setVisibility(View.INVISIBLE);
				int w, h;
				w = mUserImage.getWidth();
				h = mUserImage.getHeight();
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(pathToNewImage,	w, h);
				mUserImage.setImageBitmap(bitmap);
				mHintText.setVisibility(View.INVISIBLE);
				Toast.makeText(this, pathToNewImage, Toast.LENGTH_LONG).show();		
				
			}
		}
	}

}
