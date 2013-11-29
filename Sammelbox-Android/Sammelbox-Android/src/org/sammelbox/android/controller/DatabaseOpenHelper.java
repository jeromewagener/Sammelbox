package org.sammelbox.android.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Sammelbox/sammelbox.db";
    private static final int DATABASE_VERSION = 1;
    
    public DatabaseOpenHelper(Context context) {
        super(context, Environment.getExternalStorageDirectory() + "/" + DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
