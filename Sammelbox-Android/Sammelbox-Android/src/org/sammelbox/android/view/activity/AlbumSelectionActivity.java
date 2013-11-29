package org.sammelbox.android.view.activity;

import java.util.Arrays;
import java.util.Map;

import org.sammelbox.R;
import org.sammelbox.android.GlobalState;
import org.sammelbox.android.controller.DatabaseQueryOperation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AlbumSelectionActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_selection);
		
		final Map<String,String> albumNameToTableName = DatabaseQueryOperation.getAlbumNamesToAlbumTablesMapping(this);
		final String[] albumNames = Arrays.copyOf(albumNameToTableName.keySet().toArray(), albumNameToTableName.keySet().toArray().length, String[].class);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumNames);
		ListView albumList = (ListView)findViewById(R.id.listAlbums);
		albumList.setAdapter(adapter);
		
		albumList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				GlobalState.setSelectedAlbum(albumNames[position]);
				GlobalState.setAlbumNameToTableName(albumNameToTableName);
				
				Intent openAlbumItemListToBrowse = new Intent(AlbumSelectionActivity.this, AlbumItemBrowserActivity.class);
                startActivity(openAlbumItemListToBrowse);
            }
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.album_selection, menu);
		return true;
	}

}
