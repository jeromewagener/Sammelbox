package org.sammelbox.view.browser;

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.model.album.AlbumItem;

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
				"<div id=\"imageId" + albumItem.getItemID() + "\" " +
				     "class=\"pictureContainer\" " +
				     "onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" " +
				     "onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">" +
                   "<div class=\"innerPictureContainer\">" +
		              "<img src=\"" + getThumbnailForFirstPicture(albumItem) + "\">" +
                   "</div>" +
                "</div>");
	}
}
