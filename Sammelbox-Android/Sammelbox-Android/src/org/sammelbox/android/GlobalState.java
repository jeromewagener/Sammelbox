package org.sammelbox.android;

import java.util.Map;

import org.sammelbox.android.controller.DatabaseQueryOperation;
import org.sammelbox.android.model.SimplifiedAlbumItemResultSet;

import android.content.Context;

public class GlobalState {
	private static String selectedAlbum = null;
	private static Map<String,String> albumNameToTableName = null;
	/** The last simplified album item result set that was retrieved */
	private static SimplifiedAlbumItemResultSet simplifiedAlbumItemResultSet = null;
	
	public static String getSelectedAlbum() {
		return selectedAlbum;
	}

	public static void setSelectedAlbum(String selectedAlbum) {
		GlobalState.selectedAlbum = selectedAlbum;
	}

	public static Map<String,String> getAlbumNameToTableName(Context context) {
		if (albumNameToTableName == null) {
			albumNameToTableName = DatabaseQueryOperation.getAlbumNamesToAlbumTablesMapping(context);
		}
		
		return albumNameToTableName;
	}

	public static void setAlbumNameToTableName(Map<String,String> albumNameToTableName) {
		GlobalState.albumNameToTableName = albumNameToTableName;
	}

	public static SimplifiedAlbumItemResultSet getSimplifiedAlbumItemResultSet() {
		return simplifiedAlbumItemResultSet;
	}

	public static void setSimplifiedAlbumItemResultSet(
			SimplifiedAlbumItemResultSet simplifiedAlbumItemResultSet) {
		GlobalState.simplifiedAlbumItemResultSet = simplifiedAlbumItemResultSet;
	}
}
