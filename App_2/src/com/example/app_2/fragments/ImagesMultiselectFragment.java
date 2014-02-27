package com.example.app_2.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.spinner.adapter.ImageSpinnerAdapter;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.DFS;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.widget.CheckableLinearLayout;

public class ImagesMultiselectFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private final static String LOG_TAG = "ImageMultiselectFragment";
	private SimpleCursorAdapter adapter;
	private ListView listView;
	private EditText searchText;
	public CheckBox onlyCategoriesCheckBox;
	private Spinner mSpinner;
	private ArrayList<ImageSpinnerItem> items;
	private ArrayList<Long> imagesInExecutingCategory = new ArrayList<Long>();
	private int mCategoryBackgroundColor, mContextCategoryBackgroundColor;

		
	int mCurCheckPosition = 0;
	public static Long row_id;
	private static final int LOADER_ID = 32;

	private Long selected_user_id = null;
	boolean viewOnlyCategories = false; 
	boolean viewUserRootCategories = false;

	private Long executing_category_id;
	private Long executing_category_author;
	
	private String[] projection = new String[] { 
			"i."+ImageContract.Columns._ID,
			 "i."+ImageContract.Columns.FILENAME,
			 "i."+ImageContract.Columns.DESC,
			 "i."+ImageContract.Columns.IS_CATEGORY,
			 "i."+ImageContract.Columns.IS_ADD_TO_EXPR,
			 "u."+UserContract.Columns.USERNAME};

	public String sortOrder;
	private boolean showOnlyCategories = false;
	private String constraint;
			
			
	private TextWatcher filterTextWatcher= new TextWatcher(){
		public void afterTextChanged(Editable s){}
		public void beforeTextChanged(CharSequence s, int start, int count, int after){	}
		public void onTextChanged(CharSequence s, int start, int before, int count){
			constraint = s.toString();
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, lc);
			//adapter.getFilter().filter(s); 
			}
	};

	LoaderCallbacks<Cursor> lc = (LoaderCallbacks<Cursor>)this;
	
	private OnCheckedChangeListener cb_checkChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			showOnlyCategories = isChecked;
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, lc);
			
		}
	};
	
	private OnClickListener cb_clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showOnlyCategories = (((CheckBox)v).isChecked()) ? true : false;
			getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, lc);
						
		}
	};
	
	public ImagesMultiselectFragment(){	}
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
	    Bundle args = getArguments();
	    if(args!=null){
			executing_category_id = args.getLong("category_id");
			viewOnlyCategories = args.getBoolean("viewOnlyCategories");
			viewUserRootCategories = args.getBoolean("viewUserRootCategories");
	    }
	    

		

		executing_category_author = getCategoryAuthor(executing_category_id);
		
		
		

		
		getImageIdsFromCategory(executing_category_id);										// pobieram listê obrazków bêd¹cych ju¿ w kategorii
		
	} 
	
	@Override
	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		final View v = inflater.inflate(R.layout.image_list_fragment, container ,false);
		searchText = (EditText) v.findViewById(R.id.search_box);
		searchText.addTextChangedListener(filterTextWatcher);
		mSpinner = (Spinner) v.findViewById(R.id.user_select_spinner);
		onlyCategoriesCheckBox = (CheckBox) v.findViewById(R.id.show_only_categories_cb);
		onlyCategoriesCheckBox.setOnCheckedChangeListener(cb_checkChangeListener);
		if(viewOnlyCategories){
			onlyCategoriesCheckBox.setChecked(true);
			onlyCategoriesCheckBox.setEnabled(false);
			
			//onlyCategoriesCheckBox.setVisibility(View.INVISIBLE);
		}
		
		
		addItemsOnUserSpinner();
		return v;
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mCategoryBackgroundColor =sharedPref.getInt("category_view_background", 0xff33b5e5);
		mContextCategoryBackgroundColor =sharedPref.getInt("context_category_view_background",0xffe446ff);

		
		String[] from = new String[] {
				ImageContract.Columns._ID,
				ImageContract.Columns.FILENAME,
				ImageContract.Columns.DESC,
				ImageContract.Columns.IS_CATEGORY,
				ImageContract.Columns.IS_ADD_TO_EXPR,
				UserContract.Columns.USERNAME
				};
		int[] to = new int[] {0, R.id.mc_icon, R.id.mc_text, R.id.mc_info, 0,R.id.mc_author }; 
		
		listView = getListView();
		adapter = new SimpleCursorAdapter( getActivity().getApplicationContext(), R.layout.multiple_choice_item, null, from, to, 0);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		
		//adapter.setFilterQueryProvider(fqp);
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor,int columnIndex) {
				Long img_id = cursor.getLong(0);
				String path =  Storage.getPathToScaledBitmap(cursor.getString(1),100);
				int isCategory = cursor.getInt(3);
				
				switch (view.getId()) {
				case R.id.mc_icon:
					ImageLoader.loadBitmap(path, (ImageView) view,100);
					//CheckableLinearLayout cll = (CheckableLinearLayout)view.getParent();
					if(isCategory==1){
						int isAddToExpr = cursor.getInt(4);
						if(isAddToExpr == 1)
							view.setBackgroundColor(mContextCategoryBackgroundColor);
						else
							view.setBackgroundColor(mCategoryBackgroundColor);
					}else
						view.setBackgroundColor(Color.TRANSPARENT);
					return true;
				/*
				case R.id.mc_dfs:
					//TextView tv = (TextView) view;
					//if(isCategory)
					//	calculateDfsTask(img_id, tv);
					//else
					//	tv.setText("");
						
					return true;
					*/
					
				case R.id.mc_info:
					TextView tv = (TextView) view;
					CheckableLinearLayout cl  = (CheckableLinearLayout) view.getParent().getParent().getParent();
					if(imagesInExecutingCategory.contains(img_id)){
						tv.setText("Jest w kategorii");
						cl.setClickable(true);
					}
					else{
						tv.setText("");
						cl.setClickable(false);
						}
					return true;
								
					

				default:
					return false;
				}

			}
		});
		
		setListAdapter(adapter);		
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

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
		int owner_pos_in_spinner = 0;

		for(  ImageSpinnerItem item : items)
			if(item.getItemId()== executing_category_author)
				break;
			else
				owner_pos_in_spinner++;
		
		mSpinner.setSelection(owner_pos_in_spinner);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			//Bundle bundle = new Bundle();
			
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				ImageSpinnerItem data = items.get(position);
				if(data.isHint()){				// wszyscy u¿ytkownicy
					TextView tv = (TextView)selectedItemView;
					if(tv!=null)
						tv.setTextColor(Color.rgb(148, 150, 148));
				}
								

				if(selected_user_id != data.getItemId()){
					selected_user_id = data.getItemId();
					a.getSupportLoaderManager().restartLoader(0, null, lc);
				}

			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				//bundle.putLong("SELECTED_USER_ID", executing_category_author);
				selected_user_id = null;
				a.getSupportLoaderManager().restartLoader(0, null, lc);
			}

		});
	}
	
	/**
	 * 
	 * @param category_fk
	 * @return category author id or null
	 */
	private Long getCategoryAuthor(Long category_fk){
		if(category_fk == null)
			return null;
		
		Long author_id = null ;
		 Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + executing_category_id);
		 Cursor c = getActivity().getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns.AUTHOR_FK}, null, null, null);
		 c.moveToFirst();
		 if(!c.isAfterLast())
			 author_id = c.getLong(0);
		 
		return author_id;
	}
	
	private void calculateDfsTask(Long cat_root, TextView tv){
		if(cat_root == null || tv == null){
			return;
		}

		
		CalculateDfsWorkerTask task = new CalculateDfsWorkerTask(tv);
		task.executeOnExecutor(com.example.app_2.utils.AsyncTask.DUAL_THREAD_EXECUTOR, cat_root);
		
	}

	private static  CalculateDfsWorkerTask getCalculateDfsWorkerTask(TextView textView) {

		    return null;
		}
	
	
	private static class CalculateDfsWorkerTask extends AsyncTask<Long, Void, Integer>{
		private final WeakReference<TextView> textViewReference;
		private Long cat_root = null;
		
		public CalculateDfsWorkerTask(TextView tv) {
			textViewReference = new WeakReference<TextView>(tv);
		}

		@Override
		protected Integer doInBackground(Long... params) {
			cat_root = params[0];
			int count = DFS.getElements(cat_root);
			return count-1;
		}
		
		@Override
		protected void onPostExecute(Integer count){
			if(textViewReference != null && count != null){
					final TextView textView = textViewReference.get();
					textView.setText("zawiera: "+ count + " elementów");	
			}
		}
	}

	
	
	private void getImageIdsFromCategory(Long category_id){
		if(executing_category_id != null){
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI+"/"+category_id);
			Cursor c= getActivity().getContentResolver().query(uri, new String[]{"i."+ImageContract.Columns._ID}, null, null, null);
			c.moveToFirst();
			while(!c.isAfterLast()){
				imagesInExecutingCategory.add(c.getLong(0));
				c.moveToNext();
			}
			c.close();
		}
	}
	
	
	//		---- L	O	A	D	E	R	----
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		String selection = new String();
		LinkedHashMap<String,String> selArgs = new LinkedHashMap<String, String>();
		
		
		
		if(!viewUserRootCategories){
			selection = "p."+ParentContract.Columns.PARENT_FK+" = ? AND"; 							// - obrazki które wskazuj¹ na s³ownik ( czyli wszystkie )
			selArgs.put("PARENT_FK", Long.toString(Database.getMainDictFk()));
		}

		if(executing_category_id!=null){													// - usuñ mo¿liwoœæ tworzenia pêtli prostych na kategori¹ z której wywo³ujemy dodawanie obrazka
			selection+= " i." + ImageContract.Columns._ID + "<> ? ";
			selArgs.put("_ID",  Long.toString(executing_category_id));
		}
		
		if(selected_user_id!= null){
			selection += " AND i." + ImageContract.Columns.AUTHOR_FK + "= ? "; 					// - w³aœcicielem jest autor kategorii do której dodajemy obrazki
			selArgs.put("AUTHOR_FK", Long.toString(selected_user_id));
		}


		
		
		if(constraint!=null && !constraint.isEmpty()){
			selection = "(" + selection + " ) " +
				"AND (i."+ImageContract.Columns.FILENAME+" LIKE ?" +
				" OR i."+ImageContract.Columns.DESC+" LIKE ?" +
				" OR i."+ImageContract.Columns.TTS_M+" LIKE ?" +
				" OR i."+ImageContract.Columns.TTS_F+" LIKE ?)";
			
			selArgs.put("FILENAME", constraint+"%");
			selArgs.put("DESC", constraint+"%");
			selArgs.put("TTS_M",  constraint+"%");
			selArgs.put("TTS_F", constraint+"%"
					);
		}
		
		


		//if(selected_user_id != null)
		//	selArgs.put("AUTHOR_FK", Long.toString(selected_user_id));
		
		
		
		if(showOnlyCategories){
			selection += " AND ( i." + ImageContract.Columns.IS_CATEGORY + "= ?)";
			selArgs.put("SHOW_ONLY_CATEGORIES", "1");
		}
		
		
		String [] selectionArguments = selArgs.values().toArray(new String [selArgs.size()]);
		//String [] selectionArguments = (String[]) selArgs.values().toArray();

		CursorLoader cursorLoader = new CursorLoader(getActivity(),	ImagesOfParentContract.CONTENT_URI, projection, selection, selectionArguments, sortOrder);
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
