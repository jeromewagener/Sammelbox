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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.sammelbox.controller.filesystem.FileSystemAccessWrapper;
import org.sammelbox.controller.filesystem.FileSystemLocations;
import org.sammelbox.controller.i18n.Translator;
import org.sammelbox.view.browser.BrowserFacade;
import org.sammelbox.view.various.SynchronizeCompositeHelper;
import org.sammelbox.view.various.SynchronizeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeromewagener.soutils.Utilities;
import com.jeromewagener.soutils.desktop.beaconing.BeaconSender;
import com.jeromewagener.soutils.desktop.networking.CommunicationManager;
import com.jeromewagener.soutils.desktop.networking.NetworkFacade;
import com.jeromewagener.soutils.filetransfer.FileTransferServer;

public class SyncServerServiceImpl implements SyncServerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncServerServiceImpl.class);
	private static final int SYNC_PORT = 12345;
	private static final String SYNC_DIRECTORY_PATH = FileSystemLocations.TEMP_DIR + "sammelbox-sync" + File.separatorChar;
	private static final String SYNC_ZIP_ARCHIVE_PATH = FileSystemLocations.TEMP_DIR + "sammelbox-sync.zip";
	
	private String currentSynchronizationCode = null;
	
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
			syncFolder.mkdir();

			FileSystemAccessWrapper.copyFile(
					new File(FileSystemLocations.getDatabaseFile()), 
					new File(SYNC_DIRECTORY_PATH + FileSystemLocations.DATABASE_NAME));

			FileSystemAccessWrapper.copyDirectory(
					new File(FileSystemLocations.getThumbnailsDir()), 
					new File(SYNC_DIRECTORY_PATH + FileSystemLocations.THUMBNAILS_DIR_NAME));

			FileSystemAccessWrapper.zipFolderToFile(syncFolder.getAbsolutePath(), SYNC_ZIP_ARCHIVE_PATH);
		} catch (IOException ioe) {
			LOGGER.error("An error occured while packaging the information before synchronization", ioe);
		}
		
		return new File(SYNC_ZIP_ARCHIVE_PATH);
	}

	@Override
	public void createSynchronizationCode() {
		currentSynchronizationCode = String.valueOf(Utilities.randomNumberBetweenIntervals(10000, 99999));
	}
	
	@Override
	public String getCurrentSynchronizationCode() {
		if (currentSynchronizationCode == null) {
			createSynchronizationCode();
		}
		
		return currentSynchronizationCode;
	}
	
	@Override
	public String getHashedSynchronizationCode() {
		if (currentSynchronizationCode == null) {
			createSynchronizationCode();
		}
		
		return Utilities.stringToMD5(currentSynchronizationCode);
	}

	@Override
	public void startBeaconingHashedSynchronizationCode() {
		if (beaconSenders == null) {
			beaconSenders = new ArrayList<BeaconSender>();
			for (InetAddress ipAddress : NetworkFacade.getAllIPsAndAssignedBroadcastAddresses().keySet()) {
				BeaconSender beaconSender = new BeaconSender(
						"sammelbox-desktop:sync-code:" + getHashedSynchronizationCode(), 
						NetworkFacade.getAllIPsAndAssignedBroadcastAddresses().get(ipAddress));
				beaconSender.start();
				
				beaconSenders.add(beaconSender);
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
			communicationManager = new CommunicationManager(SYNC_PORT);
			communicationManager.registerMessageReceptionObserver(this);
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
			fileTransferServer = new FileTransferServer(storageLocationAsAbsolutPath);
			fileTransferServer.start();
		} else {
			LOGGER.warn("File transfer server already up and running!");
		}
	}
	
	@Override
	public int getFileTransferProgressPercentage() {
		return fileTransferServer.isDone() ? 1 : 0; // TODO return percentage
	}
	
	@Override
	public void stopFileTransferServer() {
		if (fileTransferServer != null) {
			fileTransferServer.setDone(true);
			fileTransferServer = null;
		}
	}

	@Override
	public void reactToMessage(String ipAddress, String message) {
		if (message.startsWith("sammelbox-android:connect:")) {
			if (getCurrentSynchronizationCode().equals(message.split(":")[2])) {
				openFileTransferServer(zipHomeForSynchronziation().getAbsolutePath());
				stopBeaconingHashedSynchronizationCode();
				communicationManager.sendMessageToAllConnectedPeers("sammelbox-desktop:accept-transfer");
				
				SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.ESTABLISH_CONNECTION);
				SynchronizeCompositeHelper.enabledSynchronizeStep(SynchronizeStep.TRANSFER_DATA);
				
				Display.getDefault().asyncExec(new Runnable() {
				    public void run() {
				    	BrowserFacade.showSynchronizePage(Translator.toBeTranslated(
				    			"Data transfer in progress. This might take some time. <b>Please Wait</b>"));
				    }
				});
				
				while (getFileTransferProgressPercentage() != 1) {
					try {
						Thread.sleep(500); // TODO avoid busy waiting here!
						System.out.println("Transfering data"); // TODO show percentage
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.TRANSFER_DATA);
				SynchronizeCompositeHelper.enabledSynchronizeStep(SynchronizeStep.FINISH);
				
				Display.getDefault().asyncExec(new Runnable() {
				    public void run() {
				    	BrowserFacade.showSynchronizePage(Translator.toBeTranslated("Terminating Synchronization"));
				    }
				});
						
				communicationManager.sendMessageToAllConnectedPeers("sammelbox-desktop:transfer-finished");
				
				SynchronizeCompositeHelper.disableSynchronizeStep(SynchronizeStep.FINISH);
				
				Display.getDefault().asyncExec(new Runnable() {
				    public void run() {
				    	BrowserFacade.showSynchronizePage(Translator.toBeTranslated(
				    			"Synchronization finished. You can now use your albums on your mobile device!"));
				    }
				});
			} else {
				LOGGER.info("Received faulty synchronization code");
			}
		} else {
			LOGGER.info("Received message in unknown format");
		}
	}
}
