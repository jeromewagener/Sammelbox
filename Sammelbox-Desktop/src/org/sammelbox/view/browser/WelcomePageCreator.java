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

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.AlbumManager;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.model.album.Album;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;

public final class WelcomePageCreator {
	private WelcomePageCreator() {
	}
	
	static void loadWelcomePage() {		
		ApplicationUI.createOrRetrieveAlbumItemBrowser().setText(
				"<!DOCTYPE HTML>" +
				"<html>" +
				"  <head>" +
				"    <title>" + Translator.get(DictKeys.TITLE_MAIN_WINDOW) + "</title>" +
				"    <meta " + UIConstants.META_PARAMS + ">" + 
				"    <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
				"    <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
				"  </head>" +
				"  <body style=\"width:100%;\">" +
				"    <div style=\"width:100%;\">" +
				"      <div style=\"float:left; margin:25px;\">" +
				"        <img width=\"450px\" src=\" " + FileSystemLocations.getLogoPNG() + " \">" +
				"      </div>" +
		        "      <div style=\"float:left; margin-top:25px; padding:10px; background-color:#;" + BrowserUtils.getBackgroundColorOfWidgetInHex() + "\">" +
					     generateAlbumInformation() +
			    "      </div>" +
			    "    </div>" +
				"  </body>" +
				"</html>");
	}
	
	private static String generateAlbumInformation() {
		StringBuilder albumInformationBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_ALBUM_INFORMATION) + "</h4><ul>");

		if (AlbumManager.getAlbums().isEmpty()) {
			albumInformationBuilder.append("<li>" +
					Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) +
					"</li>");
		} else {
			for (Album album : AlbumManager.getAlbums()) {
				albumInformationBuilder.append("<li>" +
						"  Album <b>" + album.getAlbumName() + "</b><br>" +
						"  <font size=-1>" +
						"  <i>" +
						Translator.get(DictKeys.BROWSER_NUMBER_OF_ITEMS_AND_LAST_UPDATED,
								WelcomePageManager.getNumberOfItemsInAlbum(album.getAlbumName()),
								WelcomePageManager.getLastModifiedDate(album.getAlbumName())) +
						"  </i>" +
						"  </font>" +
						"</li>");
			}
		}

		return albumInformationBuilder.append("</ul>").toString();
	}
}
