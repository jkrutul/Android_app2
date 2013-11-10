package com.example.app_2.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.example.app_2.R;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;

public class UsersActivity extends FragmentActivity implements LoaderCallbacks<Cursor>{
	private ListView lv;
	private TextView empty;
	private SimpleCursorAdapter adapter;
	private static final int LOADER_ID = 14;
	private static final int DELETE_ID = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_users);
		lv = (ListView) findViewById(R.id.users_list_view);
		empty = (TextView) findViewById(android.R.id.empty);
		
		
		new ImageLoader(this);

		String[] from = new String[] {
				UserContract.Columns._ID,
				UserContract.Columns.USERNAME,
				UserContract.Columns.IMG_FILENAME,
				UserContract.Columns.ROOT_FK,
				UserContract.Columns.ISMALE};

		int[] to = new int[] { 0, R.id.user_name, R.id.user_image, R.id.user_root, R.id.user_gender}; 		// Fields on the UI to which we map

		adapter = new SimpleCursorAdapter( this.getApplicationContext(), R.layout.user_row, null, from, to, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				switch(view.getId()){
				case R.id.user_image:
					String path = Images.getImageThumbsPath(cursor.getString(cursor.getColumnIndex(UserContract.Columns.IMG_FILENAME)));
					ImageLoader.loadBitmap(path, (ImageView) view, false);
					return true;
				
				case R.id.user_gender:
					TextView tv=(TextView) view;
					int l = Integer.valueOf(cursor.getString(cursor.getColumnIndex(UserContract.Columns.ISMALE)));
					if(l == 1)
						tv.setText("ch³opiec");
					else
						tv.setText("dziewczyna");
					return true;
				}
				return false;
			}
		});
		
		
		lv.setAdapter(adapter);
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		registerForContextMenu(lv);
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, "usuñ");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Uri uri = Uri.parse(UserContract.CONTENT_URI + "/"	+ info.id);
			getContentResolver().delete(uri, null, null);
			getSupportLoaderManager().restartLoader(0, null, this);
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.users_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.adduser:
			i = new Intent(this, AddUserActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	



	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		CursorLoader cursorLoader = null;

	
		String[] projection = new String[] {
				UserContract.Columns._ID,
				UserContract.Columns.USERNAME,
				UserContract.Columns.IMG_FILENAME,
				UserContract.Columns.ROOT_FK,
				UserContract.Columns.ISMALE};

		cursorLoader = new CursorLoader(this,UserContract.CONTENT_URI, projection, null, null, null);

		return cursorLoader;
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)  {
	     ((SimpleCursorAdapter) adapter).swapCursor(cursor);
	     if(lv.getCount()==0)
	    	 empty.setVisibility(View.VISIBLE);
	     else
	    	 empty.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter) adapter).swapCursor(null);
	}

}
