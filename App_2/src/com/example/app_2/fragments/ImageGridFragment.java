package com.example.app_2.fragments;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.activities.SettingsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.models.ImageObject;
import com.example.app_2.provider.Images;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

@SuppressLint("NewApi")
public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener{
    private static final String LOG_TAG = ImageGridActivity.GRID_FRAGMENT_TAG;
    private ImageView expandedImageView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
	private int mImageFontSize = 20;
    private ActionMode mActionMode;
    //private ImageCursorAdapter adapter;
    private SimpleCursorAdapter adapter;
    private Animator mCurrentAnimator;
    private GridView mGridView;
    private RelativeLayout.LayoutParams mImageViewLayoutParams;
    private int mItemHeight = 0;
    private boolean mChangeNumColumns = false;
	private static final int LOADER_ID = 1;
	
	private static final int dev_h = App_2.getMaxHeight();
	private static final int dev_w = App_2.getMaxWidth();

	
	//private int img_size =100;

	
	private OnItemLongClickListener ilcL = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, final  View thumbView, int position, long i) {
    		Cursor c = (Cursor) adapter.getItem(position);						
    		ImageObject img_object= new ImageObject();
    		img_object.setImageName(c.getString(c.getColumnIndex(ImageContract.Columns.FILENAME)));
    		img_object.setDescription( c.getString(c.getColumnIndex(ImageContract.Columns.DESC)));
    		img_object.setCategory(c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY)));
    		    // Load the high-resolution "zoomed-in" image.
    			if(expandedImageView!=null){
    				expandedImageView.bringToFront();
    				//0 String path = Images.getImageFullScreenThumbsPath(img_object.getImageName());
    				 String path = Storage.getPathToScaledBitmap(img_object.getImageName(), dev_w);
    				 //Bitmap b = BitmapFactory.decodeFile(path);
    				 //Drawable verticalImage = new BitmapDrawable(getResources(), b);
    		         Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, App_2.getMaxWidth(), App_2.getMaxHeight(), ScalingLogic.FIT);

    		            // Part 2: Scale image
    		         Bitmap bitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, App_2.getMaxWidth(), App_2.getMaxHeight(), ScalingLogic.FIT);
    		         unscaledBitmap.recycle();
    		         expandedImageView.setImageBitmap(bitmap);

    				 //BitmapCalc.decodeSampleBitmapFromFile(filePath, reqWidth, reqHeight)
    				 //imageLoader.loadBitmap(path, expandedImageView);
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
                    //setNumColumns(numColumns);
                    mGridView.setColumnWidth(columnWidth);
                    setItemHeight(columnWidth);
                        Log.d(LOG_TAG, "onCreateView - numColumns set to " + numColumns);
                        mChangeNumColumns = false;
                }
			}
		}
	};
   
	private SimpleCursorAdapter.ViewBinder vb = new SimpleCursorAdapter.ViewBinder(){
		   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			   switch (view.getId()) {
				case R.id.recycling_image:
					 ImageView iv = (ImageView) view;
					 mImageViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
		    	     iv.setLayoutParams(mImageViewLayoutParams);
		    	     String path  = Storage.getPathToScaledBitmap(cursor.getString(1), mItemHeight);
					 //String path = Images.getImageFullScreenThumbsPath(cursor.getString(1));
					 String category = cursor.getString(3);
					 boolean isCategory = (category != null  && !category.isEmpty() ) ? true : false; 
					 ImageLoader.loadBitmap(path, iv, true);
					 if(isCategory)
						 view.setBackgroundColor(Color.argb(120, 0, 255, 0));
					 else
						 view.setBackgroundColor(Color.TRANSPARENT);
					 return true; 
				case R.id.image_desc:
					TextView tv = (TextView) view;
					tv.setTextSize(mImageFontSize);
					
					break;
	
				default:
					return false;
			}
			return false;
		   }

	};
	
    public ImageGridFragment(){
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mImageThumbSize = Integer.valueOf(sharedPref.getString("pref_img_size", ""));
		mImageFontSize = Integer.valueOf(sharedPref.getString("pref_img_desc_font_size", ""));
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);	
		mChangeNumColumns = true;
    	
    	String[] from = new String[] {
				   ImageContract.Columns._ID, 
				   ImageContract.Columns.FILENAME,
				   ImageContract.Columns.DESC,
				   ImageContract.Columns.CATEGORY};
		int[] to = new int[] { 0, R.id.recycling_image, R.id.image_desc };
    	
    	adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.image_item, null, from, to, 0);
    	adapter.setViewBinder(vb);
    			
		getLoaderManager().initLoader(LOADER_ID, this.getArguments(), this);
		
		setHasOptionsMenu(true);
		//mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		//mImageThumbSize = img_size;

	}  
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    	super.onCreateView(inflater, container, savedInstanceState);
    	final View v = inflater.inflate(R.layout.fragment_image_grid, container,false);
    	mGridView  = (GridView) v.findViewById(R.id.gridView);
    	getActivity().findViewById(R.id.main_grid).setBackgroundDrawable(App_2.wallpaperDrawable);

    	expandedImageView = (ImageView) getActivity().findViewById(R.id.expanded_image);
        mGridView.setAdapter(adapter);
	    mGridView.setOnItemClickListener(this);
	    mGridView.setMultiChoiceModeListener(this);
	    mGridView.setOnItemLongClickListener(ilcL);
	    mGridView.getViewTreeObserver().addOnGlobalLayoutListener(vto);
	    //mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);	        
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



	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onItemClick(AdapterView<?> parent, final  View thumbView, int position, long id) {
		Cursor c = (Cursor) adapter.getItem(position);						
		ImageObject img_object= new ImageObject();
		img_object.setImageName(c.getString(c.getColumnIndex(ImageContract.Columns.FILENAME)));
		img_object.setDescription( c.getString(c.getColumnIndex(ImageContract.Columns.DESC)));
		img_object.setCategory(c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY)));
	
		
		if(img_object.getCategory() == null || img_object.getCategory().isEmpty())
			((ImageGridActivity)getActivity()).addImageToAdapter(img_object);
		
		if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }
		 String category = img_object.getCategory();
		 if(category!=null){
			Fragment fragment = new ImageGridFragment();
			Bundle args = new Bundle();			
			args.putLong("CATEGORY_ID", c.getLong(c.getColumnIndex(ImageContract.Columns._ID)));
			fragment.setArguments(args);
			 
			final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			 ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		     ft.replace(R.id.content_frame, fragment, ImageGridActivity.GRID_FRAGMENT_TAG);
		     ImageGridActivity.fragmentsHistory.add(ImageGridActivity.actual_category_fk);
		     ft.commit();		
		     ActionBar actionBar = getActivity().getActionBar();
		     String title = (String) actionBar.getTitle();
		     title+="/"+category;
		     actionBar.setTitle(title);
		 }
		 

		
	}
	/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }
	*/
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {

		CursorLoader cursorLoader= null;
		ImageGridActivity.actual_category_fk = (bundle!= null) ? (int) bundle.getLong("CATEGORY_ID", -1)	: -1;
		Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI + "/" + ImageGridActivity.actual_category_fk);
		
		String[] projection = new String[] { "i."+ImageContract.Columns._ID,  "i."+ImageContract.Columns.FILENAME,  "i."+ImageContract.Columns.DESC,  "i."+ImageContract.Columns.CATEGORY};	
		String selection = "p."+ParentContract.Columns.PARENT_FK +" = ?";
		String[] selectionArgs = new String[]{String.valueOf(ImageGridActivity.actual_category_fk)};
		cursorLoader = new CursorLoader(getActivity().getApplicationContext(),uri, projection, selection, selectionArgs ,null);
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
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        // Respond to clicks on the actions in the CAB
        switch (item.getItemId()) {
            case R.id.delete:
                //deleteSelectedItems();
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }

	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		 // Inflate the menu for the CAB
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;

	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
        // Here you can make any necessary updates to the activity when
        // the CAB is removed. By default, selected items are deselected/unchecked.	
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        // Here you can perform updates to the CAB due to
        // an invalidate() request
        return false;

	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		// TODO Auto-generated method stub
		
	}

}
