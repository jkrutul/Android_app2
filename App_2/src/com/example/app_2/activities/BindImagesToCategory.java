package com.example.app_2.activities;


import java.util.ArrayList;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.fragments.ImageGridFragment;
import com.example.app_2.fragments.ImagesMultiselectFragment;

public class BindImagesToCategory extends FragmentActivity{
	ImagesMultiselectFragment imf;
	int executing_category_id;
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		executing_category_id = ImageGridActivity.actual_category_fk;
		//if(bundle!= null)
		//	executing_category_id = bundle.getLong("executing_category_id");
		
		setContentView(R.layout.activity_bind_images_to_category);
		
        imf = new ImagesMultiselectFragment();
        
    	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
   	 	ft.replace(R.id.list_fragment_container, imf);
        ft.commit();
	}
	
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.bi_confirm_button:
			
				ArrayList<Long> checked_list = imf.getCheckedItemIds();
				ContentValues[] cvArray  = new ContentValues[checked_list.size()];
				int i =0;
				for(Long image_fk :checked_list){
					ContentValues cv = new ContentValues();
					cv.put(ParentContract.Columns.IMAGE_FK, image_fk);
					cv.put(ParentContract.Columns.PARENT_FK, executing_category_id);
					cvArray[i++] = cv;
				}
				App_2.getAppContext().getContentResolver().bulkInsert(ParentContract.CONTENT_URI, cvArray);
				
			finish();
			break;
		case R.id.bi_cancel_button:
			finish();
		default:
			break;
		}
	}

}
