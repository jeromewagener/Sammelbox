package org.sammelbox.android.view.activity;

import org.sammelbox.R;
import org.sammelbox.android.GlobalState;
import org.sammelbox.android.controller.DatabaseQueryOperation;
import org.sammelbox.android.model.SimplifiedAlbumItemResultSet;
import org.sammelbox.android.view.AlbumItemList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class AlbumItemBrowserActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_item_browser);
		
		TextView heading = (TextView)findViewById(R.id.lblAlbumItemBrowserHeading);
		heading.setText(GlobalState.getSelectedAlbum());
		
		SimplifiedAlbumItemResultSet simplifiedAlbumItemResultSet = DatabaseQueryOperation.getAllAlbumItemsFromAlbum(this);
		
		AlbumItemList adapter = new AlbumItemList(
				AlbumItemBrowserActivity.this,
				simplifiedAlbumItemResultSet.getResultSetImagesAsDrawableArray(),
				simplifiedAlbumItemResultSet.getResultSetDataAsStringArray());
		
		ListView albumItems = (ListView)findViewById(R.id.albumItems);
		albumItems.setAdapter(adapter);
		albumItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO show other images if album item has multiple
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.album_item_browse, menu);
		return true;
	}

}
