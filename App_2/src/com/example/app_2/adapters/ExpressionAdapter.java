package com.example.app_2.adapters;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.models.ImageObject;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.RecyclingImageView;
import com.example.app_2.views.WordView;

public class ExpressionAdapter extends BaseAdapter{
	private boolean doubleBackToExitPressedOnce = false;
	public static List<ImageObject> dataObjects = new LinkedList<ImageObject>();
	private Context context;
    LayoutParams params;
    LayoutInflater inflater;
    
    private static class ViewHolderItem{
    	RecyclingImageView rImageView;
    	TextView textView;
    }
	
	public ExpressionAdapter(Context context){
		this.context = context;
		inflater = LayoutInflater.from(App_2.getAppContext());
		 params = new LayoutParams(100, 100);
	}
	
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv = null;
		
			View retval = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, null);
			TextView tv= (TextView) retval.findViewById(R.id.image_desc);
			iv = (ImageView) retval.findViewById(R.id.recycling_image);
			
			tv.setText(dataObjects.get(position).getDescription());
			convertView = retval;
	
		String path = Storage.getPathToScaledBitmap(dataObjects.get(position).getImageName(), 100);
		ImageLoader.loadBitmap(path, iv);
        return convertView;

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
	
	public static void incrUseCounter(){
		Database db = Database.open();
		for(ImageObject img_o : dataObjects)
			db.updateImageCounter(img_o);
		
	}
	
	public void addImageToExpressionAdapter(ImageObject image_object){
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
