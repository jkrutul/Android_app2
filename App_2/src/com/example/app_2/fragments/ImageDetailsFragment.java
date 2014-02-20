package com.example.app_2.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.app_2.App_2;
import com.example.app_2.R;
import com.example.app_2.activities.ImageDetailsActivity;
import com.example.app_2.contentprovider.ImageContract;
import com.example.app_2.contentprovider.ParentContract;
import com.example.app_2.contentprovider.ParentsOfImageContract;
import com.example.app_2.contentprovider.UserContract;
import com.example.app_2.intents.ImageIntents;
import com.example.app_2.storage.Storage;
import com.example.app_2.utils.ImageLoader;
//import android.widget.ArrayAdapter;

public class ImageDetailsFragment extends Fragment{
	private final static String IMAGE_LIST_FRAGMENT= "image_list_fragment_tag";
	
	private static Bitmap bitmap; // ?
	private TextView mId, mSymbolInfo;
	
	private EditText mTTSMaleET, mTTSFemaleET, mImgDescET;
	private RadioButton mIsCategoryRB, mAddToExprRB, mAddToLlRB, mIsNOTCategoryRB, mNOTAddToExprRB, mNOTAddToLlRB;
	private ImageView mImage;
	private TextView mAddToLLInfo1;
	private View mAddToLLInfo2;
	private RadioGroup mAddToLLRG; 	

	//private Map<String, Long> categories_map;
	List<String> list = new ArrayList<String>();

	private Uri imageUri;
	private static Long row_id;
	public static final int TAKE_PIC_REQUEST = 2;
	public static final int FILE_SELECT_REQUEST = 3;
	private static Activity executing_activity;



	

	public static ImageDetailsFragment newInstance(Long id) {
		ImageDetailsFragment f = new ImageDetailsFragment();
		Bundle args = new Bundle();
		args.putLong("row_id", id);
		row_id = id;
		f.setArguments(args);
		return f;

	}

	public Long getShownId() {
		return getArguments().getLong("row_id", -1);
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		executing_activity=getActivity();
		// if(row_id == null)
		row_id = (bundle == null) ? null : (Long) bundle.getLong("row_id");
		if (row_id == null) {
			if (this.getArguments() != null)
				row_id = (Long) getArguments().get("row_id");
		}

		imageUri = Uri.parse(ImageContract.CONTENT_URI + "/" + row_id);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		executing_activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if (container == null) {}
		
		final View view = inflater.inflate(R.layout.image_details, container,false);
		initViews(view);

		
		
		
		
		/*
		mId = (TextView) view.findViewById(R.id.img_id);
		mImage = (ImageView) view.findViewById(R.id.img);
		mButton = (Button) view.findViewById(R.id.submit_button);
		mTitleText = (TextView) view.findViewById(R.id.edit_name);
		mCategory = (EditText) view.findViewById(R.id.edit_category);
		mDescText = (EditText) view.findViewById(R.id.edit_description);
		mParents = (TextView) view.findViewById(R.id.parents);
		mAuthor = (TextView) view.findViewById(R.id.image_author);

		mIsContextualCategory = (CheckBox) view.findViewById(R.id.is_contextual_category);
		mCreateCategoryCheckBox = (CheckBox) view.findViewById(R.id.create_category);
		mCreateCategoryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked){
							String image_description = mDescText.getText().toString();
							mCategory.setText(image_description);
							mCategory.setVisibility(View.VISIBLE);
							//mIsContextualCategory.setVisibility(View.VISIBLE);
							}
						else
							mCategory.setVisibility(View.INVISIBLE);
							//mIsContextualCategory.setVisibility(View.INVISIBLE);
					}
				});
		
		
		
		categories_map = new HashMap<String, Long>();
		String[] projection = { 
				"i."+ImageContract.Columns._ID,
				"i."+ImageContract.Columns.IS_CATEGORY,
				"i."+ImageContract.Columns.IS_ADD_TO_EXPR};
		String selection = "i."+ImageContract.Columns.IS_CATEGORY + "=1";
		Cursor c = executing_activity.getContentResolver().query(ImageContract.CONTENT_URI, projection, selection, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			categories_map.put(c.getString(1), c.getLong(0));
			c.moveToNext();
		}
		c.close();
		
		*/

		
		if (row_id != null && row_id != 0){
			fillData(row_id);
			//mImage.setClickable(false);
			}
		else{
			//mImage.setClickable(true);
			if(bitmap!=null)
				mImage.setImageBitmap(bitmap);
		}
		return view;

	}
	
	/*
	public void setParentsView(long ids[]){
		mParents.setText("");
		String parents = new String();
		for(Long l : ids)
			if(categories_map.containsKey(l))
				parents +=categories_map.get(l)+"\n";
		mParents.setText(parents);	
	}
	*/


