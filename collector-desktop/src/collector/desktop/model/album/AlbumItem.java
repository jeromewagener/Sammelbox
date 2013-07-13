package collector.desktop.model.album;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.model.database.DatabaseFacade;
import collector.desktop.model.database.DatabaseStringUtilities;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;

public class AlbumItem {
	public static Long ITEM_ID_UNDEFINED = Long.MAX_VALUE;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AlbumItem.class);
	
	protected long itemId = ITEM_ID_UNDEFINED;
	protected String albumName = "";
	protected List<ItemField> fields;
	protected List<AlbumItemPicture> albumItemPictures;
	protected UUID contentVersion;
	
	/**
	 * Constructor
	 * @param albumName The name of the album this item belongs to.
	 */
	public AlbumItem(String albumName) {
		this.albumName = albumName;
		fields = new LinkedList<ItemField>();
	}

	/**
	 * Constructor
	 * @param albumName The name of the album this item belongs to
	 * @param itemFields The item fields with which this album item should be initialized
	 * @throws DatabaseWrapperOperationException 
	 */
	public AlbumItem(String albumName, List<ItemField> itemFields) throws DatabaseWrapperOperationException {
		this.albumName = albumName;
		fields = itemFields;
		
		for (ItemField itemField : itemFields) {
			if (itemField.getName().equals(DatabaseFacade.ID_COLUMN_NAME)) {
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
	
	public long getItemID() {
		return getField(DatabaseFacade.ID_COLUMN_NAME).getValue();
	}
	
	/**
	 * Retrieves a field contained within this item. 
	 * @param fieldName The name of the field to be retrieved. When retrieving items from the database field names are unique 
	 * if no error is present.
	 * @return The itemField with the specified name or null if not found.
	 */
	public ItemField getField(String fieldName) {
		for (ItemField itemField : fields) {
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
		for (ItemField itemField : fields) {
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
		for (ItemField itemField : fields) {
			if (itemField.getName().equals(fieldName)) {
				itemField.setValue(value);
			} else if (itemField.getName().equals(DatabaseFacade.ID_COLUMN_NAME)) {
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
		return fields.get(fieldIndex);
	}
	
	/**
	 * Gets all the fields as list.
	 * @return A Java List interface of ItemFields providing access to all fields.
	 */
	public List<ItemField> getFields() {
		return fields;
	}

	/**
	 * Sets all the fields by specifying a list of fields. The order of the list specifies the later indices if inserted into the db.
	 * Ususally used for bulk initialization of the item.
	 * @param fields A Java List interface of ItemFields specifying the fields of this item. 
	 */
	public void setFields(List<ItemField> fields) {
		this.fields = fields;
		
		for (ItemField itemField : fields) {
			if (itemField.getName().equals(DatabaseFacade.ID_COLUMN_NAME)) {
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
		for (ItemField itemField : fields) {
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
		fields.add(new ItemField(fieldName, type, value, quickSearchable));
		
		if (fieldName.equals(DatabaseFacade.ID_COLUMN_NAME)) {
			itemId = (Long) value;
		}
	}
	
	/**
	 * Adds a field without the need to provide a list of all fields and manipulate them outside of the item class. The field 
	 * won't be availble for the quicksearch feature. A convenience method.
	 * @param fieldName The field name under which the field will be stored. Should be unique among all fields.
	 * @param type The FieldType of the field. IDs and Picture types should be used with care, due to internal use and special format.
	 * @param value The Java object representing the value of the field. Must be compliant with the specified type.
	 */
	public void addField(String fieldName,  FieldType type, Object value) {
		fields.add(new ItemField(fieldName, type, value));
	
		if (fieldName.equals(DatabaseFacade.ID_COLUMN_NAME)) {
			itemId = (Long) value;
		}
	}
	
	/**
	 * Removes a field from the internal list.
	 * @param fieldIndex The index of the field to be removed. The index represents the order by which the fields are organized. 
	 * The first index starts at 0, the next is 1 etc..
	 */
	public void removeField(int fieldIndex) {
		fields.remove(fieldIndex);
	}
	
	/**
	 * Removes an ItemField from the internal fieldList by matching it against the provided metaItem. 
	 * More specifically tests for equality between name and type.
	 * @param metaItemField The metaItemField to test against.
	 */
	public void removeField(MetaItemField metaItemField) {
		ItemField toRemove = null;
		for (ItemField itemField : fields) {
			 MetaItemField tempMetaItemField = new MetaItemField(itemField.getName(), itemField.getType(), itemField.isQuickSearchable());	
			if (tempMetaItemField.equals(metaItemField)) {
				toRemove = itemField;
			}
		}
		if (toRemove != null) {
			fields.remove(toRemove);
		}
	}
	
	/**
	 * Renames a field. Currently type changes are not permitted and wioll be ignored.
	 * @param oldMetaItemField The metadata of the field before the rename. 
	 * @param newMetaItemField The metadata of the field after the rename.
	 */
	public void renameField(MetaItemField oldMetaItemField, MetaItemField newMetaItemField) {
		for (ItemField itemField : fields) {
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
		for (ItemField itemField : fields) {
			MetaItemField tempMetaItemField = new MetaItemField(itemField.getName(), itemField.getType());	
			if (tempMetaItemField.equals(metaItemField)) {
				toMove = itemField;
			}
		}
		
		if (moveAfterField == null){
			moveAfterField = fields.get(0);
		}
		
		int moveAfterIndex = fields.indexOf(moveAfterField);
		if (toMove != null && moveAfterIndex != -1) {
			fields.remove(toMove);
			fields.add(moveAfterIndex<=fields.size() ? moveAfterIndex : fields.size()-1, toMove);
		}
	}
	
	/**
	 * Removes a field.
	 * @param itemField The field specifying the field to be deleted. A equal test will be performed to locate the element if present. 
	 */
	public void removeField(ItemField itemField) {
		fields.remove(itemField);
	}
	
	/**
	 * Checks if all fields are valid according to field.isValid()
	 * @return True if all fields are valid, false otherwise.
	 */
	public boolean isValid() {
		for (ItemField field : fields) {
			if (!field.isValid()) {
				LOGGER.error("{}  is not valid!", field);
				return false;
			}
		}
		return true;
	}

	/**
	 * Getter for the album name formatted for low level db interaction.
	 * @return The string representing the properly formatted album name.
	 */
	public String getDBAlbumName() {
		return DatabaseStringUtilities.encloseNameWithQuotes(albumName);
	}
	
	/**
	 * Getter for the album name formatted for low level db interaction.
	 * @param FieldName The field name.
	 * @return The string representing the properly formatted field name.
	 */
	public static String getDBFieldName(String FieldName) {
		return DatabaseStringUtilities.encloseNameWithQuotes(FieldName);
	}

	public UUID getContentVersion() {
		return contentVersion;
	}

	public void setContentVersion(UUID contentVersion) {
		this.contentVersion = contentVersion;
	}
	
	/** Loads the pictures associated with this album item from the database.*/
	public void loadPicturesFromDatabase() {
		try {
			setPictures(DatabaseFacade.getAlbumItemPictures(albumName, itemId));
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("Couldn't load album item pictures for album " + albumName + " with id " + itemId + "\n" + 
							" Stacktrace: " +  e.getMessage());
		}
	}
	
	public void setPictures(List<AlbumItemPicture> pictures) {
		this.albumItemPictures = pictures;
	}
	
	/** Returns the list of pictures associated with the album item
	 * @return the list of pictures associated with the album item, or null if pictures are not supported by the album
	 * */
	public List<AlbumItemPicture> getPictures() {
		if (albumItemPictures == null) {
			loadPicturesFromDatabase();
		}
		
		return albumItemPictures;
	}
	
	/** Returns the first picture associated with the album item 
	 * @return the first picture associated with the album item, or null if pictures are not supported by the album
	 * */
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
}
