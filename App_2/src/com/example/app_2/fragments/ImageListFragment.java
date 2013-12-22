package com.example.app_2.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

public class ImageListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	SimpleCursorAdapter adapter;
	private EditText searchText;
	private ListView listView;
	private CheckBox onlyCategoriesCheckBox;
	private Spinner mSpinner;
	ArrayList<ImageSpinnerItem> items;
	private Long selected_user_id = null;
	
	private Long logged_user_id;
	
	boolean mDualPane;
	int mCurCheckPosition = 0;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int LOADER_ID = 0;
	private static final String LOG_TAG = "ImageListFragment";
	Long cat_id;
	
	
	/*
	String[] projection = { 
			"i."+ImageContract.Columns._ID,
			"i."+ImageContract.Columns.FILENAME,
			"i."+ImageContract.Columns.CATEGORY,
			"i."+ImageContract.Columns.DESC,
			"u."+UserContract.Columns.USERNAME
			};
	
	private TextWatcher filterTextWatcher= new TextWatcher(){
		public void afterTextChanged(Editable s){}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){	}
		public void onTextChanged(CharSequence s, int start, int before, int count){	adapter.getFilter().filter(s); }
	};
	
	private FilterQueryProvider fqp = new FilterQueryProvider() {
		@Override
		public Cursor runQuery(CharSequence constraint){
			String selection = "i."+ImageContract.Columns.FILENAME+" LIKE ? OR i."+ImageContract.Columns.DESC+" LIKE ? ";
			Log.d("imageListFragment", " runQuery constraint:"+constraint);
			String partialItemName = null;
			if(constraint != null){
				partialItemName = constraint.toString()+"%";
			}
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+cat_id);
			return getActivity().getContentResolver().query(uri, projection, selection, new String[]{partialItemName, partialItemName}, null);
		}
	};
	
	public ImageListFragment(){
		
	}
	
	*/
	
	private String[] projection = new String[] { "i."+ImageContract.Columns._ID,
			 "i."+ImageContract.Columns.FILENAME,
			 "i."+ImageContract.Columns.DESC,
			 "i."+ImageContract.Columns.CATEGORY,
			 "u."+UserContract.Columns.USERNAME};
	private String selection;
	private String[] selectionArgs;
	public String sortOrder;
	
	private boolean showOnlyCategories = false;
			
			
	private TextWatcher filterTextWatcher= new TextWatcher(){
		public void afterTextChanged(Editable s){}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){	}
		public void onTextChanged(CharSequence s, int start, int before, int count){	adapter.getFilter().filter(s); }
	};
	
	private FilterQueryProvider fqp = new FilterQueryProvider() {
		@Override
		public Cursor runQuery(CharSequence constraint){
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+cat_id);
			if(constraint != null && constraint.length()>0){
				String partialItemName = constraint.toString()+"%";
				String s = "(" + selection + " ) AND (i."+ImageContract.Columns.FILENAME+" LIKE ? OR i."+ImageContract.Columns.DESC+" LIKE ? )";// AND i."+ImageContract.Columns._ID+" <> "+executing_category_id;
				String [] sArgs = new String[5];
			    sArgs[0] = selectionArgs[0];
			    sArgs[1] = selectionArgs[1];
			    sArgs[2] = selectionArgs[2];
			    sArgs[3] = partialItemName;
			    sArgs[4] = partialItemName;			
				return getActivity().getContentResolver().query(uri, projection, s, sArgs, null);
			}else{
				return getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, null);
			}
		}
	};
	
	LoaderCallbacks<Cursor> lc = (LoaderCallbacks<Cursor>)this;
	
	private OnClickListener cb_clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showOnlyCategories = (((CheckBox)v).isChecked()) ? true : false;
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, lc);
						
		}
	};

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("USER",Context.MODE_PRIVATE);			// pobranie informacji o zalogowanym u¿ytkowniku
		logged_user_id = sharedPref.getLong("logged_user_id", 0);
		if(logged_user_id == 0)
			logged_user_id = null;
		
		if(logged_user_id == null){
			selection = "p."+ParentContract.Columns.PARENT_FK+" = ? " ;							// - obrazki które wskazuj¹ na s³ownik ( czyli wszystkie )
			selectionArgs= new String[]{Long.toString(Database.getMainDictFk())	};
		}else{
		
		selection = "p."+ParentContract.Columns.PARENT_FK+" = ? " 							// - obrazki które wskazuj¹ na s³ownik ( czyli wszystkie )
				+ " AND i." + ImageContract.Columns.AUTHOR_FK + "= ? "; 					// - w³aœcicielem jest zalogowany u¿ytkownik
	

		selectionArgs= new String[]{Long.toString(Database.getMainDictFk()),
								Long.toString(logged_user_id)
								};
		}
										
	};
	
	@Override
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View v = inflater.inflate(R.layout.image_list_fragment, container ,false);
		searchText = (EditText) v.findViewById(R.id.search_box);
		searchText.addTextChangedListener(filterTextWatcher);
		mSpinner = (Spinner) v.findViewById(R.id.user_select_spinner);
		onlyCategoriesCheckBox = (CheckBox) v.findViewById(R.id.show_only_categories_cb);
		onlyCategoriesCheckBox.setOnClickListener(cb_clickListener);
		addItemsOnUserSpinner();
		return v;
	};
	
	private void addItemsOnUserSpinner(){
		final LoaderCallbacks<Cursor> lc =  (LoaderCallbacks<Cursor>)this;
		final FragmentActivity a = getActivity();

		items =  new ArrayList<ImageSpinnerItem>();
		items.add(new ImageSpinnerItem(null,"Wszyscy u¿ytkownicy", null, true));
		String[] projection = {UserContract.Columns._ID, UserContract.Columns.IMG_FILENAME, UserContract.Columns.USERNAME };
	
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
		
		mSpinner.setSelection(0);
		if(logged_user_id !=null){
			int logged_user_pos_in_spinner = 0;
	
			for(  ImageSpinnerItem item : items){
				if(item.getItemId()== logged_user_id){
					mSpinner.setSelection(logged_user_pos_in_spinner);
					break;
				}
				else
					logged_user_pos_in_spinner++;	
			}
		}

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			Bundle bundle = new Bundle();
			
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				ImageSpinnerItem data = items.get(position);
				if(data.isHint()){		// wszyscy u¿ytkownicy
					TextView tv = (TextView)selectedItemView;
					if(tv!=null)
						tv.setTextColor(Color.rgb(148, 150, 148));
				}
								
				Long selection = data.getItemId();
				if(selected_user_id != selection){
					selected_user_id = selection;
					a.getSupportLoaderManager().restartLoader(0, null, lc);
				}
				//if(user_id != null){
				//	bundle.putLong("SELECTED_USER_ID", user_id);
				//	a.getSupportLoaderManager().restartLoader(0, bundle, lc);
				//}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				//bundle.putLong("SELECTED_USER_ID", logged_user);
				//a.getSupportLoaderManager().restartLoader(0, bundle, lc);
			}

		});
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new ImageLoader(getActivity());

		String[] from = new String[] {ImageContract.Columns._ID,  ImageContract.Columns.FILENAME,  ImageContract.Columns.DESC,  ImageContract.Columns.CATEGORY, UserContract.Columns.USERNAME};
		int[] to = new int[] { 0, R.id.mc_icon, R.id.mc_text, R.id.mc_dfs, R.id.mc_author }; 
		
		
		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.images_list_row, null, from, to, 0);
		adapter.setFilterQueryProvider(fqp);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				Long img_id = cursor.getLong(0);
				String path =  Storage.getPathToScaledBitmap(cursor.getString(1),100);
				String category = cursor.getString(3);
				boolean isCategory =  ( category != null && !category.isEmpty()) ? true : false;
				
				switch (view.getId()) {
				case R.id.mc_icon:
					ImageLoader.loadBitmap(path, (ImageView) view);
					LinearLayout ll = (LinearLayout)view.getParent();
					if(isCategory)
						ll.setBackgroundColor(Color.argb(120, 0, 255, 0));
					else
						ll.setBackgroundColor(Color.TRANSPARENT);
					return true;
				
				case R.id.mc_dfs:
					//TextView tv = (TextView) view;
					//if(isCategory)
					//	calculateDfsTask(img_id, tv);
					//else
					//	tv.setText("");
						
					return true;

				default:
					return false;
				}

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
		}
		
		

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		searchText.removeTextChangedListener(filterTextWatcher);
	};
	
	
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
		
		/*
		CursorLoader cursorLoader = null;
		Uri uri ;
		cat_id= Long.valueOf(-1);
		
		if(bundle!=null)
			cat_id = bundle.getLong("cat_id");


		uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+cat_id);
		cursorLoader = new CursorLoader(getActivity(),uri, projection, null, null, null);

		return cursorLoader;
		*/
		
		String sel = selection;
		String selArgs[] = selectionArgs;

		if(selected_user_id != null){
			sel = "p."+ParentContract.Columns.PARENT_FK+" = ? " 							// - obrazki które wskazuj¹ na s³ownik ( czyli wszystkie )
					+ " AND i." + ImageContract.Columns.AUTHOR_FK + "= ? "; 				// - w³aœcicielem jest zalogowany u¿ytkownik
			selArgs= new String[]{ Long.toString(Database.getMainDictFk()), Long.toString(selected_user_id)	};
		
			if(showOnlyCategories){
				sel += " AND ( " + ImageContract.Columns.CATEGORY + "<> ? OR " + ImageContract.Columns.CATEGORY + " IS NOT NULL )";
				selArgs = new String[]{ selArgs[0], selArgs[1], ""};
			}
		}else{
			sel = "p."+ParentContract.Columns.PARENT_FK+" = ? " ;							// - obrazki które wskazuj¹ na s³ownik ( czyli wszystkie )
			selArgs= new String[]{ Long.toString(Database.getMainDictFk())	};
		
			if(showOnlyCategories){
				sel += " AND ( " + ImageContract.Columns.CATEGORY + "<> ? OR " + ImageContract.Columns.CATEGORY + " IS NOT NULL )";
				selArgs = new String[]{ selArgs[0], ""};
			}	
		}

		CursorLoader cursorLoader = new CursorLoader(App_2.getAppContext(),	ImagesOfParentContract.CONTENT_URI, projection, sel, selArgs, sortOrder);
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
