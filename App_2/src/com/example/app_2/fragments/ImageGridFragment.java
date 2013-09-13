package com.example.app_2.fragments;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.models.ImageObject;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageFetcher;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener{
    private static final String TAG = "ImageGridFragment";
    
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    //private ImageFetcher mImageFetcher;
    private ImageLoader imageLoader;

	
    public ImageGridFragment(){
    	
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
		mAdapter = new ImageAdapter(getActivity());
        imageLoader = new ImageLoader();
        //getActivity().getSupportFragmentManager(); //?
		
	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle sacedInstanceState){
    	final View v = inflater.inflate(R.layout.fragment_image_grid, containter,false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);
	        mGridView.setAdapter(mAdapter);
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
	                        if (mAdapter.getNumColumns() == 0) {
	                            final int numColumns = (int) Math.floor(
	                                    mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
	                            if (numColumns > 0) {
	                                final int columnWidth =
	                                        (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
	                                mAdapter.setNumColumns(numColumns);
	                                mAdapter.setItemHeight(columnWidth);
	                                    Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
	                            }
	                        }
	                    }
	                });

	        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ImageObject imgO = mAdapter.getItemAtPosition(position);							// TODO debug info
		Toast.makeText(App_2.getAppContext(), "pos:"+position+"\n"+imgO, Toast.LENGTH_SHORT).show();
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }
    
    
    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private int mActionBarHeight = 0;
        private GridView.LayoutParams mImageViewLayoutParams;
        

        public ImageAdapter(Context context) {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, context.getResources().getDisplayMetrics());
            }
            
            Images.readImagesFromDB();
            //Images.populateImagePaths(Long.valueOf(1)); // main category in tree     
            //Images.generateThumbs();
        }

        @Override
        public int getCount() {
            // Size + number of columns for top empty row
            return Images.images.size();
            // return Images.images.size()+ mNumColumns;
        }

        @Override
        public Object getItem(int position) {
            return position < mNumColumns ?
                    null : Images.getImageThubms(position);
        }
        
        public ImageObject getItemAtPosition(int position){
        	return Images.images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position < mNumColumns ? 0 : position;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position < mNumColumns) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

        	/*
            // First check if this is the top row
            if (position < mNumColumns) {
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                // Set empty view with height of ActionBar
                convertView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
                return convertView;
            }
            
            */

            // Now handle the main ImageView thumbnails
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
                imageView = new RecyclingImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            } else { // Otherwise re-use the converted view
                imageView = (ImageView) convertView;
            }

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            
            
            // TODO 1
            imageLoader.loadBitmap(Images.getImageThubms(position), (ImageView) imageView);
            //ImageLoader.loadImage(Images.imagesPaths.get(position - mNumColumns), imageView);
            return imageView;
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
            mImageViewLayoutParams =
                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            ImageLoader.setImageSize(height);
            notifyDataSetChanged();
        }
        
        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
    }

}
