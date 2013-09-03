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

package org.sammelbox.view.browser;

import java.text.SimpleDateFormat;
import java.util.List;

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.settings.SettingsManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ItemCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemCreator.class);
	
	private ItemCreator() {
	}
	
	static String getAlbumItemTableRowHtml(AlbumItem albumItem) {
		return getAlbumItemTableRowHtml(albumItem, true);
	}
	
	static String getAlbumItemTableRowHtml(AlbumItem albumItem, boolean showUpdateAndRemoveButtons) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, showUpdateAndRemoveButtons);

		return albumItemTableRowHtml.toString();
	}

	static String getAlbumItemDivContainerHtml(AlbumItem albumItem) {
		StringBuilder htmlBuilder = new StringBuilder();
		addAlbumItemDivContainer(albumItem, htmlBuilder);

		return htmlBuilder.toString();
	}

	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		addAlbumItemTableRow(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
	}
	
	private static String getThumbnailForFirstPicture(AlbumItem albumItem) {
		return ((albumItem.getFirstPicture() != null) ? albumItem.getFirstPicture().getThumbnailPicturePath() : FileSystemLocations.getPlaceholderPNG());
	}
	
	static void addAlbumItemDivContainer(AlbumItem albumItem, StringBuilder htmlBuilder) {
		htmlBuilder.append(
				"<div id=\"imageId" + albumItem.getItemID() + "\" " +
				"    class=\"pictureContainer\" " +
				"    onMouseOver=\"parent.location.href=&quot;show:///details=" + albumItem.getItemID() + "&quot;\" " +
				"    onClick=\"parent.location.href=&quot;show:///detailsComposite=" + albumItem.getItemID() + "&quot;\">" +
                "  <div class=\"innerPictureContainer\">" +
		        "    <img src=\"" + getThumbnailForFirstPicture(albumItem) + "\">" +
                "  </div>" +
                "</div>");
	}
	
	static void addAlbumItemTableRow(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, 
			StringBuilder albumItemTableRowHtml, boolean showUpdateAndRemoveButtons) {
		
		// the id of the current album item
		long id = -1;	
		
		for (ItemField fieldItem : albumItem.getFields()) {			
			if (fieldItem.getType().equals(FieldType.UUID)) {
				// schema or content version UUID --> ignore 
			} else if (fieldItem.getType().equals(FieldType.ID)) {
				if (fieldItem.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					// do not show, but store id
					id = fieldItem.getValue();
				}
			} else if (fieldItem.getType().equals(FieldType.OPTION)) {
				if (fieldItem.getValue() == OptionType.YES) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_YES)));
				} else if (fieldItem.getValue() == OptionType.NO) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_NO)));
				} else if (fieldItem.getValue() == OptionType.UNKNOWN) {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Translator.get(DictKeys.BROWSER_UNKNOWN)));
				}
			} else if (fieldItem.getType().equals(FieldType.DATE)) {
				java.sql.Date sqlDate = fieldItem.getValue();
				if (sqlDate != null) {
					java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
	
					SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), dateFormater.format(utilDate)));
				} else {
					htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ""));
				}
			} else  if (fieldItem.getType().equals(FieldType.TEXT)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), Utilities.escapeHtmlString((String) fieldItem.getValue())));
			} else if (fieldItem.getType().equals(FieldType.INTEGER)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Integer) fieldItem.getValue()).toString()));
			} else if (fieldItem.getType().equals(FieldType.DECIMAL)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Double) fieldItem.getValue()).toString()));
			} else if (fieldItem.getType().equals(FieldType.STAR_RATING)) {
				htmlDataColumnContent.append(getFieldNameAndStars(fieldItem.getName(), (StarRating) fieldItem.getValue()));
			} else if (fieldItem.getType().equals(FieldType.URL)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((String) fieldItem.getValue())));
			}
		}
				
		try {
			List<AlbumItemPicture> pictures = albumItem.getPictures();
			if (DatabaseOperations.isPictureAlbum(ApplicationUI.getSelectedAlbum()) || !pictures.isEmpty()) {
				
				htmlPictureColumnContent.append(
						"<table border=0>" +
						"  <tr>" +
						"    <td align=center width=200 height=200>" +
								getMainPictureHtml(id, pictures) +
						"    </td>" +
						"  </tr>" +
						"  <tr>" +
						"    <td>" +
						"      <div style=\"max-width:200px;\">" +
								getAlternativePicturesHtml(id, pictures) +
						"      </div>" + 
						"    </td>" +
						"  </tr>" + 
						"</table>");
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An issue regarding the album item picture occured", ex);
		}

		if (showUpdateAndRemoveButtons) {
			htmlDataColumnContent.append(getUpdateRemoveButtonsHtml(id));
		}	

		
		// set the picture column with only if there is also a picture
		String widthParameter = "";
		if (htmlPictureColumnContent.length() != 0) {
			widthParameter = "width=200px";
		}
		
		albumItemTableRowHtml.append("<tr id=\"albumId" + id + "\">" +
				                     "  <td " + widthParameter + ">" + htmlPictureColumnContent + "</td>" +
				                     "    <td bgcolor=" + Utilities.getBackgroundColorOfWidgetInHex() + ">" + 
				                     "       <div style=\"margin:20px\">" + htmlDataColumnContent + "</div>" + 
				                     "    </td>" +
				                     "  </tr>" +
				                     "  <tr>" +
				                     "    <td height=\"20\" colspan=\"2\">" +
				                     "  </td>" +
				                     "</tr>");		
	}
	
	private static String getUpdateRemoveButtonsHtml(long id) {
		return "<form style=\"margin-top:10px\">" +
		       "  <input type=\"button\" " +
			   "         onclick=parent.location.href=\"show:///updateComposite=" + id + "\" " +
			   "         value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">" +
		       "  <input type=\"button\" " +
			   "    onclick=parent.location.href=\"show:///deleteComposite=" + id + "\" " +
		       "    value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">" +
		       "</form>";
	}
	
	private static String getFieldNameAndValueLine(String fieldName, String value) {
		return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span> : " + value + "<br>"; 
	}
	
	private static String getFieldNameAndStars(String fieldName, StarRating rating) {
		if (rating.equals(StarRating.ONE_STAR)) {
			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getOneStarPNG() + "><br>";
		} else if (rating.equals(StarRating.TWO_STARS)) {
			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getTwoStarsPNG() + "><br>";
		} else if (rating.equals(StarRating.THREE_STARS)) {
			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getThreeStarsPNG() + "><br>";
		} else if (rating.equals(StarRating.FOUR_STARS)) {
			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getFourStarsPNG() + "><br>";
		} else if (rating.equals(StarRating.FIVE_STARS)) {
			return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getFiveStarsPNG() + "><br>";
		}
		
		return "<span class=\"boldy\"> " + Utilities.escapeHtmlString(fieldName) + "</span><img height=20 src=" + FileSystemLocations.getZeroStarsPNG() + "><br>";
	}
	
	private static String getAlternativePicturesHtml(long id, List<AlbumItemPicture> pictures) {
		StringBuilder htmlBuilder = new StringBuilder();
		
		if (pictures.size() > 1) {
			for(AlbumItemPicture picture : pictures) {
				String escapedJavascriptFilePath = Utilities.escapeBackslashesInFilePath(picture.getThumbnailPicturePath());
				htmlBuilder.append(
					"<div align=center style=\"display:inline; min-width:40px; width:auto; width:40px\">" +
					"  <a onClick=showBigPicture(\"imageId" + id + "\") " +
					"     onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + escapedJavascriptFilePath + "&quot;)\">" +
				    "    <img onMouseOver=this.style.cursor=\"pointer\" style=\"max-width:40px; max-height:40px;\" src=\"" + escapedJavascriptFilePath + "\">" +
				    "  </a>" +
				    "</div>");
			}
		}
		
		return htmlBuilder.toString();
	}

	private static String getMainPictureHtml(long id, List<AlbumItemPicture> albumItemPictures) {
		// Initialize with placeholder
		String mainPictureHtml = "<img id=\"imageId" + id + "\" " +
				 				 "     style=\"max-width:195px; max-height:195px;\"" +
				 				 "     src=\"" + FileSystemLocations.getPlaceholderPNG() + "\">";
		
		// Use primary image if available
		if (!albumItemPictures.isEmpty()) {
			mainPictureHtml = "<img id=\"imageId" + id + "\" " +
							  "     style=\"max-width:195px; " +
							  "     max-height:195px;\" " +
							  "     src=\"" + albumItemPictures.get(0).getThumbnailPicturePath() + "\" " +
							  "     onMouseOver=changeCursorToHand(\"imageId" + id + "\") " +
							  "     onClick=showBigPicture(\"imageId" + id + "\")>";
		}
		
		return mainPictureHtml;
	}
}
