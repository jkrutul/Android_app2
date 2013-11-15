package com.example.app_2.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.app_2.R;

public class WordView extends RelativeLayout{
	private View mView;
	private ImageView mImage;
	private TextView mTitle;
	
	public WordView(Context context) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.word_view, this, true);

		mTitle = (TextView) getChildAt(2);
		mView = getChildAt(1);
		mImage = (ImageView) getChildAt(0);
	}

	public WordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WordViewOptions, 0,0 );
		String titleText = a.getString(R.styleable.WordViewOptions_titleText);
		int valueColor = a.getColor(R.styleable.WordViewOptions_valueColor, android.R.color.holo_orange_light);
		a.recycle();
		
		//setOrientation(LinearLayout.HORIZONTAL);
		//setGravity(Gravity.CENTER_VERTICAL);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.word_view, this, true);

		TextView title = (TextView) getChildAt(2);
		title.setText(titleText);
		
		mView = getChildAt(1);
		mView.setBackgroundColor(valueColor);

		mImage = (ImageView) getChildAt(0);
	}


	public void setValueColor(int color) {
		mView.setBackgroundColor(color);
	}
	
	public void setText(String text){
		mTitle.setText(text);
	}

	public void setImageVisible(boolean visible) {
		mImage.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
}
