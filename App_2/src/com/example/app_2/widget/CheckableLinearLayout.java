package com.example.app_2.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable{

    private OnCheckedChangeListener onCheckedChangeListener;
	private boolean isChecked;
	private List<Checkable> checkableViews;


	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(CheckableLinearLayout layout, boolean isChecked);
	}
	public CheckableLinearLayout(Context context, AttributeSet attrs,	int defStyle) {
		super(context, attrs, defStyle);
		initialise(attrs);
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise(attrs);
	}

	public CheckableLinearLayout(Context context, int checkableId) {
		super(context);
		initialise(null);
	}

	/*
	 * @see android.widget.Checkable#isChecked()
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/*
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
		for(Checkable c : checkableViews){
			c.setChecked(isChecked);		
		}
		if(onCheckedChangeListener != null){
			onCheckedChangeListener.onCheckedChanged(this, isChecked);
		}
	}

	@Override
	public void toggle() {
		this.isChecked = !this.isChecked;
		for(Checkable c : checkableViews){
			c.toggle();		
		}
	}

	 public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
         this.onCheckedChangeListener = onCheckedChangeListener;
	 }

	@Override
	protected void onFinishInflate(){
		super.onFinishInflate();
		final int childCount = this.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			findCheckableChildren(this.getChildAt(i));			
		}
	}

	  /**
     * Read the custom XML attributes
     */
    private void initialise(AttributeSet attrs) {
            this.isChecked = false;
            this.checkableViews = new ArrayList<Checkable>();
    }


	
	private void findCheckableChildren(View v){
		if(v instanceof Checkable){
			this.checkableViews.add((Checkable)v);
		}
		
		if(v instanceof ViewGroup){
			final ViewGroup vg = (ViewGroup) v;
			final int childCount = vg.getChildCount();
			for (int i = 0; i < childCount; ++i) {
				findCheckableChildren(vg.getChildAt(i));
			}			
		}
	}

}
