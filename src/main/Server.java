package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import main.logging.*;
import main.messaging.*;
import main.messaging.payloads.*;

public class Server implements Runnable {

	private static List<String> interested = new ArrayList<>();

	public static synchronized List<String> getInterested() {
		return interested;
	}

	public static synchronized void addInterested(String peerID) {
		interested.add(peerID);
	}

	public static synchronized void removeInterested(String peerID) {
		interested.removeIf(p -> p.equals(peerID));
	}

	/**
	 * Mapping of peerID -> ObjectOutputStream.
	 */
	private static HashMap<String, ObjectOutputStream> outputStreams = new HashMap<>();

	/**
	 * @param peerID the peerID associated with the output stream.
	 * @param out    the output stream associated with the target peer.
	 */
	public static synchronized void insertOutputStream(String peerID, ObjectOutputStream out) {
		outputStreams.put(peerID, out);
	}

	/**
	 * @return a hash map of peerID -> ObjectOutputStream.
	 */
	public static synchronized HashMap<String, ObjectOutputStream> getOutputStreams() {
		return outputStreams;
	}

	private static HashMap<String, Boolean> unchoked = new HashMap<>();

	public static synchronized Boolean isUnchoked(String peerID) {
		if (unchoked.containsKey(peerID)) {
			return unchoked.get(peerID);
		} else {
			return false;
		}
	}

	public static synchronized void setUnchoked(String peerID, boolean val) {
		unchoked.put(peerID, val);
	}

	private PeerInfo hostInfo;
	private int numHandlers;

	public Server(PeerInfo hostInfo) {
		this.hostInfo = hostInfo;
		this.numHandlers = PeerInfoList.instance.getNumPeersAfterThisPeer();
	}

