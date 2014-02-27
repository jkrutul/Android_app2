package com.example.app_2.activities;

import java.io.File;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.app_2.R;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.fragments.ImagesMultiselectFragment;
import com.example.app_2.fragments.ParentMultiselectFragment;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.utils.Utils;

public class AddImagesFromFolderActivity  extends FragmentActivity{
	private Button import_button;
	private EditText pathEditText;
	private Spinner user_spinner;
	
	ArrayList<ImageSpinnerItem> items;
	Long selected_user_id;
	
	private static final String TAG = "AddImagesFromFolderActivity";
	private static final int FILE_SELECT_CODE = 0;
	public static final int PLEASE_WAIT_DIALOG = 1;
	public static final int ADD_TO_DB_WAIT_DIALOG = 2;
	public static ProgressDialog dialog;
	private Long logged_user_id;
	//ParentMultiselectFragment pmf;
	private ImagesMultiselectFragment imf;

	ArrayList<ImageSpinnerItem> categoryItems;
	ArrayList<ImageSpinnerItem> userItems;
	
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
		
		SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);			// pobranie informacji o zalogowanym u¿ytkowniku
		logged_user_id = sharedPref.getLong("logged_user_id", 0);
		if(logged_user_id == 0){
			logged_user_id = null;
		}
		
		//pmf = ParentMultiselectFragment.newInstance(logged_user_id);
		
        
        imf = new ImagesMultiselectFragment();
        Bundle args = new Bundle();
        if(logged_user_id!=null)
        	args.putLong("category_id", logged_user_id);
        
        args.putBoolean("viewOnlyCategories", true);
        args.putBoolean("viewUserRootCategories", true);
        imf.setArguments(args);

    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.replace(R.id.choose_list, imf);
        ft.commit();

        
        
       // setUpUserSpinner();
	}

	/*
	private void setUpUserSpinner(){
		user_spinner = (Spinner) findViewById(R.id.user_spinner);

			items =  new ArrayList<ImageSpinnerItem>();
			String[] projection = {UserContract.Columns._ID, UserContract.Columns.IMG_FILENAME, UserContract.Columns.USERNAME };
		
			Cursor c = getContentResolver().query(UserContract.CONTENT_URI, projection, null, null, null);
			c.moveToFirst();
			while (!c.isAfterLast()) {
				items.add(new ImageSpinnerItem(c.getString(1), c.getString(2), c.getLong(0),false));
				c.moveToNext();
			}
			c.close();
			
			ImageSpinnerAdapter mySpinnerAdapter = new ImageSpinnerAdapter(this, android.R.layout.simple_spinner_item, items);
			mySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			user_spinner.setAdapter(mySpinnerAdapter);
			user_spinner.setSelection(0);
			
			if(logged_user_id!=null){
				int logged_user_pos_in_spinner = 0;

				for(  ImageSpinnerItem item : items){
					if(item.getItemId()== logged_user_id){
						user_spinner.setSelection(logged_user_pos_in_spinner);
						break;
					}
					else
						logged_user_pos_in_spinner++;
				}
			}

			selected_user_id = user_spinner.getSelectedItemId();

			user_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				Bundle bundle = new Bundle();
				
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					ImageSpinnerItem data = items.get(position);
					if(data.isHint()){		
						TextView tv = (TextView)selectedItemView;
						if(tv!=null)
							tv.setTextColor(Color.rgb(148, 150, 148));
					}
									
					Long selection = data.getItemId();
					if(selected_user_id != selection){
						selected_user_id = selection;
						//bundle.putLong("USER_ID", selected_user_id);
						//getSupportLoaderManager().initLoader(ParentMultiselectFragment.LOADER_ID, bundle, pmf);
					
					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					user_spinner.setSelection(0);
					selected_user_id = user_spinner.getSelectedItemId();
				}

			});
		
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
	            Uri uri = data.getData();
	            Log.d(TAG, "File Uri: " + uri.toString());
	            String path = null;
					path = Utils.getPath(this, uri);
					String[] fn = path.split("\\/");
			    	if(fn.length>1)
			    		path = "";
			    		for(int i =0; i< (fn.length-1) ; i++)
			    			path += fn[i] +"/";	    			

				pathEditText.setText(path);
	            Log.d(TAG, "File Path: " + path);
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
							ProcessBitmapsTask processBitmapsTask = new ProcessBitmapsTask(a, selected_user_id);
							ArrayList<String> lArgs = new ArrayList<String>();
							lArgs.add(pathEditText.getText().toString());
							for(Long l :  imf.getCheckedItemIds())
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
