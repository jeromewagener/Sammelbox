package org.sammelbox.controller.synchronization;

import java.io.File;

public interface SyncServerService {
	/** Creates a ZIP-archive which contains a reduced version of the 
	 * Sammelbox home for synchronization with mobile devices */
	public File zipHomeForSynchronziation();
	
	/*public String getBeaconMessageWithHashedAccessCode();
	public void startBroadcastingBeacons();
	public void startCommunicationServer();
	public void reactToMessage();
	public void startFileServer();
	public void stopFileServer();*/
	
	public static class Default {
		public static SyncServerService getServiceInstance() {
			return new SyncServerServiceImpl();
		}
	}
}
