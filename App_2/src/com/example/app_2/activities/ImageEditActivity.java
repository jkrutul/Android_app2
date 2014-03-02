package com.example.app_2.activities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ActionBar.OnNavigationListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.actionbar.adapter.TitleNavigationAdapter;
import com.example.app_2.actionbar.model.SpinnerNavItem;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.fragments.ImageDetailsFragment;
import com.example.app_2.fragments.ImageListFragment;
import com.example.app_2.spinner.model.ImageSpinnerItem;
import com.example.app_2.storage.Database;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.Utils;

public class ImageEditActivity extends FragmentActivity implements OnNavigationListener{
	private static ImageListFragment ilf;
    private ArrayList<SpinnerNavItem> navSpinner;
    private TitleNavigationAdapter title_nav_adapter;
    
	private String newDbFilePath;
	
	ArrayList<ImageSpinnerItem> items;
	private final static int TAKE_PIC_REQUEST = 86;
	private final static int FILE_SELECT_REQUEST = 25;
	private final static int DB_SELECT_REQUEST = 24;
	
	private final static String IMAGE_LIST_FRAGMENT= "image_list_fragment";
	

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_image_edit);
        ilf = new ImageListFragment();
        
        
        if (getSupportFragmentManager().findFragmentByTag(IMAGE_LIST_FRAGMENT) == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fcontainer, ilf, IMAGE_LIST_FRAGMENT);
            ft.commit();
        }
        
    	navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Alfabetycznie", R.drawable.sort_ascend));
		navSpinner.add(new SpinnerNavItem("Ostatnio zmodyfikowane", R.drawable.clock));
		navSpinner.add(new SpinnerNavItem("Najczêœciej u¿ywane", R.drawable.favourites));
		
		title_nav_adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
		ActionBar actionBar = getActionBar();
		actionBar.setListNavigationCallbacks(title_nav_adapter, this);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_green_dark))); 
       
    }
    
    
    @Override
    protected void onResume() {
    	super.onResume();
		//getSupportLoaderManager().initLoader(0, null, (LoaderCallbacks<Cursor>)ilf);
    	//ilf.getLoaderManager().restartLoader(0, ilf.getArguments(), ilf);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	//ilf = null;
    };
         
	// Create the menu based on the XML defintion
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.take_pic:
			i = new Intent(this, NewImgTemplateActivity.class);
			startActivity(i);
			return true;
		case R.id.add_folder:
			i = new Intent(this, AddImagesFromFolderActivity.class);
			startActivity(i);
			return true;
		case R.id.adduser:
			i = new Intent(this, AddUserActivity.class);
			startActivity(i);
			return true;
			
		case R.id.backup_db:
			 AlertDialog.Builder alert = new AlertDialog.Builder(this);

			  alert.setTitle("Tworzenie kopii zapasowej bazy danych");
			  alert.setMessage("Nazwa pliku kopii bazy danych");

			  // Set an EditText view to get user input 
			  final EditText input = new EditText(this);

			  
			  SimpleDateFormat dateFormat= new SimpleDateFormat("HH_mm_ss_dd_MM_yyyy");
			  Date date = new Date();
			  input.setText(dateFormat.format(date));
			  alert.setView(input);

			  alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
				    String user_filename = input.getText().toString();
				    Database.backupDb(user_filename);
					Toast.makeText(getApplicationContext(), "Baza "+user_filename+" zosta³a zapisana", Toast.LENGTH_LONG).show();
					Database.open();
			    }
			  });

			  alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int whichButton) {
			      // Canceled.
			    }
			  });

			  alert.show();
			  		
			return true;
		
		case R.id.import_db:
			i = new Intent(this, FilesSelectActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("single_select", true);
			bundle.putString("DIR_PATH", Storage.getBackupDir().getAbsolutePath());
			i.putExtras(bundle);
			startActivityForResult(i, DB_SELECT_REQUEST);
			/*
			if(Database.importDb(newDbFilePath)){
				Toast.makeText(getApplicationContext(), "Baza zosta³a zapisana", Toast.LENGTH_LONG).show();
				Database.open();
			}else{
				Toast.makeText(getApplicationContext(), "Baza nie zosta³a zapisana", Toast.LENGTH_LONG).show();
			}
			*/
			return true;

			
		}
		

			
		return super.onOptionsItemSelected(item);
	}

	  public void onButtonClick(View view){
			switch(view.getId()){
				case R.id.id_submit_button:
					ImageDetailsFragment idf = (ImageDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details);
					boolean mDualPane = idf != null && idf.getView().getVisibility() == View.VISIBLE;
					if(mDualPane)
						idf.onButtonClick(view);
				    Toast.makeText(this, "Zmiany zosta³y zapisane", Toast.LENGTH_SHORT).show();				        			
			}
	  }
	  

	  
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			
			switch (requestCode) {
			case FILE_SELECT_REQUEST:
				if (resultCode == RESULT_OK) {
					//Intent i = new Intent(this, NewImgTemplateActivity.class);
					//startActivity(i);
				}
				break;
			case TAKE_PIC_REQUEST:
				if (resultCode == RESULT_OK) {
					//Intent i = new Intent(this, NewImgTemplateActivity.class);
					//startActivity(i);
				}
				break;
			
			case DB_SELECT_REQUEST:
				if (resultCode == RESULT_OK) {
					if(data.getExtras().containsKey("SELECTED_FILE_DIR")){
						String pathToImportedDb = data.getStringExtra("SELECTED_FILE_DIR");
						if(Utils.getExtention(Utils.getFilenameFromPath(pathToImportedDb)).equals("db") && Database.importDb(pathToImportedDb)){
							Toast.makeText(getApplicationContext(), "Baza zosta³a zaimportowana", Toast.LENGTH_LONG).show();
							Database.open();
						}else{
							Toast.makeText(getApplicationContext(), "Wyst¹pi³ b³¹d, importowanie bazy nie powiod³o siê", Toast.LENGTH_LONG).show();
						}
					}
						
				}
				break;

			}
		}


		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {

			
			if(ilf.isVisible()){
				Bundle args = new Bundle();	
				switch (itemPosition) {
				
				case 0: // alfabetycznie 
					ilf.sortOrder = "i."+ImageContract.Columns.DESC + " COLLATE LOCALIZED ASC";			
					ilf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) ilf);
					break;
				case 1: // ostatnio zmodyfikowane
					ilf.sortOrder = "i."+ImageContract.Columns.MODIFIED + " DESC";
					ilf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) ilf);
					break;
				case 2: // najczêœciej u¿ywane
					ilf.sortOrder = "i."+ImageContract.Columns.TIME_USED + " DESC";
					ilf.getLoaderManager().restartLoader(0, null, (LoaderCallbacks<Cursor>) ilf);
					break;
		
				default:
					break;
				}
			}
			
			
		
		return false;
		}



		
}
