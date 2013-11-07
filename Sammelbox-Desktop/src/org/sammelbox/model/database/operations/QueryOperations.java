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

package org.sammelbox.model.database.operations;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.settings.SettingsManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.AlbumItemResultSet;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.OptionType;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.QueryBuilder;
import org.sammelbox.model.database.QueryComponent;
import org.sammelbox.model.database.QueryOperator;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryOperations {
	private static final String SQLITE_INDEX_COLUMN_NAME = "COLUMN_NAME";
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryOperations.class);
	
	private QueryOperations() {
		// use static methods
	}
	
	static AlbumItemResultSet executeSQLQuery(String sqlStatement, String albumName) throws DatabaseWrapperOperationException {	
		Map<Integer, MetaItemField> metaInfoMap = QueryOperations.getAlbumItemMetaMap(albumName);
		return new AlbumItemResultSet(ConnectionManager.getConnection(), sqlStatement, metaInfoMap);
	}
	
	static AlbumItemResultSet executeQuickSearchQuery(String sqlStatement, String albumName) throws DatabaseWrapperOperationException {	
		Map<Integer, MetaItemField> metaInfoMap = QueryOperations.getAlbumItemMetaMap(albumName);
		return new AlbumItemResultSet(ConnectionManager.getConnection(), albumName, sqlStatement, metaInfoMap);
	}

	static AlbumItemResultSet executeSQLQuery(String sqlStatement) throws DatabaseWrapperOperationException {
		AlbumItemResultSet albumItemRS = null;
		try {
			albumItemRS = new AlbumItemResultSet(ConnectionManager.getConnection(), sqlStatement);
			return albumItemRS;
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("The query: \"{}\" could not be executed and terminated with message: {}", sqlStatement ,  e.getMessage());
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}
	
	static AlbumItemResultSet executeQuickSearch(String albumName, List<String> quickSearchTerms) throws DatabaseWrapperOperationException {
		List<MetaItemField> albumFields = getAllAlbumItemMetaItemFields(albumName);
		String query = "";
		ArrayList<QueryComponent> queryFields = null;
		List<String> quicksearchFieldNames = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));

		// If no field is quicksearchable return select * from albumName or no terms have been entered
		if (quicksearchFieldNames == null || quicksearchFieldNames.isEmpty() || quickSearchTerms == null || quickSearchTerms.isEmpty() ) {
			query = QueryBuilder.createSelectStarQuery(albumName);
			return executeSQLQuery(query);
		}

		boolean first = true;
		for (String term : quickSearchTerms) {
			if (term.isEmpty()) {
				continue;
			}
			if (!first) {
				query += " UNION ";
			} else {
				first = false;
			}
			queryFields = new ArrayList<QueryComponent>();
			for (MetaItemField field : albumFields) {
				if (field.isQuickSearchable()) {
					if (field.getType().equals(FieldType.TEXT) || field.getType().equals(FieldType.OPTION) || 
							field.getType().equals(FieldType.URL)) {
						queryFields.add(QueryBuilder.getQueryComponent(field.getName(), QueryOperator.CONTAINS, term));
					}
					else if ((field.getType().equals(FieldType.INTEGER) || field.getType().equals(FieldType.STAR_RATING))
							&& (Pattern.compile("-?[0-9]+").matcher(term).matches())) {
						queryFields.add(QueryBuilder.getQueryComponent(
								field.getName(), QueryOperator.EQUALS, Integer.valueOf(term).toString()));
					}
					else if (field.getType().equals(FieldType.DECIMAL) && Pattern.compile("\\d+(.\\d+)*").matcher(term).matches()) {
						queryFields.add(QueryBuilder.getQueryComponent(
								field.getName(), QueryOperator.EQUALS, Double.valueOf(term).toString()));
					}
					else if (field.getType().equals(FieldType.DATE)) {
						try {
							
							long utcTimeForDateTerm = transformDateStringToUTCUnixTime(term);
							
							queryFields.add(QueryBuilder.getQueryComponent(
									field.getName(), QueryOperator.EQUALS, String.valueOf(utcTimeForDateTerm)));
						} catch (ParseException e) {
							continue;
						}
					}
				}
			}// end of for - fields
			if (queryFields != null && !queryFields.isEmpty()) {
				query += QueryBuilder.buildQuery(queryFields, false, albumName);
			}

		}// end of for - terms

		return executeQuickSearchQuery(query, albumName);
	}
	
	// TODO: Find a better spot for this method.
	public static long transformDateStringToUTCUnixTime(String dateString) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(SettingsManager.getSettings().getDateFormat());
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date parsedDate = sdf.parse(dateString);
		return parsedDate.getTime();
	}

	static long getNumberOfItemsInAlbum(String albumName) throws DatabaseWrapperOperationException {
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(QueryBuilder.createCountAsAliasStarWhere(albumName, "numberOfItems"));){			
			
			if (resultSet.next()) {
				return resultSet.getLong("numberOfItems");
			}
			LOGGER.error("The number of items could not be fetch for album {}", albumName);
			throw new DatabaseWrapperOperationException(
					DBErrorState.ERROR_CLEAN_STATE, "The number of items could not be fetched for album " + albumName);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
	}
	
	static List<String> getListOfAllAlbums() throws DatabaseWrapperOperationException {
		List<String> albumList = new ArrayList<String>();
		String queryAllAlbumsSQL = QueryBuilder.createSelectColumnQuery(
				DatabaseConstants.ALBUM_MASTER_TABLE_NAME, DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE);

		try (Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			 ResultSet rs = statement.executeQuery(queryAllAlbumsSQL);) {

			while(rs.next()) {
				albumList.add(rs.getString(1));
			}
			return albumList;
			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	static List<String> getIndexedColumnNames(String tableName) throws DatabaseWrapperOperationException {
		List<String> indexedColumns = new ArrayList<String>();
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata = ConnectionManager.getConnection().getMetaData();			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
				
		try (ResultSet indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true)) {	
			while (indexRS.next()) {
				if (indexRS.getString(SQLITE_INDEX_COLUMN_NAME) != null) {
					indexedColumns.add(indexRS.getString(SQLITE_INDEX_COLUMN_NAME));
				}
			}
			return indexedColumns;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	static String getTableIndexName(String tableName) throws DatabaseWrapperOperationException {		
		String indexName = null;
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata =  ConnectionManager.getConnection().getMetaData();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
		
		try (ResultSet indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true);) {			
			if (indexRS.next()) {
				indexName = indexRS.getString("INDEX_NAME");
			}
			return indexName;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	static List<MetaItemField> getAlbumItemFieldNamesAndTypes(String albumName) throws DatabaseWrapperOperationException {
		String tableName = DatabaseStringUtilities.generateTableName(albumName);
		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();
		
		// Is available means that it does not exist in the db, hence its fields cannot be retrieved
		if (QueryOperations.isAlbumNameAvailable(albumName)) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
		}

		List<String> quickSearchableColumnNames = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));
		List<String> internalColumnNames = Arrays.asList("id", DatabaseConstants.TYPE_INFO_COLUMN_NAME, DatabaseConstants.CONTENT_VERSION_COLUMN_NAME);
		
		try (
			Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(QueryBuilder.createSelectStarQuery(tableName));)
		{
			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// Each ItemField. Classic for loop since meta data only provides access via indices.
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {

				// Excludes all columns that are for internal use only.
				String columnName = metaData.getColumnName(columnIndex);
				if (!internalColumnNames.contains(columnName)) {
					FieldType type = HelperOperations.detectDataType(tableName, columnName);
					MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
					itemMetadata.add(metaItem);
				}
			}
			
			return itemMetadata;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	/**
	 *  Retrieves a list of all MetaItemFields, including those that are for internal use only. Meta item fields describe the items of the album.
	 * @param albumName The name of the album of which to retrieve the information.
	 * @return The list of MetaItemFields. Return an empty  list if a structural error exists in the database. Null when an internal SQL error occurred.
	 * @throws DatabaseWrapperOperationException 
	 */
	static List<MetaItemField> getAllAlbumItemMetaItemFields(String albumName) throws DatabaseWrapperOperationException{
		String tableName = DatabaseStringUtilities.generateTableName(albumName);
		
		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();
		List<String> quickSearchableColumnNames = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));
		try (
			Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(QueryBuilder.createSelectStarQuery(albumName));) {					

			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// Each ItemField
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				String columnName = metaData.getColumnName(columnIndex);
				FieldType type = HelperOperations.detectDataType(tableName, columnName);
				MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
				itemMetadata.add(metaItem);
			}
			return itemMetadata;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	static Map<Integer, MetaItemField> getAlbumItemMetaMap(String albumName) throws DatabaseWrapperOperationException {
		List<String> quickSearchableColumns = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));

		Map<Integer, MetaItemField> itemMetaData = new HashMap<Integer, MetaItemField>();
		
		try (Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			 ResultSet set = statement.executeQuery(QueryBuilder.createSelectStarQuery(albumName))) {			
			
			// Retrieve table metadata	
			ResultSetMetaData metaData = set.getMetaData();
			
			int columnCount = metaData.getColumnCount();
			// Each ItemField
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				String name = metaData.getColumnName(columnIndex);
				
				FieldType type = HelperOperations.detectDataType(DatabaseStringUtilities.generateTableName(albumName), name);	
				
				MetaItemField metaItemField = new MetaItemField(name, type, quickSearchableColumns.contains(name));
				itemMetaData.put(columnIndex, metaItemField);
			}
			return itemMetaData;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}
	}
	
	static List<AlbumItemPicture> getAlbumItemPictures(String albumName, long albumItemID) throws DatabaseWrapperOperationException {
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();
		
		if (isPictureAlbum(albumName)) {
			String picturesQuery = 
				   " SELECT " +
						DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ID_COLUMN_NAME) + ", " +
						DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", " +
						DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", " +
						DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE) +
				   " FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generatePictureTableName(albumName)) +
				   " WHERE " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE) + " = " + albumItemID;
			
			try (Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				 ResultSet rs = statement.executeQuery(picturesQuery);) {			
			
				while (rs.next()) {
					pictures.add(new AlbumItemPicture(rs.getLong(1), rs.getString(2), rs.getString(3), albumName, rs.getLong(4)));
				}			
			} catch (SQLException e) {			
				throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
			}
		}
		
		return pictures;
	}
	
	static AlbumItem getAlbumItem(String albumName, long albumItemId) throws DatabaseWrapperOperationException {
		String queryString = QueryBuilder.createSelectStarQuery(
				DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName))) + 
				" WHERE id = " + albumItemId;
		List<AlbumItem> items = getAlbumItems(queryString);

		AlbumItem requestedItem = null;
		try {
			requestedItem = items.get(0);
			return requestedItem;
		} catch (IndexOutOfBoundsException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		}		
	}

	static List<AlbumItem> getAlbumItems(String queryString) throws DatabaseWrapperOperationException {

 		LinkedList<AlbumItem> list = new LinkedList<AlbumItem>();		
		
		try (
			Statement statement = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(queryString); ) {
	
			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// For each albumItem
			while (rs.next()) {
				// Create a new AlbumItem instance
				AlbumItem albumItem = new AlbumItem("");
				// Each ItemField
				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {

					// Add new field
					String fieldName = metaData.getColumnName(columnIndex);
					String tableName = metaData.getTableName(1);
					String albumName = DatabaseOperations.getAlbumName(tableName);
					
					albumItem.setAlbumName(albumName);
					
					FieldType type = HelperOperations.detectDataType(tableName, fieldName);
					Object value = HelperOperations.fetchFieldItemValue(rs, columnIndex, type, albumName);
					boolean quicksearchable = isAlbumFieldQuicksearchable(albumName, fieldName);
					// omit the typeinfo field and set the contentVersion separately
					if (type == FieldType.ID && fieldName.endsWith(DatabaseConstants.TYPE_INFO_COLUMN_NAME)){
						continue;
					} else if (type.equals(FieldType.UUID) && fieldName.equals(DatabaseConstants.CONTENT_VERSION_COLUMN_NAME)) {
						albumItem.setContentVersion((UUID) value);
					} else {
						albumItem.addField(fieldName, type, value, quicksearchable);
					}
				}
				list.add(albumItem);
			}

		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE,e);
		}

		return list;
	}
	
	static boolean isDateField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.DATE)) {
				return true;
			}
		}
		return false;
	}

	static boolean isOptionField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.OPTION)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean isStarRatingField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.STAR_RATING)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean isAlbumNameAvailable(String requestedAlbumName) throws DatabaseWrapperOperationException {
		for (String albumName : getListOfAllAlbums()) {
			if (albumName.equalsIgnoreCase(requestedAlbumName)) {
				return false;
			}
		}
		return true;
	}

	static boolean isItemFieldNameAvailable(String albumName, String requestedFieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAlbumItemFieldNamesAndTypes(albumName)) {
			if (requestedFieldName.equalsIgnoreCase(metaItemField.getName())) {
				return false;
			}
		}
		return true;
	}
	
	static boolean isAlbumFieldQuicksearchable(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		List<String> quicksearchableFieldNames = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));
		return quicksearchableFieldNames.contains(fieldName);
	}

	static boolean isAlbumQuicksearchable(String albumName) throws DatabaseWrapperOperationException {
		List<String> quicksearchableFieldNames = getIndexedColumnNames(DatabaseStringUtilities.generateTableName(albumName));

		return quicksearchableFieldNames.size() >= 1;
	}
	
	static boolean isPictureAlbum(String albumName) throws DatabaseWrapperOperationException {
		if (DatabaseStringUtilities.isStringNullOrEmpty(albumName)) {
			return false;
		}
		
		String query = " SELECT " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.HAS_PICTURES_COLUMN_IN_ALBUM_MASTER_TABLE) +
 					   "   FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ALBUM_MASTER_TABLE_NAME) +
					   "  WHERE " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE) + 
					   					"=" + DatabaseStringUtilities.encloseNameWithQuotes(albumName);
		
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(query)) {		
			
			if (resultSet.next()) {
				return OptionType.valueOf(resultSet.getString(1)) == OptionType.YES;
			}
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}
		
		return false;
	}
	
	static String getAlbumName(String tableName) throws DatabaseWrapperOperationException {
		String query = " SELECT " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE) +
				   	   "   FROM " + DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ALBUM_MASTER_TABLE_NAME) +
				       "  WHERE " + DatabaseStringUtilities.transformColumnNameToSelectQueryName(DatabaseConstants.ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE) + 
				   						"=" + DatabaseStringUtilities.encloseNameWithQuotes(tableName);
	
		try (Statement statement = ConnectionManager.getConnection().createStatement();
			 ResultSet resultSet = statement.executeQuery(query)) {		
			
			if (resultSet.next()) {
				return resultSet.getString(1);
			}
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}
		
		return null;
	}
	
	static Map<String, FieldType> getAlbumItemFieldNameToTypeMap(String albumName) throws DatabaseWrapperOperationException {
		Map<String, FieldType> fieldNameToType = new HashMap<String, FieldType>();
		Map<Integer, MetaItemField> metaItemFields = getAlbumItemMetaMap(albumName);
		
		for (MetaItemField metaItemField : metaItemFields.values()) {
			fieldNameToType.put(metaItemField.getName(), metaItemField.getType());
		}
		
		return fieldNameToType;
	}
}
