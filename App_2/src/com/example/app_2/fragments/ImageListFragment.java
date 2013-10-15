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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;
import com.examples.app_2.activities.ImageDetailActivity;
import com.examples.app_2.activities.ImageDetailsActivity;
import com.examples.app_2.activities.ImageGridActivity;

public class ImageListFragment extends ListFragment implements
		AdapterView.OnItemClickListener, LoaderCallbacks<Cursor> {
	SimpleCursorAdapter adapter;
	boolean mDualPane;
	int mCurCheckPosition = 0;
	
	public ImageListFragment(){
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());

		String[] from = new String[] { ImageContract.Columns._ID,
				ImageContract.Columns.PATH, ImageContract.Columns.PATH,
				ImageContract.Columns.CATEGORY, ImageContract.Columns.PARENT };
		// Fields on the UI to which we map
		int[] to = new int[] { 0, R.id.label, R.id.icon, R.id.category,	R.id.perent };
		getActivity().getSupportLoaderManager().initLoader(0, null, this);

		Cursor c = getActivity().getContentResolver().query(
				ImageContract.CONTENT_URI, from, null, null, null);
		if (c.getCount() <= 0) {
			c.close();
		}

		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.image_row, c, from, to, 0);

		getLoaderManager().restartLoader(0, this.getArguments(), this);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (view.getId() == R.id.icon) {
					String path = Images.getImageThumbsPath(cursor.getString(cursor
							.getColumnIndex(ImageContract.Columns.PATH)));
					ImageLoader.loadBitmap(path, (ImageView) view);
					return true; // true because the data was bound to the view
				}
				return false;
			}
		});

		setListAdapter(adapter);

		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("COL_CATEGORY", -1);
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
		showDetails(position, id);
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails(int index, Long id) {
		mCurCheckPosition = index;
		Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		

		if (mDualPane) {
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			getListView().setItemChecked(index, true);

			// Check what fragment is currently shown, replace if needed.
			ImageDetailsFragment details = (ImageDetailsFragment) getFragmentManager().findFragmentById(R.id.details);
			if (details == null || details.getShownIndex() != index) {
				// Make new fragment to show this selection.
				//details = ImageDetailsFragment.newInstance(index);
				details = ImageDetailsFragment.newInstance(id);

				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (index == 0) {
					ft.replace(R.id.details, details);
				} else {
					ft.replace(R.id.details, details);
				}
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}

		} else {
			// Otherwise we need to launch a new activity to display
			// the dialog fragment with selected text.
			Intent intent = new Intent();
			intent.setClass(getActivity(), ImageDetailsActivity.class);
			intent.putExtra("index", index);
			startActivity(intent);
		}
	}

	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View v = inflater.inflate(R.layout.image_list, container, false);
		return v;
	}
*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		String[] projection = { ImageContract.Columns._ID,
				ImageContract.Columns.PATH, ImageContract.Columns.CATEGORY,
				ImageContract.Columns.PARENT };
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				ImageContract.CONTENT_URI, projection, null, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}
}
