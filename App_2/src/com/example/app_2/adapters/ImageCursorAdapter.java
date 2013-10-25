package com.example.app_2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.app_2.R;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.provider.Images;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.utils.Utils;
import com.example.app_2.views.RecyclingImageView;

public class ImageCursorAdapter extends CursorAdapter {
	
	GridView.LayoutParams params;
	
    public ImageCursorAdapter(Context context, Cursor c, GridView.LayoutParams params) {
        super(context, c, 0);
        this.params = params;	
        this.params.height = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        this.params.width = context.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
    }

    @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String pos = cursor.getString(cursor.getColumnIndex(ImageContract.Columns._ID));
            String path = cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH));
            RecyclingImageView image = (RecyclingImageView)view;
            //image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //image.setScaleType(ImageView.ScaleType.CENTER);
            //image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //image.setScaleType(ImageView.ScaleType.FIT_END);
            //.setScaleType(ImageView.ScaleType.FIT_START);
            //image.setScaleType(ImageView.ScaleType.FIT_XY);
            //image.setScaleType(ImageView.ScaleType.MATRIX);
            image.setLayoutParams(params);
            
            ImageLoader.loadBitmap(Images.getImageThumbsPath(path), (ImageView)view, true);
            
            //image.setImageDrawable(Drawable.createFromPath(path));
        }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	String textToDraw = Utils.cutExtention(cursor.getString(cursor.getColumnIndex(ImageContract.Columns.PATH)));
    	RecyclingImageView imageView = new RecyclingImageView(context,textToDraw);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //imageView.setLayoutParams(params);
       // bindView(v, context, cursor);
        return imageView;
    }
}