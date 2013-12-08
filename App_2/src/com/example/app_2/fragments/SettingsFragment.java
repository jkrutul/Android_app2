package com.example.app_2.fragments;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.example.app_2.R;
import com.example.app_2.utils.ImageLoader.BitmapWorkerTask;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		Preference pref = findPreference("pref_img_size");
		pref.setSummary(sp.getString("pref_img_size", ""));
		pref = findPreference("pref_img_desc_font_size");
		pref.setSummary(sp.getString("pref_img_desc_font_size", ""));
		
		sp.registerOnSharedPreferenceChangeListener(this);
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("pref_img_size")){
			Preference pref = findPreference(key);
			pref.setSummary(sharedPreferences.getString(key, ""));
		}
		else if(key.equals("pref_img_desc_font_size")){
			Preference pref = findPreference(key);
			pref.setSummary(sharedPreferences.getString(key, ""));
		}

	}

}
