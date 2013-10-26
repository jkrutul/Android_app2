package com.example.app_2.adapters;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;

public class ExpressionAdapter extends BaseAdapter {
	private static List<String> dataObjects = new LinkedList<String>();
	private Context context;
	
	public ExpressionAdapter(Context context){
		this.context = context;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;
        // Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
            imageView = new RecyclingImageView(this.context);
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setLayoutParams(mImageViewLayoutParams);
        } else { // Otherwise re-use the converted view
            imageView = (ImageView) convertView;
        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        ImageLoader.loadBitmap(Storage.getThumbsDir()+File.separator+dataObjects.get(position), imageView, true);
        imageView.setClickable(true);
        imageView.setOnClickListener(new OnClickListener(){
    		
    		@Override
    		public void onClick(View v) {
    			dataObjects.remove(pos);
    			notifyDataSetChanged();    			
    		}
    	});
        return imageView;
		//return null;
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	@Override
	public int getCount() {
		return dataObjects.size();
	}
	
	
	public void addImageToAdapter(String path){
		dataObjects.add(path);
		notifyDataSetChanged();
	}
	
	public void removeLastImage(){
		dataObjects.remove(dataObjects.size()-1);
		notifyDataSetChanged();
	}
	
	public void removeAllImages(){
		dataObjects.removeAll(dataObjects);
		notifyDataSetChanged();
	}
	
	
	private OnClickListener mOnButtonClicked = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(App_2.getAppContext());
			builder.setMessage("hello from " + v);
			builder.setPositiveButton("Cool", null);
			builder.show();
			
		}
	};
}
