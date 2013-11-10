package com.example.app_2.adapters;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.provider.Images;
import com.example.app_2.provider.SpinnerItem;
import com.example.app_2.utils.ImageLoader;

public class MySpinnerAdapter extends ArrayAdapter<SpinnerItem> {
	public static List<SpinnerItem> items;
	private Activity context;
	ImageLoader il;
	
	

	
    public MySpinnerAdapter(Activity context, int resource, List<SpinnerItem> objects) {
        super(context, resource, objects);
        this.items = objects;
        this.context = context;
        il = new ImageLoader(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
       ((TextView)v.findViewById(android.R.id.text1)).setText(items.get(position).getItemString());
        return v;
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
    	View row = convertView;
    	
    	if(row == null){
    		 LayoutInflater inflater = context.getLayoutInflater();
             row = inflater.inflate(R.layout.image_row, parent, false);
    	}

    	SpinnerItem item = items.get(position);
    	

    	if(item != null){
        	if(item.isHint()){
       		 	LayoutInflater inflater = context.getLayoutInflater();
       		 	row = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
    			TextView tv = (TextView) row.findViewById((android.R.id.text1));
    			tv.setText("ANULUJ");
    			tv.setTextSize(25);
    			tv.setHeight(150);
    			tv.setGravity(Gravity.CENTER);
    			tv.setTextColor(Color.rgb(148, 150, 148));
    		}
        	else{
       		 	LayoutInflater inflater = context.getLayoutInflater();
       		 	row = inflater.inflate(R.layout.image_row, parent, false);
				TextView categoryName = (TextView) row.findViewById(R.id.category);
				ImageView imageView = (ImageView) row.findViewById(R.id.icon);
				
				categoryName.setText(item.getItemString());
				if(item.getPath()!=null)
					ImageLoader.loadBitmap(Images.getImageThumbsPath(item.getPath()), imageView, false);
    		}
    	}
    	return row;    	
    }
    
    @Override
    public int getCount() {
        return super.getCount(); // This makes the trick: do not show last item
    }

    @Override
    public SpinnerItem getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    
    
    

}