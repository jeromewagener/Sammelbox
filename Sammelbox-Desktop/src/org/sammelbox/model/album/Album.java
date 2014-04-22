package org.sammelbox.model.album;

public class Album implements Comparable<Album> {
	private String albumName;
	private String sortByField;
	
	public String getAlbumName() {
		return albumName;
	}
	
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getSortByField() {
		return sortByField;
	}

	public void setSortByField(String sortByField) {
		this.sortByField = sortByField;
	}	
	
	@Override
	public int compareTo(Album otherAlbum) {
		return this.albumName.compareTo(otherAlbum.getAlbumName());
	}
	
	@Override
	public boolean equals(Object otherAlbum) {
		return this.compareTo((Album) otherAlbum) == 0;
	}
}
