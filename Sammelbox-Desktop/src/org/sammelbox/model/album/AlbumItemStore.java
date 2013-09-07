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

package org.sammelbox.model.album;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.composites.StatusBarComposite;

public final class AlbumItemStore {
	private static final String SAMPLE = "Sample";
	private static final int DEFAULT_STOP_INDEX_INCREASE_AMOUNT = 0;
	private static final int DEFAULT_STOP_INDEX = 50;
	
	private static List<AlbumItem> albumItems = new ArrayList<AlbumItem>();
	private static int stopIndex = DEFAULT_STOP_INDEX;
	private static int previousStopIndex = DEFAULT_STOP_INDEX;
	
	private AlbumItemStore() {
	}
	
	public static void reinitializeStore(AlbumItemResultSet albumItemResultSet) throws DatabaseWrapperOperationException {
		albumItems.clear();
		stopIndex = DEFAULT_STOP_INDEX;
		previousStopIndex = DEFAULT_STOP_INDEX;
		
		while (albumItemResultSet.moveToNext()) {
			List<ItemField> itemFields = new ArrayList<ItemField>();
			
			for (int i=1; i<=albumItemResultSet.getFieldCount(); i++) {				
				itemFields.add(new ItemField(albumItemResultSet.getFieldName(i), albumItemResultSet.getFieldType(i), albumItemResultSet.getFieldValue(i)));
			}
			
			AlbumItem albumItem = new AlbumItem(albumItemResultSet.getAlbumName(), itemFields);
			albumItem.setFields(itemFields);
			albumItems.add(albumItem);
		}
		
		albumItemResultSet.close();
		
		StatusBarComposite.getInstance(ApplicationUI.getShell()).writeStatus(
				Translator.get(DictKeys.STATUSBAR_NUMBER_OF_ITEMS, albumItems.size()), false);
	}
	
	public static List<AlbumItem> getAllAlbumItems() {
		return albumItems;
	}
	
	public static List<AlbumItem> getAlbumItemsInRange(int startIndex, int stopIndex) {
		List<AlbumItem> resultList = new ArrayList<AlbumItem>();
		
		for (int i=startIndex; i<=stopIndex; i++) {
			resultList.add(albumItems.get(i));
		}
		
		return resultList;
	}
	
	public static List<AlbumItem> getAlbumItems(int stopIndex) {
		return getAlbumItemsInRange(0, stopIndex);
	}
	
	public static int getPreviousStopIndex() {
		if (previousStopIndex >= albumItems.size() - 1) {
			return albumItems.size() - 1;
		}
		
		return previousStopIndex;
	}
	
	public static int getStopIndex() {
		if (stopIndex >= albumItems.size() - 1) {
			return albumItems.size() - 1;
		}
		
		return stopIndex;
	}

	public static void increaseStopIndex() {
		previousStopIndex = stopIndex;		
		stopIndex += DEFAULT_STOP_INDEX_INCREASE_AMOUNT;
		
		if (stopIndex > albumItems.size() - 1) {
			stopIndex = albumItems.size() - 1;
		}
	}

	public static boolean isStopIndexAtEnd() {
		return stopIndex >= albumItems.size() - 1;
	}

	public static AlbumItem getAlbumItem(long albumItemId) {
		for (AlbumItem albumItem : albumItems) {
			if (albumItem.getItemID() == albumItemId) {
				return albumItem;
			}
		}
		
		return null;
	}
	
	public static AlbumItem getSamplePictureAlbumItemWithoutFields() {
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();
		
		pictures.add(new SampleAlbumItemPicture(FileSystemLocations.getPlaceholder2PNG()));
		pictures.add(new SampleAlbumItemPicture(FileSystemLocations.getPlaceholder3PNG()));
		
		List<ItemField> itemFields = new ArrayList<ItemField>();
		
		itemFields.add(new ItemField(Translator.get(
				DictKeys.BROWSER_NO_FIELDS_ADDED_YET), FieldType.TEXT, Translator.get(DictKeys.BROWSER_PLEASE_USE_NEW_ALBUM_SIDEPANE)));		
		
		AlbumItem albumItem = new AlbumItem(SAMPLE, itemFields);
		albumItem.setPictures(pictures);
		
		return albumItem;
	}
	
	public static AlbumItem getSampleAlbumItem(boolean containsPictures, List<MetaItemField> metaItemFields) {
		List<ItemField> itemFields = new ArrayList<ItemField>();
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();		
		
		if (containsPictures) {
			pictures.add(new SampleAlbumItemPicture(FileSystemLocations.getPlaceholder2PNG()));
			pictures.add(new SampleAlbumItemPicture(FileSystemLocations.getPlaceholder3PNG()));
		}
		
		if (metaItemFields.isEmpty()) {
			itemFields.add(new ItemField(Translator.get(
					DictKeys.BROWSER_NO_FIELDS_ADDED_YET), FieldType.TEXT, Translator.get(DictKeys.BROWSER_PLEASE_USE_NEW_ALBUM_SIDEPANE)));
		} else {
			for (MetaItemField metaItemField : metaItemFields) {
				if (metaItemField.getType().equals(FieldType.TEXT)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), Translator.get(
							DictKeys.BROWSER_THIS_IS_A_SAMPLE_TEXT, metaItemField.getName()), false));
				} else if (metaItemField.getType().equals(FieldType.DATE)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), new java.sql.Date(System.currentTimeMillis()), false));
				} else if (metaItemField.getType().equals(FieldType.INTEGER)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 10 + (int)(Math.random() * ((90) + 1)), false));
				} else if (metaItemField.getType().equals(FieldType.DECIMAL)) {
					BigDecimal randomDecimal = new BigDecimal(Math.random() * 100);
				    randomDecimal = randomDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), randomDecimal.doubleValue()));
				} else if (metaItemField.getType().equals(FieldType.OPTION)) {
					int option = (int)(Math.random() * ((2) + 1));
					
					if (option == 0) {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.NO, false));
					} else if (option != 1) {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.YES, false));
					} else {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.UNKNOWN, false));
					}
					
				} else if (metaItemField.getType().equals(FieldType.STAR_RATING)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), StarRating.values()[(int)(Math.random() * ((5) + 1))], false));
				} else if (metaItemField.getType().equals(FieldType.TIME)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), System.currentTimeMillis(), false));
				} else if (metaItemField.getType().equals(FieldType.URL)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), "www.sammelbox.org", false));
				}
			}
		}
		
		AlbumItem albumItem = new AlbumItem(SAMPLE, itemFields);
		albumItem.setPictures(pictures);
		
		return albumItem;
	}
}
