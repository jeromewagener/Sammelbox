package org.sammelbox.android.view.activity;

import org.sammelbox.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        
        // Browse album items and searches
        ImageButton btnOpenBrowseAlbumsAndSearches = (ImageButton) findViewById(R.id.btnOpenBrowseAlbumsAndSearches);
        btnOpenBrowseAlbumsAndSearches.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAlbumSelection = new Intent(WelcomeActivity.this, AlbumSelectionActivity.class);
                startActivity(openAlbumSelection);
            }
        });
        
        // Search for particular items
        ImageButton btnOpenSearchForAlbumItems = (ImageButton) findViewById(R.id.btnOpenSearchForAlbumItems);
        btnOpenSearchForAlbumItems.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSearchForAlbumItems = new Intent(WelcomeActivity.this, SearchActivity.class);
                startActivity(openSearchForAlbumItems);
            }
        });
        
        // Open the synchronization
        ImageButton btnOpenSynchronization = (ImageButton) findViewById(R.id.btnOpenSynchronization);
        btnOpenSynchronization.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSynchronization = new Intent(WelcomeActivity.this, SynchronizationActivity.class);
                startActivity(openSynchronization);
            }
        });
        
        // Open the help
        ImageButton btnOpenHelp = (ImageButton) findViewById(R.id.btnOpenHelp);
        btnOpenHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openHelp = new Intent(WelcomeActivity.this, HelpActivity.class);
                startActivity(openHelp);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }
    
}
