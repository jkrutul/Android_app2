package com.example.app_2.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.adapters.ImageCursorAdapter;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.Images.ProcessBitmapsTask;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.BitmapCalc;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.examples.app_2.activities.ImageGridActivity;

public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor>{
    private static final String TAG = "ImageGridFragment";
    private ImageView expandedImageView;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageCursorAdapter adapter;
    private ImageLoader imageLoader;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    
    private GridView.LayoutParams mImageViewLayoutParams;
    private int mItemHeight = 0;
    private int mNumColumns = 0;
	ImageView mImageView;
    	
    public ImageGridFragment(){
    }
    
   

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	imageLoader = new ImageLoader();
    	mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		//if(App_2.actvity!=null){ //TODO
		//	expandedImageView = (ImageView) App_2.actvity.findViewById(R.id.expanded_image);
		//}
    	
    	expandedImageView = (ImageView)getActivity().findViewById(R.id.expanded_image);
    	
		adapter = new ImageCursorAdapter(getActivity(), null, mImageViewLayoutParams, imageLoader);
		getLoaderManager().restartLoader(0, this.getArguments(), this);
		//getLoaderManager().initLoader(0, this.getArguments(), this);
		
		setHasOptionsMenu(true);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);	
	}
    /*
    @SuppressLint("NewApi")
	private void fillData(int category_fk){
		String[] from = new String[] { ImageContract.Columns._ID,
				   ImageContract.Columns.PATH,
				   ImageContract.Columns.DESC };		
		Cursor c ;
		if(category_fk == -1){
			ProcessBitmapsTask pbt = new ProcessBitmapsTask(getActivity());
			pbt.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			c = getActivity().getContentResolver().query(ImageContract.CONTENT_URI, from, null, null ,null);
		}else{
			String selection = ImageContract.Columns.PARENT +" = ?";
			String[] selectionArgs = new String[]{String.valueOf(category_fk)};
			c = getActivity().getContentResolver().query(ImageContract.CONTENT_URI, from, selection, selectionArgs ,null);
		}
		adapter = new ImageCursorAdapter(getActivity(), c, mImageViewLayoutParams, imageLoader);
    }
    */
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle sacedInstanceState){
    	final View v = inflater.inflate(R.layout.fragment_image_grid, containter,false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
	        mGridView.setOnItemClickListener(this);
	        
	        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
	            @Override
	            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
	                // Pause fetcher to ensure smoother scrolling when flinging
	                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
	                    //mImageFetcher.setPauseWork(true);
	                } else {
	                    //mImageFetcher.setPauseWork(false);
	                }
	            }

	            @Override
	            public void onScroll(AbsListView absListView, int firstVisibleItem,
	                    int visibleItemCount, int totalItemCount) {
	            }
	        });
	        // This listener is used to get the final width of the GridView and then calculate the
	        // number of columns and the width of each column. The width of each column is variable
	        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
	        // of each view so we get nice square thumbnails.
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
	        
	        mGridView.setAdapter(adapter);

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
		Cursor c = (Cursor) adapter.getItem(position);							// TODO przejœcie do nowej kategorii
		String filename = c.getString(c.getColumnIndex(ImageContract.Columns.PATH));
		String description = c.getString(c.getColumnIndex(ImageContract.Columns.DESC));
		//c.close();
		if (mCurrentAnimator != null) {
	        mCurrentAnimator.cancel();
	    }
		
		expandedImageView.bringToFront();
	    // Load the high-resolution "zoomed-in" image.
		if(expandedImageView!=null){
			 String path = Images.getImageFullScreenThumbsPath(filename);
			 Bitmap b = BitmapFactory.decodeFile(path);
			 Drawable verticalImage = new BitmapDrawable(getResources(), b);
			 expandedImageView.setImageDrawable(verticalImage);
			 //BitmapCalc.decodeSampleBitmapFromFile(filePath, reqWidth, reqHeight)
			 //imageLoader.loadBitmap(path, expandedImageView);
			 if(TextUtils.isEmpty(description))
				 ImageGridActivity.speakOut(Utils.cutExtention(filename));
			 else
				 ImageGridActivity.speakOut(Utils.cutExtention(description));
		}
		
	    final Rect startBounds = new Rect();
	    final Rect finalBounds = new Rect();
	    final Point globalOffset = new Point();
	    thumbView.getGlobalVisibleRect(startBounds);
	    getActivity().findViewById(R.id.content_frame)
	            .getGlobalVisibleRect(finalBounds, globalOffset);
	    startBounds.offset(-globalOffset.x, -globalOffset.y);
	    finalBounds.offset(-globalOffset.x, -globalOffset.y);

	    // Adjust the start bounds to be the same aspect ratio as the final
	    // bounds using the "center crop" technique. This prevents undesirable
	    // stretching during the animation. Also calculate the start scaling
	    // factor (the end scaling factor is always 1.0).
	    float startScale;
	    if ((float) finalBounds.width() / finalBounds.height()
	            > (float) startBounds.width() / startBounds.height()) {
	        // Extend start bounds horizontally
	        startScale = (float) startBounds.height() / finalBounds.height();
	        float startWidth = startScale * finalBounds.width();
	        float deltaWidth = (startWidth - startBounds.width()) / 2;
	        startBounds.left -= deltaWidth;
	        startBounds.right += deltaWidth;
	    } else {
	        // Extend start bounds vertically
	        startScale = (float) startBounds.width() / finalBounds.width();
	        float startHeight = startScale * finalBounds.height();
	        float deltaHeight = (startHeight - startBounds.height()) / 2;
	        startBounds.top -= deltaHeight;
	        startBounds.bottom += deltaHeight;
	    }

	    // Hide the thumbnail and show the zoomed-in view. When the animation
	    // begins, it will position the zoomed-in view in the place of the
	    // thumbnail.
	    thumbView.setAlpha(0f);
	    expandedImageView.setVisibility(View.VISIBLE);

	    // Set the pivot point for SCALE_X and SCALE_Y transformations
	    // to the top-left corner of the zoomed-in view (the default
	    // is the center of the view).
	    expandedImageView.setPivotX(0f);
	    expandedImageView.setPivotY(0f);

	    // Construct and run the parallel animation of the four translation and
	    // scale properties (X, Y, SCALE_X, and SCALE_Y).
	    AnimatorSet set = new AnimatorSet();
	    set
	            .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
	                    startBounds.left, finalBounds.left))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
	                    startBounds.top, finalBounds.top))
	            .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
	            startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
	                    View.SCALE_Y, startScale, 1f));
	    set.setDuration(mShortAnimationDuration);
	    set.setInterpolator(new DecelerateInterpolator());
	    set.addListener(new AnimatorListenerAdapter() {
	        @Override
	        public void onAnimationEnd(Animator animation) {
	            mCurrentAnimator = null;
	        }

	        @Override
	        public void onAnimationCancel(Animator animation) {
	            mCurrentAnimator = null;
	        }
	    });
	    set.start();
	    mCurrentAnimator = set;
	    

	    // Upon clicking the zoomed-in image, it should zoom back down
	    // to the original bounds and show the thumbnail instead of
	    // the expanded image.
	    final float startScaleFinal = startScale;
	    expandedImageView.setOnClickListener(new View.OnClickListener() {
	        @SuppressLint("NewApi")
			@Override
	        public void onClick(View view) {
	            if (mCurrentAnimator != null) {
	                mCurrentAnimator.cancel();
	            }

	            // Animate the four positioning/sizing properties in parallel,
	            // back to their original values.
	            AnimatorSet set = new AnimatorSet();
	            set.play(ObjectAnimator
	                        .ofFloat(expandedImageView, View.X, startBounds.left))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.Y,startBounds.top))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_X, startScaleFinal))
	                        .with(ObjectAnimator
	                                .ofFloat(expandedImageView, 
	                                        View.SCALE_Y, startScaleFinal));
	            set.setDuration(mShortAnimationDuration);
	            set.setInterpolator(new DecelerateInterpolator());
	            set.addListener(new AnimatorListenerAdapter() {
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    expandedImageView.setVisibility(View.GONE);
	                    expandedImageView.getDrawable().setCallback(null);
	                    //expandedImageView.setImageResource(R.drawable.empty_photo);
	                    mCurrentAnimator = null;
	                }

	                @Override
	                public void onAnimationCancel(Animator animation) {
	                    thumbView.setAlpha(1f);
	                    expandedImageView.setVisibility(View.GONE);
	                    mCurrentAnimator = null;
	                }
	            });
	            set.start();
	            mCurrentAnimator = set;
	            //App_2.actvity.getActionBar().show();
	        }
	    });


		
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
		
		String[] from = new String[] { ImageContract.Columns._ID, ImageContract.Columns.PATH, ImageContract.Columns.DESC };	
		String selection = ImageContract.Columns.PARENT +" = ?";
		String[] selectionArgs = new String[]{String.valueOf(category_fk)};
		//if(category_fk == -1){
		//	Long imgLastModified = Storage.getImagesDir().lastModified();
		//	Long img_dir_last_read = Long.valueOf(Storage.readFromSharedPreferences(String.valueOf(0), "imgDirLastRead", "imgDirLastRead", App_2.getAppContext(), Context.MODE_PRIVATE));
		//	if(imgLastModified> img_dir_last_read)
		//		new ProcessBitmapsTask(getActivity()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
		//}

		cursorLoader = new CursorLoader(App_2.getAppContext(),ImageContract.CONTENT_URI, from, selection, selectionArgs ,null);
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
}
