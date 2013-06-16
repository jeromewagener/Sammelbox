package collector.desktop.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.jdbcdslog.ConnectionLoggingProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.database.exceptions.FailedDatabaseWrapperOperationException;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class ConnectionManager {
	
	static Connection connection = null;
	/**The normal logger for all info, debug, error and warning in this class*/
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
	/**
	 * Opens the default connection for the FileSystemAccessWrapper.DATABASE database. Only opens a new connection if none is currently open.
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static void openConnection() throws FailedDatabaseWrapperOperationException {
		// Catch the internal sql exception to give a definite state on the db connection using the collector exceptions
		// This hides all internal sql exceptions
		try {
			if (ConnectionManager.connection == null || connection.isClosed()) {
				ConnectionManager.connection = DriverManager.getConnection(ConnectionManager.sqliteConnectionString + FileSystemAccessWrapper.DATABASE);
				ConnectionManager.connection =  ConnectionLoggingProxy.wrap(connection);
	
				ConnectionManager.enableForeignKeySupportForCurrentSession();
				/* 
				 * Autocommit state makes little difference here since all relevant public methods roll back on
				 * failures anyway and we have only a single connection so concurrency is not relevan either.
				 */				
				ConnectionManager.connection.setAutoCommit(true);
								
				LOGGER.info("Autocommit is on {}", connection.getAutoCommit());				
			}
			// Create the album master table if it does not exist 
			DatabaseWrapper.createAlbumMasterTableIfNotExits();
			
			// Create the picture table if it does not exist
			DatabaseWrapper.createPictureTable();
	
			// Run a fetch  to check if db is ok
			if ( ConnectionManager.isConnectionReady() == false ) {
				throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
			}
		} catch (SQLException e) {			
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
	}

	/**
	 * Tries to close the database connection. If the connection is closed or null calling this method has no effect.
	 * @throws FailedDatabaseWrapperOperationException
	 */
	public static void closeConnection() throws FailedDatabaseWrapperOperationException {
		try {
			if (ConnectionManager.connection != null) {
				if (ConnectionManager.connection.isClosed()) {
					return;
				}
				ConnectionManager.connection.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Unable to close the database connection");
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * Test if the connection is open and ready to be used. 
	 */
	public static boolean isConnectionReady() {
		//TODO do some logging when this method fails.
	
		if (ConnectionManager.connection == null) {			
			return false;
		}		
		try {
			if ( ConnectionManager.connection.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			return false;			
		}
	
		/*
		 *  Querying all albums should be successful on all working dbs,
		 *  independently of how many albums are stored.
		 */
		List<String> albums = null;
		try {
		albums = DatabaseWrapper.listAllAlbums();
		} catch (FailedDatabaseWrapperOperationException e) {
			LOGGER.error("Unable to fetch the list of albums when testing for a valid connection");
			return false;
		}
		
		if (albums == null) {			
			return false;
		}
		return true;
	}

	/**
	 * This method can be used when the database connection cannot be opened (e.g. corrupt db file).
	 * I saves the collector home for manual inspection, then clears the whole  collector home including the db
	 * and opens a connection to a blank db.
	 * If the connection is unexcpectantly in a usable state it, this is a no-operation.
	 * @throws FailedDatabaseWrapperOperationException 
	 */
	public static void openCleanConnection() throws FailedDatabaseWrapperOperationException {
		if (isConnectionReady() == true) {
			return;
		}
	
		closeConnection();			
	
		String corruptSnapshotFileName = DatabaseIntegrityManager.corruptDatabaseSnapshotPrefix + System.currentTimeMillis();
		File corruptTemporarySnapshotFile = new File(FileSystemAccessWrapper.USER_HOME + File.separator + corruptSnapshotFileName);
		corruptTemporarySnapshotFile.deleteOnExit();
		// Copy file to temporary location
		try {
			FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), corruptTemporarySnapshotFile);
		} catch (IOException e1) {
			LOGGER.error("Copying the corrupt database file to a temporary location failed" , e1);
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
	
		//TODO: implement checks on the success of this operation
		FileSystemAccessWrapper.removeCollectorHome();
		// Copy the corrupt snapshot from the temporary location into the app data folder 
		String corruptSnapshotFilePath = FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separator + corruptSnapshotFileName;
		File corruptSnapshotFile = new File(corruptSnapshotFilePath);			
		try {
			FileSystemAccessWrapper.copyFile(corruptTemporarySnapshotFile, corruptSnapshotFile);
		} catch (IOException e) {
			LOGGER.error("Copying the corrupt database file from the temporary location back to the clean Collector HOME failed. Manual cleanup may be required", e);
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
	
		// Try to open a regular connection to the newly setup collector HOME
		openConnection();
		
		if (FileSystemAccessWrapper.updateCollectorFileStructure() == false) {
			LOGGER.error("Updating the structure of the Collector HOME failed. Manual cleanup may be required");
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
	}

	/**
	 * Gets the connection.
	 * @return A valid connection or null if not properly initialized.
	 */
	public static Connection getConnection() {
		return connection;
	}

	static void enableForeignKeySupportForCurrentSession() throws FailedDatabaseWrapperOperationException {
		
		try ( PreparedStatement preparedStatement = connection.prepareStatement("PRAGMA foreign_keys = ON");){			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new FailedDatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		} 
	}

	static String sqliteConnectionString = "jdbc:sqlite:";	

}
