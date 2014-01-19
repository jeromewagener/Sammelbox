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

package org.sammelbox.controller.synchronization;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Display;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.ApplicationUI;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.various.SynchronizeCompositeHelper;
import org.sammelbox.view.various.SynchronizeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeromewagener.soutils.beaconing.BeaconSender;
import com.jeromewagener.soutils.communication.CommunicationManager;
import com.jeromewagener.soutils.filetransfer.FileTransferServer;
import com.jeromewagener.soutils.messaging.SoutilsMessage;
import com.jeromewagener.soutils.utilities.InetAddressUtilities;
import com.jeromewagener.soutils.utilities.Soutilities;

public class SyncServerServiceImpl implements SyncServerService {
	// TODO define file transfer port
	private static final int FILE_TRANSFER_PORT = 6565;
	// TODO define communication port
	private static final int COMMUNICATION_PORT = 12345;
	// TODO define broadcast port
	private static final int BROADCAST_PORT = 5454;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncServerServiceImpl.class);
	private static final String SYNC_DIRECTORY_PATH = FileSystemLocations.TEMP_DIR + "sammelbox-sync" + File.separatorChar;
	private static final String SYNC_ZIP_ARCHIVE_PATH = FileSystemLocations.TEMP_DIR + "sammelbox-sync.zip";

	private static final int SYNC_FINISH_LOOP_TIME_SLEEP_IN_MILLISECONDS = 200;
	private static final int MAX_VALUE_FOR_SYNC_CODE = 99999;
	private static final int MIN_VALUE_FOR_SYNC_CODE = 10000;
	
	private String synchronizationCode = null;
	
	private CommunicationManager communicationManager = null;
	private FileTransferServer fileTransferServer = null;
	private List<BeaconSender> beaconSenders = null;
	
	@Override
	public File zipHomeForSynchronziation() {
		File syncFolder = new File(SYNC_DIRECTORY_PATH);
		
		if (syncFolder.exists()) {
			FileSystemAccessWrapper.deleteDirectoryRecursively(syncFolder);
		}
		
		try {
			if (syncFolder.mkdir()) {
				LOGGER.error("Could not create sync folder");
			}

			FileSystemAccessWrapper.copyFile(
					new File(FileSystemLocations.getDatabaseFile()), 
					new File(SYNC_DIRECTORY_PATH + FileSystemLocations.DATABASE_NAME));

			FileSystemAccessWrapper.copyDirectory(
					new File(FileSystemLocations.getThumbnailsDir()), 
					new File(SYNC_DIRECTORY_PATH + FileSystemLocations.THUMBNAILS_DIR_NAME));
			
			FileSystemAccessWrapper.copyDirectory(
					new File(FileSystemLocations.getAppDataDir()), 
					new File(SYNC_DIRECTORY_PATH + FileSystemLocations.APP_DATA_DIR_NAME));

			FileSystemAccessWrapper.zipFolderToFile(syncFolder.getAbsolutePath(), SYNC_ZIP_ARCHIVE_PATH);
		} catch (IOException ioe) {
			LOGGER.error("An error occured while packaging the information before synchronization", ioe);
		}
		
		return new File(SYNC_ZIP_ARCHIVE_PATH);
	}
	
	@Override
	public String getSynchronizationCode() {
		if (synchronizationCode == null) {
			synchronizationCode = String.valueOf(
					Soutilities.randomNumberBetweenIntervals(MIN_VALUE_FOR_SYNC_CODE, MAX_VALUE_FOR_SYNC_CODE));
		}
		
		return synchronizationCode;
	}
	
	@Override
	public String getHashedSynchronizationCode() {
		if (synchronizationCode == null) {
			getSynchronizationCode();
		}
		
		try {
			return Soutilities.stringToMD5(synchronizationCode);
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			LOGGER.error("Could not calculate the hash", noSuchAlgorithmException);
		}
		
		return null;
	}

	@Override
	public void startBeaconingHashedSynchronizationCode() {
		if (beaconSenders == null) {
			try {	

				beaconSenders = new ArrayList<BeaconSender>();
				for (InetAddress ipAddress : InetAddressUtilities.getAllIPsAndAssignedBroadcastAddresses().keySet()) {
					BeaconSender beaconSender = new BeaconSender(
							"sammelbox-desktop:sync-code:" + getHashedSynchronizationCode(), 
							InetAddressUtilities.getAllIPsAndAssignedBroadcastAddresses().get(ipAddress),
							BROADCAST_PORT, this);
					beaconSender.start();

					beaconSenders.add(beaconSender);
				}
			} catch (SocketException socketException) {
				LOGGER.error("Could not retrieve broadcast addresses", socketException);
			}
		} else {
			LOGGER.warn("Beacons are already sent!");
		}
	}

	@Override
	public void stopBeaconingHashedSynchronizationCode() {
		if (beaconSenders != null) {
			for (BeaconSender beaconSender : beaconSenders) {
				beaconSender.done();
			}
			beaconSenders = null;
		}
	}

	@Override
	public void startCommunicationChannel() {
		if (communicationManager == null) {
			communicationManager = new CommunicationManager(COMMUNICATION_PORT, this);
			communicationManager.start();
		} else {
			LOGGER.warn("Communication channel is already up and running!");
		}
	}

	@Override
	public void stopCommunicationChannel() {
		if (communicationManager != null) {
			communicationManager.done();
			communicationManager = null;
		}
	}

	@Override
	public void openFileTransferServer(String storageLocationAsAbsolutPath) {
		if (fileTransferServer == null) {
			fileTransferServer = new FileTransferServer(storageLocationAsAbsolutPath, FILE_TRANSFER_PORT ,this);
			fileTransferServer.start();
		} else {
			LOGGER.warn("File transfer server already up and running!");
		}
	}
	
	@Override
	public long getFileTransferProgressPercentage() {
		return fileTransferServer.getFileTransferPercentage();
	}
	
	@Override
	public void stopFileTransferServer() {
		if (fileTransferServer != null) {
			fileTransferServer.setDone(true);
			fileTransferServer = null;
		}
	}

	@Override
	public void handleSoutilsMessage(SoutilsMessage soutilsMessage) {
		LOGGER.info(soutilsMessage.getMessageType() + ":" + soutilsMessage.getContent() + ":" + soutilsMessage.getSenderAddress(), soutilsMessage.getThrowable());
		
		if (soutilsMessage.getContent().startsWith("sammelbox-android:connect:") && getSynchronizationCode().equals(soutilsMessage.getContent().split(":")[2])) {
			openFileTransferServer(zipHomeForSynchronziation().getAbsolutePath());
			stopBeaconingHashedSynchronizationCode();
			communicationManager.sendMessageToAllConnectedPeers(
					"sammelbox-desktop:accept-transfer:" + new File(zipHomeForSynchronziation().getAbsolutePath()).length());
			
			SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.ESTABLISH_CONNECTION);
			SynchronizeCompositeHelper.enabledSynchronizeStep(SynchronizeStep.TRANSFER_DATA);
			executeSyncPageWithProgressbarUpdateInUIThread(Translator.toBeTranslated("Data transfer in progress. This might take some time. <b>Please Wait</b>"));
			
			while (!fileTransferServer.isDone()) {
				try {
					Thread.sleep(SYNC_FINISH_LOOP_TIME_SLEEP_IN_MILLISECONDS);
					Display.getDefault().asyncExec(new Runnable() {
					    public void run() {
					    	ApplicationUI.getAlbumItemBrowser().execute("updateProgress('" + getFileTransferProgressPercentage() + "%')");
					    }
					});    	
											
				} catch (InterruptedException interrruptedException) {
					LOGGER.error("An error occurred while updating the progress bar", interrruptedException);
				}
			}
			
			SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.TRANSFER_DATA);
			SynchronizeCompositeHelper.enabledSynchronizeStep(SynchronizeStep.FINISH);
			executeSyncPageUpdateInUIThread(Translator.toBeTranslated("Terminating Synchronization"));
					
			communicationManager.sendMessageToAllConnectedPeers("sammelbox-desktop:transfer-finished");
			
			SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.FINISH);
			executeSyncPageUpdateInUIThread(Translator.toBeTranslated("Synchronization finished. You can now use your albums on your mobile device!"));
		} else {
			LOGGER.info("Received message in unknown format or faulty synchronization code");
		}
	}
	
	private void executeSyncPageUpdateInUIThread(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	BrowserFacade.showSynchronizePage(message);
		    }
		});
	}
	
	private void executeSyncPageWithProgressbarUpdateInUIThread(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	BrowserFacade.showSynchronizePageWithProgressBar(message);
		    }
		});
	}
}
