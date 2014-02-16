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
import java.util.ArrayList;
import java.util.List;

import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.filters.ItemFieldFilter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SpreadsheetItemCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetItemCreator.class);
	
	private SpreadsheetItemCreator() {
	}

	public static void createSpreadsheetHeader(StringBuilder htmlSpreadsheetHeader, AlbumItem albumItem) {
		htmlSpreadsheetHeader.append("<tr class=\"header\">");
		htmlSpreadsheetHeader.append("<th>" + Translator.get(DictKeys.BUTTON_REMOVE) + "</th>");
		
		for(ItemField itemField : ItemFieldFilter.getValidItemFields(albumItem.getFields())) {
			htmlSpreadsheetHeader.append("<th>" + itemField.getName() + "</th>");
		}
		
		htmlSpreadsheetHeader.append("</tr>");
	}
	
	// TODO refactor to smaller and more reusable methods
	public static void createSpreadsheetFooter(StringBuilder htmlSpreadsheetFooter, AlbumItem albumItem) {
		// TODO find a better solution for finding an unused id - just for testing purposes
		long id = Math.round((Math.random() * 1000000));
		if (id > 0) {
			id = id * -1;
		}
		
		String albumName = GuiController.getGuiState().getSelectedAlbum();
		AlbumItem newAlbumItem = null;
		
		List<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();
		try {
			metaItemFields = DatabaseOperations.getAlbumItemFieldNamesAndTypes(albumName);
		} catch (DatabaseWrapperOperationException databaseWrapperOperationException) {
			LOGGER.error("An error occurred while retrieving the meta item fields for " + albumName, databaseWrapperOperationException);
		}
		
		List<ItemField> itemFields = new ArrayList<>();
		itemFields.add(new ItemField(DatabaseConstants.ID_COLUMN_NAME, FieldType.ID, id));
		for (MetaItemField metaItemField : metaItemFields) {
			if (metaItemField.getType().equals(FieldType.DATE)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), null));
			} else if (metaItemField.getType().equals(FieldType.DECIMAL)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 0.0));
			} else if (metaItemField.getType().equals(FieldType.ID)) {
				System.out.println("Name : " + metaItemField.getName());
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), -1));
			} else if (metaItemField.getType().equals(FieldType.INTEGER)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 0));
			} else if (metaItemField.getType().equals(FieldType.OPTION)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.UNKNOWN));
			} else if (metaItemField.getType().equals(FieldType.STAR_RATING)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), StarRating.ZERO_STARS));
			} else if (metaItemField.getType().equals(FieldType.TEXT)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), ""));
			} else if (metaItemField.getType().equals(FieldType.TIME)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), null));
			} else if (metaItemField.getType().equals(FieldType.URL)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), ""));
			} else if (metaItemField.getType().equals(FieldType.UUID)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), null));
			}
		}
		
		newAlbumItem = new AlbumItem(albumName, itemFields);		
		newAlbumItem.setFieldValue("id", id);
		
		StringBuilder tmp = new StringBuilder();
		createSpreadsheetRow(newAlbumItem, tmp); 
		
		htmlSpreadsheetFooter.append("<tr id=\"row:" + id + "\" class=\"empty\">");
		htmlSpreadsheetFooter.append(tmp);
		htmlSpreadsheetFooter.append("</tr>");
	}
	
	static long createSpreadsheetRow(AlbumItem albumItem, StringBuilder htmlSpreadsheetRow) {
		long id = -1;
		long fieldIndex = 0;
		
		for (ItemField fieldItem : albumItem.getFields()) {			
			if (fieldItem.getType().equals(FieldType.UUID)) {
				// schema or content version UUID --> ignore 
			} else if (fieldItem.getType().equals(FieldType.ID)) {
				if (fieldItem.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
					// do not show, but store id
					id = fieldItem.getValue();
					htmlSpreadsheetRow.append("<td><input type=\"checkbox\" id=\"delete:" + id + "\" value=\"" + id + "\" onClick='markAsDelete(" + id + ");'></td>");
				}
			} else if (fieldItem.getType().equals(FieldType.OPTION)) {
				if (fieldItem.getValue() == OptionType.YES) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_YES), OptionType.YES, id, fieldIndex));
				} else if (fieldItem.getValue() == OptionType.NO) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_NO), OptionType.NO, id, fieldIndex));
				} else if (fieldItem.getValue() == OptionType.UNKNOWN) {
					htmlSpreadsheetRow.append(getYesNoUnknownComboBox(Translator.get(DictKeys.BROWSER_UNKNOWN), OptionType.UNKNOWN, id, fieldIndex));
				}	
			} else if (fieldItem.getType().equals(FieldType.DATE)) {
				
				java.sql.Date sqlDate = fieldItem.getValue();
				if (sqlDate != null) {
					java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
					SimpleDateFormat dateFormater = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
					htmlSpreadsheetRow.append(getValueLine(dateFormater.format(utilDate), id, fieldIndex));
				} else {
					htmlSpreadsheetRow.append(getValueLine("", id, fieldIndex));
				}	
			} else  if (fieldItem.getType().equals(FieldType.TEXT)) {
				htmlSpreadsheetRow.append(getValueLine(BrowserUtils.escapeHtmlString((String) fieldItem.getValue()), id, fieldIndex));
			} else if (fieldItem.getType().equals(FieldType.INTEGER)) {
				htmlSpreadsheetRow.append(getValueLine(((Integer) fieldItem.getValue()).toString(), id, fieldIndex));
			} else if (fieldItem.getType().equals(FieldType.DECIMAL)) {
				htmlSpreadsheetRow.append(getValueLine(((Double) fieldItem.getValue()).toString(), id, fieldIndex));
			} else if (fieldItem.getType().equals(FieldType.STAR_RATING)) {
				htmlSpreadsheetRow.append(getStarsAsComboBoxes(((StarRating) fieldItem.getValue()), id, fieldIndex));
			} else if (fieldItem.getType().equals(FieldType.URL)) {
				htmlSpreadsheetRow.append(getValueLine(((String) fieldItem.getValue()), id, fieldIndex));	
			}
			
			fieldIndex++;
		}
		
		return id;
	}
	
	public static void createNextDataRow(AlbumItem albumItem, StringBuilder htmlSpreadsheetData, StringBuilder htmlSpreadsheetRow, boolean hasEvenCountingInList) {
		// the id of the current album item
		long id = -1;
		String oddOrEven = null;
		id = createSpreadsheetRow(albumItem, htmlSpreadsheetRow);
		
		if (hasEvenCountingInList) {
			oddOrEven = "evenBackGround";
		}
		else{
			oddOrEven = "oddBackGround";
		}
		
		// Here a way to add multiple id's without violating anything
		// <div id='enclosing_id_123'><span id='enclosed_id_123'></span></div> 
		htmlSpreadsheetData.append(		
			"<tr id=\"row:" + id + "\" class=\"albumItem " + oddOrEven + " \">" +
					htmlSpreadsheetRow +	
			"</tr>");
	}
	
	
	private static String getValueLine(String value, long id, long fieldIndex) {
		return "<td id=\"value:" + id + ":" + fieldIndex + "\" class=\"field\"> " +
				"<input id=\"input:" + id + ":" + fieldIndex + "\" type=\"text\" value=\"" + value + "\" onChange=\"markAsDirty('"+id+"');\">" +
				"</td>"; 
	}
	
	private static String getYesNoUnknownComboBox(String value, OptionType selectedOption, long id, long fieldIndex) {
		
		String selectedYes = "";
		String selectedNo = "";
		String selectedUnknown = "";

		if (selectedOption.equals(OptionType.YES)){
			selectedYes = "Selected";
		}
		else if (selectedOption.equals(OptionType.NO)){
			selectedNo = "Selected";
		}
		else if (selectedOption.equals(OptionType.UNKNOWN)) {
			selectedUnknown = "Selected";
		}
		else {
			LOGGER.error("Should not get here. This optiontype is not known...");
		}
		
		return "<td><select id=\"select:" + id + ":" + fieldIndex + "\" onChange=\"markAsDirty('"+id+"');\">" +
				"<option value=\"" + OptionType.YES + "\" " + selectedYes + ">" + Translator.get(DictKeys.BROWSER_YES) + "</option>" +
				"<option value=\"" + OptionType.NO + "\" " + selectedNo + ">" + Translator.get(DictKeys.BROWSER_NO) + "</option>" +
				"<option value=\"" + OptionType.UNKNOWN + "\" " + selectedUnknown + ">" + Translator.get(DictKeys.BROWSER_UNKNOWN) + "</option>" +
				"</select></td>";
	}
	
	// TODO decide to use or delete if not
	@SuppressWarnings("unused")
	private static String getStars(StarRating rating) {
		if (rating.equals(StarRating.ONE_STAR)) {
			return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getOneStarPNG() + "\"></td>";
		} else if (rating.equals(StarRating.TWO_STARS)) {
			return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getTwoStarsPNG() + "\"></td>";
		} else if (rating.equals(StarRating.THREE_STARS)) {
			return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getThreeStarsPNG() + "\"></td>";
		} else if (rating.equals(StarRating.FOUR_STARS)) {
			return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getFourStarsPNG() + "\"></td>";
		} else if (rating.equals(StarRating.FIVE_STARS)) {
			return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getFiveStarsPNG() + "\"></td>";
		}
		return "<td class=\"field\"><img alt=\"\" height=\"20\" src=\"" + FileSystemLocations.getZeroStarsPNG() + "\"></td>";
	}
	
	private static String getStarsAsComboBoxes(StarRating rating, long id, long fieldIndex) {
		String oneSelected = "";
		String twoSelected = "";
		String threeSelected = "";
		String fourSelected = "";
		String fiveSelected = "";
		String zeroSelected = "";
		
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
		} else{
			zeroSelected = "selected";
		}
		
		String zeroStar = StarRating.ZERO_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String oneStar = StarRating.ONE_STAR.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String twoStar = StarRating.TWO_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String threeStar = StarRating.THREE_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String fourStar = StarRating.FOUR_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		String fiveStar = StarRating.FIVE_STARS.getIntegerValue() + " " + Translator.get(DictKeys.COMBOBOX_CONTENT_STARS);
		
		return "<td><select id=\"select:" + id + ":" + fieldIndex + "\" onChange=\"markAsDirty('"+id+"');\">" +
		"<option value=\"" + StarRating.ZERO_STARS + "\" " + zeroSelected + ">" + zeroStar + "</option>" +
		"<option value=\"" + StarRating.ONE_STAR + "\" " + oneSelected + ">" + oneStar + "</option>" +
		"<option value=\"" + StarRating.TWO_STARS + "\" " + twoSelected + ">" + twoStar + "</option>" +
		"<option value=\"" + StarRating.THREE_STARS + "\" " + threeSelected + ">" + threeStar + "</option>" +
		"<option value=\"" + StarRating.FOUR_STARS + "\" " + fourSelected + ">" + fourStar + "</option>" +
		"<option value=\"" + StarRating.FIVE_STARS + "\" " + fiveSelected + ">" + fiveStar + "</option>" +
		"</select></td>";
	}
}
