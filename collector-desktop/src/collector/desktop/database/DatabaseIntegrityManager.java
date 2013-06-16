package collector.desktop.database;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class DatabaseIntegrityManager {
	/**The normal logger for all info, debug, error and warning in this class*/
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseIntegrityManager.class);
	/** The extension used on file names for autosaves.*/
	private static final String AUTO_SAVE_EXTENSION = "autosave";
	/** Regular expression describing the file name format including the extension of auto saves.*/
	private  static final String AUTO_SAVE_FILE_REGEX = "(\\w)+(\\u005F([0-9]+)+\\."+AUTO_SAVE_EXTENSION+")$";
	static final String corruptDatabaseSnapshotPrefix = ".corruptDatabaseSnapshot_";
	/** The maximum amount of autosaves that can be stored until the oldes it overwritten */
	private static int autoSaveLimit = 8;
	private static long lastChangeTimeStamp = -1;
	
	/**
	 * Creates a savepoint to which the database state can be rolled back to. A new transaction is started.
	 * @return The name of the created savepoint or null in case of failure. The create savepoint should only be used in public methods to avoid ovehead
	 * with nested savepoints. 
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static String createSavepoint() throws FailedDatabaseWrapperOperationException  {
		String savepointName = UUID.randomUUID().toString();
	
		try (PreparedStatement createSavepointStatement = ConnectionManager.connection.prepareStatement("SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName));){			
			createSavepointStatement.execute();
			return savepointName;
		} catch (SQLException e) {
			LOGGER.error("Creating the savepoint {} failed", savepointName);
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}		
	}
	/**
	 * Removes the savepoint from the internal stack such that it cannot be used for any future rollbacks.
	 * If the transaction stack is empty all changes done since the creation of the saveopoint will be committed.
	 * If the stack of transaction is not empty (i.e. inner transaction) then no changes are comitted but the savepoint
	 * is removed nonetheless from the stack. The release should only be used in public methods to avoid ovehead
	 * with nested savepoints. 
	 * @param The name of the savepoint to be released. Must not be null or empty! 
	 * @throws FailedDatabaseWrapperOperationException If the errorstate within is ErrorWithDirtyState that means the release was not possible 
	 */
	public static void releaseSavepoint(String savepointName) throws FailedDatabaseWrapperOperationException {
	
		if (savepointName == null || savepointName.isEmpty()){
			LOGGER.error("The savepoint could not be released since the name string is null or empty");
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
		
		try (PreparedStatement releaseSavepointStatement = ConnectionManager.connection.prepareStatement("RELEASE SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName));){			
			releaseSavepointStatement.execute();
		} catch (SQLException e) {
			LOGGER.error("Releasing the savepoint {} failed", savepointName);
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}
	/**
	 * The rollback should only be used in public methods to avoid ovehead with nested savepoints. 
	 * @param savepointName
	 * * @throws FailedDatabaseWrapperOperationException If the errorstate within is ErrorWithDirtyState that means the release was not possible
	 */
	public static void rollbackToSavepoint(String savepointName) throws FailedDatabaseWrapperOperationException{
	
		if (savepointName == null || savepointName.isEmpty()){
			LOGGER.error("The savepoint could not be rolledback to since the name string is null or empty");
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}	
	
		try (PreparedStatement rollbackToSavepointStatement = ConnectionManager.connection.prepareStatement("ROLLBACK TO SAVEPOINT " + DatabaseStringUtilities.encloseNameWithQuotes(savepointName))){
			rollbackToSavepointStatement.execute();
		} catch (SQLException e) {
			LOGGER.error("Rolling back the savepoint {} failed", savepointName);
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	}
	/**
	 * Backs the database entries along the properties and pictures up to the specified file.
	 * @param filePath The path ending with the file name under which the backup will be stored.
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static void backupToFile(String filePath) throws FailedDatabaseWrapperOperationException {
		//TODO: check for 'file rollback' or simple delete of any remnants	
		//TODO: PIC check if the new picture file structure is backed up correctly. Especially with separate thumbnail folder
		String tempDirName=  java.util.UUID.randomUUID().toString();
		File tempDir = new File(System.getProperty("user.home")+File.separator,tempDirName);
		if( !tempDir.exists() ) {
			tempDir.mkdir();
		}
	
		// backup pictures
		File tempAlbumPictureDir = new File(tempDir.getPath() + File.separatorChar + FileSystemAccessWrapper.ALBUM_PICTURES);
		File sourceAlbumPictureDir = new File(FileSystemAccessWrapper.COLLECTOR_HOME_ALBUM_PICTURES);
		try {
			FileSystemAccessWrapper.copyDirectory(sourceAlbumPictureDir, tempAlbumPictureDir);
		} catch (IOException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	
		// backup application data
		File tempAppDataDir = new File(tempDir.getPath() + File.separatorChar + "app-data");
		File sourceAppDataDir = new File(FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA);
		try {
			String lockFileRegex = "^\\.lock$"; 
			FileSystemAccessWrapper.copyDirectory(sourceAppDataDir, tempAppDataDir, lockFileRegex);
		} catch (IOException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	
		//boolean successState = true;
		try (Statement statement = ConnectionManager.connection.createStatement()){				
			statement.executeUpdate("backup to '" + tempDir.getPath() + File.separatorChar + FileSystemAccessWrapper.DATABASE_TO_RESTORE_NAME+"'");
		} catch (SQLException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	
		// Zip the whole temp folder
		FileSystemAccessWrapper.zipFolderToFile(tempDir.getPath(), filePath);
	
		// delete temp folder
		FileSystemAccessWrapper.recursiveDeleteFSObject(tempDir);
		
	}
	/**
	 * Restores the database entries along the properties and pictures from the specified backup file
	 * @param filePath The path of the file ending with the file name from which the backup will be restored.
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static void restoreFromFile(String filePath) throws FailedDatabaseWrapperOperationException {	
		//TODO: implement 'file rollback' or restore in tempfile first then move and delete on error 
		FileSystemAccessWrapper.clearCollectorHome();
		FileSystemAccessWrapper.unzipFileToFolder(filePath, FileSystemAccessWrapper.COLLECTOR_HOME);
	
	
		try (Statement statement = ConnectionManager.connection.createStatement()) {			
			statement.executeUpdate("restore from '" + FileSystemAccessWrapper.DATABASE_TO_RESTORE+"'");
			try {
				DatabaseIntegrityManager.lastChangeTimeStamp =  DatabaseIntegrityManager.extractTimeStamp(new File(filePath));
			} catch (FailedDatabaseWrapperOperationException e) {
				DatabaseIntegrityManager.lastChangeTimeStamp = System.currentTimeMillis();
			}
		} catch (SQLException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState,e);
		}
	
		if ( !FileSystemAccessWrapper.deleteDatabaseRestoreFile() ) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
	
		if ( !FileSystemAccessWrapper.updateCollectorFileStructure() ) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
	
		if ( !FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.connection) ) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
		// Update timestamp
		DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
	}
	/**
	 * Gets the time stamp when the last change to the database happened.
	 * @return The time in milliseconds when the last change to the database occured. -1 If not initialized.
	 */
	public static long getLastDatabaseChangeTimeStamp() {
		return DatabaseIntegrityManager.lastChangeTimeStamp;
	}
	static void updateLastDatabaseChangeTimeStamp() {
		DatabaseIntegrityManager.lastChangeTimeStamp = System.currentTimeMillis();
	}
	/**
	 * Gets the list of existing autosaves sorted by filename timestamp, newest to oldest.
	 * @return List of files of previous autosaves. Empty list if none exist
	 */
	public static List<File> getAllAutoSaves() throws FailedDatabaseWrapperOperationException{
		List<File> autoSaves = FileSystemAccessWrapper.getAllMatchingFilesInCollectorHome(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX);
		Collections.sort(autoSaves, new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				try {
					return Long.compare(DatabaseIntegrityManager.extractTimeStamp(file2),  DatabaseIntegrityManager.extractTimeStamp(file1));
				} catch (FailedDatabaseWrapperOperationException e) {
					return -1;//TODO: find a better solution when extractTimeStamp fails					
				}
			}
		});
		return autoSaves;
	}
	/**
	 * Extracts the database last change timestamp from the autosave file. Requires the correct format of the name.
	 * @param autoSaveFile
	 * @return A long integer representing the last change of the database.
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	static long extractTimeStamp(File autoSaveFile) throws FailedDatabaseWrapperOperationException {
	
		String fileName;
		try {
			fileName = autoSaveFile.getCanonicalFile().getName();
		} catch (IOException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState, e);
		}
		
		if ( !fileName.matches(DatabaseIntegrityManager.AUTO_SAVE_FILE_REGEX) ) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
		
		try {
			long databaseChangeTimeStamp =Long.parseLong(fileName.substring(fileName.indexOf("_")+1, fileName.indexOf(".")));
			return databaseChangeTimeStamp;
		} catch (NumberFormatException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState,e);
		}
	}
	/**
	 * Creates an automatic backup when the current state of the database is newer than the most recent autosave.  
	 * To reduce the memory footprint of the backup (i.e. in case of a large amount of pictures) only the db file is
	 * backed up. 
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static void backupAutoSave() throws FailedDatabaseWrapperOperationException {		
		// TODO: implement programm version.
		// TODO: check if 'file rollbacks' are necessary and adjust errors!
		String programVersion = ""; 		
		// TODO: convert timestamp to UTC to compensate for backup/restores across timezones
		String timeStamp = Long.toString(getLastDatabaseChangeTimeStamp());		
	
		String autoSaveFilePath = 	FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + 
				File.separator +
				"periodicalBackup"+
				programVersion+
				"_";// separator for the timestamp	
	
		List<File> previousAutoSaveList = getAllAutoSaves();
	
		if(DatabaseIntegrityManager.autoSaveLimit<1) {			
			return;
		};
	
		if (previousAutoSaveList.isEmpty()) {
			// When no changes were made then the timestamp is the current time
			if (getLastDatabaseChangeTimeStamp() == -1) {
				timeStamp = Long.toString(System.currentTimeMillis());
			}
			autoSaveFilePath = autoSaveFilePath +timeStamp+"."+ DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
			try {
				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
			} catch (IOException e) {
				LOGGER.error("Autosave - backup failed");
				throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
			}
		}else {// Autosaves detected
	
			// No need to overwrite the last autosave when no changes were made.
			if (getLastDatabaseChangeTimeStamp() == -1 ) {
				return;
			}
	
			// Auto save limit reached, delete the oldest
			if (previousAutoSaveList.size()>=DatabaseIntegrityManager.autoSaveLimit) {
				File oldestAutoSave = previousAutoSaveList.get(previousAutoSaveList.size()-1);
				if (oldestAutoSave.exists()) {
					if (!oldestAutoSave.delete()){
						LOGGER.error("Autosave - cannot delete old autosave");
						throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
					}
				}
			}
			autoSaveFilePath = autoSaveFilePath +timeStamp+"."+ DatabaseIntegrityManager.AUTO_SAVE_EXTENSION;
	
			try {
				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
			} catch (IOException e) {
				LOGGER.error("Autosave - backup failed");
				throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
			}
		}
	}
}
