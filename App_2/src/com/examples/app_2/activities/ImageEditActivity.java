package com.examples.app_2.activities;

import com.example.app_2.R;
import com.example.app_2.fragments.ImageDetailsFragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;

public class ImageEditActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
    }

}
