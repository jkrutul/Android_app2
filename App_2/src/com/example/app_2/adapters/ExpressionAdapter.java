package com.example.app_2.adapters;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.example.app_2.views.RecyclingImageView;

public class ExpressionAdapter extends BaseAdapter{
	private boolean doubleBackToExitPressedOnce = false;
	public static List<ImageObject> dataObjects = new LinkedList<ImageObject>();
	private Context context;
    LayoutParams params;
	
	public ExpressionAdapter(Context context){
		this.context = context;
		 params = new LayoutParams(100, 100);
	}
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;
		
        // Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
            imageView = new RecyclingImageView(this.context);

            imageView.setLayoutParams(params);
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else { // Otherwise re-use the converted view
            imageView = (ImageView) convertView;
            imageView.setLayoutParams(params);
        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        ImageLoader.loadBitmap(Storage.getThumbsDir()+File.separator+dataObjects.get(position).getImageName(), imageView, false);
        imageView.setClickable(true);
        
        
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
	
	
	public void addImageToAdapter(ImageObject image_object){
		dataObjects.add(image_object);
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
	


}
