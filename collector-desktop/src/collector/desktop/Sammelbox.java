package collector.desktop;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collector.desktop.controller.filesystem.BuildInformation;
import collector.desktop.controller.filesystem.FileSystemAccessWrapper;
import collector.desktop.controller.settings.ApplicationSettingsManager;
import collector.desktop.model.database.ConnectionManager;
import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
import collector.desktop.view.ApplicationUI;
import collector.desktop.view.internationalization.DictKeys;
import collector.desktop.view.internationalization.Translator;
import collector.desktop.view.various.ComponentFactory;

public class Sammelbox {
	/** TODO why? This logger must NOT be used since it always logs the start and end of the program */
	private final static Logger GATE_LOGGER = LoggerFactory.getLogger("collector.desktop.GateLogger");
	private final static Logger LOGGER = LoggerFactory.getLogger(Sammelbox.class);
	
	/**
	 * This method initializes the file structure and opens the database connections.
	 * @throws Exception Either a class not found exception if the jdbc driver could not be initialized or
	 * an exception if the database connection could not be established.
	 */
	private static void setupConnectionAndFilesystem() throws Exception {		
		Class.forName("org.sqlite.JDBC");
		
		try {
			ConnectionManager.openConnection();
		} catch (DatabaseWrapperOperationException ex){
			try {
				ConnectionManager.openCleanConnection();				
			}catch (DatabaseWrapperOperationException ex2) {
				LOGGER.error("The database is corrupt since opening a connection failed. A dump of the db can be found in the program App folder.");
			}			
		}
	}
	
	/** The main method initializes the database (using the collector constructor) and establishes the user interface */
	public static void main(String[] args) throws ClassNotFoundException {
		GATE_LOGGER.trace("Collector (build: " + BuildInformation.instance().getVersion() + " build on " + BuildInformation.instance().getBuildTimeStamp() +") started");
		try {
			ApplicationSettingsManager.initializeFromSettingsFile();
			Translator.setLanguageFromSettingsOrSystem();
			// Ensure that folder structure including lock file exists before locking
			FileSystemAccessWrapper.updateCollectorFileStructure();

			RandomAccessFile randomFile = new RandomAccessFile(FileSystemAccessWrapper.LOCK_FILE, "rw");
			FileChannel channel = randomFile.getChannel();

			if (channel.tryLock() != null) {
				// Initialize the Database connection
				setupConnectionAndFilesystem();

				// create the shell and show the user interface. This blocks until the shell is closed
				ApplicationUI.createCollectorShell(ApplicationUI.getShell());

				// close the database connection if the the shell is closed
				ConnectionManager.closeConnection();

				// close file & channel
				channel.close();
				randomFile.close();
			} else {
				ComponentFactory.getMessageBox(ApplicationUI.getShell(), 
						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
						Translator.get(DictKeys.DIALOG_TITLE_PROGRAM_IS_RUNNING), 
						SWT.ICON_INFORMATION).open();
			}
		} catch (Exception ex) {
			GATE_LOGGER.error("Collector crashed", ex);
		}finally {
			GATE_LOGGER.trace("Collector ended");
		}
	}

}
