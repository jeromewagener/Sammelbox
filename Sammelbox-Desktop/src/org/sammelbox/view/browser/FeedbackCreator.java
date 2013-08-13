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

import java.util.LinkedList;

import org.eclipse.swt.browser.Browser;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedbackCreator {
	private final static Logger LOGGER = LoggerFactory.getLogger(FeedbackCreator.class);
	
	/** A list of alterations that have already been performed via the alter album functionality */
	private static LinkedList<String> alterations = new LinkedList<String>();
	
	static void generatAlbumItemUpdatedPage(long albumItemId) {
		AlbumItem updatedAlbumItem;
		try {
			updatedAlbumItem = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), albumItemId);


			if (updatedAlbumItem != null) {
				ApplicationUI.getAlbumItemBrowser().setText(
						generateItemAddedOrUpdatedFeedbackConstruct(
								Translator.get(DictKeys.BROWSER_ITEM_UPDATED),
								ItemCreator.getAlbumItemTableRowHtml(updatedAlbumItem)));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
							ApplicationUI.getSelectedAlbum(), ex);
		}
	}

	static void generateAlbumItemAddedPage(long albumItemId) {
		AlbumItem addedAlbumItem;
		try {
			addedAlbumItem = DatabaseOperations.getAlbumItem(ApplicationUI.getSelectedAlbum(), albumItemId);

			if (addedAlbumItem != null) {
				ApplicationUI.getAlbumItemBrowser().setText(
						generateItemAddedOrUpdatedFeedbackConstruct(
								Translator.get(DictKeys.BROWSER_ITEM_ADDED),
								ItemCreator.getAlbumItemTableRowHtml(addedAlbumItem)));
			}
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while fetching the album item #" + albumItemId + " in the album: " + 
					ApplicationUI.getSelectedAlbum(), ex);
		}
	}
	
	private static String generateItemAddedOrUpdatedFeedbackConstruct(String title, String addedOrUpdatedItemAsHtmlTableRowItem) {
		return 	"<!DOCTYPE HTML>" +
				"  <html>" +
				"    <head>" +
				"      <meta " + UIConstants.META_PARAMS + ">" +
				"      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
				"      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
				"    </head>" +
				"    <body bgcolor=white>" +
				"      <font face=\"" + Utilities.getDefaultSystemFont() + "\">" +
				"        <h3>" + title + "</h3>" +
				"          <table id=\"albumItems\" border=0>" + addedOrUpdatedItemAsHtmlTableRowItem + "</table>" +
				"      </font>" +
				"      <br>" +
				"      <form>" +
				"        <input type=\"button\" " +
				"               onclick=parent.location.href=\"show:///showDetailsViewOfAlbum\" " +
				"               value=\"" + Translator.get(DictKeys.BROWSER_BACK_TO_ALBUM) + "\">" +
				"      </form>" +
				"    </body>" +
				"  </html>";
	}
	
	static void showCreateNewAlbumPage(Browser browser, AlbumItem albumItem) {
		browser.setText(generateAlbumAddedOrUpdatedFeedbackConstruct(
				albumItem,
				Translator.get(DictKeys.BROWSER_CREATING_NEW_ALBUM),
				Translator.get(DictKeys.BROWSER_ALBUM_WILL_HANDLE_FOLLOWING_FORMAT)));
	}
	
	static void showCreateAlterAlbumPage(Browser browser, AlbumItem albumItem) {		
		browser.setText(generateAlbumAddedOrUpdatedFeedbackConstruct(
				albumItem,
				Translator.get(DictKeys.BROWSER_MODIFYING_ALBUM, ApplicationUI.getSelectedAlbum()),
				Translator.get(DictKeys.BROWSER_MODIFY_WARNING)));
	}
	
	private static String generateAlbumAddedOrUpdatedFeedbackConstruct(AlbumItem albumItem, String title, String subTitle) {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta " + UIConstants.META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
			   "      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
			   "    </head>" +
			   "    <body>" +
		       "      <h3>" + title + "</h3>" +
		       "      <h4>" + subTitle + "</h4>" +
		       "      <hr noshade size=\"1\">" +
		       "      <table>" + ItemCreator.getAlbumItemTableRowHtml(albumItem, false) + "</table>" +
		       "      <hr noshade size=\"1\">" +
		       "      <ul>" + getAlterationsAsListItems() + "</ul>" +
		       "    </body>" +
		       "  </html>";
	}
	
	static String getAlterationsAsListItems() {
		StringBuilder listItems = new StringBuilder();
		
		for (String alteration : alterations) {
			listItems.append("<li>" + alteration + "</li>");
		}
		
		return listItems.toString();
	}
	
	static void clearAlterationList() {
		alterations.clear();
	}
	
	static void addModificationToAlterationList(String modification) {
		alterations.addFirst(modification);
	}
	
	static String generateAlbumDeletedPage(String deletedAlbum) {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta " + UIConstants.META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
			   "      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
			   "    </head>" +
			   "    <body>" +
		       "      <h3>" + Translator.get(DictKeys.BROWSER_ALBUM_DELETED_HEADER, deletedAlbum) + "</h3>" +
		       "      <div>" + Translator.get(DictKeys.BROWSER_ALBUM_DELETED) + "</div>" +
		       "    </body>" +
		       "  </html>";
	}
	
	static String generateAlbumsRestoredPage() {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta " + UIConstants.META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
			   "      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
			   "    </head>" +
			   "    <body>" +
		       "      <h3>" + Translator.get(DictKeys.BROWSER_ALBUMS_RESTORED_HEADER) + "</h3>" +
		       "      <div>" + Translator.get(DictKeys.BROWSER_ALBUM_DELETED) + "</div>" +
		       "    </body>" +
		       "  </html>";
	}
	
	static String generateSynchronizationPage() {
		return "<!DOCTYPE HTML>" +
			   "  <html>" +
			   "    <head>" +
			   "      <meta " + UIConstants.META_PARAMS + ">" + 
			   "      <link rel=stylesheet href=\"" + UIConstants.STYLE_CSS + "\" />" +
			   "      <script src=\"" + UIConstants.EFFECTS_JS + "\"></script>" +
			   "    </head>" +
			   "    <body>" +
		       "      <h3>" + Translator.get(DictKeys.BROWSER_SYNCRONIZATION_HEADER) + "</h3>" +
		       "      <div>" + Translator.get(DictKeys.BROWSER_SYNCRONIZATION) + "</div>" +
		       "    </body>" +
		       "  </html>";
	}
}
