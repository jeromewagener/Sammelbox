/** -----------------------------------------------------------------
 *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
 *    Copyright (C) 2014 Jerome Wagener, Paul Bicheler & Olivier Wagener
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

package org.sammelbox.view.browser.spreadsheet;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.browser.Browser;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemStore;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.browser.BrowserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpreadsheetViewCreator {	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetViewCreator.class);

	private SpreadsheetViewCreator() {
	}
	
	/** Inverts map. Keys become values.
	 * Values must be unique for this function to work. */
	private static <V, K> Map<V, K> invert(Map<K, V> map) {
	    Map<V, K> inv = new HashMap<V, K>();

	    for (Map.Entry<K, V> entry : map.entrySet()) {
	        inv.put(entry.getValue(), entry.getKey());
	    }
	    
	    return inv;
	}
	
	public static void showSpreadsheetAlbum(Browser browser) {
		// Exit if no album is selected
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		
		Map<Integer, MetaItemField> columnIndexToMetaItemMap = null;
		Map<MetaItemField, Integer> metaItemToColumnIndexMap = null;
		
		try {
			columnIndexToMetaItemMap = DatabaseOperations.getAlbumItemMetaMap(GuiController.getGuiState().getSelectedAlbum());
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("An error occured while retieving the albumItemMetaMap. ", e);
		}
		
		metaItemToColumnIndexMap = invert(columnIndexToMetaItemMap);
		
		// Builders for efficient html creation
		StringBuilder htmlSpreadsheet = new StringBuilder();
		StringBuilder htmlSpreadsheetHeader = new StringBuilder();
		StringBuilder htmlSpreadsheetData = new StringBuilder();
		StringBuilder htmlSpreadsheetRow = new StringBuilder();
		StringBuilder htmlSpreadsheetFooter = new StringBuilder();
		StringBuilder javaScriptArray = new StringBuilder();

		// Create the header of the spreadsheet
		if (AlbumItemStore.getAlbumItems().size() > 0) {
			SpreadsheetItemCreator.createSpreadsheetHeader(
					htmlSpreadsheetHeader, AlbumItemStore.getAlbumItems().get(0), metaItemToColumnIndexMap);
		}
		
		// Add all available album items
		boolean hasEvenCountingInList = false;
		long idForUncreatedItem = -1;
		
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			SpreadsheetItemCreator.createNextDataRow(
					albumItem, htmlSpreadsheetData, htmlSpreadsheetRow, hasEvenCountingInList, metaItemToColumnIndexMap);
			hasEvenCountingInList = !hasEvenCountingInList;
		}

		// Create the footer of the spreadsheet
		if(AlbumItemStore.getAlbumItems().size() > 0) {
			idForUncreatedItem = SpreadsheetItemCreator.createSpreadsheetFooter(
					htmlSpreadsheetFooter, AlbumItemStore.getAlbumItems().get(0), metaItemToColumnIndexMap);
		}
		
		// Create the javascript array that is used to access every line in the table
		javaScriptArray.append("<script>");
		javaScriptArray.append("var tableRowId=[");
		
		// Lists all the id's and puts them in an array for javascript usages.
		for (AlbumItem albumItem : AlbumItemStore.getAlbumItems()) {
			javaScriptArray.append(albumItem.getItemID() + ",");
		}
		
		javaScriptArray.append(idForUncreatedItem + "];");
		javaScriptArray.append("</script>");
		
		// If no album items have been found
		if (htmlSpreadsheetData.length() == 0) {
			htmlSpreadsheet.append(
	          "<tr><td><div>" + 
	            "<h3>" + 
	              Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND, GuiController.getGuiState().getSelectedAlbum()) + 
	            "</h3>" + 
	            "<p>" + Translator.get(DictKeys.BROWSER_NO_ITEMS_FOUND_EXPLANATION) + "</p>" +
	          "</div></td></tr>"); 
		} else {
			htmlSpreadsheet.append("<div class=\"tableWrapper\">");
			htmlSpreadsheet.append("<table id=\"spreadsheetTable\">");
			htmlSpreadsheet.append(htmlSpreadsheetHeader);
			htmlSpreadsheet.append(htmlSpreadsheetData);
			htmlSpreadsheet.append(htmlSpreadsheetFooter);
			htmlSpreadsheet.append("</table>");
			htmlSpreadsheet.append("</div>");
			htmlSpreadsheet.append("</br>");
			htmlSpreadsheet.append("<button type=\"button\">" + Translator.toBeTranslated("Änderungen Sichern") + "</button> ");
			htmlSpreadsheet.append("</br></br>");
			htmlSpreadsheet.append("<div id=\"showModify\" class=\"smallLabel dirty\">To be modified <span id=\"modifyCount\">0</span></div> ");
			htmlSpreadsheet.append("<div id=\"showAdd\" class=\"smallLabel new\">To be added <span id=\"addCount\">0</span></div> ");
			htmlSpreadsheet.append("<div id=\"showDelete\" class=\"smallLabel delete\">To be deleted <span id=\"deleteCount\">0</span></div> ");
			htmlSpreadsheet.append("<div id=\"rowCount\" class=\"hidden\">" + AlbumItemStore.getAlbumItems().size() + "</div> ");
		}
		
		// Build header using album name. Include view name if appropriated
		String collectionHeader = GuiController.getGuiState().getSelectedAlbum();
		if (GuiController.getGuiState().isViewSelected()) {
			collectionHeader += " - " + GuiController.getGuiState().getSelectedSavedSearch();
		}
		
		// Create final page html
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"<html onMouseUp=\"stopDrag(event);\">" +
				  "<head>" +
				    "<title>sammelbox.org</title>" +
				    "<meta " + UIConstants.META_PARAMS + ">" + 
				    "<link rel=\"stylesheet\" href=\"" + UIConstants.STYLE_CSS_SPREADSHEET + "\" />" +
				    "<script src=\"" + UIConstants.SPREADSHEETSCRIPTS_JS + "\"></script>" +
				    javaScriptArray +
				  "</head>" +
				  "<body id=\"body\" class=\"normal\">" +
				    "<h2>" + collectionHeader + "</h2> <span id=\"info\"></span>" + 
				  	htmlSpreadsheet +
				  "</body>" +
				"</html>";
				
		browser.setText(finalPageAsHtml);		
		BrowserUtils.setLastPageAsHtml(finalPageAsHtml);		
	}
}