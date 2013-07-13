package collector.desktop.model.database;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.album.AlbumItemPicture;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;

public class DatabaseFacade {
	/** The default name for ID columns */
	public static final String ID_COLUMN_NAME = "id";
	
	/**
	 * Creates a new album according to the specified properties.
	 * @param albumName The name of the album to be created.
	 * @param fields The metadata fields describing the fields of the new album. Pass an empty list as argument
	 * when creating an album with no fields.
	 * @param hasAlbumPictures When set to true creates a single picture field in the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void createNewAlbum(String albumName, List<MetaItemField> fields, boolean hasAlbumPictures) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.createNewAlbum(albumName, fields, hasAlbumPictures);
	}
	
	/**
	 * Permanently renames an album in the specified database
	 * @param oldAlbumName The old name of the album to be renamed
	 * @param newAlbumName The new name of the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void renameAlbum(String oldAlbumName, String newAlbumName) throws DatabaseWrapperOperationException {	
		DatabaseWrapperImpl.renameAlbum(oldAlbumName, newAlbumName);
	}
	
	/**
	 * Permanently removes a field from an album. Removing fields of type ID is not allowed.
	 * @param albumName The name of the album to be removed.
	 * @param metaItemField A description, name, type and quicksearch flag of the original metaItemField. 
	 * The object does not need to be a reference to the original metaItemfield of the album but in order to delete the item field
	 * ALL values of the meta item field have to be set correctly including the quicksearch flag.
	 * To remove pictures please use {@link removePictureField}
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void removeAlbumItemField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.removeAlbumItemField(albumName, metaItemField);
	}
	
	/**
	 * Renames the field specified in oldMetaItemField to the name in newMetaItemField. No type change nor renaming fields of type ID is allowed.
	 * @param albumName The name of the album to which the item belongs.
	 * @param oldMetaItemField A description of the original metaItemField, no need for direct reference.
	 * A description, name, type and quicksearch flag of the original metaItemField. In order to rename the item field
	 * ALL values of the meta item field have to be set correctly including the quicksearch flag.
	 * @param newMetaItemField A description of the new metaItemField, no need for direct reference.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void renameAlbumItemField(String albumName, MetaItemField oldMetaItemField, MetaItemField newMetaItemField) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.renameAlbumItemField(albumName, oldMetaItemField, newMetaItemField);
	}
	
	/**
	 * Moves the specified field to a the position directly after the column with the name moveAfterColumnName.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The metadata to identify the field (column) to be moved.
	 * @param preceedingField The field (column) which is preceeding the field after the reordering. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void reorderAlbumItemField(String albumName, MetaItemField metaItemField, MetaItemField preceedingField) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.reorderAlbumItemField(albumName, metaItemField, preceedingField);
	}
	
	/**
	 * Sets the ability of albumField to the value found in the metaItemField describing that field.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The field (column) to be set quicksearchable.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void setQuickSearchable(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.setQuickSearchable(albumName, metaItemField);
	}
	
	// TODO comment
	public static void removeAlbumAndAlbumPictures(String albumName) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.removeAlbumAndAlbumPictures(albumName);
	}
	
	/**
	 * Permanently removes an album along with its typeInfo metadata.
	 * @param albumName The name of the album which is to be removed.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void removeAlbum(String albumName) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.removeAlbum(albumName);
	}
	
	// TODO comment
	public static void removeAlbumPictures(String albumName) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.removeAlbumPictures(albumName);
	}
	
	/**
	 * Appends new field to the end of the album. Fields with the type FieldType.ID or FieldType.Picture fail the whole operation, no fields will be added then.
	 * @param albumName The name of the album to be modified.
	 * @param metaItemField The metaItemField to be appended to the album. Must not be null for successful insertion
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void appendNewAlbumField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.appendNewAlbumField(albumName, metaItemField);
	}
	
	/**
	 * Enables or disables the picture functionality for a given album
	 * @param albumName The name of the album which is concerned
	 * @param albumPicturesEnabled true if pictures are enabled, false otherwise
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void setAlbumPictureFunctionality(String albumName, boolean albumPicturesEnabled) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.setAlbumPictureFunctionality(albumName, albumPicturesEnabled);
	}
	
	/**
	 * Indicates whether the album contains a picture field.
	 * @param albumName The name of the album to be queried.
	 * @return True if the album contains a picture field, false otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean albumHasPictureField(String albumName) throws DatabaseWrapperOperationException {	
		return DatabaseWrapperImpl.albumHasPictureField(albumName);
	}
	
	/**
	 * Adds the specified item to an existing album. Automatically sets a new contentVersion for the item if updateContentVersion flag is set.
	 * Automatically links album item pictures to the album item by setting the foreign key
	 * @param item The album item to be added. 
	 * @param updateContentVersion True if the content version should be updated, this is the regular case for any content change. 
	 * False in case the the last contentVersion should be copied over, restore or alter table options make use of this. If the old content version
	 * should be copied over make sure the albumItem contains a content version!
	 * @return The ID of the newly added item.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static long addNewAlbumItem(AlbumItem item, boolean updateContentVersion) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.addNewAlbumItem(item, updateContentVersion);
	}
	
	/**
	 * Updates all the fields of the specified item in the database using the values provided through item.
	 * @param albumItem The item to be updated.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void updateAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.updateAlbumItem(albumItem);
	}
	
	/**
	 * Permanently deletes the albumItem with the specified id from the database.
	 * @param albumName The name of the album to which the item belongs.
	 * @param albumItemId The id of the item to be deleted.
	 * @return  True if the operation was successful. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void deleteAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		DatabaseWrapperImpl.deleteAlbumItem(albumItem);
	}
	
	// TODO comment
	public static long getNumberOfItemsInAlbum(String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getNumberOfItemsInAlbum(albumName);
	}
	
	/**
	 * Executes an SQL query against the database.
	 * @param sqlStatement The SQL statement to be executed. Must be proper SQL compliant to the database.
	 * @param albumName The name of the album to which the query refers to.
	 * @return The albumItemResultSet which represent the results of the query. Null if the query fails at any point.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement, String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.executeSQLQuery(sqlStatement, albumName);
	}
	
	/**
	 * Simply executes the provided sql query via the connection against a database and returns the results.
	 * @param sqlStatement An sql query, typically a SELECT statement like SELECT * FROM albumName.
	 * @return A resultSet containing the desired entries. Null if the query failed. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.executeSQLQuery(sqlStatement);
	}
	
	/**
	 * Performs a quicksearch. A quicksearch is a search limited to the marked fields. Every item return contains at least a field
	 * whose value partially matches the any query term.
	 * @param albumName The name of the album to which the query refers to.
	 * @param quickSearchTerms A list of terms to be matched against the marked fields. If null, a select * is performed.
	 * @return A valid albumItemResultSet for the provided quicksearch terms or a select * 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeQuickSearch(String albumName, List<String> quickSearchTerms) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.executeQuickSearch(albumName, quickSearchTerms);
	}
	
	/**
	 * Lists all albums currently stored in the database.
	 * @return A list of album names. May be empty if no albums were created yet. Null in case of an error.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<String> listAllAlbums() throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.listAllAlbums();
	}
	
	/**
	 * Lists all the columns of a table which are indexed. Indexed columns are also taken into account for 
	 * the quick-search feature.
	 * @param tableName The name of the table to which the columns belong. Table name must NOT be escaped! 
	 * @return A list of indexed, meaning also quickSearchable, columns. List may be empty if none were indexed. 
	 * Null if an error occurred. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<String> getIndexedColumnNames(String tableName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getIndexedColumnNames(tableName);
	}
	
	/**
	 * Retrieves the name of the index. In case there are multiple indices only the first one is looked up. 
	 * Multiple indices may indicate database inconsistency.
	 * @param tableName The name of the table to which the index belongs.
	 * @return The name of the index table if it exists, null otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static String getTableIndexName(String tableName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getTableIndexName(tableName);
	}
	
	/**
	 * Retrieves a list of MetaItemFields, excluding those that are for internal use only. Meta item fields describe the items of the album
	 * @param albumName The name of the album of which to retrieve the information.
	 * @return The list of MetaItemFields. 
	 */
	public static List<MetaItemField> getAlbumItemFieldNamesAndTypes(String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getAlbumItemFieldNamesAndTypes(albumName);
	}
	
