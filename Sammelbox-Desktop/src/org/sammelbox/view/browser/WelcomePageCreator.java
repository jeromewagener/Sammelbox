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
import org.sammelbox.controller.managers.BuildInformationManager;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;

public final class WelcomePageCreator {
	private static final int NUMBER_OF_FAVORITES_SHOWN = 5;

	private WelcomePageCreator() {
	}
	
	static void loadWelcomePage() {
		String browserIframe = new String("");		
		if (BrowserUtils.isProjectWebsiteReachable()) {
			browserIframe = 
					" <iframe style=\"border:none; width:100%; height:30px; overflow:hidden;\"" +
					"         scrolling=\"no\"" +
					"         src=\"http://www.sammelbox.org/current.php?current=" + BuildInformationManager.instance().getPublicVersionString() +
					                                                   "&language=" + Translator.getUsedLanguage() + "\"></iframe> ";
		}
		
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
				"	 <div>" + browserIframe + "</div>" +
				"    <div style=\"width:100%;\">" +
				"      <div style=\"float:left; margin:25px;\">" +
				"        <img width=\"450px\" src=\" " + FileSystemLocations.getLogoPNG() + " \">" +
				"      </div>" +
		        "      <div style=\"float:left; margin:25px; padding:10px; background-color:#" + BrowserUtils.getBackgroundColorOfWidgetInHex() + "\">" +
					     generateAlbumInformation() +
					     generateFavorites() +
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
