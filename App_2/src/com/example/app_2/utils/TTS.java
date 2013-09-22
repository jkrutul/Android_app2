package com.example.app_2.utils;
import java.util.Arrays;
import java.util.Locale;

import com.example.app_2.App_2;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TTS implements TextToSpeech.OnInitListener {
	private static TTS instance;
	
	public static final int MY_DATA_CHECK_CODE = 1;
	private static TextToSpeech tts;
	
	private TTS(){ }
	
	public static TTS getInstance(){
		if(instance==null)
			instance = new TTS();
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		App_2.actvity.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		return instance;
		
	}

	@Override
	public void onInit(int status) {
		int result;
		if (status == TextToSpeech.SUCCESS) {
	        Locale[] AvalLoc = Locale.getAvailableLocales();

	        Log.i("TTS","Available locales " + Arrays.toString(AvalLoc));
	        
			Locale pol_loc = new Locale("en", "EN");
			if(TextToSpeech.LANG_AVAILABLE ==tts.isLanguageAvailable(pol_loc)){
				result = tts.setLanguage(pol_loc);
				}
			else{
				result=tts.setLanguage(Locale.ITALIAN);
			}
				
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "LANG_NOT_SUPPORTED");
			} else {
				//btnSpeak.setEnabled(true);
				speakOut("TTS init sucesfull");
			}

		} else {
			Log.e("TTS", "Initialization Failed");
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, crate the TTS instance
				tts = new TextToSpeech(App_2.getAppContext(), this);
			} else {
				// missing data, install it
				Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				App_2.actvity.startActivity(installIntent);
			}
		}
	}
	
	public static void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	public static void TTSshutdown(){
		if(tts != null){
			tts.stop();
			tts.shutdown();
		}
	}
}
