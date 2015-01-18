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

import org.eclipse.swt.browser.Browser;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.UIConstants;
import org.sammelbox.view.browser.BrowserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpreadsheetViewCreator {	
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetViewCreator.class);

	private SpreadsheetViewCreator() {
	}
	
	/** Inverts map. Keys become values.
	 * Values must be unique for this function to work. */
	private static <V, K> Map<V, K> invert(Map<K, V> map) {
	    Map<V, K> inv = new HashMap<>();

	    for (Map.Entry<K, V> entry : map.entrySet()) {
	        inv.put(entry.getValue(), entry.getKey());
	    }
	    
	    return inv;
	}
	
	public static void showSpreadsheetAlbum(List<Long> selectedIds, Browser browser) {
		// Exit if no album is selected
		if (!ApplicationUI.isAlbumSelectedAndShowMessageIfNot()) {
			return;
		}
		
		Map<Integer, MetaItemField> indexToMetaItemFieldMap;
		List<MetaItemField> metaItemFields = null;
		
		try {
			indexToMetaItemFieldMap = DatabaseOperations.getAlbumItemMetaMap(GuiController.getGuiState().getSelectedAlbum());
			metaItemFields = new ArrayList<>(indexToMetaItemFieldMap.values());
		} catch (DatabaseWrapperOperationException dwoe) {
			LOGGER.error("An error occurred while gathering meta information", dwoe);
		}
		
		Map<Integer, MetaItemField> columnIndexToMetaItemMap = null;
		Map<MetaItemField, Integer> metaItemToColumnIndexMap;
		
		try {
			columnIndexToMetaItemMap = DatabaseOperations.getAlbumItemMetaMap(GuiController.getGuiState().getSelectedAlbum());
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("An error occurred while retrieving the albumItemMetaMap. ", e);
		}
		
		metaItemToColumnIndexMap = invert(columnIndexToMetaItemMap);
		
		// Builders for efficient html creation
		StringBuilder htmlSpreadsheet = new StringBuilder();
		StringBuilder htmlSpreadsheetHeader = new StringBuilder();
		StringBuilder htmlSpreadsheetData = new StringBuilder();
		StringBuilder htmlSpreadsheetRow = new StringBuilder();
		StringBuilder htmlSpreadsheetFooter = new StringBuilder();
		StringBuilder javaScriptArrayTableRowId = new StringBuilder();
		StringBuilder javaScriptArrayTableColId = new StringBuilder();
		StringBuilder javaScriptArrayTableColType = new StringBuilder();
		StringBuilder javaScriptArrayTableColName = new StringBuilder();
		
		AlbumItem emptyAlbumItem = AlbumItemStore.getEmptyAlbumItem(GuiController.getGuiState().getSelectedAlbum(), metaItemFields);
		
		// Create the header of the spreadsheet
			SpreadsheetItemCreator.createSpreadsheetHeader(
					htmlSpreadsheetHeader, emptyAlbumItem, metaItemToColumnIndexMap);
		
		// Add all available album items
		boolean hasEvenCountingInList = false;

		for(Long id : selectedIds){
			AlbumItem albumItem = AlbumItemStore.getAlbumItem(id);
			
			SpreadsheetItemCreator.createNextDataRow(
					albumItem, htmlSpreadsheetData, htmlSpreadsheetRow, hasEvenCountingInList, metaItemToColumnIndexMap);
			hasEvenCountingInList = !hasEvenCountingInList;
		}
		
		// Create the footer of the spreadsheet
		long idForUncreatedItem = SpreadsheetItemCreator.createSpreadsheetFooter(htmlSpreadsheetFooter, emptyAlbumItem, metaItemToColumnIndexMap);

		// List all the rowIDs and put them in an array for javascript usages.
		javaScriptArrayTableRowId.append(" var tableRowId=[");
			
		for (Long id : selectedIds) {
			javaScriptArrayTableRowId.append("'").append(id).append("', ");
		}
		
		// List all the columnIDs and put them in arrays for javascript usages.
		javaScriptArrayTableColId.append(" var tableColId=[");
		javaScriptArrayTableColType.append(" var tableColType=[");
		javaScriptArrayTableColName.append(" var tableColName=[");
		
		javaScriptArrayTableColId.append("'1'");
		javaScriptArrayTableColType.append("'").append(FieldType.ID.toString()).append("'");
		javaScriptArrayTableColName.append("'" + DatabaseConstants.ID_COLUMN_NAME + "'");
		
		for (ItemField itemField : ItemFieldFilter.getValidItemFields(emptyAlbumItem.getFields())) {
			int columnIndex = 0;
			
			for (MetaItemField metaItemField : metaItemToColumnIndexMap.keySet()) {
				if (metaItemField.getName().equals(itemField.getName())
					&& metaItemField.getType().equals(itemField.getType())) {
					columnIndex = metaItemToColumnIndexMap.get(metaItemField);
					break;
				}
			}
			
			String columnType = itemField.getType().toString();
			String columnName = itemField.getName();

			javaScriptArrayTableColType.append(", '").append(columnType).append("'");
			javaScriptArrayTableColName.append(", '").append(columnName).append("'");
			
			javaScriptArrayTableColId.append(", '").append(columnIndex).append("'");
		}
		
		javaScriptArrayTableRowId.append(idForUncreatedItem);
		
		javaScriptArrayTableRowId.append("]; ");
		javaScriptArrayTableColId.append("]; ");
		javaScriptArrayTableColType.append("]; ");
		javaScriptArrayTableColName.append("]; ");
		
		htmlSpreadsheet.append("<div id=\"nextFreeId\" class=\"hidden\">").append(AlbumItem.ITEM_ID_UNDEFINED).append("</div>");
		
		htmlSpreadsheet.append("<div class=\"tableWrapper\">");
		htmlSpreadsheet.append("<table id=\"spreadsheetTable\">");
		htmlSpreadsheet.append(htmlSpreadsheetHeader);
		htmlSpreadsheet.append(htmlSpreadsheetData);
		htmlSpreadsheet.append(htmlSpreadsheetFooter);
		htmlSpreadsheet.append("</table>");
		htmlSpreadsheet.append("</div>");
		htmlSpreadsheet.append("</br>");
		htmlSpreadsheet.append("<button id=\"checkAndSend\" type=\"button\" onclick=\"checkAndSend();\" >").append(Translator.get(DictKeys.BROWSER_SAVE_CHANGES)).append("</button> ");
		htmlSpreadsheet.append("<button id=\"return\" type=\"button\" onclick=\"spreadsheetAbortFunction();\" >").append(Translator.get(DictKeys.BROWSER_ABORT)).append("</button> ");
		htmlSpreadsheet.append("</br></br>");
		htmlSpreadsheet.append("<label>");
		htmlSpreadsheet.append("<div id=\"showModify\" class=\"hidden smallLabel dirty\">To be modified <span id=\"modifyCount\">0</span></div> ");
		htmlSpreadsheet.append("<div id=\"showAdd\" class=\"hidden smallLabel new\">To be added <span id=\"addCount\">0</span></div> ");
		htmlSpreadsheet.append("<div id=\"rowCount\" class=\"hidden\">").append(AlbumItemStore.getAlbumItems().size()).append("</div> ");
		htmlSpreadsheet.append("<div id=\"dragPreview\" class=\"dragPreview hidden\"></div>");
		htmlSpreadsheet.append("</label>");
		
		// Create final page
		String finalPageAsHtml = 
				"<!DOCTYPE HTML>" +
				"<html onMouseUp=\"stopDrag(event);\" onMouseMove=\"moveDiv(event);\">" +
				  "<head>" +
				    "<title>sammelbox.org</title>" +
				    "<meta " + UIConstants.META_PARAMS + ">" + 
				    "<link rel=\"stylesheet\" href=\"" + UIConstants.STYLE_CSS_SPREADSHEET + "\" />" +
				    "<script src=\"" + UIConstants.SPREADSHEETSCRIPTS_JS + "\"></script>" +
				    "<script>" +
						javaScriptArrayTableRowId +
						javaScriptArrayTableColId +
						javaScriptArrayTableColType +
						javaScriptArrayTableColName +
				    "</script>" +
				  "</head>" +
				  "<body id=\"body\" class=\"normal\">" +
				  	htmlSpreadsheet +
				  "</body>" +
				"</html>";
				
		browser.setText(finalPageAsHtml);		
		BrowserUtils.setLastPageAsHtml(finalPageAsHtml);		
	}
}