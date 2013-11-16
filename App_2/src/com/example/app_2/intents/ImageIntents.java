package com.example.app_2.intents;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.example.app_2.storage.Storage;
import com.example.app_2.utils.Utils;

public class ImageIntents {

		public static final int FILE_SELECT_REQUEST= 12;
		public static final int TAKE_PIC_REQUEST = 24;
		

	public static void selectImageIntent(Activity a, int requestCode){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		File f = Storage.createTempImageFile(); // tworzy tymczasowy plik
		String mCurrentPhotoPath = f.getAbsolutePath();
		Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", a, Activity.MODE_PRIVATE);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("aspectX",1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
		try {
			a.startActivityForResult(Intent.createChooser(intent, "Wybierz obrazek"),	requestCode);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(a.getApplicationContext(),"Proszê zainstalowaæ menad¿er plików", Toast.LENGTH_SHORT).show();
		}
	}
	

	public static void cameraIntent(Activity a, int requestCode){
		Intent camera = new Intent(	MediaStore.ACTION_IMAGE_CAPTURE);
		if (Utils.verifyResolves(camera)) {
			File f = Storage.createTempImageFile(); // tworzy tymczasowy plik
			String mCurrentPhotoPath = f.getAbsolutePath();
			Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", a, Activity.MODE_PRIVATE);
			camera.putExtra("crop", "true");
			camera.putExtra("outputX", 150);
			camera.putExtra("outputY", 150);
			camera.putExtra("aspectX",1);
			camera.putExtra("aspectY", 1);
			camera.putExtra("scale", true);
			camera.putExtra("return-data", true);
			camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			camera.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
			try{
				a.startActivityForResult(camera, requestCode);
			}catch (android.content.ActivityNotFoundException ex){
				Toast.makeText(a.getApplicationContext(),"Brak aparatu???", Toast.LENGTH_SHORT).show();
			}
		}		
	}
	
	public static void selectImageIntent(Activity a, Fragment f, int requestCode){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		File tmp_f = Storage.createTempImageFile(); // tworzy tymczasowy plik
		String mCurrentPhotoPath = tmp_f.getAbsolutePath();
		Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", a, Activity.MODE_PRIVATE);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("aspectX",1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmp_f));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
		try {
			f.startActivityForResult(Intent.createChooser(intent, "Wybierz obrazek"),	requestCode);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(a.getApplicationContext(),"Proszê zainstalowaæ menad¿er plików", Toast.LENGTH_SHORT).show();
		}
	}
	

	public static void cameraIntent(Activity a, Fragment f, int requestCode){
		Intent camera = new Intent(	MediaStore.ACTION_IMAGE_CAPTURE);
		if (Utils.verifyResolves(camera)) {
			File tmp_f = Storage.createTempImageFile(); // tworzy tymczasowy plik
			String mCurrentPhotoPath = tmp_f.getAbsolutePath();
			Storage.saveToPreferences(mCurrentPhotoPath,"photoPath", a, Activity.MODE_PRIVATE);
			camera.putExtra("crop", "true");
			camera.putExtra("outputX", 150);
			camera.putExtra("outputY", 150);
			camera.putExtra("aspectX",1);
			camera.putExtra("aspectY", 1);
			camera.putExtra("scale", true);
			camera.putExtra("return-data", true);
			camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmp_f));
			camera.putExtra("outputFormat", Bitmap.CompressFormat.JPEG);
			try{
			f.startActivityForResult(camera, requestCode);
			}catch (android.content.ActivityNotFoundException ex){
				Toast.makeText(a.getApplicationContext(),"Brak aparatu???", Toast.LENGTH_SHORT).show();
			}
		}		
	}
}
