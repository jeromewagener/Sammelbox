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

import java.text.SimpleDateFormat;
import java.util.List;

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
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

public final class DetailedItemCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DetailedItemCreator.class);
	
	private DetailedItemCreator() {
	}
	
	static String getImageAndDetailContainer(AlbumItem albumItem) {
		return getImageAndDetailContainer(albumItem, true);
	}
	
	static String getImageAndDetailContainer(AlbumItem albumItem, boolean hasButtonsAndLinks) {
		StringBuilder htmlDataColumnContent = new StringBuilder();
		StringBuilder htmlPictureColumnContent = new StringBuilder();
		StringBuilder albumItemTableRowHtml = new StringBuilder();
		addImageAndDetailContainer(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, hasButtonsAndLinks);

		return albumItemTableRowHtml.toString();
	}

	static void addImageAndDetailContainer(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, StringBuilder albumItemTableRowHtml) {
		addImageAndDetailContainer(albumItem, htmlDataColumnContent, htmlPictureColumnContent, albumItemTableRowHtml, true);
	}
	
	static void addImageAndDetailContainer(AlbumItem albumItem, StringBuilder htmlDataColumnContent, StringBuilder htmlPictureColumnContent, 
			StringBuilder albumItems, boolean hasButtonsAndLinks) {
		
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
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), BrowserUtils.escapeHtmlString((String) fieldItem.getValue())));
			} else if (fieldItem.getType().equals(FieldType.INTEGER)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Integer) fieldItem.getValue()).toString()));
			} else if (fieldItem.getType().equals(FieldType.DECIMAL)) {
				htmlDataColumnContent.append(getFieldNameAndValueLine(fieldItem.getName(), ((Double) fieldItem.getValue()).toString()));
			} else if (fieldItem.getType().equals(FieldType.STAR_RATING)) {
				htmlDataColumnContent.append(getFieldNameAndStars(fieldItem.getName(), (StarRating) fieldItem.getValue()));
			} else if (fieldItem.getType().equals(FieldType.URL)) {
				htmlDataColumnContent.append(getUrlNameAndLocationLine(fieldItem.getName(), ((String) fieldItem.getValue())));
			}
		}
				
		try {
			List<AlbumItemPicture> pictures = albumItem.getPictures();
			if (DatabaseOperations.isPictureAlbum(ApplicationUI.getSelectedAlbum()) || !pictures.isEmpty()) {
				
				htmlPictureColumnContent.append(			
					"<div class=\"mainPictureWrapper\">" + getMainPictureHtml(id, pictures, hasButtonsAndLinks) + "</div>" +
		            "<div>" + getAlternativePicturesHtml(id, pictures, hasButtonsAndLinks) + "</div>");
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An issue regarding the album item picture occured", ex);
		}

		if (hasButtonsAndLinks) {
			htmlDataColumnContent.append(getUpdateRemoveButtonsForm(id));
		}
		
		albumItems.append(		
			"<div id=\"albumId" + id + "\" class=\"albumItem\">" +
			  "<div class=\"albumItemPictures\">" +
					htmlPictureColumnContent +
			  "</div>" +
			  "<div class=\"details\">" +
			  		htmlDataColumnContent +
			  "</div>" +
			"</div>");
	}
	
	private static String getUpdateRemoveButtonsForm(long id) {
		return "<form class=\"buttonWrapper\">" +
		         "<input type=\"button\" " +
			       "onclick=\"parent.location.href=&quot;show:///updateComposite=" + id + "&quot;\" " +
			       "value=\"" + Translator.get(DictKeys.BROWSER_UPDATE) + "\">" +
		         "<input type=\"button\" " +
			       "onclick=\"parent.location.href=&quot;show:///deleteComposite=" + id + "&quot;\" " +
		           "value=\"" + Translator.get(DictKeys.BROWSER_DELETE) + "\">" +
		       "</form>";
	}
	
	private static String getFieldNameAndValueLine(String fieldName, String value) {
		return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span> : " + value + "<br>"; 
	}
	
	private static String getUrlNameAndLocationLine(String fieldName, String value) {
		return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span> : <a href=\"show:///url=" + value + "\">" + value + "</a><br>";
	}
	
	private static String getFieldNameAndStars(String fieldName, StarRating rating) {
		if (rating.equals(StarRating.ONE_STAR)) {
			return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getOneStarPNG() + "\"><br>";
		} else if (rating.equals(StarRating.TWO_STARS)) {
			return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getTwoStarsPNG() + "\"><br>";
		} else if (rating.equals(StarRating.THREE_STARS)) {
			return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getThreeStarsPNG() + "\"><br>";
		} else if (rating.equals(StarRating.FOUR_STARS)) {
			return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getFourStarsPNG() + "\"><br>";
		} else if (rating.equals(StarRating.FIVE_STARS)) {
			return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getFiveStarsPNG() + "\"><br>";
		}
		
		return "<span class=\"field\"> " + BrowserUtils.escapeHtmlString(fieldName) + "</span><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getZeroStarsPNG() + "\"><br>";
	}
	
	private static String getAlternativePicturesHtml(long id, List<AlbumItemPicture> pictures, boolean hasButtonsAndLinks) {
		StringBuilder htmlBuilder = new StringBuilder();
		
		if (pictures.size() > 1) {
			for(AlbumItemPicture picture : pictures) {
				String escapedJavascriptFilePath = BrowserUtils.escapeBackslashesInFilePath(picture.getThumbnailPicturePath());
				htmlBuilder.append("<div class=\"thumbnailWrapper\">");
				
				htmlBuilder.append("<a ");
				if (hasButtonsAndLinks) {
					htmlBuilder.append(" onClick=\"showBigPicture(&quot;imageId" + id + "&quot;)\" ");
				}
				htmlBuilder.append(" onMouseOver=\"change(&quot;imageId" + id + "&quot;, &quot;" + escapedJavascriptFilePath + "&quot;)\">");
								
				htmlBuilder.append("<img alt=\"\" onMouseOver=\"this.style.cursor=&quot;pointer&quot;\" "
				        + "class=\"thumbnailPicture\" src=\"" + escapedJavascriptFilePath + "\">");
				      
				if (hasButtonsAndLinks) {
					htmlBuilder.append("</a>");
				}
						
				htmlBuilder.append("</div>");
			}
		}
		
		return htmlBuilder.toString();
	}

	private static String getMainPictureHtml(long id, List<AlbumItemPicture> albumItemPictures, boolean hasButtonsAndLinks) {
		// Initialize with placeholder
		String mainPictureHtml = "<img id=\"imageId" + id + "\" " +
								 " alt=\"\"" +
				 				 " class=\"mainPicture\"" +
				 				 " src=\"" + FileSystemLocations.getPlaceholderPNG() + "\">";
		
		// Use primary image if available
		if (!albumItemPictures.isEmpty()) {
			mainPictureHtml = "<img id=\"imageId" + id + "\" " +
					          " alt=\"\"" +
							  " class=\"mainPicture\"" +
							  " src=\"" + albumItemPictures.get(0).getThumbnailPicturePath() + "\"";
			
			if (hasButtonsAndLinks) {
				mainPictureHtml += " onMouseOver=\"changeCursorToHand(&quot;imageId" + id + "&quot;)\""
						         + " onClick=\"showBigPicture(&quot;imageId" + id + "&quot;)\">";
			} else {
				mainPictureHtml += ">";
			}
		}
		
		return mainPictureHtml;
	}
}
