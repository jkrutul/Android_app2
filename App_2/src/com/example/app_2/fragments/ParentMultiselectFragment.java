package com.example.app_2.fragments;

import java.util.ArrayList;

import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ParentMultiselectFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	SimpleCursorAdapter adapter;
	boolean mDualPane;
	int mCurCheckPosition = 0;
	public static Long row_id;
	private static final int LOADER_ID = 32;
	private static final String TAG = "ParentMultiselectFragment";
	
	public ParentMultiselectFragment(){
		
	}
	
	public static ParentMultiselectFragment newInstance(Long id){
		ParentMultiselectFragment f = new ParentMultiselectFragment();
		Bundle args = new Bundle();
		args.putLong("row_id", id);
		f.setArguments(args);
		return f;
	}

	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
		
		row_id = (bundle ==null) ? null : (Long) bundle.getLong("row_id");
		if(row_id == null){
			
		}
	} 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());

		String[] from = new String[] { ImageContract.Columns._ID,ImageContract.Columns.CATEGORY};
		int[] to = new int[] { 0, android.R.id.text1}; 		// Fields on the UI to which we map

		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, null, from, to, 0);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				if (view.getId() == R.id.icon) {
					String path = Images.getImageThumbsPath(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH)));
					ImageLoader.loadBitmap(path, (ImageView) view, false);
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);


	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putInt("curChoice", mCurCheckPosition);
	}
		
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		String[] projection = { 
				ImageContract.Columns._ID,
				ImageContract.Columns.PATH,
				ImageContract.Columns.CATEGORY,
				ImageContract.Columns.PARENT
				};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
		CursorLoader cursorLoader = new CursorLoader(getActivity(),	ImageContract.CONTENT_URI, projection, selection, selectionArgs, null);
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

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id){
		 CheckedTextView check = (CheckedTextView)view;
	     check.setChecked(!check.isChecked());
	    
	}
	
	public  ArrayList<Long> getCheckedItemIds(){
		//BiMap<String,Long> map = new BiMap
        ArrayList<Long> selectedItems = new ArrayList<Long>();
        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);// Item position in adapter
            if (checked.valueAt(i)) 		
                selectedItems.add(adapter.getItemId(position));
        }
        return selectedItems;
	}
}
