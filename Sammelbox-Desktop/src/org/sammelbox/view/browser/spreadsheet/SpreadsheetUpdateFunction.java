package org.sammelbox.view.browser.spreadsheet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.sammelbox.controller.GuiController;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.model.album.*;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class SpreadsheetUpdateFunction extends BrowserFunction {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetUpdateFunction.class);

	public SpreadsheetUpdateFunction (Browser browser, String name) {
		super (browser, name);
	}
	
	@Override
	public Object function (Object[] arguments) {
		List<Object> updatesColumnNameMapping = Arrays.asList((Object[]) arguments[0]);
		List<Object> updatesColumnTypeMapping = Arrays.asList((Object[]) arguments[1]);
		List<Object> updates = Arrays.asList((Object[]) arguments[2]);
		List<Object> deleteCandidates = Arrays.asList((Object[]) arguments[3]);
		
		for (Object completeRow : updates) {
			try	{
				boolean isNewAlbumItem = true;
				
				FieldType idFieldType = FieldType.valueOf((String) updatesColumnTypeMapping.get(0));
				String idFieldItemValue = (String) (((Object[]) completeRow)[0]);
				
				AlbumItem tmpAlbumItem = new AlbumItem(GuiController.getGuiState().getSelectedAlbum());
				
				if (idFieldType.equals(FieldType.ID)) {
					Long id = Long.parseLong(idFieldItemValue);
					
					if (id > AlbumItem.ITEM_ID_UNDEFINED) {
						isNewAlbumItem = false;
						tmpAlbumItem.setItemId(id);
					}
				} else {
					ComponentFactory.getMessageBox(
							  Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED_HEADER), 
							  Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED, "(Error: The first field type must be an ID field)"),
							  SWT.ERROR | SWT.OK).open();
					
					LOGGER.error("The first field type must be an ID field.");
					continue;
				}
				
				for (int i = 1; i < ((Object[]) completeRow).length; i++) {
					FieldType fieldType = FieldType.valueOf((String) updatesColumnTypeMapping.get(i));
					String fieldItemValue = (String) (((Object[]) completeRow)[i]);

					// Under Linux, leaving a spreadsheet field empty results in an empty string.
					// However, under Windows, an empty field in the spreadsheet results in a null pointer!
					// This causes then issues with the processing of the line.
					if (fieldItemValue == null) {
						fieldItemValue = "";
					}
					
					if (fieldType.equals(FieldType.OPTION)) {
						if (fieldItemValue.equals(OptionType.YES.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, OptionType.YES);
						} else if (fieldItemValue.equals(OptionType.NO.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, OptionType.NO);
						} else if (fieldItemValue.equals(OptionType.UNKNOWN.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, OptionType.UNKNOWN);
						} else {
							LOGGER.error("Tried to parse the optiontyp " + fieldItemValue + " which seems not to be a valid option.");
						}
					} else if (fieldType.equals(FieldType.DATE)) {
						if (!fieldItemValue.isEmpty()) {
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
							java.util.Date utilDate = simpleDateFormat.parse(fieldItemValue);
							utilDate.setTime(utilDate.getTime() + (1000 * 60 * 60));
							java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, sqlDate);
						} else {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, null);
						}	
					} else if (fieldType.equals(FieldType.TEXT)) {
						tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, fieldItemValue);
					} else if (fieldType.equals(FieldType.INTEGER)) {
						tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, Integer.valueOf(fieldItemValue));
					} else if (fieldType.equals(FieldType.DECIMAL)) {
						tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, Double.valueOf(fieldItemValue));
					} else if (fieldType.equals(FieldType.STAR_RATING)) {
						if (fieldItemValue.equals(StarRating.ZERO_STARS.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.ZERO_STARS);
						} else if (fieldItemValue.equals(StarRating.ONE_STAR.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.ONE_STAR);
						} else if (fieldItemValue.equals(StarRating.TWO_STARS.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.TWO_STARS);
						} else if (fieldItemValue.equals(StarRating.THREE_STARS.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.THREE_STARS);
						} else if (fieldItemValue.equals(StarRating.FOUR_STARS.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.FOUR_STARS);
						} else if (fieldItemValue.equals(StarRating.FIVE_STARS.toString())) {
							tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, StarRating.FIVE_STARS);
						} else {
							LOGGER.error("Tried to parse the starRating " + fieldItemValue + " which seems not to be a valid starRating.");
						}
					} else if (fieldType.equals(FieldType.URL)) {
						tmpAlbumItem.addField((String) updatesColumnNameMapping.get(i), fieldType, fieldItemValue);
					}
				}
				
				if (isNewAlbumItem) {
					DatabaseOperations.addAlbumItem(tmpAlbumItem, true);
				} else {
					DatabaseOperations.updateAlbumItem(tmpAlbumItem);
				}

			} catch (NumberFormatException nfe) {
				ComponentFactory.getMessageBox(
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED_HEADER), 
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED, "(Error: An error occurred while parsing a number of an update row.)"),
						SWT.ERROR | SWT.OK).open();
				
				LOGGER.error("An error occurred while parsing a number of an update row.", nfe);
			} catch (DatabaseWrapperOperationException dbwoe) {
				ComponentFactory.getMessageBox(
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED_HEADER), 
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED, "(Error: An error occurred while storing or updating an item.)"),
						SWT.ERROR | SWT.OK).open();
				
				LOGGER.error("An error occurred while storing or updating an item.", dbwoe);
			} catch (ParseException pe) {
				ComponentFactory.getMessageBox(
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED_HEADER), 
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED, "(Error: An error occurred while storing or updating a date.)"),
					    SWT.ERROR | SWT.OK).open();
				
				LOGGER.error("An error occurred while storing or updating a date.", pe);
			}
		}
		
		for (Object o : deleteCandidates){
			//JavaScript has only one type of numbers. Numbers can be written with, or without decimals:
			long id = ((Double)o).longValue();
			
			try {
				DatabaseOperations.deleteAlbumItem(AlbumItemStore.getAlbumItem(id));
			} catch (DatabaseWrapperOperationException dbwoe) {
				ComponentFactory.getMessageBox(
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED_HEADER), 
						Translator.get(DictKeys.ERROR_AN_INTERNAL_ERROR_OCCURRED, "(Error: An error occurred while deleting an item.)"),
						SWT.ERROR | SWT.OK).open();
				
				LOGGER.error("An error occurred while deleting an item.", dbwoe);
			}
		}

		ApplicationUI.setSelectedAlbumAndReload(GuiController.getGuiState().getSelectedAlbum());
		
		return null;
	}
}