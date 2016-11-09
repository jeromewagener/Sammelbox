package org.sammelbox.controller.filesystem.backup;

import java.io.File;
import java.io.IOException;
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
import org.sammelbox.view.browser.BrowserFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupThread extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(BackupThread.class);
	private static final String BACKUP_TO = "backup to ";
	private static final String REGEX_BEGIN_OF_LINE = "^";
	private static final String REGEX_END_OF_LINE = "$";
	private static final String REGEX_OR = "|";
	private static final String LOCK_FILE_REGEX = REGEX_BEGIN_OF_LINE + "\\.lock" + REGEX_END_OF_LINE;	
	private static final String DATABASE_FILE_REGEX = REGEX_BEGIN_OF_LINE + FileSystemLocations.DATABASE_NAME + REGEX_END_OF_LINE;
	
	private final String backupLocationPath;
	private String errorString = "";
	private boolean done = false;
	
	public BackupThread(String backupLocationPath) {
		this.backupLocationPath = backupLocationPath;
	}
	
	public void run() {
		backup();
    }
	
	public void backup() {
		// create temporary directory which includes backup files
		String tempDirName = java.util.UUID.randomUUID().toString();
		File tempDir = new File(System.getProperty("user.home") + File.separator, tempDirName);
		if (!tempDir.exists() && !tempDir.mkdir()) {
			LOGGER.error("Could not create temp directory");
		}
	
		// backup home directory
		File tempAppDataDir = new File(tempDir.getPath());
		File sourceAppDataDir = new File(FileSystemLocations.getActiveHomeDir());
		try {
			String excludeRegex = LOCK_FILE_REGEX + REGEX_OR + DATABASE_FILE_REGEX; 
			FileSystemAccessWrapper.copyDirectory(sourceAppDataDir, tempAppDataDir, excludeRegex);
		} catch (IOException e) {
			LOGGER.error("An error occurred while creating the backup", e);
			errorString = Translator.get(DictKeys.ERROR_BACKUP_CREATION_FAILED, e.getMessage());
			done = true;
		}

		final String backupToSQLcommand = String.format(
				BACKUP_TO + "'%s'", tempDir.getPath() + File.separatorChar + FileSystemLocations.DATABASE_TO_RESTORE_NAME);
		
		// backup database to file
		try (Statement statement = ConnectionManager.getConnection().createStatement()){				
			 statement.executeUpdate(backupToSQLcommand);
		} catch (SQLException e) {
			LOGGER.error("An error occurred while creating the backup", e);
			errorString = Translator.get(DictKeys.ERROR_BACKUP_CREATION_FAILED, e.getMessage());
			done = true;
		}
	
		// zip the whole temp folder
		FileSystemAccessWrapper.zipFolderToFile(tempDir.getPath(), backupLocationPath);
	
		// delete temp folder
		FileSystemAccessWrapper.deleteDirectoryRecursively(tempDir);
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {							
				BrowserFacade.showBackupFinishedPage();
				EventObservable.addEventToQueue(SammelboxEvent.ENABLE_SAMMELBOX);
			}
		});
		
		done = true;
	}
	
	public String getErrorString() {
		return errorString;
	}	
	
	public boolean isDone() {		
		return done;
	}
}
