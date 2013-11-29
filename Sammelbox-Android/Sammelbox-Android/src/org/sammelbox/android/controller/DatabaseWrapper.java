package org.sammelbox.android.controller;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseWrapper {
	
	public static SQLiteDatabase getSQLiteDatabase(Context context) {
		DatabaseOpenHelper db = new DatabaseOpenHelper(context);
		return db.getReadableDatabase();
	}
    
	public static Cursor executeRawSQLQuery(SQLiteDatabase database, String rawQuery) {
		return database.rawQuery(rawQuery, null);
	}
	
	public static Cursor executeRawSQLQuery(SQLiteDatabase database, String rawQuery, String argument) {
		return database.rawQuery(rawQuery, new String[] { argument} );
	}
	
	public static Cursor executeRawSQLQuery(SQLiteDatabase database, String rawQuery, String[] arguments) {
		return database.rawQuery(rawQuery, arguments);
	}
}
