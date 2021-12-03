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
    PeerInfo hostInfo;
    PeerInfo targetInfo;

    MessageHandler msgHandler;

    public Client(PeerInfo hostInfo, PeerInfo targetInfo) {
        this.hostInfo = hostInfo;
        this.targetInfo = targetInfo;

        // create a socket to connect to the server
        try {
            Socket requestSocket = new Socket(targetInfo.hostname, Integer.parseInt(targetInfo.port));
            this.msgHandler = new MessageHandler(requestSocket);
            DebugLogger.instance.log("Opened socket to peer %s on %s:%s", targetInfo.peerID, targetInfo.hostname,
                    targetInfo.port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            // Perform handshake
            DebugLogger.instance.log("Sent handshake message to peer %s", targetInfo.peerID);
            msgHandler.sendHandshake(hostInfo.peerID);
            boolean checkHandshake = msgHandler.receiveHandshakeClient(targetInfo.peerID);
            DebugLogger.instance.log("Handshake message received from peer %s, verifying...", targetInfo.peerID);

            if (checkHandshake) {
                DebugLogger.instance.log("Handshake completed with peer %s", targetInfo.peerID);
                FileLogger.instance.logTCPConnectionTo(targetInfo.peerID);

                // Store the message handler
                MessageHandlerList.instance.insertMessageHandler(targetInfo.peerID, msgHandler);

                // Send bitfield message
                DebugLogger.instance.log("Sent bitfield message to peer %s", targetInfo.peerID);
                BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
                MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.BITFIELD,
                        new BitfieldPayload(thisBitfield));

                while (PeerInfoList.instance.checkAllPeersHaveFile() == false) {
                    // Wait for message
                    Message received = msgHandler.receiveMessage();
                    DebugLogger.instance.log("Received %s message from peer %s", received.type.toString(),
                            targetInfo.peerID);

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
                // All peers have the file, exit
                DebugLogger.instance.log("Client exiting...");
            } else {
                DebugLogger.instance.err("Handshake invalid");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close connections
            // TODO: close sockets?
            DebugLogger.instance.log("Successfully closed client connected to peer %s", targetInfo.peerID);
            System.exit(0);
        }
    }

    private void handleBitfieldReceived(Message received) {
        // Store bitfield in list of peers
        BitfieldPayload payload = (BitfieldPayload) received.getPayload();
        PeerInfoList.instance.updatePeerBitfield(targetInfo.listIndex, payload.bitfield);

        // Compare bitfields to check if interested or not
        boolean interested = payload.compare(PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield);
        if (interested) {
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.INTERESTED, new EmptyPayload());
        } else {
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
    }

    /**
     *
     * Updates the target host's bitfield with the index received.
     *
     * @param received the message received, containing a `have` payload.
     */
    private void handleHaveReceived(Message received) {
        HavePayload payload = (HavePayload) received.getPayload();
        // Log
        FileLogger.instance.logHaveMessage(targetInfo.peerID, payload.index);
        // Update bitfield
        PeerInfoList.instance.setPeerBitfieldIndex(targetInfo.peerID, payload.index);
        // Check if we should send an interested message
        List<Integer> interestedIndices = getInterestedIndices();
        if (interestedIndices.size() == 0) {
            // We are not interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.NOT_INTERESTED, new EmptyPayload());
        } else {
            // We are interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.INTERESTED, new EmptyPayload());
        }
    }

    /**
     * Returns a list of indices this peer is interested in.
     *
     * @return list of integers representing the indices.
     */
    private List<Integer> getInterestedIndices() {
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
        return interestedIndices;
    }

    /**
     *
     * Sends a `have` message to all of the connected peers.
     *
     * @param index the index that was successfully received.
     */
    private void sendHaveToConnectedPeers(int index) {
        MessageHandlerList.instance.sendMessagesToAll(MessageType.HAVE, new HavePayload(index));
    }

    private void handlePieceReceived(Message received) {
        // Store piece in data structure
        PiecePayload payload = (PiecePayload) received.getPayload();
        PieceStorage.instance.setPiece(payload.index, payload.data);
        // Update upload rate
        RateTracker.instance.updateRate(targetInfo.peerID, payload.data.length);
        // Update bitfield
        PeerInfoList.instance.setPeerBitfieldIndex(hostInfo.peerID, payload.index);
        // Log
        BitSet thisBitfield = PeerInfoList.instance.getPeer(hostInfo.peerID).bitfield;
        FileLogger.instance.logDownloadPiece(targetInfo.peerID, payload.index, thisBitfield.cardinality());
        if (PeerInfoList.instance.getPeer(hostInfo.peerID).hasFile) {
            DebugLogger.instance.log("This peer has downloaded the whole file");
            FileLogger.instance.logCompletionDownload();
        }
        // Determine next message to send
        List<Integer> interestedIndices = getInterestedIndices();
        // unchoked but they have nothing we want
        if (interestedIndices.size() == 0) {
            // send not interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
        // unchoked and they have things we don't
        else {
            // Send request message with random index
            int randIndex = interestedIndices.get(new Random().nextInt(interestedIndices.size()));
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.REQUEST,
                    new RequestPayload(randIndex));
        }
        // Send "have" message to connected peers
        sendHaveToConnectedPeers(payload.index);
    }

    // need to know which peer sent the message
    private void handleChokeReceived(Message received) {
        // Log
        FileLogger.instance.logChocking(targetInfo.peerID);
        // Check if we are interested
        List<Integer> interestedIndices = getInterestedIndices();
        if (interestedIndices.size() == 0) {
            // We are not interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.NOT_INTERESTED, new EmptyPayload());
        } else {
            // We are interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.INTERESTED, new EmptyPayload());
        }
    }

    private void handleUnchokeReceived(Message received) {
        // Log
        FileLogger.instance.logUnchoking(targetInfo.peerID);
        // Find needed indices
        List<Integer> interestedIndices = getInterestedIndices();
        // unchoked but they have nothing we want
        if (interestedIndices.size() == 0) {
            // send not interested
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.NOT_INTERESTED, new EmptyPayload());
        }
        // unchoked and they have things we don't
        else {
            // Send request message with random index
            int randIndex = interestedIndices.get(new Random().nextInt(interestedIndices.size()));
            MessageHandlerList.instance.sendMessage(targetInfo.peerID, MessageType.REQUEST,
                    new RequestPayload(randIndex));
        }
    }
}
