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

package org.sammelbox.model.database.operations;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;

public class DatabaseOperations {	
	/**
	 * Creates a new album according to the specified properties.
	 * @param albumName The name of the album to be created.
	 * @param fields The metadata fields describing the fields of the new album. Pass an empty list as argument
	 * when creating an album with no fields.
	 * @param hasAlbumPictures When set to true creates a single picture field in the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void createNewAlbum(String albumName, List<MetaItemField> fields, boolean hasAlbumPictures) throws DatabaseWrapperOperationException {
		CreateOperations.createNewAlbum(albumName, fields, hasAlbumPictures);
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
	public static long addAlbumItem(AlbumItem item, boolean updateContentVersion) throws DatabaseWrapperOperationException {
		return CreateOperations.addAlbumItem(item, updateContentVersion);
	}
	
	/**
	 * Permanently renames an album in the specified database
	 * @param oldAlbumName The old name of the album to be renamed
	 * @param newAlbumName The new name of the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void renameAlbum(String oldAlbumName, String newAlbumName) throws DatabaseWrapperOperationException {	
		UpdateOperations.renameAlbum(oldAlbumName, newAlbumName);
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
		UpdateOperations.renameAlbumItemField(albumName, oldMetaItemField, newMetaItemField);
	}
	
	/**
	 * Moves the specified field to a the position directly after the column with the name moveAfterColumnName.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The metadata to identify the field (column) to be moved.
	 * @param preceedingField The field (column) which is preceeding the field after the reordering. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void reorderAlbumItemField(String albumName, MetaItemField metaItemField, MetaItemField preceedingField) throws DatabaseWrapperOperationException {
		UpdateOperations.reorderAlbumItemField(albumName, metaItemField, preceedingField);
	}
	
	/**
	 * Sets the ability of albumField to the value found in the metaItemField describing that field.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The field (column) to be set quicksearchable.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void updateQuickSearchable(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		UpdateOperations.updateQuickSearchable(albumName, metaItemField);
	}
	
	/**
	 * Appends new field to the end of the album. Fields with the type FieldType.ID or FieldType.Picture fail the whole operation, no fields will be added then.
	 * @param albumName The name of the album to be modified.
	 * @param metaItemField The metaItemField to be appended to the album. Must not be null for successful insertion
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void appendNewAlbumField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		UpdateOperations.appendNewAlbumField(albumName, metaItemField);
	}
	
	/**
	 * Enables or disables the picture functionality for a given album
	 * @param albumName The name of the album which is concerned
	 * @param albumPicturesEnabled true if pictures are enabled, false otherwise
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void setAlbumPictureFunctionality(String albumName, boolean albumPicturesEnabled) throws DatabaseWrapperOperationException {
		UpdateOperations.setAlbumPictureFunctionality(albumName, albumPicturesEnabled);
	}
	
	/**
	 * Updates all the fields of the specified item in the database using the values provided through item.
	 * @param albumItem The item to be updated.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void updateAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		UpdateOperations.updateAlbumItem(albumItem);
	}
	
	/**
	 * Executes an SQL query against the database.
	 * @param sqlStatement The SQL statement to be executed. Must be proper SQL compliant to the database.
	 * @param albumName The name of the album to which the query refers to.
	 * @return The albumItemResultSet which represent the results of the query. Null if the query fails at any point.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement, String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.executeSQLQuery(sqlStatement, albumName);
	}
	
	/**
	 * Simply executes the provided sql query via the connection against a database and returns the results.
	 * @param sqlStatement An sql query, typically a SELECT statement like SELECT * FROM albumName.
	 * @return A resultSet containing the desired entries. Null if the query failed. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement) throws DatabaseWrapperOperationException {
		return QueryOperations.executeSQLQuery(sqlStatement);
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
		return QueryOperations.executeQuickSearch(albumName, quickSearchTerms);
	}
	
	/**
	 * Returns the matching album name for a given table name
	 * @param tableName the table name for which the album name is requested
	 * @return the album name
	 */
	public static String getAlbumName(String tableName) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumName(tableName);
	}
	
	/**
	 * Indicates whether the album contains pictures or not
	 * @param albumName The name of the album to be queried
	 * @return true if the album contains a picture field, false otherwise
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isPictureAlbum(String albumName) throws DatabaseWrapperOperationException {	
		return QueryOperations.isPictureAlbum(albumName);
	}
	
	/**
	 * Get the number of album items in the given album
	 * @param albumName the album name for which the items should be counted
	 * @return the number of items in the given album
	 * @throws DatabaseWrapperOperationException
	 */
	public static long getNumberOfItemsInAlbum(String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.getNumberOfItemsInAlbum(albumName);
	}
	
	/**
	 * Lists all albums currently stored in the database.
	 * @return A list of album names. May be empty if no albums were created yet. Null in case of an error.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<String> getListOfAllAlbums() throws DatabaseWrapperOperationException {
		return QueryOperations.getListOfAllAlbums();
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
		return QueryOperations.getIndexedColumnNames(tableName);
	}
	
	/**
	 * Retrieves the name of the index. In case there are multiple indices only the first one is looked up. 
	 * Multiple indices may indicate database inconsistency.
	 * @param tableName The name of the table to which the index belongs.
	 * @return The name of the index table if it exists, null otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static String getTableIndexName(String tableName) throws DatabaseWrapperOperationException {
		return QueryOperations.getTableIndexName(tableName);
	}
	
	/**
	 * Retrieves a list of MetaItemFields, excluding those that are for internal use only. Meta item fields describe the items of the album
	 * @param albumName The name of the album of which to retrieve the information.
	 * @return The list of MetaItemFields. 
	 */
	public static List<MetaItemField> getAlbumItemFieldNamesAndTypes(String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItemFieldNamesAndTypes(albumName);
	}
	
	/**
	 * Fetches a map of metaItemFields keyed by their field (column) index. 
	 * @param albumName The name of the album to which this map belongs.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Map<Integer, MetaItemField> getAlbumItemMetaMap(String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItemMetaMap(albumName);
	}
	
	/**
	 * Fetches a map of field types keyed by their field name
	 * @param albumName The name of the album to which this map belongs.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Map<String, FieldType> getAlbumItemFieldNameToTypeMap(String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItemFieldNameToTypeMap(albumName);
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
		return HelperOperations.fetchFieldItemValue(results, columnIndex, type, albumName);
	}
	
	/**
	 * Queries whether the field is available for the quicksearch feature.
	 * @param albumName The name of the album to which the field belongs to.
	 * @param fieldName The name of the field to be queried.
	 * @return True if the the specified field is available for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumFieldQuicksearchable(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return QueryOperations.isAlbumFieldQuicksearchable(albumName, fieldName);
	}
	
	/**
	 * Queries whether the specified album contains at least one quicksearchable field.
	 * @param albumName The name of the album to be queried.
	 * @return True if the the specified album contains at least a single field enabled for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumQuicksearchable(String albumName) throws DatabaseWrapperOperationException {
		return QueryOperations.isAlbumQuicksearchable(albumName);
	}
	
	/**
	 * Tests if the album name is not already in use by another album.
	 * @param requestedAlbumName The proposed album name to be tested of availability.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumNameAvailable(String requestedAlbumName) throws DatabaseWrapperOperationException {
		return QueryOperations.isAlbumNameAvailable(requestedAlbumName);
	}
	
	/**
	 * Tests if the proposed item field name is not already in use by another field of the same album.
	 * @param albumName The album name that contains the fields that the name is checked against.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isItemFieldNameAvailable(String albumName, String requestedFieldName) throws DatabaseWrapperOperationException {
		return QueryOperations.isItemFieldNameAvailable(albumName, requestedFieldName);
	}
	
	/**
	 * Fetches an album item by its id and album name.
	 * @param albumName The name of the album to which this item belongs to.
	 * @param albumItemId The unique id of the item within the album.
	 * @return The requested albumItem. Null if no item with the specified id was found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItem getAlbumItem(String albumName, long albumItemId) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItem(albumName, albumItemId);
	}
	
	/**
	 * Retrieves a list of albumItems from the database.
	 * @param queryString The proper SQL query string compliant with the database. 
	 * @return The list albumItems making up the results of the query. An empty list if no matching album item were found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<AlbumItem> getAlbumItems(String queryString) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItems(queryString);
	}
	
	/**
	 * Returns true if the given field in the given album is a date field, false otherwise
	 * @throws DatabaseWrapperOperationException
	 */
	public static boolean isDateField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return QueryOperations.isDateField(albumName, fieldName);
	}
	
	/**
	 * Returns true if the given field in the given album is a option field, false otherwise
	 * @throws DatabaseWrapperOperationException
	 */
	public static boolean isOptionField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return QueryOperations.isOptionField(albumName, fieldName);
	}
	
	/**
	 * Returns true if the given field in the given album is a star-rating field, false otherwise
	 * @throws DatabaseWrapperOperationException
	 */
	public static boolean isStarRatingField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		return QueryOperations.isStarRatingField(albumName, fieldName);
	}
	
	/**
	 * Returns the list of pictures attached to a specified album item
	 * @param albumName the album which contains the relevant album item
	 * @param albumItemID the id of the album item for which the pictures are requested
	 * @return a list of album item pictures
	 * @throws DatabaseWrapperOperationException
	 */
	public static List<AlbumItemPicture> getAlbumItemPictures(String albumName, long albumItemID) throws DatabaseWrapperOperationException {
		return QueryOperations.getAlbumItemPictures(albumName, albumItemID);
	}
	
	/**
	 * Creates the album master table if it does not already exist
	 * @throws DatabaseWrapperOperationException
	 */
	public static void createAlbumMasterTableIfItDoesNotExist() throws DatabaseWrapperOperationException {
		CreateOperations.createAlbumMasterTableIfItDoesNotExist();
	}
	
	/**
	 * Permanently deletes the albumItem with the specified id from the database
	 * @param albumName The name of the album to which the item belongs
	 * @param albumItemId The id of the item to be deleted
	 * @return true if the operation was successful, false otherwise
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void deleteAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		DeleteOperations.deleteAlbumItem(albumItem);
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
		DeleteOperations.removeAlbumItemField(albumName, metaItemField);
	}
	
	/**
	 * Remove the album and everything related
	 * @param albumName the album to be removed
	 * @throws DatabaseWrapperOperationException
	 */
	public static void removeAlbumAndAlbumPictures(String albumName) throws DatabaseWrapperOperationException {
		DeleteOperations.removeAlbumAndAlbumPictures(albumName);
	}
}
