package org.sammelbox.view.browser;

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.view.UIConstants;

public final class GalleryItemCreator {
	private GalleryItemCreator() {
	}
	
	static String getImageContainer(AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		addImageContainer(albumItem, htmlBuilder);

		return htmlBuilder.toString();
	}
	
	private static String getThumbnailForFirstPicture(AlbumItem albumItem) {
		return ((albumItem.getFirstPicture() != null) ? albumItem.getFirstPicture().getThumbnailPicturePath() : FileSystemLocations.getPlaceholderPNG());
	}
	
	static void addImageContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
		htmlBuilder.append(
				"<div id=\"imageId" + albumItem.getItemId() + "\" " +
				     "class=\"pictureContainer\" " +
				     "onMouseOver=\"parent.location.href=&quot;" + UIConstants.SHOW_DETAILS  + albumItem.getItemId() + "&quot;\" " +
				     "onClick=\"parent.location.href=&quot;" + UIConstants.SHOW_UPDATE_ENTRY_COMPOSITE  + albumItem.getItemId() + "&quot;\">" +
				     
				     "<div class=\"pictureWrapper\">" + 
				     	"<img src=\"" + getThumbnailForFirstPicture(albumItem) + "\">" +
				     "</div>" +
                "</div>");
	}
}
