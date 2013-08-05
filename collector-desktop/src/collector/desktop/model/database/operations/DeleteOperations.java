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

package collector.desktop.model.database.operations;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.managers.ConnectionManager;
import collector.desktop.controller.managers.DatabaseIntegrityManager;
import collector.desktop.model.album.AlbumItem;
import collector.desktop.model.album.AlbumItemPicture;
import collector.desktop.model.album.FieldType;
import collector.desktop.model.album.MetaItemField;
import collector.desktop.model.database.DatabaseStringUtilities;
import collector.desktop.model.database.QueryBuilder;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;

public class DeleteOperations {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteOperations.class);
	
	static void removeAlbumItemField(String albumName, MetaItemField metaItemField) throws DatabaseWrapperOperationException {
		// Check if the specified columns exists.
		List<MetaItemField> metaInfos = QueryOperations.getAllAlbumItemMetaItemFields(albumName);
		if (!metaInfos.contains(metaItemField)) {
			if (metaInfos.contains(new MetaItemField(metaItemField.getName(), metaItemField.getType(), !metaItemField.isQuickSearchable()))){
				LOGGER.error("The specified meta item field's quicksearch flag is not set appropriately!");
			} else {
				LOGGER.error("The specified meta item field is not part of the album");
			}
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		
		// Backup the old data in java objects
		List<AlbumItem> albumItems = QueryOperations.getAlbumItems(QueryBuilder.createSelectStarQuery(albumName));
		
		// Create the new table pointing to new typeinfo
		boolean keepPictureField = QueryOperations.isPictureAlbum(albumName);
		List<MetaItemField> newFields = QueryOperations.getAlbumItemFieldNamesAndTypes(albumName);
		newFields = UpdateOperations.removeFieldFromMetaItemList(metaItemField, newFields);

		// remember savepoint
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		try {
			// Drop the old table and typeTable
			removeAlbum(albumName);
		
			// The following three columns are automatically created by createNewAlbumTable
			newFields = UpdateOperations.removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
			newFields = UpdateOperations.removeFieldFromMetaItemList(new MetaItemField(DatabaseConstants.TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
			CreateOperations.createNewAlbumTable(newFields, albumName, DatabaseStringUtilities.encloseNameWithQuotes(
					DatabaseStringUtilities.generateTableName(albumName)), keepPictureField);
			
			// Restore the old data from the java objects
			List<AlbumItem> newAlbumItems = removeFieldFromAlbumItemList(metaItemField, albumItems);
			for (AlbumItem albumItem : newAlbumItems) {
				albumItem.setAlbumName(albumName);
				CreateOperations.addAlbumItem(albumItem, false, false);
			}
	
			UpdateOperations.rebuildIndexForTable(albumName, newFields);
			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
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
	
	static void removeAlbumAndAlbumPictures(String albumName) throws DatabaseWrapperOperationException {
		removeAlbum(albumName);
		removeAlbumPictures(albumName);
	}
	
	/**
	 * Permanently removes an album along with its typeInfo metadata
	 * @param albumName The name of the album which is to be removed
	 * @throws DatabaseWrapperOperationException 
	 */
	static void removeAlbum(String albumName) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {	
			String typeInfoTableName = DatabaseStringUtilities.generateTypeInfoTableName(albumName);
			
			dropTable(DatabaseStringUtilities.generateTableName(albumName));
			dropTable(typeInfoTableName);
			
			UpdateOperations.removeAlbumFromAlbumMasterTable(albumName); 
			
			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
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
	 * Removes the album pictures for the given album
	 * @param albumName the album for which the pictures should be removed
	 * @throws DatabaseWrapperOperationException
	 */
	static void removeAlbumPictures(String albumName) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		try {
			dropTable(DatabaseStringUtilities.generatePictureTableName(albumName));
			
			FileSystemAccessWrapper.deleteDirectoryRecursively(
					new File(FileSystemAccessWrapper.getFilePathForAlbum(albumName)));
		} catch (DatabaseWrapperOperationException e) {
			if (e.ErrorState.equals(DBErrorState.ErrorWithDirtyState)) {
				DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
			}
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}
	
	/** Removes all picture records from the picture table for the given album item
	 * ATTENTION: this method does no delete the physical files!
	 * @param albumItem the album item for which all picture records should be deleted */
	static void removeAllPicturesForAlbumItemFromPictureTable(AlbumItem albumItem) throws DatabaseWrapperOperationException {		
		StringBuilder sb = new StringBuilder("DELETE FROM ");
		sb.append(DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generatePictureTableName(albumItem.getAlbumName())));
		sb.append(" WHERE ");
		sb.append(DatabaseConstants.ALBUM_ITEM_ID_REFERENCE_IN_PICTURE_TABLE + " = " + albumItem.getItemID());
				
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(sb.toString())) {						
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
	
	/**
	 * Drops a table if it exists. No error or side effects if it does not exist.
	 * @param tableName The name of the table which is to be dropped.
	 * @throws DatabaseWrapperOperationException 
	 */
	static void dropTable(String tableName) throws DatabaseWrapperOperationException  {
		try (Statement statement = ConnectionManager.getConnection().createStatement()){		
			statement.execute("DROP TABLE IF EXISTS " + tableName);
		} catch (Exception e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
	
	/**
	 * Drops the first index associated to the given table name. 
	 * @param tableName The name of the table to which the index belongs.
	 * @return true if the table has no associated index to it. false if the operation failed.
	 * @throws DatabaseWrapperOperationException 
	 */
	static void dropIndex(String tableName) throws DatabaseWrapperOperationException {
		String indexName = QueryOperations.getTableIndexName(tableName);		
		
		// null indicates that no index was there to drop
		if (indexName == null) {
			return;
		}
				
		String quotedIndexName = DatabaseStringUtilities.encloseNameWithQuotes(indexName);
		String sqlStatementString = "DROP INDEX IF EXISTS " + quotedIndexName;
		
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		try (Statement statement = ConnectionManager.getConnection().createStatement()){			
			statement.execute(sqlStatementString);
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}
	
	static void deleteAlbumItem(AlbumItem albumItem) throws DatabaseWrapperOperationException {
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		// retrieve a list of the physical files to be deleted
		List<AlbumItemPicture> picturesToBeRemoved = QueryOperations.getAlbumItemPictures(albumItem.getAlbumName(), albumItem.getItemID());
		
		// delete album item in table
		String deleteAlbumItemString = "DELETE FROM " + DatabaseStringUtilities.encloseNameWithQuotes(
				DatabaseStringUtilities.generateTableName(albumItem.getAlbumName())) + " WHERE id=" + albumItem.getItemID();
		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(deleteAlbumItemString)) {
			preparedStatement.executeUpdate();
			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			
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
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}
	
	/** Use this method to clear the picture table of the specified album 
	 * as well as the corresponding picture folder on the filesystem */
	static void clearPictureTable(String albumName) throws DatabaseWrapperOperationException {
		String clearPictureTableQuery = "DELETE FROM " + 
				DatabaseStringUtilities.encloseNameWithQuotes(DatabaseStringUtilities.generatePictureTableName(albumName));
		String savepointName = DatabaseIntegrityManager.createSavepoint();
		
		try (PreparedStatement preparedStatement = ConnectionManager.getConnection().prepareStatement(clearPictureTableQuery)) {		
			preparedStatement.executeUpdate();
			DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			
			FileSystemAccessWrapper.deleteDirectoryRecursively(new File(FileSystemAccessWrapper.getFilePathForAlbum(albumName)));			
		} catch (SQLException e) {
			DatabaseIntegrityManager.rollbackToSavepoint(savepointName);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		} finally {
			DatabaseIntegrityManager.releaseSavepoint(savepointName);
		}
	}
}
