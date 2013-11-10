package com.example.app_2.activities;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.adapters.MySpinnerAdapter;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.provider.SpinnerItem;
import com.example.app_2.utils.Utils;

public class AddImagesFromFolderActivity  extends Activity{
	Button import_button;
	EditText pathEditText;
	private Spinner mCatSpinner, mUserSpinner;
	private static final String TAG = "AddImagesFromFolderActivity";
	private static final int FILE_SELECT_CODE = 0;
	public static final int PLEASE_WAIT_DIALOG = 1;
	public static final int ADD_TO_DB_WAIT_DIALOG = 2;
	public static ProgressDialog dialog;
	Long import_to_parent_id, user_id;
	
	private MySpinnerAdapter category_adapter;
	private MySpinnerAdapter user_adapter;
	
	ArrayList<SpinnerItem> categoryItems;
	ArrayList<SpinnerItem> userItems;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		setContentView(R.layout.activity_import);
		pathEditText = (EditText) findViewById(R.id.path_to_folder);
		import_button= (Button) findViewById(R.id.import_button);
		mUserSpinner = (Spinner) findViewById(R.id.user_spinner);
		mCatSpinner = (Spinner) findViewById(R.id.parent_spinner);
		
		//addItemsOnUserSpinner();
		addItemsOnCategorySpinner();
	}



	private void addItemsOnCategorySpinner() {

		mCatSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SpinnerItem data = categoryItems.get(position);
				if(data.isHint()){
					TextView tv = (TextView)selectedItemView;
					tv.setTextColor(Color.rgb(148, 150, 148));
				}
				import_to_parent_id = data.getItemId();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				import_to_parent_id = Long.valueOf(-1);
			}

		});
		
		categoryItems =  new ArrayList<SpinnerItem>();
		categoryItems.add(new SpinnerItem(null,"Wybierz kategoriê", Long.valueOf(-1), true));
		String[] projection = { ImageContract.Columns._ID, ImageContract.Columns.CATEGORY, ImageContract.Columns.PATH };
		String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		Cursor c = getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			categoryItems.add(new SpinnerItem(c.getString(2), c.getString(1), c.getLong(0),false));
			c.moveToNext();
		}
		c.close();
		
		category_adapter = new MySpinnerAdapter(this, android.R.layout.simple_spinner_item, categoryItems);
		category_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCatSpinner.setAdapter(category_adapter);
		mCatSpinner.setSelection(0);
	}

	private void addItemsOnUserSpinner() {

		mUserSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SpinnerItem data = userItems.get(position);
				if(data.isHint()){
					TextView tv = (TextView)selectedItemView;
					tv.setTextColor(Color.rgb(148, 150, 148));
				}
				user_id = data.getItemId();

			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				user_id = Long.valueOf(-1);
			}

		});
		
		userItems =  new ArrayList<SpinnerItem>();
		userItems.add(new SpinnerItem(null,"Wszyscy u¿ytkownicy", Long.valueOf(-1), true));
		String[] projection = { UserContract.Columns._ID, UserContract.Columns.USERNAME, UserContract.Columns.IMG_FILENAME };
		Cursor c = getContentResolver().query(UserContract.CONTENT_URI, projection, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			userItems.add(new SpinnerItem(c.getString(2), c.getString(1), c.getLong(0),false));
			c.moveToNext();
		}
		c.close();
		
		user_adapter = new MySpinnerAdapter(this, android.R.layout.simple_spinner_item, userItems);
		user_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mUserSpinner.setAdapter(user_adapter);
		mUserSpinner.setSelection(0);
	}
	
	
	
	public void showFileChooser(View view) {
		
		//Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		//Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
		//    + "/myFolder/");
		//intent.setDataAndType(uri, "text/csv");
		//startActivity(Intent.createChooser(intent, "Open folder"));
		
		
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
	    intent.setType("*/*"); 
	    //intent.addCategory(Intent.CATEGORY_OPENABLE);

	    try {
	        startActivityForResult( Intent.createChooser(intent, "Wybierz folder z obrazkami"), FILE_SELECT_CODE);
	    } catch (android.content.ActivityNotFoundException ex) {
	        // Potentially direct the user to the Market with a Dialog
	        Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	        case FILE_SELECT_CODE:
	        if (resultCode == RESULT_OK) {
	            // Get the Uri of the selected file 
	            Uri uri = data.getData();
	            Log.d(TAG, "File Uri: " + uri.toString());
	            // Get the path
	            String path = null;
				try {
					path = Utils.getPath(this, uri);
					String[] fn = path.split("\\/");
			    	if(fn.length>1)
			    		path = "";
			    		for(int i =0; i< (fn.length-1) ; i++)
			    			path += fn[i] +"/";	    			
			    	
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pathEditText.setText(path);
	            Log.d(TAG, "File Path: " + path);
	            // Get the file instance
	            // File file = new File(path);
	            // Initiate the upload
	        }
	        break;
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onImportClick(View view){
		String parent_fk= null;
		int spinnerSelectedPos = mCatSpinner.getSelectedItemPosition();
		if (spinnerSelectedPos != Spinner.INVALID_POSITION)
			parent_fk = String.valueOf(categoryItems.get(spinnerSelectedPos).getItemId());
		
		ProcessBitmapsTask processBitmapsTask = new ProcessBitmapsTask(this);
		//AddToDatabaseTask addToDbTask = new AddToDatabaseTask(this);
		
		//if(processBitmapsTask.getStatus() == android.os.AsyncTask.Status.PENDING)
			processBitmapsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pathEditText.getText().toString(), parent_fk);
			//finish();
		//else if(processBitmapsTask.getStatus() == android.os.AsyncTask.Status.FINISHED)
		//	addToDbTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,  pathEditText.getText().toString(), parent_fk);
		
		//else
		//	;
		/*
		finish();
		 if (asynclass.getStatus() == android.os.AsyncTask.Status.PENDING) {
             asynclass.execute();
         } else if (RF.getStatus() == android.os.AsyncTask.Status.FINISHED) {
             asynclass = new asyncclass();
             asynclass.execute();
         } else {
             Toast.maketoast(this, "Plz wait", 1).show();
         }
         */
	}
	
	@Override
    public Dialog onCreateDialog(int dialogId) {
	        switch (dialogId) {
	        case PLEASE_WAIT_DIALOG:
	        	dialog = new ProgressDialog(this);
	            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            dialog.setCancelable(false);
	            dialog.setTitle("Import i skalowanie obrazków....");
	            dialog.setMessage("Proszê czekaæ....");
	            return dialog;
	            
	        case ADD_TO_DB_WAIT_DIALOG:
	        	dialog = new ProgressDialog(this);
	            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            dialog.setCancelable(false);
	            dialog.setTitle("Dodawanie wpisów do bazy danych....");
	            dialog.setMessage("Proszê czekaæ....");
	        	break;
	 
	        default:
	            break;
	        }
	        return null;
	    }
}
