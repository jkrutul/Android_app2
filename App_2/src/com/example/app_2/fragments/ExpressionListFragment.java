package com.example.app_2.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.app_2.views.HorizontalListView;
import com.example.app_2.R;
import com.example.app_2.adapters.ExpressionAdapter;
import com.example.app_2.utils.ImageLoader;

public class ExpressionListFragment extends Fragment{
	
	private ExpressionAdapter mAdapter;
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new ExpressionAdapter(getActivity().getApplicationContext());
		HorizontalListView listview = (HorizontalListView) getActivity().findViewById(R.id.horizontal_listview);
		listview.setAdapter(mAdapter);
		
		new ImageLoader(getActivity());	
	}
	
	public void addImageToAdapter(String path){
		mAdapter.addImageToAdapter(path);
		
	}
	
}