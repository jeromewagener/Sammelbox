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

package org.sammelbox.model.album;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.sammelbox.controller.filters.ItemFieldFilter;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.operations.DatabaseConstants;
import org.sammelbox.model.database.operations.DatabaseOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlbumItem {
	/** All IDs smaller or equal to ITEM_ID_UNDEFINED are considered undefined (and can be used as temporary IDs for new items). */
	public static final Long ITEM_ID_UNDEFINED = -1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumItem.class);
	
	private long itemId = ITEM_ID_UNDEFINED;
	private String albumName = "";
	private List<ItemField> itemFields;
	private List<AlbumItemPicture> albumItemPictures;
	private UUID contentVersion;
	
	/**
	 * Constructor
	 * @param albumName The name of the album this item belongs to.
	 */
	public AlbumItem(String albumName) {
		this.albumName = albumName;
		itemFields = new LinkedList<ItemField>();
	}

	/**
	 * Constructor
	 * @param albumName The name of the album this item belongs to
	 * @param itemFields The item fields with which this album item should be initialized
	 * @throws DatabaseWrapperOperationException 
	 */
	public AlbumItem(String albumName, List<ItemField> itemFields) {
		this.albumName = albumName;
		this.itemFields = itemFields;
		
		for (ItemField itemField : itemFields) {
			if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
				itemId = (Long) itemField.getValue();
				break;
			}
		}
	}
	
	/**
	 * Getter for the AlbumName.
	 * @return String representing the name of the album this item belongs to.
	 */
	public String getAlbumName() {
		return albumName;
	}
	
	/**
	 * Setter for the album name this item belongs to.
	 * @param albumName The name this item belongs to.
	 */
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	
	public long getItemId() {
		ItemField albumItemIdField = getField(DatabaseConstants.ID_COLUMN_NAME);
		if (albumItemIdField == null) {
			return AlbumItem.ITEM_ID_UNDEFINED;
		}
		
		return albumItemIdField.getValue();
	}
	
	public void setItemId(Long itemId) {
		ItemField albumItemIdField = getField(DatabaseConstants.ID_COLUMN_NAME);
		if (albumItemIdField == null) {
			addField(DatabaseConstants.ID_COLUMN_NAME, FieldType.ID, itemId);
		} else {
			albumItemIdField.setValue(itemId);
		}
		
		this.itemId = itemId;
	}
	
	/**
	 * Retrieves a field contained within this item. 
	 * @param fieldName The name of the field to be retrieved. When retrieving items from the database field names are unique 
	 * if no error is present.
	 * @return The itemField with the specified name or null if not found.
	 */
	public ItemField getField(String fieldName) {
		for (ItemField itemField : itemFields) {
			if (itemField.getName().equals(fieldName)) {
				return itemField;
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieves the first field of the specified type which is contained within this item
	 * @param fieldType the type of the field to be retrieved.
	 * @return The itemField with the specified type or null if not found
	 */
	public ItemField getField(FieldType fieldType) {
		for (ItemField itemField : itemFields) {
			if (itemField.getType().equals(fieldType)) {
				return itemField;
			}
		}
		
		return null;
	}
	
	/**
	 * Sets a field to the specified value.
	 * @param fieldName The name of the field to be set. When retrieving items from the database field names are unique if no error is present.
	 * @param value A java object representing the value of this field
	 */
	public void setFieldValue(String fieldName, Object value) {
		for (ItemField itemField : itemFields) {
			if (itemField.getName().equals(fieldName)) {
				itemField.setValue(value);
			} else if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
				itemId = (Long) itemField.getValue();
			}
		}
	}
	
	/**
	 * Retrieves the field by its index. 
	 * @param fieldIndex The index represents the order by which the fields are organized. The first index starts at 0, the next is 1 etc..
	 * @return The requested field.
	 */
	public ItemField getField(int fieldIndex) {
		return itemFields.get(fieldIndex);
	}
	
	/**
	 * Gets all the fields as list.
	 * @return A Java List interface of ItemFields providing access to all fields.
	 */
	public List<ItemField> getFields() {
		return itemFields;
	}

	/**
	 * Sets all the fields by specifying a list of fields. The order of the list specifies the later indices if inserted into the db.
	 * Ususally used for bulk initialization of the item.
	 * @param fields A Java List interface of ItemFields specifying the fields of this item. 
	 */
	public void setFields(List<ItemField> fields) {
		this.itemFields = fields;
		
		for (ItemField itemField : fields) {
			if (itemField.getName().equals(DatabaseConstants.ID_COLUMN_NAME)) {
				itemId = (Long) itemField.getValue();
				break;
			}
		}
	}
	
	/**
	 * Checks if all fields in this album item are valid.
	 * @return True if all fields are valid according to ItemField.isValid(), false otherwise.
	 */
	public boolean areFieldsValid() {
		for (ItemField itemField : itemFields) {
			if (!itemField.isValid()) {
				return false;
			}
		}
		return  true;
	}

	/**
	 * Adds a field without the need to provide a list of all fields and manipulate them outside of the item class.
	 * @param fieldName The field name under which the field will be stored. Should be unique among all fields.
	 * @param type The FieldType of the field. IDs and Picture types should be used with care, due to internal use and special format.
	 * @param value The Java object representing the value of the field. Must be compliant with the specified type.
	 * @param quickSearchable Boolean value indicating whether this item will be available for the quicksearch feature. Usually is of 
	 * greater importance.
	 */
	public void addField(String fieldName,  FieldType type, Object value, boolean quickSearchable) {
		itemFields.add(new ItemField(fieldName, type, value, quickSearchable));
		
		if (fieldName.equals(DatabaseConstants.ID_COLUMN_NAME)) {
			itemId = (Long) value;
		}
	}
	
	/**
	 * Adds a field without the need to provide a list of all fields and manipulate them outside of the item class. The field 
	 * won't be available for the quicksearch feature. A convenience method.
	 * @param fieldName The field name under which the field will be stored. Should be unique among all fields.
	 * @param type The FieldType of the field. IDs and Picture types should be used with care, due to internal use and special format.
	 * @param value The Java object representing the value of the field. Must be compliant with the specified type.
	 */
	public void addField(String fieldName,  FieldType type, Object value) {
		itemFields.add(new ItemField(fieldName, type, value));
	
		if (fieldName.equals(DatabaseConstants.ID_COLUMN_NAME)) {
			itemId = (Long) value;
		}
	}
	
	/**
	 * Removes a field from the internal list.
	 * @param fieldIndex The index of the field to be removed. The index represents the order by which the fields are organized. 
	 * The first index starts at 0, the next is 1 etc..
	 */
	public void removeField(int fieldIndex) {
		itemFields.remove(fieldIndex);
	}
	
	/**
	 * Removes an ItemField from the internal fieldList by matching it against the provided metaItem. 
	 * More specifically tests for equality between name and type.
	 * @param metaItemField The metaItemField to test against.
	 */
	public void removeField(MetaItemField metaItemField) {
		ItemField toRemove = null;
		for (ItemField itemField : itemFields) {
			 MetaItemField tempMetaItemField = new MetaItemField(itemField.getName(), itemField.getType(), itemField.isQuickSearchable());	
			if (tempMetaItemField.equals(metaItemField)) {
				toRemove = itemField;
			}
		}
		if (toRemove != null) {
			itemFields.remove(toRemove);
		}
	}
	
	/**
	 * Renames a field. Currently type changes are not permitted and will be ignored.
	 * @param oldMetaItemField The meta data of the field before the rename. 
	 * @param newMetaItemField The meta data of the field after the rename.
	 */
	public void renameField(MetaItemField oldMetaItemField, MetaItemField newMetaItemField) {
		for (ItemField itemField : itemFields) {
			 MetaItemField tempMetaItemField = new MetaItemField(itemField.getName(), itemField.getType(), itemField.isQuickSearchable());	
			if (tempMetaItemField.equals(oldMetaItemField)) {
				itemField.setName(newMetaItemField.getName());
			}
		}
	}
	
	/**
	 * Moves the metaItemField after the specified moveAfterField. In case the latter is null, move to beginning of list.
	 * @param metaItemField
	 * @param moveAfterField
	 */
	public void reorderField(MetaItemField metaItemField, MetaItemField moveAfterField) {
		ItemField toMove = null;
		for (ItemField itemField : itemFields) {
			MetaItemField tempMetaItemField = new MetaItemField(itemField.getName(), itemField.getType());	
			if (tempMetaItemField.equals(metaItemField)) {
				toMove = itemField;
			}
		}
		
		int moveAfterIndex = itemFields.indexOf(moveAfterField != null ? moveAfterField : itemFields.get(0));
		
		if (toMove != null && moveAfterIndex != -1) {
			itemFields.remove(toMove);
			itemFields.add(moveAfterIndex<=itemFields.size() ? moveAfterIndex : itemFields.size()-1, toMove);
		}
	}
	
	/**
	 * Removes a field.
	 * @param itemField The field specifying the field to be deleted. A equal test will be performed to locate the element if present. 
	 */
	public void removeField(ItemField itemField) {
		itemFields.remove(itemField);
	}
	
	/**
	 * Checks if all fields are valid according to field.isValid()
	 * @return true if all fields are valid, false otherwise.
	 */
	public boolean isValid() {
		for (ItemField field : itemFields) {
			if (!field.isValid()) {
				LOGGER.error("{}  is not valid!", field);
				return false;
			}
		}
		return true;
	}

	/**
	 * Getter for the album name formatted for low level database interaction.
	 * @return The string representing the properly formatted album name.
	 */
	public String getDatabaseAlbumName() {
		return DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName));
	}
	
	/**
	 * Getter for the album name formatted for low level database interaction.
	 * @param fieldName The field name.
	 * @return The string representing the properly formatted field name.
	 */
	public static String getDatabaseFieldName(String fieldName) {
		return DatabaseStringUtilities.encloseNameWithQuotes(fieldName);
	}

	public UUID getContentVersion() {
		return contentVersion;
	}

	public void setContentVersion(UUID contentVersion) {
		this.contentVersion = contentVersion;
	}
	
	/** Loads the pictures associated with this album item from the database. */
	public void loadPicturesFromDatabase() {
		try {
			setPictures(DatabaseOperations.getAlbumItemPictures(albumName, itemId));
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("Couldn't load album item pictures for album " + albumName + " with id " + itemId + "\n" + 
							" Stacktrace: " +  e.getMessage());
		}
	}
	
	public void setPictures(List<AlbumItemPicture> pictures) {
		this.albumItemPictures = pictures;
	}
	
	/** Returns the list of pictures associated with the album item
	 * @return the list of pictures associated with the album item, or null if pictures are not supported by the album */
	public List<AlbumItemPicture> getPictures() {
		if (albumItemPictures == null) {
			loadPicturesFromDatabase();
		}
		
		return albumItemPictures;
	}
	
	/** Returns the first picture associated with the album item 
	 * @return the first picture associated with the album item, or null if 
	 * A) no picture is associated with the album item, B) pictures are generally not supported by the album */
	public AlbumItemPicture getFirstPicture() {
		if (albumItemPictures == null) {
			loadPicturesFromDatabase();
		}
		
		if (!albumItemPictures.isEmpty()) {
			return albumItemPictures.get(0);
		} else {
			return null;
		}
	}
	
	public void initializeWithDefaultValuesUsingMetaItems(List<MetaItemField> metaItemFields) {
		itemFields.clear();
		itemFields.add(new ItemField(DatabaseConstants.ID_COLUMN_NAME, FieldType.ID, ITEM_ID_UNDEFINED));
		
		for (MetaItemField metaItemField : metaItemFields) {
			if (metaItemField.getType().equals(FieldType.DATE)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), null));
			} else if (metaItemField.getType().equals(FieldType.DECIMAL)) {
				itemFields.add(new ItemField(metaItemField.getName(), metaItemField.getType(), 0.0));
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
	}
	
	public AlbumItem clone() {
		List<ItemField> clonedItemFields = new ArrayList<ItemField>();		
		
		for (ItemField itemField : ItemFieldFilter.getValidItemFields(this.itemFields)) {
			clonedItemFields.add(new ItemField(itemField.getName(), itemField.getType(), itemField.getValue()));
		}
		
		return new AlbumItem(albumName, clonedItemFields);
	}
}
