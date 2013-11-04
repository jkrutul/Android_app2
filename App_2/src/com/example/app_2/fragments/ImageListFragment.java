package com.example.app_2.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;

public class ImageListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	SimpleCursorAdapter adapter;
	boolean mDualPane;
	int mCurCheckPosition = 0;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int LOADER_ID = 0;
	private static final String TAG = "ImageListFragment";
	
	public ImageListFragment(){
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());

		String[] from = new String[] { 
				ImageContract.Columns._ID,
				ImageContract.Columns.PATH,
				ImageContract.Columns.PATH,
				ImageContract.Columns.CATEGORY};
		int[] to = new int[] { 0, R.id.label, R.id.icon, R.id.category}; 		// Fields on the UI to which we map

		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.image_row, null, from, to, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				if (view.getId() == R.id.icon) {
					String path = Images.getImageThumbsPath(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH)));
					ImageLoader.loadBitmap(path, (ImageView) view, false);
					return true; // true because the data was bound to the view
				}
				return false;
			}
		});

		setListAdapter(adapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		registerForContextMenu(getListView());
		
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}

		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			//showDetails(mCurCheckPosition,Long.valueOf(0));
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurCheckPosition);
	}
		
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		l.setItemChecked(position, true);
		showDetails(id);
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
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"	+ info.id);
			getActivity().getContentResolver().delete(uri, null, null);
			getLoaderManager().restartLoader(0, this.getArguments(), this);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails( Long id) {
		if (mDualPane) {
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.

			// Check what fragment is currently shown, replace if needed.
			ImageDetailsFragment details = (ImageDetailsFragment) getFragmentManager().findFragmentById(R.id.details);
			ParentMultiselectFragment parents = (ParentMultiselectFragment) getFragmentManager().findFragmentById(R.id.parent_select);
			
			if (details == null || details.getShownId() != id) {
					// Make new fragment to show this selection.
					details = ImageDetailsFragment.newInstance(id);
					parents = ParentMultiselectFragment.newInstance(id);
					// Execute a transaction, replacing any existing fragment
					// with this one inside the frame.
					FragmentTransaction ft = getFragmentManager().beginTransaction();
					ft.replace(R.id.details, details);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.replace(R.id.parent_select, parents);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				
			}

		} else {
			// Otherwise we need to launch a new activity to display the dialog fragment with selected text.
			Intent intent = new Intent();
			intent.setClass(getActivity(), ImageDetailsActivity.class);
			intent.putExtra("row_id", id);
			startActivity(intent);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		String[] projection = { 
				"i."+ImageContract.Columns._ID,
				"i."+ImageContract.Columns.PATH,
				"i."+ImageContract.Columns.CATEGORY,
				};
		Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/-1");
		CursorLoader cursorLoader = new CursorLoader(getActivity(),uri, projection, null, null, null);
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
