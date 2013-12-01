package org.sammelbox.android.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sammelbox.R;
import org.sammelbox.android.GlobalState;
import org.sammelbox.android.model.FieldType;
import org.sammelbox.android.model.SimplifiedAlbumItemResultSet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class DatabaseQueryOperation {
	public static Map<String, String> getAlbumNamesToAlbumTablesMapping(Context context) {
		Cursor cursor = DatabaseWrapper.executeRawSQLQuery(
				DatabaseWrapper.getSQLiteDatabase(context), "select * from album_master_table");
		
		final Map<String,String> albumNameToTableName = new HashMap<String, String>();
		
		if (cursor.moveToFirst()) {
			while (cursor.isAfterLast() != true) {
				albumNameToTableName.put(cursor.getString(cursor.getColumnIndex("album_name")), 
						cursor.getString(cursor.getColumnIndex("album_table_name")));
				
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		return albumNameToTableName;
	}
	
	public static SimplifiedAlbumItemResultSet getAllAlbumItemsFromAlbum(Context context) {
		String selectedTableName = GlobalState.getAlbumNameToTableName(context).get(GlobalState.getSelectedAlbum());
		Cursor cursor = DatabaseWrapper.executeRawSQLQuery(
				DatabaseWrapper.getSQLiteDatabase(context), "select * from " + selectedTableName);
		
		return getAlbumItems(context, cursor);
	}
	
	public static SimplifiedAlbumItemResultSet getAlbumItems(Context context, Cursor cursor) {
		SimplifiedAlbumItemResultSet simplifiedAlbumItemResultSet = new SimplifiedAlbumItemResultSet();
		
		String selectedTableName = GlobalState.getAlbumNameToTableName(context).get(GlobalState.getSelectedAlbum());
		Map<String, FieldType> fieldNameToTypeMapping = 
				retrieveFieldnameToFieldTypeMapping(DatabaseWrapper.getSQLiteDatabase(context), context, selectedTableName);
		
		if (cursor.moveToFirst()) {
			while (cursor.isAfterLast() != true) {
				Drawable placeHolderImage = context.getResources().getDrawable(R.drawable.placeholder);
				StringBuilder data = new StringBuilder();
				
				for (String fieldName : fieldNameToTypeMapping.keySet()) {
					data.append(fieldName + ": " + readToStringByFieldnameAndType(
							cursor, fieldNameToTypeMapping, fieldName, selectedTableName) + "\n");
				}
				
				Drawable primaryImage = retrievePrimaryImage(
						context, selectedTableName, String.valueOf(cursor.getLong(cursor.getColumnIndex("id"))));
				if (primaryImage == null) {
					primaryImage = placeHolderImage;
				}
				
				simplifiedAlbumItemResultSet.addSimplifiedAlbumItem(primaryImage, data.toString());
				cursor.moveToNext();
			}
		}
		cursor.close();
		
		return simplifiedAlbumItemResultSet;
	}
	
	private static Drawable retrievePrimaryImage(Context context, String albumTableName, String albumItemId) {
		Cursor cursor = DatabaseWrapper.executeRawSQLQuery(
				DatabaseWrapper.getSQLiteDatabase(context),
				"select * from " + albumTableName + "_pictures where album_item_foreign_key = ?", albumItemId);
		
		if (cursor.moveToFirst()) {
			while (cursor.isAfterLast() != true) {				
				String thumbnailName = cursor.getString(cursor.getColumnIndex("thumbnail_picture_filename")); 
				Drawable drawable = Drawable.createFromPath(Environment.getExternalStorageDirectory() + "/Sammelbox/thumbnails/" + thumbnailName);
				cursor.close();
				
				return drawable;
			}
		}
		
		return null;
	}
	
	public static Map<String, FieldType> retrieveFieldnameToFieldTypeMapping(SQLiteDatabase database, Context context, String albumTableName) {
		ArrayList<String> fieldNames = new ArrayList<String>();
		ArrayList<FieldType> fieldTypes = new ArrayList<FieldType>();
		
		Cursor columnCursor = database.rawQuery("PRAGMA table_info(" + albumTableName + "_typeinfo)", null);
		if (columnCursor.moveToFirst()) {
		    do {
		    	String fieldName = columnCursor.getString(1);
		    	if (!DatabaseQueryHelper.isSpecialField(fieldName)) {
		    		fieldNames.add(fieldName);
		    	}
		    } while (columnCursor.moveToNext());
		}
		
		Cursor cursor = DatabaseWrapper.executeRawSQLQuery(
				DatabaseWrapper.getSQLiteDatabase(context),
				"select * from " + albumTableName + "_typeinfo");
				
		if (cursor.moveToFirst()) {
			for (String fieldName : fieldNames) {
				if (!DatabaseQueryHelper.isSpecialField(fieldName)) {
					fieldTypes.add(FieldType.valueOf(cursor.getString(cursor.getColumnIndex(fieldName))));	
				}
			}		
		}
		cursor.close();
		
		Map<String, FieldType> fieldNameToTypeMapping = new LinkedHashMap<String, FieldType>();
		for (int i=0; i<fieldNames.size(); i++) {
			fieldNameToTypeMapping.put(fieldNames.get(i), fieldTypes.get(i));
		}
		
		return fieldNameToTypeMapping;
	}
	
	public static String readToStringByFieldnameAndType(Cursor cursor, Map<String, FieldType> fieldNamesToTypes, String fieldName, String albumTableName) {		
		if (fieldNamesToTypes.get(fieldName).equals(FieldType.TEXT)) {
			return cursor.getString(cursor.getColumnIndex(fieldName));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.INTEGER)) {
			return String.valueOf(cursor.getInt(cursor.getColumnIndex(fieldName)));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.DECIMAL)) {
			return String.valueOf(cursor.getDouble(cursor.getColumnIndex(fieldName)));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.DATE)) {
			return String.valueOf(cursor.getLong(cursor.getColumnIndex(fieldName)));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.OPTION)) {
			return cursor.getString(cursor.getColumnIndex(fieldName));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.STAR_RATING)) {
			return String.valueOf(cursor.getInt(cursor.getColumnIndex(fieldName)));
		} else if (fieldNamesToTypes.get(fieldName).equals(FieldType.URL)) {
			return cursor.getString(cursor.getColumnIndex(fieldName));
		}
		
		return null;
	}
}
