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
	private static final String SYNC_FOLDER = "sammelbox-sync";
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncServerServiceImpl.class);
	
	private String synchronizationCode = null;	
	private CommunicationManager communicationManager = null;
	private FileTransferServer fileTransferServer = null;
	private BeaconSender beaconSender = null;
	
	@Override
	public File zipHomeForSynchronziation() {
		File syncFolder = new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER);
		
		if (syncFolder.exists()) {
			FileSystemAccessWrapper.deleteDirectoryRecursively(syncFolder);
		}
		
		try {
			syncFolder.mkdir();

			FileSystemAccessWrapper.copyFile(
					new File(FileSystemLocations.getDatabaseFile()), 
					new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER + File.separatorChar + FileSystemLocations.DATABASE_NAME));

			FileSystemAccessWrapper.copyDirectory(
					new File(FileSystemLocations.getThumbnailsDir()), 
					new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER + File.separatorChar + FileSystemLocations.THUMBNAILS_DIR_NAME)); // TODO uncomment

			FileSystemAccessWrapper.zipFolderToFile(
					syncFolder.getAbsolutePath(), FileSystemLocations.TEMP_DIR + SYNC_FOLDER + ".zip");
		} catch (IOException ioe) {
			LOGGER.error("An error occured while packaging the information before synchronization", ioe);
		}
		
		return new File(FileSystemLocations.TEMP_DIR + SYNC_FOLDER + ".zip");
	}

	@Override
	public void createSynchronizationCode() {
		synchronizationCode = String.valueOf(Soutilities.randomNumberBetweenIntervals(10000, 99999));
	}
	
	@Override
	public String getSynchronizationCode() {
		if (synchronizationCode == null) {
			createSynchronizationCode();
		}
		
		return synchronizationCode;
	}
	
	@Override
	public String getHashedSynchronizationCode() {
		if (synchronizationCode == null) {
			createSynchronizationCode();
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
		if (beaconSender == null) {
			try {
				beaconSender = new BeaconSender(
						"sammelbox-desktop:sync-code:" + getHashedSynchronizationCode(), 
						InetAddressUtilities.getAllIPsAndAssignedBroadcastAddresses().values().iterator().next(),
						5454, this); // TODO hard coded test code only!
				beaconSender.start();
			} catch (SocketException socketException) {
				LOGGER.error("Could not retrieve broadcast addresses", socketException);
			}
		} else {
			LOGGER.warn("Beacons are already sent!");
		}
	}

	@Override
	public void stopBeaconingHashedSynchronizationCode() {
		if (beaconSender != null) {
			beaconSender.done();
			beaconSender = null;
		}
	}

	@Override
	public void startCommunicationChannel() {
		if (communicationManager == null) {
			communicationManager = new CommunicationManager(12345, this); // TODO define port
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
			fileTransferServer = new FileTransferServer(storageLocationAsAbsolutPath, 6565 ,this); // TODO define port
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
					Thread.sleep(200);
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
