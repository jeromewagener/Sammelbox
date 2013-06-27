package collector.desktop.database;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.album.AlbumItem;
import collector.desktop.album.AlbumItem.AlbumItemPicture;
import collector.desktop.album.FieldType;
import collector.desktop.album.ItemField;
import collector.desktop.album.MetaItemField;
import collector.desktop.album.OptionType;
import collector.desktop.album.StarRating;
import collector.desktop.database.QueryBuilder.QueryComponent;
import collector.desktop.database.QueryBuilder.QueryOperator;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class DatabaseWrapper  {
	/** The suffix to the table containing all picture information for a single album */
	private static final String PICTURE_TABLE_SUFFIX = "pictures";
	/** The name of the picture table column that stores the filename of the original picture */
	private static final String ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE = "original_picture_filename";
	/** The name of the picture table column that stores the filename of the thumbnail picture */
	private static final String THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE = "thumbnail_picture_filename";
	/** The reference to the album item which is associated with the current picture */
	private static final String ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE = "album_item_foreign_key";
	/** Suffix used to append to the name of the main table to obtain the index name during index creation.*/
	private static final String INDEX_NAME_SUFFIX = "_index";
	/** The suffix used to append to the main table name to obtain the typeInfo table name.*/
	private static final String TYPE_INFO_SUFFIX = "_typeinfo";
	/** The suffix used to append to the main table to obtain the temporary table name.*/
	private static final String TEMP_TABLE_SUFFIX = "_temptable";
	/** The final foreign key of all main table entries to their type information entry.*/
	private static final int TYPE_INFO_FOREIGN_KEY = 1;
	/** The final name of the column containing the type information foreign key in the main table.*/
	protected static final String TYPE_INFO_COLUMN_NAME = "typeinfo";
	/** The final name of the schema version column. Updated at each structural change of an album.*/
	protected static final String SCHEMA_VERSION_COLUMN_NAME = "schema_version";
	/** The final name of the content version column. Updated at each change of the content of the field.*/
	protected static final String CONTENT_VERSION_COLUMN_NAME = "content_version";
	/** The name of the album master table containing all stored album table names and their type table names*/
	private static final String ALBUM_MASTER_TABLE_NAME = "album_master_table";
	/** The column name for the album table. */
	private static final String ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE= "album_table_name";
	/** The column name for the album type table. */
	private static final String TYPE_TABLENAME_ALBUM_MASTER_TABLE = "album_type_table_name";
	/** The final name of the picture column. Currently only a single column is supported, this is its name.*/
	private static final String PICTURE_COLUMN_NAME_IN_ALBUM_MASTER_TABLE = "has_pictures";
	/** The default name for ID columns */
	public static final String ID_COLUMN_NAME = "id";
	/**The normal logger for all info, debug, error and warning in this class*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWrapper.class);
	/**
	 * Creates a new album according to the specified properties.
	 * @param albumName The name of the album to be created.
	 * @param fields The metadata fields describing the fields of the new album. Pass an empty list as argument
	 * when creating an album with no fields.  
	 * @param hasAlbumPictures When set to true creates a single picture field in the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void createNewAlbum(String albumName, List<MetaItemField> fields, boolean hasAlbumPictures) throws DatabaseWrapperOperationException {
		if (fields == null || !albumNameIsAvailable(albumName)) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, "The chosen album name is already in use");
		}
		
		//String savepointName = DatabaseIntegrityManager.createSavepoint();
		createNewAlbumTable(fields, albumName, hasAlbumPictures);
		// Indicate which fields are quicksearchable
		List<String> quickSearchableColumnNames = new ArrayList<String>();
		for (MetaItemField metaItemField : fields) {
			if (metaItemField.isQuickSearchable()){
				quickSearchableColumnNames.add(DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName()));
			}
		}
		
		// Create picture table
		createPictureTable(albumName);
		
		// Create picture directory
		FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.connection);
		
		// Make columns quick searchable
		createIndex(albumName, quickSearchableColumnNames);
	}

	/**
	 * Permanently renames an album in the specified database
	 * @param oldAlbumName The old name of the album to be renamed
	 * @param newAlbumName The new name of the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	// FIXME also rename picture table
	public static void renameAlbum(String oldAlbumName, String newAlbumName) throws DatabaseWrapperOperationException {		
		String savepointName =  DatabaseIntegrityManager.createSavepoint();
		try {
			// Rename the album table
			renameTable(oldAlbumName, newAlbumName);
			
			// Rename the type info table		
			String oldTypeInfoTableName = makeTypeInfoTableName(oldAlbumName);
			String newTypeInfoTableName = makeTypeInfoTableName(newAlbumName);
	
			renameTable(oldTypeInfoTableName, newTypeInfoTableName);
			// Change the entry in the album master table. OptionType.UNKNOWN indicates no change of the picture storing 
			modifyAlbumInAlbumMasterTable(oldAlbumName, newAlbumName, newTypeInfoTableName, OptionType.UNKNOWN);			
	
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			}
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Renames a table. All referenced columns and indices
	 * @param oldTableName The name of the table to be renamed.
	 * @param newTableName The new name of the table.
	 * @return True if the operation was successful, false otherwise.
	 */
	private static boolean renameTable(String oldTableName, String newTableName) {
		boolean success = true;

		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(oldTableName));
		sb.append(" RENAME TO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(newTableName));
		String renameTableSQLString = sb.toString();

		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = ConnectionManager.connection.prepareStatement(renameTableSQLString);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			success = false;
		}finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				success = false;
			}
		}
		return success;
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
		// Check if the specified columns exists.
		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
		if (!metaInfos.contains(metaItemField)) {
			if (metaInfos.contains(new MetaItemField(metaItemField.getName(), metaItemField.getType(), !metaItemField.isQuickSearchable()))){
				LOGGER.error("The specified meta item field's quicksearch flag is not set appropriately!");
			}else {
				LOGGER.error("The specified meta item field is not part of the album");
			}
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		
		// Backup the old data in java objects
		List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(QueryBuilder.createSelectStarQuery(albumName));
		// Create the new table pointing to new typeinfo
		boolean keepPictureField = albumHasPictureField(albumName);
		List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
		newFields = removeFieldFromMetaItemList(metaItemField, newFields);// [delete column]

		String savepointName = DatabaseIntegrityManager.createSavepoint();
		// Drop the old table + typeTable
		try {
			removeAlbum(albumName);
		
			// The following three columns are automatically created by createNewAlbumTable
			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
			createNewAlbumTable( newFields, albumName, keepPictureField);
			
			// Restore the old data from the java objects in the new tables [delete column]
			List<AlbumItem> newAlbumItems = removeFieldFromAlbumItemList(metaItemField, albumItems);
			for (AlbumItem albumItem : newAlbumItems) {
				albumItem.setAlbumName(albumName);
				addNewAlbumItem(albumItem, false);
			}
	
			rebuildIndexForTable(albumName, newFields);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithCleanState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);					
				LOGGER.error("Unable to roll back before to state before the removal of the album item field");
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
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

		// Check if the specified columns exists.
		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
		if (!metaInfos.contains(oldMetaItemField) || oldMetaItemField.getType().equals(FieldType.ID)) {
			if (metaInfos.contains(new MetaItemField(oldMetaItemField.getName(), oldMetaItemField.getType(), !oldMetaItemField.isQuickSearchable()))){
				LOGGER.error("The specified meta item field's quicksearch flag is not set appropriately!");
			}else {
				LOGGER.error("The specified meta item field is not part of the album");
			}
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		
		String savepointName = DatabaseIntegrityManager.createSavepoint();		
		try {
			// Backup the old data in java objects
			List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(QueryBuilder.createSelectStarQuery(albumName));

			// Create the new table pointing to new typeinfo
			boolean hasPictureField = albumHasPictureField(albumName);
			List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
			newFields = renameFieldInMetaItemList(oldMetaItemField, newMetaItemField, newFields);// [rename column]
		
			// Drop the old table + typeTable
			removeAlbum(albumName);

			// the following three columns are automatically created by createNewAlbumTable
			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);

			createNewAlbumTable(newFields, albumName, hasPictureField);	

			// Restore the old data from the java objects in the new tables [rename column]
			renameFieldInAlbumItemList(oldMetaItemField, newMetaItemField, albumItems);
		
			rebuildIndexForTable(albumName, newFields);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}		
	}

	/**
	 * Moves the specified field to a the position directly after the column with the name moveAfterColumnName.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The metadata to identify the field (column) to be moved.
	 * @param preceedingField The field (column) which is preceeding the field after the reordering. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void reorderAlbumItemField(String albumName, MetaItemField metaItemField, MetaItemField preceedingField) throws DatabaseWrapperOperationException {

		// Check if the specified columns exists.
//		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
//		if (!metaInfos.contains(metaItemField)) {
//			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
//		}

		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {
			// Backup the old data in java objects
			List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(QueryBuilder.createSelectStarQuery(albumName));
			// Create the new table pointing to new typeinfo
			boolean hasPictureField = albumHasPictureField(albumName);
			List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
			newFields = reorderFieldInMetaItemList(metaItemField, preceedingField, newFields);// [reorder column]

			// Drop the old table + typeTable
			removeAlbum(albumName);

			// the following three columns are automatically created by createNewAlbumTable
			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);

			// Create the new table pointing to new typeinfo
			createNewAlbumTable(newFields, albumName, hasPictureField);

			// Restore the old data from the temptables in the new tables [reorder column]
			List<AlbumItem> newAlbumItems = reorderFieldInAlbumItemList(metaItemField, preceedingField, albumItems);
			// replace the empty picField with the saved raw PicField 
			for (AlbumItem albumItem : newAlbumItems) {
				albumItem.setAlbumName(albumName);
				addNewAlbumItem(albumItem, false);				
			}

			rebuildIndexForTable(albumName, newFields);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Sets the ability of albumField to the value found in the metaItemField describing that field.
	 * @param albumName The name of the album to which the item belongs.
	 * @param metaItemField The field (column) to be set quicksearchable.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void setQuickSearchable(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {
			List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);			
			// Enable for quicksearch feature
			if (metaItemField.isQuickSearchable() && !quickSearchableColumnNames.contains(metaItemField.getName())) {
				quickSearchableColumnNames.add(metaItemField.getName());
	
				dropIndex(albumName);
				createIndex(albumName, quickSearchableColumnNames);			
			}else if (!metaItemField.isQuickSearchable()){	
				// Disable for quicksearch feature
				quickSearchableColumnNames.remove(metaItemField.getName());
				createIndex(albumName, quickSearchableColumnNames);
			}				
			updateSchemaVersion(albumName);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
			}
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}		
	}

	/**
	 * Generates a new random UUID. ID collisions may happen although highly unlikely
	 * @return The uuid value.
	 */
	private static UUID generateNewUUID () {
		return UUID.randomUUID();
	}

	/**
	 * Removes a MetaItemField from a list by using an equals test. Returns the result in a new list.
	 * @param metaItemField The field to be removed from the list.
	 * @param fieldList The list of which the metaItemField will be removed.
	 * @return The parameter fieldList with the specified field removed.
	 */
	private static List<MetaItemField> removeFieldFromMetaItemList(MetaItemField metaItemField, final List<MetaItemField> fieldList) {
		List<MetaItemField> newFieldList = fieldList;
		newFieldList.remove(metaItemField);
		return newFieldList; 
	}

	/**
	 * Renames a metaItemField for all the fields in the list.Type changes are currently not supported. Returns the result in a new list.
	 * @param oldMetaItemField The metaItemField to be renamed.
	 * @param newMetaItemField The metaItemField containing the new name.
	 * @param fieldList The list of fields to be renamed.
	 * @return The list of renamed fields.
	 */
	private static List<MetaItemField> renameFieldInMetaItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<MetaItemField> fieldList) {
		List<MetaItemField> newFieldList = fieldList;
		int index = newFieldList.indexOf(oldMetaItemField);
		MetaItemField renameMetaItemField = newFieldList.get(index);
		renameMetaItemField.setName(newMetaItemField.getName());
		return newFieldList; 
	}

	/**
	 * Moves the metaItemField after the specified moveAfterField. In case the latter is null, move to beginning of list. 
	 * Returns the result in a new list.
	 * @param metaItemField The field to be moved to a new position.
	 * @param precedingField The field which will precede the field in the new ordering. If null the field will be inserted at the beginning
	 * of the list.
	 * @param fieldList List of fields containing the field in the old ordering.
	 * @return The list of fields in the new ordering.
	 */
	private static List<MetaItemField> reorderFieldInMetaItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<MetaItemField> fieldList) {
		List<MetaItemField> newFieldList = fieldList;
		newFieldList.remove(metaItemField);
		if (precedingField == null) {
			newFieldList.add(0,metaItemField);
		} else {

			int insertAfterIndex = newFieldList.indexOf(precedingField);
			if (insertAfterIndex==-1) {
				newFieldList.add(metaItemField);
			}else {
				newFieldList.add(insertAfterIndex+1, metaItemField);
			}
		}
		return newFieldList; 
	}

	/**
	 * Removes the provided metaItemField from each AlbumItem entry in the albumList. Returns the result in a new list.
	 * @param metaItemField The metaItemFiel to be removed from each entry of the list.
	 * @param albumList The list containing all the AlbumItems.
	 * @return The albumList with the specified metaItemField removed from each entry.
	 */
	private static List<AlbumItem> removeFieldFromAlbumItemList(MetaItemField metaItemField, final List<AlbumItem> albumList) {
		List<AlbumItem> newAlbumItemList = albumList;
		for (AlbumItem albumItem: newAlbumItemList) {
			albumItem.removeField(metaItemField);
		}
		return newAlbumItemList; 
	}

	/**
	 * Renames a field in all the album items in the list. Returns the result in a new list.
	 * @param oldMetaItemField The metaItemField to be renamed.
	 * @param newMetaItemField The metaItemField containing the new name.
	 * @param albumList The list of album items whose field is to be renamed.
	 * @return The new list of album items.
	 */
	private static List<AlbumItem> renameFieldInAlbumItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<AlbumItem> albumList) {
		List<AlbumItem> newAlbumItemList = albumList;
		for (AlbumItem albumItem: newAlbumItemList) {
			albumItem.renameField(oldMetaItemField, newMetaItemField);
		}
		return newAlbumItemList; 
	}

	/**
	 * Moves a field of an album item to a new position. Returns the result in a new list. 
	 * @param metaItemField The field to be moved.
	 * @param precedingField The field which will precede the specified item after the reordering. If null the item will be moved to the top
	 * of the list. 
	 * @param albumList The list of album items in their original ordering.
	 * @return The list of album items in their new order. Content remains untouched.
	 */
	private static List<AlbumItem> reorderFieldInAlbumItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<AlbumItem> albumList) {
		List<AlbumItem> newAlbumItemList = albumList;
		for (AlbumItem albumItem: newAlbumItemList) {
			albumItem.reorderField(metaItemField, precedingField);
		}
		return newAlbumItemList; 
	}

	/**
	 * Permanently removes an album along with its typeInfo metadata.
	 * @param albumName The name of the album which is to be removed.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void removeAlbum(String albumName) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {	
			String typeInfoTableName = getTypeInfoTableName(albumName);
			dropTable(albumName);
				
			dropTable(typeInfoTableName);
	
			removeAlbumFromAlbumMasterTable(albumName); 
	
			dropPictureTableForAlbum(albumName);
			
			FileSystemAccessWrapper.deleteDirectoryRecursively(
					new File(FileSystemAccessWrapper.getFilePathForAlbum(albumName)));
			
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Drops a table if it exists. No error or side effects if it does not exist.
	 * @param tableName The name of the table which is to be dropped.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void dropTable(String tableName) throws DatabaseWrapperOperationException  {
		try (Statement statement = ConnectionManager.connection.createStatement()){		
			statement.execute("DROP TABLE IF EXISTS "+DatabaseStringUtilities.encloseNameWithQuotes(tableName));
		} catch (Exception e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * Creates a new table with the specified fields. See fields parameter for temporary table creation.
	 * Beware a temporary table does not show up in the list of all albums.
	 * @param fields The fields making up the new album content. Null if a temporary copy of the specified existing table should be made.
	 * @param tableName The name of the table to be created.
	 * @param albumHasPictures True indicates that this table may contain pictures and the related flag in the master table is set.   
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void createNewAlbumTable(List<MetaItemField> fields, String tableName, boolean albumHasPictures) throws DatabaseWrapperOperationException {
		String typeInfoTableName = "";
		String createTempTableSQL = "";
		List<MetaItemField> columns =  new ArrayList<MetaItemField>(fields);
		boolean temporary = (columns == null);

		if (temporary) {
			// Retrieve the typeInfo of the old Album before creating a new temp table
			columns = getAllAlbumItemMetaItemFields(tableName);
			// Remove the id field from the old table
			columns.remove(new MetaItemField("id", FieldType.ID));
			// Remove the type info  field from the old table
			columns.remove(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID));
			tableName =  tableName + TEMP_TABLE_SUFFIX;
			typeInfoTableName =  tableName + TYPE_INFO_SUFFIX;
			createTempTableSQL = "TEMPORARY";
		}else {
			typeInfoTableName =  tableName + TYPE_INFO_SUFFIX;
		}

		// Ensures that the table has a contentVersion column
		MetaItemField contentVersion = new MetaItemField(CONTENT_VERSION_COLUMN_NAME, FieldType.UUID);
		if (!columns.contains(contentVersion)) {
			columns.add(contentVersion);
		}

		// Prepare statement string
		String createMainTableString = "";
		StringBuilder sb = new StringBuilder("CREATE ");

		// Insert temporary table qualifier when necessary
		sb.append(createTempTableSQL);
		
		sb.append(" TABLE ");
		tableName = DatabaseStringUtilities.encloseNameWithQuotes(tableName);
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
		sb.append(TYPE_INFO_COLUMN_NAME);
		sb.append(" INTEGER, FOREIGN KEY(");
		sb.append(TYPE_INFO_COLUMN_NAME);
		sb.append(") REFERENCES ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(typeInfoTableName));
		sb.append("(id))");
		createMainTableString = sb.toString();

		// Save the type informations in a separate table
		createTypeInfoTable(typeInfoTableName, columns, temporary);
		
		// Add the album back to the album master table
		addNewAlbumToAlbumMasterTable(tableName, typeInfoTableName, albumHasPictures);
		
		try (Statement statement = ConnectionManager.connection.createStatement()) {
			// Create the Album table			
			statement.executeUpdate(createMainTableString);
		}catch (SQLException sqlException) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, sqlException);
		}
		
		
	}

	/**
	 * Appends new field to the end of the album. Fields with the type FieldType.ID or FieldType.Picture fail the whole operation, no fields will be added then.
	 * @param albumName The name of the album to be modified.
	 * @param metaItemField The metaItemField to be appended to the album. Must not be null for successful insertion
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void appendNewAlbumField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		if (metaItemField.getType().equals(FieldType.ID) || metaItemField == null || !itemFieldNameIsAvailable(albumName, metaItemField.getName())) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {
			appendNewTableColumn(albumName, metaItemField);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
			}
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);			
		}
	}
	
	/**
	 * Appends a new column to the album table. This internal method does allows to add any type of column, even id and picture column.
	 * An exception is that you cannot add an additional picture column to an table.
	 * To prevent accidental corruption of the tables, perform checks in the enclosing methods.
	 * @param albumName The name of the album to be modified.
	 * @param metaItemField he metaItemFields to be appended to the album.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void appendNewTableColumn(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		// Prepare the append column string for the main table.
		StringBuilder sb = new StringBuilder("ALTER TABLE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumName));
		sb.append(" ADD COLUMN ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName()));
		sb.append(" ");
		sb.append(FieldType.Text.toDatabaseTypeString());

		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}

		updateTableColumnWithDefaultValue(albumName, metaItemField);

		// Append and update column for type table.
		appendNewTypeInfoTableColumn(albumName, metaItemField);

		updateSchemaVersion(albumName);
	}


	/**
	 * Enables or disables the picture functionality for a given album
	 * @param albumName The name of the album which is concerned
	 * @param albumPicturesEnabled true if pictures are enabled, false otherwise
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void setAlbumPictureFunctionality(String albumName, boolean albumPicturesEnabled) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		if (albumHasPictureField(albumName)) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, "Album "+ albumName +" already contains pictures");
		}

		try {
			String albumTypeInfoTableName = getTypeInfoTableName(albumName);
			
			if (albumPicturesEnabled) {			
				modifyAlbumInAlbumMasterTable(albumName, albumName, albumTypeInfoTableName, OptionType.YES);
			} else {
				modifyAlbumInAlbumMasterTable(albumName, albumName, albumTypeInfoTableName, OptionType.NO);
			}
		} catch ( DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			}
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Indicates whether the album contains a picture field.
	 * @param albumName The name of the album to be queried.
	 * @return True if the album contains a picture field, false otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean albumHasPictureField(String albumName) throws DatabaseWrapperOperationException {		
		String query = " SELECT count(*) AS numberOfItems" +
					   "   FROM " + ALBUM_MASTER_TABLE_NAME +
					   "  WHERE " + ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE + "=" + DatabaseStringUtilities.encloseNameWithQuotes(albumName) +
					   "    AND " + PICTURE_COLUMN_NAME_IN_ALBUM_MASTER_TABLE + "=" + DatabaseStringUtilities.encloseNameWithQuotes(OptionType.YES.toString());
		
		try (Statement statement = ConnectionManager.connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)) {		
			
			if (resultSet.next()) {
				return resultSet.getLong("numberOfItems") == 1;
			}
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
		
		return false;
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
		sb.append(SCHEMA_VERSION_COLUMN_NAME);
		sb.append(" TEXT )");
		createTypeInfoTableString = sb.toString();
		
		// Drop old type info table.
		dropTable(typeInfoTableName);			
			
		// Create the typeInfo table		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(createTypeInfoTableString);) {
			preparedStatement.executeUpdate();
		} catch(SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}

		// Append the schema version uuid to the list of metaFields
		metafields.add(new MetaItemField(SCHEMA_VERSION_COLUMN_NAME, FieldType.UUID));
		
		// Add the entry about the type info in the newly created TypeInfo table 
		addTypeInfo(typeInfoTableName, metafields);			
	}

	private static String makeTypeInfoTableName(String albumTableName) {
		if (albumTableName == null || albumTableName.isEmpty()) {
			return "";
		}
		return albumTableName + TYPE_INFO_SUFFIX;
	}


	/**
	 * Appends a new column to the typeInfoTable. Necessary when the main table is altered to have additional columns.
	 * @param tableName The name of the table to which the column belongs.
	 * @param metaItemField The metadata of the new column.
	 * @throws SQLException Exception thrown if any part of the operation fails.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void appendNewTypeInfoTableColumn(String tableName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		String quotedTypeInfoTableName = DatabaseStringUtilities.encloseNameWithQuotes(getTypeInfoTableName(tableName));
		String columnName = DatabaseStringUtilities.encloseNameWithQuotes(metaItemField.getName());
		// Prepare the append column string for the type table.
		StringBuilder sb = new StringBuilder("ALTER TABLE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(quotedTypeInfoTableName));
		sb.append(" ADD COLUMN ");
		sb.append(columnName);
		sb.append(" TEXT");

		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {
			preparedStatement.executeUpdate();					
		}catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
		
		sb.delete(0,sb.length());
		sb.append("UPDATE ");
		sb.append(quotedTypeInfoTableName);
		sb.append(" SET ");
		sb.append(columnName);
		sb.append(" = ?");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())){
			preparedStatement.setString(1, metaItemField.getType().toString());
			preparedStatement.executeUpdate();
		}catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}		

		updateSchemaVersion(tableName);
	}

	/**
	 * Helper method which adds an entry to the typeInfo table to indicate the types used in the main table and updates the 
	 * schema version UUID if properly included in the metafields.
	 * @param item the item describing the newly created main table. Making up the content of the typeInfoTable.
	 * @return True if the operation was successful. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void addTypeInfo(String typeInfoTableName, List<MetaItemField> metafields) throws DatabaseWrapperOperationException {
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(typeInfoTableName));
		sb.append(" ( ");

		// The 'while iterator loop' is used here because it is cheaper and more reliable than a foreach
		// to add commas ',' in between elements
		Iterator<MetaItemField> it = metafields.iterator();		
		while(it.hasNext()) {
			String fieldName = DatabaseStringUtilities.encloseNameWithQuotes(it.next().getName()); 
			sb.append(fieldName);
			if (it.hasNext())
			{
				sb.append(", ");
			}
		}
		sb.append(" ) VALUES ( ");

		it = metafields.iterator();		
		while(it.hasNext()) {
			it.next();
			sb.append("?");
			if (it.hasNext())
			{
				sb.append(", ");
			}
		}

		sb.append(") ");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {			
			// Replace the wildcard character '?' by the real type values
			int parameterIndex = 1;
			for (MetaItemField metaItemField : metafields){
				String columnValue = metaItemField.getType().toString();

				// Generate a new schema version UUID a table is created or modified.
				if ( metaItemField.getType().equals(FieldType.UUID) && metaItemField.getName().equals(SCHEMA_VERSION_COLUMN_NAME) ) {
					columnValue = generateNewUUID().toString();
				}

				preparedStatement.setString(parameterIndex, columnValue);
				parameterIndex++;
			}

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);			
		}
	}

	/**
	 * Rebuilds the index for a table after an alter table operation. 
	 * @param albumName The album to which these fields belong.
	 * @param items The items containing the information of whether they are quicksearchable.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void rebuildIndexForTable(String albumName, List<MetaItemField> fields) throws DatabaseWrapperOperationException {
		List<String> quicksearchColumnNames = new ArrayList<String>();
		for (MetaItemField metaItemField : fields) {
			if (metaItemField.isQuickSearchable()) {
				quicksearchColumnNames.add(metaItemField.getName());
			}
		}
		if (!quicksearchColumnNames.isEmpty()){
			String savepointName = DatabaseIntegrityManager.createSavepoint();
			try {
				createIndex(albumName, quicksearchColumnNames);
			} catch (DatabaseWrapperOperationException e) {
				if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
					DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				}
			}finally {
				DatabaseIntegrityManager.releaseSavepoint(savepointName);
			}
		}
	}


	/**
	 * Creates a database index containing the specified columns of the album (table). This index is currently used to identify the 
	 * fields (columns)  marked for the quicksearch feature.
	 * @param albumName The name of the album to which the index belongs. This name should NOT be escaped.
	 * @param columnNames The list of names of columns to be included in the index. Performs an automatic test to see if the column names 
	 * are quoted.  
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void createIndex(String albumName, List<String> columnNames) throws DatabaseWrapperOperationException{
		if (columnNames.isEmpty()) {
			return;
		}
		
		StringBuilder sqlStringbuiler = new StringBuilder("CREATE INDEX ");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(albumName + INDEX_NAME_SUFFIX));
		sqlStringbuiler.append(" ON ");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(albumName));
		sqlStringbuiler.append(" (");
		sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(columnNames.get(0)));		
		if (columnNames.size()>=2) {
			for (int i=1;i<columnNames.size(); i++) {
				sqlStringbuiler.append(", ");
				sqlStringbuiler.append(DatabaseStringUtilities.encloseNameWithQuotes(columnNames.get(i))); 
			}
		}		
		sqlStringbuiler.append(")");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sqlStringbuiler.toString())) {
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * Drops the first index associated to the given table name. 
	 * @param tableName The name of the table to which the index belongs.
	 * @return True if the table has no associated index to it. False if the operation failed.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void dropIndex(String tableName) throws DatabaseWrapperOperationException {
		String indexName = getTableIndexName(tableName);		
		if (indexName == null) {			
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
				
		String quotedIndexName = DatabaseStringUtilities.encloseNameWithQuotes(indexName);
		String sqlStatementString = "DROP INDEX IF EXISTS "+ quotedIndexName;
		
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		try (Statement statement = ConnectionManager.connection.createStatement()){			
			statement.execute(sqlStatementString);
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Adds the specified item to an existing album. Automatically sets a new contentVersion for the item if updateContentVersion flaag is set.
	 * Automatically links album item pictures to the album item by setting the foreign key
	 * @param item The album item to be added. 
	 * @param updateContentVersion True if the content version should be updated, this is the regular case for any content change. 
	 * False in case the the last contentVersion should be copied over, restore or alter table options make use of this. If the old content version
	 * should be copied over make sure the albumItem contains a content version!
	 * @return The ID of the newly added item.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static long addNewAlbumItem(AlbumItem item, boolean updateContentVersion) throws DatabaseWrapperOperationException {
		// Check if the item contains a albumName
		if (item.getAlbumName().isEmpty()) {
			LOGGER.error("Item {} has no albumName", item);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		
		// Check if specified album Item is valid
		if (!item.isValid()) {
			LOGGER.error("Item {} is invalid", item);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		// Check if content version should be carried over if yes ensure a content version is present
		if (updateContentVersion == false && item.getContentVersion() == null) {
			LOGGER.error("The option for carrying over the old content version is checked but no content version is found in the item!");
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		// Build the sql string with placeholders '?'
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(item.getAlbumName()));
		sb.append(" ( ");

		for (ItemField itemField : item.getFields()) {
			String name = itemField.getName();
			// Ensure that no field with the name of typeInfoColumnName
			if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {
				sb.append(DatabaseStringUtilities.encloseNameWithQuotes(name));
				sb.append(", ");
			}
		}

		// Add the typeInfoColumnName 
		sb.append(TYPE_INFO_COLUMN_NAME);
		sb.append(" ) VALUES ( ");

		for (ItemField itemField : item.getFields()) {
			String name = itemField.getName();
			if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {
				sb.append("?, ");
			}
		}
		// Add wildcard for the typeInfoColumn Value
		sb.append("? )");
		
		String savepointName = DatabaseIntegrityManager.createSavepoint();
	
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())){
			ResultSet generatedKeys = null;
			long idOfAddedItem = -1;

			// Replace the wildcard character '?' by the real type values
			int parameterIndex = 1;
			for (ItemField itemField : item.getFields()) {	
				String name = itemField.getName();
				if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {					
					setValueToPreparedStatement(preparedStatement, parameterIndex, itemField, item.getAlbumName());
					parameterIndex++;
				}
			}
			preparedStatement.setLong(parameterIndex, TYPE_INFO_FOREIGN_KEY);			

			// Retrieves the generated key used in the new  album item
			preparedStatement.executeUpdate();
			generatedKeys = preparedStatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				idOfAddedItem = generatedKeys.getLong(1);
			}
			
			// Store picture links
			if (item.getPictures() != null) {
				for (AlbumItemPicture picture : item.getPictures()) {
					picture.setAlbumItemID(idOfAddedItem);
					picture.setAlbumName(item.getAlbumName());
					addToPictureTable(picture);
				}
			}
			
			// Either copies the old content version over or generates a new one
			UUID newUUID = generateNewUUID();
			if (!updateContentVersion) {
				// Carry over old content version
				newUUID = item.getContentVersion();

			}
			updateContentVersion(item.getAlbumName(), idOfAddedItem, newUUID);
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			return idOfAddedItem;
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * This helper method sets a value based on type to a preparedStatement at the specified position
	 * @param preparedStatement The jdbc prepared statement to which the value is to be set.
	 * @param parameterIndex The index of the parameter to be set.
	 * @param field The field containing the value to be set as well as the according metadata.
	 * @param albumName The name of the album to which the item of the field belongs.
	 * @throws DatabaseWrapperOperationException Exception thrown if any part of the operation fails. 
	 */
	private static void setValueToPreparedStatement(PreparedStatement preparedStatement, int parameterIndex,  ItemField field, String albumName) throws DatabaseWrapperOperationException {
		try {
			switch (field.getType()) {
			case Text: 
				String text = field.getValue();
				preparedStatement.setString(parameterIndex, text);		
				break;
			case Number: 
				Double real = field.getValue();
				preparedStatement.setDouble(parameterIndex, real);		
				break;
			case Date: 
				Date date = field.getValue();
				preparedStatement.setDate(parameterIndex, date);		
				break;
			case Time: 
				Time time = field.getValue();
				preparedStatement.setTime(parameterIndex, time);		
				break;
			case Option: 
				OptionType option = field.getValue();
				// Textual representation of the enum is stored in the DB. 
				preparedStatement.setString(parameterIndex, option.toString());		
				break;
			case URL: 
				String	url = field.getValue();
				preparedStatement.setString(parameterIndex, url);		
				break;
			case StarRating: 
				StarRating	starRating = field.getValue();
				preparedStatement.setString(parameterIndex, starRating.toString());		
				break;
			case Integer: 
				Integer	integer = field.getValue();
				preparedStatement.setString(parameterIndex, integer.toString());		
				break;
			default:
				break;
			}			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
		
	}

	/**
	 * Updates all the fields of the specified item in the database using the values provided through item.
	 * @param albumItem The item to be updated.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void updateAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		// Check if the item contains a albumName
		if (albumItem.getAlbumName().isEmpty()) {
			LOGGER.error("Album item {} has no albumName", albumItem);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		// Get the id and make sure the field exist;
		ItemField idField = albumItem.getField("id");

		if (idField == null) {
			LOGGER.error("The album item {} which should be updated has no id field", albumItem);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		// Build the string with placeholders '?'
		StringBuilder sb = new StringBuilder("UPDATE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItem.getAlbumName()));
		sb.append(" SET ");

		// Add each field to be update by the query
		Iterator<ItemField> it = albumItem.getFields().iterator();
		boolean firstAppended = true;
		while (it.hasNext()) {
			ItemField next = it.next();
			// Exclude the id and fid fields
			if (!next.getType().equals(FieldType.ID)){
				if (!firstAppended) {
					sb.append(", ");

				}
				sb.append(DatabaseStringUtilities.encloseNameWithQuotes(next.getName()));
				sb.append("=? ");
				firstAppended = false;				
			}

		}
		sb.append("WHERE id=?");
		
		String savepointName =  DatabaseIntegrityManager.createSavepoint();		
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {
			// Replace the wildcards
			int parameterIndex = 1;
			for (ItemField next : albumItem.getFields()) {
				// Exclude the id and fid fields
				if (!next.getType().equals(FieldType.ID)){
					setValueToPreparedStatement(preparedStatement, parameterIndex, next, albumItem.getAlbumName());
					parameterIndex++;
				}
			}

			// Replace wildcard char '?' in WHERE id=? clause
			Long id = idField.getValue();
			preparedStatement.setString(parameterIndex, id.toString());
			preparedStatement.executeUpdate();

			// Get those physical picture files that are currently still referenced
			List<AlbumItemPicture> picturesBeforeUpdate = getAlbumItemPictures(albumItem.getAlbumName(), albumItem.getItemID());
			
			// Remove those physical pictures that are no longer needed. However, the table records will remain for the moment
			for (AlbumItemPicture stillReferencedPicture : picturesBeforeUpdate) {
				boolean pictureIsNoLongerNeeded = true;
				
				for (AlbumItemPicture albumItemPicture : albumItem.getPictures()) {
					if (stillReferencedPicture.getOriginalPictureName().equals(albumItemPicture.getOriginalPictureName())) {
						pictureIsNoLongerNeeded = false;
					}
				}
				
				if (pictureIsNoLongerNeeded) {
					FileSystemAccessWrapper.deleteFile(stillReferencedPicture.getThumbnailPicturePath());
					FileSystemAccessWrapper.deleteFile(stillReferencedPicture.getOriginalPicturePath());
				}
			}
			
			// Update picture table by first deleting all pictures for this album item, and then rewriting the references
			removeAllPicturesForAlbumItemFromPictureTable(albumItem);
			for (AlbumItemPicture albumItemPicture : albumItem.getPictures()) {				
				albumItemPicture.setAlbumItemID(albumItem.getItemID());
				addToPictureTable(albumItemPicture);
			}
			
			updateContentVersion(albumItem.getAlbumName(), id, generateNewUUID());
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			}
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);			
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}		
	}

	/**
	 * Permanently deletes the albumItem with the specified id from the database.
	 * @param albumName The name of the album to which the item belongs.
	 * @param albumItemId The id of the item to be deleted.
	 * @return  True if the operation was successful. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void deleteAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		// retrieve a list of the physical files to be deleted
		List<AlbumItemPicture> picturesToBeRemoved = getAlbumItemPictures(albumItem.getAlbumName(), albumItem.getItemID());
		
		// delete album item in table
		String deleteAlbumItemString = "DELETE FROM " + DatabaseStringUtilities.encloseNameWithQuotes(albumItem.getAlbumName()) + 
				" WHERE id=" + albumItem.getItemID();
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(deleteAlbumItemString)) {
			preparedStatement.executeUpdate();
			//TODO remove DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			
			// delete album pictures in picture table
			removeAllPicturesForAlbumItemFromPictureTable(albumItem);
			
			// delete physical files first
			for (AlbumItemPicture albumItemPicture : picturesToBeRemoved) {
				FileSystemAccessWrapper.deleteFile(albumItemPicture.getThumbnailPicturePath());
				FileSystemAccessWrapper.deleteFile(albumItemPicture.getOriginalPicturePath());
			}			
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}


	public static long getNumberOfItemsInAlbum(String albumName) throws DatabaseWrapperOperationException {
				
		try (Statement statement = ConnectionManager.connection.createStatement()){			
			ResultSet resultSet = statement.executeQuery(QueryBuilder.createCountAsAliasStarWhere(albumName, "numberOfItems"));
			if (resultSet.next()) {
				return resultSet.getLong("numberOfItems");
			}
			LOGGER.error("The number of items could not be fetch for album {}", albumName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, "The number of items could not be fetch for album " + albumName);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
	}

	private static void updateContentVersion(String albumName, long itemID, UUID newUuid) throws DatabaseWrapperOperationException {	
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		StringBuilder sb = new StringBuilder("UPDATE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumName));
		sb.append(" SET ");
		sb.append(CONTENT_VERSION_COLUMN_NAME);
		sb.append(" = ? ");
		sb.append("WHERE id = ?");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())){			
			preparedStatement.setString(1, newUuid.toString());
			preparedStatement.setLong(2, itemID);
			preparedStatement.executeUpdate();
		}catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	private static void updateSchemaVersion(String albumName) throws DatabaseWrapperOperationException  {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		String typeInfoTableName = getTypeInfoTableName(albumName);
		StringBuilder sb = new StringBuilder("UPDATE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(typeInfoTableName));
		sb.append(" SET ");
		sb.append(SCHEMA_VERSION_COLUMN_NAME);
		sb.append(" = ?");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {		
			preparedStatement.setString(1, generateNewUUID().toString());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}

	/**
	 * Executes an SQL query against the database.
	 * @param sqlStatement The SQL statement to be executed. Must be proper SQL compliant to the database.
	 * @param albumName The name of the album to which the query refers to.
	 * @return The albumItemResultSet which represent the results of the query. Null if the query fails at any point.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement, String albumName) throws DatabaseWrapperOperationException{
		AlbumItemResultSet albumItemRS = null;
	
		Map<Integer, MetaItemField> metaInfoMap = DatabaseWrapper.getAlbumItemMetaMap(albumName);
		
		albumItemRS = new AlbumItemResultSet(ConnectionManager.connection, sqlStatement, metaInfoMap);
		return albumItemRS;
	}

	/**
	 * Simply executes the provided sql query via the connection against a database and returns the results.
	 * @param sqlStatement An sql query, typically a SELECT statement like SELECT * FROM albumName.
	 * @return A resultSet containing the desired entries. Null if the query failed. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItemResultSet executeSQLQuery(String sqlStatement) throws DatabaseWrapperOperationException{
		AlbumItemResultSet albumItemRS = null;
		try {
			albumItemRS = new AlbumItemResultSet(ConnectionManager.connection, sqlStatement);
			return albumItemRS;
		} catch (DatabaseWrapperOperationException e) {
			LOGGER.error("The query: \"{}\" could not be executed and terminated with message: {}", sqlStatement ,  e.getMessage());
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
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
		List<MetaItemField> albumFields = getAllAlbumItemMetaItemFields(albumName);
		String query = "";
		ArrayList<QueryComponent> queryFields = null;
		List<String> quicksearchFieldNames = getIndexedColumnNames(albumName);

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
				// Only take quicksearchable fields into account
				if (field.isQuickSearchable()) {
					if (field.getType().equals(FieldType.Text)) {
						queryFields.add(QueryBuilder.getQueryComponent(field.getName(), QueryOperator.like, term));
					} else {
						queryFields.add(QueryBuilder.getQueryComponent(field.getName(), QueryOperator.equals, term));
					}
				}
			}// end of for - fields
			if (queryFields != null && !queryFields.isEmpty()) {
				query += QueryBuilder.buildQuery(queryFields, false, albumName);
			}

		}// end of for - terms

		return executeSQLQuery(query, albumName);
	}

	/**
	 * Lists all albums currently stored in the database.
	 * @return A list of album names. May be empty if no albums were created yet. Null in case of an error.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<String> listAllAlbums() throws DatabaseWrapperOperationException {
		List<String> albumList = new ArrayList<String>();
		String queryAllAlbumsSQL = QueryBuilder.createSelectColumnQuery(ALBUM_MASTER_TABLE_NAME, ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);

		try (	Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = statement.executeQuery(queryAllAlbumsSQL);) {			
			

			while(rs.next()) {
				albumList.add(rs.getString(1));
			}
			return albumList;
			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/** 
	 * Retrieves the name of the table containing the type information about it.
	 * @param mainTableName The name of the table of which the type information belongs to.
	 * @return The name of the related typeInfo table.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static String getTypeInfoTableName(String mainTableName) throws DatabaseWrapperOperationException  {
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata = ConnectionManager.connection.getMetaData();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
		
		try (ResultSet dbmetars = dbmetadata.getImportedKeys(null, null, mainTableName)){			
			if (dbmetars.next()) {
				String typeInfoTable = dbmetars.getString("PKTABLE_NAME");

				return typeInfoTable;
			}
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, "No type info table found for "+mainTableName);
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 * Lists all the columns of a table which are indexed. Indexed columns are also taken into account for the quicksearch feature.
	 * @param tableName The name of the table to which the columns belong. Table name must NOT be escaped! 
	 * @return A list of indexed, meaning also quickSearchable, columns. List may be empty if none were indexed. Null if an error occured. 
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<String> getIndexedColumnNames(String tableName) throws DatabaseWrapperOperationException {
		List<String> indexedColumns = new ArrayList<String>();
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata = ConnectionManager.connection.getMetaData();			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
				
		try (ResultSet indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true)) {	
			while (indexRS.next()) {
				if(indexRS.getString("COLUMN_NAME") != null)
					indexedColumns.add(indexRS.getString("COLUMN_NAME"));
			}
			return indexedColumns;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 * Retrieves the name of the index. In case there are multiple indices only the first one is looked up. Multiple indices may indicate 
	 * database insistency.
	 * @param tableName The name of the table to which the index belongs.
	 * @return The name of the index table if it exists, null otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static String getTableIndexName(String tableName) throws DatabaseWrapperOperationException {		
		String indexName = null;
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata =  ConnectionManager.connection.getMetaData();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
		
		try (ResultSet indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true);) {			
			if(indexRS.next()) {
				indexName = indexRS.getString("INDEX_NAME");
			}
			return indexName;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 * Retrieves a list of MetaItemFields, excluding those that are for internal use only. Meta item fields describe the items of the album
	 * @param albumName The name of the album of which to retrieve the information.
	 * @return The list of MetaItemFields. 
	 */
	public static List<MetaItemField> getAlbumItemFieldNamesAndTypes(String albumName) throws DatabaseWrapperOperationException {
		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();

		// Is available means that it does not exist in the db, hence its fields cannot be retrieved
		if (albumNameIsAvailable(albumName)) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);
		List<String> internalColumnNames = Arrays.asList("id", TYPE_INFO_COLUMN_NAME, CONTENT_VERSION_COLUMN_NAME);
		String dbAlbumName = DatabaseStringUtilities.encloseNameWithQuotes(albumName);
		
		try (
				Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = statement.executeQuery(QueryBuilder.createSelectStarQuery(dbAlbumName));) {		

			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// Each ItemField. Classic for loop since meta data only provides access via indices.
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {

				//Excludes all columns that are for internal use only.
				String columnName = metaData.getColumnName(columnIndex);
				if (!internalColumnNames.contains(columnName)) {
					FieldType type = detectDataType(albumName, columnName);
					MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
					itemMetadata.add(metaItem);
				}
			}
			
			return itemMetadata;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 *  Retrieves a list of all MetaItemFields, including those that are for internal use only. Meta item fields describe the items of the album.
	 * @param albumName The name of the album of which to retrieve the information.
	 * @return The list of MetaItemFields. Return an empty  list if a structural error exists in the database. Null when an internal SQL error occurred.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static List<MetaItemField> getAllAlbumItemMetaItemFields(String albumName) throws DatabaseWrapperOperationException{
		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();
		List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);
		try (
			Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(QueryBuilder.createSelectStarQuery(albumName));) {					

			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// Each ItemField
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {

				String columnName = metaData.getColumnName(columnIndex);
				FieldType type = detectDataType(albumName, columnName);
				MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
				itemMetadata.add(metaItem);
			}
			return itemMetadata;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 * Fetches a map of metaItemFields keyed by their field (column) index. 
	 * @param albumName The name of the album to which this map belongs.
	 * @return True if the query was successful. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Map<Integer, MetaItemField> getAlbumItemMetaMap(String albumName) throws DatabaseWrapperOperationException {
		List<String> quickSearchableColumns = getIndexedColumnNames(albumName);

		Map<Integer, MetaItemField> itemMetadata = new HashMap<Integer, MetaItemField>();
		
		try (Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY)) {			
			// Retrieve table metadata
						
			ResultSet set = statement.executeQuery(QueryBuilder.createSelectStarQuery(albumName)); 	
			ResultSetMetaData metaData = set.getMetaData();
			
			int columnCount = metaData.getColumnCount();
			// Each ItemField
			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
				String name = metaData.getColumnName(columnIndex);
				
				FieldType type = detectDataType(albumName, name);	
				
				MetaItemField metaItemField = new MetaItemField(name, type,quickSearchableColumns.contains(name));
				itemMetadata.put(columnIndex, metaItemField);
			}
			return itemMetadata;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
	}

	/**
	 * A helper method to detect the collector FieldType using the type information in the separate typeInfo table.
	 * @param tableName The name of the table to which the column belongs to.
	 * @param columnName The name of the column whose type should be determined.
	 * @return A FieldType expressing the type of the specified column in the resultSet.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static FieldType detectDataType(String tableName, String columnName) throws DatabaseWrapperOperationException {
		DatabaseMetaData dbmetadata = null;
		try {
			dbmetadata = ConnectionManager.connection.getMetaData();
		} catch (SQLException e1) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		try (ResultSet dbmetars = dbmetadata.getImportedKeys(null, null, tableName)) {
			// Get the primary and foreign keys
			String primaryKey = dbmetars.getString(4);
			String foreignKey = dbmetars.getString(8);
			
			if (columnName.equalsIgnoreCase(primaryKey) || columnName.equalsIgnoreCase(foreignKey)) {
				return FieldType.ID; 
			}
			
		} catch (SQLException sqlException) {
			return FieldType.Text;
		}	

		String dbtypeInfoTableName = DatabaseStringUtilities.encloseNameWithQuotes(getTypeInfoTableName(tableName));
		try (
				Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet typeResultSet = statement.executeQuery(QueryBuilder.createSelectColumnQuery(dbtypeInfoTableName, columnName));) {			
			return FieldType.valueOf(typeResultSet.getString(1));
			
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		} catch (IllegalArgumentException e) {
			return FieldType.Text;
		}	
		
	}


	/**
	 * Fetches the value of an item field.
	 * @param results The result set pointing to the field whose value is to be fetched.
	 * @param columnIndex The index of the field (column) whithin the item (entry).
	 * @param type The type of the field.
	 * @param albumName The name of the album this value of the item belongs to.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static Object fetchFieldItemValue(ResultSet results, int columnIndex, FieldType type, String albumName) throws DatabaseWrapperOperationException {
		Object value = null;
		try {
			switch (type) {
			case ID:
				value = results.getLong(columnIndex);
				break;
			case Text:
				value = results.getString(columnIndex);
				break;
			case Number:
				value = results.getDouble(columnIndex);
				break;
			case Integer:
				value = results.getInt(columnIndex);
				break;
			case Date:
				value = results.getDate(columnIndex);
				break;
			case Time:
				value = results.getTime(columnIndex);
				break;
			case Option:
				String optionValue = results.getString(columnIndex);
				if (optionValue != null && !optionValue.isEmpty()) {
					value =  OptionType.valueOf(optionValue);
				}else {
					LOGGER.error("Fetching option type for item field failed. Url string is unexpectantly null or empty");
				}
				break;
			case URL:
				String urlString = results.getString(columnIndex);
				value =  urlString;
				break;
			case StarRating:
				String rating = results.getString(columnIndex);
				if (rating != null && !rating.isEmpty()) {
					value =  StarRating.valueOf(rating);
				}else {
					LOGGER.error("Fetching star ratingfor item field failed.- star rating string is unexpectantly null or empty");
				}
				break;
			case UUID:
				value  = UUID.fromString(results.getString(columnIndex));
				break;
			default:
				// FieldType by is default of type text
				value = null;
				break;
			}
			return value;
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
	}

	/**
	 * Queries whether the field is available for the quicksearch feature.
	 * @param albumName The name of the album to which the field belongs to.
	 * @param fieldName The name of the field to be queried.
	 * @return True if the the specified field is available for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumFieldQuicksearchable(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		List<String> quicksearchableFieldNames = getIndexedColumnNames(albumName);

		if (quicksearchableFieldNames != null) {
			return quicksearchableFieldNames.contains(fieldName);
		}
		return false;
	}

	/**
	 * Queries whether the specified album contains at least one quicksearchable field.
	 * @param albumName The name of the album to be queried.
	 * @return True if the the specified album contains at least a single field enabled for the quicksearch feature. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean isAlbumQuicksearchable(String albumName) throws DatabaseWrapperOperationException {
		List<String> quicksearchableFieldNames = getIndexedColumnNames(albumName);

		if (quicksearchableFieldNames.size()>=1){
			return true;
		}else {
			return false;
		}	
	}

	/**
	 * Tests if the album name is not already in use by another album.
	 * @param requestedAlbumName The proposed album name to be tested of availability.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean albumNameIsAvailable(String requestedAlbumName) throws DatabaseWrapperOperationException {
		for (String albumName : DatabaseWrapper.listAllAlbums()) {
			if (albumName.equalsIgnoreCase(requestedAlbumName)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests if the proposed item field name is not already in use by another field of the same album.
	 * @param albumName The album name that contains the fields that the name is checked against.
	 * @return True if the name can be inserted into the database. False otherwise.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static boolean itemFieldNameIsAvailable(String albumName, String requestedFieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName)) {
			if (requestedFieldName.equalsIgnoreCase(metaItemField.getName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fetches an album item by its id and album name.
	 * @param albumName The name of the album to which this item belongs to.
	 * @param albumItemId The unique id of the item within the album.
	 * @return The requested albumItem. Null if no item with the specified id was found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static AlbumItem fetchAlbumItem(String albumName, long albumItemId) throws DatabaseWrapperOperationException {
		String queryString =  QueryBuilder.createSelectStarQuery(albumName)+ " WHERE id=" + albumItemId;
		List<AlbumItem> items = fetchAlbumItemsFromDatabase(queryString);

		AlbumItem requestedItem = null;
		try {
			requestedItem = items.get(0);
			return requestedItem;
		} catch (IndexOutOfBoundsException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}

	/**
	 * Retrieves a list of albumItems from the database.
	 * @param queryString The proper SQL query string compliant with the database. 
	 * @return The list albumItems making up the results of the query. An empty list if no matching album item were found.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static List<AlbumItem> fetchAlbumItemsFromDatabase(String queryString) throws DatabaseWrapperOperationException {

		LinkedList<AlbumItem> list = new LinkedList<AlbumItem>();		
		
		try (
				Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				ResultSet rs = statement.executeQuery(queryString); ){
	
			// Retrieve table metadata
			ResultSetMetaData metaData = rs.getMetaData();

			int columnCount = metaData.getColumnCount();
			// For each albumItem
			while (rs.next()) {
				// Create a new AlbumItem instance
				AlbumItem item = new AlbumItem("");
				// Each ItemField
				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {

					// Add new field
					String fieldName = metaData.getColumnName(columnIndex);
					String albumName = metaData.getTableName(1);
					item.setAlbumName(albumName);
					FieldType type = detectDataType(albumName, fieldName);
					Object value = fetchFieldItemValue(rs, columnIndex, type, albumName);
					boolean quicksearchable = isAlbumFieldQuicksearchable(albumName, fieldName);
					// omit the typeinfo field and set the contentVersion separately
					if (type == FieldType.ID && fieldName.endsWith(TYPE_INFO_COLUMN_NAME)){
						continue;
					}else if (type.equals(FieldType.UUID) && fieldName.equals(CONTENT_VERSION_COLUMN_NAME)) {
						item.setContentVersion((UUID) value);
					}else {
						item.addField(fieldName, type, value, quicksearchable);
					}
				}
				list.add(item);
			}

		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState,e);
		}

		return list;
	}	

	/**
	 * Updates a table entry with a default value for the specific type of that column.
	 * @param tableName The name of the table which will be updated.
	 * @param columnMetaInfo The metadata specifying the name and type of the column entry to be updated.
	 * @throws DatabaseWrapperOperationException 
	 */
	private static void updateTableColumnWithDefaultValue(String tableName, MetaItemField columnMetaInfo) throws DatabaseWrapperOperationException {		
		String sqlString = "UPDATE "+ DatabaseStringUtilities.encloseNameWithQuotes(tableName)+ " SET " +DatabaseStringUtilities.encloseNameWithQuotes(columnMetaInfo.getName()) + "=?";
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sqlString)){						
			
			switch (columnMetaInfo.getType()) {
			case Text: 
				preparedStatement.setString(1, (String) columnMetaInfo.getType().getDefaultValue());
				break;
			case Number: 
				preparedStatement.setDouble(1, (Double) columnMetaInfo.getType().getDefaultValue());
				break;
			case Integer: 
				preparedStatement.setInt(1, (Integer) columnMetaInfo.getType().getDefaultValue());
				break;
			case Date: 
				preparedStatement.setDate(1, (Date) columnMetaInfo.getType().getDefaultValue());
				break;
			case Time:
				preparedStatement.setTime(1, (Time) columnMetaInfo.getType().getDefaultValue());
				break;
			case Option: 
				String option = columnMetaInfo.getType().getDefaultValue().toString();
				preparedStatement.setString(1, option);
				break;
			case URL: 
				String url = columnMetaInfo.getType().getDefaultValue().toString();
				preparedStatement.setString(1, url);
				break;
			case StarRating: 
				String rating = columnMetaInfo.getType().getDefaultValue().toString();
				preparedStatement.setString(1, rating);
				break;
			default:
				break;
			}
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	}

	public static boolean isDateField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.Date)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOptionField(String albumName, String fieldName) throws DatabaseWrapperOperationException {
		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.Option)) {
				return true;
			}
		}
		return false;
	}

	static void createAlbumMasterTableIfNotExits() throws DatabaseWrapperOperationException  {
		List<MetaItemField> fields = new ArrayList<MetaItemField>();
		
		// Add the table name column
		fields.add(new MetaItemField(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE, FieldType.Text));
		// Add the table's type info table name column
		fields.add(new MetaItemField(TYPE_TABLENAME_ALBUM_MASTER_TABLE, FieldType.Text));
		// Add the table's picture state column indicating if the album structure has pictures or not
		fields.add(new MetaItemField(PICTURE_COLUMN_NAME_IN_ALBUM_MASTER_TABLE, FieldType.Option));		
		// Create the album master table.
		createTableWithIdAsPrimaryKey(ALBUM_MASTER_TABLE_NAME, fields , false, true);
	}

	private static void addNewAlbumToAlbumMasterTable(String albumTableName, String albumTypeInfoTableName, boolean hasPictures) throws DatabaseWrapperOperationException {		
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(ALBUM_MASTER_TABLE_NAME));
		sb.append(" (");
		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
		sb.append(", ");
		sb.append(TYPE_TABLENAME_ALBUM_MASTER_TABLE);		
		sb.append(", ");
		sb.append(PICTURE_COLUMN_NAME_IN_ALBUM_MASTER_TABLE);
		sb.append(") VALUES( ?, ?, ?)");

		
		String registerNewAlbumToAlbumMasterableString = sb.toString();

		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(registerNewAlbumToAlbumMasterableString)){			
			// New album name
			preparedStatement.setString(1, DatabaseStringUtilities.removeEnclosingNameWithQuotes(albumTableName));
			// New type info name
			preparedStatement.setString(2, DatabaseStringUtilities.removeEnclosingNameWithQuotes(albumTypeInfoTableName));
			// New album contains picture flag
			OptionType hasPictureFlag = hasPictures ? OptionType.YES : OptionType.NO ; 
			preparedStatement.setString(3, hasPictureFlag.toString());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	private static void removeAlbumFromAlbumMasterTable(String albumTableName) throws DatabaseWrapperOperationException  {
		StringBuilder sb = new StringBuilder("DELETE FROM ");	
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(ALBUM_MASTER_TABLE_NAME));
		sb.append(" WHERE ");
		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
		sb.append(" = ?");

		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();		

		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(unRegisterNewAlbumFromAlbumMasterableString)){  			
			// WHERE album name
			preparedStatement.setString(1, albumTableName);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * 
	 * @param oldAlbumTableName
	 * @param newAlbumTableName
	 * @param newAlbumTypeInfoTableName
	 * @param newHasPicturesFlag OptionType.UNKNOWN will be ignored. Yes and no will be set accordingly
	 * @throws DatabaseWrapperOperationException
	 */
	private static void modifyAlbumInAlbumMasterTable(String oldAlbumTableName, String newAlbumTableName, String newAlbumTypeInfoTableName, OptionType newHasPicturesFlag) throws DatabaseWrapperOperationException  {

		StringBuilder sb = new StringBuilder("UPDATE ");		
		sb.append(ALBUM_MASTER_TABLE_NAME);
		sb.append(" SET ");
		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
		sb.append(" = ?, ");
		sb.append(TYPE_TABLENAME_ALBUM_MASTER_TABLE);
		sb.append(" = ?, ");
		if (newHasPicturesFlag != OptionType.UNKNOWN) {
			sb.append(PICTURE_COLUMN_NAME_IN_ALBUM_MASTER_TABLE);
			sb.append(" = ? ");
		}
		sb.append("WHERE ");
		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
		sb.append(" = ?");

		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();

		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(unRegisterNewAlbumFromAlbumMasterableString);){			
			// New album name
			preparedStatement.setString(1, newAlbumTableName);
			// New type info name
			preparedStatement.setString(2, newAlbumTypeInfoTableName);
			if (newHasPicturesFlag != OptionType.UNKNOWN) {
				// New hasPictures flag
				preparedStatement.setString(3, newHasPicturesFlag.toString());				
				// Where old album name
				preparedStatement.setString(4, oldAlbumTableName);
			}else {		
				// Where old album name
				preparedStatement.setString(3, oldAlbumTableName);
			}
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * Helper function to create a new table in the database. Only if ALL of the foreign key parameters are valid, a foreign key column will be created.
	 * @param tableName The name of the table to be created. It is not necessary for it to be enclosed by quotes.
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
		sb.append(ID_COLUMN_NAME);
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
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(createTableString);){			
			preparedStatement.executeUpdate();
		} catch (Exception e) {			
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	public static void createPictureTable (String albumName) throws DatabaseWrapperOperationException {		
		List<MetaItemField> columns = new ArrayList<MetaItemField>();
		// The filename of the original picture 
		columns.add(new MetaItemField(DatabaseStringUtilities.encloseNameWithQuotes(ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE), FieldType.Text));
		// The filename of the generated thumbnail picture
		columns.add(new MetaItemField(DatabaseStringUtilities.encloseNameWithQuotes(THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE), FieldType.Text));
		// The id of the album item the picture belongs to
		columns.add(new MetaItemField(DatabaseStringUtilities.encloseNameWithQuotes(ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE), FieldType.ID));
	
		
		createTableWithIdAsPrimaryKey(DatabaseStringUtilities.encloseNameWithQuotes(albumName + "_" + PICTURE_TABLE_SUFFIX), columns , false, true);
	}
	
	private static void addToPictureTable(AlbumItemPicture albumItemPicture) throws DatabaseWrapperOperationException {
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItemPicture.getAlbumName() + "_" + PICTURE_TABLE_SUFFIX));
		sb.append(" ( ");

		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE));
		
		sb.append(") VALUES( ");

		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItemPicture.getOriginalPictureName()) + ", ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItemPicture.getThumbnailPictureName()) + ", ");
		sb.append(albumItemPicture.getAlbumItemID());

		sb.append(" ) ");
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())){			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	public static List<AlbumItemPicture> getAlbumItemPictures(String albumName, long albumItemID) throws DatabaseWrapperOperationException{
		List<AlbumItemPicture> pictures = new ArrayList<AlbumItemPicture>();
		
		if (albumHasPictureField(albumName)) {
			String picturesQuery = 
				   " SELECT " +
						ID_COLUMN_NAME + ", " +
						THUMBNAIL_PICTURE_FILE_NAME_IN_PICTURE_TABLE + ", " +
						ORIGINAL_PICTURE_FILE_NAME_IN_PICTURE_TABLE + ", " +
						ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE +
				   " FROM " + DatabaseStringUtilities.encloseNameWithQuotes(albumName + "_" + PICTURE_TABLE_SUFFIX) +
				   " WHERE " + ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE + " = " + String.valueOf(albumItemID) + ";";
	
			try (Statement statement = ConnectionManager.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				 ResultSet rs = statement.executeQuery(picturesQuery);) {			
			
				while (rs.next()) {
					pictures.add(new AlbumItemPicture(rs.getLong(1), rs.getString(2), rs.getString(3), albumName, rs.getLong(4)));
				}			
			} catch (SQLException e) {
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		}
		
		return pictures;
	}
	
	/** Removes all picture records from the picture table for the given album item
	 * ATTENTION: this method does no delete the physical files!
	 * @param albumItem the album item for which all picture records should be deleted */
	public static void removeAllPicturesForAlbumItemFromPictureTable(AlbumItem albumItem) throws DatabaseWrapperOperationException {		
		StringBuilder sb = new StringBuilder("DELETE FROM ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumItem.getAlbumName() + "_" + PICTURE_TABLE_SUFFIX));
		sb.append(" WHERE ");
		sb.append(ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE + " = " + albumItem.getItemID());
				
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {						
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
	
	/** Drops the picture table of the specified album
	 * ATTENTION: this method does no delete the physical files!
	 * @param albumName the album name for which the picture table should be droped */
	public static void dropPictureTableForAlbum(String albumName) throws DatabaseWrapperOperationException {
		StringBuilder sb = new StringBuilder("DROP TABLE ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(albumName + "_" + PICTURE_TABLE_SUFFIX));
		
		try (PreparedStatement preparedStatement = ConnectionManager.connection.prepareStatement(sb.toString())) {						
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
}
