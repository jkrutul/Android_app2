package com.examples.app_2.activities;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.RelativeLayout;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;

import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;


//@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImagesOverviewActivity extends android.support.v4.app.FragmentActivity implements	 AdapterView.OnItemClickListener, LoaderCallbacks<Cursor> {

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int DELETE_ID = Menu.FIRST + 1;
	//ImageLoader il;
	private static final String TAG = "ImagesOveriewActivity";

	private SimpleCursorAdapter adapter;
	private ListView lv;


	
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ImageLoader(getApplicationContext());
		setContentView(R.layout.image_list);
		lv = (ListView) findViewById(R.id.image_list_view);
		lv.setOnItemClickListener(this);
		//this.getListView().setDividerHeight(2);
		fillData();
		registerForContextMenu(lv);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}

	// Create the menu based on the XML defintion
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}

	// Reaction to the menu selection
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.insert:
			createTodo();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"
					+ info.id);
			getContentResolver().delete(uri, null, null);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createTodo() {
		Intent i = new Intent(this, ImageDetailActivity.class);
		startActivity(i);
	}

	// Opens the second activity if an entry is clicked
/*
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, ImageDetailActivity.class);
		Uri todoUri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		i.putExtra(ImageContract.CONTENT_ITEM_TYPE, todoUri);

		startActivity(i);
	}
*/

	private void fillData() {
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { ImageContract.Columns._ID,
									   ImageContract.Columns.PATH,
									   ImageContract.Columns.PATH,
									   ImageContract.Columns.CATEGORY,
									   ImageContract.Columns.PARENT};
		// Fields on the UI to which we map
		int[] to = new int[] { 0,R.id.label, R.id.icon, R.id.category, R.id.perent };
		getSupportLoaderManager().initLoader(0, null, this);
		
		Cursor c = getContentResolver().query(ImageContract.CONTENT_URI, from, null, null ,null);
		if(c.getCount()<=0){
			c.close();
		}
		
		adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.image_row, c, from,to, 0);
		
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			   /** Binds the Cursor column defined by the specified index to the specified view */
			   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			       if(view.getId() == R.id.icon){
						 String path = Images.getImageThumbsPath(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH)));
						 ImageLoader.loadBitmap(path, (ImageView) view);
			           return true; //true because the data was bound to the view
			       }
			       return false;
			   }
			});

		lv.setAdapter(adapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, "delete");
	}

	// Creates a new loader after the initLoader () call
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { ImageContract.Columns._ID,	ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY , ImageContract.Columns.PARENT};
		CursorLoader cursorLoader = new CursorLoader(this,ImageContract.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, final  View thumbView, int position, long id) {
		Intent i = new Intent(this, ImageDetailActivity.class);
		Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		i.putExtra(ImageContract.CONTENT_ITEM_TYPE, uri);

		startActivity(i);
		overridePendingTransition(R.anim.right_slide_in,R.anim.right_slide_out);
	}
	@Override
	public void onBackPressed() {
		this.finish();
		overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
	}

}
