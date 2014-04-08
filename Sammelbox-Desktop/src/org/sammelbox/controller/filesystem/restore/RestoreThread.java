package org.sammelbox.controller.filesystem.restore;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.swt.widgets.Display;
import org.sammelbox.controller.events.EventObservable;
import org.sammelbox.controller.events.SammelboxEvent;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.DatabaseIntegrityManager;
import org.sammelbox.controller.managers.SavedSearchManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestoreThread extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestoreThread.class);
	private static final String RESTORE_FROM = "restore from "; 
	
	private final String backupLocationPath;
	private String errorString = "";
	private boolean done = false;
	
	public RestoreThread(String backupLocationPath) {
		this.backupLocationPath = backupLocationPath;
	}
	
	public void run() {
		restore();
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {							
				SavedSearchManager.initialize();
				
				// No default album is selected on restore
				ApplicationUI.refreshAlbumList();
				BrowserFacade.showBackupRestoredPage();
				
				EventObservable.addEventToQueue(SammelboxEvent.ENABLE_SAMMELBOX);
			}
		});
	}
	
	public void restore() {
		FileSystemAccessWrapper.clearHomeDirectory();
		FileSystemAccessWrapper.unzipFileToFolder(backupLocationPath, FileSystemLocations.getActiveHomeDir());
	
		final String restoreToSQLCommand = String.format(RESTORE_FROM + "%s", FileSystemLocations.getDatabaseRestoreFile());
		
		try (Statement statement = ConnectionManager.getConnection().createStatement()) {			
			statement.executeUpdate(restoreToSQLCommand);
			try {
				DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp(DatabaseIntegrityManager.extractTimeStamp(new File(backupLocationPath)));
			} catch (DatabaseWrapperOperationException e) {
				DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
			}
		} catch (SQLException e) {
			LOGGER.error("A error occurred while restoring the backup", e);
			errorString = Translator.get(DictKeys.ERROR_RESTORE_FAILED, e.getMessage());
			done = true;
		}
	
		if (!FileSystemAccessWrapper.deleteDatabaseRestoreFile()) {
			LOGGER.error("An issue occurred while updating the album file structure");
			errorString = Translator.get(DictKeys.ERROR_RESTORE_CLEANUP_FAILED);
		}
			
		if (!FileSystemAccessWrapper.updateSammelboxFileStructure()) {
			LOGGER.error("An issue occurred while updating the album file structure");
			errorString = Translator.get(DictKeys.ERROR_RESTORE_CLEANUP_FAILED);
		}
		
		if (!FileSystemAccessWrapper.updateAlbumFileStructure(ConnectionManager.getConnection())) {
			LOGGER.error("An issue occurred while updating the album file structure");
			errorString = Translator.get(DictKeys.ERROR_RESTORE_CLEANUP_FAILED);
		}
		
		// Update timestamp
		DatabaseIntegrityManager.updateLastDatabaseChangeTimeStamp();
		
		done = true;
	}
	
	public String getErrorString() {
		return errorString;
	}	
	
	public boolean isDone() {
		return done;
	}
}
