package com.example.app_2.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import  android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

public class ImagesMultiselectFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private final static String LOG_TAG = "ImageMultiselectFragment";
	private SimpleCursorAdapter adapter;
	private ListView listView;
	private EditText searchText;
	Spinner mSpinner;
	ArrayList<ImageSpinnerItem> items;
	//private List<Item> data;
	
	boolean mDualPane;
	int mCurCheckPosition = 0;
	public static Long row_id;
	private static final int LOADER_ID = 32;
	private static final String TAG = "ParentMultiselectFragment";
	private Map<Long,Integer> posMapOfAllItems;
	private ArrayList<Long> selectedItemsOnCreate;
	private Long cat_id;
	private Long logged_user_id, logged_user_root ;
	
	String[] projection = new String[] { "i."+ImageContract.Columns._ID,
			 "i."+ImageContract.Columns.FILENAME,
			 "i."+ImageContract.Columns.DESC,
			 "i."+ImageContract.Columns.CATEGORY,
			 "u."+UserContract.Columns.USERNAME};	
	private TextWatcher filterTextWatcher= new TextWatcher(){
		public void afterTextChanged(Editable s){}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){	}
		public void onTextChanged(CharSequence s, int start, int before, int count){	adapter.getFilter().filter(s); }
	};
	
	private FilterQueryProvider fqp = new FilterQueryProvider() {
		@Override
		public Cursor runQuery(CharSequence constraint){
			String selection = "i."+ImageContract.Columns.FILENAME+" LIKE ? OR i."+ImageContract.Columns.DESC+" LIKE ? ";
			Log.d( LOG_TAG," runQuery constraint:"+constraint);
			String partialItemName = null;
			if(constraint != null){
				partialItemName = constraint.toString()+"%";
			}
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+cat_id);
			return getActivity().getContentResolver().query(uri, projection, selection, new String[]{partialItemName, partialItemName}, null);
		}
	};
	
	
	public ImagesMultiselectFragment(){	}
	/*
	public static ParentMultiselectFragment newInstance(Long id){
		ParentMultiselectFragment f = new ParentMultiselectFragment();
		Bundle args = new Bundle();
		args.putLong("row_id", id);
		f.setArguments(args);
		return f;
	}
	 */
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		SharedPreferences sharedPref = getActivity().getSharedPreferences("USER",Context.MODE_PRIVATE);
		logged_user_root = sharedPref.getLong("logged_user_root", Database.getMainRootFk());
		logged_user_id = sharedPref.getLong("logged_user_id", 0);
		
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
		
		String[] from = new String[] {ImageContract.Columns._ID,  ImageContract.Columns.FILENAME,  ImageContract.Columns.DESC,  ImageContract.Columns.CATEGORY, UserContract.Columns.USERNAME};
		int[] to = new int[] { 0, R.id.mc_icon, R.id.mc_text, 0, R.id.mc_author }; 		
		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.multiple_choice_item, null, from, to, 0);
		adapter.setFilterQueryProvider(fqp);
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
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View v = inflater.inflate(R.layout.image_list_fragment, container ,false);
		searchText = (EditText) v.findViewById(R.id.search_box);
		searchText.addTextChangedListener(filterTextWatcher);
		mSpinner = (Spinner) v.findViewById(R.id.user_select_spinner);
		addItemsOnUserSpinner();
		
		return v;
	};
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putInt("curChoice", mCurCheckPosition);
	}

	public ArrayList<Long> getCheckedItemIds(){
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
	
	private void addItemsOnUserSpinner(){
		final LoaderCallbacks<Cursor> lc =  (LoaderCallbacks<Cursor>)this;
		final FragmentActivity a = getActivity();

		
		items =  new ArrayList<ImageSpinnerItem>();
		items.add(new ImageSpinnerItem(null,"Wybierz u¿ytkownika", null, true));
		String[] projection = {UserContract.Columns._ID, UserContract.Columns.IMG_FILENAME, UserContract.Columns.USERNAME };
		//String selection = ImageContract.Columns.CATEGORY + " IS NOT NULL";
		
		Cursor c = a.getContentResolver().query(UserContract.CONTENT_URI, projection, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			items.add(new ImageSpinnerItem(c.getString(1), c.getString(2), c.getLong(0),false));
			c.moveToNext();
		}
		c.close();
		
		ImageSpinnerAdapter mySpinnerAdapter = new ImageSpinnerAdapter(a, android.R.layout.simple_spinner_item, items);
		mySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(mySpinnerAdapter);
		int logged_user_pos_in_spinner = 0;
		
		if(logged_user_id != null && logged_user_id != 0){
			for(  ImageSpinnerItem item : items)
				if(item.getItemId()== logged_user_id)
					break;
				else
					logged_user_pos_in_spinner++;
		}
		mSpinner.setSelection(logged_user_pos_in_spinner);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			Bundle bundle = new Bundle();
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				ImageSpinnerItem data = items.get(position);
				if(data.isHint()){
					TextView tv = (TextView)selectedItemView;
					if(tv!=null)
						tv.setTextColor(Color.rgb(148, 150, 148));
				}
				Long user_id = data.getItemId();
				if(user_id != null)
					bundle.putLong("USER_ID", user_id);
				//bundle.putLong("CATEGORY_ID", data.getItemId());
				getLoaderManager().restartLoader(0, bundle, lc);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				bundle.putLong("USER_ID", -1);
				a.getSupportLoaderManager().restartLoader(0, bundle, lc);
			}

		});
	}
		
	//		---- L	O	A	D	E	R	----
	@SuppressLint("NewApi")
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		String selection = "p."+ParentContract.Columns.PARENT_FK+" =? ";
		Long user_id = null;	
		String[] selectionArgs= { Long.toString(Database.getMainDictFk())};
		if(bundle != null)
			user_id = bundle.getLong("USER_ID", -1);

		if(user_id != null && user_id !=-1){
			selection += " AND i." + ImageContract.Columns.AUTHOR_FK + "= ?";
			selectionArgs = new String[]{selectionArgs[0],user_id.toString()};
		}
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(),	ImagesOfParentContract.CONTENT_URI, projection, selection, selectionArgs, null);
		
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

}
