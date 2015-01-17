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

package org.sammelbox;

import org.eclipse.swt.SWT;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.DictKeys;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.controller.managers.BuildInformationManager;
import org.sammelbox.controller.managers.ConnectionManager;
import org.sammelbox.controller.managers.SettingsManager;
import org.sammelbox.controller.managers.WelcomePageManager;
import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.various.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public final class Sammelbox {
	public static final String ORG_SQLITE_JDBC = "org.sqlite.JDBC";
	private static final Logger LOGGER = LoggerFactory.getLogger(Sammelbox.class);

	private Sammelbox() {
		// Sammelbox is launched using the main method
	}
	
	/** This method initializes the file structure and opens the database connections. */
	private static void setupConnectionAndFilesystem() {
		try {
			Class.forName(ORG_SQLITE_JDBC);
			ConnectionManager.openConnection();
		} catch (ClassNotFoundException classNotFoundException) {
			LOGGER.warn(ORG_SQLITE_JDBC + " couldn't be found on the classpath", classNotFoundException);
		} catch (DatabaseWrapperOperationException databaseWrapperOperationException) {
			try {
				LOGGER.warn("Couldn't open a database connection. Will try to open a clean connection instead.", databaseWrapperOperationException);
				ConnectionManager.openCleanConnection();	
			} catch (DatabaseWrapperOperationException dwoe2) {
				LOGGER.error("The database is corrupt since opening a connection failed. " +
						"Recent autosaves of the database can be found in: " + FileSystemLocations.getBackupDir(), dwoe2);
				
				ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_SAMMELBOX_CANT_BE_LAUNCHED), 
						Translator.get(DictKeys.DIALOG_CONTENT_SAMMELBOX_CANT_BE_LAUNCHED), 
						SWT.ERROR).open();
			}			
		}
	}
	
	/** Initializes and launches Sammelbox */
	static void launch() {
		// check whether Sammelbox home is installed to a non default configuration
		if (new File(FileSystemLocations.HOME_REDIRECTION_FILE).exists()) {
			FileSystemLocations.setActiveHomeDir(FileSystemAccessWrapper.readFileAsString(FileSystemLocations.HOME_REDIRECTION_FILE));
		} else {
			FileSystemLocations.setActiveHomeDir(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
		}
		
		LOGGER.info("Sammelbox (build: " + BuildInformationManager.instance().getVersion() +
				" build on " + BuildInformationManager.instance().getBuildTimeStamp() + ") started");
		try {
			// Ensure that the folder structure including the lock file exists before locking
			FileSystemAccessWrapper.updateSammelboxFileStructure();

			// Load available files
			SettingsManager.initializeFromSettingsFile();
			WelcomePageManager.initializeFromWelcomeFile();
			Translator.setLanguageFromSettingsOrSystem();
			
			RandomAccessFile lockFile = new RandomAccessFile(FileSystemLocations.getLockFile(), "rw");
			FileChannel fileChannel = lockFile.getChannel();

			if (fileChannel.tryLock() != null) {
				// Initialize the Database connection
				setupConnectionAndFilesystem();

				// create the shell and show the user interface. This blocks until the shell is closed
				ApplicationUI.initialize();

				// close the database connection if the the shell is closed
				ConnectionManager.closeConnection();

				// close file & channel
				fileChannel.close();
				lockFile.close();
			} else {
				ComponentFactory.getMessageBox(Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
						Translator.get(DictKeys.DIALOG_CONTENT_PROGRAM_IS_RUNNING),
						SWT.ICON_INFORMATION).open();
			}
		} catch (Exception ex) {
			LOGGER.error("Sammelbox crashed", ex);
		} finally {
			LOGGER.info("Sammelbox stopped");
		}
	}
	
	/** The main method initializes the database (using the constructor) and establishes the user interface */
	public static void main(String[] args) {
		if (new File(FileSystemLocations.HOME_REDIRECTION_FILE).exists()) {
			FileSystemLocations.setActiveHomeDir(FileSystemAccessWrapper.readFileAsString(FileSystemLocations.HOME_REDIRECTION_FILE));
		} else {
			FileSystemLocations.setActiveHomeDir(FileSystemLocations.DEFAULT_SAMMELBOX_HOME);
		}

		copyExecutableToActiveHomeDir();
		Sammelbox.launch();
	}

	/** We copy the executable to the active home dir to have it handy in case we copy around the sammelbox */
	private static void copyExecutableToActiveHomeDir() {
		File workingDirectory = new File(FileSystemLocations.WORKING_DIR);
		File[] filesInWorkingDirectory = workingDirectory.listFiles();

		if (filesInWorkingDirectory != null) {
			// search for sammelbox exe in current directory
			for (File file : filesInWorkingDirectory) {
				if (file.getName().toLowerCase().contains("sammelbox") && file.getName().toLowerCase().endsWith(".jar")) {
					try {
						FileSystemAccessWrapper.copyFile(file, new File(
								FileSystemLocations.DEFAULT_SAMMELBOX_HOME + File.separatorChar + file.getName()));
					} catch (IOException | SecurityException ex) {
						LOGGER.error("An error occurred while copying the Sammelbox executable", ex);
					}

					break;
				}
			}
		}
	}
}
