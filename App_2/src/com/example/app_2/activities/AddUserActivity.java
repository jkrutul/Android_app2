package com.example.app_2.activities;

import java.net.URISyntaxException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.provider.Images.AddingImageTask;
import com.example.app_2.utils.Utils;

public class AddUserActivity extends Activity {
	EditText mUserName;
	RadioButton mMaleRB, mFemaleRB;
	private Button mSubmit_button;
	private final int FILE_SELECT_CODE = 12;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adduser);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mUserName = (EditText) findViewById(R.id.user_name);
		mMaleRB = (RadioButton) findViewById(R.id.radioMale);
		mSubmit_button= (Button) findViewById(R.id.adduser_button);

		mUserName.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {

	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){
	        	if(s.length()==0){
	        		mSubmit_button.setEnabled(false);
	        	}
	        	else
	        		mSubmit_button.setEnabled(true);
	        }
	    }); 
	}
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.adduser_button:
			String username = mUserName.getText().toString();
			int ismale = mMaleRB.isChecked() == true  ? 1 : 0;
			//String[] projection = { ImageContract.Columns._ID,
			//		ImageContract.Columns.CATEGORY };
			
			ContentValues img_val = new ContentValues();
			img_val.put(ImageContract.Columns.CATEGORY, username+" - G³ówna");
			//img_val.put(ImageContract.Columns.PATH, )
			Uri img_uri = getContentResolver().insert(ImageContract.CONTENT_URI, img_val);

			Long user_root_fk = Long.valueOf(img_uri.getLastPathSegment());
			ContentValues user_val = new ContentValues();
			user_val.put(UserContract.Columns.USERNAME, username);
			user_val.put(UserContract.Columns.ISMALE, ismale);
			user_val.put(UserContract.Columns.ROOT_FK, user_root_fk);
			Uri user_uri = getContentResolver().insert(UserContract.CONTENT_URI, user_val);
			
			
			img_val.put(ImageContract.Columns.AUTHOR_FK, user_uri.getLastPathSegment()); // ustawienie autora korzenia
			getContentResolver().update(img_uri, img_val, null, null);
			
			finish();
			break;
			
		case R.id.cancel_adduser_button:
			
			finish();
			break;
			
		case R.id.user_image:
		    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
		    intent.setType("*/*"); 
		    //intent.addCategory(Intent.CATEGORY_OPENABLE);

		    try {
		        startActivityForResult( Intent.createChooser(intent, "Wybierz folder z obrazkami"), FILE_SELECT_CODE);
		    } catch (android.content.ActivityNotFoundException ex) {
		        // Potentially direct the user to the Market with a Dialog
		        Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
		    }
			break;

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
					path = Utils.getPath(this, uri);			    	
				} catch (URISyntaxException e) {

					e.printStackTrace();
				}
				AddingImageTask ait = new AddingImageTask();
				ait.execute(path);
				
	        }
	        break;
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

}
