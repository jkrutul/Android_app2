package com.example.app_2.activities;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.example.app_2.R;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.spinner.model.UserSpinnerItem;

public class UserLoginActivity extends Activity {
	
	private Spinner mSpinner;
	private ArrayList<UserSpinnerItem> items;
	private UserSpinnerItem data;
	
	private OnItemSelectedListener oisl = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			data = items.get(position);
			//if(data.isHint()){
			//	TextView tv = (TextView)selectedItemView;
			//	if(tv!=null)
			//		tv.setTextColor(Color.rgb(148, 150, 148));
			//}
			//bundle.putLong("cat_id", data.getItemId());
			//getLoaderManager().restartLoader(0, bundle, ilf);
		}
		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
			//bundle.putLong("cat_id", -1);
			//getLoaderManager().restartLoader(0, bundle, ilf);
		}
	
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_login);
		mSpinner = (Spinner) findViewById(R.id.user_select_spinner);
		addItemsOnSpinner();
		getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_green_light))); 
		
	}

	private void addItemsOnSpinner() {

		mSpinner.setOnItemSelectedListener(oisl);
		
		items =  new ArrayList<UserSpinnerItem>();
		//items.add(new SpinnerItem(null,"Wybierz kategoriê", Long.valueOf(-1), true));
		String[] projection = { UserContract.Columns._ID,
								UserContract.Columns.IMG_FILENAME,
								UserContract.Columns.USERNAME,
								UserContract.Columns.ROOT_FK };
		Cursor c = getContentResolver().query(UserContract.CONTENT_URI, projection, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			items.add(new UserSpinnerItem(c.getString(1), c.getString(2), c.getLong(0), c.getLong(3),false));
			c.moveToNext();
		}
		c.close();
		
		ImageSpinnerAdapter mySpinnerAdapter = new ImageSpinnerAdapter(this, android.R.layout.simple_spinner_item, (List<ImageSpinnerItem>)(List<?>) items);
		mySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mySpinnerAdapter);
		mSpinner.setSelection(0);
	}

	
	public void onLoginButtonClick(View view){
		if(data != null){
			long logged_user_root = data.getUser_root_fk();
			long user_id = data.getItemId();
			SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putLong("logged_user_root", logged_user_root);
			editor.putLong("logged_user_id", user_id);

			editor.commit();
			
			Uri uri = Uri.parse(UserContract.CONTENT_URI + "/"	+ user_id);
			Cursor c = getContentResolver().query(uri, new String[]{
					UserContract.Columns.FONT_SIZE,							//0
					UserContract.Columns.IMG_SIZE,							//1
					UserContract.Columns.CAT_BACKGROUND,					//2
					UserContract.Columns.CONTEXT_CAT_BACKGROUND,			//3
					UserContract.Columns.ISMALE								//4
					},null, null, null);
			c.moveToFirst();
			if(!c.isAfterLast()){
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
				SharedPreferences.Editor e = sp.edit();
				e.putInt("pref_img_size", Integer.valueOf(c.getString(1)));
				e.putInt("pref_img_desc_font_size", Integer.valueOf(c.getString(0)));
				
				String catColorValue = c.getString(2);
				String ctxColorValue = c.getString(3);
				
				if(catColorValue!=null)
					e.putInt("category_view_background", Integer.valueOf(catColorValue));
				if(ctxColorValue!=null)
					e.putInt("context_category_view_background", Integer.valueOf(ctxColorValue));
				
				e.putInt("is_male", c.getInt(4));

				e.commit();
			}
			c.close();
			

			Intent intent = new Intent(this, ImageGridActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
			finish();
		}
	}
}
