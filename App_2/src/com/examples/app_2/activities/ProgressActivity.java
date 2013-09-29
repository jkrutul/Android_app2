package com.examples.app_2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.example.app_2.R;
import com.example.app_2.provider.Images.ThumbsProcessTask;

public class ProgressActivity extends Activity {
	private static final int PROGRESS = 0x1;

	private ProgressBar mProgress;
	public static int mProgressStatus = 0;

	private Handler mHandler = new Handler();

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_progress);

		mProgress = (ProgressBar) findViewById(R.id.progress_bar);

		// Start lengthy operation in a background thread
		new Thread(new Runnable() {
			public void run() {
				while (mProgressStatus < 100) {

					//new ThumbsProcessTask().execute();
					// Update the progress bar
					mHandler.post(new Runnable() {
						public void run() {
							mProgress.setProgress(mProgressStatus);
						}
					});
				}
			}
		}).start();
	}

}
