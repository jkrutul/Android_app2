package com.example.app_2.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.actionbar.model.IdPositionModel;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.models.EdgeModel;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.DFS;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

@SuppressLint("NewApi")
public class ImageGridFragment extends Fragment implements LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = ImageGridActivity.GRID_FRAGMENT_TAG;
    private ImageView expandedImageView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mCategoryBackgroundColor;
    private int mContextCategoryBackgroundColor;
	private int mImageFontSize = 20;
    private SimpleCursorAdapter adapter;
    private ArrayList<Long> selected_images_ids = new ArrayList<Long>();
    private Animator mCurrentAnimator;
    public  GridView mGridView;
    private RelativeLayout.LayoutParams mImageViewLayoutParams;
    private int mItemHeight = 0;
    private boolean mChangeNumColumns = false;
    private boolean restartLoader = false;
    
	private static final int LOADER_ID = 1;
	
	public boolean mEditMode = true;

	private  ImageGridActivity executingActivity;
	private  SharedPreferences sharedPref; 
	
	private static final int dev_h = App_2.getMaxHeight();
	private static final int dev_w = App_2.getMaxWidth();
	
	private static ActionMode mActionMode = null;
	
	public static String sortOrder ="i."+ImageContract.Columns.TIME_USED + " DESC";
	
	private static String[] from = new String[] {  ImageContract.Columns.FILENAME,   ImageContract.Columns.DESC	};
	private static int[] to = new int[] {R.id.recycling_image, R.id.image_desc };

	private static String[] loader_projection = new String[] { "i."+ImageContract.Columns._ID,						//0
															   "i."+ImageContract.Columns.FILENAME,					//1
															   "i."+ImageContract.Columns.DESC,						//2
															   "i."+ImageContract.Columns.CATEGORY,					//3
															   "i."+ImageContract.Columns.IS_CONTEXTUAL_CATEGORY,	//4
														       "i."+ImageContract.Columns.MODIFIED,					//5
															   "i."+ImageContract.Columns.TIME_USED};				//6
	
	private OnItemLongClickListener ilcL = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, final  View thumbView, int position, long i) {
    		Cursor c = (Cursor) adapter.getItem(position);						
   			if(expandedImageView!=null){
   				expandedImageView.bringToFront();
   				 String path = Storage.getPathToScaledBitmap(c.getString(1), dev_w);
   		         Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, App_2.getMaxWidth(), App_2.getMaxHeight(), ScalingLogic.FIT);
   		         Bitmap bitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, App_2.getMaxWidth(), App_2.getMaxHeight(), ScalingLogic.FIT);
   		         unscaledBitmap.recycle();
   		         expandedImageView.setImageBitmap(bitmap);
   			}
   		    thumbView.setAlpha(0f);
   		    expandedImageView.setVisibility(View.VISIBLE);
   		    expandedImageView.setOnClickListener(new View.OnClickListener() {
   		        @SuppressLint("NewApi")
   				@Override
   		        public void onClick(View view) {
   		        	thumbView.setAlpha(1f);
   		            expandedImageView.setVisibility(View.GONE);
   		        }
   		    });
   		    return true;  			        	
    	}
	};
	
	private ViewTreeObserver.OnGlobalLayoutListener vto = new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			if(mChangeNumColumns){
                final int numColumns = (int) Math.floor(mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                if (numColumns > 0) {
                    final int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                    mGridView.setColumnWidth(columnWidth);
                    setItemHeight(columnWidth);
                        Log.d(LOG_TAG, "onCreateView - numColumns set to " + numColumns);
                        mChangeNumColumns = false;
                }
			}
		}
	};
   
	private SimpleCursorAdapter.ViewBinder vb = new SimpleCursorAdapter.ViewBinder(){
		@Override
		   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			   switch (view.getId()) {
				case R.id.recycling_image:
					String filename = cursor.getString(1);
					RecyclingImageView iv = (RecyclingImageView) view;
				    mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			        iv.setLayoutParams(mImageViewLayoutParams);
			        String path  = Storage.getPathToScaledBitmap(filename, mItemHeight);  
			        String category = cursor.getString(3);
					boolean isCategory = (category != null  && !category.isEmpty() ) ? true : false; 
					ImageLoader.loadBitmap(path, iv);
					if(isCategory)
						if(cursor.getInt(4) == 1)
							//view.setBackgroundColor(Color.argb(120, 149,39,225));
							view.setBackgroundColor(mCategoryBackgroundColor);
						else
							//view.setBackgroundColor(Color.argb(120, 0, 255, 0));
							view.setBackgroundColor(mContextCategoryBackgroundColor);
					else
						view.setBackgroundColor(Color.TRANSPARENT);
					if(selected_images_ids.contains(cursor.getLong(0)))
						view.setBackgroundColor(Color.argb(255, 255, 0, 0));
					
					return true; 

				case R.id.image_desc:
					TextView tv = (TextView) view;
					if(tv.getTextSize() != mImageFontSize)
						tv.setTextSize(mImageFontSize);
					return false;
					
				default:
					return false;
			}
		   }

	};
		
	private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
		int select_counter = 0;
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			selected_images_ids.clear();
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) { // Inflate the menu for the CAB
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_menu, menu);
	        mActionMode = mode;
	        return true;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
            case R.id.remove:          	
            	removeSelectedBindings();
            	
            	
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.make_categoies:
            	makeCategoiesFromSelected();
            	mode.finish();
            	return true;
            case R.id.edit_image:
				Intent intent = new Intent();
				intent.setClass(getActivity(), ImageDetailsActivity.class);
				intent.putExtra("row_id", selected_images_ids.get(0));
				startActivity(intent);
            	mode.finish();
            	restartLoader = true;
            	return true;
            default:
                return false;
	        }

		}
		
		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			if(checked){
				select_counter++;
				if(!selected_images_ids.contains(id))
					selected_images_ids.add(id);
			}
			else{
				select_counter--;
				if(selected_images_ids.contains(id))
					selected_images_ids.remove(id);
			}
			mode.setTitle("Zaznaczono: "+ select_counter + " elementów");			
		}
	};
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, final  View thumbView, int position, long id) {
			Cursor c = (Cursor) adapter.getItem(position);	
			ImageObject img_object= new ImageObject();
			img_object.setId(c.getLong(c.getColumnIndex(ImageContract.Columns._ID)));
			img_object.setImageName(c.getString(c.getColumnIndex(ImageContract.Columns.FILENAME)));
			img_object.setDescription( c.getString(c.getColumnIndex(ImageContract.Columns.DESC)));
			img_object.setCategory(c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY)));
			img_object.setTimes_used(c.getLong(c.getColumnIndex(ImageContract.Columns.TIME_USED)));
			img_object.setIsContextualCategory(c.getInt(c.getColumnIndex(ImageContract.Columns.IS_CONTEXTUAL_CATEGORY)));
							
			if(img_object.getCategory() == null || img_object.getCategory().isEmpty())
				executingActivity.addImageToAdapter(img_object);
			
			if (mCurrentAnimator != null) {
			       mCurrentAnimator.cancel();
			   }
			String category = img_object.getCategory();
			if(category!=null){
				Long l = c.getLong(c.getColumnIndex(ImageContract.Columns._ID));
				executingActivity.replaceCategory(l, position, true);
				ActionBar actionBar = executingActivity.getActionBar();
				actionBar.setTitle(category);
				if(img_object.getIsContextualCategory() == 1){
					executingActivity.addImageToAdapter(img_object);
				}
				
			}				
	 }
	};
	
    public ImageGridFragment(){   }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		executingActivity = (ImageGridActivity) getActivity();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(executingActivity);
		mImageThumbSize =sharedPref.getInt("pref_img_size",150);
		mImageFontSize = sharedPref.getInt("pref_img_desc_font_size", 15);
		/*
		mCategoryBackgroundColor = Integer.valueOf(sharedPref.getString("category_view_background", "0xff33b5e5"));
		mContextCategoryBackgroundColor = Integer.valueOf(sharedPref.getString("context_category_view_background","0xffe446ff"));
		*/
		mCategoryBackgroundColor =sharedPref.getInt("category_view_background", 0xff33b5e5);
		mContextCategoryBackgroundColor =sharedPref.getInt("context_category_view_background",0xffe446ff);
		
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);	
		mChangeNumColumns = true;

    	adapter = new SimpleCursorAdapter(executingActivity.getApplicationContext(), R.layout.image_item, null, from, to, 0);
    	adapter.setViewBinder(vb);
		//getLoaderManager().initLoader(LOADER_ID, this.getArguments(), this);
		setHasOptionsMenu(true);
	}  
    
	@Override
	public void onResume(){
		super.onResume();
		//if(restartLoader){
		/*
			Bundle args = new Bundle();		
			args.putLong("CATEGORY_ID", ImageGridActivity.actual_category_fk.getCategoryId());
			args.putInt("RET_POSITION",  ImageGridActivity.actual_category_fk.getNextCatPosition());
		*/
			getLoaderManager().restartLoader(1, null, this);		
			restartLoader = false;
			executingActivity.setDrawerOrLeftList();
		
		//}

	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    	super.onCreateView(inflater, container, savedInstanceState);
    	final View v = inflater.inflate(R.layout.fragment_image_grid, container,false);
    	mGridView  = (GridView) v.findViewById(R.id.gridView);
    	expandedImageView = (ImageView) executingActivity.findViewById(R.id.expanded_image);
        mGridView.setAdapter(adapter);
	    mGridView.setOnItemClickListener(mOnItemClickListener);
	  
	    if(executingActivity.mEditMode){
	    	mGridView.setMultiChoiceModeListener(mMultiChoiceModeListener);
	    	mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
	    	
	    }
	    else
	    	mGridView.setOnItemLongClickListener(ilcL);

	    
	    mGridView.getViewTreeObserver().addOnGlobalLayoutListener(vto);        
	    return v;

    }
    
    /**
     * Sets the item height. Useful for when we know the column width so the height can be set
     * to match.
     *
     * @param height
     */
    
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        ImageLoader.setImageSize(height);
        adapter.notifyDataSetChanged();
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {

			//TODO login user root id
		/*
		if(bundle!= null){
			Long category_id = bundle.getLong("CATEGORY_ID", Long.valueOf(1));
			int position = bundle.getInt("RET_POSITION", 0);
			//ImageGridActivity.actual_category_fk = new IdPositionModel(category_id, position);
		}
		*/

		
			Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI + "/" + ImageGridActivity.actual_category_fk.getCategoryId());
			return new CursorLoader(executingActivity.getApplicationContext(),uri, loader_projection, null, null ,sortOrder);			
		

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) { 
		adapter.swapCursor(data);	
		adapter.notifyDataSetChanged();
		mGridView.post( new Runnable() {
		    @Override
		    public void run() {
				int posToScrool = ImageGridActivity.actual_category_fk.getNextCatPosition();
				Long categoryId = ImageGridActivity.actual_category_fk.getCategoryId();
				Log.i(LOG_TAG, "Category id: "+categoryId+" Scrool to pos: "+posToScrool );
				mGridView.smoothScrollToPositionFromTop(posToScrool, 10, 0);
		    }
		  });

		}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) { 
		adapter.swapCursor(null); }

	public void finishActionMode(){
		   if(mActionMode!= null){
			   mActionMode.finish();
			   selected_images_ids.clear();
			   
		   }
	}
	
	public void makeCategoiesFromSelected(){
		boolean isCategory = false;
		Cursor c = null;
		for(Long l : selected_images_ids){
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + l);
			c =getActivity().getContentResolver().query(uri, new String[]{ImageContract.Columns.DESC, ImageContract.Columns.CATEGORY}, null, null, null);
			if(c!= null){
				c.moveToFirst();
				if(c.getString(1) != null)
					isCategory = true;
			}
			
			if(!isCategory){
				ContentValues cv = new ContentValues();
				cv.put(ImageContract.Columns.CATEGORY, c.getString(0));
				getActivity().getContentResolver().update(uri, cv, null, null);
				
			}
		}
		c.close();

		Bundle args = new Bundle();		
		args.putLong("CATEGORY_ID", ImageGridActivity.actual_category_fk.getCategoryId());
		getLoaderManager().restartLoader(1, args, this);
		refreshDrawer();
	}
	
	private void removeSelectedBindings(){
		boolean isCategory = false;
		boolean isBindToAnotherCategory = false;
		String where = ParentContract.Columns.IMAGE_FK+" = ? AND "+ ParentContract.Columns.PARENT_FK+" = ? ";
		Long category_fk = ImageGridActivity.actual_category_fk.getCategoryId();
		Long main_dict_fk = App_2.getMain_dict_id();
		Cursor c =null;
		
		for(final Long l : selected_images_ids){
			Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + l);
			String categoryName = null;
			
			c =getActivity().getContentResolver().query(uri, new String[]{ImageContract.Columns.CATEGORY}, null, null, null);
			if(c!= null){
				c.moveToFirst();
				categoryName = c.getString(0);
				if(categoryName != null)
					isCategory = true;
			}

			if(isCategory){
				LinkedList<Long> parentsOfSelectedImage = new LinkedList<Long>();
				//sprawdzam czy kategoria oprócz s³ownika i aktualnej kategorii wskazuje na coœ jeszcze,
			
				Uri parents_of_image_uri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+l);
				Cursor parents_cursor= getActivity().getContentResolver().query(parents_of_image_uri, new String[]{"p."+ParentContract.Columns.PARENT_FK}, null, null, null);
				if(parents_cursor!=null){
					parents_cursor.moveToFirst();
					while(!parents_cursor.isAfterLast()){
						parentsOfSelectedImage.add(parents_cursor.getLong(0));
						parents_cursor.moveToNext();
					}
					parents_cursor.close();
					parentsOfSelectedImage.remove(main_dict_fk); // TODO nie trzeba tego usuwaæ bo nie zwraca obiektów króre nie maj¹ autora
					parentsOfSelectedImage.remove(category_fk);
					isBindToAnotherCategory = (parentsOfSelectedImage.size()>0) ? true : false;
				}
				
				
				
				if(!isBindToAnotherCategory){//jeœli nic innego nie wskazuje na kategoriê 
					DFS.getElements(l);
					boolean isCategoryEmpty = (DFS.edges.size() == 0) ? true : false;	// sprawdzam czy kategoria jest pusta
					
					if(isCategoryEmpty){							// jeœli tak usuwam kategoriê ( ustawiam dla obrazka null w polu kategorii )
						ContentValues cv = new ContentValues();
						cv.put(ImageContract.Columns.CATEGORY, (Long)null);
						Uri p_uri = Uri.parse(ImageContract.CONTENT_URI + "/" + l);
						getActivity().getContentResolver().update(p_uri, cv, null, null);
	
					}else{											// kategoria nie jest pusta, pytam u¿ytkownika czy pozostawiæ j¹ w s³owniku						
						new AlertDialog.Builder(getActivity())
				        .setTitle("Kategoria \""+ categoryName+"\" nie jest pusta.")
				        .setMessage("Pozostawiæ j¹ w s³owniku?")
				        .setNegativeButton("TAK", null)
				        .setPositiveButton("Nie, usuñ kategoriê ze s³ownika", new OnClickListener() {
				        	

							@Override
							public void onClick(DialogInterface dialog, int which) {
								removeCategoryRelations(l);
							}
				        }).create().show();	
					}
				}
				//jeœli tak to usuwam tylko wiazanie na akutaln¹ kategoriê	
			}		
				getActivity().getContentResolver().delete(ParentContract.CONTENT_URI, where , new String[]{String.valueOf(l), String.valueOf(category_fk) });		
		}

		c.close();
		Bundle args = new Bundle();		
		args.putLong("CATEGORY_ID", category_fk);
		getLoaderManager().restartLoader(1, args, this);
		refreshDrawer();
		
	}
	
	private void removeCategoryRelations(Long category){
		DFS.getElements(category);
		for(EdgeModel em : DFS.edges){
			getActivity().getContentResolver().delete(ParentContract.CONTENT_URI, ParentContract.Columns.IMAGE_FK+" = ? AND "+ ParentContract.Columns.PARENT_FK+" = ? " , new String[]{String.valueOf(em.getChild()), String.valueOf(em.getParent()) });
		   ContentValues cv = new ContentValues();
			cv.put(ImageContract.Columns.CATEGORY, (Long)null);
			// jeœli obrazki poddrzewa s¹ kategoriami to ustawiam na "null", ¿eby nie by³y widoczne w DrawerPane
			Uri p_uri = Uri.parse(ImageContract.CONTENT_URI + "/" + em.getParent());
			getActivity().getContentResolver().update(p_uri, cv, null, null);

		}		
	}
	
	
	
	
	private void refreshDrawer(){
		executingActivity.setDrawerOrLeftList();
	}
}
