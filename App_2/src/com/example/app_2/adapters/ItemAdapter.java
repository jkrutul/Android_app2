package com.example.app_2.adapters;


import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;

public class ItemAdapter extends CursorAdapter{
	
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    

	

	public ItemAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		String filename = c.getString(c.getColumnIndexOrThrow(ImageContract.Columns.PATH));
		
		TextView img_name = (TextView) v.findViewById(R.id.label);
		ImageView img_thumb = (ImageView) v.findViewById(R.id.icon);
		ImageLoader il = new ImageLoader();
		
		if(filename!=null){
			img_name.setText(filename);
			il.loadBitmap(Storage.getThumbsDir()+File.separator+filename, img_thumb);
			
		}
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = mLayoutInflater.inflate(R.layout.image_row, parent, false);
		return v;
	}

}
