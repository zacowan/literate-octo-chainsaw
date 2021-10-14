import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server implements Runnable {

	private PeerInfo hostInfo;

	public Server(PeerInfo hostInfo) {
		this.hostInfo = hostInfo;
	}

	public void run() {
		System.out.printf("Server for peer %s is running at %s:%s.\n", hostInfo.peerID, hostInfo.hostname, hostInfo.port);
		try {
			ServerSocket listener = new ServerSocket(Integer.parseInt(hostInfo.port));
			int clientNum = 1;
			try {
				while (true) {
					new Handler(listener.accept(), clientNum, hostInfo).start();
					System.out.println("Client " + clientNum + " is connected!");
					clientNum++;
				}
			} finally {
				listener.close();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening loop and are
	 * responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int no; // The index number of the client
		private PeerInfo hostInfo;

		public Handler(Socket connection, int no, PeerInfo hostInfo) {
			this.connection = connection;
			this.no = no;
			this.hostInfo = hostInfo;
		}

		public void run() {
			try {
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());

				MessageHandler msgHandler = new MessageHandler();

				// Perform handshake
				String peerID = msgHandler.receiveHandshakeServer(in);
				if (peerID != null) {
					System.out.printf("Peer %s connected.\n", peerID);
					Logger.instance.logTCPConnectionFrom(peerID);
					msgHandler.sendHandshake(out, peerID);

					while (true) {
						// Wait for message
						Message received = msgHandler.receiveMessage(in);

						// Handle the received message
						switch (received.type) {
							case BITFIELD:
								System.out.println("Received bitfield.");
								// TODO: store bitfield in list of peers

								// TODO: add bitfield to payload
								Message toSend = new Message(MessageType.BITFIELD, null);
								msgHandler.sendMessage(out, toSend);
							case INTERESTED:
								System.out.println("Received interested.");
							case NOT_INTERESTED:
								System.out.println("Received not interested.");
							case REQUEST:
								System.out.println("Received request.");
							default:
								System.out.println("Default case.");
						}
					}

				} else {
					System.err.println("Handhsake failed.");
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + no);
			} finally {
				// Close connections
				try {
					Logger.instance.closeLogFile();
					in.close();
					out.close();
					connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

	}

}
