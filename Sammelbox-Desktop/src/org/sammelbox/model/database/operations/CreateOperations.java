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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.model.album.AlbumItem;
import org.sammelbox.model.album.AlbumItemPicture;
import org.sammelbox.model.album.FieldType;
import org.sammelbox.model.album.ItemField;
import org.sammelbox.model.album.MetaItemField;
import org.sammelbox.model.album.SampleAlbumItemPicture;
import org.sammelbox.model.database.DatabaseStringUtilities;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateOperations {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateOperations.class);
	
	private CreateOperations() {
		// use static methods
	}
	
	static void createNewAlbum(String albumName, List<MetaItemField> fields, boolean hasAlbumPictures) throws DatabaseWrapperOperationException {
		if (fields == null || !DatabaseOperations.isAlbumNameAvailable(albumName)) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, "The chosen album name is already in use");
		}
		
		createNewAlbumTable(fields, albumName, DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generateTableName(albumName)), hasAlbumPictures);
		// Indicate which fields are quick-searchable
		List<String> quickSearchableColumnNames = new ArrayList<String>();
		for (MetaItemField metaItemField : fields) {
			if (metaItemField.isQuickSearchable()){
				quickSearchableColumnNames.add(DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName()));
			}
		}
		
		// Create picture table
		createPictureTable(DatabaseStringUtilities.generateTableName(albumName));
		
		// Create picture directory
		FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection());
		
		// Make columns quick-searchable
		createIndex(DatabaseStringUtilities.generateTableName(albumName), quickSearchableColumnNames);
	}
	
	/**
	 * Creates a new table with the specified fields. See fields parameter for temporary table creation.
	 * Beware a temporary table does not show up in the list of all albums.
	 * @param fields The fields making up the new album content. Null if a temporary copy of the specified existing table should be made.
	 * @param albumName The name of the album which is currently created
	 * @param tableName The database name of the table to be created
	 * @throws DatabaseWrapperOperationException 
	 * @param albumHasPictures true indicates that this album may contain pictures and the related flag in the master table should be set  
	 */
	static void createNewAlbumTable(List<MetaItemField> fields, String albumName, String tableName, boolean albumHasPictures) throws DatabaseWrapperOperationException {
		String typeInfoTableName = "";
		String createTempTableSQL = "";
		List<MetaItemField> columns =  new ArrayList<MetaItemField>(fields);
		boolean temporary = (columns == null); // TODO comment. Whats up with this temporary table?

		if (temporary) {
			// Retrieve the typeInfo of the old Album before creating a new temp table
			columns = QueryOperations.getAllAlbumItemMetaItemFields(tableName);
			// Remove the id field from the old table
			columns.remove(new MetaItemField("id", FieldType.ID));
			// Remove the type info  field from the old table
			columns.remove(new MetaItemField(DatabaseConstants.TYPE_INFO_COLUMN_NAME, FieldType.ID));
			tableName = DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTempTableName(albumName));
			typeInfoTableName = DatabaseStringUtilities.encloseNameWithQuotes(
					DatabaseStringUtilities.generateTypeInfoTableName(albumName));
			createTempTableSQL = "TEMPORARY";
		} else {
			typeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(albumName);
		}

		// Ensures that the table has a contentVersion column
		MetaItemField contentVersion = new MetaItemField(DatabaseConstants.CONTENT_VERSION_COLUMN_NAME, FieldType.UUID);
		if (!columns.contains(contentVersion)) {
			columns.add(contentVersion);
		}

		// Prepare statement string
		String createMainTableString = "";
		StringBuilder sb = new StringBuilder("CREATE ");

		// Insert temporary table qualifier when necessary
		sb.append(createTempTableSQL);
		
		sb.append(" TABLE ");
		sb.append(tableName);
		sb.append(" ( id INTEGER PRIMARY KEY");


		for (MetaItemField item : columns) {
			sb.append(" , ");
			sb.append(DatabaseStringUtilities.encloseNameWithQuotes(item.getName()));	// " , 'fieldName'"  
			sb.append(" ");										// " , 'fieldName' " 
			sb.append(item.getType().toDatabaseTypeString());	// " , 'fieldName' TYPE"
		}

		sb.append(" ,  ");

		// Add the typeInfo column and foreign key reference to the typeInfo table.
		sb.append(DatabaseConstants.TYPE_INFO_COLUMN_NAME);
		sb.append(" INTEGER, FOREIGN KEY(");
		sb.append(DatabaseConstants.TYPE_INFO_COLUMN_NAME);
		sb.append(") REFERENCES ");
		sb.append(typeInfoTableName);
		sb.append("(id))");
		createMainTableString = sb.toString();

		// Save the type informations in a separate table
		createTypeInfoTable(typeInfoTableName, columns, temporary);
		
		// Add the album back to the album master table
		UpdateOperations.addNewAlbumToAlbumMasterTable(albumName, albumHasPictures);
		
		try (Statement statement = ConnectionManager.getConnection().createStatement()) {
			// Create the Album table			
			statement.executeUpdate(createMainTableString);
		} catch (SQLException sqlException) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, sqlException);
		}
	}
	
	/**
	 * Creates the table that contains the type information for the specified album.
	 * @param typeInfoTableName The name of the typeInfoTable to be created.
	 * @param metafields A list of meta data describing the main table and making up the content of the typeInfoTable.
	 * @param temporary True if the table is for temporary storage only.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void createTypeInfoTable(String typeInfoTableName, List<MetaItemField> metafields, boolean temporary) throws DatabaseWrapperOperationException {
		// Prepare createTypeInfoTable string.
		String createTypeInfoTableString = "";	
		StringBuilder sb = new StringBuilder("CREATE ");
		if (temporary) {
			sb.append("TEMPORARY ");
		}
		
		// Add the table id column, id column of main table is not stored, id type is determined differently via dbMetaData.
		sb.append("TABLE IF NOT EXISTS ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(typeInfoTableName));
		sb.append(" ( id INTEGER PRIMARY KEY");
				
		// Add the main table columns sans id field or typeInfo column
		for (MetaItemField metaItemField : metafields) {
			sb.append(" , ");// " , "
			sb.append(DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName()));// " , 'fieldName'"
			sb.append(" TEXT");// " , 'fieldName' TEXT" //stored as text to ease transformation back to java enum.
		}
		
		// Add the schema version uuid column
		sb.append(" , ");
		sb.append(DatabaseConstants.SCHEMA_VERSION_COLUMN_NAME);
		sb.append(" TEXT )");
		createTypeInfoTableString = sb.toString();
		
		// Drop old type info table.
		DeleteOperations.dropTable(typeInfoTableName);			
			
		// Create the typeInfo table		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(createTypeInfoTableString);) {
			preparedStatement.executeUpdate();
		} catch(SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}

		// Append the schema version uuid to the list of metaFields
		metafields.add(new MetaItemField(DatabaseConstants.SCHEMA_VERSION_COLUMN_NAME, FieldType.UUID));
		
		// Add the entry about the type info in the newly created TypeInfo table 
		UpdateOperations.addTypeInfo(typeInfoTableName, metafields);			
	}

	/**
	 * Creates a database index containing the specified columns of the album (table). This index is currently used to identify the 
	 * fields (columns)  marked for the quicksearch feature.
	 * @param albumName The name of the album to which the index belongs. This name should NOT be escaped.
	 * @param columnNames The list of names of columns to be included in the index. Performs an automatic test to see if the column names 
	 * are quoted.  
	 * @throws DatabaseWrapperOperationException 
	 */
	static void createIndex(String albumName, List<String> columnNames) throws DatabaseWrapperOperationException {
		if (columnNames.isEmpty()) {
			return;
		}
		
		StringBuilder sqlStringbuiler = new StringBuilder("CREATE INDEX ");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generateIndexTableName(albumName)));
		sqlStringbuiler.append(" ON ");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumName)));
		sqlStringbuiler.append(" (");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(columnNames.get(0)));		
		if (columnNames.size()>=2) {
			for (int i=1;i<columnNames.size(); i++) {
				sqlStringbuiler.append(", ");
				sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(columnNames.get(i))); 
			}
		}		
		sqlStringbuiler.append(")");
		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sqlStringbuiler.toString())) {
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}
	}
	
	static void createAlbumMasterTableIfItDoesNotExist() throws DatabaseWrapperOperationException {
		List<MetaItemField> fields = new ArrayList<MetaItemField>();
		
		// Add the natural album name
		fields.add(new MetaItemField(DatabaseConstants.ALBUMNAME_IN_ALBUM_MASTER_TABLE, FieldType.TEXT));
		// Add the table name column
		fields.add(new MetaItemField(DatabaseConstants.ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE, FieldType.TEXT));
		// Add the table's picture state column indicating if the album structure has pictures or not
		fields.add(new MetaItemField(DatabaseConstants.HAS_PICTURES_COLUMN_IN_ALBUM_MASTER_TABLE, FieldType.OPTION));		
		
		// Create the album master table.
		createTableWithIdAsPrimaryKey(DatabaseConstants.ALBUM_MASTER_TABLE_NAME, fields , false, true);
	}
	
	/**
	 * Helper function to create a new table in the database. Only if ALL of the foreign key parameters are valid, a foreign key column will be created.
	 * @param tableName The name of the table to be created
	 * @param columns The columns making up the new table. No column constraints will be applied here.
	 * @param temporaryTable True indicates a table for temporary use only. This table will be created in the temporary database.
	 * @param ifNotExistsClause True adds the 'IF NOT EXISTS' to the create table command.
	 * @throws DatabaseWrapperOperationException
	 */
	private static void createTableWithIdAsPrimaryKey(String tableName, List<MetaItemField> columns, boolean temporaryTable, boolean ifNotExistsClause) throws DatabaseWrapperOperationException {
		StringBuilder sb = new StringBuilder("CREATE ");
		if (temporaryTable) {
			sb.append("TEMPORARY ");
		}

		sb.append("TABLE ");
		if (ifNotExistsClause) {
			sb.append("IF NOT EXISTS "); 
		}

		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(tableName));
		sb.append(" ( ");
		sb.append(DatabaseConstants.ID_COLUMN_NAME);
		sb.append(" INTEGER PRIMARY KEY");
		
		for (MetaItemField item : columns) {
			sb.append(" , ");
			sb.append(DatabaseStringUtilities.encloseNameWithQuotes(item.getName()));	// " , 'fieldName'"  
			sb.append(" ");																// " , 'fieldName' " 
			sb.append(item.getType().toDatabaseTypeString());							// " , 'fieldName' TYPE"
		}
		
		sb.append(")");
		
		String createTableString = sb.toString();

		// Create the table.		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(createTableString);) {			
			preparedStatement.executeUpdate();
		} catch (Exception e) {			
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}
	}

	static void createPictureTable(String albumName) throws DatabaseWrapperOperationException {		
		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		// The filename of the original picture 
		columns.add(new MetaItemField(DatabaseConstants.ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE, FieldType.TEXT));
		// The filename of the generated thumbnail picture
		columns.add(new MetaItemField(DatabaseConstants.THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE, FieldType.TEXT));
		// The id of the album item the picture belongs to
		columns.add(new MetaItemField(DatabaseConstants.ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE, FieldType.ID));
	
		createTableWithIdAsPrimaryKey(DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generatePictureTableName(albumName)), columns , false, true);
	}
	
	/** Adds the given album item to the corresponding album table 
	 * @param albumItem the item to be added 
	 * @param addPictures should the pictures from the given album item be added to the corresponding picture table?
	 * This flag is useful in the case of internal operations which do not touch the picture table such as the 
	 * rename of a field
	 * @param updateContentVersion should the content version be automatically be set/updated during the addition?
	 * This flag is useful in the case where items are re-added to the table after some structural alterations have
	 * been performed that have not modified the content */
	static long addAlbumItem(AlbumItem albumItem, boolean addPictures, boolean updateContentVersion) throws DatabaseWrapperOperationException {
		// Check if the item contains a albumName
		if (albumItem.getAlbumName().isEmpty()) {
			LOGGER.error("Item {} has no albumName", albumItem);
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
		}
		
		// Check if specified album Item is valid
		if (!albumItem.isValid()) {
			LOGGER.error("Item {} is invalid", albumItem);
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
		}

		// Check if content version should be carried over if yes ensure a content version is present
		if (updateContentVersion == false && albumItem.getContentVersion() == null) {
			LOGGER.error("The option for carrying over the old content version " +
					"is checked but no content version is found in the item!");
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE);
		}

		// Build the SQL string with place-holders '?'
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generateTableName(albumItem.getAlbumName())));
		sb.append(" ( ");

		for (ItemField itemField : albumItem.getFields()) {
			String name = itemField.getName();
			// Ensure that no field with the name of typeInfoColumnName
			if (!name.equalsIgnoreCase(DatabaseConstants.TYPE_INFO_COLUMN_NAME)) {
				sb.append(DatabaseStringUtilities.encloseNameWithQuotes(name));
				sb.append(", ");
			}
		}

		// Add the typeInfoColumnName 
		sb.append(DatabaseConstants.TYPE_INFO_COLUMN_NAME);
		sb.append(" ) VALUES ( ");

		for (ItemField itemField : albumItem.getFields()) {
			String name = itemField.getName();
			if (!name.equalsIgnoreCase(DatabaseConstants.TYPE_INFO_COLUMN_NAME)) {
				sb.append("?, ");
			}
		}
		// Add wildcard for the typeInfoColumn Value
		sb.append("? )");
		
		String savepointName = DatabaseIntegrityManager.createSavepoint();
	
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())){
			long idOfAddedItem = albumItem.getItemID() != AlbumItem.ITEM_ID_UNDEFINED ? albumItem.getItemID() : -1;
			
			// Replace the wildcard character '?' by the real type values
			int parameterIndex = 1;
			for (ItemField itemField : albumItem.getFields()) {	
				String name = itemField.getName();
				
				if (itemField.getType().equals(FieldType.ID) && idOfAddedItem != AlbumItem.ITEM_ID_UNDEFINED) {
					HelperOperations.setValueToPreparedStatement(preparedStatement, parameterIndex, itemField, albumItem.getAlbumName());
				}
				
				if (!name.equalsIgnoreCase(DatabaseConstants.TYPE_INFO_COLUMN_NAME)) {					
					HelperOperations.setValueToPreparedStatement(preparedStatement, parameterIndex, itemField, albumItem.getAlbumName());
					parameterIndex++;
				}
			}
			preparedStatement.setLong(parameterIndex, DatabaseConstants.TYPE_INFO_FOREIGN_KEY);			

			// Retrieves the generated key used in the new  album item
			preparedStatement.executeUpdate();
			
			if (albumItem.getItemID() == AlbumItem.ITEM_ID_UNDEFINED ) {
				try(ResultSet generatedKeys = preparedStatement.getGeneratedKeys();) {
					if (generatedKeys.next()) {
						idOfAddedItem = generatedKeys.getLong(1);
					}
				} catch (SQLException sqlEx) {
					DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
					throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, sqlEx);
				}
			}
			
			// If possible (and demanded) store picture links
			if (addPictures && albumItem.getPictures() != null) {
				for (AlbumItemPicture picture : albumItem.getPictures()) {
					picture.setAlbumItemID(idOfAddedItem);
					picture.setAlbumName(albumItem.getAlbumName());
					addAlbumItemPicture(picture);
				}
			}
			
			// Either copies the old content version over or generates a new one
			UUID newUUID = UUID.randomUUID();
			if (!updateContentVersion) {
				// Carry over old content version
				newUUID = albumItem.getContentVersion();

			}
			UpdateOperations.updateContentVersion(albumItem.getAlbumName(), idOfAddedItem, newUUID);
			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			return idOfAddedItem;
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}
	
	/** See {@link #addAlbumItem(AlbumItem, boolean, boolean)} */
	static long addAlbumItem(AlbumItem albumItem, boolean updateContentVersion) throws DatabaseWrapperOperationException {
		return addAlbumItem(albumItem, true, updateContentVersion);
	}
	
	static void addAlbumItemPicture(AlbumItemPicture albumItemPicture) throws DatabaseWrapperOperationException {
		if (albumItemPicture instanceof SampleAlbumItemPicture) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, "Cannot persist SampleAlbumItemPicture");
		}
		
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generatePictureTableName(albumItemPicture.getAlbumName())));
		sb.append(" ( ");

		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(DatabaseConstants.ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE));
		
		sb.append(") VALUES( ");

		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItemPicture.getOriginalPictureName()) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItemPicture.getThumbnailPictureName()) + ", ");
		sb.append(albumItemPicture.getAlbumItemID());

		sb.append(" ) ");
		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())){			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_DIRTY_STATE, e);
		}
	}
}
