package collector.desktop.database;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.album.MetaItemField;
import collector.desktop.album.OptionType;
import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;
import collector.desktop.internationalization.Translator;

public class AlbumItemStore {
	private static final int DEFAULT_STOP_INDEX_INCREASE_AMOUNT = 10;
	private static final int DEFAULT_STOP_INDEX = 40;
	
	private static List<AlbumItem> albumItems = new ArrayList<AlbumItem>();
	private static int stopIndex = DEFAULT_STOP_INDEX;
	private static int previousStopIndex = DEFAULT_STOP_INDEX;
	
	public static void reinitializeStore(AlbumItemResultSet albumItemResultSet) throws FailedDatabaseWrapperOperationException {
		albumItems.clear();
		stopIndex = DEFAULT_STOP_INDEX;
		previousStopIndex = DEFAULT_STOP_INDEX;
		
		while (albumItemResultSet.moveToNext()) {
			AlbumItem albumItem = new AlbumItem(albumItemResultSet.getAlbumName());
			List<ItemField> itemFields = new ArrayList<ItemField>();
			
			for (int i=1; i<=albumItemResultSet.getFieldCount(); i++) {
				itemFields.add(new ItemField(albumItemResultSet.getFieldName(i), 
					albumItemResultSet.getFieldType(i), albumItemResultSet.getFieldValue(i)));
			}
			
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
		List<URI> pictureUris = new ArrayList<URI>();
		
		pictureUris.add(new File(FileSystemAccessWrapper.PLACEHOLDERIMAGE).toURI());
		pictureUris.add(new File(FileSystemAccessWrapper.PLACEHOLDERIMAGE2).toURI());
		
		List<ItemField> itemFields = new ArrayList<ItemField>();
		itemFields.add(new ItemField(DatabaseWrapper.PICTURE_COLUMN_NAME, FieldType.Picture, pictureUris));
		itemFields.add(new ItemField(Translator.toBeTranslated("You have not added any fields yet!"), FieldType.Text, Translator.toBeTranslated("Please add fields using the \"Create new album sidepane\"")));		
		
		return new AlbumItem("DummyItem", itemFields);
	}
	
	public static AlbumItem getSampleAlbumItem(boolean containsPictures, List<MetaItemField> metaItemFields) {
		List<ItemField> itemFields = new ArrayList<ItemField>();
		
		if (containsPictures) {
			List<URI> pictureUris = new ArrayList<URI>();
			
			pictureUris.add(new File(FileSystemAccessWrapper.PLACEHOLDERIMAGE).toURI());
			pictureUris.add(new File(FileSystemAccessWrapper.PLACEHOLDERIMAGE2).toURI());
			
			itemFields.add(new ItemField(DatabaseWrapper.PICTURE_COLUMN_NAME, FieldType.Picture, pictureUris));
		}
		
		if (metaItemFields.isEmpty()) {
			itemFields.add(new ItemField(Translator.toBeTranslated("You have not added any fields yet!"), 
					FieldType.Text, Translator.toBeTranslated("Please add fields using the \"Create new album sidepane\"")));
		} else {
			for (MetaItemField metaItemField : metaItemFields) {
				if (metaItemField.getType().equals(FieldType.Text)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 
							Translator.toBeTranslated("This is a sample " + metaItemField.getName() + " text" ), false));
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
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.No, false));
					} else if (option != 1) {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.Yes, false));
					} else {
						itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), OptionType.UNKNOWN, false));
					}
					
				} else if (metaItemField.getType().equals(FieldType.StarRating)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 0 + (int)(Math.random() * ((5) + 1)), false));
				} else if (metaItemField.getType().equals(FieldType.Time)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), System.currentTimeMillis(), false));
				} else if (metaItemField.getType().equals(FieldType.URL)) {
					itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), "www.sammelbox.it", false));
				}
			}
		}
		
		return new AlbumItem("DummyItem", itemFields);
	}
}
