package com.example.app_2.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
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
import android.widget.Toast;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.models.EdgeModel;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.AsyncTask;
import com.example.app_2.utils.DFS;
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
	
	private String[] projection = new String[] { 
			 "i."+ImageContract.Columns._ID, 					//0
			 "i."+ImageContract.Columns.FILENAME,				//1
			 "i."+ImageContract.Columns.DESC,					//2
			 "i."+ImageContract.Columns.CATEGORY,				//3
			 "i."+ImageContract.Columns.IS_CONTEXTUAL_CATEGORY,	//4
			 "u."+UserContract.Columns.USERNAME};				//5
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

		String[] from = new String[] {ImageContract.Columns._ID,  ImageContract.Columns.FILENAME,  ImageContract.Columns.DESC,  ImageContract.Columns.CATEGORY, UserContract.Columns.USERNAME, ImageContract.Columns.IS_CONTEXTUAL_CATEGORY};
		int[] to = new int[] { 0, R.id.mc_icon, R.id.mc_text, R.id.mc_dfs, R.id.mc_author, 0 }; 
		
		
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
					if(isCategory){				
						if(cursor.getInt(4) == 1)
							ll.setBackgroundColor(Color.argb(120, 149,39,225));
						else
							ll.setBackgroundColor(Color.argb(120, 0, 255, 0));
					}
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
		case DELETE_ID:		//TODO usuwanie z aplikacji obrazka
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			removeImageFromApp(info.id, true);
			
			//Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/"	+ info.id);
			//getActivity().getContentResolver().delete(uri, null, null);
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

	/***
	 * Usuwa obrazek z bazy danych i dysku, jeœli usuwana jest kategoria, usuwa podkategorie których nikt nie u¿ywa 
	 * @param l id usuwanego obrazka
	 * @return true - jeœli usuniêto, false - w przeciwnym wypadku
	*/
	private boolean removeImageFromApp(Long id_to_delete, boolean removeGraphics){	
		Long main_dict_fk = Database.getMainDictFk();
		removeParentsEdges(id_to_delete);

		if(isCategory(id_to_delete)){			
				DFS.getElements(id_to_delete);
				
				boolean isCategoryEmpty = (DFS.edges.size() == 0) ? true : false;	// sprawdzam czy kategoria jest pusta			
				if(isCategoryEmpty)												// jeœli tak usuwam kategoriê ( ustawiam dla obrazka null w polu kategorii )
					deleteImage(id_to_delete, removeGraphics);
				else{																// kategoria nie jest pusta		
					LinkedList<Long> dfs_list = DFS.visited;
					dfs_list.remove(id_to_delete);
					LinkedList<Long> imagesToDelete = filterNotUsedImages(dfs_list);
					imagesToDelete.add(id_to_delete);
					
					for(Long l : imagesToDelete){
						removeParentsEdges(l);
						removeChildEdges(l);
						deleteImage(l, removeGraphics);
					}
				}	
		}else{
			deleteImage(id_to_delete, removeGraphics);
		}

		return false;
	}
	
	private LinkedList<Long> filterNotUsedImages(LinkedList<Long> dfs_list){
		LinkedList<Long> inSet = new LinkedList<Long>();
		
		for(Long e : dfs_list){
			LinkedList<Long> ll = getParents(e);
			ll.remove(Database.getMainDictFk());
			ll.removeAll(dfs_list);
			if(ll.size() == 0){
				inSet.add(e);
			}
		}
		return inSet;
	}

	/**
	 * Usuwa obrazek z bazy i grafikê z dysku, nie usuwa wi¹zañ!
	 * @param l - id pustej kategorii
	 * @return true - operacja siê powiod³a, false - wyst¹pi³ b³¹d
	 */
	private boolean deleteImage(Long l, boolean removeGraphics){
		String filename= null;
		Uri cat_uri = Uri.parse(ImageContract.CONTENT_URI + "/" + l);
		
		if(removeGraphics){
			// pobieram nazê pliku z bazy dla kategorii
			Cursor c = getActivity().getContentResolver().query(cat_uri, new String[]{ImageContract.Columns.FILENAME}, null, null, null);
			if(c!=null){
				c.moveToFirst();
				if(!c.isAfterLast())
					filename = c.getString(0);
				c.close();
			}
	
			if(filename != null){
				ArrayList<String> filesToDelete = new ArrayList<String>();
				filesToDelete.add(filename);
				RemoveFilesTask rft = new RemoveFilesTask();
				rft.execute(filesToDelete);
			}			
		}

		
		int row_deleted = getActivity().getContentResolver().delete(cat_uri,  null, null);
		if(row_deleted == 1 && filename !=null)		// TODO poprawiæ spr
			return true;
		else
			return false;
	}
	
	/***
	 * Usuwa obrazek o podanym id ze wszyskich kategorii w których by³ dostêpny, tak¿e ze s³ownika
	 * @param l - id usuwanego obrazka
	 * @return void
	 */
	private void removeParentsEdges(Long l){
		LinkedList<Long> parentsOfSelectedImage = new LinkedList<Long>();				//sprawdzam gdzie dostêpna jest kategoria
		Uri parents_of_image_uri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+l);
		Cursor parents_cursor= getActivity().getContentResolver().query(parents_of_image_uri, new String[]{"p."+ParentContract.Columns.PARENT_FK}, null, null, null);
		if(parents_cursor!=null){
			parents_cursor.moveToFirst();
			while(!parents_cursor.isAfterLast()){
				parentsOfSelectedImage.add(parents_cursor.getLong(0));
				parents_cursor.moveToNext();
			}
			parents_cursor.close();
		}
		
		int countUsingCategories = parentsOfSelectedImage.size();
		Log.i(LOG_TAG, "Usuwany obrazek jest u¿ywany w: "+ (countUsingCategories - 1) +" kategoriach");
		
		if(countUsingCategories <= 0){
			return;
		}
		
		String selection = ParentContract.Columns.IMAGE_FK+" = " + l;
		String projection[] = {ParentContract.Columns._ID};
		LinkedList<Long> imageToParentEdgesId = new LinkedList<Long>();
		
		for(Long parentId : parentsOfSelectedImage){
			selection +=" AND " +ParentContract.Columns.PARENT_FK+ " = ? ";
			Cursor c = getActivity().getContentResolver().query(ParentContract.CONTENT_URI, projection , selection, new String[]{String.valueOf(parentId)}, null);
			if(c != null){
				c.moveToFirst();
				while(!c.isAfterLast()){
					imageToParentEdgesId.add(c.getLong(0));
					c.moveToNext();
				}
				c.close();
			}
		}
		
		for(Long id : imageToParentEdgesId){
			Uri uri = Uri.parse(ParentContract.CONTENT_URI+"/"+id);
			getActivity().getContentResolver().delete(uri,null, null);
		}
	}
	
	/***
	 * Usuwa wi¹zania elementów na kategoriê l
	 * @param l - id kategorii z kórej wi¹zania usun¹æ
	 * @return lista obrazków które by³y w kategorii
	 */
	private LinkedList<Long> removeChildEdges(Long l){
		LinkedList<Long> childsOfSelectedImage = new LinkedList<Long>();
		Uri childs_of_image_uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+l);
		Cursor childs_cursor = getActivity().getContentResolver().query(childs_of_image_uri, new String[]{"p."+ParentContract.Columns.IMAGE_FK} ,null, null, null);
		if(childs_cursor != null){
			childs_cursor.moveToFirst();
			while(!childs_cursor.isAfterLast()){
				childsOfSelectedImage.add(childs_cursor.getLong(0));
				childs_cursor.moveToNext();
			}
			childs_cursor.close();
		}
		
		int countImagesInCategory = childsOfSelectedImage.size();
		Log.i(LOG_TAG, "Usuwana kategoria zawiera: " + countImagesInCategory+" obrazków");
		
		if(countImagesInCategory <=0){
			return childsOfSelectedImage;
		}

		String selection = ParentContract.Columns.PARENT_FK+" = " + l;
		String projection[] = {ParentContract.Columns._ID};
		LinkedList<Long> imageToChildEdgesId = new LinkedList<Long>();
		
		for(Long child_id : childsOfSelectedImage){
			selection +=" AND " +ParentContract.Columns.IMAGE_FK+ " = ? ";
			Cursor c = getActivity().getContentResolver().query(ParentContract.CONTENT_URI, projection , selection, new String[]{String.valueOf(child_id)}, null);
			if(c != null){
				c.moveToFirst();
				while(!c.isAfterLast()){
					imageToChildEdgesId.add(c.getLong(0));
					c.moveToNext();
				}
				c.close();
			}
		}
		
		for(Long id : childsOfSelectedImage ){
			Uri uri = Uri.parse(ParentContract.CONTENT_URI+"/"+id);
			getActivity().getContentResolver().delete(uri,null, null);
		}
		return childsOfSelectedImage;
	}
	
	private LinkedList<Long> getParents(Long image_id){
		LinkedList<Long> parentsOfSelectedImage = new LinkedList<Long>();
		Uri parents_of_image_uri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+image_id);
		Cursor parents_cursor= getActivity().getContentResolver().query(parents_of_image_uri, new String[]{"p."+ParentContract.Columns.PARENT_FK}, null, null, null);
		if(parents_cursor!=null){
			parents_cursor.moveToFirst();
			while(!parents_cursor.isAfterLast()){
				parentsOfSelectedImage.add(parents_cursor.getLong(0));
				parents_cursor.moveToNext();
			}
			parents_cursor.close();
		}
		return parentsOfSelectedImage;
	}
	
	/***
	 * Sprawdza czy obrazek o podanym id jest kategori¹
	 * @param l - id obrazka
	 * @return true - jeœli jest kategori¹, false jeœli nie jest
	 */
	private boolean isCategory(Long l){
		Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + l);			
		Cursor c =getActivity().getContentResolver().query(uri, new String[]{ImageContract.Columns.CATEGORY}, null, null, null);
		if(c!= null){
			c.moveToFirst();
			String filename = c.getString(0);
			c.close();
			if(filename!= null)
				return true;
		}
		return false;
	}
	
	
	public static class RemoveFilesTask extends AsyncTask<ArrayList<String>, String, Void>{
		@Override
		protected Void doInBackground(ArrayList<String>... params) {
			ArrayList<String> filenames = params[0];
			for(int scale : Storage.scaleTab){
				String pathToDir = Storage.getScaledThumbsDir(Integer.toString(scale), false).getAbsolutePath() + File.separator;
				for(String filename : filenames){
					File fileToRemove = new File( pathToDir + filename);
					if(fileToRemove != null && fileToRemove.exists())
						if(fileToRemove.delete())
							Log.i(LOG_TAG, filename + " - removed" );
						else
							Log.w(LOG_TAG, filename + " - not removed");
					else
						Log.w(LOG_TAG, filename + " not exist");
				}
			}
			return null;
		}
	}
}
