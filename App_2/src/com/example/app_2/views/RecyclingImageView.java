/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.app_2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Sub-class of ImageView which automatically notifies the drawable when it is
 * being displayed.
 */
public class RecyclingImageView extends ImageView {
	Paint mTextPaint;
	String textToDraw;
	
	public RecyclingImageView(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
		init();
	}

	public RecyclingImageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		init();
	}

	public RecyclingImageView(Context context) {
		super(context);
		init();
	}
	
	public RecyclingImageView(Context context, String textToDraw) {
		super(context);
		this.textToDraw = textToDraw;
		init();
	}


	/**
	 * @see android.widget.ImageView#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		// This has been detached from Window, so clear the drawable
		setImageDrawable(null);

		super.onDetachedFromWindow();
	}

	/**
	 * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	public void setImageDrawable(Drawable drawable) {
		final Drawable previousDrawable = getDrawable();// Keep hold of previous Drawable
		super.setImageDrawable(drawable);				// Call super to set new Drawable
		notifyDrawable(drawable, true);					// Notify new Drawable that it is being displayed
		notifyDrawable(previousDrawable, false);		// Notify old Drawable so it is no longer being displayed
	}

	/**
	 * Notifies the drawable that it's displayed state has changed.
	 * 
	 * @param drawable
	 * @param isDisplayed
	 */
	private static void notifyDrawable(Drawable drawable,
			final boolean isDisplayed) {
		if (drawable instanceof RecyclingBitmapDrawable) {// The drawable is a CountingBitmapDrawable, so notify it
			((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
		} else if (drawable instanceof LayerDrawable) {		// The drawable is a LayerDrawable, so recurse on each layer
			LayerDrawable layerDrawable = (LayerDrawable) drawable;
			for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
				notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
			}
		}
	}

	private void init() {
		mTextPaint = new Paint();
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(25);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int imgWidth = getMeasuredWidth();
		int imgHeight = getMeasuredHeight();
		float txtWidth = mTextPaint.measureText(textToDraw);
		int x = Math.round(imgWidth / 2 - txtWidth / 2);
		int y = imgHeight / 2 - 6;
		canvas.drawText(textToDraw, x, y, mTextPaint);
	}

}
