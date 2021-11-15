package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.messaging.*;
import main.messaging.payloads.*;

public class Server implements Runnable {

	/**
	 * The list of peerIDs that are interested in this peer's pieces.
	 */
	private static List<String> interested = new ArrayList<>();

	/**
	 *
	 * @return a list of peerIDs that are interested.
	 */
	public static synchronized List<String> getInterested() {
		return interested;
	}

	/**
	 *
	 * @param peerID the peerID to add to the list of interested peers.
	 */
	public static synchronized void addInterested(String peerID) {
		interested.add(peerID);
	}

	/**
	 *
	 * @param peerID the peerID to remove from the list of interested peers.
	 */
	public static synchronized void removeInterested(String peerID) {
		interested.removeIf(p -> p.equals(peerID));
	}

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
					FileLogger.instance.logTCPConnectionFrom(peerID);
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
							handleInterestedReceived(received);
							break;
						case NOT_INTERESTED:
							handleNotInterestedReceived(received);
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

		private void handleInterestedReceived(Message received) {
			// Add the peer to the list of interested peers
			Server.addInterested(connectedInfo.peerID);
			// TODO: do we send a message here?
		}

		private void handleNotInterestedReceived(Message received) {
			// Remove the peer from the list of interested peers
			Server.removeInterested(connectedInfo.peerID);
		}

		private void handleBitfieldReceived(Message received) {
			// store bitfield in list of peers
			BitfieldPayload payload = (BitfieldPayload) received.getPayload();
			PeerInfoList.instance.updatePeerBitfield(PeerInfoList.instance.getPeerIndex(connectedInfo.peerID),
					payload.bitfield);
			// Send bitfield message
			BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
			msgHandler.sendMessage(out, MessageType.BITFIELD, new BitfieldPayload(thisBitfield));
		}

		private void handleRequestReceived(Message received) {
			RequestPayload payload = (RequestPayload) received.getPayload();
			byte[] piece = PieceStorage.instance.getPiece(payload.index);
			msgHandler.sendMessage(out, MessageType.PIECE, new PiecePayload(payload.index, piece));
		}
	}

}
