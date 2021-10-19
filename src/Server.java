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
		private PeerInfo connectedInfo;

		private MessageHandler msgHandler;

		public Handler(Socket connection, int no, PeerInfo hostInfo) {
			this.connection = connection;
			this.no = no;
			this.hostInfo = hostInfo;
			this.msgHandler = new MessageHandler();
		}

		public void run() {
			try {
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());

				// Perform handshake
				String peerID = msgHandler.receiveHandshakeServer(in);
				this.connectedInfo = PeerInfoList.instance.getPeer(peerID);
				if (this.connectedInfo != null) {
					// Create an item in ConnectedClientsList
					Logger.instance.logTCPConnectionFrom(peerID);
					msgHandler.sendHandshake(out, hostInfo.peerID);
					DebugLogger.instance.log("Handshake completed");

					while (PeerInfoList.instance.checkAllPeersHaveFile() == false) {
						// Wait for message
						Message received = msgHandler.receiveMessage(in);
						DebugLogger.instance.log("Received %s message", received.type.toString());

						// Handle the received message
						switch (received.type) {
						case BITFIELD:
							handleBitfieldReceived(received);
							break;
						case INTERESTED:
							// TODO
							break;
						case NOT_INTERESTED:
							// TODO
							break;
						case REQUEST:
							handleRequestReceived(received);
							break;
						default:
							DebugLogger.instance.log("Default case");
							break;
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

		private void handleBitfieldReceived(Message received) {
			// TODO: store bitfield in list of peers

			// TODO: add bitfield to payload
			msgHandler.sendMessage(out, 0, MessageType.BITFIELD, null);
		}

		private void handleRequestReceived(Message received) {
			RequestPayload requestPayload = new RequestPayload(received.payload);
			byte[] piece = PieceStorage.instance.getPiece(requestPayload.index);
			PiecePayload piecePayload = new PiecePayload(requestPayload.index, piece);
			msgHandler.sendMessage(out, piecePayload.getLength(), MessageType.PIECE, piecePayload.getBytes());
		}
	}

}
