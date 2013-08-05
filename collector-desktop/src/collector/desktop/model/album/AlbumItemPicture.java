/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ** ----------------------------------------------------------------- */

package collector.desktop.model.album;

import java.io.File;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;

public class AlbumItemPicture {
	public static final Long PICTURE_ID_UNDEFINED = Long.MAX_VALUE;
	public static final String ALBUM_ITEM_PICTURE = "ALBUM_ITEM_PICTURE";

	private long pictureID;
	/** Is always a uuid.extension e.g. 8bdb7e3f-c66b-4df6-9640-95642c4d823b_1348734436938.png */
	private String thumbnailPictureName;
	/** Is always a uuid.extension e.g. 3294367d-7901-4bb2-8757-ad13ff3616f7_1348734351845.jpg */
	private String originalPictureName;		
	private String albumName;
	private long albumItemID;

	/** Creates an initially unassigned picture object for an album */
	public AlbumItemPicture(String thumbnailPictureName, String originalPictureName, String albumName, long albumItemID) {
		this.thumbnailPictureName = thumbnailPictureName;
		this.originalPictureName = originalPictureName;
		this.albumName = albumName;
		this.albumItemID = albumItemID;
	}

	/** Creates an initially unassigned picture object for an album */
	public AlbumItemPicture(long pictureID, String thumbnailPictureName, String originalPictureName, String albumName, long albumItemID) {
		this.pictureID = pictureID;
		this.thumbnailPictureName = thumbnailPictureName;
		this.originalPictureName = originalPictureName;
		this.albumName = albumName;
		this.albumItemID = albumItemID;
	}

	/** Returns the picture ID which cannot be set manually. It stays at -1 until it is persisted. 
	 * Only after it is reloaded from the database, the value will be different to -1 
	 * @return the database ID, or -1 if not yet stored/reloaded from the database */
	public long getPictureID() {
		return pictureID;
	}	

	public void setPictureID(long pictureID) {
		this.pictureID = pictureID;
	}	

	public void setThumbnailPictureName(String thumbnailPictureName) {
		this.thumbnailPictureName = thumbnailPictureName;
	}

	public String getOriginalPictureName() {
		return originalPictureName;
	}

	public void setOriginalPictureName(String originalPictureName) {
		this.originalPictureName = originalPictureName;
	}		

	public String getThumbnailPictureName() {
		return thumbnailPictureName;
	}

	public String getAlbumName() {
		return albumName;
	}

	/** If not set, the album name is automatically initialized when the album item is persisted */
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public long getAlbumItemID() {
		return albumItemID;
	}

	/** If not set, the album item id is automatically initialized when the album item is persisted */
	public void setAlbumItemID(long albumItemID) {
		this.albumItemID = albumItemID;
	}

	public String getThumbnailPicturePath() {
		return FileSystemAccessWrapper.COLLECTOR_HOME_THUMBNAILS_FOLDER + 
				File.separatorChar + getThumbnailPictureName();
	}

	public String getOriginalPicturePath() {
		return FileSystemAccessWrapper.COLLECTOR_HOME_ALBUM_PICTURES + 
				File.separatorChar + albumName + File.separatorChar + getOriginalPictureName();
	}
}
