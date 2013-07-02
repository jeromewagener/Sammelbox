package collector.desktop.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jdbcdslog.ConnectionLoggingProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
import collector.desktop.database.exceptions.ExceptionHelper;
import collector.desktop.filesystem.FileSystemAccessWrapper;

public class ConnectionManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
	private static final String sqliteConnectionString = "jdbc:sqlite:";
	private static Connection connection = null;

	/**
	 * Opens the default connection for the FileSystemAccessWrapper.DATABASE database. Only opens a new connection if none is currently open.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void openConnection() throws DatabaseWrapperOperationException {
		// Catch the internal SQL exception to give a definite state on the database connection using the collector exceptions
		// This hides all internal SQL exceptions
		try {
			if (ConnectionManager.connection == null || connection.isClosed()) {
				ConnectionManager.connection = DriverManager.getConnection(ConnectionManager.sqliteConnectionString + FileSystemAccessWrapper.DATABASE);
				ConnectionManager.connection =  ConnectionLoggingProxy.wrap(connection);

				ConnectionManager.enableForeignKeySupportForCurrentSession();
				
				// The AutoCommit state makes little difference here since all relevant public methods roll back on
				// failures anyway and we have only a single connection so concurrency is not relevant either.		
				ConnectionManager.connection.setAutoCommit(true);

				LOGGER.info("Autocommit is on {}", connection.getAutoCommit());				
			}
			
			// Create the album master table if it does not exist 
			DatabaseWrapper.createAlbumMasterTableIfItDoesNotExist();

			// Run a fetch  to check if the database connection is up and running
			if (ConnectionManager.isConnectionReady() == false) {
				throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
			}
		} catch (SQLException e) {			
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}
	}

	/**
	 * Tries to close the database connection. If the connection is closed or null calling this method has no effect.
	 * @throws DatabaseWrapperOperationException
	 */
	public static void closeConnection() throws DatabaseWrapperOperationException {
		try {
			if (ConnectionManager.connection != null && !ConnectionManager.connection.isClosed()) {
				ConnectionManager.connection.close();
			}
		} catch (SQLException e) {
			LOGGER.error("Unable to close the database connection");
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState, e);
		}
	}

	/**
	 * Test if the connection is open and ready to be used. 
	 */
	public static boolean isConnectionReady() {
		try {
			// Querying all albums should be successful on all working databases, independently of how many albums are stored.
			// If not, (e.g. due to connection problems) or missing albums, indicate the failure
			if (ConnectionManager.connection == null || ConnectionManager.connection.isClosed() || DatabaseWrapper.listAllAlbums() == null) {			
				return false;
			}
		} catch (Exception ex) {
			LOGGER.error("Unable to test the database connection \n Stacktrace: " + ExceptionHelper.toString(ex));
			return false;			
		}
		
		return true;
	}

	/**
	 * This method can be used when the database connection cannot be opened (e.g. corrupt database file).
	 * I saves the collector home for manual inspection, then clears the whole  collector home including the database
	 * and opens a connection to a blank database.
	 * If the connection is unexpectedly in a usable state it, this is a no-operation.
	 * @throws DatabaseWrapperOperationException 
	 */
	public static void openCleanConnection() throws DatabaseWrapperOperationException {
		if (isConnectionReady() == true) {
			return;
		}

		closeConnection();			

		String corruptSnapshotFileName = DatabaseIntegrityManager.CORRUPT_DATABASE_SNAPSHOT_PREFIX + System.currentTimeMillis();
		File corruptTemporarySnapshotFile = new File(FileSystemAccessWrapper.USER_HOME + File.separator + corruptSnapshotFileName);
		corruptTemporarySnapshotFile.deleteOnExit();
		// Copy file to temporary location
		try {
			FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), corruptTemporarySnapshotFile);
		} catch (IOException e1) {
			LOGGER.error("Copying the corrupt database file to a temporary location failed" , e1);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithCleanState);
		}

		// Clean collector home
		FileSystemAccessWrapper.removeCollectorHome();

		// Copy the corrupt snapshot from the temporary location into the app data folder 
		String corruptSnapshotFilePath = FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separator + corruptSnapshotFileName;
		File corruptSnapshotFile = new File(corruptSnapshotFilePath);			
		try {
			FileSystemAccessWrapper.copyFile(corruptTemporarySnapshotFile, corruptSnapshotFile);
		} catch (IOException e) {
			LOGGER.error("Copying the corrupt database file from the temporary location back to the clean Collector HOME failed. Manual cleanup may be required", e);
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}

		// Try to open a regular connection to the newly setup collector HOME
		openConnection();

		if (FileSystemAccessWrapper.updateCollectorFileStructure() == false) {
			LOGGER.error("Updating the structure of the Collector HOME failed. Manual cleanup may be required");
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		}
	}

	/**
	 * Gets the connection.
	 * @return A valid connection or null if not properly initialized.
	 */
	public static Connection getConnection() {
		return connection;
	}

	static void enableForeignKeySupportForCurrentSession() throws DatabaseWrapperOperationException {

		try (PreparedStatement preparedStatement = connection.prepareStatement("PRAGMA foreign_keys = ON");) {			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new DatabaseWrapperOperationException(DBErrorState.ErrorWithDirtyState);
		} 
	}
}
