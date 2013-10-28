package com.example.app_2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;

import com.example.app_2.R;
import com.example.app_2.fragments.ParentMultiselectFragment;

public class ParentMultiselectActivity extends FragmentActivity{
	
	private ParentMultiselectFragment pmf;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_multiselect);
		findViewById(R.id.parents_list_fragment);
		pmf = (ParentMultiselectFragment) getSupportFragmentManager().findFragmentById(R.id.parents_list_fragment);
	}
	
	
	public void onButtonClick(View view){
		Intent returnIntent = new Intent();
		
		switch (view.getId()) {
		case R.id.parent_submit_button:
			 ArrayList<Long> ids = pmf.getCheckedItemIds();
			 returnIntent.putExtra("result",ids.toString());
			 setResult(RESULT_OK,returnIntent);     
			 finish();
			break;
		case R.id.parent_cancel_button:
			setResult(RESULT_CANCELED, returnIntent);        
			finish();
			break;

		default:
			setResult(RESULT_CANCELED, returnIntent);        
			finish();
			break;
		}
	}
}