	/**
	 * Fetches a map of metaItemFields keyed by their field (column) index. 
	 * @param albumName The name of the album to which this map belongs.
	 * @return True if the query was successful. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Map<Integer, MetaItemField> getAlbumItemMetaMap(String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getAlbumItemMetaMap(albumName);
	}
	
	/**
	 * Fetches the value of an item field.
	 * @param results The result set pointing to the field whose value is to be fetched.
	 * @param columnIndex The index of the field (column) within the item (entry).
	 * @param type The type of the field.
	 * @param albumName The name of the album this value of the item belongs to.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Object fetchFieldItemValue(ResultSet results, int columnIndex, FieldType type, String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.fetchFieldItemValue(results, columnIndex, type, albumName);
	}
	
	/**
	 * Queries whether the field is available for the quicksearch feature.
	 * @param albumName The name of the album to which the field belongs to.
	 * @param fieldName The name of the field to be queried.
	 * @return True if the the specified field is available for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumFieldQuicksearchable(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.isAlbumFieldQuicksearchable(albumName, fieldName);
	}
	
	/**
	 * Queries whether the specified album contains at least one quicksearchable field.
	 * @param albumName The name of the album to be queried.
	 * @return True if the the specified album contains at least a single field enabled for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumQuicksearchable(String albumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.isAlbumQuicksearchable(albumName);
	}
	
	/**
	 * Tests if the album name is not already in use by another album.
	 * @param requestedAlbumName The proposed album name to be tested of availability.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean albumNameIsAvailable(String requestedAlbumName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.albumNameIsAvailable(requestedAlbumName);
	}
	
	/**
	 * Tests if the proposed item field name is not already in use by another field of the same album.
	 * @param albumName The album name that contains the fields that the name is checked against.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean itemFieldNameIsAvailable(String albumName, String requestedFieldName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.itemFieldNameIsAvailable(albumName, requestedFieldName);
	}
	
	/**
	 * Fetches an album item by its id and album name.
	 * @param albumName The name of the album to which this item belongs to.
	 * @param albumItemId The unique id of the item within the album.
	 * @return The requested albumItem. Null if no item with the specified id was found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItem fetchAlbumItem(String albumName, long albumItemId) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.fetchAlbumItem(albumName, albumItemId);
	}
	
	/**
	 * Retrieves a list of albumItems from the database.
	 * @param queryString The proper SQL query string compliant with the database. 
	 * @return The list albumItems making up the results of the query. An empty list if no matching album item were found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<AlbumItem> fetchAlbumItemsFromDatabase(String queryString) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.fetchAlbumItemsFromDatabase(queryString);
	}
	
	// TODO comment
	public static boolean isDateField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.isDateField(albumName, fieldName);
	}
	
	// TODO comment
	public static boolean isOptionField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.isOptionField(albumName, fieldName);
	}
	
	// TODO comment
	public static void createPictureTable(String albumName) throws DatabaseWrapperOperationException {	
		DatabaseWrapperImpl.createPictureTable(albumName);
	}
	
	// TODO comment
	public static List<AlbumItemPicture> getAlbumItemPictures(String albumName, long albumItemID) throws DatabaseWrapperOperationException {
		return DatabaseWrapperImpl.getAlbumItemPictures(albumName, albumItemID);
	}
	
	/** Removes all picture records from the picture table for the given album item
	 * ATTENTION: this method does no delete the physical files!
	 * @param albumItem the album item for which all picture records should be deleted */
	public static void removeAllPicturesForAlbumItemFromPictureTable(AlbumItem albumItem) throws DatabaseWrapperOperationException {	
		DatabaseWrapperImpl.removeAllPicturesForAlbumItemFromPictureTable(albumItem);
	}
}
