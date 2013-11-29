package com.example.app_2.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;
import com.example.app_2.widget.CheckableLinearLayout;

public class ParentMultiselectFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private SimpleCursorAdapter adapter;
	private ListView listView;
	
	boolean mDualPane;
	int mCurCheckPosition = 0;
	public static Long row_id;
	private static final int LOADER_ID = 32;
	private static final String TAG = "ParentMultiselectFragment";
	private Map<Long,Integer> posMapOfAllItems;
	private ArrayList<Long> selectedItemsOnCreate;
	
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
		Intent intent =  getActivity().getIntent();
		if(getArguments()!=null)
			row_id  = getArguments().getLong("row_id");
		if(bundle != null)
			row_id = bundle.getLong("row_id");
		else
			if(intent != null && row_id == null){
				Bundle b = intent.getExtras();
				if(b!=null)
					row_id = (Long)b.get("row_id");	
			}
	} 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());
		selectedItemsOnCreate = new ArrayList<Long>();
		
		String[] from = new String[] { ImageContract.Columns._ID,ImageContract.Columns.FILENAME, ImageContract.Columns.CATEGORY};
		int[] to = new int[] { 0, R.id.mc_icon, R.id.mc_text}; 		
		String[] projection3 = { 
				ImageContract.Columns._ID,
				ImageContract.Columns.FILENAME,
				ImageContract.Columns.CATEGORY
				};
        String selection2 = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs2 ={""};
		Cursor cursor2 = getActivity().getContentResolver().query(ImageContract.CONTENT_URI, projection3, selection2, selectionArgs2, null);
		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(),  R.layout.multiple_choice_item, cursor2, from, to, 0);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				if (view.getId() == R.id.mc_icon) {
					String path = Storage.getPathToScaledBitmap(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.FILENAME)),100);
					ImageLoader.loadBitmap(path, (ImageView) view, false);
					return true;
				}
				return false;
			}
		});
		
		setListAdapter(adapter);
		listView = getListView();
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);


		//wype³nienie - posMapOfAllItems id i pozycj¹ kategorii w liœcie
		posMapOfAllItems = new HashMap<Long, Integer>();
		SimpleCursorAdapter sca = (SimpleCursorAdapter) this.getListView().getAdapter();
		for(int i=0; i<sca.getCount(); i++)
			posMapOfAllItems.put(sca.getItemId(i), i);

		if(row_id != null){
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
				if(posMapOfAllItems.containsKey(lo)){
					int position = posMapOfAllItems.get(lo);
					selectedItemsOnCreate.add(lo);
					lv.setItemChecked(position, true);
				}
			}
			c.close();
		}

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
				ImageContract.Columns.FILENAME,
				ImageContract.Columns.CATEGORY
				};
        String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL AND ("+ImageContract.Columns.CATEGORY +" <> ?)";
        String[] selectionArgs ={""};
		CursorLoader cursorLoader = new CursorLoader(getActivity(),	ImageContract.CONTENT_URI, projection, selection, selectionArgs, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)  {
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	
	public  ArrayList<Long> getCheckedItemIds(){
	    ArrayList<Long> selectedItems = new ArrayList<Long>();
	    ListView lv = getListView();
        SparseBooleanArray checked = lv.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);// Item position in adapter
            if (checked.valueAt(i))
                selectedItems.add(adapter.getItemId(position));
        }
        return selectedItems;
	}
	
	public ArrayList<Long> getUncheckedItemsIds(ArrayList<Long> checkedItemIds){
		 ArrayList<Long> unSelectedItems  = new ArrayList<Long>();
		 for(Long l : selectedItemsOnCreate){
			 if(!checkedItemIds.contains(l))
				 unSelectedItems.add(l);
		 }
		 return unSelectedItems;
	}
}
