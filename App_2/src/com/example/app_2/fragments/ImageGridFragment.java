package com.example.app_2.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.view.ActionMode;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ImagesOfParentContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.models.ImageObject;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;

@SuppressLint("NewApi")
public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener{
    private static final String TAG = "ImageGridFragment";
    private ImageView expandedImageView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ActionMode mActionMode;
    //private ImageCursorAdapter adapter;
    private SimpleCursorAdapter adapter;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private GridView.LayoutParams mImageViewLayoutParams;
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private static boolean loadExpandedImage= false;
	private static final int LOADER_ID = 1;
	private Activity executing_activity;
	


    	
    public ImageGridFragment(){
    }
    /*
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.remove:
                    //shareCurrentItem();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
   */ 
    


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		executing_activity = getActivity();
		
    	mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mImageViewLayoutParams.height = getActivity().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageViewLayoutParams.width = getActivity().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    	
    	String[] from = new String[] {
				   ImageContract.Columns._ID, 
				   ImageContract.Columns.PATH,
				   ImageContract.Columns.DESC,
				   ImageContract.Columns.CATEGORY};
				// Fields on the UI to which we map
		int[] to = new int[] { 0, R.id.recycling_image, R.id.image_desc };
    	
    	adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.image_item, null, from, to, 0);
    	adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
			   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
			       if(view.getId() == R.id.recycling_image){
			    	   
						 String path = Images.getImageThumbsPath(cursor.getString(1));
						 String category = cursor.getString(3);
						 boolean isCategory = (category != null  && !category.isEmpty() ) ? true : false; 
						 ImageLoader.loadBitmap(path, (ImageView) view, true);
						 if(isCategory)
							 view.setBackgroundColor(Color.argb(120, 0, 255, 0));
						 else
							 view.setBackgroundColor(Color.TRANSPARENT);
			           return true; //true because the data was bound to the view
			       }
			       return false;
			   }
			});
    			
		getLoaderManager().initLoader(LOADER_ID, this.getArguments(), this);
		
		setHasOptionsMenu(true);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);	
	}  
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    	super.onCreateView(inflater, container, savedInstanceState);
    	final View v = inflater.inflate(R.layout.fragment_image_grid, container,false);
    	getActivity().findViewById(R.id.main_grid).setBackgroundDrawable(App_2.wallpaperDrawable);
    	//v.setBackgroundDrawable(App_2.wallpaperDrawable);
    	expandedImageView = (ImageView) getActivity().findViewById(R.id.expanded_image);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setAdapter(adapter);
	    mGridView.setOnItemClickListener(this);
	    mGridView.setMultiChoiceModeListener(this);
	    mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
	    	public boolean onItemLongClick(AdapterView<?> parent, final  View thumbView, int position, long i) {
	        	/*
	            if (mActionMode != null) {
	                return false;
	            }

	            // Start the CAB using the ActionMode.Callback defined above
	            mActionMode = getActivity().startActionMode(mActionModeCallback);
	            view.setSelected(true);
	            return true;
	            */
	        	
	    		Cursor c = (Cursor) adapter.getItem(position);						
	    		ImageObject img_object= new ImageObject();
	    		img_object.setImageName(c.getString(c.getColumnIndex(ImageContract.Columns.PATH)));
	    		img_object.setDescription( c.getString(c.getColumnIndex(ImageContract.Columns.DESC)));
	    		img_object.setCategory(c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY)));
	    		    // Load the high-resolution "zoomed-in" image.
	    			if(expandedImageView!=null){
	    				expandedImageView.bringToFront();
	    				 String path = Images.getImageFullScreenThumbsPath(img_object.getImageName());
	    				 Bitmap b = BitmapFactory.decodeFile(path);
	    				 Drawable verticalImage = new BitmapDrawable(getResources(), b);
	    				 expandedImageView.setImageDrawable(verticalImage);
	    				 //BitmapCalc.decodeSampleBitmapFromFile(filePath, reqWidth, reqHeight)
	    				 //imageLoader.loadBitmap(path, expandedImageView);
	    				 /*
	    				 if(TextUtils.isEmpty(description))
	    					 ((ImageGridActivity) getActivity()).speakOut(Utils.cutExtention(filename));
	    				 else
	    					 ((ImageGridActivity) getActivity()).speakOut(Utils.cutExtention(description));
	    				 */
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
	    }
	        );
	 
	    //mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

	        	        
	        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
	                new ViewTreeObserver.OnGlobalLayoutListener() {
	                    @Override
	                    public void onGlobalLayout() {
	                        if (getNumColumns() == 0) {
	                            final int numColumns = (int) Math.floor(mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
	                            if (numColumns > 0) {
	                                final int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
	                                setNumColumns(numColumns);
	                                setItemHeight(columnWidth);
	                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
	                            }
	                        }
	                    }
	                });

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
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        ImageLoader.setImageSize(height);
    }
     
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }
    
    public int getNumColumns() {
        return mNumColumns;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
   
        
        
    }
    

	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onItemClick(AdapterView<?> parent, final  View thumbView, int position, long id) {
		Cursor c = (Cursor) adapter.getItem(position);						
		ImageObject img_object= new ImageObject();
		img_object.setImageName(c.getString(c.getColumnIndex(ImageContract.Columns.PATH)));
		img_object.setDescription( c.getString(c.getColumnIndex(ImageContract.Columns.DESC)));
		img_object.setCategory(c.getString(c.getColumnIndex(ImageContract.Columns.CATEGORY)));
	
		
		if(img_object.getCategory() == null || img_object.getCategory().isEmpty())
			((ImageGridActivity)getActivity()).addImageToAdapter(img_object);
		
		//c.close();
		if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }
		 
		 if(img_object.getCategory()!=null){
			Fragment fragment = new ImageGridFragment();
			Bundle args = new Bundle();			
			args.putLong("CATEGORY_ID", c.getLong(c.getColumnIndex(ImageContract.Columns._ID)));
			fragment.setArguments(args);
			 
			final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		            //ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE); 
					//overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
		            //ft.setTransition(R.anim.right_slide_in);
		            //ft.setCustomAnimations(android.R.anim.,android.R.anim.slide_out_left);
			//ft.setTransition(android.R.anim.fade_in);
					//ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			 ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		     ft.replace(R.id.content_frame, fragment, TAG);
		     ft.addToBackStack(null);
		     ft.commit();				
		 }
		 

		
	}
		
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {

		CursorLoader cursorLoader= null;
		int category_fk = (bundle!= null) ? (int) bundle.getLong("CATEGORY_ID", -1)	: -1;
		Uri uri = Uri.parse(ImagesOfParentContract.CONTENT_URI + "/" + category_fk);
		
		String[] projection = new String[] { "i."+ImageContract.Columns._ID,  "i."+ImageContract.Columns.PATH,  "i."+ImageContract.Columns.DESC,  "i."+ImageContract.Columns.CATEGORY};	
		String selection = "p."+ParentContract.Columns.PARENT_FK +" = ?";
		String[] selectionArgs = new String[]{String.valueOf(category_fk)};
		/*if(category_fk == -1){
			Long imgLastModified = Storage.getImagesDir().lastModified();
			Long img_dir_last_read = Long.valueOf(Storage.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead", "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE));
			if(imgLastModified> img_dir_last_read) {
				ProcessBitmapsTask processBitmapsTask = new ProcessBitmapsTask(getActivity());
				processBitmapsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			}

		}*/

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
