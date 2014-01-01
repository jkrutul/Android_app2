package com.example.app_2.activities;

import javax.crypto.spec.OAEPParameterSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.intents.ImageIntents;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

public class AddUserActivity extends Activity {
	EditText mUserName;
	TextView mHintText;
	RadioButton mMaleRB, mFemaleRB;
	private Button mSubmit_button;
	private ImageView mUserImage;

	private final int FILE_SELECT_REQUEST = 12;
	private final int TAKE_PIC_REQUEST = 24;
	private String pathToNewImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adduser);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mMaleRB = (RadioButton) findViewById(R.id.radioMale);
		mUserImage = (ImageView) findViewById(R.id.user_image);
		mSubmit_button = (Button) findViewById(R.id.adduser_button);
		mHintText = (TextView) findViewById(R.id.user_img_hint);
		mUserName = (EditText) findViewById(R.id.user_name);
		mUserName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0)
					mSubmit_button.setEnabled(false);
				else
					mSubmit_button.setEnabled(true);
			}
		});
		if(savedInstanceState!= null){
			pathToNewImage = (String) savedInstanceState.get("photoPath");
			if(pathToNewImage!= null){
				mHintText.setVisibility(View.INVISIBLE);
				mUserImage.setImageBitmap(ScalingUtilities.decodeFile(pathToNewImage, 150, 150, ScalingLogic.FIT));
			}
		}
	
	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.adduser_button:
			String username = mUserName.getText().toString();
			String filename = null;
			int ismale = mMaleRB.isChecked() == true ? 1 : 0;
			if (pathToNewImage != null) {
				filename = Utils.getFilenameFromPath(pathToNewImage);
				AddingImageTask ait = new AddingImageTask(this);		// dodanie obrazka do katalogu aplikacji
				ait.filenameVerification = false;
				ait.execute(pathToNewImage);
			}
			addNewUserToDb(username, ismale, filename);
			finish();
			break;

		case R.id.cancel_adduser_button:
			Storage.saveToPreferences(null, "photoPath", this, Activity.MODE_PRIVATE); // clear preferences
			finish();
			break;

		case R.id.user_image:
			final Activity a = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this); // Add the buttons
			builder.setPositiveButton("zrób zdjêcie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.cameraIntent(a, TAKE_PIC_REQUEST);
						}
					});
			builder.setNegativeButton("wybierz obrazek",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a,FILE_SELECT_REQUEST);
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
				
				//mUserImage.setImageBitmap(ScalingUtilities.decodeFile(pathToNewImage, 300, 300, ScalingLogic.FIT));
				ImageLoader.loadBitmap(pathToNewImage, mUserImage);
				mHintText.setVisibility(View.INVISIBLE);
				Toast.makeText(this, pathToNewImage, Toast.LENGTH_LONG).show();		
				
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(pathToNewImage!= null)
			outState.putString("photoPath", pathToNewImage);
	}
	
	private void addNewUserToDb(String username, int ismale, String user_img){
		ContentValues img_val = new ContentValues();								// stworzenie nowego korzenia dla u¿ytkownika
		img_val.put(ImageContract.Columns.FILENAME, user_img);
		img_val.put(ImageContract.Columns.DESC, username + " - G³ówna");
		img_val.put(ImageContract.Columns.CATEGORY, username + " - G³ówna");
		Uri img_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_val);
		
		ContentValues parent_root = new ContentValues();							// powi¹zanie korzenia u¿ytkownika z g³ównym korzeniem
		parent_root.put(ParentContract.Columns.IMAGE_FK, img_uri.getLastPathSegment());
		parent_root.put(ParentContract.Columns.PARENT_FK, Database.getMainRootFk());
		getContentResolver().insert(ParentContract.CONTENT_URI, parent_root);
		
		Long user_root_fk = Long.valueOf(img_uri.getLastPathSegment()); 			// dodanie nowego u¿ytkownika do bazy
		ContentValues user_val = new ContentValues();
		user_val.put(UserContract.Columns.USERNAME, username);
		user_val.put(UserContract.Columns.ISMALE, ismale);
		user_val.put(UserContract.Columns.IMG_FILENAME, user_img);
		user_val.put(UserContract.Columns.ROOT_FK, user_root_fk);
		Uri user_uri = getContentResolver().insert(UserContract.CONTENT_URI, user_val);

		img_val.put(ImageContract.Columns.AUTHOR_FK,user_uri.getLastPathSegment()); // powi¹zanie u¿ytkownika z jego korzeniem
		getContentResolver().update(img_uri, img_val, null, null);
		Storage.saveToPreferences(null, "photoPath", this, Activity.MODE_PRIVATE); 	// clear preferences
	}
}
