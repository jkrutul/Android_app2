package com.example.app_2.activities;

import com.example.app_2.R;
import com.example.app_2.fragments.ImageDetailsFragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class ImageEditActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_image_edit);
    }
    
    
	// Create the menu based on the XML defintion
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.insert:
			Intent i = new Intent(this, AddNewImageActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	  public void onButtonClick(View view){
			switch(view.getId()){
				case R.id.submit_button:
			       // if (TextUtils.isEmpty(mTitleText.getText().toString())) {
				          //makeToast();
				     //   } else {
				          setResult(RESULT_OK);
				          Toast.makeText(this, "Zmiany zosta³y zapisane", Toast.LENGTH_SHORT).show();
				          
				          //finish();
				        			
			}
	  }

}
