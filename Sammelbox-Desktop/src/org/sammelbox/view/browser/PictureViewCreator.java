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

import java.util.List;

import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.composites.StatusBarComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PictureViewCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewCreator.class);
	
	private PictureViewCreator() {
	}
	
	static void showPicture(long albumItemId) {
		StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(Translator.get(DictKeys.STATUSBAR_CLICK_TO_RETURN));

		StringBuilder picturePage = new StringBuilder();
		try {
			List<AlbumItemPicture> pictures = DatabaseOperations.getAlbumItemPictures(ApplicationUI.getSelectedAlbum(), albumItemId);
			
			String originalPathToPicture = "";
			if (pictures.size() >= 1) {
				originalPathToPicture = pictures.get(0).getOriginalPicturePath();
			}
	
			StringBuilder smallPictures = new StringBuilder();

			smallPictures.append("<div style=\" float:right; width:120; padding-top:20px\">" +
					             "  <input type='button' " +
					             "         onclick=\"parent.location.href='show:///lastPage'\" " +
					             "         value='" + Translator.toBeTranslated("Go Back") + "'>" +
					             "</div>");
			
			if (pictures.size() >= 2) {
				int counter = 1;
	
				for (AlbumItemPicture picture : pictures) {
					String escapedPicturePath = BrowserUtils.escapeBackslashesInFilePath(picture.getOriginalPicturePath());
					smallPictures.append(
							"<a style=\"\" onMouseOver='change(\"bigimg\", \"" + escapedPicturePath + "\");maximizeImageSize(\"bigimg\", 110);'>" + 
									"  <img " +
									"       onMouseOver='this.style.cursor=\"pointer\"' " +
									"       id=\"smallimage" + counter + "\" " +
									"		style=\" max-height:50px; margin-top:10px;\"" +
									"       src=\"" + picture.getThumbnailPicturePath() + "\">" +
									"</a>");

					counter++;
				}
			}
	
			picturePage.append(
					"<html>" +
	                "  <head>" +
					"      <meta " + UIConstants.META_PARAMS + ">" + 
					"      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
			        "  </head>" +
			        "           <div style=\" width:100%; float:right; overflow-y:scroll; overflow:auto; height:70px;\">" + 
			        				smallPictures.toString() + 
			        			"</div>" +		
			        "			<hr>" +
			        "		   <div id=\"bigImgDiv\" style=\" width: 200px; display: block; margin-left: auto; margin-right: auto;\">" +
			        "          		<img " +
			        "               	id=\"bigimg\" src=\"" + originalPathToPicture + "\" " +
			        "               	onMouseOver=\"changeCursorToHand('bigimg')\" " +
			        "               	onClick=\"parent.location.href='show:///lastPage'\"" +
			        "                   onLoad=\"maximizeImageSize('bigimg', 110)\">" +
			        "          </div>" +
			        "  </body>" +
			        "</html>");
	
			ApplicationUI.createOrRetrieveAlbumItemBrowser().setText(picturePage.toString());
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
					ApplicationUI.getSelectedAlbum(), ex);
		}
	}
}