	public void run() {
		DebugLogger.instance.log("Server is running at %s:%s", hostInfo.hostname, hostInfo.port);
		try {
			ServerSocket listener = new ServerSocket(Integer.parseInt(hostInfo.port));
			try {
				for (int i = 0; i < numHandlers; i++) {
					new Handler(listener.accept(), i, hostInfo).start();
					DebugLogger.instance.log("Client %d is connected", i);
				}
			} finally {
				listener.close();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		// Spawn the threads for choosing neighbors to send data to
		new Thread(new HandlePreferredNeighbors()).run();
		new Thread(new HandleOptimisticUnchoke()).run();
	}

	// Handles choosing preferred neighbors
	private static class HandlePreferredNeighbors implements Runnable {
		/**
		 * Called if this peer has the full file.
		 *
		 * Chooses preferred neighbors completely randomly from list of interested
		 * neighbors.
		 *
		 * @param k - the number of neighbors to choose.
		 *
		 * @return list of peer IDs that are preferred.
		 */
		private List<String> determinePreferredRandomly(int k) {
			List<String> currInterested = new ArrayList<>(interested);
			ArrayList<String> preferredNeighbors = new ArrayList<String>();

			Random numberGenerator = new Random();

			for (int i = 0; i < Math.min(k, currInterested.size()); i++) {
				// Adding random index of currInterested into preferredNeighbors
				int randNumber = numberGenerator.nextInt(currInterested.size());
				preferredNeighbors.add(currInterested.get(randNumber));

				// Removing from currInterested
				currInterested.remove(randNumber);
			}

			return preferredNeighbors;
		}

		/**
		 * Called if this peer doesn't have the full file.
		 *
		 * Choose preferred neighbors based on how many pieces they have sent to this
		 * peer, in the most recent unchoking interval.
		 *
		 * @param k the number of neighbors to choose.
		 *
		 * @return list of peer IDs that are preferred.
		 */
		private List<String> determinePreferredNeighbors(int k) {
			List<String> currInterested = new ArrayList<>(interested);
			HashMap<String, Double> rates = new HashMap<>();
			// Determine k neighbors that have fed data at the highest rate
			for (String p : currInterested) {
				rates.put(p, RateTracker.instance.getRate(p));
			}
			currInterested.sort((p1, p2) -> rates.get(p1).compareTo(rates.get(p2)));
			List<String> picked = new ArrayList<>();
			// TODO: check out "More than 2 neighbors = random decision"
			for (int i = 0; i < Math.min(k, currInterested.size()); i++) {
				picked.add(currInterested.get(i));
			}
			return picked;
		}

		public void run() {
			MessageHandler msgHandler = new MessageHandler();
			long time = Long.parseLong(CommonConfig.unchokingInterval);
			int k = Integer.parseInt(CommonConfig.numberOfPreferredNeighbors);

			// Wait the first interval
			try {
				TimeUnit.SECONDS.sleep(time);
			} catch (InterruptedException e) {
				System.out.print(e);
			}

			while (true) {
				DebugLogger.instance.log("Determining preferred neighbors");

				List<String> newPreferred = new ArrayList<>();

				if (PeerInfoList.instance.getThisPeer().hasFile) {
					// Determine toUnchoke randomly
					newPreferred = determinePreferredRandomly(k);
				} else {
					// Determine toUnchoke mathematically
					newPreferred = determinePreferredNeighbors(k);
				}

				// Determine which neighbors to send unchoke to
				List<String> toUnchoke = new ArrayList<>();
				for (String p : newPreferred) {
					if (!isUnchoked(p)) {
						DebugLogger.instance.log("Add to toUnchoke %s", p);
						toUnchoke.add(p);
					}
				}

				// Determine which previously preferred neighbors to choke
				// Remove preferred neighbors that are no longe preferred
				List<String> toChoke = new ArrayList<>();
				for (Map.Entry<String, Boolean> set : unchoked.entrySet()) {
					if (set.getValue() && !newPreferred.contains(set.getKey())) {
						toChoke.add(set.getKey());
					}
				}

				// Send choke to toChoke
				for (String p : toChoke) {
					msgHandler.sendMessage(Server.getOutputStreams().get(p), MessageType.CHOKE, new EmptyPayload());
				}

				// Send unchoke message to every peer that needs to be unchoked
				for (int i = 0; i < toUnchoke.size(); i++) {
					DebugLogger.instance.log("Unchoking %s", toUnchoke.get(i));
					msgHandler.sendMessage(Server.getOutputStreams().get(toUnchoke.get(i)), MessageType.UNCHOKE,
							new EmptyPayload());
				}

				// Set the new currUnchoked
				for (String p : toChoke) {
					setUnchoked(p, false);
				}
				for (String p : toUnchoke) {
					setUnchoked(p, true);
				}

				DebugLogger.instance.log("Finished determining preferred neighbors");

				// Wait the interval
				try {
					TimeUnit.SECONDS.sleep(time);
				} catch (InterruptedException e) {
					System.out.print(e);
				}
			}
		}
	}

	// Handles optimistically unchoking an interested peer
	private static class HandleOptimisticUnchoke implements Runnable {
		private String prev = null;

		public void run() {
			MessageHandler msgHandler = new MessageHandler();
			long time = Long.parseLong(CommonConfig.optimisticUnchokingInterval);

			// Wait the interval
			try {
				TimeUnit.SECONDS.sleep(time);
			} catch (InterruptedException e) {
				System.out.println(e);
			}

			while (true) {
				DebugLogger.instance.log("Determining optimistically unchoked neighbor");
				// Every m seconds, pick 1 interested neighbor among choked at random that
				// should be optimistically unchoked
				// Get list of choked, yet interested neighbors
				List<String> choked = new ArrayList<>();
				for (String p : interested) {
					if (!isUnchoked(p)) {
						choked.add(p);
					}
				}
				int randIndex = new Random().nextInt(choked.size());
				String randomPeer = choked.get(randIndex);

				// Send unchoke to that neighbor
				msgHandler.sendMessage(Server.getOutputStreams().get(randomPeer), MessageType.UNCHOKE,
						new EmptyPayload());

				// Send choke to previous optimistically unchoked neighbor
				if (prev != null) {
					msgHandler.sendMessage(Server.getOutputStreams().get(prev), MessageType.CHOKE, new EmptyPayload());
				}

				// Update state variables
				setUnchoked(prev, false);
				prev = randomPeer;
				setUnchoked(randomPeer, true);

				DebugLogger.instance.log("Finished determining optimistically unchoked neighbor");

				// Wait the interval
				try {
					TimeUnit.SECONDS.sleep(time);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}

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

					// Store the output stream
					Server.insertOutputStream(connectedInfo.peerID, out);

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
