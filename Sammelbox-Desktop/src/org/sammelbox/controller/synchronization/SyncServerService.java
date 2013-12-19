package org.sammelbox.controller.synchronization;

import java.io.File;

import com.jeromewagener.soutils.desktop.networking.MessageReceptionObserver;

public interface SyncServerService extends MessageReceptionObserver {
	/** Creates a ZIP-archive which contains a reduced version of the 
	 * Sammelbox home for synchronization with mobile devices */
	public File zipHomeForSynchronziation();
	
	/** Creates a new synchronization code */
	public void createSynchronizationCode();
	/** Returns the current synchronization code */
	public String getCurrentSynchronizationCode();
	/** Returns a hash of the current synchronization code */
	public String getHashedSynchronizationCode();
	
	public void startBeaconingHashedSynchronizationCode();
	public void stopBeaconingHashedSynchronizationCode();
	
	public void startCommunicationChannel();
	public void stopCommunicationChannel();
	
	public void openFileTransferServer(String storageLocationAsAbsolutPath);
	public int getFileTransferProgressPercentage();
	public void stopFileTransferServer();
	
	public static class Default {
		public static SyncServerService getServiceInstance() {
			return new SyncServerServiceImpl();
		}
	}
}
