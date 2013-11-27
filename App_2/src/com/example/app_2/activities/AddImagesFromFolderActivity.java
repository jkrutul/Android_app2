package com.example.app_2.activities;

import java.io.File;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.app_2.fragments.ImageListFragment;
import com.example.app_2.fragments.ParentMultiselectFragment;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.provider.SpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.Utils;

public class AddImagesFromFolderActivity  extends FragmentActivity{
	Button import_button;
	EditText pathEditText;
	private Spinner mCatSpinner, mUserSpinner;
	private static final String TAG = "AddImagesFromFolderActivity";
	private static final int FILE_SELECT_CODE = 0;
	public static final int PLEASE_WAIT_DIALOG = 1;
	public static final int ADD_TO_DB_WAIT_DIALOG = 2;
	public static ProgressDialog dialog;
	Long import_to_parent_id, user_id;
	ParentMultiselectFragment pmf;
	
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
		pathEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0)
					import_button.setEnabled(false);
				else
					import_button.setEnabled(true);
			}
		});
		
        pmf = new ParentMultiselectFragment();
        
    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.replace(R.id.choose_list, pmf);
        ft.commit();
		//mCatSpinner = (Spinner) findViewById(R.id.parent_spinner);
		//addItemsOnCategorySpinner();
	}

/*

	private void addItemsOnCategorySpinner() {

		mCatSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				SpinnerItem data = categoryItems.get(position);
				if(data.isHint()){
					TextView tv = (TextView)selectedItemView;
					if(tv != null)
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
		String[] projection = { ImageContract.Columns._ID, ImageContract.Columns.CATEGORY, ImageContract.Columns.FILENAME };
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
	*/
/*
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
	
*/
	
	public void showFileChooser(View view) {
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
	    intent.setType("*/*"); 
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
					path = Utils.getPath(this, uri);
					String[] fn = path.split("\\/");
			    	if(fn.length>1)
			    		path = "";
			    		for(int i =0; i< (fn.length-1) ; i++)
			    			path += fn[i] +"/";	    			

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
		String importPath = pathEditText.getText().toString();
		if(!importPath.equals("")){
			File f = new File(importPath);
			if(f.exists()){
				final Activity a = this;
				 new AlertDialog.Builder(this)
			        .setTitle("Znaleziono "+Images.getListOfImageFiles(f.getAbsolutePath()).size()+" obrazków")
			        .setMessage("Dodaæ do aplikacji?")
			        .setNegativeButton(android.R.string.no,  new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
			        })
			        .setPositiveButton(android.R.string.yes, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String parent_fk= null;
							//int spinnerSelectedPos = mCatSpinner.getSelectedItemPosition();
							//if (spinnerSelectedPos != Spinner.INVALID_POSITION)
							//	parent_fk = String.valueOf(categoryItems.get(spinnerSelectedPos).getItemId());
							
							ProcessBitmapsTask processBitmapsTask = new ProcessBitmapsTask(a);
							ArrayList<String> lArgs = new ArrayList<String>();
							lArgs.add(pathEditText.getText().toString());
							lArgs.add("-1");
							for(Long l :  pmf.getCheckedItemIds())
								lArgs.add(String.valueOf(l));
							
							processBitmapsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, lArgs);
						}
			        }).create().show();
			}
			else{
				Toast.makeText(this, "Folder o podanej œcie¿ce nie istnieje!", Toast.LENGTH_LONG).show();
			}
		}
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
