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

import org.sammelbox.controller.filesystem.FileSystemConstants;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.AlbumManager;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;

public class WelcomePageCreator {
	private static final int NUMBER_OF_FAVORITES_SHOWN = 5;

	static void loadWelcomePage() {
		ApplicationUI.getAlbumItemBrowser().setText(
				"<!DOCTYPE HTML>" +
				"<html>" +
				"  <head>" +
				"    <title>" + Translator.get(DictKeys.TITLE_MAIN_WINDOW) + "</title>" +
				"    <meta " + UIConstants.META_PARAMS + ">" + 
				"    <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
				"    <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
				"  </head>" +
				"  <body>" +
				"    <font face=\"" + Utilities.getDefaultSystemFont() + "\">" +
				"	 <br>" +
				"	 <table>" +
				"      <tr>" +
				"	     <td align=\"center\">" +
				"          <img width=\"450\" src=\" " + FileSystemConstants.LOGO + " \">" +
				"        </td>" +
				"        <td width=\"430px\">" +
		        "          <div style=\"margin-left:30px; padding:10px; background-color:#" + Utilities.getBackgroundColorOfWidgetInHex() + "\">" +
						     generateAlbumInformation() +
						     generateFavorites() +
			    "          </div>" +
				"        </td>" +
				"      </tr>" +
				"	 </table>" +
				"    </font>" +
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
			for (String album : AlbumManager.getAlbums()) {
				albumInformationBuilder.append("<li>" +
											   "  Album <b>" + album + "</b><br>" +
											   "  <font size=-1>" +
											   "  <i>" + 
													Translator.get(DictKeys.BROWSER_NUMBER_OF_ITEMS_AND_LAST_UPDATED, 
															WelcomePageManager.getNumberOfItemsInAlbum(album),
															WelcomePageManager.getLastModifiedDate(album)) +
											   "  </i>" +
											   "  </font>" +
											   "</li>");
			}
		}

		return albumInformationBuilder.append("</ul>").toString();
	}

	private static String generateFavorites() {
		StringBuilder favoriteBuilder = new StringBuilder("<h4>" + Translator.get(DictKeys.BROWSER_FAVORITE_ALBUMS_AND_VIEWS) + "</h4><ol>");

		int favCounter = 0;
		if (WelcomePageManager.getAlbumAndViewsSortedByClicks().keySet().isEmpty()) {
			favoriteBuilder.append("<li>" + 
									  Translator.get(DictKeys.BROWSER_NO_INFORMATION_AVAILABLE) + 
								   "</li>");
		} else {
			for (String albumOrViewName : WelcomePageManager.getAlbumAndViewsSortedByClicks().keySet()) {
				favoriteBuilder.append("<li>" +
										  albumOrViewName + 
										  "<font size=-1>" +
										    "<i>" + 
										      Translator.get(DictKeys.BROWSER_CLICKS_FOR_ALBUM, 
										  			WelcomePageManager.getAlbumAndViewsSortedByClicks().get(albumOrViewName)) + 
										    "</i>" +
										  "</font>" +
										"</li>");
	
				if (++favCounter == NUMBER_OF_FAVORITES_SHOWN) {
					break;
				}
			}
		}

		return favoriteBuilder.append("</ol>").toString();	
	}
}