	private void setImageParents(Long id){
		SymbolCategoriesByIdFragment scbif = new SymbolCategoriesByIdFragment();
		Bundle args = new Bundle();
		args.putLong("item_id",id);    
		
		scbif.setArguments(args);
		
        if (getActivity().getSupportFragmentManager().findFragmentByTag(IMAGE_LIST_FRAGMENT) == null) {
            final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.cat_list_content_frame, scbif, IMAGE_LIST_FRAGMENT);
            ft.commit();
        }
	}
	
	
	public static void addParents(long[] ids){
		if(row_id != null){
			List<Long> alreadyBindParentsIds = new ArrayList<Long>();
			Uri imageParentsUri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+row_id);
			String[] projection = { "i."+ImageContract.Columns._ID};
			Cursor c = executing_activity.getContentResolver().query(imageParentsUri, projection, null, null, null);
			c.moveToFirst();

			while(!c.isAfterLast()){
				alreadyBindParentsIds.add(c.getLong(0));
				c.moveToNext();
			}
			
			ContentValues cv = new ContentValues();
			for(Long id : ids){
				if(!alreadyBindParentsIds.contains(id)){
					cv.put(ParentContract.Columns.IMAGE_FK, row_id);
					cv.put(ParentContract.Columns.PARENT_FK, id);
					executing_activity.getContentResolver().insert(ParentContract.CONTENT_URI, cv);
				}

			}
		}
		
	}
	
	public static void deleteParents(long[] ids){
		if(row_id != null){
			List<Long> alreadyBindParentsIds = new ArrayList<Long>();
			Uri imageParentsUri = Uri.parse(ParentsOfImageContract.CONTENT_URI+"/"+row_id);
			String[] projection = { "i."+ImageContract.Columns._ID};
			Cursor c = executing_activity.getContentResolver().query(imageParentsUri, projection, null, null, null);
			c.moveToFirst();

			while(!c.isAfterLast()){
				alreadyBindParentsIds.add(c.getLong(0));
				c.moveToNext();
			}
			
			for(Long id : ids){
				if(alreadyBindParentsIds.contains(id)){
					String[] selectionArgs ={ String.valueOf(row_id), String.valueOf(id) };
					executing_activity.getContentResolver().delete(ParentContract.CONTENT_URI, ParentContract.Columns.IMAGE_FK +" =? AND "+ ParentContract.Columns.PARENT_FK+ "=? ",selectionArgs );
				}

			}
		}
	}
	
	/*
	public void onCancelClick(View view){
		switch (view.getId()) {
		case R.id.cancel_button:
			
			break;

		default:
			break;
		}
	}
	*/
	
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.id_submit_button:
			saveState();
			break;
		case R.id.id_cancel_button: 
			Log.i("idf", "cancel clicked");
			break;
			
		case R.id.img:
			final Activity a = getActivity();
			final Fragment f = this;
			AlertDialog.Builder builder = new AlertDialog.Builder(a);	// Add the buttons
			builder.setPositiveButton("zrób zdjêcie",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

							ImageIntents.cameraIntent(a, f, TAKE_PIC_REQUEST, App_2.maxWidth, App_2.getMaxWidth());
						}
					});
			builder.setNegativeButton("wybierz obrazek",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							ImageIntents.selectImageIntent(a, f , FILE_SELECT_REQUEST, App_2.maxWidth, App_2.getMaxWidth());
						}
					});

			AlertDialog dialog = builder.create();
			dialog.show();
			break;

		default:
			break;
		}


		if(executing_activity instanceof ImageDetailsActivity)
			executing_activity.finish();
	}

	
	
	private void fillData(Long id) {
		Uri uri = Uri.parse(ImageContract.CONTENT_URI + "/" + id);
		String[] projection = { 
				"i."+ImageContract.Columns._ID,						//0
				"i."+ImageContract.Columns.FILENAME,				//1
				"i."+ImageContract.Columns.DESC,					//2
				"i."+ImageContract.Columns.TTS_M,					//3
				"i."+ImageContract.Columns.TTS_F,					//4	
				"i."+ImageContract.Columns.IS_CATEGORY,				//5
				"i."+ImageContract.Columns.IS_ADD_TO_EXPR, 			//6
				"i."+ImageContract.Columns.IS_ADD_TO_CAT_LIST,		//7
				"u."+UserContract.Columns.USERNAME};				//8
		
		Cursor cursor = executing_activity.getContentResolver().query(uri,	projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			String filename, desc, tts_m, tts_f, username, symbolInfo;
			boolean isCategory, isAddToExpr, isAddToCatList;
			
			filename = cursor.getString(1);
			desc = cursor.getString(2);
			tts_m = cursor.getString(3);
			tts_f = cursor.getString(4);
			isCategory = (cursor.getInt(5) == 1) ? true : false;
			isAddToExpr = (cursor.getInt(6) == 1) ? true : false;
			isAddToCatList = (cursor.getInt(7) == 1) ? true : false;
			username = cursor.getString(8);
			
			
			mId.setText(Long.toString(id));
			mImgDescET.setText(desc);
			mTTSMaleET.setText(tts_m);
			mTTSFemaleET.setText(tts_f);
			
			mIsCategoryRB.setChecked(isCategory);
			mIsNOTCategoryRB.setChecked(!isCategory);
			
			mAddToExprRB.setChecked(isAddToExpr);
			mNOTAddToExprRB.setChecked(!isAddToExpr);
			
			mAddToLlRB.setChecked(isAddToCatList);
			mNOTAddToLlRB.setChecked(!isAddToCatList);
			
			mSymbolInfo.setText("ID: "+ id + "\nU¿ytkownik: "+username);
			cursor.close();
			ImageLoader.loadBitmap(Storage.getPathToScaledBitmap(filename, 150), mImage,150);
			setImageParents(id);
		}
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		if (resultCode == Activity.RESULT_OK) {
			if(requestCode == TAKE_PIC_REQUEST || requestCode == FILE_SELECT_REQUEST){
				Uri uri = data.getData();
				if(uri != null)
					pathToNewImage = Utils.getPath(getActivity(), uri);
				else
					pathToNewImage = Storage.readFromPreferences(null,"photoPath", getActivity(), Activity.MODE_PRIVATE);
				
				filename = Utils.getFilenameFromPath(pathToNewImage);
				String title = Utils.cutExtention(filename);
				mTitleText.setText(title);
				mDescText.setText(title);
				mCategory.setText(title);
				bitmap = BitmapCalc.decodeSampleBitmapFromFile(pathToNewImage,	mImage.getWidth(), mImage.getHeight());
				mImage.setImageBitmap(bitmap);
			}
		}
		*/
	}
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (!imageUri.getLastPathSegment().equals("null")) {
			outState.putLong("row_id",
					Long.valueOf(imageUri.getLastPathSegment()));
			saveState();
		}

		// outState.putParcelable(ImageContract.CONTENT_ITEM_TYPE, imageUri);
	}

	@Override
	public void onPause() {
		super.onPause();
		//saveState();
	}

	private void saveState() {
		
		String filename, desc, tts_m, tts_f, username, symbolInfo;
		boolean isCategory, isAddToExpr, isAddToCatList;
		
		
		desc = mImgDescET.getText().toString();
		tts_m = mTTSMaleET.getText().toString();
		tts_f = mTTSFemaleET.getText().toString();
		isCategory = mIsCategoryRB.isChecked();
		isAddToExpr = mAddToExprRB.isChecked();
		isAddToCatList = mAddToLlRB.isChecked();
		
		
		


		ContentValues values = new ContentValues();
		values.put(ImageContract.Columns.DESC, desc);
		values.put(ImageContract.Columns.TTS_M, tts_m);
		values.put(ImageContract.Columns.TTS_F, tts_f);
		values.put(ImageContract.Columns.IS_CATEGORY, isCategory);
		values.put(ImageContract.Columns.IS_ADD_TO_CAT_LIST, isAddToExpr);
		values.put(ImageContract.Columns.IS_ADD_TO_EXPR, isAddToCatList);

		/*
		if (row_id == null && this.filename!=null) {
			values.put(ImageContract.Columns.FILENAME, this.filename);
			imageUri = executing_activity.getContentResolver().insert(ImageContract.CONTENT_URI, values);
		} else {
		*/
		executing_activity.getContentResolver().update(imageUri, values, null,	null);
		//}
		

	}
	
	private void initViews(View view){		
		mId = (TextView) view.findViewById(R.id.img_id);
		mImage = (ImageView) view.findViewById(R.id.img);
		mImgDescET = (EditText) view.findViewById(R.id.img_desc);
		mSymbolInfo = (TextView) view.findViewById(R.id.symbol_info);
		mTTSMaleET =  (EditText) view.findViewById(R.id.tts_male);
		mTTSFemaleET = (EditText) view.findViewById(R.id.tts_female);
		mIsCategoryRB = (RadioButton) view.findViewById(R.id.category_yes);
		mIsNOTCategoryRB = (RadioButton) view.findViewById(R.id.category_no);
		mAddToExprRB = (RadioButton) view.findViewById(R.id.add_to_expr_yes);
		mNOTAddToExprRB = (RadioButton) view.findViewById(R.id.add_to_expr_no);
		mAddToLlRB = (RadioButton) view.findViewById(R.id.add_to_ll_yes);
		mNOTAddToLlRB = (RadioButton) view.findViewById(R.id.add_to_ll_no);
		mAddToLLInfo1 = (TextView) view.findViewById(R.id.addToLLInfo1);
		mAddToLLInfo2 = (View) view.findViewById(R.id.addToLLInfo2);
		mAddToLLRG = (RadioGroup) view.findViewById(R.id.addToLL_RG);
				
		mIsCategoryRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					mAddToLLInfo1.setVisibility(View.VISIBLE);
					mAddToLLInfo2.setVisibility(View.VISIBLE);
					mAddToLLRG.setVisibility(View.VISIBLE);
				}else{
					mAddToLLInfo1.setVisibility(View.GONE);
					mAddToLLInfo2.setVisibility(View.GONE);
					mAddToLLRG.setVisibility(View.GONE);
				}
			}
		});
		
		
		
	}
	

}
