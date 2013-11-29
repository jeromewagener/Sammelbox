package org.sammelbox.android;

import java.util.Map;

public class GlobalState {
	private static Map<String,String> albumNameToTableName = null;
	private static String selectedAlbum = null;
	
	public static String getSelectedAlbum() {
		return selectedAlbum;
	}

	public static void setSelectedAlbum(String selectedAlbum) {
		GlobalState.selectedAlbum = selectedAlbum;
	}

	public static Map<String,String> getAlbumNameToTableName() {
		return albumNameToTableName;
	}

	public static void setAlbumNameToTableName(Map<String,String> albumNameToTableName) {
		GlobalState.albumNameToTableName = albumNameToTableName;
	}
}
