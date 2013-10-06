package com.example.app_2.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.models.ImageObject;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.Images.ThumbsProcessTask;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;


/**
 * The main adapter that backs the GridView. This is fairly standard except the number of
 * columns in the GridView is used to create a fake top row of empty views as we use a
 * transparent ActionBar and don't want the real top row of images to start off covered by it.
 */
public class ImageAdapter extends BaseAdapter {
	private int imgButtonID=0;
    private final Context mContext;
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private int mActionBarHeight = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    ImageLoader imageLoader;
    

    public ImageAdapter(Context context, ImageLoader il) {
        super();
        mContext = context;
        imageLoader = il;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
        new ThumbsProcessTask(App_2.actvity).execute();
    }
    
    public ImageAdapter(Context context, int category_id, ImageLoader il){
        super();
        mContext = context;
        imageLoader = il;
        mImageViewLayoutParams = new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // Calculate ActionBar height
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, context.getResources().getDisplayMetrics());
        }
    	Images.populateImagePaths(category_id);
    }

    @Override
    public int getCount() {
        return Images.images.size();
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
        
      

        // Now handle the main ImageView thumbnails
    	ImageButton imageButton;
    	if (convertView == null) { // if it's not recycled, instantiate and initialize
    		imageButton = new ImageButton(mContext);
    		imageButton.setId(imgButtonID++);
    		imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
    		imageButton.setLayoutParams(mImageViewLayoutParams);
        } else { // Otherwise re-use the converted view
        	imageButton = (ImageButton) convertView;
        }

        // Check the height matches our calculated column width
        if (imageButton.getLayoutParams().height != mItemHeight) {
        	imageButton.setLayoutParams(mImageViewLayoutParams);
        }
    	  */
    	

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
        imageLoader.loadBitmap(Images.getImageThubms(position), imageView);
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
