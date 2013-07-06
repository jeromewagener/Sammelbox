package collector.desktop.model.database;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItem.AlbumItemPicture;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.ItemField;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.album.OptionType;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.ExceptionHelper;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;

public class AlbumItemStore {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumItemStore.class);
	
	private static final int DEFAULT_STOP_INDEX_INCREASE_AMOUNT = 10;
	private static final int DEFAULT_STOP_INDEX = 40;
	
	private static List<AlbumItem> albumItems = new ArrayList<AlbumItem>();
	private static int stopIndex = DEFAULT_STOP_INDEX;
	private static int previousStopIndex = DEFAULT_STOP_INDEX;
	
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

	public static List<AlbumItem> getAllVisibleAlbumItems() {
		return getAlbumItems(getStopIndex());
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
		
		pictures.add(new AlbumItemPicture(FileSystemAccessWrapper.PLACEHOLDERIMAGE, FileSystemAccessWrapper.PLACEHOLDERIMAGE));
		pictures.add(new AlbumItemPicture(FileSystemAccessWrapper.PLACEHOLDERIMAGE2, FileSystemAccessWrapper.PLACEHOLDERIMAGE2));
		
		List<ItemField> itemFields = new ArrayList<ItemField>();
		
		itemFields.add(new ItemField(Translator.get(DictKeys.BROWSER_NO_FIELDS_ADDED_YET), FieldType.Text, Translator.get(DictKeys.BROWSER_PLEASE_USE_NEW_ALBUM_SIDEPANE)));		
		
		try {
			AlbumItem albumItem = new AlbumItem("DummyItem", itemFields);
			albumItem.setPictures(pictures);
			
			return albumItem;
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while creating a sample album item \n " + ExceptionHelper.toString(ex));
		}
		
		return null;
	}
	
	public static AlbumItem getSampleAlbumItem(boolean containsPictures, List<MetaItemField> metaItemFields) {
		List<ItemField> itemFields = new ArrayList<ItemField>();
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();		
		
		if (containsPictures) {
			pictures.add(new AlbumItemPicture(FileSystemAccessWrapper.PLACEHOLDERIMAGE, FileSystemAccessWrapper.PLACEHOLDERIMAGE));
			pictures.add(new AlbumItemPicture(FileSystemAccessWrapper.PLACEHOLDERIMAGE2, FileSystemAccessWrapper.PLACEHOLDERIMAGE2));
		}
		
		if (metaItemFields.isEmpty()) {
			itemFields.add(new ItemField(Translator.get(DictKeys.BROWSER_NO_FIELDS_ADDED_YET), FieldType.Text, Translator.get(DictKeys.BROWSER_PLEASE_USE_NEW_ALBUM_SIDEPANE)));
		} else {
			for (MetaItemField metaItemField : metaItemFields) {
				if (metaItemField.getType().equals(FieldType.Text)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), Translator.get(DictKeys.BROWSER_THIS_IS_A_SAMPLE_TEXT, metaItemField.getName()), false));
				} else if (metaItemField.getType().equals(FieldType.Date)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), new java.sql.Date(System.currentTimeMillis()), false));
				} else if (metaItemField.getType().equals(FieldType.Integer)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 10 + (int)(Math.random() * ((90) + 1)), false));
				} else if (metaItemField.getType().equals(FieldType.Number)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 
							new BigDecimal(String.valueOf(10 + (90) * new Random().nextDouble())).setScale(2, BigDecimal.ROUND_HALF_UP), false));
				} else if (metaItemField.getType().equals(FieldType.Option)) {
					int option = (int)(Math.random() * ((2) + 1));
					
					if (option == 0) {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.NO, false));
					} else if (option != 1) {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.YES, false));
					} else {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.UNKNOWN, false));
					}
					
				} else if (metaItemField.getType().equals(FieldType.StarRating)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 0 + (int)(Math.random() * ((5) + 1)), false));
				} else if (metaItemField.getType().equals(FieldType.Time)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), System.currentTimeMillis(), false));
				} else if (metaItemField.getType().equals(FieldType.URL)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), "www.sammelbox.org", false));
				}
			}
		}
		
		try {
			AlbumItem albumItem = new AlbumItem("DummyItem", itemFields);
			albumItem.setPictures(pictures);
			
			return albumItem;
		} catch (DatabaseWrapperOperationException ex) {
			LOGGER.error("An error occured while creating a sample album item \n " + ExceptionHelper.toString(ex));
		}
		
		return null;
	}
}
