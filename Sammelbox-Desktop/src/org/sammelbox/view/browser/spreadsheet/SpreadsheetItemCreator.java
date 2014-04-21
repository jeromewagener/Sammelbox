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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.controller.filters.ItemFieldFilterPlusID;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.album.StarRating;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.browser.BrowserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpreadsheetItemCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetItemCreator.class);
	
	private SpreadsheetItemCreator() {
	}

	public static void createSpreadsheetHeader(StringBuilder htmlSpreadsheetHeader, AlbumItem albumItem, Map<MetaItemField, Integer> metaItemToColumnIndexMap) {
		htmlSpreadsheetHeader.append("<tr class=\"header\">");
		htmlSpreadsheetHeader.append("<th> Del. </th>");
		
		long columnIndex = 0;
		for(ItemField itemField : ItemFieldFilter.getValidItemFields(albumItem.getFields())) {
			for (MetaItemField metaItemField : metaItemToColumnIndexMap.keySet()) {
				if (metaItemField.getName().equals(itemField.getName())
						&& metaItemField.getType().equals(itemField.getType())) {
					columnIndex = metaItemToColumnIndexMap.get(metaItemField);
					break;
				}
			}
			
			htmlSpreadsheetHeader.append(
				"<th id=\"col:" + columnIndex + "\" width=\"170\">" + 
					"<table class=\"headerTable\">" +
						"<tr>" +
							"<td class=\"labelTd\">" + 
						    	"<div id=\"label:" + columnIndex + "\" class=\"label\">" + itemField.getName() + "</div>" + 
							"</td>" +
						    	
							"<td class=\"smallTd\">" + 
								"<div id=\"arrowRight:" + columnIndex + "\" " +
									 "class=\"hideByMe\" " +
									 "onClick=\"setHidden(" + columnIndex + ");\" " +
									 "style=\"background-image:url(" + FileSystemLocations.getMinimizeArrowPNG() + ")\">" +
								"</div>" +
								"<div id=\"arrowLeft:" + columnIndex + "\" " +
									 "class=\"hideByMe hidden tooltip\" " +
									 "onClick=\"setHidden(" + columnIndex + ");\" " +
									 "style=\"background-image:url(" + FileSystemLocations.getMaximizeArrowPNG() + ")\">" +
									 "<label> " + itemField.getName() + " </label>" +
								"</div>" +
							"</td>" +
								
							"<td class=\"smallTd\">" + 
								"<div id=\"dragMe:" + columnIndex + "\" " +
									 "class=\"dragMe\" " +
									 "onmousedown=\"startDrag(" + columnIndex + " ,event);\"  " +
									 "style=\"background-image:url(" + FileSystemLocations.getResizeSpreadsheetPNG() + ")\">" +
								"</div>" + 
							"</td>" +
						"</tr>" +
					"</table>" +
				"</th>");
		}
		
		htmlSpreadsheetHeader.append("</tr>");
	}
	
	public static long createSpreadsheetFooter(StringBuilder htmlSpreadsheetFooter, AlbumItem albumItem, 
												 Map<MetaItemField, Integer> metaItemToColumnIndexMap) {
		
		String albumName = GuiController.getGuiState().getSelectedAlbum();
		AlbumItem newAlbumItem = null;
		
		List<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();
		try {
			metaItemFields = DatabaseOperations.getMetaItemFields(albumName);
		} catch (DatabaseWrapperOperationException databaseWrapperOperationException) {
			LOGGER.error("An error occurred while retrieving the meta item fields for " + albumName, databaseWrapperOperationException);
		}
		
		newAlbumItem = new AlbumItem(albumName);		
		newAlbumItem.initializeWithDefaultValuesUsingMetaItems(metaItemFields);
		
		StringBuilder footerRowBuilder = new StringBuilder();
		createSpreadsheetRow(newAlbumItem, footerRowBuilder, metaItemToColumnIndexMap);
		
		htmlSpreadsheetFooter.append("<tr id=\"row:" + newAlbumItem.getItemId() + "\" class=\"empty\">");
		htmlSpreadsheetFooter.append(footerRowBuilder);
		htmlSpreadsheetFooter.append("</tr>");
		
		return newAlbumItem.getItemId();
	}
	
	static long createSpreadsheetRow(AlbumItem albumItem, StringBuilder htmlSpreadsheetRow, Map<MetaItemField, Integer> metaItemToColumnIndexMap) {
		long row = -1;
		htmlSpreadsheetRow.delete(0, htmlSpreadsheetRow.length());
				
		htmlSpreadsheetRow.append("<td id=\"delete:" + albumItem.getField(FieldType.ID) + "" + "\" class=\"field\">X" + albumItem.getField(FieldType.ID) + "</td>");
		
		for (ItemField itemField : ItemFieldFilterPlusID.getValidItemFields(albumItem.getFields())) {		
			long col = 0;
			for (MetaItemField metaItemField : metaItemToColumnIndexMap.keySet()) {
				if (metaItemField.getName().equals(itemField.getName())
						&& metaItemField.getType().equals(itemField.getType())) {
					col = metaItemToColumnIndexMap.get(metaItemField);
					break;
				}
			}
			
			if (itemField.getType().equals(FieldType.UUID)) {
				// schema or content version UUID --> ignore 
			} else if (itemField.getType().equals(FieldType.ID)) {
				if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					// do not show, but store id
					row = itemField.getValue();
				}
			} else if (itemField.getType().equals(FieldType.OPTION)) {
				if (itemField.getValue() == OptionType.YES) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_YES), OptionType.YES, row, col));
				} else if (itemField.getValue() == OptionType.NO) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_NO), OptionType.NO, row, col));
				} else if (itemField.getValue() == OptionType.UNKNOWN) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_UNKNOWN), OptionType.UNKNOWN, row, col));
				}	
			} else if (itemField.getType().equals(FieldType.DATE)) {
				java.sql.Date sqlDate = itemField.getValue();
				if (sqlDate != null) {
					java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
					SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
					htmlSpreadsheetRow.append(getValueLine(dateFormater.format(utilDate), row, col));
				} else {
					htmlSpreadsheetRow.append(getValueLine("", row, col));
				}	
			} else if (itemField.getType().equals(FieldType.TEXT)) {
				htmlSpreadsheetRow.append(getValueLine(BrowserUtils.escapeHtmlString((String) itemField.getValue()), row, col));
			} else if (itemField.getType().equals(FieldType.INTEGER)) {
				htmlSpreadsheetRow.append(getValueLine(((Integer) itemField.getValue()).toString(), row, col));
			} else if (itemField.getType().equals(FieldType.DECIMAL)) {
				htmlSpreadsheetRow.append(getValueLine(((Double) itemField.getValue()).toString(), row, col));
			} else if (itemField.getType().equals(FieldType.STAR_RATING)) {
				htmlSpreadsheetRow.append(getStarsAsComboBoxes(((StarRating) itemField.getValue()), row, col));
			} else if (itemField.getType().equals(FieldType.URL)) {
				htmlSpreadsheetRow.append(getValueLine(((String) itemField.getValue()), row, col));	
			}
			
			col++;
		}

		return row;
	}
	
	public static void createNextDataRow(AlbumItem albumItem, StringBuilder htmlSpreadsheetData, StringBuilder htmlSpreadsheetRow, boolean hasEvenCountingInList, Map<MetaItemField, Integer> metaItemToColumnIndexMap) {
		long id = -1;
		String oddOrEvenCssClass = null;
		id = createSpreadsheetRow(albumItem, htmlSpreadsheetRow, metaItemToColumnIndexMap);
		
		if (hasEvenCountingInList) {
			oddOrEvenCssClass = "evenBackGround";
		} else {
			oddOrEvenCssClass = "oddBackGround";
		}
		
		htmlSpreadsheetData.append(		
			"<tr id=\"row:" + id + "\" class=\"albumItem " + oddOrEvenCssClass + " \">" +
					htmlSpreadsheetRow +	
			"</tr>");
	}
	
	
	private static String getValueLine(String value, long id, long columnIndex) {
		return "<td id=\"value:" + columnIndex + ":" + id + "\" class=\"field\"> " +
					"<div id=\"hideThisContainer:"  + columnIndex + ":" + id + "\" class=\"normal\">"+
					    "<input id=\"input:" + columnIndex + ":" + id + "\" type=\"text\" value=\"" + value + "\" onChange=\"markAsDirty('" + id + "', '" + columnIndex + "');\">" +
					"</div>" +
				"</td>";
	}
	
	private static String getYesNoUnknownComboBox(String value, OptionType selectedOption, long id, long columnIndex) {
		String selectedYes = "";
		String selectedNo = "";
		String selectedUnknown = "";

		if (selectedOption.equals(OptionType.YES)) {
			selectedYes = "Selected";
		} else if (selectedOption.equals(OptionType.NO)) {
			selectedNo = "Selected";
		} else if (selectedOption.equals(OptionType.UNKNOWN)) {
			selectedUnknown = "Selected";
		} else {
			LOGGER.error("Should not get here. This optiontype is not known...");
		}
		
		return "<td id=\"value:" + columnIndex + ":" + id + "\" class=\"field\"> " +
					"<div id=\"hideThisContainer:"  + columnIndex + ":" + id + "\" class=\"normal\">" +
						"<select id=\"input:" + columnIndex + ":" + id + "\" onChange=\"markAsDirty('" + id + "', '" + columnIndex + "');\">" +
							"<option value=\"" + OptionType.YES + "\" " + selectedYes + ">" + Translator.get(DictKeys.BROWSER_YES) + "</option>" +
							"<option value=\"" + OptionType.NO + "\" " + selectedNo + ">" + Translator.get(DictKeys.BROWSER_NO) + "</option>" +
							"<option value=\"" + OptionType.UNKNOWN + "\" " + selectedUnknown + ">" + Translator.get(DictKeys.BROWSER_UNKNOWN) + "</option>" +
						"</select>" +
					"</div>" +
				"</td>";
	}
	
	private static String getStarsAsComboBoxes(StarRating rating, long id, long columnIndex) {
		String oneSelected = "", twoSelected = "", threeSelected = "", fourSelected = "", fiveSelected = "", zeroSelected = "";
		
		if (rating.equals(StarRating.ONE_STAR)) {
			oneSelected = "selected";
		} else if (rating.equals(StarRating.TWO_STARS)) {
			twoSelected = "selected";
		} else if (rating.equals(StarRating.THREE_STARS)) {
			threeSelected = "selected";
		} else if (rating.equals(StarRating.FOUR_STARS)) {
			fourSelected = "selected";
		} else if (rating.equals(StarRating.FIVE_STARS)) {
			fiveSelected = "selected";
		} else {
			zeroSelected = "selected";
		}
		
		String zeroStar = StarRating.ZERO_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String oneStar = StarRating.ONE_STAR.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String twoStar = StarRating.TWO_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String threeStar = StarRating.THREE_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String fourStar = StarRating.FOUR_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String fiveStar = StarRating.FIVE_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		
		return "<td id=\"value:" + columnIndex + ":" + id + "\" class=\"field\"> " +
					"<div id=\"hideThisContainer:"  + columnIndex + ":" + id + "\" class=\"normal\">" +
						"<select id=\"input:" + columnIndex + ":" + id + "\" onChange=\"markAsDirty('" + id + "', '" + columnIndex + "');\">" +
							"<option value=\"" + StarRating.ZERO_STARS + "\" " + zeroSelected + ">" + zeroStar + "</option>" +
							"<option value=\"" + StarRating.ONE_STAR + "\" " + oneSelected + ">" + oneStar + "</option>" +
							"<option value=\"" + StarRating.TWO_STARS + "\" " + twoSelected + ">" + twoStar + "</option>" +
							"<option value=\"" + StarRating.THREE_STARS + "\" " + threeSelected + ">" + threeStar + "</option>" +
							"<option value=\"" + StarRating.FOUR_STARS + "\" " + fourSelected + ">" + fourStar + "</option>" +
							"<option value=\"" + StarRating.FIVE_STARS + "\" " + fiveSelected + ">" + fiveStar + "</option>" +
						"</select>" +
					"</div>" +
				"</td>";
	}
}
