/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.app_2.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.widget.LinearLayout;

import com.example.app_2.App_2;
import com.example.app_2.activities.ImageGridActivity;
import com.sonyericsson.util.ScalingUtilities;
import com.sonyericsson.util.ScalingUtilities.ScalingLogic;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private Utils() {};
    
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        return sb.toString();
    }

    @TargetApi(11)
    public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder.setClassInstanceLimit(ImageGridActivity.class, 1);
                //.setClassInstanceLimit(ImageDetailActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
    public static String cutExtention(String filename){
    	if(filename != null){
	    	String[] fn = filename.split("\\.");
	    	return fn[0].replace("_", "");
	    	}
    	else
    		return null;
    }
    
    public static String cutOnlyExtention(String filename){
    	String[] fn = filename.split("\\.");
    	return fn[0];
    }
    
    public static String getExtention(String filename){
    	String[] fn = filename.split("\\.");
    	if(fn.length>1)
    		return fn[1];
    	else
    		return "";
    }
    
    public static String getFilenameFromPath(String path){
    	if(path != null){
	    	String[] fn = path.split("\\/");
	    	if(fn.length>1)
	    		return fn[fn.length-1];
	    	else
	    		if(fn.length>0)
	    			return fn[0];
	    		else
	    			return null;
    	}else
    		return null;
    	//File f = new File(path);
    	//return f.getName();
    }
    
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
    
    public static String hashKey(byte file) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(file);
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(file);
        }
        return cacheKey;
    }
	
    public static boolean verifyResolves(Intent intent) {
		PackageManager packageManager = App_2.getAppContext().getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(
				intent, PackageManager.PERMISSION_GRANTED);
		return activities.size() > 0;
	}
    
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

	public static String getPath(Context context, Uri uri) {
	    if ("content".equalsIgnoreCase(uri.getScheme())) {
	        String[] projection = { "_data" };
	        Cursor cursor = null;

	        try {
	            cursor = context.getContentResolver().query(uri, projection, null, null, null);
	            int column_index = cursor.getColumnIndexOrThrow("_data");
	            if (cursor.moveToFirst()) {
	                return cursor.getString(column_index);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	} 


	public static void setWallpaper(android.view.ViewGroup vg, int reqWidth, int reqHeight, Bitmap wallpaper, ScalingLogic sl){
		
		
		if(wallpaper == null){
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(App_2.getAppContext());
			Drawable wallpaperDrawable = wallpaperManager.getDrawable();
			wallpaper = BitmapCalc.drawableToBitmap(wallpaperDrawable);
		}
		
		if(reqHeight == 0  || reqWidth == 0 ){
			reqHeight = App_2.getMaxHeight();
			reqWidth = App_2.getMaxWidth();
		}
		
		Resources  r = App_2.getAppContext().getResources();
		int orientation = r.getConfiguration().orientation;
		

		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:				// landscape
			Bitmap wallpaperLandscape = ScalingUtilities.createScaledBitmap(wallpaper, reqHeight, reqWidth, sl);
			if(Utils.hasJellyBean())
				vg.setBackground(new BitmapDrawable(r,wallpaperLandscape));
			else{
				 if(vg instanceof LinearLayout){
					 LinearLayout ll = (LinearLayout) vg;
					 ll.setBackgroundDrawable(new BitmapDrawable(r,wallpaperLandscape));
				 }else if(vg instanceof DrawerLayout){
					 DrawerLayout dl = (DrawerLayout) vg;
					 dl.setBackgroundDrawable(new BitmapDrawable(r, wallpaperLandscape));
				 }
			 	
			}
			//wallpaperLandscape.recycle();
			break;
		case Configuration.ORIENTATION_PORTRAIT:				// portrait
			Bitmap wallpaperPortrait = ScalingUtilities.createScaledBitmap(wallpaper, reqWidth, reqHeight, sl);

			if(Utils.hasJellyBean())
				vg.setBackground(new BitmapDrawable(r, wallpaperPortrait));
			else{
				 if(vg instanceof LinearLayout){
					 LinearLayout ll = (LinearLayout) vg;
					 ll.setBackgroundDrawable(new BitmapDrawable(r,wallpaperPortrait));
				 }else if(vg instanceof DrawerLayout){
					 DrawerLayout dl = (DrawerLayout) vg;
					 dl.setBackgroundDrawable(new BitmapDrawable(r, wallpaperPortrait));
				 }
			}
			//wallpaperPortrait.recycle();
			break;
		default:
			//ll.setBackgroundDrawable(App_2.wallpaperDrawable);
			break;
		}
	}
	

}
