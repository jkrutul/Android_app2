package com.examples.app_2.activities;

import com.example.app_2.R;
import com.example.app_2.adapters.SwipePagerAdapter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class SwipeActivity extends FragmentActivity{
	SwipePagerAdapter mCollectionAdapter;	
	ViewPager mViewPager;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe);
		
		mCollectionAdapter = new SwipePagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mCollectionAdapter);
		
		
	}
	
}
