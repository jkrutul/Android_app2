package com.example.app_2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.app_2.views.HorizontalListView;

public class ExpressionListFragment extends Fragment implements OnItemClickListener{
	private ExpressionAdapter mAdapter;
	private ImageGridActivity executing_actv;
	//private int isMale;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		executing_actv = (ImageGridActivity) getActivity();
		mAdapter = new ExpressionAdapter(getActivity().getApplicationContext());
		HorizontalListView listview = (HorizontalListView) getActivity().findViewById(R.id.horizontal_listview);
		
		
		/*
		 * SharedPreferences sharedPref = getActivity().getSharedPreferences("USER",Context.MODE_PRIVATE);			// pobranie informacji o zalogowanym u¿ytkowniku
		 * isMale = sharedPref.getInt("is_male",1);
		 */

		
		
		
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
		for(ImageObject io : ExpressionAdapter.dataObjects){	
			if(ImageGridActivity.isMale == 1)
				if(io.getTts_m() != null && !io.getTts_m().isEmpty())
					expression+=io.getTts_m();
				else 
					expression+=io.getDescription();
			else
				if(io.getTts_f() != null &&  !io.getTts_f().isEmpty())
					expression+=io.getTts_f();
				else 
					expression+=io.getDescription();
				
			if(!expression.isEmpty())
				expression+=" ";
		}
		
		executing_actv.speakOut(expression);
		ExpressionAdapter.incrUseCounter();
		
	}
}