/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
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

package org.sammelbox.view.browser;

import org.eclipse.swt.browser.Browser;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class GalleryViewCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(GalleryViewCreator.class);
	
	private GalleryViewCreator() {
	}
	
	static void showOverviewAlbum(Browser browser) {
		StringBuilder galleryItemHtmlBuilder = new StringBuilder();
		StringBuilder htmlDataColumnContent = new StringBuilder();
		
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			String picturePath = "";
			long id = 0;

			for (ItemField fieldItem : albumItem.getFields()) {				
				if (fieldItem.getType().equals(FieldType.UUID)) {
					// schema or content version UUID --> ignore
				} else if (fieldItem.getType().equals(FieldType.ID) && !fieldItem.getName().equals("typeinfo")) {
					// do not show, but store id
					id = fieldItem.getValue();
				}
			}		

			List<AlbumItemPicture> pictures = null;
			try {
				pictures = DatabaseOperations.getAlbumItemPictures(ApplicationUI.getSelectedAlbum(), id);
			} catch (DatabaseWrapperOperationException ex) {
				LOGGER.error("An error occurred while retrieving the pictures associated with the album item #'" +
					id + "' from the album '" + ApplicationUI.getSelectedAlbum() + "'", ex);
			}
			
			if (pictures == null || pictures.isEmpty()) {
				picturePath = FileSystemLocations.getPlaceholderPNG();
			} else {
				picturePath = pictures.get(0).getThumbnailPicturePath();
			}
			
			htmlDataColumnContent.delete(0, htmlDataColumnContent.length());
			DetailedItemCreator.createDetailContainer(albumItem, htmlDataColumnContent);
			
			galleryItemHtmlBuilder.append("<div id=\"imageId" + id + "\" " +
					                         " class=\"pictureContainer");
			
			if (ItemFieldFilter.getValidItemFields(albumItem.getFields()).isEmpty()) {
				galleryItemHtmlBuilder.append("\" ");
			}
			else {
				galleryItemHtmlBuilder.append(" tooltip\" ");
			}
					                         
			galleryItemHtmlBuilder.append(" onMouseOver=\"parent.location.href=&quot;" + UIConstants.SHOW_DETAILS + id + "&quot;\" " +
					                         " onClick=\"parent.location.href=&quot;" + UIConstants.SHOW_UPDATE_ENTRY_COMPOSITE + id + "&quot;\">");
			
			galleryItemHtmlBuilder.append("<img class=\"pictureWrapper\" alt=\"\" src=\"" + picturePath + "\">" );
			
			galleryItemHtmlBuilder.append("<label> " + htmlDataColumnContent.toString() + " </label>");
			galleryItemHtmlBuilder.append("</div>");
		}

		// Build header using album name. Include view name if appropriated
		String collectionHeader = GuiController.getGuiState().getSelectedAlbum();
		if (GuiController.getGuiState().isViewSelected()) {
			collectionHeader += " - " + GuiController.getGuiState().getSelectedSavedSearch();
		}
		
		String finalPageAsHtml = "<!DOCTYPE html>\n" +
		   "<html>" +
		     "<head>" +
		       "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">" +
		       "<link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
		       "<script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
		       "<title>www.sammelbox.org</title>" +
		     "</head>" +
		     "<body style=\"background-color:#ffffff;font-family:" +  BrowserUtils.getDefaultSystemFont() + "\">" +
		       "<h2>" + collectionHeader + "</h2>" +
		       "<div id=\"albumItems\">" + galleryItemHtmlBuilder.toString() + "</div>" +
		     "</body>" +
		   "</html>";
		
		browser.setText(finalPageAsHtml);
		BrowserUtils.setLastPageAsHtml(finalPageAsHtml);
	}
}
