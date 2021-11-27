package main;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import main.logging.*;
import main.messaging.*;
import main.messaging.payloads.*;

public class Client implements Runnable {
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

    Socket requestSocket; // socket connect to the server
    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket

    PeerInfo hostInfo;
    PeerInfo targetInfo;

    MessageHandler msgHandler;

    public Client(PeerInfo hostInfo, PeerInfo targetInfo) {
        this.hostInfo = hostInfo;
        this.targetInfo = targetInfo;
        this.msgHandler = new MessageHandler();
    }

    public void run() {
        try {
            // create a socket to connect to the server
            requestSocket = new Socket(targetInfo.hostname, Integer.parseInt(targetInfo.port));
            DebugLogger.instance.log("Opened socket to %s:%s", targetInfo.hostname, targetInfo.port);

            // initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            // Perform handshake
            msgHandler.sendHandshake(out, hostInfo.peerID);
            boolean checkHandshake = msgHandler.receiveHandshakeClient(in, targetInfo.peerID);

            if (checkHandshake) {
                DebugLogger.instance.log("Handshake valid");
                FileLogger.instance.logTCPConnectionTo(targetInfo.peerID);

                // Store the output stream
                Server.insertOutputStream(targetInfo.peerID, out);

                // Send bitfield message
                BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
                msgHandler.sendMessage(out, MessageType.BITFIELD, new BitfieldPayload(thisBitfield));

                while (PeerInfoList.instance.getThisPeer().hasFile == false) {
                    // Wait for message
                    Message received = msgHandler.receiveMessage(in);
                    DebugLogger.instance.log("Received %s message", received.type.toString());

                    // Handle the received message
                    switch (received.type) {
                        case BITFIELD:
                            handleBitfieldReceived(received);
                            break;
                        case CHOKE:
                            handleChokeReceived(received);
                            break;
                        case UNCHOKE:
                            handleUnchokeReceived(received);
                            break;
                        case PIECE:
                            handlePieceReceived(received);
                            break;
                        case HAVE:
                            handleHaveReceived(received);
                            break;
                        default:
                            DebugLogger.instance.log("Default case");
                            break;
                    }
                }
                DebugLogger.instance.log("Have the entire file.");
            } else {
                DebugLogger.instance.err("Handshake invalid");
            }
        } catch (ConnectException e) {
            DebugLogger.instance.err("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            DebugLogger.instance.err("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // Close connections
            try {
                in.close();
                out.close();
                requestSocket.close();
                DebugLogger.instance.log("Successfully closed client for %s", hostInfo.peerID);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleBitfieldReceived(Message received) {
        // Store bitfield in list of peers
        BitfieldPayload payload = (BitfieldPayload) received.getPayload();
        PeerInfoList.instance.updatePeerBitfield(targetInfo.listIndex, payload.bitfield);

        // Compare bitfields to check if interested or not
        boolean interested = payload.compare(PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield);
        if (interested) {
            msgHandler.sendMessage(out, MessageType.INTERESTED, new EmptyPayload());
        } else {
            msgHandler.sendMessage(out, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
    }

    /**
     *
     * Updates the target host's bitfield with the index received.
     *
     * @param received the message received, containing a `have` payload.
     */
    private void handleHaveReceived(Message received) {
        // peerid
        // bitfield - update the bitfield we think they have
        // Update bitfield
        HavePayload payload = (HavePayload) received.getPayload();
        PeerInfoList.instance.setPeerBitfieldIndex(targetInfo.peerID, payload.index);
    }

    /**
     *
     * Sends a `have` message to all of the connected peers.
     *
     * @param index the index that was successfully received.
     */
    private void sendHaveToConnectedPeers(int index) {
        HashMap<String, ObjectOutputStream> serverOutputStreams = Server.getOutputStreams();
        HashMap<String, ObjectOutputStream> clientOutputStreams = Client.getOutputStreams();

        for (ObjectOutputStream outputStream : serverOutputStreams.values()) {
            msgHandler.sendMessage(outputStream, MessageType.HAVE, new HavePayload(index));
        }

        for (ObjectOutputStream outputStream : clientOutputStreams.values()) {
            msgHandler.sendMessage(outputStream, MessageType.HAVE, new HavePayload(index));
        }
    }

    private void handlePieceReceived(Message received) {
        // Store piece in data structure
        PiecePayload payload = (PiecePayload) received.getPayload();
        PieceStorage.instance.setPiece(payload.index, payload.data);
        // Update upload rate
        RateTracker.instance.updateRate(targetInfo.peerID, payload.data.length);
        // Update bitfield
        PeerInfoList.instance.setPeerBitfieldIndex(hostInfo.peerID, payload.index);
        // Send "have" message to connected peers
        sendHaveToConnectedPeers(payload.index);
        // Determine index of next request message
        BitSet targetBitfield = PeerInfoList.instance.getPeer(targetInfo.peerID).bitfield;
        BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
        // Find needed indices
        List<Integer> interestedIndices = new ArrayList<>();
        for (int i = 0; i < targetBitfield.size(); i++) {
            boolean targetHas = targetBitfield.get(i);
            boolean thisHas = thisBitfield.get(i);
            if (targetHas && !thisHas) {
                interestedIndices.add(i);
            }
        }
        // unchoked but they have nothing we want
        if (interestedIndices.size() == 0) {
            // send not interested
            msgHandler.sendMessage(out, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
        // unchoked and they have things we don't
        else {
            // Send request message with random index
            int randIndex = interestedIndices.get(new Random().nextInt(interestedIndices.size()));
            msgHandler.sendMessage(out, MessageType.REQUEST, new RequestPayload(randIndex));
        }
    }

    // need to know which peer sent the message

    private void handleChokeReceived(Message received) {
        // Compare bitfields to determine if we are interested
        // Send interested/not interested message

        BitSet targetBitfield = PeerInfoList.instance.getPeer(targetInfo.peerID).bitfield;
        BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;

        for (int i = 0; i < targetBitfield.size(); i++) {
            if (targetBitfield.get(i) == true && thisBitfield.get(i) == false) {
                msgHandler.sendMessage(out, MessageType.INTERESTED, new EmptyPayload());
                return;
            }
        }

        // Send `not interested` message
        msgHandler.sendMessage(out, MessageType.NOT_INTERESTED, new EmptyPayload());
    }

    private void handleUnchokeReceived(Message received) {
        // Compare stored bitfield of to ours to see what data to request
        BitSet targetBitfield = PeerInfoList.instance.getPeer(targetInfo.peerID).bitfield;
        BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
        // Find needed indices
        List<Integer> interestedIndices = new ArrayList<>();
        for (int i = 0; i < targetBitfield.size(); i++) {
            boolean targetHas = targetBitfield.get(i);
            boolean thisHas = thisBitfield.get(i);
            if (targetHas && !thisHas) {
                interestedIndices.add(i);
            }
        }
        // unchoked but they have nothing we want
        if (interestedIndices.size() == 0) {
            // send not interested
            msgHandler.sendMessage(out, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
        // unchoked and they have things we don't
        else {
            // Send request message with random index
            int randIndex = interestedIndices.get(new Random().nextInt(interestedIndices.size()));
            msgHandler.sendMessage(out, MessageType.REQUEST, new RequestPayload(randIndex));
        }
    }
}
