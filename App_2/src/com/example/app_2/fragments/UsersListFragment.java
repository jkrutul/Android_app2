package com.example.app_2.fragments;
// TODO NOT USED
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.provider.Images;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

public class UsersListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	SimpleCursorAdapter adapter;
	boolean mDualPane;
	int mCurCheckPosition = 0;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int LOADER_ID = 0;
	private static final String TAG = "UsersListFragment";
	
	public UsersListFragment(){
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());

		String[] from = new String[] {
				UserContract.Columns._ID,
				UserContract.Columns.USERNAME,
				UserContract.Columns.IMG_FILENAME,
				UserContract.Columns.ROOT_FK,
				UserContract.Columns.ISMALE};

		int[] to = new int[] { 0, R.id.user_name, R.id.user_image, R.id.user_root, R.id.user_gender}; 		// Fields on the UI to which we map

		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.user_row, null, from, to, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				switch(view.getId()){
				case R.id.user_image:
					String path = Storage.getPathToScaledBitmap(cursor.getString(cursor.getColumnIndex(UserContract.Columns.IMG_FILENAME)),300);
					ImageLoader.loadBitmap(path, (ImageView) view);
					return true;
				
				case R.id.user_gender:
					TextView tv=(TextView) view;
					int l = Integer.valueOf(cursor.getString(cursor.getColumnIndex(UserContract.Columns.ISMALE)));
					if(l == 1)
						tv.setText("Ch³opak");
					else
						tv.setText("Dziewczyna");
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		registerForContextMenu(getListView());
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurCheckPosition);
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
			getActivity().getContentResolver().delete(uri, null, null);
			getLoaderManager().restartLoader(0, this.getArguments(), this);
			return true;
		}
		return super.onContextItemSelected(item);
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

		cursorLoader = new CursorLoader(getActivity(),UserContract.CONTENT_URI, projection, null, null, null);

		return cursorLoader;
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)  {
	     ((SimpleCursorAdapter) this.getListAdapter()).swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		((SimpleCursorAdapter)this.getListAdapter()).swapCursor(null);
	}

}
