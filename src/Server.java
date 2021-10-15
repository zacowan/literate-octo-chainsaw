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
		DebugLogger.instance.log("Server is running at %s:%s", hostInfo.hostname, hostInfo.port);
		try {
			ServerSocket listener = new ServerSocket(Integer.parseInt(hostInfo.port));
			int clientNum = 1;
			try {
				while (true) {
					new Handler(listener.accept(), clientNum, hostInfo).start();
					DebugLogger.instance.log("Client %d is connected", clientNum);
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
					Logger.instance.logTCPConnectionFrom(peerID);
					msgHandler.sendHandshake(out, peerID);
					DebugLogger.instance.log("Handshake completed");

					while (PeerInfoList.instance.checkAllPeersHaveFile() == false) {
						// Wait for message
						Message received = msgHandler.receiveMessage(in);
						DebugLogger.instance.log("Received %s message", received.type.toString());

						// Handle the received message
						switch (received.type) {
							case BITFIELD:
								// TODO: store bitfield in list of peers

								// TODO: add bitfield to payload
								Message toSend = new Message(MessageType.BITFIELD, null);
								msgHandler.sendMessage(out, toSend);
							case INTERESTED:
								// TODO
							case NOT_INTERESTED:
								// TODO
							case REQUEST:
								// TODO
							default:
								DebugLogger.instance.log("Default case");
						}
					}

				} else {
					DebugLogger.instance.err("Handshake failed");
				}
			} catch (IOException ioException) {
				DebugLogger.instance.err("Client %d disconnected", no);
			} finally {
				// Close connections
				try {
					in.close();
					out.close();
					connection.close();
					DebugLogger.instance.log("Successfully closed server");
				} catch (IOException ioException) {
					DebugLogger.instance.err("Client %d disconnected", no);
				}
			}
		}

	}

}
