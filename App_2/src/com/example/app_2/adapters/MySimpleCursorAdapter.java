package com.example.app_2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;

public class MySimpleCursorAdapter extends SimpleCursorAdapter{

	public MySimpleCursorAdapter(Context context, int layout, Cursor c,	String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}
	

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		super.bindView(arg0, arg1, arg2);
	}
	
	@Override
	public void setViewImage(ImageView iv, String filename){
		
	}
	


}
