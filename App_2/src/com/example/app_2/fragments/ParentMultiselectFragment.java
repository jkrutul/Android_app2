package com.example.app_2.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContentProvider;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;

public class ParentMultiselectFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	SimpleCursorAdapter adapter;
	boolean mDualPane;
	int mCurCheckPosition = 0;
	public static Long row_id;
	private static final int LOADER_ID = 32;
	private static final String TAG = "ParentMultiselectFragment";
	private Map<Long,Integer> positionMap;
	
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
		
		row_id = (Long) getActivity().getIntent().getExtras().get("row_id");
		//if(row_id == null)
		//	row_id = (bundle ==null) ? null : (Long) bundle.getLong("row_id");
		//if(row_id != null){
			
		//}
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
		
		Uri uri  = Uri.parse(ParentsOfImageContract.CONTENT_URI + "/" + row_id);
		String[] projection2 = { "i."+ImageContract.Columns._ID};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
		Cursor cur = getActivity().getContentResolver().query(uri, projection2, selection, selectionArgs, null);
		positionMap = new HashMap<Long, Integer>();
		cur.moveToFirst();
		for(int i = 0 ; !cur.isAfterLast(); i++){
			positionMap.put(Long.valueOf(cur.getString(cur.getColumnIndex(ImageContract.Columns._ID))), i);
			cur.moveToNext();
		}
		cur.close();

		// zaznaczenie kategorii do których nale¿a³ wczeœniej obrazek
		
		Uri imageUri = Uri.parse(ParentsOfImageContract.CONTENT_URI + "/" + row_id);
		String [] projection ={"p."+ParentContract.Columns.PARENT_FK};
		Cursor c= getActivity().getContentResolver().query(imageUri, projection , null, null, null);
		c.moveToFirst();
		ListView lv = getListView();
		while(!c.isAfterLast()){
			String l = c.getString(c.getColumnIndex(ParentContract.Columns.PARENT_FK));
			c.moveToNext();
			Long lo = Long.valueOf(l);
			int position = positionMap.get(lo);
			 getListView().setItemChecked(position, true);
			 //(positionMap.get(Long.valueOf(l)));;			
		}
		c.close();


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
				ImageContract.Columns.CATEGORY
				};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
		CursorLoader cursorLoader = new CursorLoader(getActivity(),	ImageContract.CONTENT_URI, projection, selection, selectionArgs, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)  {
		((SimpleCursorAdapter)this.getListAdapter()).swapCursor(cursor);
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
