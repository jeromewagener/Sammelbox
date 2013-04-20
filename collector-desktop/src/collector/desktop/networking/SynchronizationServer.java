package collector.desktop.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Observable;

public class SynchronizationServer extends Observable implements Runnable {
	/** The time in milliseconds until a new connection accept attempt from the serverSocketChannel */
	private final static int MILLISECONDS_UNTIL_NEXT_ACCEPT = 500;
	/** The time in milliseconds until a new read is attempted from the socketChannel */
	private final static int MILLISECONDS_UNTIL_NEXT_READ = 500;
	/** The size of the buffer used to read from the socketChannel */
	private final static int READ_BUFFER_SIZE = 4096;
	/** The serverSocketChannel is only needed to accept an incoming connection from a (mobile) device */
	private ServerSocketChannel serverSocketChannel = null;
	/** The socket channel to the (mobile) device */
	private SocketChannel socketChannel = null;
	/** This variable must be set to true in order to stop the thread */
	private boolean done = false;

	/** This methods stops the current thread as soon as possible */
	public void done() {
		done = true;
	}

	/** The default SynchronizationServer constructor configures a non blocking serverSocketChannel to be able to accept an  
	 * incoming connection. Thereby a new socketChannel to the (mobile) device is established. The port to be used is defined by the
	 * APPLICATION_PORT constant. */
	public SynchronizationServer() {
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(NetworkGateway.APPLICATION_PORT));			
		} catch (IOException e) {
			System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
		}
	}

	/** Sends a message via the socketChannel to a (mobile) device. The socketChannel must not be null!
	 * @param message The message to be transferred. (Usually XML) */
	public synchronized void sendMessage(String message) {
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();

		try {
			ByteBuffer buffer = encoder.encode(CharBuffer.wrap(message));
			while (buffer.remaining() > 0) {
				socketChannel.write(buffer);
			}
		} catch (Exception e) {
			System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
		}
	}

	@Override
	/** This method first waits for an incoming connection in order to accept a socket channel. It then tries to read from this 
	 * socketChannel each  */
	public void run() {		
		// Wait for the incoming connection
		// This loop ends if the thread is stopped or if an incoming connection arrives
		while (done == false && socketChannel == null) {		
			try {
				// Wait for an incoming connection
				socketChannel = serverSocketChannel.accept();
				
				if (socketChannel != null) {
					// socketChannel will be different from null if a connection arrived
					socketChannel.configureBlocking(false);
				} else {
					Thread.sleep(MILLISECONDS_UNTIL_NEXT_ACCEPT);
				}
			} catch (IOException ioe) {
				System.err.println(this.getClass() + ": " + ioe.getClass() + " - " + ioe.getMessage());
			} catch (InterruptedException ie) {
				System.err.println(this.getClass() + ": " + ie.getClass() + " - " + ie.getMessage());
			}
		}
		
		// The ServerSocketChannel is no longer needed
		try {
			serverSocketChannel.close();
		} catch (IOException ioe) {
			System.err.println(this.getClass() + ": " + ioe.getClass() + " - " + ioe.getMessage());
		}

		// ByteBuffer to read from the SocketChannel
		ByteBuffer buffer = ByteBuffer.allocate(READ_BUFFER_SIZE);

		// This loop only stops reading from the buffer if the thread is stopped
		while (done == false) {
			try {
				// initialize buffer
				buffer.rewind();
				buffer.clear();

				// only read if something is there
				if (socketChannel.read(buffer) > 0) {
					setChanged();
					notifyObservers(new String(buffer.array(), "UTF-8"));
				}

				// Let the thread sleep for a while
				Thread.sleep(MILLISECONDS_UNTIL_NEXT_READ);
			} catch (InterruptedException e) {
				System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
			} catch (IOException e) {
				System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
			}
		}

		// Close the SocketChannel as it is no longer needed
		try {
			if (socketChannel != null) {
				socketChannel.close();
			}
		} catch (IOException e) {
			System.err.println(this.getClass() + ": " + e.getClass() + " - " + e.getMessage());
		}
	}
}
