package com.example.app_2.activities;

import java.net.URISyntaxException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.storage.Storage;

public class AddImagesFromFolderActivity  extends Activity{
	Button import_button;
	EditText pathEditText;
	private static final String TAG = "AddImagesFromFolderActivity";
	private static final int FILE_SELECT_CODE = 0;
	public static final int PLEASE_WAIT_DIALOG = 1;
	public static ProgressDialog dialog;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_import);
		
		pathEditText = (EditText) findViewById(R.id.path_to_folder);
		import_button= (Button) findViewById(R.id.import_button);

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
					path = getPath(this, uri);
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
	
	public static String getPath(Context context, Uri uri) throws URISyntaxException {
	    if ("content".equalsIgnoreCase(uri.getScheme())) {
	        String[] projection = { "_data" };
	        Cursor cursor = null;

	        try {
	            cursor = context.getContentResolver().query(uri, projection, null, null, null);
	            int column_index = cursor.getColumnIndexOrThrow("_data");
	            if (cursor.moveToFirst()) {
	                return cursor.getString(column_index);
	            }
	        } catch (Exception e) {
	            // Eat it
	        }
	    }
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	} 
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onImportClick(View view){
		ProcessBitmapsTask processBitmapsTask = new ProcessBitmapsTask(this);
		processBitmapsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pathEditText.getText().toString());
		
	}
	
	 @Override
	    public Dialog onCreateDialog(int dialogId) {
	        switch (dialogId) {
	        case PLEASE_WAIT_DIALOG:
	        	dialog = new ProgressDialog(this);
	            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	     
	            dialog.setCancelable(false);
	            dialog.setTitle("Import obrazków....");
	            dialog.setMessage("Proszê czekaæ....");
	            return dialog;
	 
	        default:
	            break;
	        }
	        return null;
	    }
	    
	
}
