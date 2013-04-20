package collector.desktop.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

public class NetworkGateway extends Observable implements Observer {
	/** The TCP/UDP-port used by the application */
	public final static int APPLICATION_PORT = 4242;
	/** The thread which is sending out beacons */
	private static BeaconSender beaconSender = null;
	/** The thread which is used to exchanged messages with the connected client */
	private static SynchronizationServer synchronizationServer = null;
	/** The singleton instance of the NetworkGateway */
	private static NetworkGateway instance = new NetworkGateway();

	/** Singleton constructor */
	private NetworkGateway() {
	}

	/** Return the singleton instance of the NetworkGateway */
	public static NetworkGateway getInstance() {
		return instance;
	}
	
	/** Starts a thread which is periodically sending out beacons using all available broadcast addresses. */
	private static void startBeaconing() {
		try {
			beaconSender = new BeaconSender(XmlMessageBuilders.getBeaconAsXml(InetAddress.getLocalHost().getHostName()));
		} catch (UnknownHostException e) {
			System.err.println(NetworkGateway.class + ": " + e.getClass() + " - " + e.getMessage());
		}

		beaconSender.start();
	} 

	/** This stops the thread which is currently sending out beacons. If no thread is running, then this method has no effect */
	private static void stopBeaconing() {
		if (beaconSender != null) {
			beaconSender.done();
			beaconSender = null;
		}
	}

	/** Starts the synchronization server used to exchange messages with a connected client */
	private static void startSynchronizationServer() {
		synchronizationServer = new SynchronizationServer();
		synchronizationServer.addObserver(instance);		

		new Thread(synchronizationServer).start();
	}

	/** Stops the synchronization server used to exchange messages. If no synchronization server is running, then this method has no effect */
	private static void stopSynchronizationServer() {
		if (synchronizationServer != null) {
			synchronizationServer.done();
			synchronizationServer = null;
		}
	}

	/** Initiate the synchronization. This will start the broadcasting of beacons as well as the synchronization server
	 * to which clients can connect in order to exchange messages */
	public static void startSynchronization() {
		startBeaconing();
		startSynchronizationServer();
	}

	/** Stops the synchronization. This will stop the broadcasting of beacons (if still running) 
	 * and the synchronization server (if already running) */
	public static void stopSynchronization() {
		stopBeaconing();
		stopSynchronizationServer();
	}

	@Override
	public void update(Observable observable, Object value) {		
		if (observable.getClass().equals(SynchronizationServer.class)) {
			System.out.println("NetworkGateway received the following from the sync-server: " + value);
			setChanged();
			notifyObservers();
		}
	}
}
