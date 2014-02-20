package com.example.app_2.activities;

import java.util.ArrayList;

import javax.crypto.spec.OAEPParameterSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.intents.ImageIntents;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.provider.Images.ProcessOneBitmapTask;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

public class AddUserActivity extends Activity {
	private EditText mUserName;
	private TextView mHintText;
	private RadioButton mMaleRB, mFemaleRB;

	private Button mSubmit_button;
	private ImageView mUserImage;
	
	private String image_path;
	private final int FILE_SELECT_REQUEST = 12;
	private final int TAKE_PIC_REQUEST = 24;
	public static final int PLEASE_WAIT_DIALOG = 97;
	public static ProgressDialog dialog;
	
	private String pathToNewImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adduser);
		initViews();
	
	}
	
	private void initViews(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		mMaleRB = (RadioButton) findViewById(R.id.radioMale);
		mFemaleRB = (RadioButton) findViewById(R.id.radioFemale);
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
	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.adduser_button:
			String username = mUserName.getText().toString();
			String filename = null;
			int ismale = mMaleRB.isChecked() == true ? 1 : 0;
			if (pathToNewImage != null) {
				
				ScaleAddImageTask sait = new ScaleAddImageTask(this, username, mMaleRB.isChecked());
				sait.execute(pathToNewImage);
				filename = Utils.getFilenameFromPath(pathToNewImage);
				
			}

			break;

		case R.id.cancel_adduser_button:
			Storage.saveToPreferences(null, "photoPath", this, Activity.MODE_PRIVATE); // clear preferences
			finish();
			break;

		case R.id.user_image:
			final Activity a = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(this); // Add the buttons
			builder.setTitle("Wybierz...");
			builder.setMessage("Sk�d pobra� obrazek?");
			builder.setPositiveButton("Wykonaj zdj�cie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.cameraIntent(a, TAKE_PIC_REQUEST, App_2.maxWidth, App_2.getMaxWidth());
						}
					});
			builder.setNegativeButton("Wybierz obrazek z dysku",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a,FILE_SELECT_REQUEST, App_2.maxWidth, App_2.getMaxWidth());
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
				
				
				setImage(pathToNewImage);
			
				Toast.makeText(this, pathToNewImage, Toast.LENGTH_SHORT).show();		
				
			}
		}
	}
	
	@Override
    public Dialog onCreateDialog(int dialogId) {
	        switch (dialogId) {
	        case PLEASE_WAIT_DIALOG:
	        	dialog = new ProgressDialog(this);
	            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            dialog.setCancelable(false);
	            dialog.setTitle("Dodawanie nowego obrazka");
	            dialog.setMessage("Prosz� czeka�....");
	            return dialog;
	        default:
	            break;
	        }
	        return null;
	    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("pathToImage", pathToNewImage);
		outState.putString("userName", mUserName.getText().toString());
		outState.putBoolean("isMale", mMaleRB.isChecked());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		pathToNewImage = savedInstanceState.getString("pathToImage");
		String userName = savedInstanceState.getString("userName");
		Boolean isMale = savedInstanceState.getBoolean("isMale");
		
		
		if(pathToNewImage != null)
			setImage(pathToNewImage);
		
		mUserName.setText(userName);
		
		if(isMale){
			mMaleRB.setChecked(true);
			mFemaleRB.setChecked(false);
		}else{
			mMaleRB.setChecked(false);
			mFemaleRB.setChecked(true);
		}
		

		
	}
	
	private void setImage(String path){

        Bitmap b = ScalingUtilities.decodeFile(path, 150, 150, ScalingLogic.FIT);
        mUserImage.setImageBitmap(b);
        image_path = path;
        mHintText.setVisibility(View.INVISIBLE);
	}
	private void addNewUserToDb(String username, boolean ismale, String user_img){
		ContentValues img_val = new ContentValues();								// stworzenie nowego korzenia dla u�ytkownika
		img_val.put(ImageContract.Columns.FILENAME, user_img);
		img_val.put(ImageContract.Columns.DESC, username + " - G��wna");
		img_val.put(ImageContract.Columns.IS_CATEGORY, true);
		Uri img_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_val);
		

		
		ContentValues parent_root = new ContentValues();							// powi�zanie korzenia u�ytkownika z g��wnym korzeniem
		parent_root.put(ParentContract.Columns.IMAGE_FK, img_uri.getLastPathSegment());
		parent_root.put(ParentContract.Columns.PARENT_FK, Database.getMainRootFk());
		getContentResolver().insert(ParentContract.CONTENT_URI, parent_root);
		
		Long user_root_fk = Long.valueOf(img_uri.getLastPathSegment()); 			// dodanie nowego u�ytkownika do bazy
		ContentValues user_val = new ContentValues();
		user_val.put(UserContract.Columns.USERNAME, username);
		int isMale = (ismale)  ? 1 : 0;
		user_val.put(UserContract.Columns.ISMALE, isMale);
		user_val.put(UserContract.Columns.IMG_FILENAME, user_img);
		user_val.put(UserContract.Columns.ROOT_FK, user_root_fk);
		Uri user_uri = getContentResolver().insert(UserContract.CONTENT_URI, user_val);

		Long user_id = Long.parseLong(user_uri.getLastPathSegment());
		
		img_val.put(ImageContract.Columns.AUTHOR_FK,user_id); 					// powi�zanie u�ytkownika z jego korzeniem
		getContentResolver().update(img_uri, img_val, null, null);
		
		
		ContentValues me_val = new ContentValues();								// dodanie symbolu "JA"
		me_val.put(ImageContract.Columns.FILENAME, user_img);
		me_val.put(ImageContract.Columns.DESC, "Ja");
		me_val.put(ImageContract.Columns.AUTHOR_FK,user_id);
		Uri me_uri = getContentResolver().insert(ImageContract.CONTENT_URI, me_val);
		
		ContentValues me_parent = new ContentValues();							// dodanie symbolu do s�ownika
		me_parent.put(ParentContract.Columns.IMAGE_FK, me_uri.getLastPathSegment());
		me_parent.put(ParentContract.Columns.PARENT_FK, Database.getMainDictFk());
		getContentResolver().insert(ParentContract.CONTENT_URI, me_parent);
		
		
																				
		Storage.saveToPreferences(null, "photoPath", this, Activity.MODE_PRIVATE); 	// clear preferences
	}
	
	
	public class ScaleAddImageTask extends AsyncTask<String, Void, Void>{
		private Activity executing_activity;
		private String username;
		private Boolean isMale;
		
		public ScaleAddImageTask(Activity a, String username, Boolean isMale){
			this.executing_activity = a;
			this.isMale = isMale;
			this.username = username;
			
		}
		
		@Override
		protected void onPreExecute() {
			this.executing_activity.showDialog(AddUserActivity.PLEASE_WAIT_DIALOG,null);
		}
		
		@Override
		protected Void doInBackground(String... params) {
			String path = params[0];
			Database db = Database.getInstance(App_2.getAppContext());
			Database.open();
			Storage.scaleAndSaveBitmapFromPath(path,Bitmap.CompressFormat.PNG,90,db, false);
			addNewUserToDb(username, isMale, Utils.getFilenameFromPath(path));

			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result){
			this.executing_activity.removeDialog(AddUserActivity.PLEASE_WAIT_DIALOG);
			executing_activity.finish();
		}

	}
}
