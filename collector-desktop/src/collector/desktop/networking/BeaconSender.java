package collector.desktop.networking;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

public class BeaconSender extends Thread {
	/** The time in milliseconds until a new broadcast is performed */
	private final static int MILLISECONDS_UNTIL_NEXT_BROADCAST = 500;
	/** The message to be transferred by the beacon */
	private String message = null;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;

	/** This methods stops the current thread as soon as possible */
	public void done() {
		done = true;
	}

	/** This constructor sets the message to be transferred by the beacon */
	public BeaconSender(String message) {
		this.message = message;
	}
	
	/** Returns a mapping of all assigned IP-addresses to their according broadcast addresses 
	 * @return a mapping of all assigned IP-addresses to their according broadcast addresses */
	private HashMap<InetAddress, InetAddress> getAllIpAndBroadcastAddresses() { 
		HashMap<InetAddress, InetAddress> ipAndBroadcastAddresses = new HashMap<InetAddress, InetAddress>();
		Enumeration<?> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();

			while(networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();

				if(networkInterface != null && !networkInterface.isLoopback() && networkInterface.isUp()) {
					Iterator<?> it = networkInterface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress interfaceAddress = (InterfaceAddress) it.next();

						if (interfaceAddress != null && interfaceAddress.getBroadcast() != null) {
							ipAndBroadcastAddresses.put(interfaceAddress.getAddress(), interfaceAddress.getBroadcast());
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println(this.getClass() + ": " + ex.getClass() + " - " + ex.getMessage());
		}

		return ipAndBroadcastAddresses;
	}

	/** This method sends out a single beacon transferring the message provided via the constructor
	 * @param broadcastAddress the broadcastAddress used to broadcast the message */
	private void broadcast(InetAddress broadcastAddress) { 
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket(NetworkGateway.APPLICATION_PORT);
			socket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), broadcastAddress, NetworkGateway.APPLICATION_PORT);
			socket.send(packet);
		} catch (Exception ex) {
			System.err.println(this.getClass() + ": " + ex.getClass() + " - " + ex.getMessage());
		}

		socket.close();
	}

	@Override
	/** This method constantly sends out beacons with a given message. (Every MILLISECONDS_UNTIL_NEXT_BROADCAST milliseconds) 
	 * The beacons are send out via every available broadcast address */
	public void run() {		
		HashMap<InetAddress, InetAddress> ipAndBroadcastAddresses = getAllIpAndBroadcastAddresses();
		
		while (done == false) {
			for (InetAddress ipAddress : ipAndBroadcastAddresses.keySet()) {
				broadcast(ipAndBroadcastAddresses.get(ipAddress));
			}

			try {
				Thread.sleep(MILLISECONDS_UNTIL_NEXT_BROADCAST);
			} catch (InterruptedException e) {
				System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
			}
		}
	}
}
