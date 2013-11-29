package org.sammelbox.android.view.activity;

import org.sammelbox.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;

public class SearchActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

}
