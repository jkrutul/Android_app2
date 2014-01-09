package com.example.app_2.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.app_2.R;
import com.example.app_2.activities.ImageGridActivity;
import com.example.app_2.adapters.ExpressionAdapter;
import com.example.app_2.models.ImageObject;
import com.example.app_2.utils.ImageLoader;
import com.example.app_2.views.HorizontalListView;

public class ExpressionListFragment extends Fragment implements OnItemClickListener{
	private ExpressionAdapter mAdapter;
	private ImageGridActivity executing_actv;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		executing_actv = (ImageGridActivity) getActivity();
		mAdapter = new ExpressionAdapter(getActivity().getApplicationContext());
		HorizontalListView listview = (HorizontalListView) getActivity().findViewById(R.id.horizontal_listview);
		
		
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(this);
		
		//new ImageLoader(getActivity());	
	}
	
	public void addImageToExAdapter(ImageObject image_object){
		mAdapter.addImageToExpressionAdapter(image_object);
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
/*		if(doubleBackToExitPressedOnce){
			ExpressionAdapter.dataObjects.remove(position);
			mAdapter.notifyDataSetChanged();  
			doubleBackToExitPressedOnce = false;
			return;
		}
	
		doubleBackToExitPressedOnce = true;
*/	
		ImageObject iobj = ExpressionAdapter.dataObjects.get(position);
		 if(!TextUtils.isEmpty(iobj.getDescription()))
			 executing_actv.speakOut(iobj.getDescription());
		 
/*		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				doubleBackToExitPressedOnce =false;
			}
		},2000); 
	
*/
	}
	
	
	public void removeAllImages(){
		mAdapter.removeAllImages();
	}
	
	public void speakOutExpression(){
		String expression = new String();
		for(ImageObject io : ExpressionAdapter.dataObjects)
			expression+=io.getDescription()+", ";
		
		executing_actv.speakOut(expression);
		ExpressionAdapter.incrUseCounter();
		
	}
}