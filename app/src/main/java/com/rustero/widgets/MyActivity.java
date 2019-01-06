package com.rustero.widgets;


import android.support.v7.app.AppCompatActivity;

import com.rustero.App;


public class MyActivity extends AppCompatActivity {


	protected void onResume() {
		super.onResume();
		App.setActivity(this);
	}


	protected void onPause() {
		super.onPause();
		App.setActivity(null);
	}
}
