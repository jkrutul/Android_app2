package com.example.app_2.activities;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.example.app_2.R;
import com.example.app_2.fragments.ParentMultiselectFragment;

public class ParentMultiselectActivity extends FragmentActivity{
	private Long row_id;
	ParentMultiselectFragment parent;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.activity_parent_multiselect);
		findViewById(R.id.parents_list_fragment);
    	row_id = getIntent().getExtras().getLong("row_id");
		//row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id");
		parent = ParentMultiselectFragment.newInstance(row_id);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.parents_list_fragment, parent);
		ft.commit();
		//pmf = (ParentMultiselectFragment) getSupportFragmentManager().findFragmentById(R.id.parents_list_fragment);
		//pmf.setArguments(getIntent().getExtras());

	}
	
	
	public void onButtonClick(View view){
		Intent returnIntent = new Intent();
		
		switch (view.getId()) {
		case R.id.parent_submit_button:
			 ArrayList<Long>ids = parent.getCheckedItemIds();
			 long id[] = new long[ids.size()];
			 int i=0;
			 for(Long iis : ids){
				 id[i] = iis;
			 	 i++;
			 }
			 returnIntent.putExtra("result",id);
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
