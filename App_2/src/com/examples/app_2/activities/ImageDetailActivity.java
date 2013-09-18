package com.examples.app_2.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImagesContentProvider;
import com.example.app_2.storage.Database;

public class ImageDetailActivity extends Activity{
	 private Spinner mCategory;
	  private EditText mTitleText;
	  private EditText mBodyText;
	  private ImageView mImage;

	  private Uri todoUri;

	  @Override
	  protected void onCreate(Bundle bundle) {
	    super.onCreate(bundle);
	    setContentView(R.layout.image_edit);
	    mImage = (ImageView) findViewById(R.id.img);
	    mCategory = (Spinner) findViewById(R.id.category);
	    mTitleText = (EditText) findViewById(R.id.todo_edit_summary);
	    mBodyText = (EditText) findViewById(R.id.todo_edit_description);
	    Button confirmButton = (Button) findViewById(R.id.todo_edit_button);

	    Bundle extras = getIntent().getExtras();

	    // Check from the saved Instance
	    todoUri = (bundle == null) ? null : (Uri) bundle
	        .getParcelable(ImagesContentProvider.CONTENT_ITEM_TYPE);

	    // Or passed from the other activity
	    if (extras != null) {
	      todoUri = extras.getParcelable(ImagesContentProvider.CONTENT_ITEM_TYPE);

	      fillData(todoUri);
	    }

	    confirmButton.setOnClickListener(new View.OnClickListener() {
	      public void onClick(View view) {
	        if (TextUtils.isEmpty(mTitleText.getText().toString())) {
	          makeToast();
	        } else {
	          setResult(RESULT_OK);
	          finish();
	        }
	      }

	    });
	  }

	  private void fillData(Uri uri) {
	    String[] projection = { Database.COL_PATH, Database.COL_DESC, Database.COL_CAT};
	    Cursor cursor = getContentResolver().query(uri, projection, null, null,null);
	    if (cursor != null) {
	      cursor.moveToFirst();
	      String category = cursor.getString(cursor.getColumnIndexOrThrow(Database.COL_CAT));

	      for (int i = 0; i < mCategory.getCount(); i++) {
	        String s = (String) mCategory.getItemAtPosition(i);
	        if (s.equalsIgnoreCase(category)) {
	          mCategory.setSelection(i);
	        }
	      }

	      mTitleText.setText(cursor.getString(cursor
	          .getColumnIndexOrThrow(Database.COL_PATH)));
	      mBodyText.setText(cursor.getString(cursor
	          .getColumnIndexOrThrow(Database.COL_DESC)));

	      // Always close the cursor
	      cursor.close();
	    }
	  }

	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    saveState();
	    outState.putParcelable(ImagesContentProvider.CONTENT_ITEM_TYPE, todoUri);
	  }

	  @Override
	  protected void onPause() {
	    super.onPause();
	    saveState();
	  }

	  private void saveState() {
	    String category = (String) mCategory.getSelectedItem();
	    String summary = mTitleText.getText().toString();
	    String description = mBodyText.getText().toString();

	    // Only save if either summary or description
	    // is available

	    if (description.length() == 0 && summary.length() == 0) {
	      return;
	    }

	    ContentValues values = new ContentValues();
	    values.put(Database.COL_CAT, category);
	    values.put(Database.COL_PATH, summary);
	    values.put(Database.COL_DESC, description);

	    if (todoUri == null) {
	      // New todo
	      todoUri = getContentResolver().insert(ImagesContentProvider.CONTENT_URI, values);
	    } else {
	      // Update todo
	      getContentResolver().update(todoUri, values, null, null);
	    }
	  }

	  private void makeToast() {
	    Toast.makeText(ImageDetailActivity.this, "Please maintain a summary",
	        Toast.LENGTH_LONG).show();
	  }
}
